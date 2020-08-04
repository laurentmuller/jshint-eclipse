/*******************************************************************************
 * Copyright (c) 2018 EclipseSource.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Laurent Muller - initial implementation and API
 ******************************************************************************/
package com.eclipsesource.jshint.ui.help;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.eclipsesource.jshint.ui.builder.MarkerAdapter;

/**
 * Class to display help for a JSHint marker.
 */
public class MarkerHelpHandler extends AbstractHandler {

	// http://linterrors.com/api/
	// http://api.linterrors.com/linters?linter=jshint
	// http://api.linterrors.com/explain?m=unexpected-dangling-_-in-a&f=html
	// http://api.linterrors.com/explain?m=unnecessary-semicolon&f=html

	@Override
	public Object execute(final ExecutionEvent event)
			throws ExecutionException {
		final ISelection selection = HandlerUtil.getActiveMenuSelection(event);
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}
		final IStructuredSelection ss = (IStructuredSelection) selection;
		if (!(ss.getFirstElement() instanceof IMarker)) {
			return null;
		}
		final IMarker marker = (IMarker) ss.getFirstElement();
		try {
			if (marker.getType() != MarkerAdapter.TYPE_PROBLEM) {
				return null;
			}
		} catch (final CoreException e) {
			throw new ExecutionException("Unable to get marker type.", e);
		}

		final String msg = marker.getAttribute(IMarker.MESSAGE, "");
		if (msg == null || msg.isEmpty()) {
			return null;
		}

		// final org.eclipse.core.expressions.PropertyTester teset;

		// MarkerAdapter.TYPE_PROBLEM problemmarker
		// TYPE_PROBLEM
		// getType()
		return null;
	}

}
