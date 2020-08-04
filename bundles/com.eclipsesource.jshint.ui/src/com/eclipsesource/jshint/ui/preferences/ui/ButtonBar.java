/*******************************************************************************
 * Copyright (c) 2013 EclipseSource.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ralf Sternberg - initial implementation and API
 ******************************************************************************/
package com.eclipsesource.jshint.ui.preferences.ui;

import static com.eclipsesource.jshint.ui.util.LayoutUtil.gridLayout;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

public class ButtonBar extends Composite {

	private FontMetrics fontMetrics;

	public ButtonBar(final Composite parent, final int style) {
		super(parent, style);
		gridLayout(this).spacing(5);
	}

	public Button addButton(final String text, final Listener listener) {
		final Button button = new Button(this, SWT.PUSH);
		if (text != null) {
			button.setText(text);
		}
		layoutData(button);

		button.addListener(SWT.Selection, listener);
		return button;
	}

	private FontMetrics getFontMetrics() {
		if (fontMetrics == null) {
			final GC gc = new GC(this);
			gc.setFont(JFaceResources.getDialogFont());
			fontMetrics = gc.getFontMetrics();
			gc.dispose();
		}
		return fontMetrics;
	}

	private void layoutData(final Button button) {
		final int defaultWidth = Dialog.convertHorizontalDLUsToPixels(
				getFontMetrics(), IDialogConstants.BUTTON_WIDTH);
		final int minWidth = button.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		final GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.widthHint = Math.max(defaultWidth, minWidth);
		button.setLayoutData(data);
	}
}
