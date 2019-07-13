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
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link ScalaSuspiciousFilter}.
 */
public class ScalaSuspiciousFilterTest extends FilterTestBase {

	private final ScalaSuspiciousFilter filter = new ScalaSuspiciousFilter();

	@Before
	public void setUp() {
		context.classAnnotations
				.add(ScalaFilter.SCALA_SIGNATURE_ANNOTATION);
	}

	@Test
	public void should_filter_only_methods_with_no_line_info() {
		MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()V", null, null);
		m.visitLineNumber(10, new Label());
		m.visitInsn(Opcodes.RETURN);
		context.classMethods.add(m);

		m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"bar", "()V", null, null);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_not_filter_methods_with_line_info() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"bar", "()V", null, null);
		m.visitLineNumber(10, new Label());
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_filter_methods_when_no_debug_info() {
		MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()V", null, null);
		m.visitInsn(Opcodes.RETURN);
		context.classMethods.add(m);

		m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"bar", "()V", null, null);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_filter_outer_null_checks() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "<init>",
				"(LMain;)V", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.IFNONNULL);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitInsn(Opcodes.ATHROW);

		final Range range =
				new Range(m.instructions.getFirst(), m.instructions.getLast());

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitFieldInsn(Opcodes.PUTFIELD, context.className, "$outer",
				"LMain;");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>",
				"()V", false);
		m.visitInsn(Opcodes.RETURN);

		filter.filterInternal(m, context, output);

		assertIgnored(range);
	}

}
