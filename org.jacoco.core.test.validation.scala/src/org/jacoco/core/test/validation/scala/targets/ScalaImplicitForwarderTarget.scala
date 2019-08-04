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
package org.jacoco.core.test.validation.scala.targets

import org.jacoco.core.test.validation.targets.Stubs.nop

/**
  * Test target for scala forwarder methods.
  */
object ScalaImplicitForwarderTarget { // assertEmpty()

  /*
   * Scala 2.11 sets the line number of the constructor to be 44 instead of 40,
   * so it's necessary use version-specific assert.
   *
   * public org.jacoco.core.test.validation.scala.targets.ScalaImplicitForwarderTarget$Bar(org.jacoco.core.test.validation.scala.targets.ScalaImplicitForwarderTarget);
   *   descriptor: (Lorg/jacoco/core/test/validation/scala/targets/ScalaImplicitForwarderTarget;)V
   *   flags: ACC_PUBLIC
   *   Code:
   *     stack=1, locals=2, args_size=2
   *        0: aload_0
   *        1: invokespecial #18                 
   *        4: return
   *     LocalVariableTable:
   *       Start  Length  Slot  Name   Signature
   *           0       5     0  this   Lorg/jacoco/core/test/validation/scala/targets/ScalaImplicitForwarderTarget$Bar;
   *           0       5     1 target   Lorg/jacoco/core/test/validation/scala/targets/ScalaImplicitForwarderTarget;
   *     LineNumberTable:
   *       line 44: 0
   */
  implicit class Bar(target: ScalaImplicitForwarderTarget) { // assertScalaVersion("assertFullyCovered,2.11=assertEmpty")
    def baz(): Unit = nop() // assertFullyCovered()
  }

  def main(args: Array[String]): Unit = {
    /* in case of explicit instantiation compiler will not call the
     * corresponding factory method implicitly unlike to
     * ScalaImplicitForwarderTarget().baz() */
    new Bar(new ScalaImplicitForwarderTarget()).baz()
  }

}

/*
 * Scala 2.11 sets the line number of the constructor to be 73 instead of 71,
 * so it's necessary use version-specific assert.
 *
 * public org.jacoco.core.test.validation.scala.targets.ScalaImplicitForwarderTarget();
 *   descriptor: ()V
 *   flags: ACC_PUBLIC
 *   Code:
 *     stack=1, locals=1, args_size=1
 *        0: aload_0
 *        1: invokespecial #26
 *        4: return
 *     LocalVariableTable:
 *       Start  Length  Slot  Name   Signature
 *           0       5     0  this   Lorg/jacoco/core/test/validation/scala/targets/ScalaImplicitForwarderTarget;
 *     LineNumberTable:
 *       line 73: 0
 */
class ScalaImplicitForwarderTarget { // assertScalaVersion("assertFullyCovered,2.11=assertEmpty")

}