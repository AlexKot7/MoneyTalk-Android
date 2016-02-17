package ru.tinkoff.telegram.mt.utils.formatting;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import ru.tinkoff.telegram.mt.utils.formatting.slots.Slot;


/**
 * @author Mikhail Artemyev
 */
public class Mask implements Iterable<Slot> {
    private static final char PLACEHOLDER_DEFAULT = '_';

    private static final int TAG_EXTENSION = -149635;

    private Slot firstSlot;
    private Slot lastSlot;
    private int size = 0;
    private boolean terminated = true;
    private Character placeholder;
    private boolean showingEmptySlots = false;

    private boolean showHardcodedTail = true;

    public Mask(Slot[] slots, boolean terminated) {
        this.terminated = terminated;
        this.size = slots.length;

        if (this.size == 0) {
            return;
        }

        this.firstSlot = new Slot(slots[0]);
        Slot prev = this.firstSlot;

        // link slots
        for (int i = 1; i < slots.length; i++) {
            Slot next = new Slot(slots[i]);
            prev.setNextSlot(next);
            next.setPrevSlot(prev);

            prev = next;

            if (i == slots.length - 1) {
                this.lastSlot = next;
            }
        }

    }

    @Override
    public String toString() {
        return toStringFrom(firstSlot);
    }

    @Override
    public Iterator<Slot> iterator() {
        return new MaskIterator(firstSlot);
    }

    public String toStringFrom(final Slot startSlot) {
        StringBuilder result = new StringBuilder();

        // create a string out of slots values
        Slot slot = startSlot;
        while (slot != null) {
            Character c = slot.getValue();

            boolean anyInputFromHere = slot.anyInputToTheRight();

            if (!anyInputFromHere) {
                // user input nothing to the right from this point
                if (!showHardcodedTail || !checkIsIndex(slot.hardcodedSequenceEndIndex() - 1)) {
                    break;
                }
            }

            // if we've met slot with no value we got two options:
            // 1) Stop further output. This option should apply when we have no further input value
            //                         (except hardcoded)
            // 2) Continue with showing placeholder. This option is for a case when there's some
            //                                       after current slot or {@code showingEmptySlots} = true
            if (c == null && (showingEmptySlots || anyInputFromHere)) {
                c = getPlaceholder();
            } else if (c == null) {
                break;
            }


            result.append(c);
            slot = slot.getNextSlot();
        }

        return result.toString();
    }


    public String toStringTags(Integer... tags) {

        StringBuilder result = new StringBuilder();

        Iterator<Slot> iterator = iterator();
        while (iterator.hasNext()) {
            Slot s = iterator.next();
            if (s.getValue() == null || s.getTags().size() == 0) {
                continue;
            }

            for (Integer searchTag : tags) {
                if (s.getTags().contains(searchTag)) {
                    result.append(s.getValue());
                }
            }
        }

        return result.toString();
    }

    /**
     * Looks for initial position for cursor since buffer can be predefined with starting characters
     */
    public int getInitialInputPosition() {

        int cursorPosition = 0;

        Slot slot = getSlot(cursorPosition);
        while (slot != null && slot.getValue() != null) {
            cursorPosition++;
            slot = slot.getNextSlot();
        }

        return cursorPosition;
    }

    /**
     * Checks whether user filled whole mask.
     *
     * @return
     */
    public boolean filled() {
        Iterator<Slot> maskIterator = iterator();

        while (maskIterator.hasNext()) {
            Slot s = maskIterator.next();
            if (s.hasTag(TAG_EXTENSION)) {
                continue;
            }

            if (!s.hardcoded() && s.getValue() == null) {
                return false;
            }
        }

        return true;
    }

    /**
     * Method insert {@code input} to the buffer. Only validated characters would be inserted.
     * Hardcoded slots are omitted.
     * Method returns new cursor position that is affected by input and
     * {@code cursorAfterTrailingHardcoded} flag. In most cases if input string is followed by
     * a sequence of hardcoded characters we should place cursor after them. But this behaviour can
     * be modified by {@code cursorAfterTrailingHardcoded} flag.
     *
     * @param input                        string to insert
     * @param position                     from which position to begin input
     * @param cursorAfterTrailingHardcoded when input is followed by a hardcoded characters sequence
     *                                     then this flag defines whether new cursor position should
     *                                     be after or before them
     * @return cursor position after insert
     */
    public int insertAt(final CharSequence input, final int position, boolean cursorAfterTrailingHardcoded) {
        showHardcodedTail = true;
        if (!checkIsIndex(position) || input == null || input.length() == 0) {
            return position;
        }

        int cursorPosition = position;
        Slot slotCandidate = getSlot(position);

        Deque<Character> inStack = dequeFrom(input);

        while (!inStack.isEmpty()) {

            char newValue = inStack.pop();
            cursorPosition += validSlotIndexOffset(slotCandidate, newValue);
            Slot slotForInput = getSlot(cursorPosition);
            if (slotForInput == lastSlot && !terminated) {
                // extend mask to fit all VALID input characters (if mask non-terminated)
                extendTail(slotForInput.getValidators().countValidIn(inStack));
            }

            if (slotForInput != null) {
                slotCandidate = slotForInput;
                slotCandidate.setValue(newValue);

                slotCandidate = slotCandidate.getNextSlot();
                cursorPosition++;
            }

        }

        if (cursorAfterTrailingHardcoded) {
            int hardcodedTailLength = 0;
            if (slotCandidate != null) {
                hardcodedTailLength = slotCandidate.hardcodedSequenceEndIndex();
            }

            if (hardcodedTailLength > 0) {
                cursorPosition += hardcodedTailLength;
            }
        }

        return cursorPosition;
    }

    /**
     * Removes available symbols from the buffer. This method should be called on deletion event of
     * user's input. Symbols are deleting backwards (just as backspace key).
     * Hardcoded symbols would not be deleted, only cursor will be moved over them.
     * <p/>
     * Method also updates {@code showHardcodedTail} flag that defines whether tail of hardcoded
     * symbols (at the end of user's input) should be shown. In most cases that should not. The
     * only case when they are visible - buffer starts with them and deletion was inside them.
     *
     * @param position from where to start deletion
     * @param count    number of  symbols to delete.
     * @return new cursor position after deletion
     */
    public int removeBackwards(int position, int count) {

        // go back fom position and remove any non-hardcoded characters
        for (int i = 0; i < count; i++) {
            if (checkIsIndex(position)) {
                Slot s = getSlot(position);
                if (!s.hardcoded()) {
                    s.setValue(null);
                }
            }


            position--;
        }

        trimTail();

        int cursorPosition = position;

        // We could remove a symbol before a sequence of hardcoded characters
        // that are now tail. It this case our cursor index will point at non printable
        // character. To avoid this find next not-hardcoded symbol to the left
        Slot slot = getSlot(cursorPosition);
        while (slot != null && slot.hardcoded() && cursorPosition > 0) {
            slot = getSlot(--cursorPosition);
        }

        // check if we've reached begin of the string
        // this can happen not only because we've been 'deleting' hardcoded characters
        // at he begin of the string.
        showHardcodedTail = cursorPosition <= 0;
        if (showHardcodedTail) {
            cursorPosition = position;
        }

        cursorPosition++;

        return checkIsIndex(cursorPosition) ? cursorPosition : 0;
    }

    public int getSize() {
        return size;
    }

    public boolean isShowingEmptySlots() {
        return showingEmptySlots;
    }

    public void setShowingEmptySlots(boolean showingEmptySlots) {
        this.showingEmptySlots = showingEmptySlots;
    }

    public Character getPlaceholder() {
        return placeholder != null ? placeholder : PLACEHOLDER_DEFAULT;
    }

    public void setPlaceholder(Character placeholder) {
        this.placeholder = placeholder;
    }

    public int getFirstIndexOfTag(int tag) {
        int index = -1;
        Iterator<Slot> iterator = iterator();
        while (iterator.hasNext()) {
            index++;
            Slot s = iterator.next();
            if (s.hasTag(tag)) {
                return index;
            }
        }

        return index;
    }

    public int getLastIndexOfTag(int tag) {
        int index = -1;

        Slot slot = lastSlot;
        while (slot != null) {
            index++;
            if (slot.hasTag(tag)) {
                return index;
            }

            slot = slot.getPrevSlot();
        }

        return index;
    }


    /**
     * Removes slot at specified position
     *
     * @param position position of the slot that should be removed
     */
    private void removeSlotAt(int position) {
        removeSlot(getSlot(position));
    }

    /**
     * Removes specified slot
     *
     * @param slotToRemove
     */
    private void removeSlot(Slot slotToRemove) {
        if (slotToRemove == null) {
            return;
        }

        Slot left = slotToRemove.getPrevSlot();
        Slot right = slotToRemove.getNextSlot();

        if (left != null) {
            left.setNextSlot(right);
        } else {
            firstSlot = right;
        }

        if (right != null) {
            right.setPrevSlot(left);
        } else {
            lastSlot = left;
        }

        size--;

    }

    /**
     * Returns slot by its index
     *
     * @param index index of a slot
     * @return null if index is incorrect and slot otherwise
     */
    private Slot getSlot(int index) {
        if (!checkIsIndex(index)) {
            return null;
        }

        Slot result;

        if (index < (size >> 1)) {
            // first half of a list
            result = firstSlot;
            for (int i = 0; i < index; i++) {
                result = result.getNextSlot();
            }
        } else {
            // second half of a list
            result = lastSlot;
            for (int i = size - 1; i > index; i--) {
                result = result.getPrevSlot();
            }
        }

        if (result == null) {
            throw new IllegalStateException("Slot inside the mask should not be null. But it is.");
        }

        return result;
    }

    /**
     * Looks for a slot to insert {@code value}. Search moves to the right from the specified
     * one (including it).
     *
     * @param slot  slot from where to start
     * @param value value to be inserted to slot
     * @return index offset from the slot that is passed in {@code slot} argument to the found one
     */
    private int validSlotIndexOffset(Slot slot, final char value) {
        int indexOffset = 0;
        while (slot != null && !slot.canInsertHere(value)) {
            slot = slot.getNextSlot();
            indexOffset++;
        }

        return indexOffset;
    }

    /**
     * Checks whether @{code position} posits at a slot
     *
     * @param position
     * @return
     */
    private boolean checkIsIndex(int position) {
        return 0 <= position && position < size;
    }

    /**
     * Inserts slots at the and of the mask and mark newly inserted slot as 'extension'
     * (extended tail of non-terminated mask). 'Extended' slots will be removed when their values
     * are cleared
     */
    private void extendTail(int count) {
        if (terminated || count < 1) {
            return;
        }

        while (--count >= 0) {
            // create a copy of the last slot and make it the last one
            final Slot inserted = insertSlotAt(size, lastSlot);
            inserted.withTags(TAG_EXTENSION);
        }
    }

    /**
     * Inserts a slot on a specified position
     *
     * @param position index where new slot weill be placed should be >= 0 and <= size.
     * @param slot     slot ot insert. IMPORTANT: a copy of this slot will be inserted!
     * @return newly inserted slot (copy of the passed one)
     */
    private Slot insertSlotAt(final int position, final Slot slot) {

        if (position < 0 || size < position) {
            throw new IllegalArgumentException("New slot position should be inside the mask. Or on the tail (position = size)");
        }

        if (slot == null) {
            throw new IllegalArgumentException("Slot cannot be null");
        }

        final Slot toInsert = new Slot(slot);

        Slot currentSlot = getSlot(position);
        Slot leftNeighbour;
        Slot rightNeighbour = null;
        if (currentSlot == null) {
            // this can happen only when position == size.
            // it means we want to add the slot on the tail
            leftNeighbour = lastSlot;
        } else {
            leftNeighbour = currentSlot.getPrevSlot();
            rightNeighbour = currentSlot;
        }

        toInsert.setNextSlot(rightNeighbour);
        toInsert.setPrevSlot(leftNeighbour);

        if (rightNeighbour != null) {
            // right neighbour is only available for non-last slots
            rightNeighbour.setPrevSlot(toInsert);
        }

        if (leftNeighbour != null) {
            // left neighbour is only available for not-first slots
            leftNeighbour.setNextSlot(toInsert);
        }

        if (position == 0) {
            firstSlot = toInsert;
        } else if (position == size) {
            lastSlot = toInsert;
        }

        size++;

        return toInsert;
    }

    private void trimTail() {
        if (terminated || lastSlot == null) {
            return;
        }

        Slot currentSlot = lastSlot;
        Slot prevSlot = currentSlot.getPrevSlot();
        while (
                currentSlot.hasTag(TAG_EXTENSION) &&
                        prevSlot.hasTag(TAG_EXTENSION) &&
                        currentSlot.getValue() == null
                ) {
            removeSlotAt(size - 1);
            currentSlot = prevSlot;
            prevSlot = prevSlot.getPrevSlot();
        }
    }

    /**
     * Creates deque (double-side queue) of CharSequence
     *
     * @param in char sequence to be converted to deque
     * @return
     */
    private Deque<Character> dequeFrom(CharSequence in) {
        if (in == null) {
            return null;
        }

        final Deque<Character> out = new ArrayDeque<>(in.length());

        for (int i = in.length() - 1; i >= 0; i--) {
            out.push(in.charAt(i));
        }

        return out;
    }

    public class MaskIterator implements Iterator<Slot> {

        Slot nextSlot;

        public MaskIterator(Slot currentSlot) {
            if (currentSlot == null) {
                throw new IllegalArgumentException("Initial slot for iterator cannot be null");
            }

            this.nextSlot = currentSlot;
        }

        @Override
        public boolean hasNext() {
            return nextSlot != null;
        }

        @Override
        public Slot next() {
            Slot current = nextSlot;
            nextSlot = nextSlot.getNextSlot();
            return current;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Mask cannot be modified from outside!");
        }
    }
}
