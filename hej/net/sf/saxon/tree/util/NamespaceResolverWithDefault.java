package net.sf.saxon.tree.util;

import net.sf.saxon.om.NamespaceResolver;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *  This class is a NamespaceResolver that modifies an underyling NamespaceResolver
 *  by changing the mapping of the null prefix to be a specified namespace, rather than
 *  the one used by the underlying namespace resolver.
 */
public class NamespaceResolverWithDefault implements NamespaceResolver {

    private NamespaceResolver baseResolver;
    private String defaultNamespace;

    public NamespaceResolverWithDefault(NamespaceResolver base, String defaultNamespace) {
        this.baseResolver = base;
        this.defaultNamespace = defaultNamespace;
    }

    /**
     * Get the namespace URI corresponding to a given prefix. Return null
     * if the prefix is not in scope.
     * @param prefix     the namespace prefix. May be the zero-length string, indicating
     *                   that there is no prefix. This indicates either the default namespace or the
     *                   null namespace, depending on the value of useDefault.
     * @param useDefault true if the default namespace is to be used when the
     *                   prefix is "". If false, the method returns "" when the prefix is "".
     * @return the uri for the namespace, or null if the prefix is not in scope.
     *         The "null namespace" is represented by the pseudo-URI "".
     */

    /*@Nullable*/ public String getURIForPrefix(/*@NotNull*/ String prefix, boolean useDefault) {
        if (useDefault && prefix.length()==0) {
            return defaultNamespace;
        } else {
            return baseResolver.getURIForPrefix(prefix, useDefault);
        }
    }

    /**
     * Get an iterator over all the prefixes declared in this namespace context. This will include
     * the default namespace (prefix="") and the XML namespace where appropriate
     */

    public Iterator<String> iteratePrefixes() {
        ArrayList<String> list = new ArrayList<String>(10);
        for(Iterator<String> it = baseResolver.iteratePrefixes(); it.hasNext(); ) {
            String p = it.next();
            if (p.length() != 0) {
                list.add(p);
            }
        }
        list.add("");
        return list.iterator();
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