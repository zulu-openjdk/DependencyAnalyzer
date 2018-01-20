package com.azul.tool.depsextended;

public class Dependency {
	public String klassname;
	public Module resolvedto = null;
	
	public Dependency( String klassname )
	{
		this.klassname = klassname;
	}
	
	public Dependency( String klassname, Module module )
	{
		this.klassname = klassname;
		resolvedto = module;
	}
	
	public int hashCode()
	{
		return klassname.hashCode();
	}
	
	public boolean equals(Object o )
	{
		if (!(o instanceof Dependency) || o==null) return false;
		Dependency oo = (Dependency)o;
		return this.klassname.equals(oo.klassname);
	}
}
