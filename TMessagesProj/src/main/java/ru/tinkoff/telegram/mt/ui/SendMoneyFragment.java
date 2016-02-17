package ru.tinkoff.telegram.mt.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import ru.tinkoff.telegram.mt.R;

import java.util.List;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import ru.tinkoff.telegram.mt.base.ICallback;
import ru.tinkoff.telegram.mt.base.ICondition;
import ru.tinkoff.telegram.mt.entities.Card;
import ru.tinkoff.telegram.mt.entities.Commission;
import ru.tinkoff.telegram.mt.entities.MtConfig;
import ru.tinkoff.telegram.mt.entities.User;
import ru.tinkoff.telegram.mt.mtpart.MtAppPart;
import ru.tinkoff.telegram.mt.network.Network;
import ru.tinkoff.telegram.mt.network.Requests;

import ru.tinkoff.telegram.mt.network.params.CommissionParams;
import ru.tinkoff.telegram.mt.network.params.SrcCardParams;
import ru.tinkoff.telegram.mt.ui.dialogs.ChooseCardDialog;
import ru.tinkoff.telegram.mt.utils.CardLogoCache;
import ru.tinkoff.telegram.mt.utils.PhoneNumberValidator;
import ru.tinkoff.telegram.mt.utils.Utils;
import ru.tinkoff.telegram.mt.utils.formatting.MtSlots;
import ru.tinkoff.telegram.mt.utils.formatting.formatters.AFormatWatcher;
import ru.tinkoff.telegram.mt.utils.formatting.formatters.CustomMaskFormatWatcher;
import ru.tinkoff.telegram.mt.utils.formatting.slots.Slot;
import ru.tinkoff.telegram.mt.views.EditCardView;
import ru.tinkoff.telegram.mt.views.LoadingTextView;
import ru.tinkoff.telegram.mt.views.ShowCardView;

/**
 * @author a.shishkin1
 */


public class SendMoneyFragment extends Fragment implements View.OnClickListener, EditCardView.IconsHolder, ICardInterest, EditCardView.Actions {


    public static final String EXTRA_USER = "user";
    public static final int REQUEST_CARD_NUMBER = 1273812;
    public static final int REQUEST_CARD_NUMBER_DST = REQUEST_CARD_NUMBER + 1;

    private final int SEND_TO_PHONE = 0;
    private final int SEND_TO_CARD = 1;

    private int dstMode;

    private List<Card> cards;
    private Card cardSrc;

    private IInteraction interaction;
    private Handler handler = new Handler();

    private Button btnSendMoney;
    private EditCardView ecvCard;
    private ShowCardView scvCard;
    private TextView tvPhoneTab;
    private TextView tvCardTab;
    private FrameLayout flSourceCardContainer;
    private TextView tvNewCard;
    private EditCardView ecvDstCard;
    private EditText etDstPhone;
    private EditText etMoney;
    private LoadingTextView tvCommission;
    private View vDividerNewCard;
    private TextView tvLabel;

    private AFormatWatcher phoneNumberWatcher;

    private User target;

    private ICallback<Card> cardSetAction = new ICallback<Card>() {
        @Override
        public void onResult(Card card) {
            cardSrc = card;
            if(card == null) {
                showNewCard();
            } else {
                showCardExist();
            }
            update.run();
        }
    };

    private View.OnClickListener chooseCardClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startChooseCard();
        }
    };

    private ICondition moneyValidator = new ICondition() {

        int min = -1;
        int max = -1;

        @Override
        public boolean condition() {
            String money = etMoney.getText().toString();
            int m = 0;
            try {
               m = Integer.valueOf(money);
            } catch (NumberFormatException ex) {
                return false;
            }
            if(min < 0 || max < 0) {
                MtConfig config = MtAppPart.getMtAppInstance(getActivity()).getConfig();
                if(config != null) {
                    try {
                        min = Integer.valueOf(config.getString("mtSummDetectionCriteria.min"));
                        max = Integer.valueOf(config.getString("mtSummDetectionCriteria.max"));
                    } catch (Exception e) {
                        return false;
                    }
                } else {
                    return m >= 10 && m <= 75000; // like defaults
                }
            }
            return m >= min && m <= max;
        }
    };

    private ICondition dstTargetReadyValidator = new ICondition() {
        @Override
        public boolean condition() {
            if(dstMode == SEND_TO_CARD) {
                return ecvDstCard.isFilledAndCorrect();
            } else if (dstMode == SEND_TO_PHONE){
                return PhoneNumberValidator.validatePhoneNumber(getTargetPhone());
            }
            return false;
        }
    };

    private Runnable update = new Runnable() {
        @Override
        public void run() {
            boolean isCardReady = cardSrc != null || (ecvCard != null && ecvCard.isFilledAndCorrect());
            boolean isMoneyReady = moneyValidator.condition();
            boolean isDestReady = dstTargetReadyValidator.condition();

            boolean areAllFieldsReady = isCardReady && isMoneyReady && isDestReady;

            btnSendMoney.setEnabled(areAllFieldsReady);
            if(areAllFieldsReady) {
                tvCommission.setVisibility(areAllFieldsReady ? View.VISIBLE : View.GONE);
                tvCommission.setMode(LoadingTextView.LOADING);
                askCommission();
            } else {
                tvCommission.setVisibility(View.GONE);
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        interaction = (IInteraction)activity;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.send_money_fragment, container, false);

        ecvDstCard = (EditCardView) view.findViewById(R.id.et_dst_card);
        btnSendMoney = (Button) view.findViewById(R.id.btn_send_money);
        tvPhoneTab = (TextView) view.findViewById(R.id.tv_phone);
        tvCardTab = (TextView) view.findViewById(R.id.tv_card);
        tvNewCard = (TextView) view.findViewById(R.id.tv_new_card);
        etMoney = (EditText) view.findViewById(R.id.et_money);
        tvCommission = (LoadingTextView) view.findViewById(R.id.tv_commission);
        vDividerNewCard = view.findViewById(R.id.v_divider_new_card);
        etDstPhone = (EditText) view.findViewById(R.id.et_dst_phone);
        tvLabel = (TextView) view.findViewById(R.id.tv_label);

        phoneNumberWatcher = CustomMaskFormatWatcher
                .installOn(etDstPhone, MtSlots.russianPhoneMaskSlots(), true, true);

        tvPhoneTab.setOnClickListener(this);
        tvCardTab.setOnClickListener(this);
        btnSendMoney.setOnClickListener(this);
        tvNewCard.setOnClickListener(chooseCardClick);
        etMoney.setInputType(InputType.TYPE_CLASS_NUMBER);
        tvCommission.setHint(getString(R.string.mt_commission_loading));
        ecvDstCard.setIconsHolder(this);
        ecvDstCard.setFullCardNumberMode(false);
        ecvDstCard.setActions(this);
        ecvDstCard.setHints(getString(R.string.mt_edit_card_number), getString(R.string.mt_edit_card_date), getString(R.string.mt_edit_card_cvc));

        Drawable btnBg = Utils.createBlueButtonDrawable(getActivity());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            btnSendMoney.setBackground(btnBg);
        } else {
            btnSendMoney.setBackgroundDrawable(btnBg);
        }

        target = getArguments().getParcelable(EXTRA_USER);

        flSourceCardContainer = (FrameLayout)view.findViewById(R.id.fl_container_for_src);

        TextView tvRecipient = (TextView) view.findViewById(R.id.tv_recepient);
        tvRecipient.setText(target.getName());

        TextView tvSrcDescription = (TextView) view.findViewById(R.id.tv_src_description);

        showDstPhone();

        etMoney.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                handler.removeCallbacks(update);
                handler.postDelayed(update, 300);
            }
        });

        return view;
    }


    @Override
    public void setCards(List<Card> cards) {
        this.cards = cards;
        for(Card card : cards) {
            if(card.isPrimary()) {
                cardSrc = card;
                break;
            }
        }
        if(cardSrc == null && cards.size() > 0) {
            cardSrc = cards.get(0);
        }
        cardSetAction.onResult(cardSrc);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Network.RequestImpl accountsRequest = Requests.REQUEST_ACCOUNTS_FLAT.prepare().buildOn(getActivity());
        interaction.execute(accountsRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
        update.run();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.tv_card :
                showDstCard();
                update.run();
                break;
            case R.id.tv_phone :
                showDstPhone();
                update.run();
                break;
            case R.id.btn_send_money :
                String money = etMoney.getText().toString();
                SrcCardParams sourceCard = cardSrc == null ? new SrcCardParams(ecvCard.getCardNumber(), ecvCard.getExpireDate(), ecvCard.getCvc()) : new SrcCardParams(cardSrc);
                switch (dstMode) {
                    case SEND_TO_PHONE:
                        interaction.sendMoneyToPhone(sourceCard, getTargetPhone(), money);
                        break;
                    case SEND_TO_CARD:
                        interaction.sendMoneyToCard(sourceCard, getTargetCard(), money);
                        break;
                    default:
                        throw new IllegalStateException("unknown target for send. card or phone");
                }
                v.setEnabled(false);
                break;
        }
    }

    private String getTargetPhone() {
        return phoneNumberWatcher.getUnformattedString();
    }

    private String getTargetCard() {
        return ecvDstCard.getCardNumber();
    }

    private void showDstPhone() {
        tvLabel.setText(R.string.mt_gen_transfer_phonehint);
        dstMode = SEND_TO_PHONE;
        tvCardTab.setBackgroundResource(0);
        tvPhoneTab.setBackgroundResource(R.drawable.search_dark_activated);
        ecvDstCard.setVisibility(View.GONE);
        etDstPhone.setVisibility(View.VISIBLE);
        etDstPhone.requestFocus();
        etDstPhone.setSelection(etDstPhone.length());
        fillPhoneNumber();
    }

    private void showDstCard() {
        tvLabel.setText(R.string.mt_gen_transfer_cardnumberhint);
        dstMode = SEND_TO_CARD;
        tvCardTab.setBackgroundResource(R.drawable.search_dark_activated);
        tvPhoneTab.setBackgroundResource(0);
        ecvDstCard.setVisibility(View.VISIBLE);
        etDstPhone.setVisibility(View.GONE);
        ecvDstCard.dispatchFocus();
    }

    private void fillPhoneNumber(){
        final String targetPhone = target.getPhone();
        if(targetPhone == null || targetPhone.length() == 0){
            etDstPhone.setEnabled(true);
        } else {
            etDstPhone.setEnabled(false);
            if( android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                etDstPhone.setBackgroundDrawable(null);
            } else {
                etDstPhone.setBackground(null);
            }
            etDstPhone.setText(targetPhone);
        }
    }

    private void askCommission() {
        String money = etMoney.getText().toString();
        CommissionParams.SRC src;
        CommissionParams.DEST dest;

        if(cardSrc != null) {
            src = CommissionParams.SRC.forLinkedCard(cardSrc.getId());
        } else {
            src = CommissionParams.SRC.forNotLinkedCard(ecvCard.getCardNumber());
        }

        if(dstMode == SEND_TO_CARD) {
            dest = CommissionParams.DEST.forCard(getTargetCard());
        } else if (dstMode == SEND_TO_PHONE) {
            dest = CommissionParams.DEST.forPhone(getTargetPhone());
        } else {
            throw new IllegalStateException("unknown send mode");
        }

        interaction.askCommission(src, dest, money);
    }

    public void showCommission(Commission commission) {
        tvCommission.setText(Commission.HumanReadable.createHumanReadableString(commission, getActivity()));
        tvCommission.setVisibility(View.VISIBLE);
        tvCommission.setMode(LoadingTextView.READY);
    }


    private void showNewCard() {
        int visibility = cards.size() > 0 ? View.VISIBLE : View.GONE;
        tvNewCard.setVisibility(visibility);
        vDividerNewCard.setVisibility(visibility);

        ecvCard = new EditCardView(getActivity());

        ecvCard.setIconsHolder(this);

        ecvCard.setActions(this);
        ecvCard.setHints(getString(R.string.mt_edit_card_your_number), getString(R.string.mt_edit_card_date), getString(R.string.mt_edit_card_cvc));

        flSourceCardContainer.removeAllViews();
        flSourceCardContainer.addView(ecvCard);
        ecvCard.dispatchFocus();
        scvCard = null;
    }

    private void showCardExist() {
        tvNewCard.setVisibility(View.GONE);
        scvCard = new ShowCardView(getActivity());
        scvCard.show(cardSrc.getCardName(), cardSrc.getValue());
        scvCard.setOnClickListener(chooseCardClick);
        flSourceCardContainer.removeAllViews();
        flSourceCardContainer.addView(scvCard);
        ecvCard = null;
    }


    private void startChooseCard() {
        new ChooseCardDialog(getActivity(), cards, cardSetAction).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CARD_NUMBER || requestCode == REQUEST_CARD_NUMBER_DST) {
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
                if(requestCode == REQUEST_CARD_NUMBER) {
                    ecvCard.setCardNumber(scanResult.getFormattedCardNumber());
                    ecvCard.setExpireDate(String.format("%02d%02d", scanResult.expiryMonth, (scanResult.expiryYear % 100)));
                }
                if(requestCode == REQUEST_CARD_NUMBER_DST) {
                    ecvDstCard.setCardNumber(scanResult.getFormattedCardNumber());
                }
            }
        }

    }

    @Override
    public Bitmap getCardSystemBitmap(String cardNumber) {
        return CardLogoCache.getLogoByNumber(getActivity(), cardNumber);
    }

    @Override
    public Bitmap getChangeModeIcon() {
        return BitmapFactory.decodeResource(getResources(), R.drawable.next_grey);
    }

    @Override
    public Bitmap getScanCardIcon() {
        return BitmapFactory.decodeResource(getResources(), R.drawable.scan_grey);
    }


    @Override
    public void onUpdate(EditCardView editCardView) {
        update.run();
    }

    @Override
    public void onPressScanCard(EditCardView editCardView) {
        int requestCode;
        if(editCardView == ecvCard) {
            requestCode = REQUEST_CARD_NUMBER;
        } else if (editCardView == ecvDstCard) {
            requestCode = REQUEST_CARD_NUMBER_DST;
        } else {
            throw new RuntimeException("press on unknown view");
        }
        Intent scanIntent = new Intent(getActivity(), CardIOActivity.class);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false);
        startActivityForResult(scanIntent, requestCode);
    }
}
