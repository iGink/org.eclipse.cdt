package org.eclipse.cdt.internal.ui.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IStatus;

import org.eclipse.cdt.internal.CCorePlugin;

/**
 * A settable IStatus
 * Can be an error, warning, info or ok. For error, info and warning states,
 * a message describes the problem
 */
public class StatusInfo implements IStatus {
	
	private String fStatusMessage;
	private int fSeverity;
	
	/**
	 * @see IStatus#getChildren()
	 */
	public IStatus[] getChildren() {
		return new IStatus[0];
	}
	/**
	 * @see IStatus#getCode()
	 */
	public int getCode() {
		return fSeverity;
	}
	/**
	 * @see IStatus#getException()
	 */
	public Throwable getException() {
		return null;
	}
	/**
	 * @see IStatus#getMessage
	 */
	public String getMessage() {
		return fStatusMessage;
	}
	/**
	 * @see IStatus#getPlugin()
	 */
	public String getPlugin() {
		return CCorePlugin.PLUGIN_ID;
	}
	/**
	 * @see IStatus#getSeverity()
	 */
	public int getSeverity() {
		return fSeverity;
	}
	public boolean isError() {
		return fSeverity == IStatus.ERROR;
	}
	public boolean isInfo() {
		return fSeverity == IStatus.INFO;
	}
	/**
	 * @see IStatus#isMultiStatus()
	 */
	public boolean isMultiStatus() {
		return false;
	}
	public boolean isOK() {
		return fSeverity == IStatus.OK;
	}
	public boolean isWarning() {
		return fSeverity == IStatus.WARNING;
	}
	/**
	 * @see IStatus#matches(int)
	 */
	public boolean matches(int severityMask) {
		return (fSeverity & severityMask) != 0;
	}
	public void setError(String errorMessage) {
		fStatusMessage= errorMessage;
		fSeverity= IStatus.ERROR;
	}
	public void setInfo(String infoMessage) {
		fStatusMessage= infoMessage;
		fSeverity= IStatus.INFO;
	}
	public void setOK() {
		fStatusMessage= null;
		fSeverity= IStatus.OK;
	}
	public void setWarning(String warningMessage) {
		fStatusMessage= warningMessage;
		fSeverity= IStatus.WARNING;
	}
	public StatusInfo() {
		fStatusMessage= null;
		fSeverity= OK;
	}
}
