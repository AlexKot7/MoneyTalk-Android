package ru.tinkoff.telegram.mt.utils.formatting.formatters;

import android.widget.TextView;

import ru.tinkoff.telegram.mt.utils.formatting.Mask;
import ru.tinkoff.telegram.mt.utils.formatting.slots.Slot;

/**
 * @author Mikhail Artemyev
 */
public class CustomMaskFormatWatcher extends AFormatWatcher {

    private final Slot[] predefinedSlots;
    private final boolean terminateMask;

    public static CustomMaskFormatWatcher installOn(
            TextView textView,
            Slot[] slots,
            boolean terminated,
            boolean initWithMask
    ) {
        return new CustomMaskFormatWatcher(textView, slots, terminated, initWithMask);
    }

    public static CustomMaskFormatWatcher installOn(
            TextView textView,
            Slot[] slots,
            boolean terminated
    ) {
        return installOn(textView, slots, terminated, true);
    }

    private CustomMaskFormatWatcher(TextView textView,
                                    Slot[] slots,
                                    boolean terminated,
                                    boolean initWithMask) {
        super(textView, initWithMask);
        this.predefinedSlots = slots;
        this.terminateMask = terminated;
        refreshMask();
    }

    @Override
    protected Mask createMask() {
        return new Mask(predefinedSlots, terminateMask);
    }

}
