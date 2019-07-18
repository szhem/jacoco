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
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link ScalaCaseClassFilter}.
 */
public class ScalaCaseClassFilterTest extends FilterTestBase {

	private final ScalaCaseClassFilter filter = new ScalaCaseClassFilter();

	@Before
	public void setUp() {
		context.className = "Main";
		context.classAnnotations
				.add(ScalaFilter.SCALA_SIGNATURE_ANNOTATION);

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"<init>", "(Ljava/lang/String;)V", null, null);
		m.visitLineNumber(10, new Label());
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object",
				"<init>", "()V", false);
		m.visitInsn(Opcodes.RETURN);

		context.classMethods.add(m);
	}

	@Test
	public void should_filter_productArity_method() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"productArity", "()I", null, null);
		m.visitLineNumber(10, new Label());
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_productElement_method() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"productElement", "(I)Ljava/lang/Object;", null, null);
		m.visitLineNumber(10, new Label());
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_productPrefix_method() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"productPrefix", "()Ljava/lang/String;", null, null);
		m.visitLineNumber(10, new Label());
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_productIterator_method() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"productIterator", "()Lscala/collection/Iterator;", null, null);
		m.visitLineNumber(10, new Label());
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_canEqual_method() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"canEqual", "(Ljava/lang/Object;)Z", null, null);
		m.visitLineNumber(10, new Label());
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_equals_method() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"equals", "(Ljava/lang/Object;)Z", null, null);
		m.visitLineNumber(10, new Label());
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_hashCode_method() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"hashCode", "()I", null, null);
		m.visitLineNumber(10, new Label());
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_toString_method() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"toString", "()Ljava/lang/String;", null, null);
		m.visitLineNumber(10, new Label());
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_copy_method() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"copy", "(Ljava/lang/String;)LMain;", null, null);
		m.visitLineNumber(10, new Label());
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_not_filter_non_generated_methods() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foobar", "()V", null, null);
		m.visitLineNumber(15, new Label());
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_filter_manually_defined_method() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"hashCode", "()I", null, null);
		// candidate to be filtered is on the different line, comparing to
		// the constructor's one, so it should not be filtered
		m.visitLineNumber(15, new Label());
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_filter_companion_apply_method() {
		setUpModuleClass();

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"apply", "(Ljava/lang/String;)LMain;", null, null);
		m.visitLineNumber(10, new Label());
		m.visitInsn(Opcodes.NEW);
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "Main", "<init>",
				"(Ljava/lang/String;)V", false);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_companion_unapply_method() {
		setUpModuleClass();

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"unapply", "(LMain;)Lscala/Option;", null, null);
		m.visitLineNumber(10, new Label());
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_companion_unapplySeq_method() {
		setUpModuleClass();

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"unapplySeq", "(LMain;)Lscala/Option;", null, null);
		m.visitLineNumber(10, new Label());
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_companion_readResolve_method() {
		setUpModuleClass();

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"readResolve", "()Ljava/lang/Object;", null, null);
		m.visitLineNumber(10, new Label());
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_companion_toString_method() {
		setUpModuleClass();

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"toString", "()Ljava/lang/String;", null, null);
		m.visitLineNumber(10, new Label());
		m.visitLdcInsn("foo");
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	private void setUpModuleClass() {
		context.className = "Main$";
		final FieldNode f = new FieldNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
				"MODULE$", "LMain$;", null, null);
		context.classFields.add(f);
	}

}
