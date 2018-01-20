package com.azul.tool.depsextended;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractTask implements Task {
	private String name;
	private HashSet<String> jobs = new HashSet<String>();
	
	
	public AbstractTask()
	{
		for (String job: getJobs() ) jobs.add(job);
	}
	
	public void setName( String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	/*
	 * Override for dependency checks
	 * @see com.azul.tool.depsextended.Task#canRun(com.azul.tool.depsextended.DependencyAnalyser)
	 */
	public boolean canRun(DependencyAnalyser ctx) {
		return true;
	}
	/*
	 * Override for task description
	 * @see com.azul.tool.depsextended.Task#getDescription()
	 */
	public String getDescription() {
		return "No description";
	}
	
	public String getOptions(){
		return null;
	}
	
	public String[] getJobs() {
		return new String[] {"default"};
	}
	
	public Set<String> jobs() {
		return jobs;
	}
	
	public void addJob(String job){
		jobs.add(job);
	}
	
	public boolean hasJob(String job)
	{
		return jobs.contains(job);
	}
}
