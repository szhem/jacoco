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

import java.util.Iterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
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
 * <li>lazy vals which have an accessor that forwards to
 * {@code $lzycompute}, which is the method with the corresponding code</li>
 * </ul>
 *
 * The filter is back-ported from the
 * <a href="https://github.com/sbt/sbt-jacoco/blob/master/src/main/scala/com/github/sbt/jacoco/filter/ScalaForwarderDetector.scala">
 * ScalaForwarderDetector</a> object of the
 * <a href="https://github.com/sbt/sbt-jacoco">sbt-jacoco</a> plugin.
 */
public class ScalaForwarderFilter extends ScalaFilter {

	private static final String CONSTRUCTOR_NAME = "<init>";

	private static final String LAZY_COMPUTE_SUFFIX = "$lzycompute";
	private static final String EXTENSION_SUFFIX = "$extension";
	private static final String CLASS_SUFFIX = "$class";

	public void filterInternal(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		final InsnList instructions = methodNode.instructions;
		if (instructions.size() > 100) {
			return;
		}

		boolean hasJump = false;
		for (final Iterator<AbstractInsnNode> iter = instructions.iterator();
				iter.hasNext() && !hasJump; ) {
			if (iter.next() instanceof JumpInsnNode) {
				hasJump = true;
			}
		}

		boolean hasForwarderCall = false;
		for (final Iterator<AbstractInsnNode> iter = instructions.iterator();
				iter.hasNext() && !hasForwarderCall; ) {
			final AbstractInsnNode insn = iter.next();
			if (insn instanceof MethodInsnNode) {
				hasForwarderCall = isScalaForwarder(context.getClassName(),
						methodNode.name, (MethodInsnNode) insn, hasJump);
			}
		}
		if (hasForwarderCall) {
			output.ignore(instructions.getFirst(), instructions.getLast());
		}
	}

	private boolean isScalaForwarder(final String className,
			final String methodName, final MethodInsnNode calledMethod,
			final boolean hasJump) {
		final String methodOwner = calledMethod.owner;
		final String calledMethodName = calledMethod.name;
		final int opcode = calledMethod.getOpcode();

		final boolean callingCompanionModule =
				methodOwner.equals(className + "$");
		final boolean callingImplClass = methodOwner.endsWith(CLASS_SUFFIX);
		final boolean callingImplicitClass =
				methodOwner.endsWith("$" + methodName)
						|| methodOwner.equals(methodName);
		final String extensionName = methodName + EXTENSION_SUFFIX;

		final boolean staticForwarder =
				(opcode == Opcodes.INVOKEVIRTUAL) && callingCompanionModule
						&& calledMethodName.equals(methodName);
		final boolean traitForwarder =
				(opcode == Opcodes.INVOKESTATIC) && callingImplClass
						&& calledMethodName.equals(methodName);
		final boolean extensionMethodForwarder =
				(opcode == Opcodes.INVOKEVIRTUAL) && callingCompanionModule
						&& calledMethodName.equals(extensionName);
		final boolean implicitClassFactory =
				(opcode == Opcodes.INVOKEVIRTUAL) && callingImplicitClass
						&& CONSTRUCTOR_NAME.equals(calledMethodName);
		final boolean lazyAccessor = (opcode == Opcodes.INVOKESPECIAL)
				&& calledMethodName.endsWith(LAZY_COMPUTE_SUFFIX);

		return (staticForwarder || traitForwarder || extensionMethodForwarder
					|| implicitClassFactory)
				&& !hasJump // sanity check
				|| lazyAccessor;
	}

}
