/*******************************************************************************
 * Copyright (c) 2012 EclipseSource.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ralf Sternberg - initial implementation and API
 ******************************************************************************/
package com.eclipsesource.jshint.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.Preferences;

import com.eclipsesource.jshint.ui.Activator;

public class PreferencesFactoryUtils {

	public static OptionsPreferences getProjectOptionsPreferences(
			final IProject project) {
		final Preferences node = getProjectPreferences(project);
		return new OptionsPreferences(node);
	}

	public static Preferences getProjectPreferences(final IProject project) {
		return new ProjectScope(project).getNode(Activator.PLUGIN_ID);
	}

	public static OptionsPreferences getWorkspaceOptionsPreferences() {
		final Preferences node = getWorkspacePreferences();
		return new OptionsPreferences(node);
	}

	public static Preferences getWorkspacePreferences() {
		return InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
	}

	/*
	 * prevent instance creation
	 */
	private PreferencesFactoryUtils() {
		throw new AssertionError(
				"No PreferencesFactoryUtils instances is allowed"); //$NON-NLS-1$
	}
}
