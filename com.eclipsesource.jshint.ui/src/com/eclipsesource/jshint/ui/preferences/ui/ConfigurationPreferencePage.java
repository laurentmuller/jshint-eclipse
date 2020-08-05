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

import static com.eclipsesource.jshint.ui.util.JsonUtils.jsonEquals;
import static com.eclipsesource.jshint.ui.util.LayoutUtils.gridData;
import static com.eclipsesource.jshint.ui.util.LayoutUtils.gridLayout;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.eclipsesource.jshint.JSHint;
import com.eclipsesource.jshint.ui.Activator;
import com.eclipsesource.jshint.ui.builder.BuilderUtils;
import com.eclipsesource.jshint.ui.builder.JSHintBuilder;
import com.eclipsesource.jshint.ui.options.JSHintOption;
import com.eclipsesource.jshint.ui.preferences.OptionsPreferences;
import com.eclipsesource.jshint.ui.preferences.PreferencesFactoryUtils;

public class ConfigurationPreferencePage extends PreferencePage
		implements IWorkbenchPreferencePage {

	private String originalConfiguration;

	private Button addButton;
	private Button exportButton;
	private Button formatButton;
	private ConfigurationEditor editor;

	public ConfigurationPreferencePage() {
		setDescription("Global JSHint configuration");
	}

	@Override
	public void init(final IWorkbench workbench) {
	}

	@Override
	public boolean performOk() {
		try {
			storePreferences();
			if (!jsonEquals(editor.getText(), originalConfiguration)) {
				triggerRebuild();
			}
		} catch (final CoreException e) {
			final String message = "Failed to store settings";
			Activator.handleError(message, e);
			return false;
		}
		return true;
	}

	@Override
	protected Control createContents(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		final Control labelPart = createLabelPart(composite);
		final Control configTextPart = createConfigTextPart(composite);
		final Control buttonsPart = createButtonsPart(composite);
		gridData(composite).fillBoth();
		gridLayout(composite).columns(2).spacing(3);
		gridData(labelPart).span(2, 1).fillHorizontal().widthHint(360);
		gridData(configTextPart).fillBoth().sizeHint(360, 180);
		gridData(buttonsPart).align(SWT.BEGINNING, SWT.BEGINNING);
		loadPreferences();
		return composite;
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		editor.setText(OptionsPreferences.DEFAULT_CONFIG);
	}

	private void addOption() {
		final OptionSelectionDialog dlg = new OptionSelectionDialog(getShell());
		if (dlg.open() == Window.OK) {
			final JSHintOption option = dlg.getFirstResult();
			if (option != null) {
				editor.addOption(option);
			}
		}
	}

	private Control createButtonsPart(final Composite parent) {
		final ButtonBar buttonBar = new ButtonBar(parent, SWT.NONE);
		addButton = buttonBar.addButton("&Add...", new Listener() {
			@Override
			public void handleEvent(final Event event) {
				addOption();
			}
		});
		formatButton = buttonBar.addButton("Format&", new Listener() {
			@Override
			public void handleEvent(final Event event) {
				editor.formatContent();
			}

		});
		new Label(buttonBar, SWT.LEFT);
		buttonBar.addButton("I&mport...", new Listener() {
			@Override
			public void handleEvent(final Event event) {
				editor.importConfig();
			}
		});
		exportButton = buttonBar.addButton("E&xport...", new Listener() {
			@Override
			public void handleEvent(final Event event) {
				editor.exportConfig();
			}
		});

		return buttonBar;
	}

	private Control createConfigTextPart(final Composite parent) {
		editor = new ConfigurationEditor(parent) {
			@Override
			public void handleError(final String message) {
				final boolean enabled = message == null;
				setErrorMessage(message);
				setValid(enabled);
				addButton.setEnabled(enabled);
				formatButton.setEnabled(enabled);
				exportButton.setEnabled(enabled);
			}
		};
		return editor;
	}

	private Control createLabelPart(final Composite parent) {
		final Link link = new Link(parent, SWT.WRAP);
		link.setText("For syntax, see <a>" + JSHint.DOC_URL + "</a>.");
		BrowserSupport.INSTANCE.enableHyperlinks(link);
		return link;
	}

	private void loadPreferences() {
		final OptionsPreferences optionsPreferences = new OptionsPreferences(
				getPreferences());
		originalConfiguration = optionsPreferences.getConfig();
		editor.setText(originalConfiguration);
	}

	private void savePreferences() throws CoreException {
		final Preferences node = getPreferences();
		try {
			node.flush();
		} catch (final BackingStoreException e) {
			final String message = "Failed to store preferences";
			final Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					message, e);
			throw new CoreException(status);
		}
	}

	private void storePreferences() throws CoreException {
		final OptionsPreferences optionsPreferences = new OptionsPreferences(
				getPreferences());
		optionsPreferences.setConfig(editor.getText());
		if (optionsPreferences.hasChanged()) {
			savePreferences();
		}
	}

	private void triggerRebuild() throws CoreException {
		for (final IProject project : getProjects()) {
			if (project.isAccessible()) {
				BuilderUtils.triggerClean(project, JSHintBuilder.ID);
			}
		}
	}

	Preferences getPreferences() {
		return PreferencesFactoryUtils.getWorkspacePreferences();
	}

	IProject[] getProjects() {
		return ResourcesPlugin.getWorkspace().getRoot().getProjects();
	}
}
