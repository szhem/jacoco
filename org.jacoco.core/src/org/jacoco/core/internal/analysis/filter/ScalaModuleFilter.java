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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters synthetic methods in Scala module classes.
 */
public class ScalaModuleFilter extends ScalaFilter {

	public void filterInternal(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (!isModuleClass(context)) {
			return;
		}
		new InitMatcher().ignoreMatches(methodNode, context, output);
		new ReadResolveMatcher().ignoreMatches(methodNode, context, output);
		new ExtensionMethodMatcher().ignoreMatches(methodNode, context, output);
	}

	/**
	 * Filters generated readResolve method of serializable scala modules.
	 *
	 * Consider the following Scala object:
	 * <pre>{@code
	 * object Main extends Serializable
	 * }</pre>
	 * ... which is compiled into the following byte code
	 * <pre>{@code
	 * public class Main$ implements scala.Serializable {
	 *   public static final Main$ MODULE$;
	 *     descriptor: LMain$;
	 *     flags: ACC_PUBLIC, ACC_STATIC, ACC_FINAL
	 *   private java.lang.Object readResolve();
	 *     descriptor: ()Ljava/lang/Object;
	 *     flags: ACC_PRIVATE
	 *     Code:
	 *       stack=1, locals=1, args_size=1
	 *          0: getstatic     #24  // Field MODULE$:LMain$;
	 *          3: areturn
	 * }
	 * }</pre>
	 * We'd like to filter out this generated {@code readResolve} method.
	 */
	private static class ReadResolveMatcher extends AbstractMatcher {
		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			if (!"readResolve".equals(methodNode.name)
					|| !"()Ljava/lang/Object;".equals(methodNode.desc)) {
				return;
			}

			InsnList instructions = methodNode.instructions;
			cursor = instructions.getFirst();
			skipNonOpcodes();

			if (cursor == null || cursor.getOpcode() != Opcodes.GETSTATIC) {
				return;
			}
			FieldInsnNode insn = (FieldInsnNode) cursor;
			if (insn == null || !MODULE_FIELD.equals(insn.name)
					|| !getDesc(context.getClassName()).equals(insn.desc)) {
				return;
			}

			nextIs(Opcodes.ARETURN);

			if (cursor != null) {
				output.ignore(instructions.getFirst(), instructions.getLast());
			}
		}
	}

	/**
	 * May be useful to filter generated but not used companion objects
	 *  of case- and value- classes.
	 *
	 * <pre>{@code
	 * object Main
	 * }</pre>
	 *
	 * <pre>{@code
	 * public Main$();
	 *   descriptor: ()V
	 *   flags: ACC_PUBLIC
	 *   Code:
	 *     stack=1, locals=1, args_size=1
	 *        0: aload_0
	 *        1: invokespecial #13 // Method java/lang/Object."<init>":()V
	 *        4: aload_0
	 *        5: putstatic     #15 // Field MODULE$:LMain$;
	 *        8: return
	 *     LocalVariableTable:
	 *       Start  Length  Slot  Name   Signature
	 *           0       9     0  this   LMain$;
	 *     LineNumberTable:
	 *       line 10: 0
	 * }</pre>
	 */
	private static class InitMatcher extends AbstractMatcher {
		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			if ("init".equals(methodNode.name)
					&& "()V".equals(methodNode.desc)) {
				final InsnList instructions = methodNode.instructions;
				output.ignore(instructions.getFirst(), instructions.getLast());
			}
		}
	}

	/**
	 * Filters Scala methods of value class companion objects.
	 *
	 * Consider the following example:
	 *
	 * <pre>{@code
	 * object Main
	 * class Main(val v:String) extends AnyVal {
	 *   def foo: String = v
	 * }
	 * }</pre>
	 * ... that has a companion object compiled into the following byte code:
	 * <pre>{@code
	 * public final class Main$ {
	 *   public static final Main$ MODULE$;
	 *   public static {};
	 *   public final java.lang.String foo$extension(java.lang.String);
	 *   public final int hashCode$extension(java.lang.String);
	 *   public final boolean equals$extension(java.lang.String, java.lang.Object);
	 *   private Main$();
	 * }
	 * }</pre>
	 * This matcher tries to exclude all the synthetically-generated extension
	 * methods.
	 */
	private static class ExtensionMethodMatcher extends AbstractMatcher {

		private static final Set<String> CASE_COMPANION_METHODS =
				new HashSet<String>(Arrays.asList(
						"productArity", "productElement", "productPrefix",
						"productIterator", "copy", "canEqual", "equals",
						"hashCode", "toString"
				));

		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			final String methodName = methodNode.name;
			final int sepPos = methodName.indexOf('$');
			if (sepPos == -1) {
				return;
			}

			final String prefix = methodName.substring(0, sepPos);
			final String suffix = methodName.substring(sepPos);

			if (!EXTENSION_METHOD_SUFFIX.equals(suffix)) {
				return;
			}

			if (CASE_COMPANION_METHODS.contains(prefix)) {
				final InsnList instructions = methodNode.instructions;
				output.ignore(instructions.getFirst(), instructions.getLast());
			}
		}
	}

}
