package com.azul.tool.depsextended;


import static com.azul.tool.depsextended.DependencyAnalyser.*;

import java.io.File;

@SuppressWarnings("static-access")
public class TaskCheckJRE extends AbstractTask {
	
	public void run( DependencyAnalyser ctx ) {
		File checkrtjar = new File(jreDir, "lib/rt.jar");
		File checkmodules = new File(jreDir, "lib/modules");
		if ( !checkrtjar.exists() && !checkmodules.exists())
		{
			terminateWithError( jreDir + " does not seem to be a JRE. Neither " +  checkrtjar + " nor " + checkmodules + " exists!");
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
		return "For preparation: Common sense check of the presented path is actullay a JRE.";
	}
	
	public String[] getJobs() {
		return new String[] {"default"};
	}

}
