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
package com.eclipsesource.jshint.ui.preferences.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.eclipsesource.jshint.ui.Activator;
import com.eclipsesource.jshint.ui.preferences.PreferencesFactoryUtils;

public abstract class AbstractPropertyPage extends PropertyPage {

	protected Preferences getPreferences() {
		final IProject project = getResource().getProject();
		return PreferencesFactoryUtils.getProjectPreferences(project);
	}

	protected IResource getResource() {
		final IAdaptable element = getElement();
		if (element instanceof IResource) {
			return (IResource) element;
		}
		return (IResource) element.getAdapter(IResource.class);
	}

	protected void savePreferences() throws CoreException {
		try {
			getPreferences().flush();
		} catch (final BackingStoreException e) {
			final String msg = "Failed to store preferences.";
			throw Activator.createException(msg, e);
		}
	}
}
