/*******************************************************************************
 * Copyright (c) 2017 EclipseSource.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Laurent Muller - initial implementation and API
 ******************************************************************************/
package com.eclipsesource.jshint.ui.preferences.ui;

import java.text.Collator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.dialogs.PatternFilter;

import com.eclipsesource.jshint.ui.Activator;
import com.eclipsesource.jshint.ui.options.JSHintGroup;
import com.eclipsesource.jshint.ui.options.JSHintItem;
import com.eclipsesource.jshint.ui.options.JSHintOption;
import com.eclipsesource.jshint.ui.options.JSHintRoot;
import com.eclipsesource.jshint.ui.util.LayoutUtil;;

/**
 * Dialog to select a JSHintOption.
 */
public class OptionSelectionDialog extends ElementTreeSelectionDialog {

	private static final class OptionLabelProvider extends ColumnLabelProvider {

		private Font font;
		private Color color;
		private final ISharedImages images;

		public OptionLabelProvider() {
			images = PlatformUI.getWorkbench().getSharedImages();
		}

		@Override
		public Font getFont(final Object element) {
			if (element instanceof JSHintGroup) {
				return font;
			}
			return super.getFont(element);
		}

		@Override
		public Color getForeground(final Object element) {
			if (element instanceof JSHintOption) {
				final JSHintOption option = (JSHintOption) element;
				if (option.isDeprecated()) {
					return color;
				}
			}
			return super.getForeground(element);
		}

		@Override
		public Image getImage(final Object element) {
			if (element instanceof JSHintGroup) {
				return images.getImage(ISharedImages.IMG_OBJ_FOLDER);
			}
			return images.getImage(ISharedImages.IMG_OBJ_FILE);
		}

		@Override
		public String getText(final Object element) {
			if (element instanceof JSHintItem) {
				return ((JSHintItem) element).getName();
			} else {
				return super.getText(element);
			}
		}

		@Override
		public String getToolTipText(final Object element) {
			if (element instanceof JSHintItem) {
				return ((JSHintItem) element).getDescription();
			}
			return null;
		}

		@Override
		protected void initialize(final ColumnViewer viewer,
				final ViewerColumn column) {
			super.initialize(viewer, column);
			if (font == null) {
				font = JFaceResources.getFontRegistry()
						.getBold(JFaceResources.DIALOG_FONT);
			}
			if (color == null) {
				color = JFaceResources.getColorRegistry()
						.get(JFacePreferences.QUALIFIER_COLOR);
			}
		}
	}

	private static final class OptionTreeProvider extends TreeContentProvider {

		private static final Object[] EMPTY_ARRAY = {};

		@Override
		public Object[] getElements(final Object element) {
			if (element instanceof JSHintRoot) {
				return ((JSHintRoot) element).toArray();
			} else if (element instanceof JSHintGroup) {
				return ((JSHintGroup) element).toArray();
			} else {
				return EMPTY_ARRAY;
			}
		}

		@Override
		public Object getParent(final Object element) {
			if (element instanceof JSHintGroup) {
				return ((JSHintGroup) element).getParent();
			} else if (element instanceof JSHintOption) {
				return ((JSHintOption) element).getParent();
			} else {
				return null;
			}
		}

		@Override
		public boolean hasChildren(final Object element) {
			if (element instanceof JSHintRoot) {
				return !((JSHintRoot) element).isEmpty();
			} else if (element instanceof JSHintGroup) {
				return !((JSHintGroup) element).isEmpty();
			} else {
				return false;
			}
		}
	};

	private final class OptionValidator implements ISelectionStatusValidator {
		@Override
		public IStatus validate(final Object[] selection) {
			final JSHintOption option = getOption(selection);
			if (option == null) {
				return Activator.createError("An option must be selected.",
						null);
			}
			return new Status(IStatus.OK, Activator.PLUGIN_ID, "");
		}
	}

	private Button docButton;

	public OptionSelectionDialog(final Shell parent) {
		super(parent, new OptionLabelProvider(), new OptionTreeProvider());

		setTitle("JSHINT Options");
		setMessage("&Select an option:");
		setHelpAvailable(false);
		setAllowMultiple(false);

		setValidator(new OptionValidator());
		setComparator(new ViewerComparator(Collator.getInstance()));
		setInput(JSHintRoot.getInstance());
	}

	@Override
	public JSHintOption getFirstResult() {
		final Object result = super.getFirstResult();
		return result instanceof JSHintOption ? (JSHintOption) result : null;
	}

	@Override
	protected TreeViewer createTreeViewer(final Composite parent) {
		// container
		final Composite container = new Composite(parent, SWT.NONE);
		LayoutUtil.gridLayout(container).columns(2).spacing(5);
		LayoutUtil.gridData(container).fillBoth();

		// create viewer and buttons
		final TreeViewer viewer = super.createTreeViewer(container);
		createViewerButton(container);

		return viewer;
	}

	@Override
	protected TreeViewer doCreateTreeViewer(final Composite parent,
			final int style) {
		// create filtered tree
		final FilteredTree tree = new FilteredTree(parent, style,
				new PatternFilter(), true, true);

		// initialize viewer
		final TreeViewer viewer = tree.getViewer();
		JSHintColumnViewerToolTipSupport.enableFor(viewer);
		viewer.setAutoExpandLevel(2);
		return viewer;
	}

	@Override
	protected void updateButtonsEnableState(final IStatus status) {
		super.updateButtonsEnableState(status);
		final boolean enabled = !status.matches(IStatus.ERROR);
		if (docButton != null) {
			docButton.setEnabled(enabled);
		}
	}

	private void createViewerButton(final Composite parent) {
		final ButtonBar bar = new ButtonBar(parent, SWT.NONE);
		bar.addButton("&Expand All", new Listener() {
			@Override
			public void handleEvent(final Event event) {
				getTreeViewer().expandAll();
			}
		});
		bar.addButton("&Collapse All", new Listener() {
			@Override
			public void handleEvent(final Event event) {
				getTreeViewer().collapseAll();
			}
		});
		docButton = bar.addButton("&Help", new Listener() {
			@Override
			public void handleEvent(final Event event) {
				displayDocumentation();
			}
		});
		docButton.setEnabled(false);
		LayoutUtil.gridData(bar).align(SWT.DEFAULT, SWT.TOP);
	}

	private void displayDocumentation() {
		final IStructuredSelection selection = (IStructuredSelection) getTreeViewer()
				.getSelection();
		final JSHintOption option = getOption(selection.toArray());
		if (option != null) {
			final String name = option.getName();
			final String url = "https://jshint.com/docs/options/#" + name;
			BrowserSupport.INSTANCE.openUrl(url);
		}
	}

	private JSHintOption getOption(final Object[] selection) {
		if (selection != null && selection.length > 0
				&& selection[0] instanceof JSHintOption) {
			return (JSHintOption) selection[0];
		}
		return null;
	}
}
