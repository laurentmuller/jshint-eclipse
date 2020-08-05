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
package com.eclipsesource.jshint.ui.util;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class LayoutUtils {

	public static FillLayoutConfig fillLayout(final Composite composite) {
		final FillLayoutConfig config = new FillLayoutConfig();
		composite.setLayout(config.getLayout());
		return config;
	}

	public static FormDataConfig formData(final Control control) {
		final FormDataConfig config = new FormDataConfig();
		control.setLayoutData(config.getLayoutData());
		return config;
	}

	public static FormLayoutConfig formLayout(final Composite composite) {
		final FormLayoutConfig config = new FormLayoutConfig();
		composite.setLayout(config.getLayout());
		return config;
	}

	public static GridDataConfig gridData(final Control control) {
		final GridDataConfig config = new GridDataConfig();
		control.setLayoutData(config.getLayoutData());
		return config;
	}

	public static GridLayoutConfig gridLayout(final Composite composite) {
		final GridLayoutConfig config = new GridLayoutConfig();
		composite.setLayout(config.getLayout());
		return config;
	}

	public static RowDataConfig rowData(final Control control) {
		final RowDataConfig config = new RowDataConfig();
		control.setLayoutData(config.getLayoutData());
		return config;
	}

	public static RowLayoutConfig rowLayout(final Composite composite) {
		final RowLayoutConfig config = new RowLayoutConfig();
		composite.setLayout(config.getLayout());
		return config;
	}

	/*
	 * prevent instance creation
	 */
	private LayoutUtils() {
		throw new AssertionError("No LayoutUtils instances is allowed"); //$NON-NLS-1$
	}
}
