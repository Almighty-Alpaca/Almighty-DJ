package com.almightyalpaca.discordbot.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import de.voidplus.soundcloud.SoundCloud;
import de.voidplus.soundcloud.Track;

public class SoundCloudUtil extends Thread
{

    private static SoundCloud soundcloud = null;

    public static URL getSoundCloudTrackURL(String url) throws UnresolveableLinkException
    {

        String[] parts = url.split("/");

        String artistString = parts[parts.length - 2];
        String trackString = parts[parts.length - 1];

        List<Track> tracks = soundcloud.findTrack(trackString);
        Track track = null;
        for (Track t : tracks)
        {
            if (t.getPermalink().equalsIgnoreCase(trackString) && t.getUser().getPermalink().equalsIgnoreCase(artistString))
            {
                track = t;
            }
        }

        if (track == null)
        {
            throw new UnresolveableLinkException();
        } else
        {
            try
            {
                return new URL(track.getStreamUrl());
            } catch (MalformedURLException e)
            {
                throw new UnresolveableLinkException(e);
            }
        }

    }

    /**
     * Only has to be called once ti initialze, after that only to change account
     * 
     * @param client_id
     * @param client_secret
     */
    public static void init(String client_id, String client_secret)
    {
        soundcloud = new SoundCloud(client_id, client_secret);
    }
}