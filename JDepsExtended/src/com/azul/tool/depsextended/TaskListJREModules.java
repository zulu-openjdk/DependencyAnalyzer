package com.azul.tool.depsextended;

import static com.azul.tool.depsextended.DependencyAnalyser.*;

import java.io.File;

@SuppressWarnings("static-access")
public class TaskListJREModules extends AbstractTask {
	
	public void run( DependencyAnalyser ctx ) {
		/*
		 * Traverse jre and find jars
		 */
		jreModulePaths = searchByExtension(jreDir, ".jar", true);
		if (debug) for(String s: jreModulePaths) debug("JRE module:" + s); 
	}
	
	public boolean canRun(DependencyAnalyser ctx) {
		return (ctx.jreDir != null);
	}
	
	public Option[] getOptions(){
		return new Option[] {
				new Option("-jre", true, "<jre> - Path to JRE/JDK")
		};
	}
	
	public String getDescription() {
		return "Find all modules/jars in the JRE.";
	}
	
	public String[] getJobs() {
		return new String[] {"default"};
	}

}