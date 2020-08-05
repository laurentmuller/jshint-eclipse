/*******************************************************************************
 * Copyright (c) 2013, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ralf Sternberg - initial implementation and API
 ******************************************************************************/
package com.eclipsesource.jshint.ui.builder;

import org.eclipse.core.resources.IProject;

import com.eclipsesource.jshint.ui.Activator;
import com.eclipsesource.jshint.ui.preferences.OptionsPreferences;
import com.eclipsesource.jshint.ui.preferences.PreferencesFactoryUtils;
import com.eclipsesource.jshint.ui.util.JsonUtils;
import com.eclipsesource.json.JsonObject;

public class ConfigurationLoader {

	private final IProject project;

	public ConfigurationLoader(final IProject project) {
		this.project = project;
	}

	public JsonObject getConfiguration() {
		final OptionsPreferences preferences = PreferencesFactoryUtils
				.getProjectOptionsPreferences(project);
		if (preferences.isProjectSpecific()) {
			return getProjectConfig(preferences);
		}
		return getWorkspaceConfig();
	}

	private JsonObject getProjectConfig(final OptionsPreferences projectPrefs) {
		try {
			final String json = projectPrefs.getConfig();
			return JsonUtils.readFrom(json);

		} catch (final Exception e) {
			final String msg = String.format(
					"Failed to read jshint configuration for project '%s'.",
					project.getName());
			Activator.handleError(msg, e);
			return new JsonObject();
		}
	}

	private JsonObject getWorkspaceConfig() {
		try {
			final OptionsPreferences preferences = PreferencesFactoryUtils
					.getWorkspaceOptionsPreferences();
			final String json = preferences.getConfig();
			return JsonUtils.readFrom(json);

		} catch (final Exception e) {
			final String msg = "Failed to read jshint configuration from workspace preferences.";
			Activator.handleError(msg, e);
			return new JsonObject();
		}
	}
}