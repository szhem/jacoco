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
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Filters redundant checks of outer classes for null pointer in case of
 * inner classes' constructors.
 * <p>
 * Consider the {@code wrapper} variable initialized with the subclass of
 * {@code Iterator} in the snippet below:
 * </p>
 * <pre>{@code
 * class Main {
 *   val delegate = Iterator(1,2,3)
 *   val wrapper = new Iterator[Int] {
 *     def hasNext: Boolean = delegate.hasNext
 *     def next(): Int = delegate.next()
 *   }
 * }
 * }</pre>
 * ... iterator's constructor is translated into the following
 * byte code:
 * <pre>{@code
 * public Main$$anon$1(Main);
 *   descriptor: (LMain;)V
 *   flags: ACC_PUBLIC
 *   Code:
 *     stack=2, locals=2, args_size=2
 *        0: aload_1
 *        1: ifnonnull     12
 *        4: new           #485 // class NullPointerException
 *        7: dup
 *        8: invokespecial #486 // java/lang/NullPointerException."<init>":()V
 *        11: athrow
 *        ...
 *        33: return
 *     LocalVariableTable:
 *       Start  Length  Slot  Name   Signature
 *           0      34     0  this   LMain$$anon$1;
 *           0      34     1 $outer   LMain;
 *     LineNumberTable:
 *       line 4: 0
 *     StackMapTable: number_of_entries = 1
 *       frame_type = 12
 * }</pre>
 * ... which contains unnecessary checks of {@code $outer} field (that
 * references an instance of the outer {@code Main} class) for null pointer.
 */
public class ScalaOuterNullCheckFilter extends ScalaFilter {

	private static final String CONSTRUCTOR_NAME = "<init>";
	private static final String OUTER_FIELD_NAME = "$outer";
	private static final int OUTER_FIELD_INDEX = 1;

	public void filterInternal(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (!CONSTRUCTOR_NAME.equals(methodNode.name)) {
			return;
		}

		final Matcher matcher = new Matcher();
		for (AbstractInsnNode i = methodNode.instructions.getFirst();
				i != null; i = i.getNext()) {
			matcher.match(methodNode, i, output);
		}
	}

	private static class Matcher extends AbstractMatcher {
		private void match(MethodNode methodNode, final AbstractInsnNode start,
				final IFilterOutput output) {
			cursor = start;

			nextIs(Opcodes.ALOAD);
			if (cursor == null
					|| ((VarInsnNode) cursor).var != OUTER_FIELD_INDEX) {
				return;
			}

			String varName = null;
			for (final LocalVariableNode varNode: methodNode.localVariables) {
				if (varNode.index == OUTER_FIELD_INDEX) {
					varName = varNode.name;
					break;
				}
			}
			if (!OUTER_FIELD_NAME.equals(varName)) {
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
