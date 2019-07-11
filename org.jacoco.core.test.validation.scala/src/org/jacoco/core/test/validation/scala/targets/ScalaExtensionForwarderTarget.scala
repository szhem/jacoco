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
  * Test target for scala extension forwarder methods.
  */
object ScalaExtensionForwarderTarget {

  def foo(v: String): String = v // assertNotCovered()

  def main(args: Array[String]): Unit = {
    /* to instantiate a value class
     * https://docs.scala-lang.org/overviews/core/value-classes.html
     *
     * although it may seem that 'bar' method is called, it's not true and
     * instead bar$extension method is called, so 'bar' method is
     * marked as not fully covered */
    Array(new ScalaExtensionForwarderTarget("foo")).foreach(_.bar("bar"))
  }

}

class ScalaExtensionForwarderTarget(val s: String) extends AnyVal { // assertFullyCovered()

  def bar(v: String): String = s + v
  
}

