package ru.tinkoff.telegram.mt.mtpart;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import org.telegram.messenger.ApplicationLoader;

import java.util.HashSet;
import java.util.Set;

import ru.tinkoff.telegram.mt.Glue;
import ru.tinkoff.telegram.mt.base.IStorage;
import ru.tinkoff.telegram.mt.entities.MtConfig;
import ru.tinkoff.telegram.mt.network.Network;
import ru.tinkoff.telegram.mt.network.Requests;
import ru.tinkoff.telegram.mt.network.parse.DefaultParsers;
import ru.tinkoff.telegram.mt.network.responses.BaseResult;
import ru.tinkoff.telegram.mt.network.responses.ResultCode;
import ru.tinkoff.telegram.mt.utils.Analytics;

/**
 * @author a.shishkin1
 */


public class MtAppPart extends ContextWrapper {

    public static final String MT_PREFERENCES_NAME = "mt_prefs";


    private String myPhone;
    private H handler;
    private Session session;
    private OldSessionStorage oldSessionStorage;
    private MtConfig config;
    private AccessManager accessManager;

    private Async async;

    public MtAppPart(Context context) {
        super(context);
        if (Glue.DEBUG)
            throwIfIncorrectPlace(ApplicationLoader.class, "getMtAppPart");
        this.async = new Async();
        this.handler = new H(this);
        this.oldSessionStorage = new OldSessionStorage(context);
        this.accessManager = new AccessManager();

        Analytics.getInstance(context).init();
    }

    public AccessManager getAccessManager() {
        return accessManager;
    }

    public void setConfig(MtConfig config) {
        this.config = config;
    }

    public MtConfig getConfig() {
        return config;
    }

    public boolean isConfigReady() {
        return config != null;
    }

    public Async getAsync() {
        return async;
    }

    public boolean isAuthorized() {
        return session != null && !session.isAnonymous();
    }

    public synchronized void setSession(Session session) {
        this.session = session;
        handler.removeMessages(H.SESSION_EXPIRE);
        if (isAuthorized()) {
            Message m = handler.obtainMessage(H.SESSION_EXPIRE);
            handler.sendMessageDelayed(m, session.calculateLifeTime());
        } else {
            lastPing = 0L;
        }
    }

    public synchronized void expire() {
        setSession(null);
        for(OnSessionExpire listener : onSessionExpireListeners) {
            listener.onSessionExpire();
        }
    }

    public synchronized String getSessionId() {
        return session == null ? null : session.sessionId;
    }

    public boolean hasOldSession() {
        return oldSessionStorage.restore() != null;
    }

    public String getOldSession() {
        return oldSessionStorage.restore();
    }

    public static synchronized MtAppPart getMtAppInstance(Context context) {
        ApplicationLoader application = (ApplicationLoader) context.getApplicationContext();
        return application.getMtAppPart();
    }


    public void storeSession() {
        oldSessionStorage.store(session.sessionId);
    }

    public void forgetSession() {
        oldSessionStorage.store(null);
    }


    public static class Session {

        private String sessionId;
        private long lifeTime;
        private boolean isAnonimous;

        public Session(String sessionId, long lifeTime, boolean isAuthed) {
            this.sessionId = sessionId;
            this.lifeTime = lifeTime;
            this.isAnonimous = !isAuthed;
        }

        @Override
        public String toString() {
            return sessionId;
        }

        public boolean isAnonymous() {
            return isAnonimous;
        }

        public long calculateLifeTime() {
            return 1000L * lifeTime;
        }

    }


    private Set<OnSessionExpire> onSessionExpireListeners = new HashSet<>();

    public void registerOnSessionExpire(OnSessionExpire onSessionExpire) {
        onSessionExpireListeners.add(onSessionExpire);
    }

    public void unregisterOnSessionExpire(OnSessionExpire onSessionExpire) {
        onSessionExpireListeners.remove(onSessionExpire);
    }

    public interface OnSessionExpire {
        void onSessionExpire();
    }

    private long lastPing = 0L;


    public void pingIfNeed() {
        long now = System.currentTimeMillis();
        if(!isAuthorized() || now - lastPing < session.calculateLifeTime() >> 1) {
            return;
        }
        lastPing = now;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BaseResult res = Network.execute(Requests.REQUEST_PING.prepare().buildOn(MtAppPart.this), DefaultParsers.BASE_RESULT_PARSER);
                    if(res.getResultCode() == ResultCode.OK) {
                        handler.removeMessages(H.SESSION_EXPIRE);
                        Message m = handler.obtainMessage(H.SESSION_EXPIRE);
                        handler.sendMessageDelayed(m, session.calculateLifeTime());
                    }
                } catch (Network.NetworkException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }




    private static class H extends Handler {

        private MtAppPart mtAppPart;

        public H(MtAppPart mtAppPart) {
            this.mtAppPart = mtAppPart;
        }

        public static final int SESSION_EXPIRE = -1;

        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case SESSION_EXPIRE:
                    handleExpire();
                    break;

            }
            super.dispatchMessage(msg);
        }

        private void handleExpire() {
            mtAppPart.expire();
        }
    }


    private static class OldSessionStorage implements IStorage<String> {

        private static final String OLD_SESSION_KEY = "olk";

        private SharedPreferences sp;

        public OldSessionStorage(Context context) {
            this.sp = context.getSharedPreferences(MT_PREFERENCES_NAME, Context.MODE_PRIVATE);
        }

        @Override
        public void store(String s) {
            sp.edit().putString(OLD_SESSION_KEY, s).commit();
        }

        @Override
        public String restore() {
            return sp.getString(OLD_SESSION_KEY, null);
        }
    }

    public void setMyPhone(String myPhone) {
        this.myPhone = myPhone;
    }

    public String getMyPhone() {
        return myPhone;
    }

    public H getHandler() {
        return handler;
    }

    private static void throwIfIncorrectPlace(Class cl, String method) {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        /*
        0 getThreadStackTrace
        1 getStackTrace
        2 throwIfIncorrectPlace
        3 [interest]
        4 [place]
        */
        StackTraceElement prev = ste[4];
        if (prev.getMethodName().equals(method) && prev.getClassName().equals(cl.getName()))
            return;
        throw new RuntimeException(ste[3].getClassName() + "." + ste[3].getMethodName() + " can used in " + cl.getName() + "." + method + " only");

    }




}
