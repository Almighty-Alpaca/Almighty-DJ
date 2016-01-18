package com.almightyalpaca.discordbot.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class ConfigFactory
{

    public static Config getConfig(final File file) throws IOException, WrongTypeException, KeyNotFoundException, JsonIOException, JsonSyntaxException, FileNotFoundException
    {
        file.createNewFile();
        return new RootConfig(file);
    }

    public static Config getConfig(final String string)
    {
        return new RootConfig(string);
    }

    public static Config getConfig(final URL url) throws JsonIOException, JsonSyntaxException, WrongTypeException, KeyNotFoundException, IOException
    {
        return new RootConfig(url);
    }

    public static Config getDefaultConfig() throws IOException, WrongTypeException, KeyNotFoundException, JsonIOException, JsonSyntaxException, FileNotFoundException
    {
        final File file = new File("config.json");
        return ConfigFactory.getConfig(file);
    }

}
