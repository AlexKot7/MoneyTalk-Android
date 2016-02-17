package ru.tinkoff.telegram.mt.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;

import org.json.JSONException;
import org.json.JSONObject;

import ru.tinkoff.telegram.mt.R;
import ru.tinkoff.telegram.mt.network.Network;
import ru.tinkoff.telegram.mt.network.Requests;
import ru.tinkoff.telegram.mt.network.responses.WaitingConfirmationResponse;
import ru.tinkoff.telegram.mt.views.ThreeDsView;

/**
 * @author a.shishkin1
 */


public class ThreeDsFragment extends Fragment implements ThreeDsView.Callback {

    public static String EXTRA_REDIRECT_URL = "redirect_url";

    private ThreeDsView webView;
    private IInteraction interaction;
    private WaitingConfirmationResponse waitingConfirmationResponse;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        interaction = (IInteraction) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        waitingConfirmationResponse = getArguments().getParcelable(WaitingConfirmationResponse.CONFIRMATION_EXTRA);
        webView = new ThreeDsView(getActivity());
        webView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        webView.setRedirectUrl(getArguments().getString(EXTRA_REDIRECT_URL));
        webView.setCallback(this);
        return webView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        webView.start(waitingConfirmationResponse.getUrl(), waitingConfirmationResponse.getMerchantData(), waitingConfirmationResponse.getRequestSecretCode());
    }

    @Override
    public void onReceivedSslError(Context context, final SslErrorHandler handler, SslError error) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.mt_error)
                .setMessage(context.getString(R.string.mt_untrusted_certificate))
                .setPositiveButton(context.getString(R.string.mt_continue), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.proceed();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(context.getString(R.string.mt_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.cancel();
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    public void onPaResReady(String paRes) {
        JSONObject confirmationData = new JSONObject();

        try {
            confirmationData.putOpt(waitingConfirmationResponse.getConfirmationType().getName(), paRes);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Network.RequestImpl request = Requests.REQUEST_CONFIRM.prepare(paRes,
                waitingConfirmationResponse.getConfirmationType().getName(),
                waitingConfirmationResponse.getOperationTicket(),
                waitingConfirmationResponse.getInitialOperation())
                .addParameter("confirmationData", confirmationData.toString())
                .buildOn(getActivity());
        request.setArgument(waitingConfirmationResponse.getAction());
        request.setAdditions(waitingConfirmationResponse.getAdditions());
        interaction.execute(request);
    }
}
