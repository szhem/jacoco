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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Detects accessor methods that do nothing other than load and return a field.
 *
 * The filter is back-ported from the
 * <a href="https://github.com/sbt/sbt-jacoco/blob/master/src/main/scala/com/github/sbt/jacoco/filter/AccessorDetector.scala">
 * AccessorDetector</a> object of the
 * <a href="https://github.com/sbt/sbt-jacoco">sbt-jacoco</a> plugin.
 */
public class ScalaAccessorFilter extends ScalaFilter {

	private static final Set<Integer> RETURN_OPCODES =
			new HashSet<Integer>(Arrays.asList(
					Opcodes.IRETURN, Opcodes.LRETURN, Opcodes.FRETURN,
					Opcodes.DRETURN, Opcodes.ARETURN, Opcodes.RETURN
			));

	public void filterInternal(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		final InsnList instructions = methodNode.instructions;
		if (instructions.size() >= 10) {
			return;
		}

		final List<Integer> opcodes = new ArrayList<Integer>(instructions.size());
		for (final Iterator<AbstractInsnNode> iter =
			 instructions.iterator(); iter.hasNext(); ) {
			final AbstractInsnNode insn = iter.next();
			if (!(insn instanceof JumpInsnNode
					|| insn instanceof LineNumberNode)) {
				opcodes.add(insn.getOpcode());
			}
		}
		if (opcodes.size() == 3
				&& opcodes.get(0) == Opcodes.ALOAD
				&& opcodes.get(1) == Opcodes.GETFIELD
				&& RETURN_OPCODES.contains(opcodes.get(2))) {
			output.ignore(instructions.getFirst(), instructions.getLast());
		}
	}

}
