package ru.tinkoff.telegram.mt.utils.formatting;

import ru.tinkoff.telegram.mt.utils.formatting.formatters.AFormatWatcher;
import ru.tinkoff.telegram.mt.utils.formatting.slots.Slot;
import ru.tinkoff.telegram.mt.utils.formatting.slots.SlotValidators;

/**
 * @author Mikhail Artemyev
 */
public abstract class MtSlots {

    public static Slot[] russianPhoneMaskSlots(){
        return new Slot[]{
                hardcodedPrintable('+'),
                hardcodedPrintable('7'),
                hardcoded(' '),
                hardcoded('('),
                digit(),
                digit(),
                digit(),
                hardcoded(')'),
                hardcoded(' '),
                digit(),
                digit(),
                digit(),
                hardcoded('-'),
                digit(),
                digit(),
                hardcoded('-'),
                digit(),
                digit(),
        };
    }
    private static Slot hardcodedPrintable(char value) {
        return Slot.hardcodedSlot(value).withTags(AFormatWatcher.TAG_PRINTABLE);
    }

    private static Slot hardcoded(char value) {
        return Slot.hardcodedSlot(value);
    }

    private static Slot digit() {
        return new Slot(null, new SlotValidators.DigitValidator())
                .withTags(AFormatWatcher.TAG_PRINTABLE);
    }

}
