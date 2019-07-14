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

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link ScalaAccessorFilter}.
 */
public class ScalaModuleFilterTest extends FilterTestBase {

	private final ScalaModuleFilter filter = new ScalaModuleFilter();

	@Before
	public void setUp() {
		context.className = "Main$";
		context.getClassAnnotations()
				.add(ScalaFilter.SCALA_SIGNATURE_ANNOTATION);

		final FieldNode f = new FieldNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
				"MODULE$", "LMain$;", null, null);
		context.classFields.add(f);
	}

	@Test
	public void should_filter_readResolve() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"readResolve", "()Ljava/lang/Object;", null, null);
		m.visitFieldInsn(Opcodes.GETSTATIC, "Main$", "MODULE$", "LMain$;");
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_anyval_companion_extension_methods() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"toString$extension", "(Ljava/lang/String;)Ljava/lang/String;",
				null, null);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_simple_constructors() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"<init>", "()V", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"scala/runtime/AbstractFunction1", "<init>", "()V", false);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.PUTSTATIC, context.className, "MODULE$",
				"L" + context.className + ";");
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

}
