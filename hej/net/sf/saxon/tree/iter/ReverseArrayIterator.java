package net.sf.saxon.tree.iter;

import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;


/**
  * ReverseArrayIterator is used to enumerate items held in an array in reverse order.
  * @author Michael H. Kay
  */


public class ReverseArrayIterator<T extends Item> implements UnfailingIterator<T>,
        ReversibleIterator<T>, LookaheadIterator<T>, LastPositionFinder<T> {

    T[] items;
    int index = 0;
    int start;
    int end;         // item after the last to be output
    /*@Nullable*/ T current = null;

    /**
     * Create an iterator a slice of an array
     * @param items The array of items
     * @param start The first item in the array to be be used (this will be the last
     * one in the resulting iteration). Zero-based.
     * @param end The item after the last one in the array to be used (this will be the
     * first one to be returned by the iterator). Zero-based.
    */

    public ReverseArrayIterator(T[] items, int start, int end) {
        this.items = items;
        this.end = end;
        this.start = start;
        index = end - 1;
    }

    /**
     * Determine whether there are more items to come. Note that this operation
     * is stateless and it is not necessary (or usual) to call it before calling
     * next(). It is used only when there is an explicit need to tell if we
     * are at the last element.
     *
     * @return true if there are more items in the sequence
     */

    public boolean hasNext() {
        return index >= start;
    }

    /*@Nullable*/ public T next() {
        if (index >= start) {
            current = items[index--];
            return current;
        } else {
            current = null;
            return null;
        }
    }

    /*@Nullable*/ public T current() {
        return current;
    }

    public int position() {
        if (index < start-1) {
            return -1;  // position() returns -1 after next() returns null
        }
        return end - 1 - index;
    }

    public int getLength() {
        return end - start;
    }

    public void close() {
    }

    /*@NotNull*/
    public ReverseArrayIterator<T> getAnother() {
        return new ReverseArrayIterator<T>(items, start, end);
    }

    /**
     * Get properties of this iterator, as a bit-significant integer.
     *
     * @return the properties of this iterator. This will be some combination of
     *         properties such as {@link #GROUNDED}, {@link #LAST_POSITION_FINDER},
     *         and {@link #LOOKAHEAD}. It is always
     *         acceptable to return the value zero, indicating that there are no known special properties.
     *         It is acceptable for the properties of the iterator to change depending on its state.
     */

    public int getProperties() {
        return LAST_POSITION_FINDER;
    }

    /**
     * Get an iterator that processes the same items in reverse order.
     * Since this iterator is processing the items backwards, this method
     * returns an ArrayIterator that processes them forwards.
     * @return a new ArrayIterator
     */

    public SequenceIterator<T> getReverseIterator() {
        return new ArrayIterator<T>(items, start, end);
    }
}

//
// The contents of this file are subject to the Mozilla Public License Version 1.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the
// License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations under the License.
//
// The Original Code is: all this file
//
// The Initial Developer of the Original Code is Saxonica Limited.
// Portions created by ___ are Copyright (C) ___. All rights reserved.
//
// Contributor(s):
//