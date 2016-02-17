package ru.tinkoff.telegram.mt.utils.formatting.slots;


import java.util.HashSet;
import java.util.Set;

/**
 * @author Mikhail Artemyev
 */
public class Slot {

    // On input slot moves it's current value to the nextSlot
    // This is default behavior
    public static final int RULE_INPUT_MOVES_CURRENT = 1;

    // On input slot moves new value to the next slot keeping current value
    public static final int RULE_INPUT_MOVES_INPUT = 1 << 1;

    public static final int RULES_DEFAULT = RULE_INPUT_MOVES_CURRENT;
    public static final int RULES_HARDCODED = RULE_INPUT_MOVES_INPUT;


    private int rulesFlags = RULES_DEFAULT;

    private Character value;

    private Slot nextSlot;
    private Slot prevSlot;

    private Set<Integer> tags = new HashSet<>();

    private SlotValidatorSet validators;

    public static Slot hardcodedSlot(char value) {
        return new Slot(RULES_HARDCODED, value, null);
    }

    public Slot(int rules, Character value, SlotValidatorSet validators) {
        this.rulesFlags = rules;
        this.value = value;
        this.validators = validators == null ? new SlotValidatorSet() : validators;
    }

    public Slot(Character value, SlotValidator... validators) {
        this(RULES_DEFAULT, value, SlotValidatorSet.setOf(validators));
    }

    public Slot(char value) {
        this(RULES_DEFAULT, value, null);
    }

    public Slot() {
        this(RULES_DEFAULT, null, null);
    }

    public Slot(Slot slotToCopy) {
        this(
                slotToCopy.rulesFlags,
                slotToCopy.value,
                slotToCopy.getValidators()
        );

        this.tags.addAll(slotToCopy.tags);
    }

    public boolean anyInputToTheRight() {
        if (value != null && !hardcoded()) {
            return true;
        }

        if (nextSlot != null) {
            return nextSlot.anyInputToTheRight();
        }

        return false;
    }

    public void setValue(Character newValue) {
        if (newValue == null) {
            removeCurrentValue();
        } else {
            setNewValue(newValue);
        }
    }

    public Character getValue() {
        return value;
    }

    public boolean canInsertHere(char newValue) {
        if (hardcoded()) {
            return value.equals(newValue);
        }

        return validate(newValue);
    }

    private boolean validate(char val) {
        return validators == null || validators.validate(val);
    }

    public boolean hardcoded() {
        return value != null && (rulesFlags & RULE_INPUT_MOVES_INPUT) == RULE_INPUT_MOVES_INPUT;
    }

    public int hardcodedSequenceEndIndex() {
        return hardcodedSequenceEndIndex(0);
    }

    public int hardcodedSequenceEndIndex(int fromIndex) {

        if (hardcoded() && (nextSlot == null || !nextSlot.hardcoded())) {
            // I'm last hardcoded slot
            return fromIndex + 1;
        }

        if (hardcoded() && nextSlot.hardcoded()) {
            // me and my next neightbour are hardcoded
            return nextSlot.hardcodedSequenceEndIndex(++fromIndex);
        }

        // i'm not even hardcoded
        return -1;
    }

    private void setNewValue(Character newValue) {
        boolean changeCurrent = true;

        if (hardcoded() && value.equals(newValue)) {
            return;
        }

        if (hardcoded() || (rulesFlags & RULE_INPUT_MOVES_INPUT) == RULE_INPUT_MOVES_INPUT) {
            // we should push new value further without replacing the current one
            pushValueToSlot(newValue, nextSlot);
            changeCurrent = false;
        }

        if (value != null && (rulesFlags & RULE_INPUT_MOVES_CURRENT) == RULE_INPUT_MOVES_CURRENT) {
            // we should push current value further without
            pushValueToSlot(value, nextSlot);
        }

        if (changeCurrent) {
            value = newValue;
        }
    }

    private void removeCurrentValue() {
        if (!hardcoded()) {
            value = pullValueFromSlot(nextSlot);
        } else if (prevSlot != null) {
            prevSlot.removeCurrentValue();
        }
    }

    private void pushValueToSlot(Character newValue, Slot slot) {
        if (slot == null) {
            return;
        }

        nextSlot.setValue(newValue);
    }

    private Character pullValueFromSlot(Slot slot) {
        if (slot == null) {
            return null;
        }

        Character result = null;

        if (!slot.hardcoded()) {
            result = slot.getValue();
            slot.removeCurrentValue();
        } else if (slot.getNextSlot() != null) {
            result = pullValueFromSlot(slot.getNextSlot());
        }

        return result;
    }

    public Slot getNextSlot() {
        return nextSlot;
    }

    public void setNextSlot(Slot nextSlot) {
        this.nextSlot = nextSlot;
    }

    public Slot getPrevSlot() {
        return prevSlot;
    }

    public void setPrevSlot(Slot prevSlot) {
        this.prevSlot = prevSlot;
    }

    public SlotValidatorSet getValidators() {
        return validators;
    }

    public void setValidators(SlotValidatorSet validators) {
        this.validators = validators;
    }

    public Set<Integer> getTags() {
        return tags;
    }

    public Slot withTags(Integer... tags) {
        if (tags == null) {
            return this;
        }

        for (Integer tag : tags) {
            if (tag != null) {
                this.tags.add(tag);
            }
        }
        return this;
    }

    public boolean hasTag(Integer tag) {
        if (tag == null) {
            return false;
        }

        return tags.contains(tag);
    }

    @Override
    public String toString() {
        return "Slot{" +
                "value=" + value +
                '}';
    }

    public interface SlotValidator {
        boolean validate(final char value);
    }

}
