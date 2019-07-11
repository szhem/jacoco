/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergey Zhemzhitsky - back-porting from sbt-jacoco into jacoco-core
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import java.util.Arrays;
import java.util.Iterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Detects forwarder methods added by Scala.
 *
 * The filtered methods include:
 * <ul>
 * <li>classes and objects that mix in traits have a forwarder to the
 * method body in the trait implementation class</li>
 * <li>classes which contain static forwarders to methods in the companion
 * object (for convenient Java interop)</li>
 * <li>methods which exist in (boxed) value classes and forward to the
 * method body in the corresponding companion object</li>
 * <li>implicit classes which creates a factory method beside the class</li>
 * </ul>
 */
public class ScalaForwarderFilter extends ScalaFilter {

	public void filterInternal(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		new StaticForwarderMatcher().ignoreMatches(methodNode, context, output);
		new TraitForwarderMatcher().ignoreMatches(methodNode, context, output);
		new ExtensionForwarderMatcher().ignoreMatches(methodNode, context, output);
		new ImplicitFactoryMatcher().ignoreMatches(methodNode, context, output);
	}

	/**
	 * <pre>{@code
	 * class Main
	 *
	 * object Main {
	 *   def foo(): Unit = {
	 *     println("foo")
	 *   }
	 * }
	 * }</pre>
	 *
	 * <pre>{@code
	 * public class Main {
	 *   public static void foo();
	 *     descriptor: ()V
	 *     flags: ACC_PUBLIC, ACC_STATIC
	 *     Code:
	 *       stack=1, locals=0, args_size=0
	 *          0: getstatic     #16   // Field Main$.MODULE$:LMain$;
	 *          3: invokevirtual #18   // Method Main$.foo:()V
	 *          6: return
	 * }
	 * }</pre>
	 */
	private static class StaticForwarderMatcher extends AbstractMatcher {
		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			final InsnList instructions = methodNode.instructions;
			cursor = instructions.getFirst();

			final String companionClass = context.getClassName() + "$";

			skipNonOpcodes();
			if (cursor == null || cursor.getOpcode() != Opcodes.GETSTATIC) {
				return;
			}
			final FieldInsnNode field = (FieldInsnNode) cursor;
			if (!MODULE_FIELD.equals(field.name)
					|| !field.owner.equals(companionClass)
					|| !field.desc.equals("L" + companionClass + ";")) {
				return;
			}

			final MethodInsnNode companionMethod =
					forward(Opcodes.INVOKEVIRTUAL);
			if (companionMethod == null
					|| !methodNode.desc.equals(companionMethod.desc)) {
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
	 * trait Foo {
	 *   def foo(): Unit = {
	 *     println("foo")
	 *   }
	 * }
	 * class Main extends Foo
	 * }</pre>
	 *
	 * <pre>{@code
	 * public class Main {
	 *   public void foo();
	 *     descriptor: ()V
	 *     flags: ACC_PUBLIC
	 *     Code:
	 *       stack=1, locals=1, args_size=1
	 *          0: aload_0
	 *          1: invokestatic  #17 // Method Foo$class.foo:(LFoo;)V
	 *          4: return
	 *       LocalVariableTable:
	 *         Start  Length  Slot  Name   Signature
	 *             0       5     0  this   LMain;
	 *       LineNumberTable:
	 *         line 7: 0
	 * }
	 * }</pre>
	 */
	private static class TraitForwarderMatcher extends AbstractMatcher {
		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			firstIsALoad0(methodNode);
			if (cursor == null) {
				return;
			}

			final MethodInsnNode traitMethod = forward(Opcodes.INVOKESTATIC);
			if (traitMethod == null || !isForwarder(methodNode, traitMethod)) {
				return;
			}

			skipNonOpcodes();
			next();
			if (cursor == null) {
				return;
			}
			if (Arrays.binarySearch(RETURN_OPCODES, cursor.getOpcode()) >= 0) {
				final InsnList instructions = methodNode.instructions;
				output.ignore(instructions.getFirst(), instructions.getLast());
			}
		}

		private boolean isForwarder(final MethodNode methodNode,
				final MethodInsnNode traitMethod) {
			return (methodNode.name.equals(traitMethod.name)
							&& traitMethod.owner.endsWith("$class")) // scala 2.10,2.11
					|| traitMethod.name.equals(methodNode.name + "$"); // scala 2.12
		}
	}

	/**
	 * <pre>{@code
	 * object Main {
	 *   def foo(v:String):String = v
	 * }
	 * class Main(val s:String) extends AnyVal
	 *}</pre>
	 *
	 * <pre>{@code
	 * public final class Main {
	 *   ...
	 *   public static java.lang.String foo(java.lang.String);
	 *     descriptor: (Ljava/lang/String;)Ljava/lang/String;
	 *     flags: ACC_PUBLIC, ACC_STATIC
	 *     Code:
	 *       stack=2, locals=1, args_size=1
	 *          0: getstatic     #16 // Field Main$.MODULE$:LMain$;
	 *          3: aload_0
	 *          4: invokevirtual #26 // Method Main$.foo:(Ljava/lang/String;)Ljava/lang/String;
	 *          7: areturn
	 *   ...
	 * }
	 * }</pre>
	 * This matcher attempts to filter "foo" method from the value class that
	 * is created due to its existence in the corresponding companion object.
	 */
	private static class ExtensionForwarderMatcher extends AbstractMatcher {
		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			final InsnList instructions = methodNode.instructions;
			cursor = instructions.getFirst();

			final FieldInsnNode field = forward(Opcodes.GETSTATIC);
			if (field == null || !MODULE_FIELD.equals(field.name)
					|| !field.desc.equals(getDesc(context.getClassName()))) {
				return;
			}

			final String companionClass = context.getClassName() + "$";
			final MethodInsnNode companionMethod =
					forward(Opcodes.INVOKEVIRTUAL);
			if (companionMethod == null
					|| !methodNode.name.equals(companionMethod.name)
					|| !methodNode.desc.equals(companionMethod.desc)
					|| !companionMethod.owner.equals(companionClass)) {
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
	 * class Bar {
	 *   def bar: String = "bar"
	 * }
	 * object Main {
	 *   implicit class Foo(v:Bar) {
	 *     def foo: String = v.bar
	 *   }
	 * }
	 * }</pre>
	 *
	 * <pre>{@code
	 * public final class Main$ {
	 *   ...
	 *   public Main$Foo Foo(Bar);
	 *     descriptor: (LBar;)LMain$Foo;
	 *     flags: ACC_PUBLIC
	 *     Code:
	 *       stack=3, locals=2, args_size=2
	 *          0: new           #16  // class Main$Foo
	 *          3: dup
	 *          4: aload_1
	 *          5: invokespecial #19  // Method Main$Foo."<init>":(LBar;)V
	 *          8: areturn
	 *       LocalVariableTable:
	 *         Start  Length  Slot  Name   Signature
	 *             0       9     0  this   LMain$;
	 *             0       9     1     v   LBar;
	 *       LineNumberTable:
	 *         line 5: 0
	 *   ...
	 * }
	 * }</pre>
	 */
	private static class ImplicitFactoryMatcher extends AbstractMatcher {
		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			final InsnList instructions = methodNode.instructions;
			cursor = instructions.getFirst();

			final MethodInsnNode ctor = forward(Opcodes.INVOKESPECIAL);
			if (ctor == null || !"<init>".equals(ctor.name)) {
				return;
			}

			final String methodDesc =
					ctor.desc.substring(0, ctor.desc.length() - "V".length())
							+ getDesc(ctor.owner);
			if (!methodNode.desc.equals(methodDesc)
					|| !methodNode.desc.endsWith(methodNode.name + ";")) {
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

}
