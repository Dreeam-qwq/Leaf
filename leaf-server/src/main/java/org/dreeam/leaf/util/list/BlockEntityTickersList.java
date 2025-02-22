package org.dreeam.leaf.util.list;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.level.block.entity.TickingBlockEntity;

import java.util.Arrays;
import java.util.Collection;

/**
 * A list for ServerLevel's blockEntityTickers
 * <p>
 * This list behaves identically to ObjectArrayList, but it has an additional method, `removeAllByIndex`, that allows a list of integers to be passed indicating what
 * indexes should be deleted from the list
 * <p>
 * This is faster than using removeAll, since we don't need to compare the identity of each block entity, and faster than looping thru each index manually and deleting with remove,
 * since we don't need to resize the array every single remove.
 */
public final class BlockEntityTickersList extends ObjectArrayList<TickingBlockEntity> {

    private final IntOpenHashSet toRemove = new IntOpenHashSet();
    private int startSearchFromIndex = -1;

    /**
     * Creates a new array list with {@link #DEFAULT_INITIAL_CAPACITY} capacity.
     */
    public BlockEntityTickersList() {
        super();
    }

    /**
     * Creates a new array list and fills it with a given collection.
     *
     * @param c a collection that will be used to fill the array list.
     */
    public BlockEntityTickersList(final Collection<? extends TickingBlockEntity> c) {
        super(c);
    }

    /**
     * Marks an entry as removed
     *
     * @param index the index of the item on the list to be marked as removed
     */
    public void markAsRemoved(final int index) {
        // The block entities list always loop starting from 0, so we only need to check if the startSearchFromIndex is -1 and that's it
        if (this.startSearchFromIndex == -1)
            this.startSearchFromIndex = index;

        this.toRemove.add(index);
    }

    /**
     * Removes elements that have been marked as removed.
     */
    public void removeMarkedEntries() {
        if (this.startSearchFromIndex == -1) // No entries in the list, skip
            return;

        removeAllByIndex(startSearchFromIndex, toRemove);
        toRemove.clear();
        this.startSearchFromIndex = -1; // Reset the start search index
    }

    /**
     * Removes elements by their index.
     */
    private void removeAllByIndex(final int startSearchFromIndex, final IntOpenHashSet c) { // can't use Set<Integer> because we want to avoid autoboxing when using contains
        final int requiredMatches = c.size();
        if (requiredMatches == 0)
            return; // exit early, we don't need to do anything

        final Object[] a = this.a;
        int j = startSearchFromIndex;
        int matches = 0;
        for (int i = startSearchFromIndex; i < size; i++) { // If the user knows the first index to be removed, we can skip a lot of unnecessary comparsions
            if (!c.contains(i)) {
                // TODO: It can be possible to optimize this loop by tracking the start/finish and then using arraycopy to "skip" the elements,
                //  this would optimize cases where the index to be removed are far apart, HOWEVER it does have a big performance impact if you are doing
                //  "arraycopy" for each element
                a[j++] = a[i];
            } else {
                matches++;
            }

            if (matches == requiredMatches) { // Exit the loop if we already removed everything, we don't need to check anything else
                // We need to update the final size here, because we know that we already found everything!
                // Because we know that the size must be currentSize - requiredMatches (because we have matched everything), let's update the value
                // However, we need to copy the rest of the stuff over
                if (i != (size - 1)) { // If it isn't the last index...
                    // i + 1 because we want to copy the *next* element over
                    // and the size - i - 1 is because we want to get the current size, minus the current index (which is i), and then - 1 because we want to copy -1 ahead (remember, we are adding +1 to copy the *next* element)
                    System.arraycopy(a, i + 1, a, j, size - i - 1);
                }
                j = size - requiredMatches;
                break;
            }
        }

        Arrays.fill(a, j, size, null);
        size = j;
    }
}
