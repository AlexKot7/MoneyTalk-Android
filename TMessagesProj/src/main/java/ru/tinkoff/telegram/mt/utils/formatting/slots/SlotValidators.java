package ru.tinkoff.telegram.mt.utils.formatting.slots;

/**
 * @author Mikhail Artemyev
 */
public final class SlotValidators {

    public static class DigitValidator implements Slot.SlotValidator {

            @Override
            public boolean validate(final char value) {
                return Character.isDigit(value);
            }

    }

    public static class MaskedDigitValidator extends DigitValidator {

        private static final char DIGIT_MASK = '*';

        @Override
        public boolean validate(char value) {
            if(super.validate(value)){
                return true;
            }

            return DIGIT_MASK == value;
        }
    }

    public static class LetterValidator implements Slot.SlotValidator {

        private boolean supportsEnglish;
        private boolean supportsRussian;

        public LetterValidator() {
            this(true, true);
        }

        public LetterValidator(final boolean supportsEnglish,
                               final boolean supportsRussian) {
            this.supportsEnglish = supportsEnglish;
            this.supportsRussian = supportsRussian;
        }

        @Override
        public boolean validate(final char value) {
            return validateEnglishLetter(value) || validateRussianLetter(value);
        }

        private boolean validateEnglishLetter(final char value) {
            return !(supportsEnglish ^ isEnglishCharacter(value)); // true when both 0 or 1
        }

        private boolean validateRussianLetter(final char value) {
            final int code    = (int) value;
            boolean   russian = 'А' <= code && code <= 'я'; // 'А' is russian!!

            return !(supportsRussian ^ russian); // true when both 0 or 1
        }

        private boolean isEnglishCharacter(final int charCode) {
            return ('A' <= charCode && charCode <= 'Z') || ('a' <= charCode && charCode <= 'z');
        }
    }


}
