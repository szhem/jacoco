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
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

/**
 * Detects generated accessor and mutator methods for Scala values and
 * variables.
 */
public class ScalaAccessorFilter extends ScalaFilter {

	private static final String MUTATOR_SUFFIX = "_$eq";

	public void filterInternal(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (new AccessorMatcher().match(methodNode)
				|| new MutatorMatcher().match(methodNode)) {
			final InsnList instructions = methodNode.instructions;
			output.ignore(instructions.getFirst(), instructions.getLast());
		}
	}

	/**
	 * Filters accessor methods generated for Scala values and variables.
	 * <p>
	 * Consider the following example:
	 * <pre>{@code
	 * class Main {
	 *   val foo = "foo"
	 * }
	 * }</pre>
	 * ... which is compiled into the following byte code:
	 * <pre>{@code
	 * public class Main {
	 *   private final java.lang.String foo;
	 *     descriptor: Ljava/lang/String;
	 *     flags: (0x0012) ACC_PRIVATE, ACC_FINAL
	 *   public java.lang.String foo();
	 *     descriptor: ()Ljava/lang/String;
	 *     flags: (0x0001) ACC_PUBLIC
	 *     Code:
	 *       stack=1, locals=1, args_size=1
	 *          0: aload_0
	 *          1: getfield      #15 // Field foo:Ljava/lang/String;
	 *          4: areturn
	 *       LocalVariableTable:
	 *         Start  Length  Slot  Name   Signature
	 *             0       5     0  this   LMain;
	 *       LineNumberTable:
	 *         line 2: 0
	 * }</pre>
	 * So the accessor method should be excluded if
	 * - there is a field with the same name
	 * - method has no arguments
	 * - method's return type is equal to the corresponding field's type
	 * - method does nothing as just returning the corresponding field
	 * </p>
	 */
	private static class AccessorMatcher extends AbstractMatcher {
		public boolean match(final MethodNode methodNode) {
			firstIsALoad0(methodNode);

			nextIs(Opcodes.GETFIELD);
			final FieldInsnNode field = (FieldInsnNode) cursor;
			if (cursor == null
					|| !methodNode.name.equals(field.name)
					|| !methodNode.desc.equals("()" + field.desc)) {
				return false;
			}

			cursor = AbstractMatcher.forward(cursor, RETURN_OPCODES);
			return cursor != null;
		}
	}

	/**
	 * Filters mutator methods generated for Scala variables.
	 * <p>
	 * Consider the following example:
	 * <pre>{@code
	 * class Main {
	 *   var foo = "foo"
	 * }
	 * }</pre>
	 * ... which is compiled into the following byte code:
	 * <pre>{@code
	 * public class Main {
	 *   private final java.lang.String foo;
	 *     descriptor: Ljava/lang/String;
	 *     flags: (0x0012) ACC_PRIVATE, ACC_FINAL
	 *   public void foo_$eq(java.lang.String);
	 *     descriptor: (Ljava/lang/String;)V
	 *     flags: (0x0001) ACC_PUBLIC
	 *     Code:
	 *       stack=2, locals=2, args_size=2
	 *          0: aload_0
	 *          1: aload_1
	 *          2: putfield      #19  // Field foo:Ljava/lang/String;
	 *          5: return
	 *       LocalVariableTable:
	 *         Start  Length  Slot  Name   Signature
	 *             0       6     0  this   LMain;
	 *             0       6     1   x$1   Ljava/lang/String;
	 *       LineNumberTable:
	 *         line 2: 0
	 *   public java.lang.String foo();
	 *     descriptor: ()Ljava/lang/String;
	 *     flags: (0x0001) ACC_PUBLIC
	 *     Code:
	 *       stack=1, locals=1, args_size=1
	 *          0: aload_0
	 *          1: getfield      #15 // Field foo:Ljava/lang/String;
	 *          4: areturn
	 *       LocalVariableTable:
	 *         Start  Length  Slot  Name   Signature
	 *             0       5     0  this   LMain;
	 *       LineNumberTable:
	 *         line 2: 0
	 * }</pre>
	 * So the mutator method should be excluded if
	 * - method must ends with '_$eq'
	 * - there is a field with the same name
	 * - method has just a single argument of the same type as
	 *   the corresponding field does
	 * - method's return type void
	 * - method does nothing as just setting the corresponding field
	 * </p>
	 */
	private static class MutatorMatcher extends AbstractMatcher {
		public boolean match(final MethodNode methodNode) {
			if (!methodNode.name.endsWith(MUTATOR_SUFFIX)) {
				return false;
			}

			firstIsALoad0(methodNode);
			nextIs(Opcodes.ALOAD);

			nextIs(Opcodes.PUTFIELD);
			final FieldInsnNode field = (FieldInsnNode) cursor;
			if (cursor == null
					|| !methodNode.name.equals(field.name + MUTATOR_SUFFIX)
					|| !methodNode.desc.equals("(" + field.desc + ")V")) {
				return false;
			}

			nextIs(Opcodes.RETURN);
			return cursor != null;
		}
	}

}
