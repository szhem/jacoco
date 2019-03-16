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

/**
  * Test target for scala modules.
  */
object ScalaModuleTarget {

  object ScalaModule { // assertFullyCovered()

  } // assertEmpty()

  object SerializableScalaModule extends Serializable { // assertFullyCovered()

  } // assertEmpty()

  object AnyValScalaModule { // assertFullyCovered()

  } // assertEmpty()
  class AnyValScalaModule(val v: String) extends AnyVal {
    def foo: String = v
  }

  def main(args: Array[String]): Unit = {
    ScalaModule
    SerializableScalaModule
    AnyValScalaModule
  }
}
