package com.azul.tool.depsextended;

import static  com.azul.tool.depsextended.DependencyAnalyser.*;

public class TaskSample extends AbstractTask {
	public void run( DependencyAnalyser ctx ) {
		print("Hello World");
		if (debug) debug("Sample Debug: -sampleOption " + ctx.sampleOption);
	}
	
	
	public Option[] getOptions(){
		return new Option[] {
				new Option("-jrsampleOptione", true, "<sampleValue> - Just a sample option.")
		};
	}
	
	public String getDescription() {
		return "Sample Task";
	}
	
	public String[] getJobs() {
		return new String[] {"sample"};
	}
	
	
	
}
