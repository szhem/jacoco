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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters Scala module's static initializers.
 *
 * Filtering module's static initializers is necessary to prevent reporting of
 * uncovered code, e.g. {@code object Foo} is not accessed for
 * {@code case class Foo}.
 *
 * The filter is back-ported from the
 * <a href="https://github.com/sbt/sbt-jacoco/blob/39607b62807e5ce54925fd41b7ce23fb1956da19/src/main/scala/com/github/sbt/jacoco/filter/FilteringClassAnalyzer.scala#L101">
 * isModuleStaticInit</a> method of the
 * <a href="https://github.com/sbt/sbt-jacoco">sbt-jacoco</a> plugin.
 */
public class ScalaModuleStaticInitFilter extends ScalaFilter {

	private static final String STATIC_INIT_NAME = "<clinit>";
	private static final String CONSTRUCTOR_DESC = "()V";

	public void filterInternal(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (STATIC_INIT_NAME.equals(methodNode.name) && isModuleClass(context)
				&& new Matcher().match(methodNode, context)) {
			final InsnList instructions = methodNode.instructions;
			output.ignore(instructions.getFirst(), instructions.getLast());
		}
	}

	private static class Matcher extends AbstractMatcher {
		boolean match(final MethodNode methodNode,
				final IFilterContext context) {
			cursor = methodNode.instructions.getFirst();
			nextIs(Opcodes.NEW);
			nextIsInvokeSuper(context.getClassName(), CONSTRUCTOR_DESC);
			nextIs(Opcodes.RETURN);
			return cursor != null;
		}
	}

}
