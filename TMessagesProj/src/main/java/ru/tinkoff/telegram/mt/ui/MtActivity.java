package ru.tinkoff.telegram.mt.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import ru.tinkoff.telegram.mt.R;

import java.util.ArrayList;
import java.util.List;

import ru.tinkoff.telegram.mt.NotImplementedYetException;
import ru.tinkoff.telegram.mt.android.IOrientationLocker;
import ru.tinkoff.telegram.mt.entities.Card;
import ru.tinkoff.telegram.mt.entities.Commission;
import ru.tinkoff.telegram.mt.entities.User;
import ru.tinkoff.telegram.mt.android.IBackPressable;
import ru.tinkoff.telegram.mt.mtpart.MtAppPart;
import ru.tinkoff.telegram.mt.network.Network;
import ru.tinkoff.telegram.mt.network.Requests;
import ru.tinkoff.telegram.mt.network.params.CommissionParams;
import ru.tinkoff.telegram.mt.network.params.SrcCardParams;
import ru.tinkoff.telegram.mt.network.responses.ConfirmationType;
import ru.tinkoff.telegram.mt.network.responses.WaitingConfirmationResponse;
import ru.tinkoff.telegram.mt.utils.Analytics;


/**
 * @author a.shishkin1
 */


public class MtActivity extends BaseActivity implements MtAppPart.OnSessionExpire {

    public static final String ATTACH_CARD_TAG = "attach_card";
    public static final String AUTHORIZED = "authorized";


    private MtAppPart mtAppPart;
    private String myPhone;
    private User recipient = null;
    private Dialog currentDialog;
    private boolean isPausedAsAuthorized;

    private static final int content_frame_id = R.id.content_frame;


    private ProgressDialog networkProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_activity);
        isPausedAsAuthorized = savedInstanceState != null && savedInstanceState.getBoolean(AUTHORIZED);
        mtAppPart = MtAppPart.getMtAppInstance(this);
        myPhone = getIntent().getStringExtra(EXTRA_PHONE);

        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getFragmentManager().getBackStackEntryCount() != 0) {
                    Fragment fragment = getFragmentManager().findFragmentById(content_frame_id);
                    resolveOrientation(fragment);
                }
            }
        });

        if(savedInstanceState == null) {
            if (mtAppPart.isAuthorized()) {
                resolveAction();
            } else {
                if(mtAppPart.isConfigReady()) {
                    resolveAuthorization();
                } else {
                    downloadConfig();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isPausedAsAuthorized && !mtAppPart.isAuthorized()) {
            if(mtAppPart.isConfigReady()) {
                resolveAuthorization();
            } else {
                downloadConfig();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPausedAsAuthorized = mtAppPart.isAuthorized();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mtAppPart.registerOnSessionExpire(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mtAppPart.unregisterOnSessionExpire(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTHORIZED, isPausedAsAuthorized);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if(getFragmentManager().getBackStackEntryCount() == 0) {
            resolveOrientation(fragment);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(networkProgressDialog != null){
            networkProgressDialog.dismiss();
        }
    }

    private void resolveOrientation(Object marker) {
        if(marker != null && marker instanceof IOrientationLocker) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    @Override
    public void showLoader(String[] actions) {
        setTitle(R.string.mt_abt_default);
        Bundle args = new Bundle();
        args.putStringArray(LoaderFragment.ACTIONS_EXTRA, actions);
        LoaderFragment fragment = new LoaderFragment();
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(content_frame_id, fragment).commit();
    }

    @Override
    public void showRegistration() {
        setTitle(R.string.mt_abt_default);
        Bundle args = new Bundle();
        args.putString(RegistrationFragment.PHONE_EXTRA, myPhone);
        RegistrationFragment fragment = new RegistrationFragment();
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(content_frame_id, fragment).commit();
    }

    @Override
    public void showConfirmation(WaitingConfirmationResponse waitingConfirmationResponse) {
        boolean isNeedAddToBackStack = false;
        ConfirmationType type = waitingConfirmationResponse.getConfirmationType();
        Bundle args = new Bundle();
        args.putParcelable(WaitingConfirmationResponse.CONFIRMATION_EXTRA, waitingConfirmationResponse);
        Fragment fragment = null;
        switch (type) {
            case SMS:
            case SMSBYID:
            case SMSBYREGISTRATIONID:
                setTitle(R.string.mt_abt_sms);
                fragment = new EnterSmsFragment();
                break;
            case THREE_DS:
                setTitle(R.string.mt_abt_3ds);
                isNeedAddToBackStack = true;
                fragment = new ThreeDsFragment();
                String redirectUrl = MtAppPart.getMtAppInstance(this).getConfig().getString("mt3dsUrl");
                args.putString(ThreeDsFragment.EXTRA_REDIRECT_URL, redirectUrl);
                break;
            case LOOP:
            default:
                setTitle(R.string.mt_abt_default);
                break;
        }
        if(fragment == null) {
            Analytics.getInstance(this).trackFatalException(new NotImplementedYetException("confirmation type " + type));
            return;
        }
        fragment.setArguments(args);
        FragmentTransaction ft = getFragmentManager().beginTransaction().replace(content_frame_id, fragment);
        if(isNeedAddToBackStack) {
            ft.addToBackStack("confirmation");
        }
        ft.commit();
    }

    @Override
    public void showSetPin() {
        setTitle(R.string.mt_abt_pin_code);
        hideSoftKeyboard();
        SetPinFragment fragment = new SetPinFragment();
        getFragmentManager().popBackStackImmediate("confirmation", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getFragmentManager().beginTransaction().replace(content_frame_id, fragment).addToBackStack("set_pin").commit();
    }

    @Override
    public void showChangePinConfirm() {
        setTitle(R.string.mt_abt_pin_code);
        hideSoftKeyboard();
        EnterPinFragment fragment = new EnterPinFragment();
        Bundle args = new Bundle();
        args.putString(EnterPinFragment.GOAL, EnterPinFragment.GOAL_CHANGE_PIN);
        fragment.setArguments(args);
        getFragmentManager().popBackStackImmediate("confirmation", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getFragmentManager().beginTransaction().replace(content_frame_id, fragment).addToBackStack("confirmation").commit();
    }

    @Override
    public void showChangePin() {
        setTitle(R.string.mt_abt_pin_code);
        hideSoftKeyboard();
        SetPinFragment fragment = new SetPinFragment();
        Bundle args = new Bundle();
        args.putString(SetPinFragment.GOAL, SetPinFragment.GOAL_CHANGE_PIN);
        fragment.setArguments(args);
        getFragmentManager().popBackStackImmediate("confirmation", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getFragmentManager().beginTransaction().replace(content_frame_id, fragment).addToBackStack("set_pin").commit();
    }

    @Override
    public void showEnterByPin() {
        setTitle(R.string.mt_abt_pin_code);
        hideSoftKeyboard();
        clearStack();
        EnterPinFragment fragment = new EnterPinFragment();
        getFragmentManager().beginTransaction().replace(content_frame_id, fragment).commit();
    }

    @Override
    public void showChooseUser(ArrayList<User> users) {
        setTitle(R.string.mt_abt_receiver);
        Fragment fragment = new ChooseUserFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ChooseUserFragment.EXTRA_USERS, users);
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(content_frame_id, fragment).commit();
    }

    @Override
    public void showSendMoney(User recipient) {
        setTitle(R.string.mt_abt_transfer);
        Bundle args = new Bundle();
        args.putParcelable(SendMoneyFragment.EXTRA_USER, recipient);
        SendMoneyFragment fragment = new SendMoneyFragment();
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(content_frame_id, fragment).commit();
    }

    @Override
    public void showSendMoneyFromGroup(User recipient) {
        setTitle(R.string.mt_abt_transfer);
        Bundle args = new Bundle();
        args.putParcelable(SendMoneyFragment.EXTRA_USER, recipient);
        SendMoneyFragment fragment = new SendMoneyFragment();
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(content_frame_id, fragment).addToBackStack("send_money_from_group").commit();
    }

    @Override
    public void showDefaultErrorMessage(String title, String message, String trackingId) {
        showDefaultErrorMessage(title, message, trackingId, false);
    }

    @Override
    public void showDefaultErrorMessage(String title, String message, String trackingId, final boolean closeOnOk) {
        if(currentDialog != null) {
            currentDialog.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        final SpannableStringBuilder spannedMessage = new SpannableStringBuilder(message);
        if(trackingId != null) {
            spannedMessage.append("\n\n");
            spannedMessage.append(getString(R.string.mt_tracking_id, trackingId));
            spannedMessage.setSpan(new RelativeSizeSpan(.7f), message.length(), spannedMessage.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        builder.setMessage(spannedMessage);

        if(closeOnOk) {
            builder.setCancelable(false);
        }
        builder.setPositiveButton(getString(R.string.mt_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (closeOnOk) {
                    finish();
                }
            }
        });
        currentDialog = builder.show();
    }

    @Override
    public void showDialog(Dialog dialog) {
        if(currentDialog != null) {
            currentDialog.dismiss();
        }
        dialog.setOnDismissListener(null);
        dialog.show();
        currentDialog = dialog;
    }

    @Override
    public void showSettings() {
        setTitle(R.string.mt_abt_settings);
        Fragment fragment = new SettingsFragment();
        Bundle args  = new Bundle();
        args.putString(SettingsFragment.GOAL, SettingsFragment.SETTINGS);
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(content_frame_id, fragment).commit();
    }

    @Override
    public void showResetMenu() {
        setTitle(R.string.mt_abt_reset);
        Fragment fragment = new SettingsFragment();
        Bundle args  = new Bundle();
        args.putString(SettingsFragment.GOAL, SettingsFragment.RESET);
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(content_frame_id, fragment).addToBackStack("show_reset_menu").commit();
    }

    @Override
    public void showOfferMenu() {
        setTitle(R.string.mt_abt_offer);
        Fragment fragment = new SettingsFragment();
        Bundle args  = new Bundle();
        args.putString(SettingsFragment.GOAL, SettingsFragment.OFFER);
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(content_frame_id, fragment).addToBackStack("show_offer_menu").commit();
    }

    @Override
    public void showCards() {
        setTitle(R.string.mt_abt_your_cards);
        Fragment fragment = new CardsFragment();
        getFragmentManager().beginTransaction().replace(content_frame_id, fragment).addToBackStack("show_cards").commit();
    }

    @Override
    public void downloadConfig() {
        final Network.RequestImpl configRequest = Requests.REQUEST_CONFIG.prepare().buildOn(this);
        configRequest.setShowDialog(true);
        execute(configRequest);
    }

    @Override
    public void onGetConfirmationCode(String code) {
        Fragment fragment = getFragmentManager().findFragmentById(content_frame_id);
        if(fragment != null && fragment instanceof EnterSmsFragment) {
            ((EnterSmsFragment) fragment).showCode(code);
        }
    }

    @Override
    public void onCardsReady(List<Card> cards) {
        Fragment fragment = getFragmentManager().findFragmentById(content_frame_id);
        if (fragment != null && fragment instanceof ICardInterest) {
            ((ICardInterest) fragment).setCards(cards);
        }
    }

    @Override
    public void onAttachCard(Card card) {
        Network.RequestImpl requestSetPrimaryCard = Requests.REQUEST_SET_LINKED_CARD_PRIMARY.prepare(card.getId()).buildOn(this);
        execute(requestSetPrimaryCard);
        finish();
    }

    @Override
    public void onUserSelected(User user) {
        recipient = user;
        showSendMoneyFromGroup(recipient);
    }

    @Override
    public void sendMoneyToPhone(SrcCardParams card, String recipientPhone, String money) {

        String srcAccountId = myPhone;
        String srcNetworkId = "mobile";
        String srcName = myPhone;
        String dstAccountId = recipientPhone;
        String dstNetworkId = "mobile";
        String dstName = recipientPhone;
        String moneyAmount = money;
        String currency = "RUB";
        String message = null;
        String image = null;
        String invoice = null;
        String ttl = null;
        String cardId = card.cardId;
        String cardNumber = card.cardNumber;
        String expireDate = card.expiryDate;
        String securityCode = card.securityCode;

        Network.RequestImpl sendMoneyRequest = Requests.REQUEST_TRANSFER_ANY_CARD_TO_ANY_POINTER
                .prepare(
                        srcAccountId,
                        srcNetworkId,
                        srcName,
                        dstAccountId,
                        dstNetworkId,
                        dstName,
                        moneyAmount,
                        currency,
                        message,
                        image,
                        invoice,
                        ttl,
                        cardId,
                        cardNumber,
                        expireDate,
                        securityCode
                )
                .buildOn(this);
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_MONEY, money);
        bundle.putString(EXTRA_CARD_ID, cardId);
        sendMoneyRequest.setAdditions(bundle);
        if(cardId == null) {
            Network.RequestImpl attachCardRequest = Requests.REQUEST_ATTACH_CARD.prepare(card.cardNumber, card.expiryDate, card.securityCode).buildOn(this);
            savePendingRequest(attachCardRequest, ATTACH_CARD_TAG);
        }

        sendMoneyRequest.setShowDialog(true);
        execute(sendMoneyRequest);

    }

    @Override
    public void sendMoneyToCard(SrcCardParams card, String dstCardNumber, String money) {

        String cardNumber = card.cardNumber;
        String expiryDate = card.expiryDate;
        String securityCode = card.securityCode;
        String cardId = card.cardId;
        String toCardNumber = dstCardNumber;
        String toCardId = null;
        String moneyAmount = money;
        String currency = "RUB";
        String name = null;
        String attachCard = null;
        String cardName = null;
        String screenSize = null;
        String timezone = null;

        Network.RequestImpl sendMoneyRequest = Requests.REQUEST_TRANSFER_ANY_CARD_TO_ANY_CARD
                .prepare(
                        cardNumber,
                        expiryDate,
                        securityCode,
                        cardId,
                        toCardNumber,
                        toCardId,
                        moneyAmount,
                        currency,
                        name,
                        attachCard,
                        cardName,
                        screenSize,
                        timezone).
                buildOn(this);
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_MONEY, money);
        bundle.putString(EXTRA_CARD_ID, cardId);
        sendMoneyRequest.setAdditions(bundle);
        if(cardId == null) {
            Network.RequestImpl attachCardRequest = Requests.REQUEST_ATTACH_CARD.prepare(card.cardNumber, card.expiryDate, card.securityCode).buildOn(this);
            savePendingRequest(attachCardRequest, ATTACH_CARD_TAG);
        }

        sendMoneyRequest.setShowDialog(true);
        execute(sendMoneyRequest);
    }

    @Override
    public void resetSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle(getString(R.string.mt_reset_settings))
                .setMessage(getString(R.string.mt_r_u_sure_reset))
                .setPositiveButton(getString(R.string.mt_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        execute(Requests.REQUEST_RESET_WALLET.prepare().buildOn(MtActivity.this));
                    }
                })
                .setNegativeButton(getString(R.string.mt_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();

    }

    @Override
    public void onWrongPinCode() {
        Fragment fragment = getFragmentManager().findFragmentById(content_frame_id);
        if(fragment != null && fragment instanceof EnterPinFragment) {
            ((EnterPinFragment) fragment).onWrongPinCode();
        }
    }

    @Override
    public void onPinAttemptsOverLimit(long finishTime, long currentTime) {
        Fragment fragment = getFragmentManager().findFragmentById(content_frame_id);
        if(fragment != null && fragment instanceof EnterPinFragment) {
            ((EnterPinFragment) fragment).onPinAttemptsOverLimit(finishTime, currentTime);
        }
    }

    @Override
    public void onSuccessSendMoney(String money, String cardId) {
        Intent intent = new Intent();
        String name = getIntent().getStringExtra(EXTRA_NAME);
        final String message = getString(R.string.mt_gen_transfer_completed, name != null ? name : myPhone, recipient.getName(), money);
        intent.putExtra(Intent.EXTRA_TEXT,  message);
        setResult(RESULT_OK, intent);
        if(!executePendingRequest(ATTACH_CARD_TAG)) {
            Network.RequestImpl requestSetPrimaryCard = Requests.REQUEST_SET_LINKED_CARD_PRIMARY.prepare(cardId).buildOn(this);
            execute(requestSetPrimaryCard);
            finish();
        }
    }

    @Override
    public void onSetLinkedCardPrimary(String cardId) {
        Fragment fragment = getFragmentManager().findFragmentById(content_frame_id);
        if(fragment != null && fragment instanceof CardsFragment) {
            ((CardsFragment) fragment).showCardAsPrimary(cardId);
        }
    }

    @Override
    public void onCommission(Commission commission) {
        Fragment fragment = getFragmentManager().findFragmentById(content_frame_id);
        if(fragment != null && fragment instanceof SendMoneyFragment) {
            ((SendMoneyFragment) fragment).showCommission(commission);
        }
    }

    @Override
    public void onChangePin() {
        getFragmentManager().popBackStack("set_pin", FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public void resolveAction() {
        clearStack();
        String action = getIntent().getAction();
        if (ACTION_SEND_MONEY.equals(action)) {
            ArrayList<User> users = getIntent().getParcelableArrayListExtra(EXTRA_USERS);

            if(users.size() == 0) {
                Analytics.getInstance(this).trackFatalException(new IllegalStateException("no users for send money"));
                showDefaultErrorMessage(getString(R.string.mt_error), getString(R.string.mt_unknown_error_msg), null, true);
            } else if(users.size() == 1) {
                recipient = users.get(0);
            }

            if(recipient == null) {
                showChooseUser(users);
            } else {
                showSendMoney(recipient);
            }
        } else if(ACTION_SETTINGS.equals(action)) {
            showSettings();
        } else {
            throw new IllegalStateException("can't resolve Activity work for Action " + action);
        }
    }

    @Override
    public void resolveAuthorization() {
        if (mtAppPart.hasOldSession()) {
            showEnterByPin();
        } else {
            String sessionId = mtAppPart.getSessionId();
            if (sessionId == null) {
                showLoader(new String[]{LoaderFragment.ANONYMOUS_SESSION});
            } else {
                showRegistration();
            }
        }
    }

    @Override
    public void askCommission(CommissionParams.SRC from, CommissionParams.DEST to, String money) {
        String cardId = from.cardId;
        String cardNumber = from.cardNumber;
        String paymentType = "Transfer";
        String provider = to.provider;
        String currency = "RUB";
        String moneyAmount = money;
        String fielddstPointerType = to.fielddstPointerType;
        String fielddstPointer = to.fielddstPointer;
        String fieldtoCardNumber = to.fieldtoCardNumber;

        Network.RequestImpl request = Requests.REQUEST_PAYMENT_COMMISSION
                .prepare(cardId, cardNumber, paymentType, provider, currency, moneyAmount, fielddstPointerType, fielddstPointer, fieldtoCardNumber)
                .buildOn(this);
        request.setIsNeedSpecificDispatch(true);
        execute(request);
    }

    private void clearStack() {
        while (getFragmentManager().popBackStackImmediate());
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getFragmentManager().findFragmentById(content_frame_id);
        if(fragment != null && fragment instanceof IBackPressable) {
            ((IBackPressable) fragment).onBackPress();
        } else {
            super.onBackPressed();
        }
    }

    private void hideSoftKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onNetworkDialogRequested() {
        onNetworkDialogDismissRequested();
        networkProgressDialog = new ProgressDialog(this);
        networkProgressDialog.setMessage(getString(R.string.mt_network_loading));
        networkProgressDialog.show();
    }

    @Override
    public void onNetworkDialogDismissRequested() {
        if(networkProgressDialog != null && networkProgressDialog.isShowing()){
            networkProgressDialog.hide();
        }
    }

    @Override
    public void onSessionExpire() {
        if(mtAppPart.isConfigReady()) {
            resolveAuthorization();
        } else {
            downloadConfig();
        }
    }
}
