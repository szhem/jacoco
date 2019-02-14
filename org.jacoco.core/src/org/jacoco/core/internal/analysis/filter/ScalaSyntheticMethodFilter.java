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
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters synthetic methods of Scala case classes.
 *
 * The filter is back-ported from the
 * <a href="https://github.com/sbt/sbt-jacoco/blob/master/src/main/scala/com/github/sbt/jacoco/filter/ScalaSyntheticMethod.scala">
 * ScalaSyntheticMethod</a> object of the
 * <a href="https://github.com/sbt/sbt-jacoco">sbt-jacoco</a> plugin.
 */
public class ScalaSyntheticMethodFilter extends ScalaFilter {

	private static final String CASE_COPY_DEFAULT_PREFIX = "copy$default";
	// case class Main(v:String)
	private static final Set<String> CASE_INSTANCE_METHODS =
			new HashSet<String>(Arrays.asList(
					"canEqual", "copy", "equals", "hashCode", "productPrefix",
					"productArity", "productElement", "productIterator",
					"toString"
			));
	private static final Set<String> CASE_COMPANION_METHODS =
			new HashSet<String>(Arrays.asList(
					"apply", "unapply", "unapplySeq", "readResolve"
			));
	// case class Main(v:String) extends AnyVal
	private static final Set<String> ANY_VAL_COMPANION_METHODS =
			new HashSet<String>(Arrays.asList(
					"canEqual$extension", "copy$extension", "equals$extension",
					"hashCode$extension", "productPrefix$extension",
					"productArity$extension", "productElement$extension",
					"productIterator$extension", "toString$extension"
			));

	public void filterInternal(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (!isOneLiner(methodNode)) {
			return;
		}
		if ((isModuleClass(context) && isSyntheticObjectMethod(methodNode))
				|| isSyntheticInstanceMethod(methodNode)) {
			final InsnList instructions = methodNode.instructions;
			output.ignore(instructions.getFirst(), instructions.getLast());
		}
	}

	private boolean isSyntheticInstanceMethod(final MethodNode methodNode) {
		return isCaseInstanceMethod(methodNode);
	}

	private boolean isSyntheticObjectMethod(final MethodNode methodNode) {
		return isCaseCompanionMethod(methodNode)
				|| isAnyValCompanionMethod(methodNode);
	}

	private boolean isCaseInstanceMethod(final MethodNode methodNode) {
		return CASE_INSTANCE_METHODS.contains(methodNode.name)
				|| methodNode.name.startsWith(CASE_COPY_DEFAULT_PREFIX);
	}

	private boolean isCaseCompanionMethod(final MethodNode methodNode) {
		return CASE_COMPANION_METHODS.contains(methodNode.name);
	}

	private boolean isAnyValCompanionMethod(final MethodNode methodNode) {
		return ANY_VAL_COMPANION_METHODS.contains(methodNode.name);
	}

}
