/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * {@link IFilterContext} mock for unit tests.
 */
public class FilterContextMock implements IFilterContext {

	public String className = "Foo";
	public String superClassName = "java/lang/Object";
	public Set<String> classInterfaces = new HashSet<String>();
	public Set<String> classAnnotations = new HashSet<String>();
	public Set<String> classAttributes = new HashSet<String>();
	public List<MethodNode> classMethods = new ArrayList<MethodNode>();
	public List<FieldNode> classFields = new ArrayList<FieldNode>();
	public String sourceFileName = "Foo.java";
	public String sourceDebugExtension;

	public String getClassName() {
		return className;
	}

	public String getSuperClassName() {
		return superClassName;
	}

	public Set<String> getClassInterfaces() {
		return classInterfaces;
	}

	public Set<String> getClassAnnotations() {
		return classAnnotations;
	}

	public Set<String> getClassAttributes() {
		return classAttributes;
	}

	public List<MethodNode> getClassMethods() {
		return classMethods;
	}

	public List<FieldNode> getClassFields() {
		return classFields;
	}

	public String getSourceFileName() {
		return sourceFileName;
	}

	public String getSourceDebugExtension() {
		return sourceDebugExtension;
	}

}
