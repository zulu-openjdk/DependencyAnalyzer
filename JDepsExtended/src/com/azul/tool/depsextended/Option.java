package com.azul.tool.depsextended;

public class Option {
	String name;
	boolean mandatory;
	String description;
	
	public Option(String name, boolean mandatory, String description )
	{
		this.name = name;
		this.mandatory = mandatory;
		this.description = description;
	}
}
