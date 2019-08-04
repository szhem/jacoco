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

object ScalaValueClassTarget {

  def main(args: Array[String]): Unit = {
    /* although there is a constructor call, the compiler removes it and
       calls ScalaValueClassTarget$.bar$extension(foo, bar) method instead */
    new ScalaValueClassTarget("foo").bar("bar")
  }

}

class ScalaValueClassTarget(val s: String) extends AnyVal { // assertEmpty()

  def bar(v: String): String = s + v // assertFullyCovered()

}

