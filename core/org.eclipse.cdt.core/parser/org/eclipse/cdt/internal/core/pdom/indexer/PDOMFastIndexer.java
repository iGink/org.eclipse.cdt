/*******************************************************************************
 * Copyright (c) 2006, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.indexer;

import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * Configures the abstract indexer to return tasks suitable for fast indexing.
 */
public class PDOMFastIndexer extends AbstractPDOMIndexer {
	// Must match extension id
	public static final String ID = IPDOMManager.ID_FAST_INDEXER;

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public IPDOMIndexerTask createTask(ITranslationUnit[] added, ITranslationUnit[] changed,
			ITranslationUnit[] removed) {
		return new PDOMFastIndexerTask(this, added, changed, removed);
	}
}
