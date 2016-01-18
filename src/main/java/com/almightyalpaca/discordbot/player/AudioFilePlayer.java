package com.almightyalpaca.discordbot.player;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.json.JSONObject;

import com.almightyalpaca.discordbot.BotMain;
import com.sun.jna.ptr.PointerByReference;

import net.dv8tion.jda.audio.AudioPacket;
import net.dv8tion.jda.audio.AudioWebSocket;
import net.tomp2p.opuswrapper.Opus;

public class AudioFilePlayer extends AudioPlayer
{

    BotMain bot;

    File file;

    Thread thread = new Thread(new Runnable()
    {

        @Override
        public void run()
        {
            System.out.println("starting talkings");
            final JSONObject obj = new JSONObject().put("op", 5).put("d", new JSONObject().put("speaking", true).put("delay", 0));
            AudioWebSocket.socket.sendText(obj.toString());

            final long time = System.currentTimeMillis();
            System.out.println("starting test");

            final int OPUS_SAMPLE_RATE = 48000; // (Hz) We want to use the
            // highest of qualities! All the bandwidth!
            final int OPUS_FRAME_SIZE = 960; // An opus frame size of 960 at 48000hz represents 20 milliseconds of audio.
            final int OPUS_FRAME_TIME_AMOUNT = 20; // This is 20 milliseconds. We are only dealing with 20ms opus packets.
            final int OPUS_CHANNEL_COUNT = 2; // We want to use stereo. If the audio given is mono, the encoder promotes it to Left and Right mono (stereo that is the same on both sides)

            final IntBuffer error = IntBuffer.allocate(4);
            final PointerByReference opusEncoder = Opus.INSTANCE.opus_encoder_create(OPUS_SAMPLE_RATE, OPUS_CHANNEL_COUNT, Opus.OPUS_APPLICATION_AUDIO, error);
            AudioInputStream in = null;
            AudioInputStream din = null;
            try
            {
                in = AudioSystem.getAudioInputStream(AudioFilePlayer.this.file);
                final AudioFormat baseFormat = in.getFormat();
                final AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), // We want the highest possibly quality. If the original was 8 or 16 bits, it will still have that quality.
                        baseFormat.getSampleSizeInBits() != -1 ? baseFormat.getSampleSizeInBits() : 32, baseFormat.getChannels(), baseFormat.getFrameSize() != -1 ? baseFormat.getFrameSize() : 2 * baseFormat.getChannels(), baseFormat.getFrameRate(), true);
                din = AudioSystem.getAudioInputStream(decodedFormat, in);

                char seq = 0; // Sequence of audio packets. Used to determine the order of the packets.
                int timestamp = 0; // Used to sync up our packets within the same timeframe of other people talking.
                final long start = System.currentTimeMillis(); // Debugging. Just for checking how long this took.
                long lastFrameSent = start; // Used to make sure we only send 1 audio packet per 20 milliseconds. Each packet contains 20ms of audio.

                /////////////////////////////////////////////
                /// needed for encoding
                //// START////
                int bytesRead = 0;
                byte[] nonEncoded = new byte[OPUS_FRAME_SIZE * decodedFormat.getFrameSize()];
                // byte[] nonEncoded = new byte[960 *
                // decodedFormat.getFrameSize()];
                while ((bytesRead = din.read(nonEncoded, 0, nonEncoded.length)) > 0)
                {
                    final ShortBuffer nonEncodedBuffer = ShortBuffer.allocate(bytesRead / 2);
                    final ByteBuffer encoded = ByteBuffer.allocate(4096);
                    for (int i = 0; i < bytesRead; i += 2)
                    {
                        final int firstByte = (0x000000FF & nonEncoded[i]); // Promotes to int and handles the fact that it was unsigned.
                        final int secondByte = (0x000000FF & nonEncoded[i + 1]); // Combines the 2 bytes into a short. Opus deals with unsigned shorts, not bytes.
                        final short toShort = (short) ((firstByte << 8) | secondByte);

                        nonEncodedBuffer.put(toShort);
                    }
                    nonEncodedBuffer.flip();

                    // TODO: check for 0 / negative value for error.
                    final int result = Opus.INSTANCE.opus_encode(opusEncoder, nonEncodedBuffer, OPUS_FRAME_SIZE, encoded, encoded.capacity());
                    // nonEncoded = new byte[960 *
                    // decodedFormat.getFrameSize()];
                    nonEncoded = new byte[OPUS_FRAME_SIZE * decodedFormat.getFrameSize()];

                    // ENCODING STOPS HERE

                    final byte[] audio = new byte[result];
                    encoded.get(audio);
                    final AudioPacket audioPacket = new AudioPacket(seq, timestamp, AudioWebSocket.ssrc, audio);

                    AudioWebSocket.udpSocket.send(audioPacket.asUdpPacket(AudioWebSocket.address));

                    if ((seq + 1) > Character.MAX_VALUE)
                    {
                        seq = 0;
                    } else
                    {
                        seq++;
                    }

                    timestamp += OPUS_FRAME_SIZE; // Time required to fulfill the 20 millisecond interval wait.
                    final long sleepTime = OPUS_FRAME_TIME_AMOUNT - (System.currentTimeMillis() - lastFrameSent);
                    if (sleepTime > 0)
                    {
                        Thread.sleep(sleepTime);
                    }
                    lastFrameSent = System.currentTimeMillis();
                    while (pause)
                    {
                        Thread.sleep(OPUS_FRAME_TIME_AMOUNT);
                    }
                    if (AudioFilePlayer.this.stop || AudioFilePlayer.this.skip)
                    {
                        break;
                    }
                }
                System.out.println("Time: " + (System.currentTimeMillis() - start) + "ms");

            } catch (final UnsupportedAudioFileException e)
            {
                e.printStackTrace();
            } catch (final IOException e)
            {
                e.printStackTrace();
            } catch (final InterruptedException e)
            {
                e.printStackTrace();
            }

            System.out.println("finished test. Time: " + (System.currentTimeMillis() - time) + "ms");

            final JSONObject obj2 = new JSONObject().put("op", 5).put("d", new JSONObject().put("speaking", false).put("delay", 0));
            AudioWebSocket.socket.sendText(obj2.toString());

            AudioFilePlayer.this.bot.list.remove(AudioFilePlayer.this);

            if (!AudioFilePlayer.this.stop)
            {
                AudioFilePlayer.this.bot.play();
            }

        }
    }, "Player Thread");

    public AudioFilePlayer(final BotMain bot, final File file)
    {
        this.bot = bot;
        this.file = file;

    }

    @Override
    public void play()
    {
        this.thread.setDaemon(true);
        this.thread.start();
    }

}
