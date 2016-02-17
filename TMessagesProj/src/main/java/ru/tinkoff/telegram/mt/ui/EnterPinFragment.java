package ru.tinkoff.telegram.mt.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import ru.tinkoff.telegram.mt.R;
import ru.tinkoff.telegram.mt.android.IOrientationLocker;
import ru.tinkoff.telegram.mt.mtpart.AccessManager;
import ru.tinkoff.telegram.mt.mtpart.MtAppPart;
import ru.tinkoff.telegram.mt.network.Network;
import ru.tinkoff.telegram.mt.network.Requests;
import ru.tinkoff.telegram.mt.utils.Utils;
import ru.tinkoff.telegram.mt.views.CustomDigitKeyBoard;
import ru.tinkoff.telegram.mt.views.PinView;

/**
 * @author a.shishkin1
 */


public class EnterPinFragment extends Fragment implements CustomDigitKeyBoard.KeyEventListener, IOrientationLocker, AccessManager.OnAccessStateChangedListener {

    public static String GOAL_CHANGE_PIN = "change_pin";
    public static String GOAL = "goal";

    private IInteraction interaction;

    private PinView pinView;
    private CustomDigitKeyBoard customDigitKeyBoard;
    private TextView timerView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        interaction = (IInteraction) activity;
    }




    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        FrameLayout frameLayout = new FrameLayout(getActivity());
        pinView = new PinView(getActivity());
        pinView.setDescription(getString(R.string.mt_auth_enter_pin));
        timerView = new TextView(getActivity());
        int p = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins(p, p, p, p);
        pinView.setLayoutParams(layoutParams);
        timerView.setLayoutParams(layoutParams);
        timerView.setVisibility(View.GONE);
        timerView.setTextColor(TgR.color.default_text_color);
        frameLayout.addView(timerView);
        frameLayout.addView(pinView);
        linearLayout.addView(frameLayout);

        customDigitKeyBoard = new CustomDigitKeyBoard(getActivity());
        customDigitKeyBoard.setKeyEventListener(this);

        linearLayout.addView(customDigitKeyBoard);

        ((LinearLayout.LayoutParams) frameLayout.getLayoutParams()).weight = 1;

        return linearLayout;
    }



    private void doMobileAuth() {
        String pin = pinView.getValue();
        String pinHash = Utils.md5(pin);
        String oldSession = MtAppPart.getMtAppInstance(getActivity()).getOldSession();
        Network.RequestImpl request = Requests.REQUEST_MOBILE_AUTH.prepare(pinHash, oldSession).buildAnonymousOn(getActivity());
        Bundle args = getArguments();
        if(args != null) {
            Bundle additions = new Bundle();
            additions.putString(GOAL, args.getString(GOAL));
            request.setAdditions(additions);
        }
        interaction.execute(request);
    }

    public void onWrongPinCode() {
        pinView.markError();
        customDigitKeyBoard.setEnabled(true);
    }


    public void onPinAttemptsOverLimit(long finishTime, long currentTime) {
        long delta = finishTime - currentTime;
        MtAppPart.getMtAppInstance(getActivity()).getAccessManager().setState(AccessManager.PIN_ATTEMPTS_OVERLIMIT, delta);
    }

    @Override
    public void onStart() {
        MtAppPart.getMtAppInstance(getActivity()).getAccessManager().setOnAccessStateChangedListener(this);
        super.onStart();
    }

    @Override
    public void onStop() {
        MtAppPart.getMtAppInstance(getActivity()).getAccessManager().setOnAccessStateChangedListener(null);
        super.onStop();
    }

    @Override
    public void onCancel() {
        getActivity().onBackPressed();
    }

    @Override
    public void onCorrect() {
        pinView.removeLast();
    }

    @Override
    public void onKey(int code, Character c) {
        pinView.addChar(c);
        if(pinView.isFill()) {
            customDigitKeyBoard.setEnabled(false);
            doMobileAuth();
        }
    }

    @Override
    public void onAccessStateChanged(AccessManager.State state) {
        if(state.getAccess() == AccessManager.NO_LIMITS) {
            pinView.setVisibility(View.VISIBLE);
            pinView.clear();
            timerView.setVisibility(View.GONE);
        }else if(state.getAccess() == AccessManager.PIN_ATTEMPTS_OVERLIMIT) {
            pinView.setVisibility(View.GONE);
            timerView.setVisibility(View.VISIBLE);
            long time = state.getTime();
            String timeString = timeString(time);
            timerView.setText(timeString);
        }
    }

    private String timeString(long time) {
        return String.format("%s %02d:%02d:%02d",
                getString(R.string.mt_pin_time_string)
                + "\n" +
                getString(R.string.mt_gen_pin_userblockeduntil)
                ,
                TimeUnit.MILLISECONDS.toHours(time) % 24,
                TimeUnit.MILLISECONDS.toMinutes(time) % 60,
                TimeUnit.MILLISECONDS.toSeconds(time) % 60);

    }
}
