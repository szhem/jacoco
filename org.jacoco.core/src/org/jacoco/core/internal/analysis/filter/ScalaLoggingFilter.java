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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters instruction blocks which check whether the logging is enabled.
 *
 * <pre>{@code
 * if (logger.isInfoEnabled()) {
 *   logger.info("Hello World!")
 * }
 * }</pre>
 */
public class ScalaLoggingFilter extends ScalaFilter {

	public void filterInternal(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		new LoggingEnabledMatcher().ignoreMatches(methodNode, context, output);
	}

	private static class LoggingEnabledMatcher extends AbstractMatcher {
		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			final InsnList instructions = methodNode.instructions;
			cursor = instructions.getFirst();
			while (cursor != null) {
				MethodInsnNode method =
						forward(Opcodes.INVOKEINTERFACE, Opcodes.INVOKEVIRTUAL);
				if (method == null || !isLoggingEnabled(method)) {
					next();
					continue;
				}

				next();
				if (cursor == null || cursor.getOpcode() != Opcodes.IFEQ) {
					next();
					continue;
				}

				// from time to time there is a jump at the end of the if block
				final JumpInsnNode from = (JumpInsnNode) cursor;
				AbstractInsnNode to = from.label;
				if (to.getPrevious().getOpcode() == Opcodes.GOTO) {
					to = ((JumpInsnNode) to.getPrevious()).label;
				}

				if (to != null) {
					output.ignore(from, to);
				}
				cursor = to;
			}
		}

		private static boolean isLoggingEnabled(final MethodInsnNode node) {
			if (!node.owner.endsWith("Logger")) {
				return false;
			}
			if (!node.desc.endsWith("Z")) {
				return false;
			}
			if (!node.name.startsWith("is")) {
				return false;
			}
			return node.name.endsWith("Enabled")
					|| node.name.endsWith("Loggable")
					|| node.name.endsWith("EnabledFor");
		}

	}

}
