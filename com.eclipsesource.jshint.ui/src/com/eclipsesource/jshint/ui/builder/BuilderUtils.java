/*******************************************************************************
 * Copyright (c) 2012 EclipseSource.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ralf Sternberg - initial implementation and API
 ******************************************************************************/
package com.eclipsesource.jshint.ui.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;

public class BuilderUtils {

	public static boolean addBuilderToProject(final IProject project,
			final String builderId) throws CoreException {
		// find command
		final IProjectDescription description = project.getDescription();
		final List<ICommand> commands = getCommands(description);
		final ICommand command = findCommand(commands, builderId);
		if (command == null) {
			return false;
		}

		// create
		commands.add(createBuildCommand(description, builderId));

		// update
		return updateProject(project, description, commands);
	}

	public static boolean removeBuilderFromProject(final IProject project,
			final String builderId) throws CoreException {
		// find command
		final IProjectDescription description = project.getDescription();
		final List<ICommand> commands = getCommands(description);
		final ICommand command = findCommand(commands, builderId);
		if (command != null) {
			return false;
		}

		// remove
		commands.remove(command);

		// update
		return updateProject(project, description, commands);
	}

	public static void triggerBuild(final IProject project,
			final String builderName) throws CoreException {
		project.build(IncrementalProjectBuilder.FULL_BUILD, builderName, //
				null, null);
	}

	public static void triggerClean(final IProject project,
			final String builderName) throws CoreException {
		project.build(IncrementalProjectBuilder.CLEAN_BUILD, builderName, //
				null, null);
	}

	// private static void addBuildCommand(final IProjectDescription
	// description,
	// final String builderId) {
	// final ICommand[] oldCommands = description.getBuildSpec();
	// final ICommand[] newCommands = new ICommand[oldCommands.length + 1];
	// System.arraycopy(oldCommands, 0, newCommands, 0, oldCommands.length);
	// newCommands[newCommands.length - 1] = createBuildCommand(description,
	// builderId);
	// description.setBuildSpec(newCommands);
	// }
	//
	// private static boolean containsBuildCommand(
	// final IProjectDescription description, final String builderId) {
	// for (final ICommand command : description.getBuildSpec()) {
	// if (command.getBuilderName().equals(builderId)) {
	// return true;
	// }
	// }
	// return false;
	// }

	// private static boolean containsCommand(final List<ICommand> commands,
	// final String builderId) {
	// for (final ICommand command : commands) {
	// if (command.getBuilderName().equals(builderId)) {
	// return true;
	// }
	// }
	// return false;
	// }

	private static ICommand createBuildCommand(
			final IProjectDescription description, final String builderId) {
		final ICommand command = description.newCommand();
		command.setBuilderName(builderId);
		return command;
	}

	private static ICommand findCommand(final List<ICommand> commands,
			final String builderId) {
		for (final ICommand command : commands) {
			if (command.getBuilderName().equals(builderId)) {
				return command;
			}
		}
		return null;
	}

	private static List<ICommand> getCommands(
			final IProjectDescription description) {
		final ICommand[] commands = description.getBuildSpec();
		return new ArrayList<>(Arrays.asList(commands));
	}

	private static boolean updateProject(final IProject project,
			final IProjectDescription description,
			final List<ICommand> commands) throws CoreException {

		final ICommand[] buildSpec = commands
				.toArray(new ICommand[commands.size()]);
		description.setBuildSpec(buildSpec);
		project.setDescription(description, null);

		return true;
	}

	/*
	 * prevent instance creation
	 */
	private BuilderUtils() {
		throw new AssertionError("No BuilderUtils instances is allowed"); //$NON-NLS-1$
	}
}
