package ru.tinkoff.telegram.mt.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.tinkoff.telegram.mt.R;

import ru.tinkoff.telegram.mt.Glue;
import ru.tinkoff.telegram.mt.android.IBackPressable;
import ru.tinkoff.telegram.mt.mtpart.MtAppPart;
import ru.tinkoff.telegram.mt.network.Network;
import ru.tinkoff.telegram.mt.network.Requests;
import ru.tinkoff.telegram.mt.network.parse.JsonableParser;
import ru.tinkoff.telegram.mt.network.responses.BaseResult;
import ru.tinkoff.telegram.mt.network.responses.WaitingConfirmationResponse;
import ru.tinkoff.telegram.mt.utils.Utils;

/**
 * @author a.shishkin1
 */


public class EnterSmsFragment extends Fragment implements TextWatcher, IBackPressable {

    private EditText etSms;
    private TextView tvDescription;
    private TextView tvResend;
    private IInteraction interaction;
    private WaitingConfirmationResponse waitingConfirmationResponse;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        interaction = (IInteraction)activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context = getActivity();
        waitingConfirmationResponse = getArguments().getParcelable(WaitingConfirmationResponse.CONFIRMATION_EXTRA);

        int measure = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics());
        LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lllp.setMargins(measure, measure, measure, measure);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        etSms = new EditText(context);
        etSms.addTextChangedListener(this);
        etSms.setBackgroundResource(R.drawable.edit_text);
        etSms.setFilters(new InputFilter[]{new InputFilter.LengthFilter(waitingConfirmationResponse.getConfirmCodeLength())});
        etSms.setLayoutParams(lllp);
        etSms.requestFocus();

        etSms.setHint(getString(R.string.mt_sms_hint));
        etSms.setInputType(InputType.TYPE_CLASS_NUMBER);

        tvDescription = new TextView(context);
        tvDescription.setLayoutParams(lllp);
        tvDescription.setText(getDescriptionLabelText());
        tvDescription.setTextColor(0xffd1d1d1);

        int hm = measure >> 1;
        LinearLayout.LayoutParams lllp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lllp2.setMargins(measure, hm, measure, hm);
        tvResend = new TextView(context);
        tvResend.setLayoutParams(lllp2);
        tvResend.setText(R.string.mt_resend_code);
        tvResend.setPadding(0, hm, 0, hm);
        tvResend.setTextColor(Utils.createBlueTextListColor());
        tvResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // answer not interesting here
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String operationTicket = waitingConfirmationResponse.getOperationTicket();
                        String confirmationType = waitingConfirmationResponse.getConfirmationType().getName();
                        String initialOperation = waitingConfirmationResponse.getInitialOperation();
                        Network.IRequest req = Requests.REQUEST_RESEND_CODE.prepare(operationTicket, confirmationType, initialOperation).buildOn(getActivity());
                        try {
                            Network.execute(req, new JsonableParser<BaseResult>(BaseResult.class));
                        } catch (Network.NetworkException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });

        layout.addView(tvDescription);
        layout.addView(etSms);
        layout.addView(tvResend);

        return layout;
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(Glue.DEBUG && !Network.isProdApi()) {
            MtAppPart mtAppPart = MtAppPart.getMtAppInstance(getActivity());
            Network.RequestImpl request = Requests.REQUEST_GET_CONFIRMATION_CODE.prepare(Network.X_USER, waitingConfirmationResponse.getOperationTicket(), mtAppPart.getSessionId()).build();
            request.setXApi(true);
            interaction.execute(request);
        }
    }



    private String getDescriptionLabelText() {
        return getString(R.string.mt_enter_sms_label);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String candidate = etSms.getText().toString();
        if(candidate.length() == waitingConfirmationResponse.getConfirmCodeLength()) {
            sendSecretCode(candidate);
            Activity activity = getActivity();
            View view = activity.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private void sendSecretCode(String sms) {

        Network.RequestImpl request = Requests.REQUEST_CONFIRM
                .prepare(sms,
                        waitingConfirmationResponse.getConfirmationType().getName(),
                        waitingConfirmationResponse.getOperationTicket(),
                        waitingConfirmationResponse.getInitialOperation())
                .buildOn(getActivity());
        request.setArgument(waitingConfirmationResponse.getAction());
        request.setAdditions(waitingConfirmationResponse.getAdditions());
        interaction.execute(request);
    }

    public void showCode(String code) {
        etSms.setHint(code);
    }

    @Override
    public void onBackPress() {
        getActivity().finish();
    }
}

