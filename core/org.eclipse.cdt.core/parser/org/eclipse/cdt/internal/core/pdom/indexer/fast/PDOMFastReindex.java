/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer.fast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMFastReindex extends PDOMFastIndexerJob {

	public PDOMFastReindex(PDOM pdom) {
		super(pdom);
	}
	
	protected IStatus run(IProgressMonitor monitor) {
		try {
			long start = System.currentTimeMillis();
			
			// First clear out the DB
			pdom.clear();
			
			// First index all the source files (i.e. not headers)
			final List addedSources = new ArrayList();
			
			pdom.getProject().getProject().accept(new IResourceProxyVisitor() {
				public boolean visit(IResourceProxy proxy) throws CoreException {
					if (proxy.getType() == IResource.FILE) {
						String fileName = proxy.getName();
						IContentType contentType = Platform.getContentTypeManager().findContentTypeFor(fileName);
						if (contentType == null)
							return true;
						String contentTypeId = contentType.getId();
						
						if (CCorePlugin.CONTENT_TYPE_CXXSOURCE.equals(contentTypeId)
								|| CCorePlugin.CONTENT_TYPE_CSOURCE.equals(contentTypeId)) {
							addedSources.add((ITranslationUnit)CoreModel.getDefault().create((IFile)proxy.requestResource()));
						}
						return false;
					} else {
						return true;
					}
				}
			}, 0);
			
			for (Iterator i = addedSources.iterator(); i.hasNext();)
				addTU((ITranslationUnit)i.next());

			// Now add in the header files but only if they aren't already indexed
			final List addedHeaders = new ArrayList();
			
			pdom.getProject().getProject().accept(new IResourceProxyVisitor() {
				public boolean visit(IResourceProxy proxy) throws CoreException {
					if (proxy.getType() == IResource.FILE) {
						String fileName = proxy.getName();
						IContentType contentType = Platform.getContentTypeManager().findContentTypeFor(fileName);
						if (contentType == null)
							return true;
						String contentTypeId = contentType.getId();
						
						if (CCorePlugin.CONTENT_TYPE_CXXHEADER.equals(contentTypeId)
								|| CCorePlugin.CONTENT_TYPE_CHEADER.equals(contentTypeId)) {
							IFile rfile = (IFile)proxy.requestResource();
							String filename = rfile.getLocation().toOSString();
							if (pdom.getFile(filename) == null)
								addedHeaders.add((ITranslationUnit)CoreModel.getDefault().create(rfile));
						}
						return false;
					} else {
						return true;
					}
				}
			}, 0);

			for (Iterator i = addedHeaders.iterator(); i.hasNext();)
				addTU((ITranslationUnit)i.next());
			
			String showTimings = Platform.getDebugOption(CCorePlugin.PLUGIN_ID
					+ "/debug/pdomtimings"); //$NON-NLS-1$
			if (showTimings != null && showTimings.equalsIgnoreCase("true")) //$NON-NLS-1$
				System.out.println("PDOM Fast Reindex Time: " + (System.currentTimeMillis() - start)); //$NON-NLS-1$

			return Status.OK_STATUS;
		} catch (CoreException e) {
			return e.getStatus();
		} catch (InterruptedException e) {
			return Status.CANCEL_STATUS;
		}
	}

}
