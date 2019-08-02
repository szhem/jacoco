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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link ScalaFilter}.
 */
public class ScalaFilterTest extends FilterTestBase {

	private final ScalaFilter filter = new ScalaFilter() {
		Collection<? extends ScalaMatcher> getMatchers() {
			return Collections.singleton(new ScalaMatcher() {
				@Override
				void ignoreMatches(final MethodNode methodNode,
						final IFilterContext context,
						final IFilterOutput output) {
					final InsnList instructions = methodNode.instructions;
					output.ignore(instructions.getFirst(), instructions.getLast());
				}
			});
		}
	};

	@Test
	public void should_accept_scala_classes_with_signature_annotation() {
		context.getClassAnnotations()
				.add(ScalaFilter.SCALA_SIGNATURE_ANNOTATION);

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);
		m.visitInsn(Opcodes.NOP);
		
		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_accept_scala_classes_with_long_signature_annotation() {
		context.getClassAnnotations()
				.add(ScalaFilter.SCALA_LONG_SIGNATURE_ANNOTATION);

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_accept_scala_classes_with_scalasig_attribute() {
		context.getClassAttributes().add(ScalaFilter.SCALA_SIG_ATTRIBUTE);

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_accept_scala_classes_with_scala_attribute() {
		context.getClassAttributes().add(ScalaFilter.SCALA_ATTRIBUTE);

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_skip_non_scala_methods() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_skip_empty_methods() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void isScalaClass_should_succeed_on_scala_signature_annotation() {
		context.getClassAnnotations()
				.add(ScalaFilter.SCALA_SIGNATURE_ANNOTATION);
		Assert.assertTrue(ScalaFilter.isScalaClass(context));
	}

	@Test
	public void isScalaClass_should_succeed_on_scala_long_signature_annotation() {
		context.getClassAnnotations()
				.add(ScalaFilter.SCALA_LONG_SIGNATURE_ANNOTATION);
		Assert.assertTrue(ScalaFilter.isScalaClass(context));
	}

	@Test
	public void isScalaClass_should_succeed_on_scalasig_attribute() {
		context.getClassAttributes().add(ScalaFilter.SCALA_SIG_ATTRIBUTE);
		Assert.assertTrue(ScalaFilter.isScalaClass(context));
	}

	@Test
	public void isScalaClass_should_succeed_on_scala_attribute() {
		context.getClassAttributes().add(ScalaFilter.SCALA_ATTRIBUTE);
		Assert.assertTrue(ScalaFilter.isScalaClass(context));
	}

	@Test
	public void isScalaClass_should_not_succeed_on_non_scala_class() {
		Assert.assertFalse(ScalaFilter.isScalaClass(context));
	}

	@Test
	public void isModuleClass_should_succeed_on_module_class() {
		final FieldNode f = new FieldNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
				"MODULE$", "LModule$;", null, null);
		context.classFields.add(f);
		context.className = "Module$";

		Assert.assertTrue(ScalaFilter.isModuleClass(context));
	}

	@Test
	public void isModuleClass_should_not_succeed_on_non_module_class() {
		context.className = "Module";
		Assert.assertFalse(ScalaFilter.isModuleClass(context));
	}

	@Test
	public void findMethod_should_find_method_by_name_and_desc() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);
		context.classMethods.add(m);

		Assert.assertSame(m, ScalaFilter.findMethod(context, "name", "()V"));
	}

	@Test
	public void findMethod_should_find_first_method_by_name() {
		final MethodNode m1 = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);
		final MethodNode m2 = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "(J)V", null, null);
		context.classMethods.add(m1);
		context.classMethods.add(m2);

		Assert.assertSame(m1, ScalaFilter.findMethod(context, "name", null));
	}

	@Test
	public void findMethod_should_find_first_method_by_desc() {
		final MethodNode m1 = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name1", "()V", null, null);
		final MethodNode m2 = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name2", "()V", null, null);
		context.classMethods.add(m1);
		context.classMethods.add(m2);

		Assert.assertSame(m1, ScalaFilter.findMethod(context, null, "()V"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void findMethod_should_return_first_method_on_null_name_and_desc() {
		ScalaFilter.findMethod(context, null, null);
	}

	@Test
	public void findMethod_should_not_find_unknown_method() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);
		context.classMethods.add(m);

		Assert.assertNull(ScalaFilter.findMethod(context, "name1", "()V"));
	}

	@Test
	public void findMethods_should_find_all_methods_by_name() {
		final MethodNode m1 = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);
		final MethodNode m2 = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "(J)V", null, null);
		context.classMethods.add(m1);
		context.classMethods.add(m2);

		final List<MethodNode> methodNodes =
				ScalaFilter.findMethods(context, "name", null);
		Assert.assertSame(m1, methodNodes.get(0));
		Assert.assertSame(m2, methodNodes.get(1));
	}

	@Test
	public void findMethods_should_find_all_methods_by_desc() {
		final MethodNode m1 = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name1", "()V", null, null);
		final MethodNode m2 = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name2", "()V", null, null);
		context.classMethods.add(m1);
		context.classMethods.add(m2);

		final List<MethodNode> methodNodes =
				ScalaFilter.findMethods(context, null, "()V");
		Assert.assertSame(m1, methodNodes.get(0));
		Assert.assertSame(m2, methodNodes.get(1));
	}

	@Test
	public void findField_should_find_field_by_name_and_desc() {
		final FieldNode f = new FieldNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "I", null, null);
		context.classFields.add(f);

		Assert.assertSame(f, ScalaFilter.findField(context, "name", "I"));
	}

	@Test
	public void findField_should_find_first_field_by_desc() {
		final FieldNode f1 = new FieldNode(InstrSupport.ASM_API_VERSION, 0,
				"name1", "I", null, null);
		final FieldNode f2 = new FieldNode(InstrSupport.ASM_API_VERSION, 0,
				"name2", "I", null, null);
		context.classFields.add(f1);
		context.classFields.add(f2);

		Assert.assertSame(f1, ScalaFilter.findField(context, null, "I"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void findField_should_return_first_field_on_null_name_and_desc() {
		ScalaFilter.findField(context, null, null);
	}

	@Test
	public void findField_should_not_find_unknown_method() {
		final FieldNode f = new FieldNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "I", null, null);
		context.classFields.add(f);

		Assert.assertNull(ScalaFilter.findField(context, "name1", "I"));
	}

	@Test
	public void findFields_should_find_all_fields_by_desc() {
		final FieldNode f1 = new FieldNode(InstrSupport.ASM_API_VERSION, 0,
				"name1", "I", null, null);
		final FieldNode f2 = new FieldNode(InstrSupport.ASM_API_VERSION, 0,
				"name2", "I", null, null);
		context.classFields.add(f1);
		context.classFields.add(f2);

		final List<FieldNode> fieldNodes =
				ScalaFilter.findFields(context, null, "I");
		Assert.assertSame(f1, fieldNodes.get(0));
		Assert.assertSame(f2, fieldNodes.get(1));
	}

	@Test
	public void isOneLiner_should_succeed_on_single_line_method() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);
		m.visitLineNumber(0, new Label());
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "owner", "name", "I");
		m.visitLineNumber(0, new Label());
		m.visitInsn(Opcodes.IRETURN);

		Assert.assertTrue(ScalaFilter.isOneLiner(m));
	}

	@Test
	public void isOneLiner_should_not_succeed_on_multi_line_method() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);
		m.visitLineNumber(0, new Label());
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "owner", "name", "I");
		m.visitLineNumber(2, new Label());
		m.visitInsn(Opcodes.IRETURN);

		Assert.assertFalse(ScalaFilter.isOneLiner(m));
	}

	@Test
	public void isOneLiner_should_not_succeed_on_no_lines() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "owner", "name", "I");
		m.visitInsn(Opcodes.IRETURN);

		Assert.assertFalse(ScalaFilter.isOneLiner(m));
	}

	@Test
	public void getLine_should_find_first_line_of_method() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);
		m.visitLineNumber(10, new Label());
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "owner", "name", "I");
		m.visitLineNumber(12, new Label());
		m.visitInsn(Opcodes.IRETURN);

		Assert.assertEquals(10, ScalaFilter.getLine(m).line);
	}

	@Test
	public void getLine_should_find_nothing_on_no_lines() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "owner", "name", "I");
		m.visitInsn(Opcodes.IRETURN);

		Assert.assertNull(ScalaFilter.getLine(m));
	}

	@Test
	public void getLine_should_find_nothing_on_null_method() {
		Assert.assertNull(ScalaFilter.getLine(null));
	}

	@Test
	public void isOnInitLine_should_succeed_when_on_init_line() {
		final MethodNode init = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				ScalaFilter.INIT_NAME, "()V", null, null);
		init.visitLineNumber(10, new Label());
		context.classMethods.add(init);

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);
		m.visitLineNumber(10, new Label());

		Assert.assertTrue(ScalaFilter.isOnInitLine(m, context));
	}

	@Test
	public void isOnInitLine_should_not_succeed_when_not_on_init_line() {
		final MethodNode init = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				ScalaFilter.INIT_NAME, "()V", null, null);
		init.visitLineNumber(10, new Label());
		context.classMethods.add(init);

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);
		m.visitLineNumber(12, new Label());

		Assert.assertFalse(ScalaFilter.isOnInitLine(m, context));
	}

	@Test
	public void isOnInitLine_should_not_succeed_on_no_method_lines() {
		final MethodNode init = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				ScalaFilter.INIT_NAME, "()V", null, null);
		init.visitLineNumber(10, new Label());
		context.classMethods.add(init);

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		Assert.assertFalse(ScalaFilter.isOnInitLine(m, context));
	}

	@Test
	public void isOnInitLine_should_not_succeed_on_no_init_lines() {
		final MethodNode init = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				ScalaFilter.INIT_NAME, "()V", null, null);
		context.classMethods.add(init);

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);
		m.visitLineNumber(10, new Label());
		m.visitInsn(Opcodes.NOP);

		Assert.assertFalse(ScalaFilter.isOnInitLine(m, context));
	}

	@Test
	public void getDesc_should_return_desc_by_class() {
		String desc = ScalaFilter.getDesc("java/lang/String");
		Assert.assertEquals("Ljava/lang/String;", desc);
	}

	@Test
	public void getMethodsByLine_should_return_methods_grouped_by_line() {
		final MethodNode m1 = new MethodNode(Opcodes.ACC_PUBLIC, "m1", "()V",
				null, null);

		Label m1Line = new Label();
		m1.visitLabel(m1Line);
		m1.visitLineNumber(0, m1Line);
		m1.visitVarInsn(Opcodes.ALOAD, 0);

		m1Line = new Label();
		m1.visitLabel(m1Line);
		m1.visitLineNumber(1, m1Line);
		m1.visitVarInsn(Opcodes.ALOAD, 1);

		m1Line = new Label();
		m1.visitLabel(m1Line);
		m1.visitLineNumber(2, m1Line);
		m1.visitVarInsn(Opcodes.ALOAD, 2);

		m1Line = new Label();
		m1.visitLabel(m1Line);
		m1.visitLineNumber(3, m1Line);
		m1.visitInsn(Opcodes.RETURN);

		final MethodNode m2 = new MethodNode(Opcodes.ACC_PUBLIC, "m2", "()V",
				null, null);

		Label m2Line = new Label();
		m2.visitLabel(m2Line);
		m2.visitLineNumber(4, m2Line);
		m2.visitVarInsn(Opcodes.ALOAD, 0);

		m2.visitLabel(m2Line);
		m2.visitLineNumber(4, m2Line);
		m2.visitInsn(Opcodes.RETURN);

		final MethodNode m3 = new MethodNode(Opcodes.ACC_PUBLIC, "m3", "()V",
				null, null);

		m3.visitLabel(m2Line);
		m3.visitLineNumber(4, m2Line);
		m3.visitVarInsn(Opcodes.ALOAD, 0);

		m3.visitLabel(m2Line);
		m3.visitLineNumber(4, m2Line);
		m3.visitInsn(Opcodes.RETURN);

		context.classMethods.add(m1);
		context.classMethods.add(m2);
		context.classMethods.add(m3);

		Map<Integer, Set<MethodNode>> methods
				= ScalaFilter.getMethodsByLine(context);

		Assert.assertTrue(methods.get(1).contains(m1));
		Assert.assertTrue(methods.get(2).contains(m1));
		Assert.assertTrue(methods.get(3).contains(m1));
		Assert.assertTrue(methods.get(4).containsAll(Arrays.asList(m2, m3)));
	}

	@Test
	public void getSameLineMethodsCount_should_return_methods_count_on_a_line() {
		final MethodNode m1 = new MethodNode(Opcodes.ACC_PUBLIC, "m1", "()V",
				null, null);

		Label m1Line = new Label();
		m1.visitLabel(m1Line);
		m1.visitLineNumber(0, m1Line);
		m1.visitVarInsn(Opcodes.ALOAD, 0);

		m1Line = new Label();
		m1.visitLabel(m1Line);
		m1.visitLineNumber(1, m1Line);
		m1.visitVarInsn(Opcodes.ALOAD, 1);

		m1Line = new Label();
		m1.visitLabel(m1Line);
		m1.visitLineNumber(2, m1Line);
		m1.visitVarInsn(Opcodes.ALOAD, 2);

		m1Line = new Label();
		m1.visitLabel(m1Line);
		m1.visitLineNumber(3, m1Line);
		m1.visitInsn(Opcodes.RETURN);

		final MethodNode m2 = new MethodNode(Opcodes.ACC_PUBLIC, "m2", "()V",
				null, null);

		Label m2Line = new Label();
		m2.visitLabel(m2Line);
		m2.visitLineNumber(4, m2Line);
		m2.visitVarInsn(Opcodes.ALOAD, 0);

		m2.visitLabel(m2Line);
		m2.visitLineNumber(4, m2Line);
		m2.visitInsn(Opcodes.RETURN);

		final MethodNode m3 = new MethodNode(Opcodes.ACC_PUBLIC, "m3", "()V",
				null, null);

		m3.visitLabel(m2Line);
		m3.visitLineNumber(4, m2Line);
		m3.visitVarInsn(Opcodes.ALOAD, 0);

		m3.visitLabel(m2Line);
		m3.visitLineNumber(4, m2Line);
		m3.visitInsn(Opcodes.RETURN);

		context.classMethods.add(m1);
		context.classMethods.add(m2);
		context.classMethods.add(m3);

		Map<MethodNode, Integer> counts = ScalaFilter
				.getSameLineMethodsCount(context);

		Assert.assertEquals(Integer.valueOf(1), counts.get(m1));
		Assert.assertEquals(Integer.valueOf(2), counts.get(m2));
		Assert.assertEquals(Integer.valueOf(2), counts.get(m3));
	}

}
