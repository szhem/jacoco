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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters synthetic methods in Scala module classes.
 */
public class ScalaModuleFilter extends ScalaFilter {

	@Override
	Collection<? extends ScalaMatcher> getMatchers() {
		return Arrays.asList(
				new ModuleLzyComputeMatcher(),
				new StaticInitMatcher(),
				new SimpleConstructorMatcher(),
				new ReadResolveMatcher(),
				new ExtensionMethodMatcher()
		);
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
	private static class ReadResolveMatcher extends ScalaMatcher {

		@Override
		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			if (!isModuleClass(context)) {
				return;
			}
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
	 * May be useful to filter generated but not used companion objects of
	 * case-classes as an 'apply' method of the companion object is not
	 * necessarily called when instantiating a case class.
	 *
	 * <pre>{@code
	 * case class Foo(foo: String)
	 * object Main {
	 *   def main(args: Array[String]): Unit = {
	 *     Foo("foo")
	 *   }
	 * }}</pre>
	 *
	 * <pre>{@code
	 * public final class Foo$ extends scala.runtime.AbstractFunction1<java.lang.String, Foo> implements scala.Serializable {
	 *   private Foo$();
	 *     descriptor: ()V
	 *     flags: ACC_PRIVATE
	 *     Code:
	 *       stack=1, locals=1, args_size=1
	 *          0: aload_0
	 *          1: invokespecial #58    // Method scala/runtime/AbstractFunction1."<init>":()V
	 *          4: aload_0
	 *          5: putstatic     #50    // Field MODULE$:LFoo$;
	 *          8: return
	 *       LocalVariableTable:
	 *         Start  Length  Slot  Name   Signature
	 *             0       9     0  this   LFoo$;
	 *       LineNumberTable:
	 *         line 1: 0
	 * }
	 *
	 * public final class Main$ {
	 *   ...
	 *   public void main(java.lang.String[]);
	 *     descriptor: ([Ljava/lang/String;)V
	 *     flags: ACC_PUBLIC
	 *     Code:
	 *       stack=3, locals=2, args_size=2
	 *          0: new           #16  // class Foo
	 *          3: dup
	 *          4: ldc           #18  // String foo
	 *          6: invokespecial #21  // Method Foo."<init>":(Ljava/lang/String;)V
	 *          9: pop
	 *         10: return
	 *       LocalVariableTable:
	 *         Start  Length  Slot  Name   Signature
	 *             0      11     0  this   LMain$;
	 *             0      11     1  args   [Ljava/lang/String;
	 *       LineNumberTable:
	 *         line 10: 0
	 * }}</pre>
	 * <p>
	 * In case of inner objects filters their constructors as well.
	 *
	 * For example, the following {@code object Bar}
	 *
	 * <pre>{@code
	 * class Main {
	 *   def main(): Unit = {
	 *     object Bar
	 *   }
	 * }
	 * }</pre>
	 *
	 * ... will be compiled into the following byte code
	 *
	 * <pre>{@code
	 * public class Main$Bar$2$ {
	 *   public Main$Bar$2$(Main);
	 * }
	 * }</pre>
	 *
	 * ... and constructors of these objects may never be called.
	 * </p>
	 */
	private static class SimpleConstructorMatcher extends ScalaMatcher {

		private static Collection<? extends Predicate> getAllowedInsns(
				final IFilterContext context) {
			return Arrays.asList(
					new Predicate() {
						public boolean matches(AbstractInsnNode node) {
							final int opcode = node.getOpcode();
							return opcode == Opcodes.ALOAD
									|| RETURN_OPCODES.contains(opcode);
						}
					},
					new Predicate() {
						public boolean matches(AbstractInsnNode node) {
							return node.getType() == AbstractInsnNode.FRAME
									|| node.getType() == AbstractInsnNode.LABEL
									|| node.getType() == AbstractInsnNode.LINE;
						}
					},
					// opcodes to check whether the $outer field is null
					new Predicate() {
						public boolean matches(AbstractInsnNode node) {
							return node.getOpcode() == Opcodes.IFNONNULL
									|| node.getOpcode() == Opcodes.ACONST_NULL
									|| node.getOpcode() == Opcodes.NEW
									|| node.getOpcode() == Opcodes.ATHROW;
						}
					},
					// scala 2.10-2.12 top-level object classes (setting MODULE$ field)
					new Predicate() {
						public boolean matches(AbstractInsnNode node) {
							if (node.getOpcode() != Opcodes.PUTSTATIC) {
								return false;
							}
							final FieldInsnNode field = (FieldInsnNode) node;
							final String desc = getDesc(context.getClassName());
							return MODULE_FIELD.equals(field.name)
									&& field.desc.equals(desc);
						}
					},
					// inner object classes which set $outer field
					new Predicate() {
						public boolean matches(AbstractInsnNode node) {
							if (node.getOpcode() != Opcodes.PUTFIELD) {
								return false;
							}
							final FieldInsnNode field = (FieldInsnNode) node;
							return OUTER_FIELD.equals(field.name)
									&& field.owner
									.equals(context.getClassName());
						}
					},
					new Predicate() { // super constructor calls
						public boolean matches(AbstractInsnNode node) {
							if (node.getOpcode() != Opcodes.INVOKESPECIAL) {
								return false;
							}
							final MethodInsnNode method = (MethodInsnNode) node;
							return context.getSuperClassName()
									.equals(method.owner);
						}
					},
					new Predicate() { // trait constructor calls
						private Set<String> owners = new HashSet<String>();
						{
							owners.addAll(context.getClassInterfaces());
							for (String i : context.getClassInterfaces()) {
								owners.add(i + "$class"); // scala 2.10-2.12
							}
						}
						public boolean matches(AbstractInsnNode node) {
							if (node.getOpcode() != Opcodes.INVOKESTATIC) {
								return false;
							}
							final MethodInsnNode method = (MethodInsnNode) node;
							return owners.contains(method.owner)
									&& TRAIT_INIT_NAME.equals(method.name);
						}
					}
			);
		}

		@Override
		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			if (!isModuleClass(context)) {
				return;
			}

			if (!INIT_NAME.equals(methodNode.name)) {
				return;
			}

			final InsnList instructions = methodNode.instructions;
			for (final Iterator<AbstractInsnNode> insns = instructions.iterator();
					insns.hasNext(); ) {
				final AbstractInsnNode insn = insns.next();
				boolean allowedInsn = false;
				for (final Predicate predicate : getAllowedInsns(context)) {
					if (predicate.matches(insn)) {
						allowedInsn = true;
						break;
					}
				}
				if (!allowedInsn) {
					return;
				}
			}

			output.ignore(instructions.getFirst(), instructions.getLast());
		}
	}

	/**
	 * Attempts to ignore static initialization blocks of scala objects which
	 * may never be called.
	 * <p>
	 * <strong>Scala 2.10-2.12</strong>
	 * <pre>{@code
	 *   public static {};
	 *     descriptor: ()V
	 *     flags: ACC_PUBLIC, ACC_STATIC
	 *     Code:
	 *       stack=1, locals=0, args_size=0
	 *          0: new           #2  // class Main$
	 *          3: invokespecial #21 // Method "<init>":()V
	 *          6: return
	 * }</pre>
	 * <strong>Scala 2.13</strong>
	 * <pre>{@code
	 *   public static {};
	 *     descriptor: ()V
	 *     flags: ACC_PUBLIC, ACC_STATIC
	 *     Code:
	 *       stack=2, locals=0, args_size=0
	 *          0: new           #2  // class Main$
	 *          3: dup
	 *          4: invokespecial #21 // Method "<init>":()V
	 *          7: putstatic     #23 // Field MODULE$:LMain$;
	 *         10: return
	 *       LineNumberTable:
	 *         line 23: 0
	 * }</pre>
	 * Note: in case of Scala 2.13 the compiler also adds line number
	 * information that is not added for Scala 2.10-2.12.
	 */
	private static class StaticInitMatcher extends ScalaMatcher {

		@Override
		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			if (!isModuleClass(context)) {
				return;
			}

			if (!ScalaFilter.CLINIT_NAME.equals(methodNode.name)
					|| !"()V".equals(methodNode.desc)) {
				return;
			}

			final InsnList instructions = methodNode.instructions;
			output.ignore(instructions.getFirst(), instructions.getLast());
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
	private static class ExtensionMethodMatcher extends ScalaMatcher {

		@Override
		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			if (!isModuleClass(context)) {
				return;
			}

			final String methodName = methodNode.name;
			final int sepPos = methodName.indexOf('$');
			if (sepPos == -1) {
				return;
			}

			final String prefix = methodName.substring(0, sepPos);
			final String suffix = methodName.substring(sepPos);

			if (!EXTENSION_SUFFIX.equals(suffix)) {
				return;
			}

			if (PRODUCT_CANDIDATE_METHODS.contains(prefix)) {
				final InsnList instructions = methodNode.instructions;
				output.ignore(instructions.getFirst(), instructions.getLast());
			}
		}
	}

	/**
	 * Filters factory methods which create an object represented by an inner
	 * class.
	 *
	 * <pre>{@code
	 * object InnerClass {
	 *   def main(args: Array[String]): Unit = {
	 *     object Foo {
	 *       val foo = "foo"
	 *     }
	 *     println(Foo.foo)
	 *   }
	 * }
	 * }</pre>
	 * <pre>{@code
	 * public final class InnerClass$ {
	 *   public static final InnerClass$ MODULE$;
	 *   public static {};
	 *   private final InnerClass$Foo$2$ Foo$1(scala.runtime.VolatileObjectRef);
	 *   private InnerClass$Foo$2$ Foo$1$lzycompute(scala.runtime.VolatileObjectRef);
	 *   public void main(java.lang.String[]);
	 *   private InnerClass$();
	 * }
	 * }</pre>
	 *
	 * In the example above
	 * {@code InnerClass$Foo$2$ Foo$1$lzycompute(scala.runtime.VolatileObjectRef)}
	 * and {@code InnerClass$Foo$2$ Foo$1(scala.runtime.VolatileObjectRef)}
	 * will be filtered out.
	 */
	private static class ModuleLzyComputeMatcher extends ScalaMatcher {

		private static final Set<String> PARAMS_DESC = new HashSet<String>(
				Arrays.asList(
						"(Lscala/runtime/LazyRef;)",
						"(Lscala/runtime/VolatileObjectRef;)"
				)
		);

		@Override
		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {

			final String desc = methodNode.desc;
			final int retTypeInd = desc.lastIndexOf(')');
			if (retTypeInd == -1) {
				return;
			}

			final String retType = desc.substring(retTypeInd + 1);
			// if this is not an object - return
			if (!retType.endsWith("$;")) {
				return;
			}

			final String paramsDesc = desc.substring(0, retTypeInd + 1);
			if (!PARAMS_DESC.contains(paramsDesc)) {
				return;
			}

			final String methodName = methodNode.name;
			final int sepInd = methodName.indexOf('$');
			if (sepInd == -1) {
				return;
			}
			final String objName = methodName.substring(0, sepInd);

			if (retType.contains(objName)) {
				InsnList instructions = methodNode.instructions;
				output.ignore(instructions.getFirst(), instructions.getLast());
			}
		}
	}

}
