package com.azul.tool.depsextended;

import java.util.ArrayList;
import java.util.List;

public class Klass {
	public String name;
	//private String pakage;
	public Module parentModule;
	
	public List<Dependency> dependencies;
	
	public Klass( String name )
	{
		this.name = name;
		initDeps();
	}
	
	public Klass( String name, Module mod )
	{
		this.name = name;
		this.parentModule = mod;
		initDeps();
	}
	
	public String getPackage()
	{
		String pakage = null;
		if (pakage==null)
		{
			int ppos = name.lastIndexOf(".");
			pakage = name.substring(0, ppos);
		}
		return pakage;
	}
	
	private void initDeps()
	{
		dependencies = new ArrayList<Dependency>();
	}
	
	public int hashCode()
	{
		return name.hashCode();
	}
	
	
	public boolean dependsOn(String klassname)
	{
		for( Dependency d: this.dependencies)
		{
			if (d.klassname.equals(klassname)) return true;
		}
		return false;
	}
	
	public boolean equals(Object o )
	{
		if (!(o instanceof Klass) || o==null) return false;
		Klass oo = (Klass)o;
		return this.name.equals(oo.name);
	}
}
