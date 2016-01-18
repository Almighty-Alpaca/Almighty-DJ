package com.almightyalpaca.discordbot.config;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class ConfigTest
{

    public static void main(final String[] args) throws JsonIOException, JsonSyntaxException, WrongTypeException, KeyNotFoundException, FileNotFoundException, IOException
    {

        final Config config = ConfigFactory.getDefaultConfig();

        config.save();
    }

}
