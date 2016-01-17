package com.almightyalpaca.discordbot.config;

import java.io.File;

public class FileOrigin extends ConfigOrigin {
	
	private final File origin;
	
	public FileOrigin(final File origin) {
		this.origin = origin;
	}
	
	@Override
	public Origin getEnumOrigin() {
		return Origin.FILE;
	}
	
	@Override
	public File getOrigin() {
		return this.origin;
	}
}
