/**********************************************************************
 * Created on Mar 25, 2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.model;

import org.eclipse.core.runtime.IPath;

public interface IContainerEntry extends IPathEntry {

	/**
	 * Returns the id identifying this container.
	 * @return IPath
	 */
	IPath getPath();

}
