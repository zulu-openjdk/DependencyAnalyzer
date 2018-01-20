package com.azul.tool.depsextended;

import static com.azul.tool.depsextended.DependencyAnalyser.*;

import java.io.File;

@SuppressWarnings("static-access")
public class TaskParseAppClasspath extends AbstractTask {
	
	public void run( DependencyAnalyser ctx ) {
		appModulePaths = parseClassPath(appClasspath);
		if (debug) for(String s: ctx.appModulePaths) debug("App module:" + s); 
		for(String s: ctx.appModulePaths) {
			File f = new File(s);
			if (!f.exists()) terminateWithError("Classpath element does not exist: " + appModulePaths);
		}
	}
	
	public boolean canRun(DependencyAnalyser ctx) {
		return (ctx.appClasspath != null);
	}
	
	public Option[] getOptions(){
		return new Option[] {
				new Option("-appclasspath", false, "<classpath> - Application classpath. Wildcards allowed e.g. c:/cassandra/lib/*")
		};
	}
	
	public String[] dependsOnTasks()
	{
		return new String[] { "FindJdeps"};
	}
	
	public String getDescription() {
		return "Parses and expands the classpath of the application to analyse.";
	}
	
	public String[] getJobs() {
		return new String[] {"default"};
	}
	
	
	
}
