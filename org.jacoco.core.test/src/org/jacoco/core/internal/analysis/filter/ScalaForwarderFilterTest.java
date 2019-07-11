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
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link ScalaForwarderFilter}.
 */
public class ScalaForwarderFilterTest extends FilterTestBase {

	private final ScalaForwarderFilter filter = new ScalaForwarderFilter();

	@Before
	public void setUp() {
		context.getClassAnnotations()
				.add(ScalaFilter.SCALA_SIGNATURE_ANNOTATION);
	}

	@Test
	public void should_filter_static_forwarder() {
		context.className = "Main";

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
				"foo", "()V", null, null);
		m.visitFieldInsn(Opcodes.GETSTATIC, "Main$", "MODULE$", "LMain$;");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Main$", "foo", "()V", false);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_static_forwarder_with_args() {
		context.className = "Main";

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
				"foo", "(Ljava/lang/String;I)Ljava/lang/String;", null, null);
		m.visitFieldInsn(Opcodes.GETSTATIC, "Main$", "MODULE$", "LMain$;");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ILOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Main$", "foo",
				"(Ljava/lang/String;I)Ljava/lang/String;", false);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_trait_forwarder() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "foo", "()V", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Foo$class", "foo",
				"(LFoo;)V", false);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_trait_forwarder_with_args() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "foo",
				"(Ljava/lang/String;I)Ljava/lang/String;", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ILOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Foo$class", "foo",
				"(LFoo;Ljava/lang/String;I)V", false);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_anyval_extension_forwarder() {
		context.className = "Main";

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "foo",
				"(Ljava/lang/String;)Ljava/lang/String;", null, null);
		m.visitFieldInsn(Opcodes.GETSTATIC, "Main$", "MODULE$", "LMain$;");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Main$", "foo",
				"(Ljava/lang/String;)Ljava/lang/String;", false);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_implicit_class_factory() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "Foo", "(LBar;)LMain$Foo;", null, null);
		m.visitInsn(Opcodes.NEW);
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "Main$Foo", "<init>",
				"(LBar;)V", false);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

}
