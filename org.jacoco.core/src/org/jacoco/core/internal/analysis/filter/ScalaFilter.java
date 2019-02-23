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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Base filter (to be extended by all the Scala filters) that accepts and
 * filters methods from Scala classes only.
 *
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

	static final String SCALA_SIGNATURE_ANNOTATION =
			"Lscala/reflect/ScalaSignature;";
	static final String SCALA_LONG_SIGNATURE_ANNOTATION =
			"Lscala/reflect/ScalaLongSignature;";
	static final String SCALA_SIG_ATTRIBUTE = "ScalaSig";
	static final String SCALA_ATTRIBUTE = "Scala";

	static final String INIT_NAME = "<init>";
	static final String NO_ARGS_DESC = "()V";

	static final Integer[] RETURN_OPCODES = {
			Opcodes.IRETURN, Opcodes.LRETURN, Opcodes.FRETURN,
			Opcodes.DRETURN, Opcodes.ARETURN, Opcodes.RETURN
	};

	public final void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (isScalaClass(context)) {
			filterInternal(methodNode, context, output);
		}
	}

	abstract void filterInternal(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output);

	//
	// Methods to be used by the Scala filters
	//

	static boolean isScalaClass(final IFilterContext context) {
		final Set<String> annotations = context.getClassAnnotations();
		final Set<String> attributes = context.getClassAttributes();
		return annotations.contains(SCALA_SIGNATURE_ANNOTATION)
				|| annotations.contains(SCALA_LONG_SIGNATURE_ANNOTATION)
				|| attributes.contains(SCALA_SIG_ATTRIBUTE)
				|| attributes.contains(SCALA_ATTRIBUTE);
	}

	static boolean isModuleClass(final IFilterContext context) {
		return context.getClassName().endsWith("$");
	}

	static MethodNode findMethod(final IFilterContext context,
			final String name, final String desc) {
		if (name == null && desc == null) {
			throw new IllegalArgumentException(
					"'name' and 'desc' must not be null at the same time");
		}
		for (final MethodNode methodNode: context.getClassMethods()) {
			if ((name == null || name.equals(methodNode.name))
					&& (desc == null || desc.equals(methodNode.desc))) {
				return methodNode;
			}
		}
		return null;
	}

	static boolean isOneLiner(final MethodNode methodNode) {
		final LineNumberNode first = AbstractMatcher.forward(
				methodNode.instructions.getFirst(),
				new AbstractMatcher.TypePredicate(AbstractInsnNode.LINE));
		final LineNumberNode last = AbstractMatcher.backward(
				methodNode.instructions.getLast(),
				new AbstractMatcher.TypePredicate(AbstractInsnNode.LINE));
		return first != null && last != null && first.line == last.line;
	}

	static LineNumberNode getLine(final MethodNode methodNode) {
		if (methodNode == null) {
			return null;
		}
		return AbstractMatcher.forward(
				methodNode.instructions.getFirst(),
				new AbstractMatcher.TypePredicate(AbstractInsnNode.LINE));
	}

	static boolean isOnInitLine(final MethodNode methodNode,
			final IFilterContext context) {
		final MethodNode initNode = findMethod(context, INIT_NAME, null);
		final LineNumberNode methodLine = getLine(methodNode);
		final LineNumberNode initLine = getLine(initNode);
		return methodLine != null && initLine != null
				&& methodLine.line == initLine.line;
	}

}
