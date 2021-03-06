package com.azul.tool.depsextended;


import static com.azul.tool.depsextended.DependencyAnalyser.*;

import java.io.File;
import java.util.Map.Entry;

@SuppressWarnings("static-access")
public class TaskClassDependencies extends AbstractTask {
	
	public void run( DependencyAnalyser ctx ) throws Exception {
		analyseModules();
		resolveDependencies();
	}
	
	public boolean canRun(DependencyAnalyser ctx) {
		if (ctx.jreDir == null && ctx.appClasspath == null) return false;
		if (ctx.appClasspath == null) print("Testing JRE consistency only. No appclasspath specified.");
		if (ctx.jreDir == null) print("Testing application consistency only. No JRE specified.");
		return true;
	}
	
	public String getDescription() {
		return "Parse all class dependencies using jdeps. Try to resolve in application and to JRE. Report all unresolve depdencies in the application and towards the JRE.";
	}
	
	public String[] getJobs() {
		return new String[] {"default"};
	}
	
	public static void analyseModules() throws Exception
	{
		for(String module: appModulePaths) {
			analyseModule( module, appModules, allModules);
			
		}
		for(String module: jreModulePaths) {
			analyseModule( module, jreModules, allModules);
		}
		print("");
	}

	public String[] dependsOnTasks()
	{
		return new String[] { "ListJREModules", "ParseAppClasspath" };
	}
	
	public static void resolveDependencies()
	{
		for(Module module: allModules) 
		{
			print("Resolving:" + module.name); 
			for(Entry<String, Klass> entry: module.klasses.entrySet()) 
			{
				for(Dependency dep: entry.getValue().dependencies ) 
				{
					if ( dep.resolvedto==null ) {
						Module depModule = resolveKlass(dep.klassname);	
						dep.resolvedto = depModule;			
						if (dep.resolvedto==null)
						{
							module.addUnresolved(dep.klassname);
							String s = "Resolution failure: " + module.name +" : "+entry.getValue().name+" -> "+dep.klassname;
							if (detailsClass==true)
								print(s);
							else
								if (debug) debug(s);
						}
						else {
							module.addDependency(depModule, depModule.klasses.get(dep.klassname));
						}
					}
				}
			}
			module.reportDependencies();
		}
	}

}