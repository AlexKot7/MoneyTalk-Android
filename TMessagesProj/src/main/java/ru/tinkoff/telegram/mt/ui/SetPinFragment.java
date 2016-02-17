package ru.tinkoff.telegram.mt.ui;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.tinkoff.telegram.mt.R;
import ru.tinkoff.telegram.mt.android.IOrientationLocker;
import ru.tinkoff.telegram.mt.mtpart.MtAppPart;
import ru.tinkoff.telegram.mt.network.Network;
import ru.tinkoff.telegram.mt.network.Requests;
import ru.tinkoff.telegram.mt.utils.Utils;
import ru.tinkoff.telegram.mt.views.CustomDigitKeyBoard;
import ru.tinkoff.telegram.mt.views.PinView;

/**
 * @author a.shishkin1
 */


public class SetPinFragment extends Fragment implements CustomDigitKeyBoard.KeyEventListener, IOrientationLocker {


    public static String GOAL_CHANGE_PIN = "change_pin";
    public static String GOAL = "goal";

    private IInteraction interaction;

    private PinView pinView;
    private PinView pinViewConfirm;
    private CustomDigitKeyBoard customDigitKeyBoard;
    private PinView current;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        interaction = (IInteraction) activity;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context = getActivity();
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        FrameLayout frameLayout = new FrameLayout(getActivity());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;

        LinearLayout linearLayoutCenter = new LinearLayout(context);
        linearLayoutCenter.setLayoutParams(layoutParams);
        linearLayoutCenter.setOrientation(LinearLayout.VERTICAL);

        pinView = new PinView(context);
        pinView.setDescription(getString(R.string.mt_auth_set_pin));
        pinViewConfirm = new PinView(context);
        pinViewConfirm.setDescription(getString(R.string.mt_auth_repeat_pin));
        current = pinView;
        pinViewConfirm.setAlpha(0);

        linearLayoutCenter.addView(pinView);
        linearLayoutCenter.addView(pinViewConfirm);

        frameLayout.addView(linearLayoutCenter);
        linearLayout.addView(frameLayout);

        initOfertaTextView(frameLayout);

        customDigitKeyBoard = new CustomDigitKeyBoard(getActivity());
        customDigitKeyBoard.setKeyEventListener(this);

        linearLayout.addView(customDigitKeyBoard);

        ((LinearLayout.LayoutParams) frameLayout.getLayoutParams()).weight = 1;

        return linearLayout;
    }


    private void initOfertaTextView(FrameLayout frameLayout) {
        TextView textView = new TextView(frameLayout.getContext());
        textView.setMovementMethod(new LinkMovementMethod());
        String url = MtAppPart.getMtAppInstance(getActivity()).getConfig().getString("mtEula.ofertaUrl");
        String text = String.format("%s<br><a href=\"%s\">%s</a>", getString(R.string.mt_gen_oferta_auth_text), url , getString(R.string.mt_gen_oferta_conditions_link));
        textView.setText(Html.fromHtml(text));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        lp.setMargins(margin, margin, margin, margin);
        lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        textView.setLayoutParams(lp);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(TgR.color.default_text_color);
        textView.setLinkTextColor(TgR.color.default_text_color);
        frameLayout.addView(textView);
    }


    public void setPin(String pin) {
        Network.RequestImpl request = Requests.REQUEST_MOBILE_SAVE_PIN.prepare(Utils.md5(pin), null).buildOn(getActivity());
        Bundle fragmentArgs = getArguments();
        Bundle args = new Bundle();
        args.putString(GOAL, fragmentArgs != null ? fragmentArgs.getString(GOAL, null) : null);
        interaction.execute(request);
    }

    @Override
    public void onCancel() {
        getActivity().finish();
    }

    @Override
    public void onCorrect() {
        if(current.isEmpty() && current == pinViewConfirm) {
            current = pinView;
            hideRepeat();
        }
        current.removeLast();
    }

    @Override
    public void onKey(int code, Character c) {
        current.addChar(c);
        if(current.isFill()) {
            if(current == pinView) {
                showRepeat();
                current = pinViewConfirm;
            } else {
                String repeatCode = current.getValue();
                String source = pinView.getValue();

                if(repeatCode.equals(source)) {
                    setPin(source);
                } else {
                    pinViewConfirm.markError(new Runnable() {
                        @Override
                        public void run() {
                            hideRepeat();
                        }
                    });
                    pinView.markError();
                    pinView.clear();
                    current = pinView;
                }
            }
        }
    }

    private void showRepeat() {
        ObjectAnimator alphaForRepeat = ObjectAnimator.ofFloat(pinViewConfirm, "alpha", 0f, 1f );
        alphaForRepeat.setDuration(600);
        alphaForRepeat.start();
    }

    private void hideRepeat() {
        ObjectAnimator alphaForRepeat = ObjectAnimator.ofFloat(pinViewConfirm, "alpha", 1f, 0f );
        alphaForRepeat.setDuration(300);
        alphaForRepeat.setInterpolator(new DecelerateInterpolator());
        alphaForRepeat.start();
    }

}
