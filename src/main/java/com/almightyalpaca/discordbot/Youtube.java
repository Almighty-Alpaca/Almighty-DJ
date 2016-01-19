package com.almightyalpaca.discordbot;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Youtube
{
    public static String getYoutubeVideoId(URL url)
    {
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";

        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url.toString());

        if (matcher.find())
        {
            return matcher.group();
        }
        return null;
    }
}
