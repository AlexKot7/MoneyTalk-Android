package ru.tinkoff.telegram.mt.utils.formatting.formatters;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Iterator;

import ru.tinkoff.telegram.mt.utils.formatting.FormattedTextChangeListener;
import ru.tinkoff.telegram.mt.utils.formatting.Mask;
import ru.tinkoff.telegram.mt.utils.formatting.slots.Slot;

/**
 * @author Mikhail Artemyev
 *         <p/>
 *         This class encapsutates logic of formatting (pretty printing) content of a TextView.
 *         All the formatting logic is incapsulated inside the {@link Mask} class. This class is only used
 *         to follow TextView changes and format it according to the {@link Mask}.
 *         It's okay to use it either with {@link TextView} or {@link EditText}. Important note for using
 *         with bare {@link TextView}. Since its content usually changes with {@link TextView#setText},
 *         inserting text should contain all the hardcoded symbols of the {@link Mask}.
 *         <p/>
 *         All the children classes should implement their own way of creating {@link Mask}.
 */
public abstract class AFormatWatcher implements TextWatcher {

    public static final int TAG_PRINTABLE = 1;

    private static final int INSERT = 1;
    private static final int REMOVE = 1 << 1;

    private int diffStartPosition;
    private int diffInsertLength;
    private int diffType;
    private int cursorPosition;

    private CharSequence textBeforeChange;
    private boolean trimmingSequence = false;

    private Mask mask;
    private TextView textView;
    private boolean initWithMask;

    private WeakReference<FormattedTextChangeListener> callback;

    protected abstract Mask createMask();

    /**
     * Note for children implementations of this class. Since {@code initWithMask} is critical to be
     * set at the object construction time. It's highly recommended to use public static methods
     * instead of constructor of your class. This might be something like this
     * {@code
     * public static MyFormatWatcher initiatedWatcher(TextView textView){
     * return new MyFormatWatcher(textView, true);
     * }
     * }
     * <p/>
     * And same for {@code uninitiatedWatcher(TextView)}
     *
     * @param textView     an observable text view which content text will be formatted using {@link Mask}.
     *                     This class calls {@link TextView#addTextChangedListener(TextWatcher)} at
     *                     the construction time.
     * @param initWithMask this flags defines whether hardcoded head of the mask (e.g "+7 ") will
     *                     fill the initial text of the {@code textView}.
     */
    protected AFormatWatcher(TextView textView, boolean initWithMask) {
        this.textView = textView;
        this.initWithMask = initWithMask;
        textView.addTextChangedListener(this);
    }

    public void refreshMask() {
        final boolean initial = this.mask == null;

        this.mask = createMask();
        checkMask();

        if (!initial || initWithMask) {
            textView.removeTextChangedListener(this);
            textView.setText(mask.toString());
            setSelection(mask.getInitialInputPosition());
            textView.addTextChangedListener(this);
        }
    }

    public String getUnformattedString() {
        Iterator<Slot> maskIterator = mask.iterator();
        StringBuilder result = new StringBuilder(mask.getSize());
        while (maskIterator.hasNext()) {
            Slot s = maskIterator.next();
            if (s.hasTag(TAG_PRINTABLE) && s.getValue() != null) {
                result.append(s.getValue());
            }
        }

        return result.toString();
    }

    public boolean filled() {
        return mask.filled();
    }

    @Override
    public String toString() {
        return mask.toString();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        checkMask();

        textBeforeChange = s;

        diffStartPosition = start;
        diffType = 0;
        diffInsertLength = 0;
        cursorPosition = -1;

        int diffRemoveLength = 0;

        if (after > 0) {
            diffType |= INSERT;
            diffInsertLength = after;
        }

        if (count > 0) {
            diffType |= REMOVE;
            diffRemoveLength = count;
        }

        trimmingSequence =
                diffInsertLength > 0
                        && diffRemoveLength > 0
                        && diffInsertLength < diffRemoveLength;

        if ((diffType & REMOVE) == REMOVE) {
            int removePosition = diffStartPosition + diffRemoveLength - 1;
            cursorPosition = mask.removeBackwards(removePosition, diffRemoveLength);
        }

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        checkMask();
        if ((diffType & INSERT) != INSERT) {
            return;
        }

        boolean usualInsert = true;
        CharSequence diffChars = s.subSequence(diffStartPosition, diffStartPosition + diffInsertLength);

        if (trimmingSequence) {
            CharSequence diffBefore = textBeforeChange.subSequence(diffStartPosition, diffStartPosition + diffInsertLength);
            usualInsert = !diffBefore.equals(diffChars);
        }

        cursorPosition = mask.insertAt(diffChars, diffStartPosition, usualInsert);

    }

    @Override
    public void afterTextChanged(Editable newText) {

        checkMask();

        String formatted = mask.toString();

        // force change text of EditText we're attached to
        // only in case it's necessary (formatted text differs from inputted)
        if (!formatted.equals(textView.getText().toString())) {
            textView.removeTextChangedListener(this);
            textView.setText(formatted);
            textView.addTextChangedListener(this);
        }

        if (0 <= cursorPosition && cursorPosition <= textView.getText().length()) {
            setSelection(cursorPosition);
        }

        if (callback != null && callback.get() != null) {
            callback.get().onTextFormatted(this, toString());
        }
    }

    protected Mask getMask() {
        return mask;
    }

    protected void setMask(Mask mask) {
        this.mask = mask;
    }

    protected TextView getTextView() {
        return textView;
    }

    protected void setTextView(TextView textView) {
        this.textView = textView;
    }

    public void setCallback(FormattedTextChangeListener callback) {
        this.callback = new WeakReference<>(callback);
    }

    private void checkMask() throws RuntimeException {
        if (mask == null) {
            throw new RuntimeException("Mask cannot be null at this point. Check maybe you forgot " +
                    "to call refreshMask()");
        }
    }

    private void setSelection(int position) {
        if (textView != null && textView instanceof EditText) {
            ((EditText) textView).setSelection(position);
        }
    }
}
