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

public class ScalaInnerClassConstructorNullCheckFilter implements IFilter {

    public void filter(MethodNode methodNode, IFilterContext context, IFilterOutput output) {
        if (isOneLiner(methodNode) &&
                (isModuleClass(context) && isSyntheticObjectMethodName(methodNode)) ||
                isSyntheticInstanceMethodName(methodNode)) {
            output.ignore(methodNode.instructions.getFirst(),
                methodNode.instructions.getLast());
        }
    }

    private boolean isOneLiner(MethodNode methodNode) {
        int firstLine = 0;
        int lastLine = 0;
        for (AbstractInsnNode i = methodNode.instructions
                .getFirst(); i != null; i = i.getNext()) {
            if (AbstractInsnNode.LINE == i.getType()) {
                if (firstLine == 0) {
                    firstLine = ((LineNumberNode) i).line;
                }
                lastLine = ((LineNumberNode) i).line;
            }
        }
        return firstLine == lastLine;
    }

    private static class Matcher extends AbstractMatcher {
        private boolean match(final MethodNode methodNode, IFilterContext context) {
            if (!("<init>".equals(methodNode.name) && "()V".equals(methodNode.desc))) {
                return false;
            }
            cursor = methodNode.instructions.getFirst();
            nextIs(Opcodes.ALOAD);
            if (cursor == null || ((VarInsnNode) cursor).var != 1) {
                return false;
            }

            LocalVariableNode varNode = methodNode.localVariables.get(1);

            if (!"$outer".equals(varNode.name) && varNode.desc.equals(context.get)

            nextIs(Opcodes.IFNONNULL);

            return cursor != null;
        }
    }

}
