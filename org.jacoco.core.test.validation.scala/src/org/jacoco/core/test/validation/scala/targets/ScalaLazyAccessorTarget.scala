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
  * Test target for scala lazy accessors.
  */
object ScalaLazyAccessorTarget {

  def main(args: Array[String]): Unit = {
    val boolBitmap = new ScalaClassBoolBitmap
    boolBitmap.foo0  /* one boolean to contain 1 bit */

    val byteBitmap = new ScalaClassByteBitmap
    byteBitmap.foo0
    byteBitmap.foo1 /* one byte(8 bits) to contain 1+1=2 bits */

    val intBitmap = new ScalaClassIntBitmap
    intBitmap.foo0
    intBitmap.foo8 /* one int(32 bits) to contain 8+1=9 bits */

    val longBitmap = new ScalaClassLongBitmap
    longBitmap.foo0
    longBitmap.foo32 /* one long(64 bits) to contain 32+1=33 bits */

    val doubleLongBitmap = new ScalaClassDoubleLongBitmap
    doubleLongBitmap.foo0
    doubleLongBitmap.foo64 /* two longs(128 bits) to contain 64+1=65 bits */
  }

  class ScalaClassBoolBitmap {
    lazy val foo0 = "foo0" // assertFullyCovered()
  } // assertEmpty()

  class ScalaClassByteBitmap {
    lazy val foo0 = "foo0" // assertFullyCovered()
    lazy val foo1 = "foo1" // assertFullyCovered()
  } // assertEmpty()

  class ScalaClassIntBitmap {
    lazy val foo0 = "foo0" // assertFullyCovered()
    lazy val foo1 = "foo1" // assertNotCovered()
    lazy val foo2 = "foo2" // assertNotCovered()
    lazy val foo3 = "foo3" // assertNotCovered()
    lazy val foo4 = "foo4" // assertNotCovered()
    lazy val foo5 = "foo5" // assertNotCovered()
    lazy val foo6 = "foo6" // assertNotCovered()
    lazy val foo7 = "foo7" // assertNotCovered()
    lazy val foo8 = "foo8" // assertFullyCovered()
  } // assertEmpty()

  class ScalaClassLongBitmap {
    lazy val foo0 = "foo0" // assertFullyCovered()
    lazy val foo1 = "foo1" // assertNotCovered()
    lazy val foo2 = "foo2" // assertNotCovered()
    lazy val foo3 = "foo3" // assertNotCovered()
    lazy val foo4 = "foo4" // assertNotCovered()
    lazy val foo5 = "foo5" // assertNotCovered()
    lazy val foo6 = "foo6" // assertNotCovered()
    lazy val foo7 = "foo7" // assertNotCovered()
    lazy val foo8 = "foo8" // assertNotCovered()
    lazy val foo9 = "foo9" // assertNotCovered()
    lazy val foo10 = "foo10" // assertNotCovered()
    lazy val foo11 = "foo11" // assertNotCovered()
    lazy val foo12 = "foo12" // assertNotCovered()
    lazy val foo13 = "foo13" // assertNotCovered()
    lazy val foo14 = "foo14" // assertNotCovered()
    lazy val foo15 = "foo15" // assertNotCovered()
    lazy val foo16 = "foo16" // assertNotCovered()
    lazy val foo17 = "foo17" // assertNotCovered()
    lazy val foo18 = "foo18" // assertNotCovered()
    lazy val foo19 = "foo19" // assertNotCovered()
    lazy val foo20 = "foo20" // assertNotCovered()
    lazy val foo21 = "foo21" // assertNotCovered()
    lazy val foo22 = "foo22" // assertNotCovered()
    lazy val foo23 = "foo23" // assertNotCovered()
    lazy val foo24 = "foo24" // assertNotCovered()
    lazy val foo25 = "foo25" // assertNotCovered()
    lazy val foo26 = "foo26" // assertNotCovered()
    lazy val foo27 = "foo27" // assertNotCovered()
    lazy val foo28 = "foo28" // assertNotCovered()
    lazy val foo29 = "foo29" // assertNotCovered()
    lazy val foo30 = "foo30" // assertNotCovered()
    lazy val foo31 = "foo31" // assertNotCovered()
    lazy val foo32 = "foo32" // assertFullyCovered()
  } // assertEmpty()

  class ScalaClassDoubleLongBitmap {
    lazy val foo0 = "foo0" // assertFullyCovered()
    lazy val foo1 = "foo1" // assertNotCovered()
    lazy val foo2 = "foo2" // assertNotCovered()
    lazy val foo3 = "foo3" // assertNotCovered()
    lazy val foo4 = "foo4" // assertNotCovered()
    lazy val foo5 = "foo5" // assertNotCovered()
    lazy val foo6 = "foo6" // assertNotCovered()
    lazy val foo7 = "foo7" // assertNotCovered()
    lazy val foo8 = "foo8" // assertNotCovered()
    lazy val foo9 = "foo9" // assertNotCovered()
    lazy val foo10 = "foo10" // assertNotCovered()
    lazy val foo11 = "foo11" // assertNotCovered()
    lazy val foo12 = "foo12" // assertNotCovered()
    lazy val foo13 = "foo13" // assertNotCovered()
    lazy val foo14 = "foo14" // assertNotCovered()
    lazy val foo15 = "foo15" // assertNotCovered()
    lazy val foo16 = "foo16" // assertNotCovered()
    lazy val foo17 = "foo17" // assertNotCovered()
    lazy val foo18 = "foo18" // assertNotCovered()
    lazy val foo19 = "foo19" // assertNotCovered()
    lazy val foo20 = "foo20" // assertNotCovered()
    lazy val foo21 = "foo21" // assertNotCovered()
    lazy val foo22 = "foo22" // assertNotCovered()
    lazy val foo23 = "foo23" // assertNotCovered()
    lazy val foo24 = "foo24" // assertNotCovered()
    lazy val foo25 = "foo25" // assertNotCovered()
    lazy val foo26 = "foo26" // assertNotCovered()
    lazy val foo27 = "foo27" // assertNotCovered()
    lazy val foo28 = "foo28" // assertNotCovered()
    lazy val foo29 = "foo29" // assertNotCovered()
    lazy val foo30 = "foo30" // assertNotCovered()
    lazy val foo31 = "foo31" // assertNotCovered()
    lazy val foo32 = "foo32" // assertNotCovered()
    lazy val foo33 = "foo33" // assertNotCovered()
    lazy val foo34 = "foo34" // assertNotCovered()
    lazy val foo35 = "foo35" // assertNotCovered()
    lazy val foo36 = "foo36" // assertNotCovered()
    lazy val foo37 = "foo37" // assertNotCovered()
    lazy val foo38 = "foo38" // assertNotCovered()
    lazy val foo39 = "foo39" // assertNotCovered()
    lazy val foo40 = "foo40" // assertNotCovered()
    lazy val foo41 = "foo41" // assertNotCovered()
    lazy val foo42 = "foo42" // assertNotCovered()
    lazy val foo43 = "foo43" // assertNotCovered()
    lazy val foo44 = "foo44" // assertNotCovered()
    lazy val foo45 = "foo45" // assertNotCovered()
    lazy val foo46 = "foo46" // assertNotCovered()
    lazy val foo47 = "foo47" // assertNotCovered()
    lazy val foo48 = "foo48" // assertNotCovered()
    lazy val foo49 = "foo49" // assertNotCovered()
    lazy val foo50 = "foo50" // assertNotCovered()
    lazy val foo51 = "foo51" // assertNotCovered()
    lazy val foo52 = "foo52" // assertNotCovered()
    lazy val foo53 = "foo53" // assertNotCovered()
    lazy val foo54 = "foo54" // assertNotCovered()
    lazy val foo55 = "foo55" // assertNotCovered()
    lazy val foo56 = "foo56" // assertNotCovered()
    lazy val foo57 = "foo57" // assertNotCovered()
    lazy val foo58 = "foo58" // assertNotCovered()
    lazy val foo59 = "foo59" // assertNotCovered()
    lazy val foo60 = "foo60" // assertNotCovered()
    lazy val foo61 = "foo61" // assertNotCovered()
    lazy val foo62 = "foo62" // assertNotCovered()
    lazy val foo63 = "foo63" // assertNotCovered()
    lazy val foo64 = "foo64" // assertFullyCovered()
  } // assertEmpty()

}
