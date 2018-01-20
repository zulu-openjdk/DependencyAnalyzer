package com.azul.tool.depsextended;

import java.util.HashMap;
import java.util.HashSet;

public class Module {
	public String name;
	public String path;
	
	public HashSet<Module> dependentModules;
	public HashMap<String,Klass> klasses;
	
	public Module( String name )
	{
		this.name = name;
	}
	
	public void initDeps()
	{
		dependentModules = new HashSet<Module>();
		klasses = new HashMap<String,Klass>();
	}
	
	public int hashCode()
	{
		return path.hashCode();
	}
	
	public boolean equals(Object o )
	{
		if (!(o instanceof Module) || o==null) return false;
		Module oo = (Module)o;
		return this.path.equals(oo.path);
	}
}
