/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.util.Objects;

/**
 * Verifies that checked code doesn't call a given method, matched by its name. The {@link #methodName} property
 * determines the method name to check.
 */
public class ForbiddenMethodCheck extends AbstractCheck {
    private String methodName;

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int[] getDefaultTokens() {
        return new int[] {TokenTypes.METHOD_CALL};
    }

    public int[] getAcceptableTokens() {
        return new int[0];
    }

    public int[] getRequiredTokens() {
        return new int[0];
    }

    @Override
    public void visitToken(DetailAST ast) {
        // Method calls appear in the AST as a single IDENT, or as DOT with one or two child IDENTs;
        // we're after the last child IDENT
        DetailAST dot = ast.findFirstToken(TokenTypes.DOT);
        if (dot != null) {
            checkIdents(dot);
        } else {
            checkIdents(ast);
        }
    }

    private void checkIdents(DetailAST callRoot) {
        DetailAST target = callRoot.findFirstToken(TokenTypes.IDENT);
        if (target != null) {
            DetailAST method = target.getNextSibling();
            if (method == null || method.getType() != TokenTypes.IDENT) {
                // We're matching against a single-node IDENT
                method = target;
            }
            if (Objects.equals(methodName, method.getText())) {
                log(callRoot.getLineNo(), "method " + methodName + " must not be called");
            }
        }
    }
}
