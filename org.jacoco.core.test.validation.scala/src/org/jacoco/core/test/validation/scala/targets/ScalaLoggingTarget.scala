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

import java.util.logging.{Level, Logger}
import org.jacoco.core.test.validation.targets.Stubs.nop

/**
  * Test target for scala logging blocks.
  */
object ScalaLoggingTarget {

  val logger: Logger = Logger.getLogger(this.getClass.getName)

  def main(args: Array[String]): Unit = {
    nop() // assertFullyCovered()
    if (logger.isLoggable(Level.FINE)) { // assertFullyCovered()
      logger.log(Level.FINE, "Hello World!") // assertEmpty()
    }
    nop() // assertFullyCovered()
    if (logger.isLoggable(Level.FINEST)) { // assertFullyCovered()
      logger.log(Level.FINEST, "Bye World!") // assertEmpty()
    }
    nop() // assertFullyCovered()
  }

}
