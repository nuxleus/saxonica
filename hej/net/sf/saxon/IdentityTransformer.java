package net.sf.saxon;

import net.sf.saxon.event.NamespaceReducer;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.Sender;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.SerializerFactory;
import net.sf.saxon.trans.XPathException;
import org.xml.sax.SAXParseException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

class IdentityTransformer extends Controller {

    protected IdentityTransformer(Configuration config) {
        super(config);
    }

    /**
    * Perform identify transformation from Source to Result
    */

    public void transform(Source source, Result result)
    throws TransformerException {
        try {
            if (getConfiguration().getEditionCode().equals("EE")) {
                getExecutable().setSchemaAware(true);
            }
            PipelineConfiguration pipe = makePipelineConfiguration();
            SerializerFactory sf = getConfiguration().getSerializerFactory();
            Receiver receiver = sf.getReceiver(
                    result, pipe, getOutputProperties());
            NamespaceReducer reducer = new NamespaceReducer(receiver);
            ParseOptions options = pipe.getParseOptions();
            options.setContinueAfterValidationErrors(true);
            Sender.send(source, reducer, options);
        } catch (XPathException err) {
            Throwable cause = err.getException();
            if (cause != null && cause instanceof SAXParseException) {
                // This generally means the error was already reported.
                // But if a RuntimeException occurs in Saxon during a callback from
                // the Crimson parser, Crimson wraps this in a SAXParseException without
                // reporting it further.
                SAXParseException spe = (SAXParseException)cause;
                cause = spe.getException();
                if (cause instanceof RuntimeException) {
                    reportFatalError(err);
                }
            } else {
                reportFatalError(err);
            }
            throw err;
        }
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