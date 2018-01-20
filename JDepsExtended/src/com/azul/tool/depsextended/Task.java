package com.azul.tool.depsextended;

import java.util.Set;

/**
 * New tasks need to implement this interface. The simplest way to do so is to extend AbstractTask.
 * 
 * A Task classname must start with the word "Task". The remainder will be set automagically as the tasks name. 
 * Register the task in the "DependencyAnalser.tasks()
 * Inplement getJobs() - return a list of job configuration this task participates. "default" is the name of the standard job to run if "-job" is not specified.
 * canRun() should check this tasks dependencies.
 * 
 */
public interface Task {
	void run(DependencyAnalyser ctx) throws Exception;
	boolean canRun(DependencyAnalyser ctx);
	
	/*
	 * Optionally return of description of the Task.
	 */
	String getDescription();
	
	/*
	 * Optionally return of description of options used by this Task
	 */
	Option[] getOptions();
	
	/*
	 * Array of default jobs this Task is part of. Cannot be null.
	 */
	String[] getJobs();
	
	/*
	 * Array of Tasks this task depends on. May not be null.
	 * Will be resolved recursively. Will not check for circular dependencies.
	 */
	String[] dependsOnTasks();
	
	/*
	 * Set of actual jobs this task if currently part of. May be modified during setup.
	 */
	Set<String> jobs();
	
	/*
	 * Set the name of the task. Called automagically.
	 */
	void setName( String name);
	
	String getName();
	boolean hasJob(String job);
	void addJob(String job);
	
	
}
