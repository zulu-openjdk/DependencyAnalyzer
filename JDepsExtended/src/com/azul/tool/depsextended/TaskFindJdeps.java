package com.azul.tool.depsextended;

import static com.azul.tool.depsextended.DependencyAnalyser.*;

import java.io.File;

@SuppressWarnings("static-access")
public class TaskFindJdeps extends AbstractTask {
	
	public void run( DependencyAnalyser ctx ) {
		javaHomePath = System.getProperty("java.home");
		if (debug)  debug("Java Home: " + javaHomePath); 
		File javaHomeDir = new File(javaHomePath );
		
		File checkjdeps = new File(javaHomeDir, "bin/jdeps");
		if ( !checkjdeps.exists() )  {
		checkjdeps = new File(javaHomeDir, "bin/jdeps.exe");
		if ( !checkjdeps.exists() ) {
		checkjdeps = new File(javaHomeDir, "../bin/jdeps");
		if ( !checkjdeps.exists() ) {
		checkjdeps = new File(javaHomeDir, "../bin/jdeps.exe");
		if ( !checkjdeps.exists() ) {
			terminateWithError("Jdeps not found in currently used JRE/JDK " + javaHomePath );
		}}}}
		jdepsPath = checkjdeps.getAbsolutePath(); 
		if (debug)  debug("Using Jdeps in: " + jdepsPath); 
	}
	
	
	public String getDescription() {
		return "For preparation: Find the jdeps executable we need to analyse modules and jar file.";
	}
	
	public String[] getJobs() {
		return new String[] {"default"};
	}

}