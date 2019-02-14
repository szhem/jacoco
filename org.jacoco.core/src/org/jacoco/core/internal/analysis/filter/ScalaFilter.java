/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergey Zhemzhitsky - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

// todo check that class is scala class using
// ScalaSig constant pool
// ScalaSignatures annotation
// https://www.scala-lang.org/old/sites/default/files/sids/dubochet/Mon,%202010-05-31,%2015:25/Storage%20of%20pickled%20Scala%20signatures%20in%20class%20files.pdf
public class ScalaOuterNullCheckFilter implements IFilter {

    public void filter(MethodNode methodNode, IFilterContext context, IFilterOutput output) {
        final Matcher matcher = new Matcher();
        for (AbstractInsnNode i = methodNode.instructions
                .getFirst(); i != null; i = i.getNext()) {
            matcher.match(methodNode, i, output);
        }
    }

    private static class Matcher extends AbstractMatcher {
        private void match(MethodNode methodNode, final AbstractInsnNode start, IFilterOutput output) {
            // todo check at the beginning
            if (!"<init>".equals(methodNode.name)) {
                return;
            }
            cursor = start;

            nextIs(Opcodes.ALOAD);
            if (cursor == null || ((VarInsnNode) cursor).var != 1) {
                return;
            }
            String varName = null;
            for (LocalVariableNode varNode : methodNode.localVariables) {
                if (varNode.index == 1) {
                    varName = varNode.name;
                    break;
                }
            }
            if (!"$outer".equals(varName)) {
                return;
            }
            nextIs(Opcodes.IFNONNULL);
            if (cursor == null) {
                return;
            }

            for (AbstractInsnNode i = cursor; i != null; i = i.getNext()) {
                if (i.getOpcode() == Opcodes.ATHROW) {
                    output.ignore(start, i);
                    break;
                }
            }
        }
    }

}
