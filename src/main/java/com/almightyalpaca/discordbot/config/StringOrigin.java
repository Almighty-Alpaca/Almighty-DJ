package com.almightyalpaca.discordbot.config;

public class StringOrigin extends ConfigOrigin {
	
	private final String origin;
	
	public StringOrigin(final String origin) {
		this.origin = origin;
	}
	
	@Override
	public Origin getEnumOrigin() {
		return Origin.STRING;
	}
	
	@Override
	public String getOrigin() {
		return this.origin;
	}
}
