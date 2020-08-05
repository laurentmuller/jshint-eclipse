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
package com.eclipsesource.jshint.ui.builder;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.eclipsesource.jshint.ui.Activator;

public class JSHintBuilder extends IncrementalProjectBuilder {

	public static final String ID = Activator.PLUGIN_ID + ".builder";

	@Override
	protected IProject[] build(final int kind, final Map<String, String> args,
			final IProgressMonitor monitor) throws CoreException {
		if (kind == IncrementalProjectBuilder.FULL_BUILD) {
			fullBuild(monitor);
		} else {
			final IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	@Override
	protected void clean(final IProgressMonitor monitor) throws CoreException {
		final IProject project = getProject();
		new MarkerAdapter(project).removeMarkers();
	}

	private void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		final IProject project = getProject();
		project.accept(new JSHintBuilderVisitor(project, monitor));
	}

	private void incrementalBuild(final IResourceDelta delta,
			final IProgressMonitor monitor) throws CoreException {
		final IProject project = getProject();
		delta.accept(new JSHintBuilderVisitor(project, monitor));
	}
}
