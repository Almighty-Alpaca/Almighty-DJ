package com.almightyalpaca.discordbot;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;

import com.almightyalpaca.discordbot.config.Config;
import com.almightyalpaca.discordbot.config.ConfigFactory;
import com.almightyalpaca.discordbot.config.KeyNotFoundException;
import com.almightyalpaca.discordbot.config.WrongTypeException;
import com.almightyalpaca.discordbot.util.SoundCloudUtil;
import com.almightyalpaca.discordbot.util.UnresolveableLinkException;
import com.almightyalpaca.discordbot.util.YoutubeUtil;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.mashape.unirest.http.exceptions.UnirestException;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.JDAInfo;
import net.dv8tion.jda.audio.player.FilePlayer;
import net.dv8tion.jda.audio.player.Player;
import net.dv8tion.jda.audio.player.URLPlayer;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.events.InviteReceivedEvent;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import net.dv8tion.jda.utils.InviteUtil;

public class BotMain extends ListenerAdapter
{

    public JDA api = null;

    public Config config = null;

    public File musicDir = null;

    public List<Player> list = new ArrayList<>();

    public TextChannel boundTextChannel = null;
    public VoiceChannel boundVoiceChannel = null;

    int currentFile = 1;

    public boolean pausePlayback = false;

    public BotMain()
    {
        int hour = ZonedDateTime.now().getHour();
        if (2 <= hour && hour < 8)
        {
            JOptionPane.showMessageDialog(null, "Go to sleep!", "Reminder", JOptionPane.WARNING_MESSAGE);
            System.exit(9000);
        }

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

            SoundCloudUtil.init(config.getString("integrations.soundcloud.client_id"), config.getString("integrations.soundcloud.client_secret"));

            YoutubeUtil.init(config.getString("integrations.marshape.appKey"));
            
            System.out.println("Finished");

        }
        catch (JsonIOException | JsonSyntaxException | WrongTypeException | KeyNotFoundException | IOException | LoginException | IllegalArgumentException
                | InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(final String[] args)
    {
        new BotMain();
    }

    public void addFile(final File file) throws IOException, UnsupportedAudioFileException
    {
        System.out.println("Adding " + file);
        final Player player = new FilePlayer(file);
        addPlayer(player);
    }

    private void addPlayer(Player player)
    {
        player.setVolume(amplitude);
        this.list.add(player);
    }

    // public void addFileAfterDownload(final URL url) throws IOException, UnsupportedAudioFileException {
    // // get file remote information
    // DownloadInfo info = new DownloadInfo(url);
    // info.extract();
    // info.enableMultipart();
    // info.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
    //
    // File file = new File(musicDir, String.valueOf(currentFile++) + " " + info.getContentFilename());
    // file.deleteOnExit();
    //
    // // initialize wget object
    // WGet w = new WGet(info, file);
    // // single thread download. will return here only when file download
    // // is complete (or error raised).
    // w.download();
    //
    // addFile(file);
    // }

    public void addURL(final URL url) throws IOException, UnsupportedAudioFileException
    {
        System.out.println("Adding " + url);
        final Player player = new URLPlayer(api, url, 1024 * 1024)
        {
            @Override
            public void stop()
            {
                System.out.println("Finished track !");
                super.stop();
                list.remove(this);
                if (!pausePlayback)
                {
                    BotMain.this.play();
                }
            }
        };
        System.out.println("Adding now !");
        addPlayer(player);
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

                        event.getJDA().getAudioManager().openAudioConnection(boundVoiceChannel);
                        break;
                    }
                }
                if (this.boundVoiceChannel == null)
                {
                    channel.sendMessage("Voice channel not found!");
                    return;
                }
            }
            else if (text.startsWith("play"))
            {
                if (this.list.size() == 0)
                {
                    channel.sendMessage("Queue empty !");
                }
                else
                {
                    this.play();
                }
            }
            else if (text.startsWith("pause"))
            {
                pause();
            }
            else if (text.startsWith("skip"))
            {
                if (this.list.size() <= 1)
                {
                    channel.sendMessage("Queue empty !");
                    this.stop();
                }
                else
                {
                    this.skip();
                }
            }
            else if (text.startsWith("restart"))
            {
                restart();
            }
            else if (text.startsWith("stop"))
            {
                stop();
            }
            else if (text.startsWith("help"))
            {
                String output = "";
                output += "The **first** music bot for discord made in **pure java** !" + "\n";
                output += "**Commands:**" + "\n";
                output += "**" + this.config.getString("prefix") + " join [channel name]** Join a voice channel" + "\n";
                output += "**" + this.config.getString("prefix") + " leave** Leave the current voice channel" + "\n";
                output += "**" + this.config.getString("prefix") + " add [link]** Add a sound file to the queue" + "\n";
                output += "**" + this.config.getString("prefix") + " play** Start/resume the music playback" + "\n";
                output += "**" + this.config.getString("prefix") + " pause** Pause the music playback" + "\n";
                output += "**" + this.config.getString("prefix") + " restart** Restart the current song" + "\n";
                output += "**" + this.config.getString("prefix") + " skip** Skip the current song" + "\n";
                output += "**" + this.config.getString("prefix") + " volume [%]** Set the volume" + "\n";
                output += "**" + this.config.getString("prefix") + " stop** Stop the music playback";
                channel.sendMessage(output);
            }
            else if (text.startsWith("add "))
            {
                text = text.replace("add ", "");
                final String link = text.substring(0, text.indexOf(" ") == -1 ? text.length() : text.indexOf(" "));
                try
                {
                    if (link.contains("youtube"))
                    {
                        addURL(YoutubeUtil.getYoutubeURL(link));
                    }
                    else if (link.contains("youtu.be"))
                    {
                        addURL(YoutubeUtil.getYoutubeURL(Utils.expand(link)));
                    }
                    else if (link.contains("soundcloud"))
                    {
                        this.addURL(SoundCloudUtil.getSoundCloudTrackURL(link));
                    }
                    else
                    {
                        this.addURL(new URL(Utils.expand(link)));
                    }
                }
                catch (final IOException | UnsupportedAudioFileException | UnirestException | UnresolveableLinkException e)
                {
                    e.printStackTrace();
                }
            }
            else if (text.startsWith("volume "))
            {
                text = text.replace("volume ", "");
                final String volume = text.substring(0, text.indexOf(" ") == -1 ? text.length() : text.indexOf(" "));

                try
                {
                    float vol = Float.parseFloat(volume);
                    this.setVolume(vol);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else if (text.startsWith("shutdown"))
            {
                if (event.getAuthor().getId().contentEquals(config.getString("ownerId")))
                {
                    System.exit(0);
                }
            }
            else if (text.startsWith("leave"))
            {
                stop();
                event.getJDA().getAudioManager().closeAudioConnection();
                boundTextChannel = null;
                boundVoiceChannel = null;
            }
        }
    }

    private void restart()
    {
        if (this.list.size() == 0)
        {
            return;
        }
        else
        {
            list.get(0).restart();
        }
    }

    private void setVolume(float volume)
    {
        this.amplitude = volume;
        for (Player player : list)
        {
            player.setVolume(volume);
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
        System.out.println("Started playing !");
        pausePlayback = false;
        if (this.list.size() == 0)
        {
            return;
        }
        else
        {
            api.getAudioManager().setSendingHandler(list.get(0));
            list.get(0).play();
        }
    }

    public void skip()
    {
        if (isPlaying())
        {
            list.get(0).stop();
        }
    }

    private boolean isPlaying()
    {
        if (this.list.size() == 0)
        {
            return false;
        }
        else
        {
            return list.get(0).isPlaying();
        }
    }

    public void pause()
    {
        if (this.list.size() == 0)
        {
            return;
        }
        else
        {
            list.get(0).pause();
        }
    }

    public void stop()
    {
        pausePlayback = true;
        if (this.list.size() == 0)
        {
            return;
        }
        else
        {
            list.get(0).stop();
        }
    }

}
