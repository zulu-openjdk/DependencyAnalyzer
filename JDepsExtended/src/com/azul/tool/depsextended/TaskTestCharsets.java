package com.azul.tool.depsextended;

import static com.azul.tool.depsextended.DependencyAnalyser.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jdk.nashorn.internal.ir.SetSplitState;

@SuppressWarnings("static-access")
public class TaskTestCharsets extends AbstractTask {
	
	public void run( DependencyAnalyser ctx ) throws Exception {
		//No JDK 9+ support so far
		String name = "charsets.jar";
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
		//Find classes matching: sun.nio.cs.ext.*
		//Exclude inner classes
		List<String> charsets = new ArrayList<String>();
		String prefix = "sun.nio.cs.ext.";
		for ( Klass klass: module.klasses.values()){
			if ( klass.name.startsWith(prefix)){
				String charsetName = klass.name.substring(prefix.length());
				if ( charsetName.indexOf("$") == -1 ){
				if ( !charsetName.endsWith("coder") ){
				if ( klass.dependsOn("java.nio.charset.Charset") ) {
					if (debug) debug("Charset: " + charsetName );
					charsets.add(charsetName);
				}
				}
				}
			}
		}
		//Save to disk (optional)
		if ( saveCharsets!=null) {
			saveList(charsets, saveCharsets);
		}
		String referenceList = "standardCharsetsJDK8";
		//Load from disk 
		List<String> list = loadList(referenceList+".txt");
		
		//Compare and report missing 
		list.removeAll(charsets);
		if ( list.size() != 0 ){
			print( "Missing charsets relative to reference: " + referenceList);
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
				new Option("-saveCharsets", false, "<filename> - Save charsets from JRE to file.")
		};
	}
	
	
	public String[] dependsOnTasks()
	{
		return new String[] { "FindJdeps", "ListJREModules"};
	}
	
	public String getDescription() {
		return "Find all supported charsets and test against known set.";
	}
	
	public String[] getJobs() {
		return new String[] {"default"};
	}

}