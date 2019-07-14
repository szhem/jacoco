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
package org.jacoco.core.test.validation.scala;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.scala.targets.ScalaStaticForwarderTarget;
import org.junit.Test;

/**
 * Test of scala static forwarder methods.
 */
public class ScalaStaticForwarderTest extends ValidationTestBase {

	public ScalaStaticForwarderTest() {
		super(ScalaStaticForwarderTarget.class);
	}

	@Test
	public void test_method_count() {
		// ScalaStaticForwarderTarget$.<clinit> : filtered
		// ScalaStaticForwarderTarget$.<init>   : filtered
		// ScalaStaticForwarderTarget$.main     : covered
		// ScalaStaticForwarderTarget$.foo      : covered
		// ScalaStaticForwarderTarget.<init>    : covered
		// ScalaStaticForwarderTarget.main      : filtered
		// ScalaStaticForwarderTarget.foo       : filtered
		assertMethodCount(0, 3);
	}

}
