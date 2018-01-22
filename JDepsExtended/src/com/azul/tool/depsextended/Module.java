package com.azul.tool.depsextended;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Module {
	public String name;
	public String path;
	
	public HashMap<String,Module> dependentModules;
	public HashMap<String,HashMap<String,Pakage>> dependentPakages;
	public HashMap<String,Klass> klasses;
	
	public HashMap<String,Pakage> unresolved;
	
	public Module( String name )
	{
		this.name = name;
	}
	
	public void initDeps()
	{
		dependentModules = new HashMap<String,Module>();
		klasses = new HashMap<String,Klass>();
		dependentPakages = new HashMap<String,HashMap<String,Pakage>>();
		unresolved = new HashMap<String,Pakage>();
	}
	
	public void addDependency( Module module, Klass klass)
	{
		String pakageName = Pakage.pakage(klass.name);
		Module mod = dependentModules.get(module.name);
		HashMap<String, Pakage> pakages = dependentPakages.get(module.name);
		if ( mod == null ) {
			dependentModules.put(module.name, module);
			pakages = new HashMap<String,Pakage>();
			dependentPakages.put(module.name, pakages);
		}
		Pakage pakage = pakages.get(pakageName);
		if (pakage==null) {
			pakage = new Pakage(pakageName);
			pakages.put(pakageName, pakage);
		}
		pakage.klasses.add(klass.name);
	}
	
	public void addUnresolved( String klassName)
	{
		String pakageName = Pakage.pakage(klassName);
		Pakage pakage = unresolved.get(pakageName);
		if (pakage==null) {
			pakage = new Pakage(pakageName);
			unresolved.put(pakageName,pakage);
		}
		pakage.klasses.add(klassName);
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
