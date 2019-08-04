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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.test.validation.Source.Line;
import org.jacoco.core.test.validation.ValidationTestBase;
import scala.util.Properties$;

/**
 * Test of scala implicit forwarder methods.
 */
public abstract class ScalaTest extends ValidationTestBase {

	public ScalaTest(Class<?> target) {
		super(target);
	}

	public void assertScalaVersion(final Line line, final String spec) throws Throwable {
		final String scalaVersion = Properties$.MODULE$.versionNumberString();
		final String majorVersion = scalaVersion
				.substring(0, scalaVersion.lastIndexOf('.'));

		final Map<String, String> specs = new HashMap<String, String>();
		for (final String versionSpec : spec.split(",")) {
			final String[] versionAssert = versionSpec.split("=");
			if (versionAssert.length == 1) {
				specs.put("", versionAssert[0].trim());
			} else {
				specs.put(versionAssert[0].trim(), versionAssert[1].trim());
			}
		}

		String methodName = specs.get(majorVersion);
		if (methodName == null) {
			methodName = specs.get("");
		}
		if (methodName == null) {
			throw new AssertionError("Unsupported scala version: " + scalaVersion);
		}

		try {
			this.getClass().getMethod(methodName, line.getClass())
					.invoke(this, line);
		} catch (InvocationTargetException e) {
			Throwable t = e;
			while (t != null && !(t instanceof AssertionError)) {
				t = t.getCause();
			}
			throw t != null ? t : e;
		}
	}

}
