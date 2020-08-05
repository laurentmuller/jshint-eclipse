/*******************************************************************************
 * Copyright (c) 2012, 2013 EclipseSource.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ralf Sternberg - initial implementation and API
 ******************************************************************************/
package com.eclipsesource.jshint.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	/**
	 * The plugin identifier
	 */
	public static final String PLUGIN_ID = "com.eclipsesource.jshint.ui"; //$NON-NLS-1$

	private static Activator instance;

	public static IStatus createError(final String message,
			final Throwable exception) {
		return new Status(IStatus.ERROR, PLUGIN_ID, message, exception);
	}

	public static CoreException createException(final String message,
			final Throwable exception) {
		final IStatus status = createError(message, exception);
		return new CoreException(status);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return instance;
	}

	public static void handleError(final IStatus status) {
		if (status != null) {
			StatusManager.getManager().handle(status);
		}
	}

	public static void handleError(final String message,
			final Throwable exception) {
		final IStatus status = createError(message, exception);
		handleError(status);
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		instance = this;
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		instance = null;
		super.stop(context);
	}

}
