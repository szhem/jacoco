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
 * Filters synthetic methods in Scala case classes.
 */
public class ScalaCaseClassFilter extends ScalaFilter {

	public void filterInternal(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		new InstanceMethodsMatcher().ignoreMatches(methodNode, context, output);
		new CompanionMethodsMatcher().ignoreMatches(methodNode, context, output);
		new AnyValCompanionMethodsMatcher().ignoreMatches(methodNode, context, output);
	}

	/**
	 * Filters Scala case class generated instance methods.
	 *
	 * Consider the following case class:
	 *
	 * <pre>{@code
	 * case class Main(foo: String, bar: String)
	 * }</pre>
	 * ... which is compiled into the following byte code:
	 * <pre>{@code
	 * public class Main implements scala.Product,scala.Serializable {
	 *   private final java.lang.String foo;
	 *   private final java.lang.String bar;
	 *   public java.lang.String foo();
	 *   public java.lang.String bar();
	 *   public Main copy(java.lang.String, java.lang.String);
	 *   public java.lang.String copy$default$1();
	 *   public java.lang.String copy$default$2();
	 *   public java.lang.String productPrefix();
	 *   public int productArity();
	 *   public java.lang.Object productElement(int);
	 *   public scala.collection.Iterator<java.lang.Object> productIterator();
	 *   public boolean canEqual(java.lang.Object);
	 *   public int hashCode();
	 *   public java.lang.String toString();
	 *   public boolean equals(java.lang.Object);
	 *   public Main(java.lang.String, java.lang.String);
	 * }
	 * }</pre>
	 * This matcher tries to exclude all the synthetically-generated methods.
	 * To work properly debug (line) information should not be excluded from the
	 * generated class files, e.g. don't expect this matcher to work when
	 * using {@code scalac -g:none} while compiling classes.
	 */
	private static class InstanceMethodsMatcher extends AbstractMatcher {

		private static final Set<String> CASE_INSTANCE_METHODS =
				new HashSet<String>(Arrays.asList(
						"productArity", "productElement", "productPrefix",
						"productIterator", "copy", "canEqual", "equals",
						"hashCode", "toString"
				));
		private static final String CASE_COPY_DEFAULT_PREFIX = "copy$default$";

		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			// generated methods are expected to be on the constructor's line
			if (isModuleClass(context) || !isOnInitLine(methodNode, context)) {
				return;
			}

			if (CASE_INSTANCE_METHODS.contains(methodNode.name)
					|| methodNode.name.startsWith(CASE_COPY_DEFAULT_PREFIX)) {
				final InsnList instructions = methodNode.instructions;
				output.ignore(instructions.getFirst(), instructions.getLast());
			}
		}
	}

	/**
	 * Filters Scala case class companion's generated instance methods.
	 *
	 * Consider the following case class:
	 *
	 * <pre>{@code
	 * case class Main(foo: String, bar: String)
	 * }</pre>
	 * ... that has a companion compiled into the following byte code:
	 * <pre>{@code
	 * public class Main$ extends scala.runtime.AbstractFunction2<java.lang.String, java.lang.String, Main> implements scala.Serializable {
	 *   public static final Main$ MODULE$;
	 *   public static {};
	 *   public Main$();
	 *   public Main apply(java.lang.String, java.lang.String);
	 *   public java.lang.Object apply(java.lang.Object, java.lang.Object);
	 *   public scala.Option<scala.Tuple2<java.lang.String, java.lang.String>> unapply(Main);
	 *   public final java.lang.String toString();
	 *   private java.lang.Object readResolve();
	 * }
	 * }</pre>
	 * This matcher tries to exclude all the synthetically-generated methods.
	 * To work properly debug (line) information should not be excluded from the
	 * generated class files, e.g. don't expect this matcher to work when
	 * using {@code scalac -g:none} while compiling classes.
	 */
	private static class CompanionMethodsMatcher extends AbstractMatcher {

		private static final Set<String> COMPANION_INSTANCE_METHODS =
				new HashSet<String>(Arrays.asList(
						"apply", "unapply", "unapplySeq", "toString",
						// although readResolve is filtered by ScalaModuleFilter
						// too leave readResolve here untouched just for
						// completeness of this filter;
						// anyway the generated methods are ignored only in case
						// they are on the same line as the constructor is.
						"readResolve"
				));

		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			// generated methods are expected to be on the constructor's line
			if (!isModuleClass(context) || !isOnInitLine(methodNode, context)) {
				return;
			}

			if (COMPANION_INSTANCE_METHODS.contains(methodNode.name)) {
				final InsnList instructions = methodNode.instructions;
				output.ignore(instructions.getFirst(), instructions.getLast());
			}
		}
	}

	/**
	 * Filters Scala generated instance methods of scala case class companion
	 * object that extends {@code AnyVal}.
	 *
	 * Consider the following case class:
	 *
	 * <pre>{@code
	 * case class Main(foo: String) extends AnyVal
	 * }</pre>
	 * ... that has a companion compiled into the following byte code:
	 * <pre>{@code
	 * public class Main$ extends scala.runtime.AbstractFunction1<java.lang.String, java.lang.String> implements scala.Serializable {
	 *   public static final Main$ MODULE$;
	 *   public static {};
	 *   public Main$();
	 *   public java.lang.String apply(java.lang.String);
	 *   public java.lang.Object apply(java.lang.Object);
	 *   public scala.Option<java.lang.String> unapply(java.lang.String);
	 *   public final java.lang.String copy$extension(java.lang.String, java.lang.String);
	 *   public final java.lang.String copy$default$1$extension(java.lang.String);
	 *   public final java.lang.String productPrefix$extension(java.lang.String);
	 *   public final int productArity$extension(java.lang.String);
	 *   public final java.lang.Object productElement$extension(java.lang.String, int);
	 *   public final scala.collection.Iterator<java.lang.Object> productIterator$extension(java.lang.String);
	 *   public final boolean canEqual$extension(java.lang.String, java.lang.Object);
	 *   public final int hashCode$extension(java.lang.String);
	 *   public final boolean equals$extension(java.lang.String, java.lang.Object);
	 *   public final java.lang.String toString$extension(java.lang.String);
	 *   public final java.lang.String toString();
	 *   private java.lang.Object readResolve();
	 * }
	 * }</pre>
	 * This matcher tries to exclude all the synthetically-generated methods.
	 * To work properly debug (line) information should not be excluded from the
	 * generated class files, e.g. don't expect this matcher to work when
	 * using {@code scalac -g:none} while compiling classes.
	 */
	private static class AnyValCompanionMethodsMatcher extends AbstractMatcher {

		private static final String EXTENSION_METHOD_SUFFIX = "$extension";

		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			// generated methods are expected to be on the constructor's line
			// some extension methods do not contain line debug info, so
			// don't check if it's on the constructor's line
			// e.g. for the following class
			//
			// case class Main(foo:String) extends AnyVal
			//
			// for some methods the following byte code (without) line numbers
			// debug info is generated ...
			//
			// public final java.lang.String copy$default$1$extension(java.lang.String);
			//   descriptor: (Ljava/lang/String;)Ljava/lang/String;
			//   flags: ACC_PUBLIC, ACC_FINAL
			//   Code:
			//     stack=1, locals=2, args_size=2
			//        0: aload_1
			//        1: areturn
			//     LocalVariableTable:
			//       Start  Length  Slot  Name   Signature
			//           0       2     0  this   LMain$;
			//           0       2     1 $this   Ljava/lang/String;
			//
			// ... although the main constructor contains such intormation
			//
			// public Main$();
			//   descriptor: ()V
			//   flags: ACC_PUBLIC
			//   Code:
			//     stack=1, locals=1, args_size=1
			//        0: aload_0
			//        1: invokespecial #110 // Method scala/runtime/AbstractFunction1."<init>":()V
			//        4: aload_0
			//        5: putstatic     #48  // Field MODULE$:LMain$;
			//        8: return
			//     LocalVariableTable:
			//       Start  Length  Slot  Name   Signature
			//           0       9     0  this   LMain$;
			//     LineNumberTable:
			//       line 6: 0
			if (!isModuleClass(context)) {
				return;
			}

			if (methodNode.name.endsWith(EXTENSION_METHOD_SUFFIX)) {
				final InsnList instructions = methodNode.instructions;
				output.ignore(instructions.getFirst(), instructions.getLast());
			}
		}
	}

}
