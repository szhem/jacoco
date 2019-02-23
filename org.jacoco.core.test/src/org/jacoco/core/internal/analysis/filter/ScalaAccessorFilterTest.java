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
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link ScalaAccessorFilter}.
 */
public class ScalaAccessorFilterTest extends FilterTestBase {

	private final ScalaAccessorFilter filter = new ScalaAccessorFilter();

	@Test
	public void should_filter_scala_accessor() {
		context.getClassAnnotations()
				.add(ScalaFilter.SCALA_SIGNATURE_ANNOTATION);

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()Ljava/lang/String;", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Main", "foo", "Ljava/lang/String;");
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_scala_mutator() {
		context.getClassAnnotations()
				.add(ScalaFilter.SCALA_SIGNATURE_ANNOTATION);

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo_$eq", "(Ljava/lang/String;)V", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitFieldInsn(Opcodes.PUTFIELD, "Main", "foo", "Ljava/lang/String;");
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_not_filter_non_scala_accessor() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()Ljava/lang/String;", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Main", "foo", "Ljava/lang/String;");
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_filter_non_scala_mutator() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo_$eq", "(Ljava/lang/String;)V", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitFieldInsn(Opcodes.PUTFIELD, "Main", "foo", "Ljava/lang/String;");
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	//	class Main {
	//		private var _foo: String = _
	//		def foo = _foo
	//		def foo_=(v:String) = _foo = v
	//	}
	@Test
	public void should_not_filter_custom_scala_accessor() {
		context.getClassAnnotations()
				.add(ScalaFilter.SCALA_SIGNATURE_ANNOTATION);

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()Ljava/lang/String;", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.INVOKESPECIAL);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	//	class Main {
	//		private var _foo: String = _
	//		def foo = _foo
	//		def foo_=(v:String) = _foo = v
	//	}
	@Test
	public void should_not_filter_custom_scala_mutator() {
		context.getClassAnnotations()
				.add(ScalaFilter.SCALA_SIGNATURE_ANNOTATION);

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo_$eq", "(Ljava/lang/String;)V", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.INVOKEVIRTUAL);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}


}
