package com.almightyalpaca.discordbot.player;

public abstract class AudioPlayer
{

    public boolean stop = false;

    public boolean skip = false;

    public boolean pause = false;

    public abstract void play();
}
