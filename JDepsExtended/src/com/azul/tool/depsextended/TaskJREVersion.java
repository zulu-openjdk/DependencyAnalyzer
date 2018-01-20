package com.azul.tool.depsextended;

import static com.azul.tool.depsextended.DependencyAnalyser.*;

import java.io.File;

@SuppressWarnings("static-access")
public class TaskJREVersion extends AbstractTask {
	
	public void run( DependencyAnalyser ctx ) {
		//TODO
	}
	
	public boolean canRun(DependencyAnalyser ctx) {
		return (ctx.jreDir != null);
	}
	
	public Option[] getOptions(){
		return new Option[] {
				new Option("-jre", false, "Path to JRE/JDK")
		};
	}
	
	public String getDescription() {
		return "Try to determine the JRE version without actually running it. Parses java.so/dll";
	}
	
	public String[] getJobs() {
		return new String[] {"default,javaversion"};
	}

}