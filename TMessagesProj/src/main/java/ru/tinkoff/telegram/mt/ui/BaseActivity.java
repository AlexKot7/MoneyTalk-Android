package ru.tinkoff.telegram.mt.ui;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.ActionBar;

import ru.tinkoff.telegram.mt.BuildConfig;
import ru.tinkoff.telegram.mt.InvalidRequestDataException;
import ru.tinkoff.telegram.mt.R;

import java.util.ArrayList;

import ru.tinkoff.telegram.mt.entities.Card;
import ru.tinkoff.telegram.mt.entities.Commission;
import ru.tinkoff.telegram.mt.entities.MtConfig;
import ru.tinkoff.telegram.mt.mtpart.Async;
import ru.tinkoff.telegram.mt.mtpart.MtAppPart;
import ru.tinkoff.telegram.mt.network.IApiCallback;
import ru.tinkoff.telegram.mt.network.INetworkCallback;
import ru.tinkoff.telegram.mt.network.Network;
import ru.tinkoff.telegram.mt.network.responses.BaseResult;
import ru.tinkoff.telegram.mt.network.responses.ResultCode;
import ru.tinkoff.telegram.mt.network.responses.WaitingConfirmationResponse;
import ru.tinkoff.telegram.mt.utils.Analytics;

/**
 * @author a.shishkin1
 */


public abstract class BaseActivity extends Activity implements INetworkCallback, IInteraction {

    private MtAppPart mtAppPart;
    private Async async;
    private ContextThemeWrapper themeWrapper;

    public static final String ACTION_SEND_MONEY = "ru.tinkoff.telegramimp.SEND_MONEY";
    public static final String ACTION_SETTINGS = "ru.tinkoff.telegramimp.SETTINGS";

    public static final String EXTRA_NAME = "extra_my_name";
    public static final String EXTRA_PHONE = "extra_my_phone";
    public static final String EXTRA_USERS = "extra_users";
    public static final String EXTRA_MONEY = "extra_money";
    public static final String EXTRA_CARD_ID = "extra_card_id";
    public static final String EXTRA_ACTION_BAR_HEIGHT = "extra_action_bar_height";

    private FrameLayout contentFrame;
    private FrameLayout actionBarFrame;
    private int abHeight;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        themeWrapper = new ContextThemeWrapper(this, R.style.Theme_TMessages);
        mtAppPart = MtAppPart.getMtAppInstance(this);
        async = mtAppPart.getAsync();
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        contentFrame = new FrameLayout(this);
        actionBarFrame = new FrameLayout(this);

        abHeight = getIntent().getIntExtra(EXTRA_ACTION_BAR_HEIGHT, 0);
        if (abHeight == 0) {
            TypedValue tv = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
            abHeight = getResources().getDimensionPixelSize(tv.resourceId);
        }
        LinearLayout.LayoutParams lpActionBar = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, abHeight);
        LinearLayout.LayoutParams lpContent = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        lpContent.weight = 1;

        actionBarFrame.setLayoutParams(lpActionBar);
        actionBarFrame.setBackgroundColor(TgR.color.main_theme_color);

        contentFrame.setLayoutParams(lpContent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(TgR.color.main_theme_color);
        }

        linearLayout.addView(actionBarFrame);
        linearLayout.addView(contentFrame);

        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        initActionBar(actionBarFrame);

        super.setContentView(linearLayout);
    }

    protected void initActionBar(FrameLayout container) {
        LinearLayout llContainer = new LinearLayout(this);
        llContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ImageView backButton = new ImageView(this);
        backButton.setBackgroundResource(R.drawable.bar_selector);
        backButton.setScaleType(ImageView.ScaleType.CENTER);
        backButton.setImageResource(R.drawable.ic_ab_back);
        int bbWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 54, getResources().getDisplayMetrics());
        backButton.setLayoutParams(new LinearLayout.LayoutParams(bbWidth, abHeight));
        llContainer.addView(backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // argh!
            }
        });
        tvTitle = new TextView(this);
        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, abHeight));
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tvTitle.setGravity(Gravity.CENTER_VERTICAL);
        tvTitle.setTypeface(tvTitle.getTypeface(), Typeface.BOLD);
        int pl = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        tvTitle.setPadding(pl, 0, pl, 0);
        llContainer.addView(tvTitle);
        container.addView(llContainer);
    }


    @Override
    public void setTitle(CharSequence title) {
        tvTitle.setText(title);
    }

    @Override
    public void setTitle(int titleId) {
        tvTitle.setText(getString(titleId));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("action_bar_title", tvTitle.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvTitle.setText(savedInstanceState.getString("action_bar_title"));
    }

    @Override
    public void setContentView(View view) {
        contentFrame.removeAllViews();
        contentFrame.addView(view);
    }

    @Override
    public void setContentView(int layoutResID) {
        contentFrame.removeAllViews();
        LayoutInflater.from(this).inflate(layoutResID, contentFrame, true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        async.registerCallback(this);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_DOWN) {
            mtAppPart.pingIfNeed();
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onStop() {
        super.onStop();
        async.unregisterCallback(this);
    }

    @Override
    public void savePendingRequest(Network.RequestImpl req, String key) {
        async.save(req, key);
    }

    @Override
    public boolean executePendingRequest(String key) {
        return async.executePendingRequest(key);
    }

    @Override
    public void execute(Network.RequestImpl request) {
        async.executeRequest(request);
    }

    @Override
    public void execute(Network.RequestImpl... requests) {
        async.executeRequests(requests);
    }

    @Override
    public void handleSignUp(BaseResult res) {
        JSONObject jsonSession = res.getSource().optJSONObject("payload");
        if(jsonSession != null) {
            mtAppPart.setSession(new MtAppPart.Session(jsonSession.optString("sessionid"), jsonSession.optLong("sessionTimeout"), true));
            showSetPin();
        }
    }

    @Override
    public void handleSession(BaseResult res) {
        mtAppPart.setSession(new MtAppPart.Session(res.getSource().optString("payload"), 300, false)); // 300 sec (as 5 mintes life time)
        showRegistration();
    }

    @Override
    public void handleMobileSavePin(BaseResult res) {
        mtAppPart.storeSession();
        Bundle args = res.getAdditions();
        if(args != null && SetPinFragment.GOAL_CHANGE_PIN.equals(args.getString(SetPinFragment.GOAL))) {
            onChangePin();
        } else {
            resolveAction();
        }
    }

    @Override
    public void handleMobileAuth(BaseResult res) {
        JSONObject jsonSession = res.getSource().optJSONObject("payload");
        if(jsonSession != null) {
            mtAppPart.setSession(new MtAppPart.Session(jsonSession.optString("sessionid"), jsonSession.optLong("sessionTimeout"), true));
            mtAppPart.storeSession();
            Bundle args = res.getAdditions();
            if (args != null && EnterPinFragment.GOAL_CHANGE_PIN.equals(args.getString(EnterPinFragment.GOAL))) {
                showChangePin();
            } else{
                resolveAction();
            }
        }
    }




    @Override
    public void handleGetConfirmationCode(BaseResult res) {
        JSONObject jobj = res.getSource();
        onGetConfirmationCode(jobj.optString("payload"));
    }


    @Override
    public void handleTransferAnyCardToAnyPointer(BaseResult res) {
        String money = null;
        String cardId = null;
        Bundle bundle = res.getAdditions();
        if (bundle != null) {
            money = bundle.getString(EXTRA_MONEY);
            cardId = bundle.getString(EXTRA_CARD_ID);
        }
        Analytics.getInstance(this).trackSuccessfulPayment(res.getSource());
        onSuccessSendMoney(money, cardId);
    }

    @Override
    public void handleTransferAnyCardToAnyCard(BaseResult res) {
        String money = null;
        String cardId = null;
        Bundle bundle = res.getAdditions();
        if (bundle != null) {
            money = bundle.getString(EXTRA_MONEY);
            cardId = bundle.getString(EXTRA_CARD_ID);
        }
        Analytics.getInstance(this).trackSuccessfulPayment(res.getSource());
        onSuccessSendMoney(money, cardId);
    }

    @Override
    public void handleAttachCard(BaseResult res) {
        Card card = new Card();
        JSONObject jobj = res.getSource().optJSONObject("payload");
        if(jobj != null) {
            jobj = jobj.optJSONObject("card");
            card.fillByJson(jobj);
            onAttachCard(card);
        }

    }

    @Override
    public void handleDetachCard(BaseResult res) {

    }

    @Override
    public void handleAccountsFlat(BaseResult res) {
        ArrayList<Card> cards = new ArrayList<>();
        JSONArray jsonAccounts = res.getSource().optJSONArray("payload");
        if(jsonAccounts != null) {
            for(int i = 0; i < jsonAccounts.length(); i++) {
                JSONObject jaccount = jsonAccounts.optJSONObject(i);
                if(!"ExternalAccount".equals(jaccount.optString("accountType"))) // only external accounts interested
                    continue;
                JSONArray jcards = jaccount.optJSONArray("cardNumbers");
                if(jcards != null) {
                    for(int j = 0; j < jcards.length(); j++) {
                        JSONObject jcard = jcards.optJSONObject(j);
                        Card card = new Card();
                        card.fillByJson(jcard);
                        cards.add(card);
                    }
                }
            }
        }
        onCardsReady(cards);
    }


    @Override
    public void handleSetLinkedCardPrimary(BaseResult res) {
        String cardId  = null;
        Bundle bundle = res.getAdditions();
        if(bundle != null) {
            cardId = bundle.getString(EXTRA_CARD_ID);
        }
        onSetLinkedCardPrimary(cardId);
    }

    @Override
    public void handlePaymentCommission(BaseResult res) {
        if(res.getResultCode() == ResultCode.OK) {
            Commission commission = new Commission();
            JSONObject jobj = res.getSource();
            JSONObject jcommission = jobj.optJSONObject("payload");
            if (jcommission != null) {
                commission.fillByJson(jcommission);
            }
            onCommission(commission);
        } else  {
            onCommission(null);
        }
    }


    @Override
    public void handlePaymentReceipt(BaseResult res) {

    }


    @Override
    public void handleNow(BaseResult res) {

    }

    @Override
    public void handlePing(BaseResult res) {

    }


    @Override
    public void handleConfirm(BaseResult res) {
        res.setWhat(res.getArgument());
        IApiCallback.NOTIFY.notify(this, res);
    }

    @Override
    public void handleConfig(BaseResult res) {
        JSONObject jobj = res.getSource().optJSONObject("payload");
        MtConfig config = new MtConfig(jobj);
        mtAppPart.setConfig(config);
        resolveAuthorization();

    }

    @Override
    public void handleResetWallet(BaseResult res) {
        mtAppPart.expire();
        mtAppPart.forgetSession();
        finish();
    }

    @Override
    public void handleResendCode(BaseResult res) {

    }

    @Override
    public void handleNotAuthenticatedException(BaseResult res) {
        showRegistration();
    }

    @Override
    public void handleWaitingConfirmationException(BaseResult res) {
        WaitingConfirmationResponse waitingConfirmationResponse = new WaitingConfirmationResponse(res.getWhat(), res.getAdditions());
        waitingConfirmationResponse.fillByJson(res.getSource());
        showConfirmation(waitingConfirmationResponse);
    }

    @Override
    public void handleDeviceLinkNeededException(BaseResult res) {
        BaseResult.Error error = res.asError(this);
        showDefaultErrorMessage(error.title, error.detail, error.trackingId);
        mtAppPart.forgetSession();
        resolveAuthorization();
    }

    @Override
    public void handleInsufficientPrivilegesException(BaseResult res) {
        resolveAuthorization();
    }

    @Override
    public void handleInvalidRequestDataException(BaseResult res) {
        final Exception e = new InvalidRequestDataException("What = " + res.getWhat());
        Analytics.getInstance(this).trackFatalException(e);
    }

    @Override
    public void handleInvalidPasswordException(BaseResult res) {
        BaseResult.Error error = res.asError(this);
        showDefaultErrorMessage(error.title, error.detail, error.trackingId);
    }

    @Override
    public void handleInternalErrorException(BaseResult res) {
        BaseResult.Error error = res.asError(this);
        showDefaultErrorMessage(error.title, error.detail, error.trackingId);
    }

    @Override
    public void handleAuthenticationFailedException(BaseResult res) {
        BaseResult.Error error = res.asError(this);
        showDefaultErrorMessage(error.title, error.detail, error.trackingId);
    }

    @Override
    public void handleConfirmationFailedException(BaseResult res) {
        BaseResult.Error error = res.asError(this);
        showDefaultErrorMessage(error.title, error.detail, error.trackingId);
    }

    @Override
    public void handleConfirmationExpiredException(BaseResult res) {
        BaseResult.Error error = res.asError(this);
        showDefaultErrorMessage(error.title, error.detail, error.trackingId);
    }

    @Override
    public void handleWrongConfirmationCodeException(BaseResult res) {
        BaseResult.Error error = res.asError(this);
        showDefaultErrorMessage(error.title, error.detail, error.trackingId);
    }

    @Override
    public void handleResendFailedException(BaseResult res) {
        BaseResult.Error error = res.asError(this);
        showDefaultErrorMessage(error.title, error.detail, error.trackingId);
    }

    @Override
    public void handleWrongOperationTicketException(BaseResult res) {
        BaseResult.Error error = res.asError(this);
        showDefaultErrorMessage(error.title, error.detail, error.trackingId);
    }

    @Override
    public void handleNoDataFoundException(BaseResult res) {
        BaseResult.Error error = res.asError(this);
        showDefaultErrorMessage(error.title, error.detail, error.trackingId);
    }

    @Override
    public void handleOperationRejectedException(BaseResult res) {
        BaseResult.Error error = res.asError(this);
        showDefaultErrorMessage(error.title, error.detail, error.trackingId);
    }

    @Override
    public void handlePinIsNotSetException(BaseResult res) {
        BaseResult.Error error = res.asError(this);
        showDefaultErrorMessage(error.title, error.detail, error.trackingId);
    }

    @Override
    public void handleWrongPinCodeException(BaseResult res) {
        onWrongPinCode();
    }

    @Override
    public void handlePinAttempsExceededException(BaseResult res) {
        BaseResult.Error error = res.asError(this);
        showDefaultErrorMessage(error.title, error.detail, error.trackingId);
        JSONObject jpayload = res.getSource().optJSONObject("payload");
        if(jpayload != null) {
            try {
                long finishTime = jpayload.optJSONObject("blockedUntil").optLong("milliseconds");
                long nowTime = jpayload.optJSONObject("now").optLong("milliseconds");
                onPinAttemptsOverLimit(finishTime, nowTime);
            } catch (NullPointerException ex) {
                //not expected
                final RuntimeException e = new RuntimeException("Incorrect pin attempts event format", ex);
                Analytics.getInstance(this).trackFatalException(e);
                throw e;
            }
        }
    }

    @Override
    public void handleDeviceAlreadyLinkedException(BaseResult res) {
        BaseResult.Error error = res.asError(this);
        showDefaultErrorMessage(error.title, error.detail, error.trackingId);
    }

    @Override
    public void handleRequestRateLimitExceededException(BaseResult res) {
        BaseResult.Error error = res.asError(this);
        showDefaultErrorMessage(error.title, error.detail, error.trackingId);
    }

    @Override
    public void handleUserLockedException(BaseResult res) {
        BaseResult.Error error = res.asError(this);
        showDefaultErrorMessage(error.title, getString(R.string.mt_account_blocked_info), null, true);
    }

    @Override
    public void handleRoleEscalationException(BaseResult res) {
        BaseResult.Error error = res.asError(this);
        showDefaultErrorMessage(error.title, error.detail, error.trackingId);
    }

    @Override
    public void handleBankServiceDisabledException(BaseResult res) {
        BaseResult.Error error = res.asError(this);
        showDefaultErrorMessage(error.title, error.detail, error.trackingId);
    }

    @Override
    public void handleRoleNotGrantedException(BaseResult res) {
        BaseResult.Error error = res.asError(this);
        showDefaultErrorMessage(error.title, error.detail, error.trackingId);
    }

    @Override
    public void handleTokenExpiredException(BaseResult res) {
        BaseResult.Error error = res.asError(this);
        showDefaultErrorMessage(error.title, error.detail, error.trackingId);
    }

    @Override
    public void handleNotIdentificatedException(BaseResult res) {
        BaseResult.Error error = res.asError(this);
        showDefaultErrorMessage(error.title, error.detail, error.trackingId);
    }


    @Override
    public void onException(Exception exception) {
        boolean closeOnAction = (exception instanceof Network.NetworkException);
        showDefaultErrorMessage(getString(R.string.mt_error), getString(R.string.mt_bad_network), null, closeOnAction);
    }
}
