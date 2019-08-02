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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
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

	static final String ANON_FUN_SUFFIX = "$anonfun$";
	static final String LAZY_SUFFIX = "$lzycompute";
	static final String EXTENSION_SUFFIX = "$extension";

	static final String OUTER_FIELD = "$outer";
	static final String MODULE_FIELD = "MODULE$";

	static final Set<Integer> RETURN_OPCODES = new HashSet<Integer>(
			Arrays.asList(
				Opcodes.IRETURN, Opcodes.LRETURN, Opcodes.FRETURN,
				Opcodes.DRETURN, Opcodes.ARETURN, Opcodes.RETURN
			)
	);

	static final Set<String> PRODUCT_CANDIDATE_METHODS =
			new HashSet<String>(Arrays.asList(
					// automatically generated methods in companion objects,
					// e.g. in the case classes
					"apply", "unapply", "unapplySeq",
					// product methods
					"productArity", "productElement", "productPrefix",
					"productIterator", "canEqual",
					// automatically generated method for case companions
					"copy",
					// automatically generated methods for objects
					"equals", "hashCode", "toString",
					// automatically generated methods for serializable objects
					"readResolve"
			));

	static abstract class ScalaMatcher extends AbstractMatcher {
		abstract void ignoreMatches(MethodNode methodNode,
				IFilterContext context, IFilterOutput output);
	}

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (methodNode.instructions.size() != 0 && isScalaClass(context)) {
			for (ScalaMatcher matcher : getMatchers()) {
				matcher.ignoreMatches(methodNode, context, output);
			}
		}
	}

	abstract Collection<? extends ScalaMatcher> getMatchers();

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

	static FieldNode findField(final IFilterContext context,
			final String name, final String desc) {
		final List<FieldNode> fieldNodes = findFields(context, name, desc);
		return fieldNodes.isEmpty() ? null : fieldNodes.get(0);
	}

	static List<FieldNode> findFields(final IFilterContext context,
			final String name, final String desc) {
		if (name == null && desc == null) {
			throw new IllegalArgumentException(
					"'name' and 'desc' must not be null at the same time");
		}
		final List<FieldNode> fieldNodes = new ArrayList<FieldNode>();
		for (final FieldNode fieldNode : context.getClassFields()) {
			if ((name == null || name.equals(fieldNode.name))
					&& (desc == null || desc.equals(fieldNode.desc))) {
				fieldNodes.add(fieldNode);
			}
		}
		return fieldNodes;
	}

	static MethodNode findMethod(final IFilterContext context,
			final String name, final String desc) {
		final List<MethodNode> methodNodes = findMethods(context, name, desc);
		return methodNodes.isEmpty() ? null : methodNodes.get(0);
	}

	static List<MethodNode> findMethods(final IFilterContext context,
			final String name, final String desc) {
		if (name == null && desc == null) {
			throw new IllegalArgumentException(
					"'name' and 'desc' must not be null at the same time");
		}
		final List<MethodNode> methodNodes = new ArrayList<MethodNode>();
		for (final MethodNode methodNode : context.getClassMethods()) {
			if ((name == null || name.equals(methodNode.name))
					&& (desc == null || desc.equals(methodNode.desc))) {
				methodNodes.add(methodNode);
			}
		}
		return methodNodes;
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
		final LineNumberNode methodLine = getLine(methodNode);
		if (methodLine == null) {
			return false;
		}
		for (MethodNode initNode : findMethods(context, INIT_NAME, null)) {
			final LineNumberNode initLine = getLine(initNode);
			if (initLine != null && methodLine.line == initLine.line) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets methods grouped by the line number they are located on.
	 */
	static Map<Integer, Set<MethodNode>> getMethodsByLine(
			final IFilterContext context) {
		final Map<Integer, Set<MethodNode>> lines
				= new HashMap<Integer, Set<MethodNode>>();
		for (final MethodNode method : context.getClassMethods()) {
			final InsnList instructions = method.instructions;
			for (final Iterator<AbstractInsnNode> iter
				 = instructions.iterator(); iter.hasNext(); ) {
				final AbstractInsnNode insn = iter.next();
				if (insn.getType() == AbstractInsnNode.LINE) {
					final LineNumberNode line = (LineNumberNode) insn;
					Set<MethodNode> methods = lines.get(line.line);
					if (methods == null) {
						methods = new HashSet<MethodNode>();
						lines.put(line.line, methods);
					}
					methods.add(method);
				}
			}
		}
		return lines;
	}

	/**
	 * Gets maximum number of methods located on the lines of another method.
	 * <p>
	 *     The values are the maximum amount of other methods which have line
	 *     numbers in common with the method represented by keys of the returned
	 *     {@code Map}.
	 * </p>
	 */
	static Map<MethodNode, Integer> getSameLineMethodsCount(
			final IFilterContext context) {
		final Map<MethodNode, Integer> counts
				= new HashMap<MethodNode, Integer>();

		final Map<Integer, Set<MethodNode>> methods
				= getMethodsByLine(context);
		for (Map.Entry<Integer, Set<MethodNode>> entry: methods.entrySet()) {
			for (MethodNode method : entry.getValue()) {
				final Integer oldNumMethods = counts.get(method);
				final int newNumMethods = entry.getValue().size();
				if (oldNumMethods == null || oldNumMethods < newNumMethods) {
					counts.put(method, newNumMethods);
				}
			}
		}

		return counts;
	}

	static String getDesc(final String className) {
		return 'L' + className + ';';
	}

}
