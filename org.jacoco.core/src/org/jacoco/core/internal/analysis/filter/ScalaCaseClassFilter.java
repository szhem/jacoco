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
import java.util.Map;

import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters synthetic methods in Scala case classes.
 */
public class ScalaCaseClassFilter extends ScalaFilter {

	@Override
	Collection<? extends ScalaMatcher> getMatchers() {
		return Arrays.asList(
				new InstanceMethodsMatcher(),
				new CompanionMethodsMatcher()
		);
	}

	private static boolean shouldFilter(final MethodNode methodNode,
			final IFilterContext context) {
		if (INIT_NAME.equals(methodNode.name)) {
			return false;
		}
		if (!PRODUCT_CANDIDATE_METHODS.contains(methodNode.name)) {
			return false;
		}
		if (isOnInitLine(methodNode, context)) {
			return true;
		}

		final Map<MethodNode, Integer> counts = getSameLineMethodsCount(context);
		final Integer count = counts.get(methodNode);

		return count != null && count > 1;
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
	private static class InstanceMethodsMatcher extends ScalaMatcher {

		@Override
		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			// generated methods are expected to be on the constructor's line
			// or at least at the same line
			if (isModuleClass(context)) {
				return;
			}
			if (!shouldFilter(methodNode, context)) {
				return;
			}

			final InsnList instructions = methodNode.instructions;
			output.ignore(instructions.getFirst(), instructions.getLast());
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
	private static class CompanionMethodsMatcher extends ScalaMatcher {

		@Override
		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			// generated methods are expected to be on the constructor's line
			// or at least at the same line
			if (!isModuleClass(context) && !hasOuterField(context)) {
				return;
			}
			if (!shouldFilter(methodNode, context)) {
				return;
			}

			final InsnList instructions = methodNode.instructions;
			output.ignore(instructions.getFirst(), instructions.getLast());
		}

		private static boolean hasOuterField(final IFilterContext context) {
			for (FieldNode field : context.getClassFields()) {
				if (OUTER_FIELD.equals(field.name)) {
					return true;
				}
			}
			return false;
		}

	}

}
