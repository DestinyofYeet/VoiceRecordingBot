package de.uwuwhatsthis.voiceRecorderBotForClara.audio;

import de.uwuwhatsthis.voiceRecorderBotForClara.customObjects.Cache;
import de.uwuwhatsthis.voiceRecorderBotForClara.customObjects.Embed;
import de.uwuwhatsthis.voiceRecorderBotForClara.main.Main;
import de.uwuwhatsthis.voiceRecorderBotForClara.utils.helper;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.soap.Text;
import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

public class ReceiveAndHandleAudioForChannel implements Runnable{
    private final VoiceChannel voiceChannel;
    private final ArrayList<byte[]> voiceData = new ArrayList<>();
    private String fileName;
    private boolean shouldDelete = true;

    public ReceiveAndHandleAudioForChannel(VoiceChannel voiceChannel){
        this.voiceChannel = voiceChannel;
        fileName = "audio_" + voiceChannel.getId() + ".wav";

        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try{
            voiceChannel.getGuild().getAudioManager().openAudioConnection(voiceChannel);
        } catch (Exception e){
            System.err.println("Could not join voice channel: \"" + voiceChannel.getName() + "\": " + e.getMessage());
            return;
        }

        // System.out.println("Successfully joined vc");

        voiceChannel.getGuild().getAudioManager().setReceivingHandler(new AudioReceiveHandler() {

            @Override
            public boolean canReceiveCombined() {
                return true;
            }


            @Override
            public void handleCombinedAudio(@NotNull CombinedAudio combinedAudio) {
                voiceData.add(combinedAudio.getAudioData(1.0));
            }

        });
    }

    public void saveAudio(){
        File file = new File("data/" + fileName);

        byte[] byteData = helper.convertObjectArrayToByteArray(voiceData);

        try {
           helper.getWavFile(file, byteData);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Cache cache = Main.cacheManager.getCacheForServer(voiceChannel.getGuild().getId());
        TextChannel channel = helper.getTextChannelById(voiceChannel.getGuild(), cache.getRecordingLogChannelId());

        if (channel != null){
            try{
                channel.sendFile(new File("data/" + fileName), LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy-KK__mm a")) + ".wav").complete();
            } catch (IllegalArgumentException e){
                // file too big
                compressFileToMp3(channel);
            }

        } else {
            System.err.println("Log channel has not been set, file will not be sent");
        }


        delFile();

    }

    private void compressFileToMp3(TextChannel channel){
        Runtime runtime = Runtime.getRuntime();

        Process ffmpeg;

        String execString = "ffmpeg -y -i \"data/" + fileName + "\" \"data/" + fileName.replace(".wav", ".mp3") + "\"";
        // System.out.println(execString);
        try {
            ffmpeg = runtime.exec(execString);
        } catch (IOException e) {
            e.printStackTrace();
            channel.sendMessageEmbeds(new Embed("Error", "Could not run ffmpeg to compress file! Not deleting file!", Color.RED).build()).queue();
            shouldDelete = false;
            return;
        }

        try {
            ffmpeg.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String output = null, error = null;

        try {
            output = IOUtils.toString(ffmpeg.getInputStream(), Charset.defaultCharset());
            error = IOUtils.toString(ffmpeg.getErrorStream(), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ffmpeg.exitValue() != 0){
            channel.sendMessageEmbeds(new Embed("Error", "FFMPEG exited with non-zero exit code: " + ffmpeg.exitValue() + "\n```" + error + "```", Color.RED).build()).queue();
            delFile();
            return;
        }


        fileName = fileName.replace(".wav", ".mp3");

        try{
            channel.sendFile(new File("data/" + fileName), LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy-KK__mm a")) + ".mp3").complete();
        } catch (IllegalArgumentException e){
            // file too big
            channel.sendMessageEmbeds(new Embed("Error", "File too big to send! Retrieve it locally!", Color.RED).build()).queue();
        }
    }

    private void delFile(){
        try {
            if (shouldDelete)
                Files.delete(Paths.get("data/" + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
