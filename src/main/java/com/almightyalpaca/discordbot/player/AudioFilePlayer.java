// package com.almightyalpaca.discordbot.player;
//
// import java.io.BufferedInputStream;
// import java.io.File;
// import java.io.FileInputStream;
// import java.io.IOException;
//
// import javax.sound.sampled.AudioSystem;
// import javax.sound.sampled.UnsupportedAudioFileException;
//
// import com.almightyalpaca.discordbot.BotMain;
//
// public class AudioFilePlayer extends AbstractAudioPlayer
// {
//
// File file;
//
// Thread thread = new Thread(new Runnable()
// {
//
// @Override
// public void run()
// {
// try
// {
// BufferedInputStream bufferedStream = new BufferedInputStream(new FileInputStream(AudioFilePlayer.this.file), 16 * 1024);
// play(AudioSystem.getAudioInputStream(bufferedStream));
// } catch (UnsupportedAudioFileException | IOException e)
// {
// e.printStackTrace();
// }
// }
// }, "Player Thread");
//
// public AudioFilePlayer(final BotMain bot, final File file)
// {
// super(bot);
// this.file = file;
//
// }
//
// @Override
// public void play()
// {
// this.thread.setDaemon(true);
// this.thread.start();
// }
//
// }
