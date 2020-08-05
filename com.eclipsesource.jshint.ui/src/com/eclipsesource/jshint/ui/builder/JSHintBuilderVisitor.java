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
package com.eclipsesource.jshint.ui.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.service.prefs.Preferences;

import com.eclipsesource.jshint.JSHint;
import com.eclipsesource.jshint.Text;
import com.eclipsesource.jshint.ui.Activator;
import com.eclipsesource.jshint.ui.preferences.EnablementPreferences;
import com.eclipsesource.jshint.ui.preferences.JSHintPreferences;
import com.eclipsesource.jshint.ui.preferences.PreferencesFactoryUtils;
import com.eclipsesource.jshint.ui.preferences.ResourceSelector;

class JSHintBuilderVisitor implements IResourceVisitor, IResourceDeltaVisitor {

	private static void clean(final IResource resource) throws CoreException {
		new MarkerAdapter(resource).removeMarkers();
	}

	private static InputStream getCustomLib() throws FileNotFoundException {
		final JSHintPreferences globalPrefs = new JSHintPreferences();
		if (globalPrefs.isUseCustomLib()) {
			final File file = new File(globalPrefs.getCustomLibPath());
			if (file.exists()) {
				return new FileInputStream(file);
			}
		}
		return null;
	}

	private static Text readContent(final IFile file) throws CoreException {
		try (InputStream stream = file.getContents()) {
			final String charset = file.getCharset();
			return readContent(stream, charset);

		} catch (final IOException e) {
			final String msg = "Failed to read resource";
			throw Activator.createException(msg, e);
		}
	}

	private static Text readContent(final InputStream stream,
			final String charset) throws IOException {
		try (Reader reader = new InputStreamReader(stream, charset)) {
			return new Text(reader);
		}
	}

	private final JSHint checker;

	private final ResourceSelector selector;

	private final IProgressMonitor monitor;

	public JSHintBuilderVisitor(final IProject project,
			final IProgressMonitor monitor) throws CoreException {
		final Preferences node = PreferencesFactoryUtils
				.getProjectPreferences(project);
		new EnablementPreferences(node);
		selector = new ResourceSelector(project);
		checker = selector.allowVisitProject() ? createJSHint(project) : null;
		this.monitor = monitor;
	}

	@Override
	public boolean visit(final IResource resource) throws CoreException {
		boolean descend = false;
		if (resource.exists() && selector.allowVisitProject()
				&& !monitor.isCanceled()) {
			if (resource.getType() != IResource.FILE) {
				descend = selector.allowVisitFolder(resource);
			} else {
				clean(resource);
				if (selector.allowVisitFile(resource)) {
					check((IFile) resource);
				}
				descend = true;
			}
		}
		return descend;
	}

	@Override
	public boolean visit(final IResourceDelta delta) throws CoreException {
		final IResource resource = delta.getResource();
		return visit(resource);
	}

	private void check(final IFile file) throws CoreException {
		final Text code = readContent(file);
		final MarkerAdapter adapter = new MarkerAdapter(file);
		final MarkerHandler handler = new MarkerHandler(adapter, code);

		try {
			checker.check(code, handler);

		} catch (final RuntimeException e) {
			final String path = file.getFullPath().toPortableString();
			final String msg = String.format("Failed checking file '%s'.",
					path);
			throw Activator.createException(msg, e);
		}
	}

	private JSHint createJSHint(final IProject project) throws CoreException {
		final JSHint jshint = new JSHint();
		try (final InputStream stream = getCustomLib()) {
			if (stream != null) {
				jshint.load(stream);
			} else {
				jshint.load();
			}
			final ConfigurationLoader loader = new ConfigurationLoader(project);
			jshint.configure(loader.getConfiguration());

		} catch (final IOException e) {
			final String msg = "Failed to intialize JSHint.";
			throw Activator.createException(msg, e);
		}
		return jshint;
	}
}
