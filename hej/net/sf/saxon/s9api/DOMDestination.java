package net.sf.saxon.s9api;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DOMWriter;
import net.sf.saxon.event.Receiver;

/**
 * This class represents a Destination (for example, the destination of the output of a transformation)
 * in which the results are written to a newly constructed DOM tree in memory. The caller must supply
 * a Document node, which will be used as the root of the constructed tree
 */

public class DOMDestination implements Destination {

    private DOMWriter domWriter;

    /**
     * Create a DOMDestination, supplying the root of a DOM document to which the
     * content of the result tree will be appended.
     * @param root the root node for the new tree.
     */

    public DOMDestination(org.w3c.dom.Document root) {
        domWriter = new DOMWriter();
        domWriter.setNode(root);
    }

    /**
     * Return a Receiver. Saxon calls this method to obtain a Receiver, to which it then sends
     * a sequence of events representing the content of an XML document.
     *
     * @param config The Saxon configuration. This is supplied so that the destination can
     *               use information from the configuration (for example, a reference to the name pool)
     *               to construct or configure the returned Receiver.
     * @return the Receiver to which events are to be sent.
     * @throws SaxonApiException
     *          if the Receiver cannot be created
     */

    public Receiver getReceiver(/*@NotNull*/ Configuration config) throws SaxonApiException {
        domWriter.setPipelineConfiguration(config.makePipelineConfiguration());
        return domWriter;
    }

    /**
     * Close the destination, allowing resources to be released. Saxon calls this method when
     * it has finished writing to the destination.
     */

    public void close() throws SaxonApiException {
        // no action
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