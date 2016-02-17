package ru.tinkoff.telegram.mt.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;

import ru.tinkoff.telegram.mt.utils.CardValidator;
import ru.tinkoff.telegram.mt.utils.MutableColorSpan;


/**
 * @author a.shishkin1
 */


public class EditCardView extends ViewGroup {


    private static final int CARD_SYSTEM_LOGO = 1;
    private static final int FULL_CARD_NUMBER = 1 << 1;
    private static final int SCAN_CARD_BUTTON = 1 << 2;
    private static final int CHANGE_MODE_BUTTON = 1 << 3;
    private static final int IN_ANIMATION = 1 << 4;
    private static final int ONLY_NUMBER_STATE = 1 << 5;

    private Runnable update;
    private int flags;

    private String cardHint = null;

    private CardValidator cardValidator;

    private CardNumberEditText etCardNumber;
    private EditText etDate;
    private EditText etCvc;
    private Bitmap cardSystemLogo;
    private Paint cardSystemLogoPaint;
    private Paint paint;

    private int additionalPadding;

    private SimpleButton btnChangeMode;
    private SimpleButton btnScanCard;

    private float cardSystemLogoAnimationFactor = 1f;
    private float cardNumberFieldAnimationFactor = 1f;

    private CardFormatter cardFormatter;

    private boolean buttonsAvailable = true;


    public EditCardView(Context context) {
        super(context);
        init();
    }

    public EditCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public String getCardNumber() {
        return cardFormatter.getNormalizedNumber(etCardNumber.getRealText().toString(), " ");
    }

    public String getCvc() {
        return etCvc.getText().toString();
    }

    public String getExpireDate() {
        return etDate.getText().toString();
    }

    public boolean isFilledAndCorrect() {
        boolean cardNumberReady = cardValidator.validateNumber(getCardNumber());
        if(!cardNumberReady)
            return false;
        return check(ONLY_NUMBER_STATE) ||
                (cardValidator.validateExpirationDate(etDate.getText().toString()) && cardValidator.validateSecurityCode(etCvc.getText().toString()));
    }

    private void init() {
        additionalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        btnChangeMode = new SimpleButton(null);
        btnScanCard = new SimpleButton(null);
        flags = 0;
        Context context = getContext();
        cardValidator = new CardValidator();

        etCardNumber = new CardNumberEditText(context);
        etDate = new EditText(context);
        etCvc = new EditText(context);

        applyBehaviour(etCardNumber, etCvc, etDate);
        etCvc.setInputType(etCvc.getInputType() | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        update = new Runnable() {
            @Override
            public void run() {
                String number = etCardNumber.getText().toString();
                boolean isCorrect = cardValidator.validateNumber(cardFormatter.getNormalizedNumber(number, " "));
                if(!isCorrect && check(CHANGE_MODE_BUTTON)) {
                    hideChangeModeButton();
                }
                if(isCorrect && !cardFormatter.isLimited() && !check(ONLY_NUMBER_STATE)) {
                    showChangeModeButton();
                }
                etCardNumber.setTextColor(cardFormatter.isNeedToCheck(etCardNumber.length()) && !isCorrect ? Color.RED : Color.BLACK);
                etDate.setTextColor(etDate.length() == 5 && !cardValidator.validateExpirationDate(etDate.getText().toString()) ? Color.RED : Color.BLACK);
                etCvc.setTextColor(etCvc.length() == 3 && !cardValidator.validateSecurityCode(etCvc.getText().toString()) ? Color.RED : Color.BLACK);
                actions.onUpdate(EditCardView.this);
            }
        };

        etDate.setGravity(Gravity.CENTER);
        etCvc.setGravity(Gravity.CENTER);

        addView(etCardNumber);
        addView(etCvc);
        addView(etDate);
        etCardNumber.addTextChangedListener(new TextWatcher() {

            int[] sel = new int[2];

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                String number = etCardNumber.getText().toString();

                if (!TextUtils.isEmpty(number)) {
                    etCardNumber.removeTextChangedListener(this);
                    char firstChar = number.charAt(0);
                    if (firstChar == '2' || firstChar == '4' || firstChar == '5') {
                        cardFormatter.setType(CardFormatter.DEFAULT); // master card and visa xxxx xxxx xxxx xxxx
                    } else if (firstChar == '6') {
                        cardFormatter.setType(CardFormatter.MAESTRO); // maestro xxxxxxxx xxxx...x
                    } else {
                        cardFormatter.setType(CardFormatter.UNKNOWN);
                    }

                    etCardNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(cardFormatter.getMaxLength())});

                    String formatted = cardFormatter.format(number, " ");
                    populateCardNumber(formatted, before > count);
                    etCardNumber.addTextChangedListener(this);
                    String normalizedNumber = cardFormatter.getNormalizedNumber(number, " ");
                    if (!check(IN_ANIMATION)) {
                        boolean isFullCardMode = check(FULL_CARD_NUMBER);
                        boolean isLimited = cardFormatter.isLimited();

                        if (isFullCardMode) {
                            if (isLimited && cardValidator.validateNumber(normalizedNumber) && !check(ONLY_NUMBER_STATE)) {
                                showCvcAndDate();
                            }

                            if (check(SCAN_CARD_BUTTON) && normalizedNumber.length() > 15) {
                                hideScanButton();
                            }

                            if (!check(SCAN_CARD_BUTTON) && normalizedNumber.length() <= 15) {
                                showScanButton();
                            }
                        }
                    }

                }

                if (s.length() == 0 && check(CARD_SYSTEM_LOGO)) {
                    hideCardSystemLogo();
                    return;
                }

                if (s.length() != 0 && !check(CARD_SYSTEM_LOGO)) {
                    showCardSystemLogo();
                    return;
                }
                update.run();
            }

            private void populateCardNumber(String formattedCardNumber, boolean delete) {
                sel[0] = etCardNumber.getSelectionStart();
                sel[1] = etCardNumber.getSelectionEnd();
                int correction = 0;
                String text = etCardNumber.getText().toString();

                correction += countMatchesBeforeIndex(formattedCardNumber, " ", sel[0]) - countMatchesBeforeIndex(text, " ", sel[0]);

                etCardNumber.setText(formattedCardNumber);

                int pos = Math.max(sel[0] + correction, 0);
                etCardNumber.setSelection(Math.min(formattedCardNumber.length(), pos));
            }

            public int countMatchesBeforeIndex(String str, String sub, int border) {
                if (TextUtils.isEmpty(str) || TextUtils.isEmpty(sub)) {
                    return 0;
                }
                int count = 0;
                int idx = 0;
                while ((idx = str.indexOf(sub, idx)) != -1 && idx < border) {
                    count++;
                    idx += sub.length();
                }
                return count;
            }


            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etCardNumber.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN && !check(FULL_CARD_NUMBER))
                    return true;
                if (event.getAction() == MotionEvent.ACTION_UP && !check(IN_ANIMATION) && !check(FULL_CARD_NUMBER)) {
                    hideCvcAndDate();
                }
                return false;
            }
        });
        etDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence string, int start, int before, int count) {
                etDate.removeTextChangedListener(this);

                String result = string.toString().replace("/", "");
                if (count != 0 && string.length() > 1) {      // set delimiter before next sign
                    result = result.substring(0, 2) + "/" + result.substring(2);
                    etDate.setText(result);
                    etDate.setSelection((start + count + 1 > result.length()) ? result.length() : (start + count + 1));
                } else {
                    if(start == 2) {
                        etDate.setText(result.substring(0, 1));
                        etDate.setSelection(etDate.length());
                    } else {
                        etDate.setSelection(start + count);
                    }
                }
                update.run();
                if (cardValidator.validateExpirationDate(etDate.getText().toString())) {
                    activate(etCvc);
                }
                etDate.addTextChangedListener(this);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etCvc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                update.run();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        showScanButton();
        setMode(true);
        etCardNumber.requestFocus();
        etCardNumber.setHint("");
        etDate.setHint("");
        etCvc.setHint("");

        etDate.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        etCvc.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        cardSystemLogoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardFormatter = new CardFormatter();
    }

    public void dispatchFocus() {
        if (check(FULL_CARD_NUMBER)) {
            activate(etCardNumber);
        } else if(etDate.length() == 5) {
            activate(etCvc);
        } else {
            activate(etDate);
        }
    }

    public void setFullCardNumberMode(boolean enable) {
        if(enable) {
            flags &= ~ONLY_NUMBER_STATE;
        } else {
            flags |= ONLY_NUMBER_STATE;
        }
        requestLayout();
        invalidate();
    }


    protected void applyBehaviour(EditText... fields) {
        for(EditText et : fields) {
            et.setSingleLine(true);
            et.setPadding(0, 0, 0, 0);
            et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            if( android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                et.setBackgroundDrawable(null);
            } else {
                et.setBackground(null);
            }
            et.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
    }


    public void setHints(String cardHint, String dateHint, String cvcHint) {
        etCardNumber.setHint(cardHint);
        etDate.setHint(dateHint);
        etCvc.setHint(cvcHint);
    }

    public void setCardNumber(String number) {
        etCardNumber.setText(number);
    }

    public void setExpireDate(String date) {
        etDate.setText(date);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        boolean isFullCardNumberMode = check(FULL_CARD_NUMBER);

        int logoWidth = check(CARD_SYSTEM_LOGO) ? calculateCardLogoWidth() : 0;

        int additionalRightSpace = 0;

        if (check(SCAN_CARD_BUTTON)) {
            additionalRightSpace += calculateScanButtonWidth();
        }
        if (check(CHANGE_MODE_BUTTON)) {
            additionalRightSpace += calculateChangeModeWidth();
        }

        int accessWidth = widthSize - logoWidth - additionalRightSpace - (check(CARD_SYSTEM_LOGO) ? additionalPadding : 0);

        int contentsWidth = accessWidth / 3;

        int contentWidthSpec = MeasureSpec.makeMeasureSpec(contentsWidth, MeasureSpec.EXACTLY);
        int contentHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

        etCardNumber.measure(isFullCardNumberMode ? MeasureSpec.makeMeasureSpec((int) (accessWidth * cardNumberFieldAnimationFactor), MeasureSpec.EXACTLY) : contentWidthSpec, contentHeightSpec);
        etDate.measure(contentWidthSpec, contentHeightSpec);
        etCvc.measure(contentWidthSpec, contentHeightSpec);
        int btnsHeight = Math.max(calculateChangeModeHeight(), calculateScanButtonHeight());
        int iconsHeight = Math.max(cardSystemLogo == null ? 0 : cardSystemLogo.getHeight(), btnsHeight);
        int fieldHeight = Math.max(etCardNumber.getMeasuredHeight(), Math.max(etCvc.getMeasuredHeight(), etDate.getMeasuredHeight()));
        int height = Math.max(iconsHeight, fieldHeight);
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(heightSize, height);
        }

        setMeasuredDimension(widthSize, height);

    }

    private int calculateCardLogoWidth() {
        return cardSystemLogo == null ? 0 : (int)(cardSystemLogo.getWidth() * cardSystemLogoAnimationFactor);
    }

    private int calculateScanButtonWidth() {
        return btnScanCard.getWidth();
    }

    private int calculateChangeModeWidth() {
        return btnChangeMode.getWidth();
    }

    private int calculateScanButtonHeight() {
        return btnScanCard.getHeight();
    }

    private int calculateChangeModeHeight() {
        return btnChangeMode.getHeight();
    }

    private boolean check(int flags) {
        return (this.flags & flags) == flags;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int logoWidth = check(CARD_SYSTEM_LOGO) ? calculateCardLogoWidth() : 0;
        int t = 0;
        int l = 0;
        int startLabels = logoWidth + l + (check(CARD_SYSTEM_LOGO) ? additionalPadding : 0);
        t = (getHeight() - etCardNumber.getMeasuredHeight()) >> 1;
        etCardNumber.layout(startLabels, t, etCardNumber.getMeasuredWidth() + startLabels, t + etCardNumber.getMeasuredHeight());
        int additionalRightSpace = 0;
        int w = getWidth();
        int hh = getHeight() >> 1;
        if (check(SCAN_CARD_BUTTON)) {
            additionalRightSpace += calculateScanButtonWidth();
            btnScanCard.layoutIn(w - additionalRightSpace + (btnScanCard.getWidth() >> 1), hh);
        }
        if (check(CHANGE_MODE_BUTTON)) {
            additionalRightSpace += calculateChangeModeWidth();
            btnChangeMode.layoutIn(w - additionalRightSpace + (btnChangeMode.getWidth() >> 1), hh);
        }

        int contentOffset = (right - l - logoWidth - additionalRightSpace) / 3;
        t = (getHeight() - etDate.getMeasuredHeight()) >> 1;
        etDate.layout(startLabels + contentOffset, t, startLabels + contentOffset + etDate.getMeasuredWidth(), t + etDate.getMeasuredHeight());
        t = (getHeight() - etCvc.getMeasuredHeight()) >> 1;
        etCvc.layout(startLabels + contentOffset * 2, t, startLabels + contentOffset * 2 + etCvc.getMeasuredWidth(), t + etCvc.getMeasuredHeight());
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if(check(CARD_SYSTEM_LOGO) && cardSystemLogo != null) {
            int yOffset = (getHeight() - cardSystemLogo.getHeight()) >> 1;
            int xOffset = additionalPadding >> 1;
            canvas.drawBitmap(cardSystemLogo, xOffset, yOffset, cardSystemLogoPaint);
        }
        if(check(CHANGE_MODE_BUTTON)) {
            btnChangeMode.drawWithPaint(canvas, paint);
        }
        if(check(SCAN_CARD_BUTTON)) {
            btnScanCard.drawWithPaint(canvas, paint);
        }

        super.dispatchDraw(canvas);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (check(CHANGE_MODE_BUTTON) && btnChangeMode.handleAction(event) && buttonsAvailable) {
                showCvcAndDate();
                return true;
            }
            if (check(SCAN_CARD_BUTTON) && btnScanCard.handleAction(event) && buttonsAvailable) {
                actions.onPressScanCard(this);
                return true;
            }
        }
        return super.onTouchEvent(event);
    }




    public void setMode(boolean isFullNumber) {
        if(check(FULL_CARD_NUMBER) == isFullNumber)
            return;
        if(isFullNumber) {
            etCvc.setVisibility(GONE);
            etDate.setVisibility(GONE);
            flags |= FULL_CARD_NUMBER;
        } else {
            etCvc.setVisibility(VISIBLE);
            etDate.setVisibility(VISIBLE);
            flags &= ~FULL_CARD_NUMBER;
        }

        requestLayout();
        invalidate();
    }



    private void showCardSystemLogo() {
        cardSystemLogo = iconsHolder.getCardSystemBitmap(etCardNumber.getText().toString());
        flags |= CARD_SYSTEM_LOGO;
        cardSystemLogoPaint.setAlpha(0);

        ObjectAnimator animatorAlpha = ObjectAnimator.ofInt(cardSystemLogoPaint, "alpha", 0, 255);
        animatorAlpha.setDuration(150);
        animatorAlpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
        ObjectAnimator animatorEditField = ObjectAnimator.ofFloat(this, "cardSystemLogoAnimationFactor", 0f, 1f);
        animatorEditField.setDuration(150);
        animatorEditField.setInterpolator(new OvershootInterpolator());
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(animatorEditField, animatorAlpha);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                requestLayout();
                invalidate();
            }
        });
        set.start();
    }

    private void hideCardSystemLogo() {
        ObjectAnimator animatorAlpha = ObjectAnimator.ofInt(cardSystemLogoPaint, "alpha", 255, 0);
        animatorAlpha.setDuration(150);
        animatorAlpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
        ObjectAnimator animatorEditField = ObjectAnimator.ofFloat(this, "cardSystemLogoAnimationFactor", 1f, 0f);
        animatorEditField.setDuration(150);
        animatorEditField.setInterpolator(new OvershootInterpolator());
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(animatorAlpha, animatorEditField);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                flags &= ~CARD_SYSTEM_LOGO;
                requestLayout();
                invalidate();
            }
        });
        set.start();
    }


    private void showCvcAndDate() {
        hideChangeModeButton();
        final MutableColorSpan span = new MutableColorSpan(etCardNumber.getPaint().getColor());

        etCardNumber.getText().setSpan(span, 0, Math.max(etCardNumber.length() - 4, 0), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ObjectAnimator animatorText = ObjectAnimator.ofInt(span, "alpha", 255, 0);
        animatorText.setDuration(200);
        animatorText.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                etCardNumber.getText().setSpan(span, 0, Math.max(etCardNumber.length() - 4, 0), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        });
        ObjectAnimator toLeftAnimator = ObjectAnimator.ofFloat(etCardNumber, "animationFactor", 0f, 1f);
        toLeftAnimator.setStartDelay(140);
        toLeftAnimator.setDuration(200);
        toLeftAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                etCardNumber.setMode(CardNumberEditText.SHORT_MODE);
            }
        });

        etDate.setVisibility(VISIBLE);
        etDate.setAlpha(0f);
        ObjectAnimator animatorDate = ObjectAnimator.ofFloat(etDate, "alpha", 0f, 1f);
        animatorDate.setDuration(200);
        animatorDate.setStartDelay(200);

        etCvc.setVisibility(VISIBLE);
        etCvc.setAlpha(0f);
        ObjectAnimator animatorCvc = ObjectAnimator.ofFloat(etCvc, "alpha", 0f, 1f);
        animatorCvc.setDuration(200);
        animatorCvc.setStartDelay(280);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(animatorText, toLeftAnimator, animatorCvc, animatorDate);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setMode(false);
                flags &= ~IN_ANIMATION;

            }
        });
        flags |= IN_ANIMATION;
        activate(etDate);
        set.start();

    }

    private void hideCvcAndDate() {
        final MutableColorSpan span = new MutableColorSpan(etCardNumber.getPaint().getColor());
        span.setAlpha(0);
        ObjectAnimator animatorText = ObjectAnimator.ofInt(span, "alpha", 0, 255);
        animatorText.setDuration(250);
        animatorText.setInterpolator(new AccelerateInterpolator());
        animatorText.setStartDelay(200);
        animatorText.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (etCardNumber.getMode() == CardNumberEditText.FULL_MODE) {
                    etCardNumber.getText().setSpan(span, 0, etCardNumber.length() - 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

            }
        });
        ObjectAnimator toRightAnimator = ObjectAnimator.ofFloat(etCardNumber, "animationFactor", 1f, 0f);
//        toRightAnimator.setStartDelay(230);
        toRightAnimator.setDuration(200);

        ObjectAnimator animatorDate = ObjectAnimator.ofFloat(etDate, "alpha", 1f, 0f);
        animatorDate.setDuration(150);
        animatorDate.setStartDelay(80);
        animatorDate.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                etCardNumber.setMode(CardNumberEditText.FULL_MODE);
                etCardNumber.setSelection(etCardNumber.length());
                etCardNumber.setAnimationFactor(1f);
                etCardNumber.getText().setSpan(span, 0, etCardNumber.length() - 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                setMode(true);
            }
        });

        ObjectAnimator animatorCvc = ObjectAnimator.ofFloat(etCvc, "alpha", 1f, 0f);
        animatorCvc.setDuration(150);

        AnimatorSet set = new AnimatorSet();
//        set.playTogether(animatorCvc, animatorDate, animatorText, toRightAnimator);
        set.play(animatorCvc).with(animatorDate).before(toRightAnimator).before(animatorText);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                flags &= ~IN_ANIMATION;
                activate(etCardNumber);
                showChangeModeButton();
            }
        });
        flags |= IN_ANIMATION;

        set.start();
    }

    private void showChangeModeButton() {
        flags |= CHANGE_MODE_BUTTON;
        requestLayout();
        invalidate();
    }


    private void hideChangeModeButton() {
        flags &= ~CHANGE_MODE_BUTTON;
        requestLayout();
        invalidate();
    }

    private void showScanButton() {
        flags |= SCAN_CARD_BUTTON;
        requestLayout();
        invalidate();
    }


    private void hideScanButton() {
        flags &= ~SCAN_CARD_BUTTON;
        requestLayout();
        invalidate();
    }

    private void activate(final EditText et) {
        et.requestFocus();
        et.post(new Runnable() {
            @Override
            public void run() {
                et.setSelection(et.length());
            }
        });
    }

    public void setCardHint(String cardHint) {
        this.cardHint = cardHint;
        etCardNumber.setHint(cardHint);
    }

    public void setCardSystemLogoAnimationFactor(float cardSystemLogoAnimationFactor) {
        this.cardSystemLogoAnimationFactor = cardSystemLogoAnimationFactor;
        requestLayout();
        invalidate();
    }

    public float getCardSystemLogoAnimationFactor() {
        return cardSystemLogoAnimationFactor;
    }


    public void setCardNumberFieldAnimationFactor(float cardNumberFieldAnimationFactor) {
        this.cardNumberFieldAnimationFactor = cardNumberFieldAnimationFactor;
        requestLayout();
        invalidate();
    }

    public float getCardNumberFieldAnimationFactor() {
        return cardNumberFieldAnimationFactor;
    }


    private IconsHolder iconsHolder;

    public void setIconsHolder(IconsHolder iconsHolder) {
        this.iconsHolder = iconsHolder;
        if(iconsHolder != null) {
            btnScanCard.setBitmap(iconsHolder.getScanCardIcon());
            btnChangeMode.setBitmap(iconsHolder.getChangeModeIcon());
        }
        requestLayout();
        invalidate();
    }

    public interface IconsHolder {
        Bitmap getCardSystemBitmap(String cardNumber);
        Bitmap getChangeModeIcon();
        Bitmap getScanCardIcon();
    }


    public class CardFormatter {

        public static final int UNKNOWN = 0;
        public static final int DEFAULT = 1;
        public static final int MAESTRO = 2;

        private int type;
        private int maxLength;
        private int[] defaultRangers = new int[] {19};
        private int[] maestroRangers = new int[] {14, 15, 16, 17, 18, 19, 20};
        private int[] unknownRangers = new int[] {Integer.MAX_VALUE - 3};

        public String format(String input, CharSequence delimiter) {
            return doFormat(getNormalizedNumber(input, delimiter), delimiter);
        }

        public void setType(int type) {
            this.type = type;
            maxLength = 0;
            for(int i : getValidationRanges()) {
                if(i > maxLength) {
                    maxLength = i;
                }
            }
        }

        public boolean isLimited() {
            return type == DEFAULT;
        }

        public boolean isNeedToCheck(int digits) {
            int[] ranges = getValidationRanges();
            for (int range : ranges) {
                if (range == digits) {
                    return true;
                }
            }

            return false;
        }

        public int getMaxLength() {
            return maxLength;
        }

        public int[] getValidationRanges() {
            if(type == DEFAULT)
                return defaultRangers;
            if(type == MAESTRO)
                return maestroRangers;
            return unknownRangers;
        }

        private String getNormalizedNumber(String string, CharSequence delimiter) {
            return string.replace(delimiter, "");
        }

        protected String doFormat(String cardNumber, CharSequence delimiter) {
            if (type == DEFAULT) {
                char[] chars = cardNumber.toCharArray();
                StringBuilder cardNumberBuilder = new StringBuilder(cardNumber);

                for (int i = 1, index = 0; i < chars.length; i++) {
                    if (i % 4 == 0) {
                        cardNumberBuilder.insert(i + index, delimiter);
                        index++;
                    }
                }

                return cardNumberBuilder.toString().trim();
            }
            if(type == MAESTRO) {
                int length = cardNumber.length();

                if(length < 8)
                    return cardNumber;

                StringBuilder cardNumberBuilder = new StringBuilder(cardNumber);
                cardNumberBuilder.insert( 8 ,delimiter);
                return cardNumberBuilder.toString().trim();
            }
            if(type == UNKNOWN) {
                return cardNumber;
            }

            return null;
        }
    }

    private Actions actions = NO_ACTIONS;


    private static Actions NO_ACTIONS = new Actions() {

        @Override
        public void onUpdate(EditCardView ecv) {

        }

        @Override
        public void onPressScanCard(EditCardView ecv) {

        }
    };

    public void setActions(Actions actions) {
        if(actions != null) {
            this.actions = actions;
        } else {
            this.actions = NO_ACTIONS;
        }
    }

    public interface Actions {
        void onUpdate(EditCardView editCardView);
        void onPressScanCard(EditCardView editCardView);
    }

    private class SimpleButton {
        private Rect rect;
        private Bitmap bitmap;
        private boolean isVisible;

        public SimpleButton(Bitmap bitmap) {
            this.bitmap = bitmap;
            this.rect = new Rect();
            this.isVisible = true;
            if(bitmap != null) {
                layoutIn(bitmap.getWidth() >> 1, bitmap.getHeight() >> 1);
            }
        }

        private boolean handleAction(MotionEvent ev) {
            int x = (int) ev.getX();
            return x > rect.left && x < rect.right;
        }

        protected void layoutIn(int centerX, int centerY) {
            if(bitmap != null) {
                int hx = bitmap.getWidth() >> 1;
                int hy = bitmap.getHeight() >> 1;
                rect.set(centerX - hx, centerY - hy, centerX + hx, centerY + hy);
            } else {
                rect.set(centerX, centerY, centerX, centerY);
            }

        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
            layoutIn(rect.centerX(), rect.centerY());
        }

        protected void drawWithPaint(Canvas canvas, Paint paint) {
            if(bitmap == null && isVisible)
                return;
            canvas.drawBitmap(bitmap, null, rect, paint);
        }

        public int getWidth() {
            return rect.width();
        }

        public int getHeight() {
            return rect.height();
        }

        public boolean isVisible() {
            return isVisible;
        }

        public void setVisibility(boolean visible) {
            this.isVisible = visible;
        }
    }


}
