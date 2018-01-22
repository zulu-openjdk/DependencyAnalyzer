package com.azul.tool.depsextended;

import java.util.HashSet;
import java.util.Set;

public class Pakage {
	public String name;
	public boolean isJava;
	
	public Set<String> klasses = new HashSet<String>();
	
	public static boolean isJava(String name)
	{
		if ( name.startsWith("java.") ) return true;
		if ( name.startsWith("javax.") ) return true;
			
		return false;
	}
	
	public static String pakage(String name)
	{
		int pos = name.lastIndexOf(".");
		if (pos ==-1) return "";
		return name.substring(0,pos);
	}
	
	public Pakage( String name )
	{
		this.name  = name;
		isJava = isJava(name);
	}
}
