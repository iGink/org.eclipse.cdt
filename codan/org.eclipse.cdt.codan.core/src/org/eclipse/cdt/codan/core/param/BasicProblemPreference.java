/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.param;

import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.regex.Pattern;

/**
 * Preference representing a problem preference of a basic type.
 * 
 * @see IProblemPreferenceDescriptor.PreferenceType for types.
 * 
 */
public class BasicProblemPreference extends AbstractProblemPreference {
	Object value;
	{
		key = PARAM;
		type = PreferenceType.TYPE_STRING;
	}

	/**
	 * Generate an info with given key and label
	 * 
	 * @param key
	 *            - property id (use in actual property hash of a checker)
	 * @param label
	 *            - label to be shown to user
	 * @param type
	 *            - parameter type
	 * @return
	 */
	public BasicProblemPreference(String key, String label, PreferenceType type) {
		if (key == null)
			throw new NullPointerException("key"); //$NON-NLS-1$
		if (type == null)
			throw new NullPointerException("type"); //$NON-NLS-1$
		setKey(key);
		setLabel(label);
		setType(type);
	}

	/**
	 * Generate an info with given key and label
	 * 
	 * @param key
	 *            - property id (use in actual property hash of a checker)
	 * @param label
	 *            - label to be shown to user
	 * @return
	 */
	public BasicProblemPreference(String key, String label) {
		setKey(key);
		setLabel(label);
	}

	@Override
	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public Object getValue() {
		return value;
	}

	public String exportValue() {
		Pattern pat = Pattern.compile("^[A-Za-z0-9._-]+$"); //$NON-NLS-1$
		String x = String.valueOf(getValue());
		if (pat.matcher(x).find() == false)
			return escape(x);
		return x;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.core.param.IProblemPreferenceValue#importValue(
	 * java.lang.String)
	 */
	@Override
	public void importValue(String str) {
		if (str.startsWith("\"")) //$NON-NLS-1$
			str = unescape(str);
		switch (getType()) {
			case TYPE_STRING:
				setValue(str);
				break;
			case TYPE_INTEGER:
				setValue(Integer.parseInt(str));
				break;
			case TYPE_BOOLEAN:
				setValue(Boolean.valueOf(str));
				break;
			case TYPE_FILE:
				setValue(new File(str));
				break;
			default:
				throw new IllegalArgumentException(getType()
						+ " is not supported for basic type"); //$NON-NLS-1$
		}
	}

	@Override
	public String toString() {
		return "(" + type + ")" + key + ((value == null) ? "" : "=" + value); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
	}

	@Override
	public void importValue(StreamTokenizer tokenizer) {
		try {
			tokenizer.nextToken();
			String val = tokenizer.sval;
			importValue(val);
		} catch (IOException e) {
			new IllegalArgumentException(e);
		}
	}
}
