package com.almightyalpaca.discordbot;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.json.JSONObject;

import com.almightyalpaca.discordbot.config.Config;
import com.almightyalpaca.discordbot.config.ConfigFactory;
import com.almightyalpaca.discordbot.config.KeyNotFoundException;
import com.almightyalpaca.discordbot.config.WrongTypeException;
import com.github.axet.wget.WGet;
import com.github.axet.wget.info.DownloadInfo;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

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

public class BotMain extends ListenerAdapter {
	
	public JDA			api					= null;
											
	public Config		config				= null;
											
	File				musicDir			= null;
											
	public List<Player>	list				= new ArrayList<>();
											
	public TextChannel	boundTextChannel	= null;
	public VoiceChannel	boundVoiceChannel	= null;
											
	int					currentFile			= 1;
											
	public boolean		pausePlayback		= true;
											
	Thread				playlistThread		= new Thread(() -> {
												// while (true) {
												// if (this.list.size() == 0 || pausePlayback) {
												// return;
												// } else {
												// Player player = list.get(0);
												// if (player.isStopped()) {
												// list.remove(player);
												// play();
												// }
												//
												// }
												// }
											} , "playlistThread");
											
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
			
			this.api.getAccountManager().setGame(this.config.getString("prefix") + " help");
			
			System.out.println("Finished");
			
		} catch (JsonIOException | JsonSyntaxException | WrongTypeException | KeyNotFoundException | IOException | LoginException | IllegalArgumentException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(final String[] args) {
		new BotMain();
	}
	
	public void addFile(final File file) throws IOException, UnsupportedAudioFileException {
		System.out.println("Adding " + file);
		final Player player = new FilePlayer(file);
		addPlayer(player);
	}
	
	private void addPlayer(Player player) {
		player.setVolume(amplitude);
		this.list.add(player);
	}
	
	public void addFileAfterDownload(final URL url) throws IOException, UnsupportedAudioFileException {
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
	
	public void addYoutube(String id) throws IOException {
		try {
			if (id != null) {
				HttpResponse<JsonNode> response = Unirest.get("http://www.youtubeinmp3.com/fetch/?format=JSON&video=http://www.youtube.com/watch?v=" + id)// .header("Accept", "application/json")
						.asJson();
				JSONObject object = response.getBody().getObject();
				
				System.out.println(response.getHeaders());
				
				System.out.println(object);
				
				String link = object.getString("link");
				
//				String link = "http://www.youtubeinmp3.com/download/?video=https://www.youtube.com/watch?v=" + id + "&autostart=1";
				
				System.out.println("A: " + link);
				
				link = Utils.expand(link);
				
				System.out.println("B: " + link);
				
//				DownloadInfo info = new DownloadInfo(new URL(link));
//				info.extract();
//				
//				File file = new File("music", info.getContentFilename());
//				System.out.println(file);
//				file.createNewFile();
//				file.deleteOnExit();
//				
//				WGet get = new WGet(info, file);
//				
//				get.download();
//				
//				addFile(file);
				
				addURL(new URL(link));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addURL(final URL url) throws IOException, UnsupportedAudioFileException {
		System.out.println("Adding " + url);
		final Player player = new URLPlayer(api, url);
		addPlayer(player);
	}
	
	@Override
	public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
		System.out.println(event.getMessage().getMentionedUsers().isEmpty());
		
		String text = event.getMessage().getRawContent();
		
		final TextChannel channel = event.getChannel();
		
		if (text.startsWith(this.config.getString("prefix") + " ")) {
			text = text.replace(this.config.getString("prefix") + " ", "");
			if (text.startsWith("join ")) {
				text = text.replace("join ", "");
				
				if (this.boundVoiceChannel != null) {
					return;
				}
				final String vcName = text.substring(0, text.indexOf(" ") == -1 ? text.length() : text.indexOf(" "));
				
				for (final VoiceChannel vc : event.getGuild().getVoiceChannels()) {
					if (vc.getName().contentEquals(vcName)) {
						System.out.println("Joining " + vc.getName());
						this.boundVoiceChannel = vc;
						this.boundTextChannel = channel;
						
						event.getJDA().getAudioManager().openAudioConnection(boundVoiceChannel);
						break;
					}
				}
				if (this.boundVoiceChannel == null) {
					channel.sendMessage("Voice channel not found!");
					return;
				}
			} else if (text.startsWith("play")) {
				if (this.list.size() == 0) {
					channel.sendMessage("Queue empty !");
				} else {
					this.play();
				}
			} else if (text.startsWith("pause")) {
				pause();
			} else if (text.startsWith("skip")) {
				if (this.list.size() <= 1) {
					channel.sendMessage("Queue empty !");
					this.stop();
				} else {
					this.skip();
				}
			} else if (text.startsWith("test")) {
				
				VoiceChannel voiceChannel = event.getChannel().getGuild().getVoiceChannels().stream().filter(chan -> chan.getName().equalsIgnoreCase("testing") || chan.getName().equalsIgnoreCase(
						"bot-testing")).findFirst().orElse(event.getChannel().getGuild().getVoiceChannels().get(0));
				event.getJDA().getAudioManager().openAudioConnection(voiceChannel);
				try {
					addFile(new File("teastep-48000.mp3"));
				} catch (IOException | UnsupportedAudioFileException e) {
					e.printStackTrace();
				}
				playAsync();
			} else if (text.startsWith("stop")) {} else if (text.startsWith("help")) {
				String output = "";
				output += "The **first** music bot for discord made in **pure java** !" + "\n";
				output += "**Commands:**" + "\n";
				output += "**" + this.config.getString("prefix") + " join [channel name]** Join a voice channel" + "\n";
				output += "**" + this.config.getString("prefix") + " leave** Leave the current voice channel" + "\n";
				output += "**" + this.config.getString("prefix") + " add [link]** Add a sound file to the queue" + "\n";
				output += "**" + this.config.getString("prefix") + " play** Start/resume the music playback" + "\n";
				output += "**" + this.config.getString("prefix") + " pause** Pause the music playback" + "\n";
				output += "**" + this.config.getString("prefix") + " skip** Skip the current song" + "\n";
				output += "**" + this.config.getString("prefix") + " volume [%]** Set the volume" + "\n";
				output += "**" + this.config.getString("prefix") + " stop** Stop the music playback";
				channel.sendMessage(output);
			} else if (text.startsWith("add ")) {
				text = text.replace("add ", "");
				final String link = text.substring(0, text.indexOf(" ") == -1 ? text.length() : text.indexOf(" "));
				try {
					final URL url = new URL(link);
					String id = Utils.getYoutubeVideoId(url);
					if (id != null) {
						this.addYoutube(id);
					} else {
						this.addURL(url);
					}
				} catch (final IOException | UnsupportedAudioFileException e) {
					e.printStackTrace();
				}
			} else if (text.startsWith("volume ")) {
				text = text.replace("volume ", "");
				final String volume = text.substring(0, text.indexOf(" ") == -1 ? text.length() : text.indexOf(" "));
				
				try {
					float vol = Float.parseFloat(volume);
					this.setVolume(vol);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (text.startsWith("shutdown")) {
				if (event.getAuthor().getId().contentEquals(config.getString("ownerId"))) {
					System.exit(0);
				}
			} else if (text.startsWith("leave")) {
				stop();
				event.getJDA().getAudioManager().closeAudioConnection();
				boundTextChannel = null;
				boundVoiceChannel = null;
			}
		}
	}
	
	private void playAsync() {
		new Thread(() -> {
			try {
				while (!api.getAudioManager().connected()) {
					TimeUnit.SECONDS.sleep(1);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			play();
		}).start();
	}
	
	private void setVolume(float volume) {
		this.amplitude = volume;
		for (Player player : list) {
			player.setVolume(volume);
		}
	}
	
	@Override
	public void onInviteReceived(final InviteReceivedEvent event) {
		if (event.getMessage().getMentionedUsers().contains(this.api.getSelfInfo())) {
			InviteUtil.join(event.getInvite(), this.api);
		}
	}
	
	public float amplitude = 1.0F;
	
	public void play() {
		if (!api.getAudioManager().connected()) {
			playAsync();
			return;
		}
		System.out.println("Started playing !");
		pausePlayback = false;
		if (this.list.size() == 0) {
			return;
		} else {
			api.getAudioManager().setSendingHandler(list.get(0));
			list.get(0).play();
		}
	}
	
	public void skip() {
		if (this.list.size() == 0) {
			return;
		} else {
			list.get(0).stop();
		}
	}
	
	public void pause() {
		if (this.list.size() == 0) {
			return;
		} else {
			list.get(0).pause();
		}
	}
	
	public void stop() {
		pausePlayback = true;
		if (this.list.size() == 0) {
			return;
		} else {
			list.get(0).stop();
		}
	}
	
}
