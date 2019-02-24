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
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters synthetic methods in Scala module classes.
 */
public class ScalaModuleFilter extends ScalaFilter {

	public void filterInternal(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (!isModuleClass(context)) {
			return;
		}
		new ReadResolveMatcher().ignoreMatches(methodNode, context, output);
	}

	/**
	 * Filters generated readResolve method of serializable scala modules.
	 *
	 * Consider the following Scala object:
	 * <pre>{@code
	 * object Main extends Serializable
	 * }</pre>
	 * ... which is compiled into the following byte code
	 * <pre>{@code
	 * public class Main$ implements scala.Serializable {
	 *   public static final Main$ MODULE$;
	 *     descriptor: LMain$;
	 *     flags: ACC_PUBLIC, ACC_STATIC, ACC_FINAL
	 *   private java.lang.Object readResolve();
	 *     descriptor: ()Ljava/lang/Object;
	 *     flags: ACC_PRIVATE
	 *     Code:
	 *       stack=1, locals=1, args_size=1
	 *          0: getstatic     #24  // Field MODULE$:LMain$;
	 *          3: areturn
	 * }
	 * }</pre>
	 * We'd like to filter out this generated {@code readResolve} method.
	 */
	private static class ReadResolveMatcher extends AbstractMatcher {
		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			if (!"readResolve".equals(methodNode.name)
					|| !"()Ljava/lang/Object;".equals(methodNode.desc)) {
				return;
			}

			InsnList instructions = methodNode.instructions;
			cursor = instructions.getFirst();
			skipNonOpcodes();

			if (cursor == null || cursor.getOpcode() != Opcodes.GETSTATIC) {
				return;
			}
			FieldInsnNode insn = (FieldInsnNode) cursor;
			if (insn == null || !MODULE_FIELD.equals(insn.name)
					|| !getDesc(context.getClassName()).equals(insn.desc)) {
				return;
			}

			nextIs(Opcodes.ARETURN);

			if (cursor != null) {
				output.ignore(instructions.getFirst(), instructions.getLast());
			}
		}
	}

}
