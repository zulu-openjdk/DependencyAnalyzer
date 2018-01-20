package com.azul.tool.depsextended;

import java.util.Set;

public interface Task {
	void run(DependencyAnalyser ctx) throws Exception;
	boolean canRun(DependencyAnalyser ctx);
	
	String getDescription();
	String getOptions();
	String[] getJobs();
	Set<String> jobs();
	void setName( String name);
	String getName();
	boolean hasJob(String job);
	void addJob(String job);
}
