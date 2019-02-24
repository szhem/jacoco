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
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link ScalaLazyAccessorFilter}.
 */
public class ScalaLazyAccessorFilterTest extends FilterTestBase {

	private final ScalaLazyAccessorFilter filter =
			new ScalaLazyAccessorFilter();

	@Test
	public void should_filter_scala_lazy_accessor_with_boolean_bitmap() {
		context.getClassAnnotations()
				.add(ScalaFilter.SCALA_SIGNATURE_ANNOTATION);

		final Label lblIf = new Label();
		final Label lblElse = new Label();

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()Ljava/lang/String;", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Main", "bitmap$0", "Z");
		m.visitJumpInsn(Opcodes.IFNE, lblIf);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "Main", "foo$lzycompute",
				"()Ljava/lang/String;", false);
		m.visitJumpInsn(Opcodes.GOTO, lblElse);
		m.visitLabel(lblIf);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Main", "foo", "Ljava/lang/String;");
		m.visitLabel(lblElse);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_scala_lazy_accessor_with_byte_bitmap() {
		context.getClassAnnotations()
				.add(ScalaFilter.SCALA_SIGNATURE_ANNOTATION);

		final Label lblIf = new Label();
		final Label lblElse = new Label();

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()Ljava/lang/String;", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Main", "bitmap$0", "B");
		m.visitInsn(Opcodes.ICONST_1);
		m.visitInsn(Opcodes.IAND);
		m.visitInsn(Opcodes.I2B);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitJumpInsn(Opcodes.IF_ICMPNE, lblIf);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "Main", "foo$lzycompute",
				"()Ljava/lang/String;", false);
		m.visitJumpInsn(Opcodes.GOTO, lblElse);
		m.visitLabel(lblIf);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Main", "foo", "Ljava/lang/String;");
		m.visitLabel(lblElse);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_scala_lazy_accessor_with_int_bitmap() {
		context.getClassAnnotations()
				.add(ScalaFilter.SCALA_SIGNATURE_ANNOTATION);

		final Label lblIf = new Label();
		final Label lblElse = new Label();

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()Ljava/lang/String;", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Main", "bitmap$0", "I");
		m.visitInsn(Opcodes.SIPUSH);
		m.visitInsn(Opcodes.IAND);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitJumpInsn(Opcodes.IF_ICMPNE, lblIf);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "Main", "foo$lzycompute",
				"()Ljava/lang/String;", false);
		m.visitJumpInsn(Opcodes.GOTO, lblElse);
		m.visitLabel(lblIf);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Main", "foo", "Ljava/lang/String;");
		m.visitLabel(lblElse);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_scala_lazy_accessor_with_long_bitmap() {
		context.getClassAnnotations()
				.add(ScalaFilter.SCALA_SIGNATURE_ANNOTATION);

		final Label lblIf = new Label();
		final Label lblElse = new Label();

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()Ljava/lang/String;", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Main", "bitmap$0", "J");
		m.visitLdcInsn(2L);
		m.visitInsn(Opcodes.LAND);
		m.visitInsn(Opcodes.LCONST_0);
		m.visitInsn(Opcodes.LCMP);
		m.visitJumpInsn(Opcodes.IFNE, lblIf);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "Main", "foo$lzycompute",
				"()Ljava/lang/String;", false);
		m.visitJumpInsn(Opcodes.GOTO, lblElse);
		m.visitLabel(lblIf);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Main", "foo", "Ljava/lang/String;");
		m.visitLabel(lblElse);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_scala_lazy_compute_with_boolean_bitmap() {
		context.getClassAnnotations()
				.add(ScalaFilter.SCALA_SIGNATURE_ANNOTATION);

		final Label lblReturn = new Label();
		final Range[] ranges = new Range[3];

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo$lzycompute", "()Ljava/lang/String;", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Main", "bitmap$0", "Z");
		m.visitInsn(Opcodes.IFNE);

		ranges[0] = new Range(
				m.instructions.getFirst(),
				m.instructions.getLast()
		);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitLdcInsn("foo");
		m.visitFieldInsn(Opcodes.PUTFIELD, "Main", "foo", "Ljava/lang/String;");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitFieldInsn(Opcodes.PUTFIELD, "Main", "bitmap$0", "Z");
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.MONITOREXIT);

		ranges[1] = new Range(
				m.instructions.getLast().getPrevious(),
				m.instructions.getLast()
		);

		m.visitJumpInsn(Opcodes.GOTO, lblReturn);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitInsn(Opcodes.ATHROW);
		m.visitLabel(lblReturn);

		ranges[2] = new Range(
				m.instructions.get(m.instructions.size() - 6),
				m.instructions.getLast()
		);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Main", "foo", "Ljava/lang/String;");
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored(ranges);
	}

	@Test
	public void should_filter_scala_lazy_compute_with_byte_bitmap() {
		context.getClassAnnotations()
				.add(ScalaFilter.SCALA_SIGNATURE_ANNOTATION);

		final Label lblIf = new Label();
		final Range[] ranges = new Range[4];

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo$lzycompute", "()Ljava/lang/String;", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Main", "bitmap$0", "B");
		m.visitInsn(Opcodes.ICONST_1);
		m.visitInsn(Opcodes.IAND);
		m.visitInsn(Opcodes.I2B);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitJumpInsn(Opcodes.IF_ICMPNE, lblIf);

		ranges[0] = new Range(
				m.instructions.getFirst(),
				m.instructions.getLast()
		);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitLdcInsn("foo");
		m.visitFieldInsn(Opcodes.PUTFIELD, "Main", "foo", "Ljava/lang/String;");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Main", "bitmap$0", "B");
		m.visitInsn(Opcodes.ICONST_1);
		m.visitInsn(Opcodes.IOR);
		m.visitInsn(Opcodes.I2B);
		m.visitFieldInsn(Opcodes.PUTFIELD, "Main", "bitmap$0", "B");
		m.visitLabel(lblIf);
		m.visitFieldInsn(Opcodes.GETSTATIC, "scala/runtime/BoxedUnit", "UNIT",
				"Lscala/runtime/BoxedUnit;");
		m.visitInsn(Opcodes.POP);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.MONITOREXIT);

		ranges[1] = new Range(
				m.instructions.getLast().getPrevious(),
				m.instructions.getLast()
		);
		ranges[2] = new Range(
				m.instructions.getLast(),
				m.instructions.getLast()
		);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Main", "foo", "Ljava/lang/String;");
		m.visitInsn(Opcodes.ARETURN);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitInsn(Opcodes.ATHROW);

		ranges[3] = new Range(
				m.instructions.get(m.instructions.size() - 3),
				m.instructions.getLast()
		);

		filter.filter(m, context, output);

		assertIgnored(ranges);
	}

	@Test
	public void should_filter_scala_lazy_compute_with_int_bitmap() {
		context.getClassAnnotations()
				.add(ScalaFilter.SCALA_SIGNATURE_ANNOTATION);

		final Label lblIf = new Label();
		final Range[] ranges = new Range[4];

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo$lzycompute", "()Ljava/lang/String;", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Main", "bitmap$0", "I");
		m.visitInsn(Opcodes.SIPUSH);
		m.visitInsn(Opcodes.IAND);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitJumpInsn(Opcodes.IF_ICMPNE, lblIf);

		ranges[0] = new Range(
				m.instructions.getFirst(),
				m.instructions.getLast()
		);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitLdcInsn("foo");
		m.visitFieldInsn(Opcodes.PUTFIELD, "Main", "foo", "Ljava/lang/String;");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Main", "bitmap$0", "I");
		m.visitInsn(Opcodes.SIPUSH);
		m.visitInsn(Opcodes.IOR);
		m.visitFieldInsn(Opcodes.PUTFIELD, "Main", "bitmap$0", "I");
		m.visitLabel(lblIf);
		m.visitFieldInsn(Opcodes.GETSTATIC, "scala/runtime/BoxedUnit", "UNIT",
				"Lscala/runtime/BoxedUnit;");
		m.visitInsn(Opcodes.POP);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.MONITOREXIT);

		ranges[1] = new Range(
				m.instructions.getLast().getPrevious(),
				m.instructions.getLast()
		);
		ranges[2] = new Range(
				m.instructions.getLast(),
				m.instructions.getLast()
		);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Main", "foo", "Ljava/lang/String;");
		m.visitInsn(Opcodes.ARETURN);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitInsn(Opcodes.ATHROW);

		ranges[3] = new Range(
				m.instructions.get(m.instructions.size() - 3),
				m.instructions.getLast()
		);

		filter.filter(m, context, output);

		assertIgnored(ranges);
	}

	@Test
	public void should_filter_scala_lazy_compute_with_long_bitmap() {
		context.getClassAnnotations()
				.add(ScalaFilter.SCALA_SIGNATURE_ANNOTATION);

		final Label lblIf = new Label();
		final Range[] ranges = new Range[4];

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo$lzycompute", "()Ljava/lang/String;", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Main", "bitmap$0", "J");
		m.visitLdcInsn(4L);
		m.visitInsn(Opcodes.LAND);
		m.visitInsn(Opcodes.LCONST_0);
		m.visitInsn(Opcodes.LCMP);
		m.visitJumpInsn(Opcodes.IFNE, lblIf);

		ranges[0] = new Range(
				m.instructions.getFirst(),
				m.instructions.getLast()
		);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitLdcInsn("foo");
		m.visitFieldInsn(Opcodes.PUTFIELD, "Main", "foo", "Ljava/lang/String;");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Main", "bitmap$0", "J");
		m.visitLdcInsn(4L);
		m.visitInsn(Opcodes.LOR);
		m.visitFieldInsn(Opcodes.PUTFIELD, "Main", "bitmap$0", "J");
		m.visitLabel(lblIf);
		m.visitFieldInsn(Opcodes.GETSTATIC, "scala/runtime/BoxedUnit", "UNIT",
				"Lscala/runtime/BoxedUnit;");
		m.visitInsn(Opcodes.POP);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.MONITOREXIT);

		ranges[1] = new Range(
				m.instructions.getLast().getPrevious(),
				m.instructions.getLast()
		);
		ranges[2] = new Range(
				m.instructions.getLast(),
				m.instructions.getLast()
		);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Main", "foo", "Ljava/lang/String;");
		m.visitInsn(Opcodes.ARETURN);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitInsn(Opcodes.ATHROW);

		ranges[3] = new Range(
				m.instructions.get(m.instructions.size() - 3),
				m.instructions.getLast()
		);

		filter.filter(m, context, output);

		assertIgnored(ranges);
	}


}
