package net.sf.saxon.style;

import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.instruct.Executable;
import net.sf.saxon.expr.instruct.GeneralVariable;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.LocalVariable;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.SequenceType;

/**
* Handler for xsl:variable elements in stylesheet. <br>
* The xsl:variable element has mandatory attribute name and optional attribute select
*/

public class XSLVariable extends XSLVariableDeclaration {

    private int state = 0;
            // 0 = before prepareAttributes()
            // 1 = during prepareAttributes()
            // 2 = after prepareAttributes()

    public void prepareAttributes() throws XPathException {
        if (state==2) return;
        if (state==1) {
            compileError("Circular reference to variable", "XTDE0640");
        }
        state = 1;
        //System.err.println("Prepare attributes of $" + getVariableName());
        super.prepareAttributes();
        state = 2;
    }

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction (well, it can be, anyway)
    */

    public boolean isInstruction() {
        return true;
    }

    /**
    * Get the static type of the variable. This is the declared type, unless the value
    * is statically known and constant, in which case it is the actual type of the value.
    */

    public SequenceType getRequiredType() {
        // System.err.println("Get required type of $" + getVariableName());
        final TypeHierarchy th = getConfiguration().getTypeHierarchy();
        SequenceType defaultType = (requiredType==null ? SequenceType.ANY_SEQUENCE : requiredType);
        if (assignable) {
            return defaultType;
        } else if (requiredType != null) {
            return requiredType;
        } else if (select!=null) {
            if (Literal.isEmptySequence(select)) {
                // returning Type.EMPTY gives problems with static type checking
                return defaultType;
            } else {
                try {
                    // try to infer the type from the select expression
                    return SequenceType.makeSequenceType(select.getItemType(th), select.getCardinality());
                } catch (Exception err) {
                    // this may fail because the select expression references a variable or function
                    // whose type is not yet known, because of forwards (perhaps recursive) references.
                    return defaultType;
                }
            }
        } else if (hasChildNodes()) {
            return SequenceType.makeSequenceType(NodeKindTest.DOCUMENT, StaticProperty.EXACTLY_ONE);
        } else {
            // no select attribute or content: value is an empty string
            return SequenceType.SINGLE_STRING;
        }
    }

    /**
     * Compile: used only for global variables.
     * This method ensures space is available for local variables declared within
     * this global variable
     */

    public void compileDeclaration(Executable exec, Declaration decl) throws XPathException {

        if (references.isEmpty() && !assignable) {
            redundant = true;
        }

        if (!redundant) {
            GeneralVariable inst;
            if (global) {
                inst = new GlobalVariable();
                ((GlobalVariable)inst).setExecutable(getPreparedStylesheet());
                if (select != null) {
                    select.setContainer(((GlobalVariable)inst));
                }
                initializeBinding(exec, decl, inst);
                inst.setVariableQName(getVariableQName());
                inst.setSlotNumber(getSlotNumber());
                inst.setRequiredType(getRequiredType());
                fixupBinding(inst);
                inst.setContainer(((GlobalVariable)inst));
                compiledVariable = inst;
            } else {
                throw new AssertionError("Local variable found when compiling a global variable");
            }
        }

    }

    /*@Nullable*/ public LocalVariable compileLocalVariable(Executable exec, Declaration decl) throws XPathException {

        if (references.isEmpty() && !assignable) {
            redundant = true;
        }

        if (!redundant) {
            LocalVariable inst;
            if (global) {
                throw new AssertionError("Global variable found when compiling local variable");
            } else {
                inst = new LocalVariable();
                inst.setContainer(this);
                if (select != null) {
                    select.setContainer(this);
                }
                initializeBinding(exec, decl, inst);
                inst.setVariableQName(getVariableQName());
                inst.setRequiredType(getRequiredType());
                return inst;
            }
        }

        return null;
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