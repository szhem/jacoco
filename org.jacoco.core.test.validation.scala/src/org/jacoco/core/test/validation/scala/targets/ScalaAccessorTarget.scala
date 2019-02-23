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
  * Test target for scala accessors and mutators.
  */
object ScalaAccessorTarget {

  class ScalaClass {
    val foo = "foo" // assertFullyCovered()
    var bar = "bar" // assertFullyCovered()
    private[scala] var _baz: String = _ // assertEmpty()
    def baz: String = _baz // assertNotCovered()
    def baz_=(v: String): Unit = _baz = v // assertNotCovered()
    private[scala] var foobar: String = "foobar" // assertFullyCovered()
  } // assertEmpty()

  def main(args: Array[String]): Unit = {
    new ScalaClass
  }
}
