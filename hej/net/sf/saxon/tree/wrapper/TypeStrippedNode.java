package net.sf.saxon.tree.wrapper;

import net.sf.saxon.Configuration;
import net.sf.saxon.tree.wrapper.VirtualNode;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.om.*;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.UntypedAtomicValue;
import net.sf.saxon.value.Value;


/**
 * A TypeStrippedNode is a view of a node, in a virtual tree that has type
 * annotations stripped from it.
*/

public class TypeStrippedNode implements NodeInfo, VirtualNode {

    protected NodeInfo node;
    protected TypeStrippedNode parent;     // null means unknown
    protected TypeStrippedDocument docWrapper;

    protected TypeStrippedNode() {}

    /**
     * This constructor is protected: nodes should be created using the makeWrapper
     * factory method
     * @param node    The node to be wrapped
     * @param parent  The StrippedNode that wraps the parent of this node
     */

    protected TypeStrippedNode(NodeInfo node, TypeStrippedNode parent) {
        this.node = node;
        this.parent = parent;
    }

    /**
     * Factory method to wrap a node with a wrapper that implements the Saxon
     * NodeInfo interface.
     * @param node        The underlying node
     * @param docWrapper  The wrapper for the document node (must be supplied)
     * @param parent      The wrapper for the parent of the node (null if unknown)
     * @return            The new wrapper for the supplied node
     */

    protected TypeStrippedNode makeWrapper(NodeInfo node,
                                       TypeStrippedDocument docWrapper,
                                       TypeStrippedNode parent) {
        TypeStrippedNode wrapper = new TypeStrippedNode(node, parent);
        wrapper.docWrapper = docWrapper;
        return wrapper;
    }

    /**
    * Get the underlying DOM node, to implement the VirtualNode interface
    */

    public Object getUnderlyingNode() {
        return node;
    }

    /**
     * Get the node underlying this virtual node. If this is a VirtualNode the method
     * will automatically drill down through several layers of wrapping.
     * @return The underlying node.
     */

    public Object getRealNode() {
        Object u = this;
        do {
            u = ((VirtualNode)u).getUnderlyingNode();
        } while (u instanceof VirtualNode);
        return u;
    }


    /**
     * Get the configuration
     */

    public Configuration getConfiguration() {
        return node.getConfiguration();
    }

    /**
     * Get the name pool for this node
     * @return the NamePool
     */

    public NamePool getNamePool() {
        return node.getNamePool();
    }

    /**
    * Return the type of node.
    * @return one of the values Node.ELEMENT, Node.TEXT, Node.ATTRIBUTE, etc.
    */

    public int getNodeKind() {
        return node.getNodeKind();
    }

    /**
    * Get the typed value of the item
    */

    public SequenceIterator getTypedValue() throws XPathException {
        return atomize().iterate();
    }

    /**
     * Get the typed value. The result of this method will always be consistent with the method
     * {@link net.sf.saxon.om.Item#getTypedValue()}. However, this method is often more convenient and may be
     * more efficient, especially in the common case where the value is expected to be a singleton.
     *
     * @return the typed value. If requireSingleton is set to true, the result will always be an
     *         AtomicValue. In other cases it may be a Value representing a sequence whose items are atomic
     *         values.
     * @since 8.5
     */

    public Value atomize() throws XPathException {
        return new UntypedAtomicValue(getStringValueCS());
    }

    /**
    * Get the type annotation
    * @return untyped or untypedAtomic (the purpose of this class is to strip the type annotation)
    */

    public int getTypeAnnotation() {
        if (getNodeKind() == Type.ELEMENT) {
            return StandardNames.XS_UNTYPED;
        } else {
            return StandardNames.XS_UNTYPED_ATOMIC;
        }
    }

    /**
    * Determine whether this is the same node as another node. <br />
    * Note: a.isSameNode(b) if and only if generateId(a)==generateId(b)
    * @return true if this Node object and the supplied Node object represent the
    * same node in the tree.
    */

    public boolean isSameNodeInfo(NodeInfo other) {
        if (other instanceof TypeStrippedNode) {
            return node.isSameNodeInfo(((TypeStrippedNode)other).node);
        } else {
            return node.isSameNodeInfo(other);
        }
    }

   /**
      * The equals() method compares nodes for identity. It is defined to give the same result
      * as isSameNodeInfo().
      * @param other the node to be compared with this node
      * @return true if this NodeInfo object and the supplied NodeInfo object represent
      *      the same node in the tree.
      * @since 8.7 Previously, the effect of the equals() method was not defined. Callers
      * should therefore be aware that third party implementations of the NodeInfo interface may
      * not implement the correct semantics. It is safer to use isSameNodeInfo() for this reason.
      * The equals() method has been defined because it is useful in contexts such as a Java Set or HashMap.
      */

     public boolean equals(Object other) {
       return other instanceof NodeInfo && isSameNodeInfo((NodeInfo)other);
   }

     /**
      * The hashCode() method obeys the contract for hashCode(): that is, if two objects are equal
      * (represent the same node) then they must have the same hashCode()
      * @since 8.7 Previously, the effect of the equals() and hashCode() methods was not defined. Callers
      * should therefore be aware that third party implementations of the NodeInfo interface may
      * not implement the correct semantics.
      */

     public int hashCode() {
         return node.hashCode() ^ 0x2c2c2c2c;
     }

    /**
    * Get the System ID for the node.
    * @return the System Identifier of the entity in the source document containing the node,
    * or null if not known. Note this is not the same as the base URI: the base URI can be
    * modified by xml:base, but the system ID cannot.
    */

    public String getSystemId() {
        return node.getSystemId();
    }

    public void setSystemId(String uri) {
        node.setSystemId(uri);
    }

    /**
    * Get the Base URI for the node, that is, the URI used for resolving a relative URI contained
     * in the node.
    */

    public String getBaseURI() {
        return node.getBaseURI();
    }

    /**
    * Get line number
    * @return the line number of the node in its original source document; or -1 if not available
    */

    public int getLineNumber() {
        return node.getLineNumber();
    }

   /**
     * Get column number
     * @return the column number of the node in its original source document; or -1 if not available
     */

    public int getColumnNumber() {
        return node.getColumnNumber();
    }

    /**
    * Determine the relative position of this node and another node, in document order.
    * The other node will always be in the same document.
    * @param other The other node, whose position is to be compared with this node
    * @return -1 if this node precedes the other node, +1 if it follows the other
    * node, or 0 if they are the same node. (In this case, isSameNode() will always
    * return true, and the two nodes will produce the same result for generateId())
    */

    public int compareOrder(NodeInfo other) {
        if (other instanceof TypeStrippedNode) {
            return node.compareOrder(((TypeStrippedNode)other).node);
        } else {
            return node.compareOrder(other);
        }
    }

    /**
    * Return the string value of the node. The interpretation of this depends on the type
    * of node. For an element it is the accumulated character content of the element,
    * including descendant elements.
    * @return the string value of the node
    */

    public String getStringValue() {
        return getStringValueCS().toString();
    }

    /**
     * Get the value of the item as a CharSequence. This is in some cases more efficient than
     * the version of the method that returns a String.
     */

    public CharSequence getStringValueCS() {
        return node.getStringValueCS();
    }

	/**
	* Get name code. The name code is a coded form of the node name: two nodes
	* with the same name code have the same namespace URI, the same local name,
	* and the same prefix. By masking the name code with &0xfffff, you get a
	* fingerprint: two nodes with the same fingerprint have the same local name
	* and namespace URI.
    * @see net.sf.saxon.om.NamePool#allocate allocate
	*/

	public int getNameCode() {
        return node.getNameCode();
	}

	/**
	* Get fingerprint. The fingerprint is a coded form of the expanded name
	* of the node: two nodes
	* with the same name code have the same namespace URI and the same local name.
	* A fingerprint of -1 should be returned for a node with no name.
	*/

	public int getFingerprint() {
	    return node.getFingerprint();
	}

    /**
    * Get the local part of the name of this node. This is the name after the ":" if any.
    * @return the local part of the name. For an unnamed node, returns null, except for
     * un unnamed namespace node, which returns "".
    */

    public String getLocalPart() {
        return node.getLocalPart();
    }

   /**
    * Get the URI part of the name of this node. This is the URI corresponding to the
    * prefix, or the URI of the default namespace if appropriate.
    * @return The URI of the namespace of this node. For an unnamed node, return null.
    * For a node with an empty prefix, return an empty string.
    */

    public String getURI() {
        return node.getURI();
    }

    /**
     * Get the prefix of the name of the node. This is defined only for elements and attributes.
     * If the node has no prefix, or for other kinds of node, return a zero-length string.
     *
     * @return The prefix of the name of the node.
     */

    public String getPrefix() {
        return node.getPrefix();
    }

    /**
    * Get the display name of this node. For elements and attributes this is [prefix:]localname.
    * For unnamed nodes, it is an empty string.
    * @return The display name of this node.
    * For a node with no name, return an empty string.
    */

    public String getDisplayName() {
        return node.getDisplayName();
    }

    /**
    * Get the NodeInfo object representing the parent of this node
    */

    public NodeInfo getParent() {
        if (parent==null) {
            NodeInfo realParent = node.getParent();
            if (realParent != null) {
                parent = makeWrapper(realParent, docWrapper, null);
            }
        }
        return parent;
    }

    /**
    * Return an iteration over the nodes reached by the given axis from this node
    * @param axisNumber the axis to be used
    * @return a SequenceIterator that scans the nodes reached by the axis in turn.
    */

    public AxisIterator iterateAxis(byte axisNumber) {
        return new WrappingIterator(node.iterateAxis(axisNumber), null);
    }

    /**
    * Return an iteration over the nodes reached by the given axis from this node
    * @param axisNumber the axis to be used
    * @param nodeTest A pattern to be matched by the returned nodes
    * @return a SequenceIterator that scans the nodes reached by the axis in turn.
    */

    public AxisIterator iterateAxis(byte axisNumber, NodeTest nodeTest) {
        return new Navigator.AxisFilter(iterateAxis(axisNumber), nodeTest);
    }

    /**
    * Get the value of a given attribute of this node
    * @param fingerprint The fingerprint of the attribute name
    * @return the attribute value if it exists or null if not
    */

    public String getAttributeValue(int fingerprint) {
        return node.getAttributeValue(fingerprint);
    }

    /**
    * Get the root node - always a document node with this tree implementation
    * @return the NodeInfo representing the containing document
    */

    public NodeInfo getRoot() {
        return docWrapper;
    }

    /**
    * Get the root (document) node
    * @return the DocumentInfo representing the containing document
    */

    public DocumentInfo getDocumentRoot() {
        return docWrapper;
    }

    /**
    * Determine whether the node has any children. <br />
    * Note: the result is equivalent to <br />
    * getEnumeration(Axis.CHILD, AnyNodeTest.getInstance()).hasNext()
    */

    public boolean hasChildNodes() {
        return node.hasChildNodes();
    }

    /**
     * Get a character string that uniquely identifies this node.
     * Note: a.isSameNode(b) if and only if generateId(a)==generateId(b)
     * @param buffer a buffer, into which will be placed
     * a string that uniquely identifies this node, within this
     * document. The calling code prepends information to make the result
     * unique across all documents.
     */

    public void generateId(FastStringBuffer buffer) {
        node.generateId(buffer);
    }

    /**
     * Get the document number of the document containing this node. For a free-standing
     * orphan node, just return the hashcode.
     */

    public long getDocumentNumber() {
        return docWrapper.getDocumentNumber();
    }

    /**
    * Copy this node to a given outputter (deep copy)
    */

    public void copy(Receiver out, int copyOptions, int locationId) throws XPathException {
        node.copy(out, copyOptions & ~CopyOptions.TYPE_ANNOTATIONS, locationId);
    }

    /**
     * Get all namespace undeclarations and undeclarations defined on this element.
     *
     * @param buffer If this is non-null, and the result array fits in this buffer, then the result
     *               may overwrite the contents of this array, to avoid the cost of allocating a new array on the heap.
     * @return An array of integers representing the namespace declarations and undeclarations present on
     *         this element. For a node other than an element, return null. Otherwise, the returned array is a
     *         sequence of namespace codes, whose meaning may be interpreted by reference to the name pool. The
     *         top half word of each namespace code represents the prefix, the bottom half represents the URI.
     *         If the bottom half is zero, then this is a namespace undeclaration rather than a declaration.
     *         The XML namespace is never included in the list. If the supplied array is larger than required,
     *         then the first unused entry will be set to -1.
     *         <p/>
     *         <p>For a node other than an element, the method returns null.</p>
     */

    public int[] getDeclaredNamespaces(int[] buffer) {
        return node.getDeclaredNamespaces(buffer);
    }


    /**
     * Determine whether this node has the is-id property
     *
     * @return true if the node is an ID
     */

    public boolean isId() {
        return node.isId();
    }

    /**
     * Determine whether this node has the is-idref property
     *
     * @return true if the node is an IDREF or IDREFS element or attribute
     */

    public boolean isIdref() {
        return node.isIdref();
    }

    /**
     * Determine whether the node has the is-nilled property
     *
     * @return true if the node has the is-nilled property
     */

    public boolean isNilled() {
        return false;
    }

    /**
     * A WrappingIterator delivers wrappers for the nodes delivered
     * by its underlying iterator. It is used when no whitespace stripping
     * is actually needed, e.g. for the attribute axis. But we still need to
     * create wrappers, so that further iteration remains in the virtual layer
     * rather than switching to the real nodes.
     */

    private final class WrappingIterator implements AxisIterator {

        AxisIterator base;
        TypeStrippedNode parent;
        NodeInfo current;
        boolean atomizing = false;

        /**
         * Create a WrappingIterator
         * @param base The underlying iterator
         * @param parent If all the nodes to be wrapped have the same parent,
         * it can be specified here. Otherwise specify null.
         */

        public WrappingIterator(AxisIterator base, TypeStrippedNode parent) {
            this.base = base;
            this.parent = parent;
        }

        /**
         * Move to the next node, without returning it. Returns true if there is
         * a next node, false if the end of the sequence has been reached. After
         * calling this method, the current node may be retrieved using the
         * current() function.
         */

        public boolean moveNext() {
            return (next() != null);
        }


        public Item next() {
            Item n = base.next();
            if (n instanceof NodeInfo && !atomizing) {
                current = makeWrapper((NodeInfo)n, docWrapper, parent);
            } else {
                current = (NodeInfo)n;
            }
            return current;
        }

        public Item current() {
            return current;
        }

        public int position() {
            return base.position();
        }

        public void close() {
            base.close();
        }

        /**
         * Return an iterator over an axis, starting at the current node.
         *
         * @param axis the axis to iterate over, using a constant such as
         *             {@link net.sf.saxon.om.Axis#CHILD}
         * @param test a predicate to apply to the nodes before returning them.
         * @throws NullPointerException if there is no current node
         */

        public AxisIterator iterateAxis(byte axis, NodeTest test) {
            return current.iterateAxis(axis, test);
        }

        /**
         * Return the atomized value of the current node.
         *
         * @return the atomized value.
         * @throws NullPointerException if there is no current node
         */

        public Value atomize() throws XPathException {
            return current.atomize();
        }

        /**
         * Return the string value of the current node.
         *
         * @return the string value, as an instance of CharSequence.
         * @throws NullPointerException if there is no current node
         */

        public CharSequence getStringValue() {
            return current.getStringValueCS();
        }

        public SequenceIterator getAnother() {
            return new WrappingIterator((AxisIterator)base.getAnother(), parent);
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
            return 0;
        }

        /**
         * Indicate that any nodes returned in the sequence will be atomized. This
         * means that if it wishes to do so, the implementation can return the typed
         * values of the nodes rather than the nodes themselves. The implementation
         * is free to ignore this hint.
         * @param atomizing true if the caller of this iterator will atomize any
         * nodes that are returned, and is therefore willing to accept the typed
         * value of the nodes instead of the nodes themselves.
         */

//        public void setIsAtomizing(boolean atomizing) {
//            this.atomizing = true;
//            if (base instanceof AtomizableIterator) {
//                ((AtomizableIterator)base).setIsAtomizing(atomizing);
//            }
//        }

    }  // end of class WrappingIterator


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
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is
// Michael H. Kay.
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
// Contributor(s): none.
//