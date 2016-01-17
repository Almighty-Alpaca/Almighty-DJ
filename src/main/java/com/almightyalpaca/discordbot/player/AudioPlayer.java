package com.almightyalpaca.discordbot.player;

public abstract class AudioPlayer {
	
	public boolean	stop	= false;
					
	public boolean	skip	= false;
					
	public abstract void play();
}
