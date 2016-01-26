//package com.almightyalpaca.discordbot.player;
//
//import java.io.BufferedInputStream;
//import java.io.IOException;
//import java.net.URL;
//import java.net.URLConnection;
//
//import javax.sound.sampled.AudioSystem;
//import javax.sound.sampled.UnsupportedAudioFileException;
//
//import com.almightyalpaca.discordbot.BotMain;
//
//public class AudioUrlPlayer extends AbstractAudioPlayer
//{
//
//    BotMain bot;
//    URL url;
//
//    Thread thread = new Thread(new Runnable()
//    {
//        @Override
//        public void run()
//        {
//            try
//            {
//                URLConnection connection = url.openConnection();
//                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
//                
//                BufferedInputStream bufferedStream = new BufferedInputStream(connection.getInputStream(), 16 * 1024);
//                
//                play(AudioSystem.getAudioInputStream(bufferedStream));
//            } catch (UnsupportedAudioFileException | IOException e)
//            {
//                e.printStackTrace();
//            }
//
//        }
//    }, "Player Thread");
//
//    public AudioUrlPlayer(final BotMain bot, final URL url)
//    {
//        super(bot);
//        this.url = url;
//
//    }
//
//    @Override
//    public void play()
//    {
//        this.thread.setDaemon(true);
//        this.thread.start();
//    }
//
//}
