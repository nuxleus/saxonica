package net.sf.saxon.value;

import net.sf.saxon.expr.RangeIterator;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;

/**
 * This class represents a sequence of consecutive ascending integers, for example 1 to 50.
 * The integers must be within the range of a Java long.
 */

public class IntegerRange extends Value<IntegerValue> implements GroundedValue<IntegerValue> {

    public long start;
    public long end;

    /**
     * Construct an integer range expression
     * @param start the first integer in the sequence (inclusive)
     * @param end the last integer in the sequence (inclusive). Must be >= start
     * @throws net.sf.saxon.trans.XPathException if the sequence is longer than the Saxon limit (2^31)
     */

    public IntegerRange(long start, long end) throws XPathException {
        if (end < start) {
            throw new IllegalArgumentException("end < start in IntegerRange");
        }
        if (end - start > Integer.MAX_VALUE) {
            throw new XPathException("Maximum length of sequence in Saxon is " + Integer.MAX_VALUE);
        }
        this.start = start;
        this.end = end;
    }

    /**
     * Get the first integer in the sequence (inclusive)
     * @return the first integer in the sequence (inclusive)
     */

    public long getStart() {
        return start;
    }

   /**
     * Get the last integer in the sequence (inclusive)
     * @return the last integer in the sequence (inclusive)
     */

    public long getEnd() {
        return end;
    }



    /**
     * Return an Iterator to iterate over the values of a sequence. The value of every
     * expression can be regarded as a sequence, so this method is supported for all
     * expressions. This default implementation handles iteration for expressions that
     * return singleton values: for non-singleton expressions, the subclass must
     * provide its own implementation.
     *
     * @return a SequenceIterator that can be used to iterate over the result
     *         of the expression
     * @throws net.sf.saxon.trans.XPathException
     *          if any dynamic error occurs evaluating the
     *          expression
     */

    /*@NotNull*/ public SequenceIterator<IntegerValue> iterate() throws XPathException {
        return new RangeIterator(start, end);
    }

    /**
     * Determine the data type of the items in the expression, if possible
     *
     * @return AnyItemType (not known)
     * @param th the type hierarchy cache
     */

    /*@NotNull*/ public ItemType getItemType(TypeHierarchy th) {
        return BuiltInAtomicType.INTEGER;
    }

    /**
     * Determine the cardinality
     */

    public int getCardinality() {
        return StaticProperty.ALLOWS_MANY;
    }

    /**
     * Get the n'th item in the sequence (starting from 0). This is defined for all
     * Values, but its real benefits come for a sequence Value stored extensionally
     * (or for a MemoClosure, once all the values have been read)
     */

    /*@Nullable*/ public IntegerValue itemAt(int n) {
        if (n < 0 || n > (end-start)) {
            return null;
        }
        return Int64Value.makeIntegerValue(start + n);
    }


    /**
     * Get a subsequence of the value
     *
     *
     * @param start  the index of the first item to be included in the result, counting from zero.
     *               A negative value is taken as zero. If the value is beyond the end of the sequence, an empty
     *               sequence is returned
     * @param length the number of items to be included in the result. Specify Integer.MAX_VALUE to
     *               get the subsequence up to the end of the base sequence. If the value is negative, an empty sequence
     *               is returned. If the value goes off the end of the sequence, the result returns items up to the end
     *               of the sequence
     * @return the required subsequence.
     */

    /*@NotNull*/ public GroundedValue<IntegerValue> subsequence(int start, int length) {
        if (length <= 0) {
            return EmptySequence.getInstance();
        }
        long newStart = this.start + (start > 0 ? start : 0);
        long newEnd = newStart + length - 1;
        if (newEnd > end) {
            newEnd = end;
        }
        if (newEnd >= newStart) {
            try {
                return new IntegerRange(newStart, newEnd);
            } catch (XPathException e) {
                // Can't happen - if a range is valid, then all subranges are valied
                throw new IllegalStateException(e);
            }
        } else {
            return EmptySequence.getInstance();
        }
    }

    /**
     * Get the length of the sequence
     */

    public int getLength() {
        return (int)(end - start + 1);
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