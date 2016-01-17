package com.almightyalpaca.discordbot.config;

public abstract class ConfigOrigin {
	
	public enum Origin {
					STRING, FILE, URL;
	}
	
	public abstract Origin getEnumOrigin();
	
	public abstract Object getOrigin();
	
}
