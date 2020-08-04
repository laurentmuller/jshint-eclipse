package com.eclipsesource.jshint.ui.preferences.ui;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

import com.eclipsesource.jshint.ui.options.JSHintGroup;
import com.eclipsesource.jshint.ui.options.JSHintItem;
import com.eclipsesource.jshint.ui.options.JSHintOption;

/**
 * Tooltip support for {@link JSHintOption} and {@link JSHintGroup}.
 */
public class JSHintColumnViewerToolTipSupport
		extends ColumnViewerToolTipSupport {

	/*
	 * the tooltip width
	 */
	private static final int TOOLTIP_WIDTH = 350;

	/*
	 * the deprecated text offset
	 */
	private static final int INDENT = 20;

	/**
	 * Enable ToolTip support for the viewer by creating an instance from this
	 * class.
	 *
	 * @param viewer
	 *            the viewer the support is attached to
	 */
	public static final void enableFor(final ColumnViewer viewer) {
		new JSHintColumnViewerToolTipSupport(viewer, ToolTip.NO_RECREATE,
				false);
	}

	/*
	 * the Windows OS
	 */
	private final boolean isWindows;

	/**
	 * Creates a new instance of this class.
	 *
	 * @param viewer
	 *            the viewer the support is attached to
	 * @param style
	 *            the style passed to control tool tip behavior
	 * @param manualActivation
	 *            <code>true</code> if the activation is done manually
	 */
	protected JSHintColumnViewerToolTipSupport(final ColumnViewer viewer,
			final int style, final boolean manualActivation) {
		super(viewer, style, manualActivation);
		isWindows = SWT.getPlatform().equals("win32");
	}

	@Override
	protected Composite createViewerToolTipContentArea(final Event event,
			final ViewerCell cell, final Composite parent) {
		// get values
		final JSHintItem item = (JSHintItem) cell.getElement();
		final String description = getDescription(item);
		final String deprecated = getDeprecated(item);

		// create widgets
		final Composite container = createContainer(parent, event.display);
		createDescription(container, description);
		if (deprecated != null) {
			createTitle(container);
			createDeprecated(container, deprecated);
		}

		return container;
	}

	private Composite createContainer(final Composite parent,
			final Display display) {
		final Composite container = new Composite(parent, SWT.NONE);
		container.setBackgroundMode(SWT.INHERIT_DEFAULT);
		container.setLayout(new GridLayout());
		container.setBackground(
				display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		container.setForeground(
				display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));

		return container;
	}

	private Label createDeprecated(final Composite parent,
			final String deprecated) {
		final Label label = new Label(parent, SWT.WRAP);
		label.setText(deprecated);

		final GridData data = new GridData(GridData.FILL_BOTH);
		final int width = label.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		if (width > TOOLTIP_WIDTH - INDENT) {
			data.widthHint = TOOLTIP_WIDTH - INDENT;
		}
		data.verticalIndent = -5;
		data.horizontalIndent = INDENT;
		label.setLayoutData(data);

		return label;
	}

	private Label createDescription(final Composite parent,
			final String description) {
		final Label label = new Label(parent, SWT.WRAP);
		label.setText(description);

		final GridData data = new GridData(GridData.FILL_BOTH);
		final int width = label.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		if (width > TOOLTIP_WIDTH) {
			data.widthHint = TOOLTIP_WIDTH;
		}
		label.setLayoutData(data);

		return label;
	}

	private Label createTitle(final Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText("Deprecated:");

		final Font font = JFaceResources.getFontRegistry()
				.getBold(JFaceResources.DIALOG_FONT);
		label.setFont(font);

		final GridData data = new GridData();
		label.setLayoutData(data);

		return label;
	}

	private String getDeprecated(final JSHintItem item) {
		if (item instanceof JSHintOption) {
			final JSHintOption option = (JSHintOption) item;
			if (option.isDeprecated()) {
				return handleAmpersand(option.getDeprecated());
			}
		}
		return null;
	}

	private String getDescription(final JSHintItem item) {
		return handleAmpersand(item.getDescription());
	}

	private String handleAmpersand(final String text) {
		if (text != null && isWindows) {
			return text.replace("&", "&&");
		}
		return text;
	}
}