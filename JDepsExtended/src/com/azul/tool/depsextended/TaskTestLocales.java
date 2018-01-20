package com.azul.tool.depsextended;

import static com.azul.tool.depsextended.DependencyAnalyser.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jdk.nashorn.internal.ir.SetSplitState;

@SuppressWarnings("static-access")
public class TaskTestLocales extends AbstractTask {
	
	public void run( DependencyAnalyser ctx ) throws Exception {
		//No JDK 9+ support so far
		String name = "localedata.jar";
		if (debug) debug("Searching: " + name); 
		Module module = findModuleByName( jreModules, name);
		if ( module == null )
		{
			String path = findModuleByName( jreModulePaths, name );
			if (debug) debug("Found path: " + path); 
			analyseModule( path, jreModules, allModules);
			module = findModuleByName( jreModules, name);
			if (debug) debug("Done module: " + module.name ); 
		}
		//Find classes matching: sun.util.resources.<locale>.LocaleNames_<locale>
		List<String> locales = new ArrayList<String>();
		String prefix = "sun.util.resources.";
		for ( Klass klass: module.klasses.values()){
			if ( klass.name.startsWith(prefix)){
				String localeName = klass.name.substring(prefix.length());
				String match = "LocaleNames_";
				int pos = localeName.indexOf(match);
				if (pos != -1 ) {
					if (debug) debug("Locale: " + klass.name + " - " + localeName);
					localeName = localeName.substring(pos+match.length());
					if (debug) debug("Locale: " + localeName );
					locales.add(localeName);
				}
			}
		}
		//Save to disk (optional)
		if ( saveLocales!=null) {
			saveList(locales, saveLocales);
		}
		String referenceList = "standardLocalesJDK8";
		//Load from disk 
		List<String> list = loadList(referenceList+".txt");
		
		//Compare and report missing 
		list.removeAll(locales);
		if ( list.size() != 0 ){
			print( "Missing locales relative to reference: " + referenceList);
			for( String s:list) {
				print("   " + s);
			}
		}
	}
	
	public boolean canRun(DependencyAnalyser ctx) {
		return (ctx.jreModulePaths != null);
	}
	
	public Option[] getOptions(){
		return new Option[] {
				new Option("-saveLocales", false, "<filename> - Save locales from JRE to file.")
		};
	}
	
	public String getDescription() {
		return "Find all supported locales and test against known set.";
	}
	
	public String[] getJobs() {
		return new String[] {"default"};
	}

}