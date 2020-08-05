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

import static com.eclipsesource.jshint.ui.util.IOUtils.readUtf8File;
import static com.eclipsesource.jshint.ui.util.IOUtils.writeUtf8File;
import static com.eclipsesource.jshint.ui.util.LayoutUtils.gridData;
import static com.eclipsesource.jshint.ui.util.LayoutUtils.gridLayout;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;

import com.eclipsesource.jshint.JSHint;
import com.eclipsesource.jshint.ui.Activator;
import com.eclipsesource.jshint.ui.builder.BuilderUtils;
import com.eclipsesource.jshint.ui.builder.JSHintBuilder;
import com.eclipsesource.jshint.ui.options.JSHintOption;
import com.eclipsesource.jshint.ui.preferences.OptionsPreferences;
import com.eclipsesource.jshint.ui.util.JsonUtils;

public class ConfigurationPropertyPage extends AbstractPropertyPage {

	private static boolean checkExists(final IFile file) {
		if (file.exists()) {
			return true;
		}
		try {
			file.refreshLocal(IResource.DEPTH_ZERO, null);
		} catch (final CoreException e) {
			Activator.handleError(e.getLocalizedMessage(), e);
		}
		return file.exists();
	}

	private String originalConfiguration;

	private Button addButton;
	private Button importButton;
	private Button exportButton;
	private Button formatButton;
	private ConfigurationEditor editor;
	private Button chkProject;

	@Override
	public boolean performOk() {
		try {
			final boolean prefsChanged = storePrefs();
			final boolean configChanged = chkProject.getSelection()
					&& storeConfig();
			if (prefsChanged || configChanged) {
				triggerRebuild();
			}
		} catch (final CoreException e) {
			Activator.handleError(e.getStatus());
			return false;
		}
		return true;
	}

	@Override
	protected Control createContents(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		final Control projectSpecificPart = createProjectSpecificPart(
				composite);
		final Control labelPart = createLabelPart(composite);
		final Control configTextPart = createConfigTextPart(composite);
		final Control buttonsPart = createButtonsPart(composite);
		gridData(composite).fillBoth();
		gridLayout(composite).columns(2).spacing(3);
		gridData(projectSpecificPart);
		gridData(labelPart).span(2, 1).fillHorizontal().widthHint(360);
		gridData(configTextPart).fillBoth().sizeHint(360, 180);
		gridData(buttonsPart).align(SWT.BEGINNING, SWT.BEGINNING);
		loadPreferences();
		return composite;
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		chkProject.setSelection(OptionsPreferences.DEFAULT_PROJ_SPECIFIC);
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
		addButton = buttonBar.addButton("&Add..", new Listener() {
			@Override
			public void handleEvent(final Event event) {
				addOption();
			}
		});
		formatButton = buttonBar.addButton("Format&.", new Listener() {
			@Override
			public void handleEvent(final Event event) {
				editor.formatContent();
			}

		});
		new Label(buttonBar, SWT.LEFT);
		importButton = buttonBar.addButton("I&mport...", new Listener() {
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
				if (chkProject.getSelection()) {
					final boolean enabled = message == null;
					setErrorMessage(message);
					setValid(enabled);
					addButton.setEnabled(enabled);
					formatButton.setEnabled(enabled);
					exportButton.setEnabled(enabled);
					importButton.setEnabled(true);
				} else {
					addButton.setEnabled(false);
					formatButton.setEnabled(false);
					exportButton.setEnabled(false);
					importButton.setEnabled(false);
				}
			}
		};
		return editor;
	}

	private Control createLabelPart(final Composite parent) {
		final Link link = new Link(parent, SWT.WRAP);
		link.setText(
				"The project specific configuration will be read from a file named .jshintrc"
						+ " in the project root. For the syntax of this file, see "
						+ "<a>" + JSHint.DOC_URL + "</a>.");
		BrowserSupport.INSTANCE.enableHyperlinks(link);
		return link;
	}

	private Control createProjectSpecificPart(final Composite parent) {
		chkProject = new Button(parent, SWT.CHECK);
		chkProject.setText("Enable project specific configuration");
		chkProject.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				prefsChanged();
			}
		});
		return chkProject;
	}

	private IFile getConfigFile() {
		return getResource().getProject().getFile(".jshintrc");
	}

	private String getDefaultConfig() {
		return new OptionsPreferences(getPreferences()).getConfig();
	}

	private void loadPreferences() {
		final OptionsPreferences prefs = new OptionsPreferences(
				getPreferences());
		chkProject.setSelection(prefs.getProjectSpecific());
		originalConfiguration = readConfigFile();
		editor.setText(originalConfiguration != null ? originalConfiguration
				: getDefaultConfig());
		editor.setEnabled(prefs.getProjectSpecific());
	}

	private void prefsChanged() {
		final boolean isProject = chkProject.getSelection();
		editor.setEnabled(isProject);
		if (isProject) {
			editor.validate();
		} else {
			importButton.setEnabled(false);
			setErrorMessage(null);
			setValid(true);
		}
	}

	private String readConfigFile() {
		final IFile configFile = getConfigFile();
		if (checkExists(configFile)) {
			try {
				return readUtf8File(configFile);
			} catch (final CoreException e) {
				final IStatus status = e.getStatus();
				setErrorMessage(status.getMessage());
				Activator.handleError(status);
			}
		}
		return null;
	}

	private boolean storeConfig() throws CoreException {
		final String content = editor.getText();
		final boolean changed = !JsonUtils.jsonEquals(content,
				originalConfiguration);
		writeUtf8File(getConfigFile(), content);
		originalConfiguration = content;
		return changed;
	}

	private boolean storePrefs() throws CoreException {
		final OptionsPreferences prefs = new OptionsPreferences(
				getPreferences());
		prefs.setProjectSpecific(chkProject.getSelection());
		final boolean changed = prefs.hasChanged();
		if (changed) {
			savePreferences();
		}
		return changed;
	}

	private void triggerRebuild() throws CoreException {
		final IProject project = getResource().getProject();
		BuilderUtils.triggerClean(project, JSHintBuilder.ID);
	}
}