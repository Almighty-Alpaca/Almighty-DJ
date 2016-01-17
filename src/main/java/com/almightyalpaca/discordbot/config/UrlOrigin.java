package com.almightyalpaca.discordbot.config;

import java.net.URL;

public class UrlOrigin extends ConfigOrigin {
	
	private final URL origin;
	
	public UrlOrigin(final URL origin) {
		this.origin = origin;
	}
	
	@Override
	public Origin getEnumOrigin() {
		return Origin.URL;
	}
	
	@Override
	public URL getOrigin() {
		return this.origin;
	}
}
