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
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Detects Scala generated methods which cannot be classified and filtered by
 * other Scala-related filters.
 */
public class ScalaSuspiciousFilter extends ScalaFilter {

	public void filterInternal(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		new NoLineInfoMethodsMatcher()
				.ignoreMatches(methodNode, context, output);
		new OuterNullCheckFilter().ignoreMatches(methodNode, context, output);
	}

	/**
	 * Filters Scala methods which don't have line number debug info in case
	 * there is any other method that contains such information.
	 *
	 * If some methods contain debug information and some - do not, then
	 * methods without debug information are treated as generated ones.
	 *
	 * An example of such a method are some of the "extension" methods of the
	 * Scala case classes. Consider the following example:
	 * <pre>{@code
	 * case class Main(foo:String) extends AnyVal
	 * }</pre>
	 * ... that has a companion compiled into the following byte code:
	 * <pre>{@code
	 * public class Main$ extends scala.runtime.AbstractFunction1<java.lang.String, java.lang.String> implements scala.Serializable
	 *   public Main$();
	 *     descriptor: ()V
	 *     flags: ACC_PUBLIC
	 *     Code:
	 *       stack=1, locals=1, args_size=1
	 *          0: aload_0
	 *          1: invokespecial #110  // Method scala/runtime/AbstractFunction1."<init>":()V
	 *          4: aload_0
	 *          5: putstatic     #48   // Field MODULE$:LMain$;
	 *          8: return
	 *       LocalVariableTable:
	 *         Start  Length  Slot  Name   Signature
	 *             0       9     0  this   LMain$;
	 *       LineNumberTable:
	 *         line 6: 0
	 *
	 *   public final java.lang.String copy$extension(java.lang.String, java.lang.String);
	 *     descriptor: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
	 *     flags: ACC_PUBLIC, ACC_FINAL
	 *     Code:
	 *       stack=1, locals=3, args_size=3
	 *          0: aload_2
	 *          1: areturn
	 *       LocalVariableTable:
	 *         Start  Length  Slot  Name   Signature
	 *             0       2     0  this   LMain$;
	 *             0       2     1 $this   Ljava/lang/String;
	 *             0       2     2   foo   Ljava/lang/String;
	 *       LineNumberTable:
	 *         line 6: 0
	 *
	 *   public final java.lang.String copy$default$1$extension(java.lang.String);
	 *     descriptor: (Ljava/lang/String;)Ljava/lang/String;
	 *     flags: ACC_PUBLIC, ACC_FINAL
	 *     Code:
	 *       stack=1, locals=2, args_size=2
	 *          0: aload_1
	 *          1: areturn
	 *       LocalVariableTable:
	 *         Start  Length  Slot  Name   Signature
	 *             0       2     0  this   LMain$;
	 *             0       2     1 $this   Ljava/lang/String;
	 * }}</pre>
	 * In that case {@code copy$default$1$extension} method does not have
	 * line numbers debug info although all the other methods do have.
	 */
	private static class NoLineInfoMethodsMatcher extends AbstractMatcher {
		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			LineNumberNode line = getLine(methodNode);
			if (line != null) {
				return;
			}

			for (MethodNode otherMethod : context.getClassMethods()) {
				line = getLine(otherMethod);
				if (line != null) {
					final InsnList instructions = methodNode.instructions;
					output.ignore(instructions.getFirst(),
							instructions.getLast());
					return;
				}
			}
		}
	}

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
	private static class OuterNullCheckFilter extends AbstractMatcher {

		private static final String OUTER_FIELD_NAME = "$outer";
		private static final int OUTER_FIELD_INDEX = 1;

		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			final InsnList instructions = methodNode.instructions;
			cursor = instructions.getFirst();

			if (!INIT_NAME.equals(methodNode.name)) {
				return;
			}

			VarInsnNode varInsnNode = forward(Opcodes.ALOAD);
			if (varInsnNode == null || varInsnNode.var != OUTER_FIELD_INDEX) {
				return;
			}

			AbstractInsnNode from = cursor;

			nextIs(Opcodes.IFNONNULL);
			nextIs(Opcodes.ACONST_NULL);
			nextIs(Opcodes.ATHROW);
			if (cursor == null) {
				return;
			}

			AbstractInsnNode to = cursor;

			final FieldInsnNode field = forward(Opcodes.PUTFIELD);
			if (field == null || !OUTER_FIELD_NAME.equals(field.name)) {
				return;
			}

			if (forward(Opcodes.RETURN) == null) {
				return;
			}

			output.ignore(from, to);
		}
	}

}
