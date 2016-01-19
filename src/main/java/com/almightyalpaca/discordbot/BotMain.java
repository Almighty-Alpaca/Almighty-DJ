package com.almightyalpaca.discordbot;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.json.JSONObject;

import com.almightyalpaca.discordbot.config.Config;
import com.almightyalpaca.discordbot.config.ConfigFactory;
import com.almightyalpaca.discordbot.config.KeyNotFoundException;
import com.almightyalpaca.discordbot.config.WrongTypeException;
import com.almightyalpaca.discordbot.player.AbstractAudioPlayer;
import com.almightyalpaca.discordbot.player.AudioFilePlayer;
import com.almightyalpaca.discordbot.player.AudioUrlPlayer;
import com.github.axet.wget.WGet;
import com.github.axet.wget.info.DownloadInfo;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.JDAInfo;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.events.InviteReceivedEvent;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import net.dv8tion.jda.requests.WebSocketClient;
import net.dv8tion.jda.utils.InviteUtil;;

public class BotMain extends ListenerAdapter
{

    public JDA api = null;

    public Config config = null;

    File musicDir = null;

    public List<AbstractAudioPlayer> list = new ArrayList<>();

    public TextChannel boundTextChannel = null;
    public VoiceChannel boundVoiceChannel = null;

    int currentFile = 1;

    public BotMain()
    {
        try
        {
            this.musicDir = new File("music");
            this.musicDir.mkdirs();

            this.config = ConfigFactory.getDefaultConfig();

            // Initialize Discord API
            System.out.println("Initialzing JDA " + JDAInfo.VERSION);

            final JDABuilder builder = new JDABuilder(this.config.getString("credentials.email"), this.config.getString("credentials.password"));

            if (this.config.getBoolean("proxy.use"))
            {
                builder.setProxy(this.config.getString("proxy.host"), this.config.getInt("proxy.port"));
            }
            builder.addListener(this);
            this.api = builder.buildBlocking();

            this.api.getAccountManager().setGame(this.config.getString("prefix") + " help");

            System.out.println("Finished");

        } catch (JsonIOException | JsonSyntaxException | WrongTypeException | KeyNotFoundException | IOException | LoginException | IllegalArgumentException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(final String[] args)
    {
        new BotMain();
    }

    public void addFile(final File file) throws IOException
    {
        System.out.println("Adding " + file);
        final AbstractAudioPlayer player = new AudioFilePlayer(this, file);
        this.list.add(player);
    }

    public void addFileAfterDownload(final URL url) throws IOException
    {
        // get file remote information
        DownloadInfo info = new DownloadInfo(url);
        info.extract();
        info.enableMultipart();
        info.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");

        File file = new File(musicDir, String.valueOf(currentFile++) + " " + info.getContentFilename());
        file.deleteOnExit();

        // initialize wget object
        WGet w = new WGet(info, file);
        // single thread download. will return here only when file download
        // is complete (or error raised).
        w.download();

        addFile(file);
    }

    public void addYoutube(String id) throws IOException
    {
        try
        {

            if (id != null)
            {
                HttpResponse<JsonNode> response = Unirest.get("http://www.youtubeinmp3.com/fetch/?format=JSON&video=http://www.youtube.com/watch?v=" + id).header("Accept", "application/json").asJson();
                JSONObject object = response.getBody().getObject();

                String link = object.getString("link");

                try
                {
                    addURL(new URL(link));
                } catch (Exception ignored)
                {
                }

            }
        } catch (UnirestException e)
        {
            e.printStackTrace();
        }
    }

    public void addURL(final URL url) throws IOException
    {
        System.out.println("Adding " + url);
        final AbstractAudioPlayer player = new AudioUrlPlayer(this, url);
        this.list.add(player);
    }

    @Override
    public void onGuildMessageReceived(final GuildMessageReceivedEvent event)
    {
        System.out.println(event.getMessage().getMentionedUsers().isEmpty());

        String text = event.getMessage().getRawContent();

        final TextChannel channel = event.getChannel();

        if (text.startsWith(this.config.getString("prefix") + " "))
        {
            text = text.replace(this.config.getString("prefix") + " ", "");
            if (text.startsWith("join "))
            {
                text = text.replace("join ", "");

                if (this.boundVoiceChannel != null)
                {
                    return;
                }
                final String vcName = text.substring(0, text.indexOf(" ") == -1 ? text.length() : text.indexOf(" "));

                for (final VoiceChannel vc : event.getGuild().getVoiceChannels())
                {
                    if (vc.getName().contentEquals(vcName))
                    {
                        System.out.println("Joining " + vc.getName());
                        this.boundVoiceChannel = vc;
                        this.boundTextChannel = channel;

                        final JSONObject obj = new JSONObject().put("op", 4).put("d", new JSONObject().put("guild_id", event.getGuild().getId()).put("channel_id", this.boundVoiceChannel.getId()).put("self_mute", false).put("self_deaf", false));
                        final WebSocketClient client = ((JDAImpl) event.getJDA()).getClient();
                        client.send(obj.toString());
                        break;
                    }
                }
                if (this.boundVoiceChannel == null)
                {
                    channel.sendMessage("Voice channel not found!");
                    return;
                }
            } else if (text.startsWith("play"))
            {
                if (this.list.size() == 0)
                {
                    channel.sendMessage("Queue empty !");
                } else
                {
                    this.play();
                }
            } else if (text.startsWith("pause"))
            {
                pause();
            } else if (text.startsWith("skip"))
            {
                if (this.list.size() <= 1)
                {
                    channel.sendMessage("No more files");
                    this.stop();
                } else
                {
                    this.skip();
                }
            } else if (text.startsWith("stop"))
            {
                this.stop();
            } else if (text.startsWith("help"))
            {
                String output = "";
                output += "The **first** music bot for discord made in **pure java** !" + "\n";
                output += "**Commands:**" + "\n";
                output += "**" + this.config.getString("prefix") + " join [channel name]** Join a voice channel" + "\n";
                // output += "**" + this.config.getString("prefix") + " leave** Leave the current voice channel" + "\n";
                output += "**" + this.config.getString("prefix") + " add [link]** Add a sound file to the queue" + "\n";
                output += "**" + this.config.getString("prefix") + " play** Start/resume the music playback" + "\n";
                output += "**" + this.config.getString("prefix") + " pause** Pause the music playback" + "\n";
                output += "**" + this.config.getString("prefix") + " skip** Skip the current song" + "\n";
                output += "**" + this.config.getString("prefix") + " stop** Stop the music playback";
                channel.sendMessage(output);
            } else if (text.startsWith("add "))
            {
                text = text.replace("add ", "");

                final String link = text.substring(0, text.indexOf(" ") == -1 ? text.length() : text.indexOf(" "));
                try
                {
                    final URL url = new URL(link);
                    String id = Youtube.getYoutubeVideoId(url);
                    if (id != null)
                    {
                        this.addYoutube(id);
                    } else
                    {
                        this.addURL(url);
                    }
                } catch (final IOException e)
                {
                    e.printStackTrace();
                }

            } else if (text.startsWith("volume "))
            {
                text = text.replace("volume ", "");
                final String volume = text.substring(0, text.indexOf(" ") == -1 ? text.length() : text.indexOf(" "));

                try
                {
                    float vol = Float.parseFloat(volume);
                    amplitude = vol;
                } catch (Exception e)
                {
                    e.printStackTrace();
                }

            } else if (text.startsWith("shutdown"))
            {
                if (event.getAuthor().getId().contentEquals(config.getString("ownerId")))
                {
                    System.exit(0);
                }
                // } else if (text.startsWith("leave"))
                // {
                // stop();
                // final JSONObject obj = new JSONObject().put("op", 4).put("d", new JSONObject().put("guild_id", JSONObject.NULL).put("channel_id", JSONObject.NULL).put("self_mute", false).put("self_deaf", false));
                // final WebSocketClient client = ((JDAImpl) event.getJDA()).getClient();
                // client.send(obj.toString());
            }
        }

    }

    @Override
    public void onInviteReceived(final InviteReceivedEvent event)
    {
        if (event.getMessage().getMentionedUsers().contains(this.api.getSelfInfo()))
        {
            InviteUtil.join(event.getInvite(), this.api);
        }
    }

    public float amplitude = 1.0F;

    public void play()
    {
        if (boundVoiceChannel == null)
        {
            return;
        }
        if (this.list.size() == 0)
        {
            return;
        } else
        {
            AbstractAudioPlayer player = list.get(0);
            if (player.pause)
            {
                player.pause = false;
            } else
            {
                player.play();
            }
        }
    }

    public void skip()
    {
        if (this.list.size() == 0)
        {
            return;
        } else
        {
            AbstractAudioPlayer player = list.get(0);
            if (player.pause)
            {
                player.pause = false;
            } else
            {
                player.skip = true;
            }
        }
    }

    public void pause()
    {
        if (this.list.size() == 0)
        {
            return;
        } else
        {
            this.list.get(0).pause = true;
        }
    }

    public void stop()
    {
        if (this.list.size() == 0)
        {
            return;
        } else
        {
            AbstractAudioPlayer player = list.get(0);
            if (player.pause)
            {
                player.pause = false;
            } else
            {
                player.stop = true;
            }
        }
    }

}
