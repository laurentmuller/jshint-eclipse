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

import static org.eclipse.ui.model.WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchViewerComparator;

import com.eclipsesource.jshint.ui.Activator;

/**
 * Dialog to select a container resource.
 */
public class ContainerSelectionDialog extends ElementTreeSelectionDialog {

	private static final class ContainerTreeProvider
			extends TreeContentProvider {

		@Override
		public Object[] getElements(final Object element) {
			if (element instanceof IContainer) {
				final IContainer container = (IContainer) element;
				try {
					final IResource[] members = container.members(false);
					final List<IResource> list = new ArrayList<>(
							members.length);
					for (final IResource resource : members) {
						if (isContainer(resource)) {
							list.add(resource);
						}
					}
					return list.toArray();

				} catch (final CoreException e) {
				}
			}

			return new Object[0];
		}

		@Override
		public Object getParent(final Object element) {
			if (element instanceof IResource) {
				return ((IResource) element).getParent();
			}
			return null;
		}

		@Override
		public boolean hasChildren(final Object element) {
			if (element instanceof IContainer) {
				return getElements(element).length > 0;
			}
			return false;
		}

		private boolean isContainer(final IResource resource) {
			if (!resource.exists() || resource.isHidden()) {
				return false;
			}
			if (!(resource instanceof IContainer)) {
				return false;
			}
			if (resource.getName().charAt(0) == '.') {
				return false;
			}
			return true;
		}
	};

	private static final class FolderValidator
			implements ISelectionStatusValidator {
		@Override
		public IStatus validate(final Object[] selection) {
			if (selection == null || selection.length == 0
					|| !(selection[0] instanceof IContainer)) {
				return Activator.createError("A folder must be selected.",
						null);
			}
			return new Status(IStatus.OK, Activator.PLUGIN_ID, "");
		}
	}

	public ContainerSelectionDialog(final Shell parent,
			final IProject project) {
		super(parent, getDecoratingWorkbenchLabelProvider(),
				new ContainerTreeProvider());

		setTitle("Folder selection");
		setMessage("&Select a folder:");
		setHelpAvailable(false);
		setAllowMultiple(false);

		setComparator(new WorkbenchViewerComparator());
		setValidator(new FolderValidator());
		setInput(project);
	}

	@Override
	public IContainer getFirstResult() {
		return (IContainer) super.getFirstResult();
	}
}
