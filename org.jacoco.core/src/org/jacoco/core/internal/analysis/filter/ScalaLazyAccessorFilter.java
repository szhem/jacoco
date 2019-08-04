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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Detects generated lazy accessors.
 */
public class ScalaLazyAccessorFilter extends ScalaFilter {

	private static final String BITMAP_PREFIX = "bitmap$";

	@Override
	Collection<? extends ScalaMatcher> getMatchers() {
		return Arrays.asList(
				new LazyAccessorMatcher(),
				new LazyComputeMatcher()
		);
	}

	/**
	 * Filters lazy accessor methods generated for Scala lazy values.
	 * <p>
	 * Consider lazy value "foo" in the following example:
	 * <pre>{@code
	 * class Main {
	 *   lazy val foo = "foo"
	 *   lazy val foo1 = "foo1"
	 * }
	 * }</pre>
	 * ... which is compiled into the following byte code:
	 * <pre>{@code
	 * public class Main {
	 *   private java.lang.String foo;
	 *     descriptor: Ljava/lang/String;
	 *     flags: (0x0002) ACC_PRIVATE
	 *   private volatile byte bitmap$0;
	 *     descriptor: B
	 *     flags: (0x0042) ACC_PRIVATE, ACC_VOLATILE
	 *   public java.lang.String foo();
	 *     descriptor: ()Ljava/lang/String;
	 *     flags: (0x0001) ACC_PUBLIC
	 *     Code:
	 *       stack=2, locals=1, args_size=1
	 *          0: aload_0
	 *          1: getfield      #14 // Field bitmap$0:B
	 *          4: iconst_1
	 *          5: iand
	 *          6: i2b
	 *          7: iconst_0
	 *          8: if_icmpne     18
	 *         11: aload_0
	 *         12: invokespecial #65 // Method foo$lzycompute:()Ljava/lang/String;
	 *         15: goto          22
	 *         18: aload_0
	 *         19: getfield      #17 // Field foo:Ljava/lang/String;
	 *         22: areturn
	 * }</pre>
	 * So the lazy accessor method should be excluded if
	 * - there is a field with the same name
	 * - method has no arguments
	 * - method's return type is equal to the corresponding field's type
	 * - method does nothing except for
	 *   - verifying the corresponding bitmap field to check whether the
	 *     given field has already been initialized
	 *   - optionally invoking the corresponding $lzycompute method
	 *   - returning the value of the initialized field
	 * </p>
	 */
	private static class LazyAccessorMatcher extends ScalaMatcher {

		@Override
		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			firstIsALoad0(methodNode);
			// find bitmap$x field
			nextIs(Opcodes.GETFIELD);
			if (cursor == null ||
					!((FieldInsnNode) cursor).name.startsWith(BITMAP_PREFIX)) {
				return;
			}

			// find a branch ("if" statement) whether the comparison of the bit
			// against 0 succeeds
			forward(Opcodes.IFEQ, Opcodes.IFNE, Opcodes.IF_ICMPNE);
			if (cursor == null) {
				return;
			}

			// find xxx$lzycompute call
			forward(new Predicate() {
				public boolean matches(AbstractInsnNode node) {
					return node.getOpcode() == Opcodes.INVOKESPECIAL
							&& ((MethodInsnNode) node).desc.equals(methodNode.desc)
							&& ((MethodInsnNode) node).name.equals(methodNode.name + LAZY_SUFFIX);
				}
			});
			if (cursor == null) {
				return;
			}

			// find return statement
			forward(RETURN_OPCODES);
			if (cursor != null) {
				InsnList instructions = methodNode.instructions;
				output.ignore(instructions.getFirst(), instructions.getLast());
			}
		}
	}

	/**
	 * Filters lazy accessor methods generated for Scala lazy values.
	 * Care should be taken as scala compiler generated synthetic bitmap
	 * fields of different types (depending on the amount of lazy fields)
	 * to check whether the lazy fields have been initialized.
	 *
	 * <p>
	 * Consider lazy value "foo" in the following example:
	 * <pre>{@code
	 * class Main {
	 *   lazy val foo = "foo"
	 *   lazy val foo1 = "foo1"
	 * }
	 * }</pre>
	 * ... which is compiled into the following byte code:
	 * <pre>{@code
	 * public class Main {
	 *   private java.lang.String foo;
	 *     descriptor: Ljava/lang/String;
	 *     flags: (0x0002) ACC_PRIVATE
	 *   private volatile byte bitmap$0;
	 *     descriptor: B
	 *     flags: (0x0042) ACC_PRIVATE, ACC_VOLATILE
	 *   private java.lang.String foo$lzycompute();
	 *     descriptor: ()Ljava/lang/String;
	 *     flags: (0x0002) ACC_PRIVATE
	 *     Code:
	 *       stack=3, locals=2, args_size=1
	 *          0: aload_0
	 *          1: dup
	 *          2: astore_1
	 *          3: monitorenter
	 *          4: aload_0
	 *          5: getfield      #14 // Field bitmap$0:B
	 *          8: iconst_1
	 *          9: iand
	 *         10: i2b
	 *         11: iconst_0
	 *         12: if_icmpne     32
	 *         15: aload_0
	 *         16: ldc           #15 // String foo
	 *         18: putfield      #17 // Field foo:Ljava/lang/String;
	 *         21: aload_0
	 *         22: aload_0
	 *         23: getfield      #14 // Field bitmap$0:B
	 *         26: iconst_1
	 *         27: ior
	 *         28: i2b
	 *         29: putfield      #14 // Field bitmap$0:B
	 *         32: getstatic     #23 // Field scala/runtime/BoxedUnit.UNIT:Lscala/runtime/BoxedUnit;
	 *         35: pop
	 *         36: aload_1
	 *         37: monitorexit
	 *         38: aload_0
	 *         39: getfield      #17 // Field foo:Ljava/lang/String;
	 *         42: areturn
	 *         43: aload_1
	 *         44: monitorexit
	 *         45: athrow
	 * }
	 * }</pre>
	 *
	 * We'd like to retain only field initialization and retrieval instructions
	 * like that:
	 * <pre>{@code
	 * public class Main {
	 *   private java.lang.String foo;
	 *     descriptor: Ljava/lang/String;
	 *     flags: (0x0002) ACC_PRIVATE
	 *   private volatile byte bitmap$0;
	 *     descriptor: B
	 *     flags: (0x0042) ACC_PRIVATE, ACC_VOLATILE
	 *   private java.lang.String foo$lzycompute();
	 *     descriptor: ()Ljava/lang/String;
	 *     flags: (0x0002) ACC_PRIVATE
	 *     Code:
	 *       stack=3, locals=2, args_size=1
	 *         15: aload_0
	 *         16: ldc           #15 // String foo
	 *         18: putfield      #17 // Field foo:Ljava/lang/String;
	 *         21: aload_0
	 *         22: aload_0
	 *         23: getfield      #14 // Field bitmap$0:B
	 *         26: iconst_1
	 *         27: ior
	 *         28: i2b
	 *         29: putfield      #14 // Field bitmap$0:B
	 *         32: getstatic     #23 // Field scala/runtime/BoxedUnit.UNIT:Lscala/runtime/BoxedUnit;
	 *         35: pop
	 *         38: aload_0
	 *         39: getfield      #17 // Field foo:Ljava/lang/String;
	 *         42: areturn
	 * }</pre>
	 */
	private static class LazyComputeMatcher extends ScalaMatcher {

		@Override
		void ignoreMatches(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			final List<InsnRange> ranges = new ArrayList<InsnRange>();
			InsnList instructions = methodNode.instructions;

			if (!methodNode.name.endsWith(LAZY_SUFFIX)) {
				return;
			}

			firstIsALoad0(methodNode);

			// find monitorenter
			cursor = forward(cursor, Opcodes.MONITORENTER);
			if (cursor == null) {
				return;
			}

			// find field initialization
			cursor = forward(cursor, Opcodes.IFNE, Opcodes.IF_ICMPNE);
			if (cursor == null) {
				return;
			}
			// remember instructions till the beginning of the comparison
			// whether field is initialized or not
			ranges.add(new InsnRange(instructions.getFirst(), cursor));

			// check that field being initialized is our ones
			cursor = forward(cursor, Opcodes.PUTFIELD);
			FieldInsnNode lzyField = (FieldInsnNode) cursor;
			if (lzyField == null
					|| !methodNode.name.equals(lzyField.name + LAZY_SUFFIX)
					|| !methodNode.desc.equals("()" + lzyField.desc)) {
				return;
			}

			// find monitorexit
			cursor = forward(cursor, Opcodes.MONITOREXIT);
			if (cursor == null) {
				return;
			}
			// there should be ALOAD before calling monitorexit, so
			// remember instructions within the synchronized block, excluding
			// instructions to exit from it (synchronized block)
			ranges.add(new InsnRange(backward(cursor, Opcodes.ALOAD), cursor));

			// find instructions to return value of the field just set
			// and retain just these ones excluding everything else
			lzyField = forward(cursor, Opcodes.GETFIELD);
			if (lzyField == null
					|| !methodNode.name.equals(lzyField.name + LAZY_SUFFIX)
					|| !methodNode.desc.equals("()" + lzyField.desc)) {
				return;
			}

			// exclude everything between monitorexit and aload
			ranges.add(new InsnRange(cursor,
					backward(lzyField, Opcodes.ALOAD).getPrevious()));
			cursor = forward(lzyField, RETURN_OPCODES);
			if (cursor == null) {
				return;
			}
			// exclude everything after returning from the method
			if (cursor.getNext() != null) {
				ranges.add(new InsnRange(
						cursor.getNext(), instructions.getLast()));
			}

			// exclude all the instruction ranges collected so far
			for (InsnRange range : ranges) {
				output.ignore(range.getFrom(), range.getTo());
			}
		}
	}

	private static class InsnRange {

		private final AbstractInsnNode from;
		private final AbstractInsnNode to;

		private InsnRange(final AbstractInsnNode from,
				final AbstractInsnNode to) {
			this.from = from;
			this.to = to;
		}

		public AbstractInsnNode getFrom() {
			return from;
		}

		public AbstractInsnNode getTo() {
			return to;
		}
	}

}
