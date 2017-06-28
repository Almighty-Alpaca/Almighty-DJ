package com.almightyalpaca.discordbot.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class YoutubeUtil
{

    private static String marshapeKey = null;

    /**
     * Only has to be called once to initialze, after that only to change key.
     * 
     * @param marshapeKey
     *        your marshape key
     */
    public static void init(String marshapeKey)
    {
        YoutubeUtil.marshapeKey = marshapeKey;
    }

    public static String getYoutubeVideoId(String url)
    {
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";

        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);

        if (matcher.find())
        {
            return matcher.group();
        }
        return null;
    }

    public static URL getYoutubeURLDirect(String link) throws IOException, UnsupportedAudioFileException, UnirestException
    {
        // These code snippets use an open-source library. http://unirest.io/java
        HttpResponse<JsonNode> response = Unirest.get("https://zazkov-youtube-grabber-v1.p.mashape.com/download.video.php?id=WSeNSzJ2-Jw")
                .header("X-Mashape-Key", marshapeKey).header("Accept", "application/json").asJson();

        JSONObject object = response.getBody().getObject();

        JSONArray audio = object.getJSONArray("audio");

        List<URL> urls = new ArrayList<>();

        audio.forEach(o ->
        {
            try
            {
                urls.add(new URL(o.toString()));
            }
            catch (Exception ignored)
            {
            }
        });

        return urls.stream().findFirst().orElse(null);
    }

    public static URL getYoutubeURL(String link) throws IOException, UnsupportedAudioFileException, UnirestException
    {
        String id = getYoutubeVideoId(link);
        HttpResponse<JsonNode> response = Unirest.get("http://www.youtubeinmp3.com/fetch/?format=JSON&video=http://www.youtube.com/watch?v=" + id)
                .header("Accept", "application/json").asJson();
        JSONObject object = response.getBody().getObject();

        String url = object.getString("link");

        return new URL(url);

    }
}
