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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class ScalaValueClassFilter extends ScalaFilter {

	public void filterInternal(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		new ConstructorMatcher().ignoreMatches(methodNode, context, output);
		new ExtensionDelegateMatcher()
				.ignoreMatches(methodNode, context, output);
	}

	/**
	 * <pre>{@code
	 * class Main(val v: String) extends AnyVal {
	 *   def foo(bar: String): String = v + bar
	 * }
	 *}</pre>
	 *
	 * <pre>{@code
	 * public final class Main {
	 *   ...
	 *   public java.lang.String foo(java.lang.String);
	 *     descriptor: (Ljava/lang/String;)Ljava/lang/String;
	 *     flags: ACC_PUBLIC
	 *     Code:
	 *       stack=3, locals=2, args_size=2
	 *          0: getstatic     #16  // Field Main$.MODULE$:LMain$;
	 *          3: aload_0
	 *          4: invokevirtual #37  // Method v:()Ljava/lang/String;
	 *          7: aload_1
	 *          8: invokevirtual #26  // Method Main$.foo$extension:(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
	 *         11: areturn
	 *       LocalVariableTable:
	 *         Start  Length  Slot  Name   Signature
	 *             0      12     0  this   LMain;
	 *             0      12     1   bar   Ljava/lang/String;
	 *       LineNumberTable:
	 *         line 31: 0
	 *
	 *   public static java.lang.String foo$extension(java.lang.String, java.lang.String);
	 *     descriptor: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
	 *     flags: ACC_PUBLIC, ACC_STATIC
	 *     Code:
	 *       stack=3, locals=2, args_size=2
	 *          0: getstatic     #16  // Field Main$.MODULE$:LMain$;
	 *          3: aload_0
	 *          4: aload_1
	 *          5: invokevirtual #26  // Method Main$.foo$extension:(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
	 *          8: areturn
	 *   ...
	 * }
	 * }</pre>
	 *
	 * This matcher attempts to filter "foo" as well as "foo$extension" methods
	 * which delegate to the corresponding "foo$extension" method in the
	 * generated companion object.
	 * Such methods in value classes are usually not invoked at all.
	 */
	private static class ExtensionDelegateMatcher extends AbstractMatcher {

		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			final InsnList instructions = methodNode.instructions;
			cursor = instructions.getFirst();

			final String companionClass = context.getClassName() + "$";

			final FieldInsnNode field = forward(Opcodes.GETSTATIC);
			if (field == null || !MODULE_FIELD.equals(field.name)
					|| !field.desc.equals(getDesc(companionClass))) {
				return;
			}

			final MethodInsnNode companionMethod = forward(new Predicate() {
				public boolean matches(AbstractInsnNode node) {
					if (node.getOpcode() != Opcodes.INVOKEVIRTUAL) {
						return false;
					}

					final MethodInsnNode methodCalled = (MethodInsnNode) node;
					if (!methodCalled.owner.equals(companionClass)) {
						return false;
					}

					final String methodName = methodNode.name;
					final String calledName = methodCalled.name;

					final boolean extensionMethod =
							methodName.endsWith(EXTENSION_METHOD_SUFFIX)
							&& methodName.equals(calledName);
					final boolean simpleMethod = calledName
							.equals(methodName + EXTENSION_METHOD_SUFFIX);

					return extensionMethod || simpleMethod;
				}
			});
			if (companionMethod == null) {
				return;
			}

			skipNonOpcodes();
			next();
			if (cursor == null) {
				return;
			}
			if (Arrays.binarySearch(RETURN_OPCODES, cursor.getOpcode()) >= 0) {
				output.ignore(instructions.getFirst(), instructions.getLast());
			}
		}
	}

	/**
	 * <pre>{@code
	 * class Main(val v: String) extends AnyVal {
	 *   def foo(bar: String): String = v + bar
	 * }
	 *}</pre>
	 *
	 * <pre>{@code
	 * public final class Main {
	 *   ...
	 *   private final java.lang.String v;
	 *     descriptor: Ljava/lang/String;
	 *     flags: ACC_PRIVATE, ACC_FINAL
	 *
	 *   public Main(java.lang.String);
	 *     descriptor: (Ljava/lang/String;)V
	 *     flags: ACC_PUBLIC
	 *     Code:
	 *       stack=2, locals=2, args_size=2
	 *          0: aload_0
	 *          1: aload_1
	 *          2: putfield      #31  // Field v:Ljava/lang/String;
	 *          5: aload_0
	 *          6: invokespecial #49  // Method java/lang/Object."<init>":()V
	 *          9: return
	 *       LocalVariableTable:
	 *         Start  Length  Slot  Name   Signature
	 *             0      10     0  this   LMain;
	 *             0      10     1     v   Ljava/lang/String;
	 *       LineNumberTable:
	 *         line 30: 0
	 *   ...
	 * }
	 * }</pre>
	 *
	 * This matcher attempts to filter constructors of value classes which may
	 * never be called.
	 */
	public static class ConstructorMatcher extends AbstractMatcher {
		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			if (!INIT_NAME.equals(methodNode.name)) {
				return;
			}

			boolean hasExtensionMethods = false;
			for (MethodNode method : context.getClassMethods()) {
				if (method.name.endsWith(EXTENSION_METHOD_SUFFIX)) {
					hasExtensionMethods = true;
					break;
				}
			}
			if (!hasExtensionMethods) {
				return;
			}

			firstIsALoad0(methodNode);
			if (cursor == null) {
				return;
			}

			if (forward(Opcodes.PUTFIELD) == null) {
				return;
			}
			if (forward(Opcodes.RETURN) != null) {
				final InsnList instructions = methodNode.instructions;
				output.ignore(instructions.getFirst(), instructions.getLast());
			}
		}
	}

}
