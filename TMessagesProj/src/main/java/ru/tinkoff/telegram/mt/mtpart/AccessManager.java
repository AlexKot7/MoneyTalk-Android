package ru.tinkoff.telegram.mt.mtpart;

import android.os.Handler;

/**
 * @author a.shishkin1
 */


public class AccessManager {

    private static final long INFINITY = -1;

    public static final int NO_LIMITS = 0;
    public static final int PIN_ATTEMPTS_OVERLIMIT = 1;
    public static final int BAN = 2;



    private State state;
    private Handler h = new Handler();


    public AccessManager() {
        this.state = new State(NO_LIMITS, INFINITY);
    }

    private OnAccessStateChangedListener onAccessStateChangedListener;

    public void setOnAccessStateChangedListener(OnAccessStateChangedListener onAccessStateChangedListener) {
        this.onAccessStateChangedListener = onAccessStateChangedListener;
    }


    public void setState(int accessLevel, long time) {
        this.state.access = time > 0 ? accessLevel : NO_LIMITS;
        this.state.time = time;
        if(onAccessStateChangedListener != null) {
            onAccessStateChangedListener.onAccessStateChanged(state);
        }
        if(time > 0) {
            h.postDelayed(tick, 1000L);
        }
    }

    public class State {
        private long time;
        private int access;

        private State(int access,long time) {
            this.time = time;
            this.access = access;
        }

        private void setTime(long time) {
            this.time = time;
        }

        private void setAccess(int access) {
            this.access = access;
        }

        public int getAccess() {
            return access;
        }

        public long getTime() {
            return time;
        }
    }


    public interface OnAccessStateChangedListener {
        void onAccessStateChanged(State state);
    }


    private Runnable tick = new Runnable() {
        @Override
        public void run() {
            long newTime = state.time - 1000L;
            setState(state.access, newTime);
        }
    };

}
