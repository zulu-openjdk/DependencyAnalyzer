package com.azul.tool.depsextended;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;

import static  com.azul.tool.depsextended.DependencyAnalyser.*;

public class JDeps {
	
	public static Module parse(File file, String name, String path) throws Exception
	{ 
		if (debug) debug("Parsing " + file);
		int depCounter = 0;
		Module module = new Module(name);
		module.path = path;
		module.initDeps();
		
		BufferedReader in = new BufferedReader( new FileReader( file) ); 
		String line;
		Klass klass = null;
		while ( (line = in.readLine()) != null )
		{
			if (debug) debug("Line: " + line);
			line = line.trim();
			int linetype = -1;
			String klassName = null;
			String moduleName = null;
			//Determine linetype
			int ppos = line.indexOf("->");
			if ( ppos == -1 )
			{
				if (line.length() == 0 ) { linetype = 0; } //Empty line
				else {
					linetype = 1; //class name followed by list of dependencies
					ppos = line.indexOf(' ');
					klassName = line.substring(0, ppos);
					if (debug) debug("Contains: " + klassName);
				}
			} else {
				if ( ppos == 0)
				{
					linetype = 2; //Dependency
					ppos = line.indexOf(' ', 3);
					if ( ppos == -1 ) {
						klassName = line.substring(3);
						moduleName = null;
					} else {
						klassName = line.substring(3,ppos);
						moduleName = line.substring(ppos).trim();
					}
					if (debug) debug("Depends on: " + klassName );
				} else {
					linetype = 3; //Module dependency
					moduleName = line.substring(ppos+3);
				}
			}
			switch (linetype) {
				case 0: 
					klass = null;
					break;
				case 1: 
					klass = new Klass(klassName); 
					klass.parentModule = module;
					module.klasses.put(klassName, klass); 
					break;
				case 2: 
					if ( module.name.equals(moduleName) )
					{
						klass.dependencies.add(new Dependency(klassName, module));
						//Add missing internal classes that do not depend on anybody else
						if ( module.klasses.get(klassName)==null ) {
							Klass _klass = new Klass(klassName); 
							_klass.parentModule = module;
							module.klasses.put(klassName, _klass); 
						}
							
					} else {
						klass.dependencies.add(new Dependency(klassName));
					}
					depCounter++; 
					break;
				case 3: 
					break; //We don't record this
			}
		}
		in.close();
		if (debug) debug("Classes: " + module.klasses.size() + " Deps: " + depCounter);
		
		return module;
	}
	
	/*
	 * %JAVA%\bin\jdeps -verbose:class D:\azul\software\zulu_embedded\ezre-1.8.0_121-8.20.0.38-headless-linux_aarch32hf\lib\rt.jar > rt.txt
	 */
	public static void execute(String module, String outFile, String jdeps ) throws Exception
	{
		String[] cmd = new String[] { jdeps,"-verbose:class", module};
		Process process = Runtime.getRuntime().exec(cmd);
		InputStream inp = process.getInputStream();
		
		OutputStream outp = new FileOutputStream(outFile);
		byte[] data = new byte[1024];
		int len;
		while ( (len=inp.read(data))!=-1 ) {
			outp.write(data, 0, len);
		}
		outp.close();
	}
	
}





