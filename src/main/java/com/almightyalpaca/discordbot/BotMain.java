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
import com.almightyalpaca.discordbot.player.AudioFilePlayer;
import com.almightyalpaca.discordbot.player.AudioPlayer;
import com.almightyalpaca.discordbot.player.AudioUrlPlayer;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

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

public class BotMain extends ListenerAdapter {
	
	public JDA			api;
						
	public Config			config;
						
	File					musicDir;
						
	public List<AudioPlayer>	list			= new ArrayList<>();
									
	public TextChannel		boundTextChannel;
						
	public VoiceChannel		boundVoiceChannel;
	int					currentFile	= 1;
									
	public BotMain() {
		try {
			this.musicDir = new File("music");
			this.musicDir.mkdirs();
			
			this.config = ConfigFactory.getDefaultConfig();
			
			// Initialize Discord API
			System.out.println("Initialzing JDA " + JDAInfo.VERSION);
			
			final JDABuilder builder = new JDABuilder(this.config.getString("credentials.email"), this.config.getString("credentials.password"));
			
			if (this.config.getBoolean("proxy.use")) {
				builder.setProxy(this.config.getString("proxy.host"), this.config.getInt("proxy.port"));
			}
			builder.addListener(this);
			this.api = builder.buildBlocking();
			
			this.api.getAccountManager().setGame(">dj help");
			
			System.out.println("Finished");
			
		} catch (JsonIOException | JsonSyntaxException | WrongTypeException | KeyNotFoundException | IOException | LoginException | IllegalArgumentException | InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(final String[] args) {
		new BotMain();
	}
	
	public void addFile(final File file) throws IOException {
		final AudioPlayer player = new AudioFilePlayer(this, file);
		this.list.add(player);
	}
	
	public void addURL(final URL url) throws IOException {
		final AudioPlayer player = new AudioUrlPlayer(this, url);
		this.list.add(player);
	}
	
	@Override
	public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
		
		String text = event.getMessage().getRawContent();
		final TextChannel channel = event.getChannel();
		
		if (text.startsWith(">dj ")) {
			text = text.replace(">dj ", "");
			
			if (text.startsWith("join ")) {
				text = text.replace("join ", "");
				
				if (this.boundVoiceChannel != null) {
					channel.sendMessage("Already joined a voice channel!");
					return;
				}
				
				final String vcName = text.substring(0, text.indexOf(" ") == -1 ? text.length() : text.indexOf(" "));
				
				for (final VoiceChannel vc : event.getGuild().getVoiceChannels()) {
					if (vc.getName().contentEquals(vcName)) {
						System.out.println("Joining " + vc.getName());
						this.boundVoiceChannel = vc;
						this.boundTextChannel = channel;
						
						final JSONObject obj = new JSONObject().put("op", 4).put("d", new JSONObject().put("guild_id", event.getGuild().getId()).put("channel_id", this.boundVoiceChannel.getId()).put("self_mute", false).put("self_deaf", false));
						final WebSocketClient client = ((JDAImpl) event.getJDA()).getClient();
						client.send(obj.toString());
						break;
					}
				}
				if (this.boundVoiceChannel == null) {
					channel.sendMessage("Voice channel not found!");
					return;
				}
			} else if (text.startsWith("play")) {
				if (this.list.size() == 0) {
					channel.sendMessage("No file to play");
				} else {
					this.playFile();
				}
			} else if (text.startsWith("skip")) {
				if (this.list.size() <= 1) {
					channel.sendMessage("No more files");
					this.stop();
				} else {
					this.skip();
				}
			} else if (text.startsWith("stop")) {
				this.stop();
			} else if (text.startsWith("help")) {
				String output = "";
				output += "The **first** music bot for discord made in **pure java** !" + "\n";
				output += "**Commands:**" + "\n";
				output += "**>dj join [channel name]** join a voice channel" + "\n";
				output += "**>dj add [link]** add a sound file to the queue" + "\n";
				output += "**>dj play** Start the music playback" + "\n";
				output += "**>dj skip** Skip the current song" + "\n";
				output += "**>dj stop** Stop the music playback";
				channel.sendMessage(output);
				
			} else if (text.startsWith("add ")) {
				text = text.replace("add ", "");
				try {
					
					final URL url = new URL(text);
					
					this.addURL(url);
					
				} catch (final IOException e) {
					e.printStackTrace();
					
				}
				
			} else if (text.startsWith("shutdown")) {
				if (event.getAuthor().getId().contentEquals("107490111414882304")) {
					System.exit(0);
				}
			}
		}
		
	}
	
	@Override
	public void onInviteReceived(final InviteReceivedEvent event) {
		if (event.getMessage().getMentionedUsers().contains(this.api.getSelfInfo())) {
			InviteUtil.join(event.getInvite(), this.api);
		}
	}
	
	public void playFile() {
		if (this.list.size() == 0) {
			return;
		} else {
			this.list.get(0).play();
		}
	}
	
	public void skip() {
		if (this.list.size() == 0) {
			return;
		} else {
			this.list.get(0).skip = true;
		}
	}
	
	public void stop() {
		if (this.list.size() == 0) {
			return;
		} else {
			this.list.get(0).stop = true;
		}
	}
	
}
