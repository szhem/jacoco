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
  * Test target for scala case classes.
  */
object ScalaCaseClassTarget {

  case class Foo(foo: String) // assertFullyCovered()
  case class Bar(bar: String*) // assertFullyCovered()
  case class Baz(baz: String) extends AnyVal // assertEmpty()
  case class FooBar( // assertFullyCovered()
    foo: String = "foo", // assertFullyCovered()
    bar: String = "bar"  // assertFullyCovered()
  )
  case object FooBaz     // assertFullyCovered()

  def main(args: Array[String]): Unit = {
    Foo("foo")
    Foo

    Bar("bar")
    Bar

    Baz("baz")
    Baz

    val foobar = FooBar()
    nop(foobar.foo + foobar.bar)

    FooBaz
  }
}
