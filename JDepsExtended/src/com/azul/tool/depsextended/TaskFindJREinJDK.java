package com.azul.tool.depsextended;

import static com.azul.tool.depsextended.DependencyAnalyser.*;

import java.io.File;

@SuppressWarnings("static-access")
public class TaskFindJREinJDK extends AbstractTask {
	
	public void run( DependencyAnalyser ctx ) {
		//Check for JDK
		File checkJDK = new File(jreDir, "jre");
		if ( checkJDK.exists() ) {
			jreDir = checkJDK;
			if (debug)  debug("Found JDK switching to JRE: " + jreDir); 
		}
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
		return "For preparation: Find the JRE inside a JDK.";
	}
	
	public String[] getJobs() {
		return new String[] {"default"};
	}

}
