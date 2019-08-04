/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Unit tests for {@link AbstractMatcher}.
 */
public class AbstractMatcherTest {

	private final AbstractMatcher matcher = new AbstractMatcher() {
	};

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"method_name", "()V", null, null);

	@Test
	public void skipNonOpcodes() {
		m.visitFrame(Opcodes.F_FULL, 0, null, 0, null);
		final Label label = new Label();
		m.visitLabel(label);
		m.visitLineNumber(42, label);
		m.visitInsn(Opcodes.NOP);

		// should skip all non opcodes
		matcher.cursor = m.instructions.getFirst();
		matcher.skipNonOpcodes();
		assertSame(m.instructions.getLast(), matcher.cursor);

		// should not change cursor when it points on instruction with opcode
		matcher.skipNonOpcodes();
		assertSame(m.instructions.getLast(), matcher.cursor);

		// should not do anything when cursor is null
		matcher.cursor = null;
		matcher.skipNonOpcodes();
	}

	@Test
	public void nextIs() {
		m.visitInsn(Opcodes.NOP);
		m.visitInsn(Opcodes.NOP);

		// should set cursor to null when opcode mismatch
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIs(Opcodes.ATHROW);
		assertNull(matcher.cursor);

		// should set cursor to next instruction when match
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIs(Opcodes.NOP);
		assertSame(m.instructions.getLast(), matcher.cursor);

		// should not do anything when cursor is null
		matcher.cursor = null;
		matcher.nextIs(Opcodes.NOP);
	}

	@Test
	public void nextIsPredicate() {
		final AbstractMatcher.Predicate predicate
				= new AbstractMatcher.Predicate() {
			public boolean matches(AbstractInsnNode node) {
				if (node.getOpcode() == Opcodes.ACONST_NULL) {
					return true;
				}
				if (node.getOpcode() == Opcodes.NEW) {
					final String typeDesc = ((TypeInsnNode) node).desc;
					return typeDesc.equals(Type.getInternalName(
							NullPointerException.class));
				}
				return false;
			}
		};

		m.visitInsn(Opcodes.NOP);
		m.visitTypeInsn(Opcodes.NEW,
				Type.getInternalName(NullPointerException.class));
		m.visitInsn(Opcodes.ACONST_NULL);

		matcher.cursor = m.instructions.getFirst();

		matcher.nextIs(predicate);
		assertNotNull(matcher.cursor);

		matcher.nextIs(predicate);
		assertNotNull(matcher.cursor);
	}

	@Test
	public void nextIsSwitch() {
		// should set cursor to null when opcode mismatch
		m.visitInsn(Opcodes.NOP);
		m.visitInsn(Opcodes.NOP);
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsSwitch();
		assertNull(matcher.cursor);

		// should set cursor to next instruction when match
		m.instructions.clear();
		m.visitInsn(Opcodes.NOP);
		m.visitTableSwitchInsn(0, 0, new Label());
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsSwitch();
		assertSame(m.instructions.getLast(), matcher.cursor);

		// should set cursor to next instruction when match
		m.instructions.clear();
		m.visitInsn(Opcodes.NOP);
		m.visitLookupSwitchInsn(new Label(), null, new Label[0]);
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsSwitch();
		assertSame(m.instructions.getLast(), matcher.cursor);

		// should not do anything when cursor is null
		matcher.cursor = null;
		matcher.nextIsSwitch();
	}

	@Test
	public void nextIsVar() {
		m.visitInsn(Opcodes.NOP);
		m.visitVarInsn(Opcodes.ILOAD, 42);

		// should set cursor to null when opcode mismatch
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsVar(Opcodes.ALOAD, "name");
		assertNull(matcher.cursor);

		// should set cursor to next instruction when match
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsVar(Opcodes.ILOAD, "name");
		assertSame(m.instructions.getLast(), matcher.cursor);

		// should set cursor to null when var mismatch
		matcher.cursor = m.instructions.getFirst();
		matcher.vars.put("name", new VarInsnNode(Opcodes.ILOAD, 13));
		matcher.nextIsVar(Opcodes.ILOAD, "name");
		assertNull(matcher.cursor);

		// should set cursor to next instruction when match
		matcher.cursor = m.instructions.getFirst();
		matcher.vars.put("name", new VarInsnNode(Opcodes.ILOAD, 42));
		matcher.nextIsVar(Opcodes.ILOAD, "name");
		assertSame(m.instructions.getLast(), matcher.cursor);

		// should not do anything when cursor is null
		matcher.cursor = null;
		matcher.nextIsVar(Opcodes.ILOAD, "name");
	}

	@Test
	public void nextIsInvoke() {
		m.visitInsn(Opcodes.NOP);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "owner", "name", "()V", false);

		// should set cursor to null when opcode mismatch
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsInvoke(Opcodes.INVOKESTATIC, "owner", "name", "()V");
		assertNull(matcher.cursor);

		// should set cursor to null when owner mismatch
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsInvoke(Opcodes.INVOKEVIRTUAL, "another_owner", "name",
				"()V");
		assertNull(matcher.cursor);

		// should set cursor to null when name mismatch
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsInvoke(Opcodes.INVOKEVIRTUAL, "owner", "another_name",
				"()V");
		assertNull(matcher.cursor);

		// should set cursor to null when descriptor mismatch
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsInvoke(Opcodes.INVOKEVIRTUAL, "owner", "name",
				"(Lanother_descriptor;)V");
		assertNull(matcher.cursor);

		// should set cursor to next instruction when match
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsInvoke(Opcodes.INVOKEVIRTUAL, "owner", "name", "()V");
		assertSame(m.instructions.getLast(), matcher.cursor);

		// should not do anything when cursor is null
		matcher.cursor = null;
		matcher.nextIsInvoke(Opcodes.INVOKEVIRTUAL, "owner", "name", "()V");
	}

	@Test
	public void nextIsType() {
		m.visitInsn(Opcodes.NOP);
		m.visitTypeInsn(Opcodes.NEW, "descriptor");

		// should set cursor to null when opcode mismatch
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsType(Opcodes.CHECKCAST, "descriptor");
		assertNull(matcher.cursor);

		// should set cursor to null when descriptor mismatch
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsType(Opcodes.NEW, "another_descriptor");
		assertNull(matcher.cursor);

		// should set cursor to next instruction when match
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsType(Opcodes.NEW, "descriptor");
		assertSame(m.instructions.getLast(), matcher.cursor);

		// should not do anything when cursor is null
		matcher.cursor = null;
		matcher.nextIsType(Opcodes.NEW, "descriptor");
	}

	@Test
	public void firstIsALoad0() {
		// should set cursor to null when opcode mismatch
		m.visitInsn(Opcodes.NOP);
		matcher.firstIsALoad0(m);
		assertNull(matcher.cursor);

		// should set cursor to null when var mismatch
		m.instructions.clear();
		m.visitVarInsn(Opcodes.ALOAD, 1);
		matcher.firstIsALoad0(m);
		assertNull(matcher.cursor);

		// should set cursor to first instruction when match
		m.instructions.clear();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		matcher.firstIsALoad0(m);
		assertSame(m.instructions.getLast(), matcher.cursor);
	}

	@Test
	public void backwardOpcode() {
		m.visitInsn(Opcodes.NOP);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "owner", "name1", "()J");
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitFieldInsn(Opcodes.GETFIELD, "owner", "name2", "()V");
		m.visitInsn(Opcodes.RETURN);

		// should find the first insn node with the provided opcode
		FieldInsnNode fieldNode = AbstractMatcher.backward(
				m.instructions.getLast(), Opcodes.GETFIELD);
		assertEquals("owner", fieldNode.owner);
		assertEquals("name2", fieldNode.name);
		assertEquals("()V", fieldNode.desc);

		// should find the first insn node if multiple opcodes provided
		VarInsnNode varNode = AbstractMatcher.backward(
				m.instructions.getLast(), Opcodes.ALOAD, Opcodes.NOP);
		assertEquals(1, varNode.var);

		// should return null if no insn node found
		MethodInsnNode methodNode = AbstractMatcher.backward(
				m.instructions.getLast(), Opcodes.INVOKESTATIC);
		assertNull(methodNode);

		// should return current insn node node if it's has the necessary opcode
		AbstractInsnNode insn = AbstractMatcher.backward(
				fieldNode, Opcodes.GETFIELD);
		assertSame(fieldNode, insn);
	}

	@Test
	public void forwardOpcode() {
		m.visitInsn(Opcodes.NOP);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "owner", "name1", "()J");
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitFieldInsn(Opcodes.GETFIELD, "owner", "name2", "()V");
		m.visitInsn(Opcodes.RETURN);

		// should find the first insn node with the provided opcode
		FieldInsnNode fieldNode = AbstractMatcher.forward(
				m.instructions.getFirst(), Opcodes.GETFIELD);
		assertEquals("owner", fieldNode.owner);
		assertEquals("name1", fieldNode.name);
		assertEquals("()J", fieldNode.desc);

		// should find the first insn node if multiple opcodes provided
		VarInsnNode varNode = AbstractMatcher.forward(
				m.instructions.getFirst(), Opcodes.ALOAD, Opcodes.GETFIELD);
		assertEquals(0, varNode.var);

		// should return null if no insn node found
		MethodInsnNode methodNode = AbstractMatcher.forward(
				m.instructions.getFirst(), Opcodes.INVOKESTATIC);
		assertNull(methodNode);

		// should return current insn node node if it's has the necessary opcode
		AbstractInsnNode insn = AbstractMatcher.forward(
				fieldNode, Opcodes.GETFIELD);
		assertSame(fieldNode, insn);
	}

	@Test
	public void backward() {
		m.visitInsn(Opcodes.NOP);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "owner", "name1", "()J");
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitFieldInsn(Opcodes.GETFIELD, "owner", "name2", "()V");
		m.visitInsn(Opcodes.RETURN);

		// should find the first insn node with the provided opcode
		FieldInsnNode fieldNode = AbstractMatcher.backward(
				m.instructions.getLast(), new AbstractMatcher.Predicate() {
					public boolean matches(AbstractInsnNode node) {
						return node.getType() == AbstractInsnNode.FIELD_INSN;
					}
				});
		assertEquals("owner", fieldNode.owner);
		assertEquals("name2", fieldNode.name);
		assertEquals("()V", fieldNode.desc);

		// should return null if no insn node found
		MethodInsnNode methodNode = AbstractMatcher.backward(
				m.instructions.getLast(), new AbstractMatcher.Predicate() {
					public boolean matches(AbstractInsnNode node) {
						return node.getType() == AbstractInsnNode.METHOD_INSN;
					}
				});
		assertNull(methodNode);

		// should return current insn node node if it's has the necessary opcode
		AbstractInsnNode insn = AbstractMatcher.backward(
				fieldNode, new AbstractMatcher.Predicate() {
					public boolean matches(AbstractInsnNode node) {
						return node.getOpcode() == Opcodes.GETFIELD
								&& "name2".equals(((FieldInsnNode) node).name);
					}
				});
		assertSame(fieldNode, insn);
	}

	@Test
	public void forward() {
		m.visitInsn(Opcodes.NOP);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "owner", "name1", "()J");
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitFieldInsn(Opcodes.GETFIELD, "owner", "name2", "()V");
		m.visitInsn(Opcodes.RETURN);

		// should find the first insn node with the provided opcode
		FieldInsnNode fieldNode = AbstractMatcher.forward(
				m.instructions.getFirst(), new AbstractMatcher.Predicate() {
					public boolean matches(AbstractInsnNode node) {
						return node.getType() == AbstractInsnNode.FIELD_INSN;
					}
				});
		assertEquals("owner", fieldNode.owner);
		assertEquals("name1", fieldNode.name);
		assertEquals("()J", fieldNode.desc);

		// should return null if no insn node found
		MethodInsnNode methodNode = AbstractMatcher.backward(
				m.instructions.getFirst(), new AbstractMatcher.Predicate() {
					public boolean matches(AbstractInsnNode node) {
						return node.getType() == AbstractInsnNode.METHOD_INSN;
					}
				});
		assertNull(methodNode);

		// should return current insn node node if it's has the necessary opcode
		AbstractInsnNode insn = AbstractMatcher.backward(
				fieldNode, new AbstractMatcher.Predicate() {
					public boolean matches(AbstractInsnNode node) {
						return node.getOpcode() == Opcodes.GETFIELD
								&& "name1".equals(((FieldInsnNode) node).name);
					}
				});
		assertSame(fieldNode, insn);
	}

	@Test
	public void count() {
		m.visitInsn(Opcodes.NOP);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "owner", "name1", "()J");
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitFieldInsn(Opcodes.GETFIELD, "owner", "name2", "()V");
		m.visitInsn(Opcodes.RETURN);

		final int varInsns = AbstractMatcher.count(m.instructions.getFirst(),
				new AbstractMatcher.OpcodePredicate(Opcodes.ALOAD));
		assertEquals(2, varInsns);

		final int fieldInsns = AbstractMatcher.count(m.instructions.getFirst(),
				new AbstractMatcher.OpcodePredicate(Opcodes.GETFIELD));
		assertEquals(2, fieldInsns);

		final int methodInsns = AbstractMatcher.count(m.instructions.getFirst(),
				new AbstractMatcher.OpcodePredicate(Opcodes.INVOKEVIRTUAL));
		assertEquals(0, methodInsns);
	}

}
