package com.almightyalpaca.discordbot.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class RootConfig extends Config
{

    private final ConfigOrigin origin;

    RootConfig(final File file) throws WrongTypeException, KeyNotFoundException, JsonIOException, JsonSyntaxException, FileNotFoundException
    {
        super(null, new JsonParser().parse(new FileReader(file)).getAsJsonObject());
        this.origin = new FileOrigin(file);
    }

    RootConfig(final String json)
    {
        super(null, new JsonParser().parse(json).getAsJsonObject());
        this.origin = new StringOrigin(json);
    }

    RootConfig(final URL url) throws JsonIOException, JsonSyntaxException, WrongTypeException, KeyNotFoundException, IOException
    {
        super(null, new JsonParser().parse(new InputStreamReader(url.openStream())).getAsJsonObject());
        this.origin = new UrlOrigin(url);
    }

    @Override
    public ConfigOrigin getOrigin()
    {
        return this.origin;
    }

    @Override
    public boolean save()
    {
        final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().serializeSpecialFloatingPointValues().create();
        final String json = gson.toJson(this.config);

        if (this.origin instanceof FileOrigin)
        {
            final FileOrigin fileOrigin = (FileOrigin) this.origin;
            try
            {
                final BufferedWriter writer = new BufferedWriter(new FileWriter(fileOrigin.getOrigin()));
                writer.write(json);
                writer.close();
            } catch (final IOException e)
            {
                e.printStackTrace();
                return false;
            }
        } else
        {
            return false;
        }
        return true;
    }

}
