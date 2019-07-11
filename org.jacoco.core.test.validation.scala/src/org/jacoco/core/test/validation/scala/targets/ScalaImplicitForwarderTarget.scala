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
object ScalaImplicitForwarderTarget { // assertFullyCovered()

  implicit class Bar(target: ScalaImplicitForwarderTarget) { // assertFullyCovered()
    def baz(): Unit = nop() // assertFullyCovered()
  }

  def main(args: Array[String]): Unit = {
    /* in case of explicit instantiation compiler will not call the
     * corresponding factory method implicitly unlike to
     * ScalaImplicitForwarderTarget().baz() */
    new Bar(new ScalaImplicitForwarderTarget()).baz()
  }

}

class ScalaImplicitForwarderTarget { // assertFullyCovered()

}