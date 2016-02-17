package ru.tinkoff.telegram.mt.mtpart;

import android.os.Handler;
import android.os.Message;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import ru.tinkoff.telegram.mt.network.IApiCallback;
import ru.tinkoff.telegram.mt.network.INetworkCallback;
import ru.tinkoff.telegram.mt.network.Network;
import ru.tinkoff.telegram.mt.network.parse.DefaultParsers;
import ru.tinkoff.telegram.mt.network.parse.JsonableParser;
import ru.tinkoff.telegram.mt.network.responses.BaseResult;

/**
 * @author a.shishkin1
 */


public class Async extends Handler {

    private static final int CORRECT_RESULT = 0;
    private static final int EXCEPTION = 1;
    private static final int SHOW_DIALOG = 2;

    private static BaseResult execute(Network.RequestImpl request) throws Network.NetworkException {
        BaseResult baseResult = Network.execute(request, DefaultParsers.BASE_RESULT_PARSER);
        baseResult.setWhat(request.what);
        baseResult.setAdditions(request.getAdditions());
        baseResult.setArgument(request.getArgument());
        baseResult.setSpecificDispatch(request.isNeedSpecificDispatch());
        return baseResult;
    }


    private HashSet<INetworkCallback> callbacks = new HashSet<>();

    private Map<String, Network.RequestImpl> pending = new HashMap<>();

    public void save(Network.RequestImpl req, String key) {
        pending.put(key, req);
    }

    public boolean executePendingRequest(String key) {
        Network.RequestImpl req = pending.get(key);
        if (req != null) {
            executeRequest(req);
            pending.remove(key);
            return true;
        }
        return false;
    }

    @Override
    public void handleMessage(Message msg) {
        if (callbacks.size() == 0) {
            return;
        }

        if (msg.what == SHOW_DIALOG) {
            for (INetworkCallback callback : callbacks) {
                callback.onNetworkDialogRequested();
            }
            return;
        }

        if (msg.what == CORRECT_RESULT) {
            for (INetworkCallback callback : callbacks) {
                IApiCallback.NOTIFY.notify(callback, (BaseResult) msg.obj);
            }
        } else if (msg.what == EXCEPTION) {
            for (INetworkCallback callback : callbacks) {
                callback.onException((Exception) msg.obj);
            }
        }
        for (INetworkCallback callback : callbacks) {
            callback.onNetworkDialogDismissRequested();
        }
    }


    public void executeRequest(final Network.RequestImpl request) { //TODO think about it later
        new Thread(new Runnable() {
            @Override
            public void run() {

                long delay = request.getDelay();

                if (request.isShowDialog()) {
                    final Message m = obtainMessage(SHOW_DIALOG);
                    m.sendToTarget();
                }

                if (delay > 0) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    BaseResult result = execute(request);
                    Message m = obtainMessage(CORRECT_RESULT, result);
                    m.sendToTarget();
                } catch (Network.NetworkException e) {
                    Message m = obtainMessage(EXCEPTION, e);
                    m.sendToTarget();
                }

            }
        }).start();
    }


    public void executeRequests(Network.RequestImpl[] requests) {
        final Network.RequestImpl[] r = new Network.RequestImpl[requests.length];
        System.arraycopy(requests, 0, r, 0, requests.length);
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Network.RequestImpl request : r) {
                    long delay = request.getDelay();

                    if (delay > 0) {
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    try {
                        BaseResult result = execute(request);
                        Message m = obtainMessage(CORRECT_RESULT, result);
                        m.sendToTarget();
                    } catch (Network.NetworkException e) {
                        Message m = obtainMessage(EXCEPTION, e);
                        m.sendToTarget();
                    }
                }
            }
        }).start();
    }


    public void registerCallback(INetworkCallback callback) {
        callbacks.add(callback);
    }

    public void unregisterCallback(INetworkCallback callback) {
        callbacks.remove(callback);
    }

    public interface IRequestExecutor {
        void execute(Network.RequestImpl request);

        void execute(Network.RequestImpl... request);

        void savePendingRequest(Network.RequestImpl req, String key);

        boolean executePendingRequest(String key);
    }


}
