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

import static com.eclipsesource.jshint.ui.util.IOUtil.readUtf8File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.osgi.service.prefs.Preferences;

import com.eclipsesource.jshint.ui.Activator;
import com.eclipsesource.jshint.ui.preferences.OptionsPreferences;
import com.eclipsesource.jshint.ui.preferences.PreferencesFactory;
import com.eclipsesource.jshint.ui.util.JsonUtil;
import com.eclipsesource.json.JsonObject;

public class ConfigurationLoader {

	private static JsonObject getWorkspaceConfig() {
		try {
			final String json = getWorkspaceConfigJson();
			return JsonUtil.readFrom(json);

		} catch (final Exception e) {
			final String msg = "Failed to read jshint configuration from workspace preferences.";
			Activator.handleError(msg, e);
			return new JsonObject();
		}
	}

	private static String getWorkspaceConfigJson() {
		final Preferences workspaceNode = PreferencesFactory
				.getWorkspacePreferences();
		return new OptionsPreferences(workspaceNode).getConfig();
	}

	private final IProject project;

	public ConfigurationLoader(final IProject project) {
		this.project = project;
	}

	public JsonObject getConfiguration() {
		final Preferences projectNode = PreferencesFactory
				.getProjectPreferences(project);
		final OptionsPreferences projectPreferences = new OptionsPreferences(
				projectNode);
		if (projectPreferences.getProjectSpecific()) {
			return getProjectConfig(projectPreferences);
		}
		return getWorkspaceConfig();
	}

	private JsonObject getProjectConfig(final OptionsPreferences projectPrefs) {
		try {
			final String json = getProjectConfigJson(projectPrefs);
			return JsonUtil.readFrom(json);

		} catch (final Exception e) {
			final String msg = String.format(
					"Failed to read jshint configuration for project '%s'.",
					project.getName());
			Activator.handleError(msg, e);
			return new JsonObject();
		}
	}

	private IFile getProjectConfigFile() {
		return project.getFile(".jshintrc");
	}

	private String getProjectConfigJson(final OptionsPreferences projectPrefs)
			throws CoreException {
		final IFile configFile = getProjectConfigFile();
		if (!configFile.exists()) {
			// compatibility
			return projectPrefs.getConfig();
		}
		return readUtf8File(configFile);
	}

}