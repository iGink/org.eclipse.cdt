/*******************************************************************************
 * Copyright (c) 2009,2010 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.param;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.codan.internal.core.CharOperation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * TODO: add description
 */
public class FileScopeProblemPreference extends AbstractProblemPreference {
	public static final String KEY = "fileScope"; //$NON-NLS-1$
	public static final String EXCLUSION = "exclusion"; //$NON-NLS-1$
	public static final String INCLUSION = "inclusion"; //$NON-NLS-1$
	private IResource resource;
	private IPath[] inclusion = new IPath[0];
	private IPath[] exclusion = new IPath[0];

	public FileScopeProblemPreference() {
		setKey(KEY);
		setLabel("File Exclusion and Inclusion");
		setType(PreferenceType.TYPE_CUSTOM);
	}

	/**
	 * @param key
	 * @return
	 */
	public IPath[] getAttribute(String key) {
		if (key == EXCLUSION)
			return exclusion;
		if (key == INCLUSION)
			return inclusion;
		return null;
	}

	public void setAttribute(String key, IPath[] value) {
		if (key == EXCLUSION)
			exclusion = value.clone();
		if (key == INCLUSION)
			inclusion = value.clone();
	}

	/**
	 * @return
	 */
	public IProject getProject() {
		if (resource != null)
			return resource.getProject();
		return null;
	}

	/**
	 * @return
	 */
	public IPath getPath() {
		if (resource != null)
			return resource.getFullPath();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		return root.getFullPath();
	}

	/**
	 * @param resource
	 *            the resource to set
	 */
	public void setResource(IResource resource) {
		this.resource = resource;
	}

	/**
	 * @return the resource
	 */
	public IResource getResource() {
		return resource;
	}

	public String exportValue() {
		return exportPathList(INCLUSION, inclusion) + "," //$NON-NLS-1$
				+ exportPathList(EXCLUSION, exclusion);
	}

	/**
	 * @param inclusion2
	 * @param inclusion3
	 * @return
	 */
	private String exportPathList(String key, IPath[] arr) {
		String res = key + "=>("; //$NON-NLS-1$
		for (int i = 0; i < arr.length; i++) {
			if (i != 0)
				res += ","; //$NON-NLS-1$
			res += escape(arr[i].toPortableString());
		}
		return res + ")"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.core.param.AbstractProblemPreference#importValue
	 * (java.io.StreamTokenizer)
	 */
	@Override
	public void importValue(StreamTokenizer tokenizer) throws IOException {
		List<IPath> inc = importPathList(tokenizer, INCLUSION);
		inclusion = inc.toArray(new IPath[inc.size()]);
		checkChar(tokenizer, ',');
		List<IPath> exc = importPathList(tokenizer, EXCLUSION);
		exclusion = exc.toArray(new IPath[exc.size()]);
	}

	/**
	 * @param tokenizer
	 * @param c
	 * @throws IOException
	 */
	private void checkChar(StreamTokenizer tokenizer, char c)
			throws IOException {
		tokenizer.nextToken();
		if (tokenizer.ttype != c)
			throw new IllegalArgumentException("Expected " + c); //$NON-NLS-1$
	}

	/**
	 * @param tokenizer
	 * @param inclusion2
	 * @throws IOException
	 * @throws IllegalAccessException
	 */
	private void checkKeyword(StreamTokenizer tokenizer, String keyword)
			throws IOException {
		tokenizer.nextToken();
		if (tokenizer.sval == null || !tokenizer.sval.equals(keyword))
			throw new IllegalArgumentException("Expected " + keyword); //$NON-NLS-1$
	}

	protected List<IPath> importPathList(StreamTokenizer tokenizer,
			String keyword) throws IOException {
		checkKeyword(tokenizer, keyword);
		checkChar(tokenizer, '=');
		checkChar(tokenizer, '>');
		ArrayList<IPath> list = new ArrayList<IPath>();
		int token;
		int index = 0;
		try {
			checkChar(tokenizer, '(');
			token = tokenizer.nextToken();
			if (token != ')')
				tokenizer.pushBack();
			else
				return Collections.emptyList();
			while (true) {
				token = tokenizer.nextToken();
				if (tokenizer.sval == null)
					throw new IllegalArgumentException();
				list.add(new Path(tokenizer.sval));
				token = tokenizer.nextToken();
				if (token == ')')
					break;
				tokenizer.pushBack();
				checkChar(tokenizer, ',');
				index++;
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		return list;
	}

	@Override
	public Object getValue() {
		return this;
	}

	@Override
	public void setValue(Object value) {
		if (this == value)
			return;
		FileScopeProblemPreference scope = (FileScopeProblemPreference) value;
		setAttribute(INCLUSION, scope.getAttribute(INCLUSION));
		setAttribute(EXCLUSION, scope.getAttribute(EXCLUSION));
		this.resource = scope.getResource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.param.AbstractProblemPreference#clone()
	 */
	@Override
	public Object clone() {
		FileScopeProblemPreference scope = (FileScopeProblemPreference) super
				.clone();
		scope.setValue(this);
		return scope;
	}

	/**
	 * @param file
	 * @return
	 */
	public boolean isInScope(IPath file) {
		//System.err.println("test " + file + " " + exportValue());
		if (inclusion.length > 0) {
			if (!matchesFilter(file, inclusion))
				return false;
		}
		if (exclusion.length > 0) {
			if (matchesFilter(file, exclusion))
				return false;
		}
		return true;
	}

	/**
	 * @param resourcePath
	 * @param inclusion2
	 * @return
	 */
	private boolean matchesFilter(IPath resourcePath, IPath[] paths) {
		char[] path = resourcePath.toString().toCharArray();
		for (int i = 0, length = paths.length; i < length; i++) {
			char[] pattern = paths[i].toString().toCharArray();
			if (CharOperation.pathMatch(pattern, path, true, '/')) {
				return true;
			}
		}
		return false;
	}

	public static boolean isExcludedPath(IPath resourcePath, IPath[] paths) {
		char[] path = resourcePath.toString().toCharArray();
		for (int i = 0, length = paths.length; i < length; i++) {
			char[] pattern = paths[i].toString().toCharArray();
			if (CharOperation.pathMatch(pattern, path, true, '/')) {
				return true;
			}
		}
		return false;
	}
}
