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

import org.jacoco.core.test.validation.scala.targets.ScalaImplicitForwarderTarget;
import org.junit.Test;

/**
 * Test of scala implicit forwarder methods.
 */
public class ScalaImplicitForwarderTest extends ScalaTest {

	public ScalaImplicitForwarderTest() {
		super(ScalaImplicitForwarderTarget.class);
	}

	@Test
	public void test_method_count() {
		// ScalaImplicitForwarderTarget$.<clinit>  : filtered
		// ScalaImplicitForwarderTarget$.<init>    : filtered
		// ScalaImplicitForwarderTarget$.main      : covered
		// ScalaImplicitForwarderTarget$.Bar       : filtered (implicit factory of ScalaImplicitForwarderTarget$Bar)
		// ScalaImplicitForwarderTarget.<init>     : covered
		// ScalaImplicitForwarderTarget.main       : filtered (static forwarder to ScalaImplicitForwarderTarget$.main)
		// ScalaImplicitForwarderTarget.Bar        : filtered (static forwarder to ScalaImplicitForwarderTarget$.Bar)
		// ScalaImplicitForwarderTarget$Bar.<init> : covered
		// ScalaImplicitForwarderTarget$Bar.baz    : covered
		assertMethodCount(0, 4);
	}

}
