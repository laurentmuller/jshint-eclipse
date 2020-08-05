/*******************************************************************************
 * Copyright (c) 2012, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ralf Sternberg - initial implementation and API
 ******************************************************************************/
package com.eclipsesource.jshint.ui.preferences;

import java.io.IOException;

import org.osgi.service.prefs.Preferences;

import com.eclipsesource.jshint.ui.util.JsonUtils;
import com.eclipsesource.json.JsonObject;

public class OptionsPreferences {

	private static final String KEY_PROJ_SPECIFIC = "projectSpecificOptions"; //$NON-NLS-1$
	private static final String KEY_GLOBALS = "globals"; //$NON-NLS-1$
	private static final String KEY_OPTIONS = "options"; //$NON-NLS-1$
	private static final String KEY_CONFIG = "config"; //$NON-NLS-1$

	public static final boolean DEFAULT_PROJ_SPECIFIC = false;

	public static final String DEFAULT_CONFIG = "{\n  \n}"; //$NON-NLS-1$

	private final Preferences node;
	private boolean dirty;

	public OptionsPreferences(final Preferences node) {
		this.node = node;
	}

	public void clearDirty() {
		dirty = false;
	}

	public String getConfig() {
		final String config = node.get(KEY_CONFIG, null);
		return config != null ? config : getOldConfig();
	}

	public Preferences getNode() {
		return node;
	}

	public boolean isDirty() {
		return dirty;
	}

	public boolean isProjectSpecific() {
		return node.getBoolean(KEY_PROJ_SPECIFIC, DEFAULT_PROJ_SPECIFIC);
	}

	public void setConfig(final String value) {
		if (!value.equals(node.get(KEY_CONFIG, null))) {
			node.put(KEY_CONFIG, value);
			dirty = true;
		}
	}

	public void setProjectSpecific(final boolean value) {
		if (value != node.getBoolean(KEY_PROJ_SPECIFIC,
				DEFAULT_PROJ_SPECIFIC)) {
			if (value == DEFAULT_PROJ_SPECIFIC) {
				node.remove(KEY_PROJ_SPECIFIC);
			} else {
				node.putBoolean(KEY_PROJ_SPECIFIC, value);
			}
			dirty = true;
		}
	}

	private String getOldConfig() {
		try {
			final String options = node.get(KEY_OPTIONS, ""); //$NON-NLS-1$
			final String globals = node.get(KEY_GLOBALS, ""); //$NON-NLS-1$
			final JsonObject obj = OptionParserUtils
					.createConfiguration(options, globals);
			return JsonUtils.prettyPrint(obj);
		} catch (final IOException e) {
			return ""; //$NON-NLS-1$
		}
	}

}
