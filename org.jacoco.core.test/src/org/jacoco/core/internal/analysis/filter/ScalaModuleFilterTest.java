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
 * Unit tests for {@link ScalaAccessorFilter}.
 */
public class ScalaModuleFilterTest extends FilterTestBase {

	private final ScalaModuleFilter filter = new ScalaModuleFilter();

	@Before
	public void setUp() {
		context.className = "Main$";
		context.getClassAnnotations()
				.add(ScalaFilter.SCALA_SIGNATURE_ANNOTATION);
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
		context.superClassName = "scala/runtime/AbstractFunction1";

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

	@Test
	public void should_filter_simple_constructors_scala213() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"<init>", "()V", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>",
				"()V", false);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_module_lazy_factory_methods() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"Foo$1$lzycompute",
				"(Lscala/runtime/VolatileObjectRef;)LInnerClass$Foo$2$;", null,
				null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_module_factory_methods() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"Foo$1",
				"(Lscala/runtime/VolatileObjectRef;)LInnerClass$Foo$2$;", null,
				null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_inner_module_simple_constructor() {
		context.className = "Main$Foo$2$";
		context.classFields.add(new FieldNode(
				Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC,
				"$outer", "LMain;", null, new Object()
		));

		final Label lblNull = new Label();

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"<init>", "(LMain;)V", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitJumpInsn(Opcodes.IFNONNULL, lblNull);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitInsn(Opcodes.ATHROW);
		m.visitLabel(lblNull);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitFieldInsn(Opcodes.PUTFIELD, "Main$Foo$2$", "$outer", "LMain;");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>",
				"()V", false);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_inner_module_simple_constructor_with_no_fields() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"<init>", "(LMain;)V", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>",
				"()V", false);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_not_filter_inner_module_constructors_with_many_fields() {
		context.classFields.add(new FieldNode(
				Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC,
				"$outer", "LMain;", null, new Object()
		));
		context.classFields.add(new FieldNode(
				Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL,
				"foo", "Ljava/lang/String;", null, new Object()
		));

		final Label lblNull = new Label();

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"<init>", "(LMain;)V", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitJumpInsn(Opcodes.IFNONNULL, lblNull);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitInsn(Opcodes.ATHROW);
		m.visitLabel(lblNull);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitFieldInsn(Opcodes.PUTFIELD, "Main$Foo$2$", "$outer", "LMain;");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>",
				"()V", false);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitLdcInsn("foo");
		m.visitFieldInsn(Opcodes.PUTFIELD, "Main$Foo$2$", "foo",
				"Ljava/lang/String;");
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_filter_inner_module_constructors_with_many_calls() {
		context.classFields.add(new FieldNode(
				Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC,
				"$outer", "LMain;", null, new Object()
		));
		context.classFields.add(new FieldNode(
				Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL,
				"foo", "Ljava/lang/String;", null, new Object()
		));

		final Label lblNull = new Label();

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"<init>", "(LMain;)V", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitJumpInsn(Opcodes.IFNONNULL, lblNull);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitInsn(Opcodes.ATHROW);
		m.visitLabel(lblNull);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitFieldInsn(Opcodes.PUTFIELD, "Main$Foo$2$", "$outer", "LMain;");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>",
				"()V", false);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Main$Foo$2$", "foo", "()V",
			false);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_filter_clinit() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "<clinit>",
				"()V", null, null);
		m.visitInsn(Opcodes.NEW);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, context.className, "<init>",
				"()V", false);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_clinit_scala213() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
				ScalaFilter.CLINIT_NAME, "()V", null, null);
		m.visitInsn(Opcodes.NEW);
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, context.className,
				ScalaFilter.INIT_NAME, "()V", false);
		m.visitFieldInsn(Opcodes.PUTSTATIC, context.className,
				ScalaFilter.MODULE_FIELD,
				ScalaFilter.getDesc(context.className));
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

}
