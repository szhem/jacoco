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

import java.util.Set;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Base filter that accepts and filters methods from Scala classes only.
 * Scala classes are identified by one of the following annotations:
 * <ol>
 *     <li>{@code @scala.reflect.ScalaSignature}</li>
 *     <li>{@code @scala.reflect.ScalaLongSignature}</li>
 * </ol>
 * ... or by the following class attributes:
 * <ol>
 *     <li>{@code ScalaSig}</li>
 *     <li>{@code Scala}</li>
 * </ol>
 * More details of determining how to determine whether a class is Scala one or
 * not are available under the following links:
 * <ol>
 *     <li><a href="https://www.scala-lang.org/old/sid/10">
 *         Storage of pickled Scala signatures in class files</a></li>
 *     <li><a href="https://www.scala-lang.org/old/sites/default/files/sids/dubochet/Mon,%202010-05-31,%2015:25/Storage%20of%20pickled%20Scala%20signatures%20in%20class%20files.pdf">
 *         Storage of pickled Scala signatures in class files.pdf</a></li>
 * </ol>
 */
public abstract class ScalaFilter implements IFilter {

	private static final String SCALA_SIGNATURE_ANNOTATION =
			"Lscala/reflect/ScalaSignature;";
	private static final String SCALA_LONG_SIGNATURE_ANNOTATION =
			"Lscala/reflect/ScalaLongSignature;";
	private static final String SCALA_SIG_ATTRIBUTE = "ScalaSig";
	private static final String SCALA_ATTRIBUTE = "Scala";

	public final void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (isScalaClass(context)) {
			filterInternal(methodNode, context, output);
		}
	}

	protected abstract void filterInternal(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output);

	protected boolean isModuleClass(final IFilterContext context) {
		return context.getClassName().endsWith("$");
	}

	protected boolean isOneLiner(final MethodNode methodNode) {
		int firstLine = -1;
		int lastLine = -1;
		for (AbstractInsnNode i = methodNode.instructions.getFirst();
				i != null; i = i.getNext()) {
			if (AbstractInsnNode.LINE == i.getType()) {
				LineNumberNode lineNode = (LineNumberNode) i;
				if (firstLine == -1) {
					firstLine = lineNode.line;
				}
				lastLine = lineNode.line;
			}
		}
		return firstLine == lastLine;
	}

	private boolean isScalaClass(final IFilterContext context) {
		final Set<String> annotations = context.getClassAnnotations();
		final Set<String> attributes = context.getClassAttributes();
		return annotations.contains(SCALA_SIGNATURE_ANNOTATION)
				|| annotations.contains(SCALA_LONG_SIGNATURE_ANNOTATION)
				|| attributes.contains(SCALA_SIG_ATTRIBUTE)
				|| attributes.contains(SCALA_ATTRIBUTE);
	}

}
