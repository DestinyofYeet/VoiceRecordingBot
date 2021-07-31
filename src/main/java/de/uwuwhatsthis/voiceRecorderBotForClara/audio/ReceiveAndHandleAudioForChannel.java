package de.uwuwhatsthis.voiceRecorderBotForClara.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.uwuwhatsthis.voiceRecorderBotForClara.customObjects.*;
import de.uwuwhatsthis.voiceRecorderBotForClara.main.Main;
import de.uwuwhatsthis.voiceRecorderBotForClara.messageReactionStuff.ReactionEmotes;
import de.uwuwhatsthis.voiceRecorderBotForClara.utils.Constants;
import de.uwuwhatsthis.voiceRecorderBotForClara.utils.helper;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.soap.Text;
import java.awt.*;
import java.io.*;
import java.net.SocketTimeoutException;
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
    private MessageReceivedEvent event;
    private Debugger debugger;
    private File fileToUpload;

    public ReceiveAndHandleAudioForChannel(VoiceChannel voiceChannel, MessageReceivedEvent event){
        this.voiceChannel = voiceChannel;
        this.event = event;
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

        debugger =  Main.debugManager.getDebugger(voiceChannel.getGuild());


        // getting consent from all members before starting the recording
        for (Member member: voiceChannel.getMembers()){
            if (member.getUser().isBot()) continue;
            member.getUser().openPrivateChannel().queue(privateChannel -> {
                new ConsentMessage(privateChannel, voiceChannel, debugger);
            }, e -> {debugger.error(e.getMessage());});
        }

        debugger.debug("Sent out all consent messages!");
        // event.getChannel().sendMessageEmbeds(new Embed("Consent", "Sent out all consent messages!", Color.GREEN).build()).queue();

        // waiting 60 seconds for users to consent, then proceed to kick everyone from the vc that did not consent
        try {
            Thread.sleep(60*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // event.getChannel().sendMessageEmbeds(new Embed("Consent", "Kicking all users that did not consent from the voice channel!", Color.GREEN).build()).queue();

        for (Member member: voiceChannel.getMembers()){
            if (member.getUser() == event.getJDA().getSelfUser()) continue;

            if (member.getUser().isBot()) continue;

            if (!helper.hasConsented(voiceChannel, member.getUser())) {
                voiceChannel.getGuild().moveVoiceMember(member, null).complete();
            }

        }


        // loading & playing the pre-recorded message
        PlayerManager playerManager = PlayerManager.getInstance();
        playerManager.loadAndPlay(voiceChannel.getGuild(), Main.config.getPreMessagePath(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
               debugger.debug("Successfully played the pre-recording message");
               // debugger.debug(audioTrack.getState().toString());

               playerManager.play(playerManager.getGuildMusicManager(event.getGuild()), audioTrack);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {

            }

            @Override
            public void noMatches() {
                debugger.error("Could not find the pre-recording message");
            }

            @Override
            public void loadFailed(FriendlyException e) {
                debugger.error("Could not load pre-recording message");
                e.printStackTrace();
            }
        });

        // debugger.debug("Audio track: " + playerManager.getGuildMusicManager(voiceChannel.getGuild()).player.getPlayingTrack());
        while (playerManager.getGuildMusicManager(voiceChannel.getGuild()).player.getPlayingTrack() != null){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        event.getChannel().sendMessageEmbeds(new Embed("Recording", "The bot is now recording in channel " + voiceChannel.getAsMention() + "!", Color.GREEN).build()).queue();

        helper.setRecordingStatus(voiceChannel.getGuild(), Status.RECORDING);

        // recording stuff
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
        helper.setRecordingStatus(voiceChannel.getGuild(), Status.IDLE);
        File file = fileToUpload =  new File("data/" + fileName);

        byte[] byteData = helper.convertObjectArrayToByteArray(voiceData);

        try {
           helper.getWavFile(file, byteData);
           debugger.debug("Saved file to " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Cache cache = Main.cacheManager.getCacheForServer(voiceChannel.getGuild().getId());
        TextChannel channel = helper.getTextChannelById(voiceChannel.getGuild(), cache.getRecordingLogChannelId());

        if (channel != null){
            compressFileToMp3(channel);

            try{
                channel.sendFile(new File("data/" + fileName), LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy-KK__mm a")) + ".mp3").complete();
            } catch (IllegalArgumentException e){
                // file too big -> upload to cloud

                try {
                    String newFileName = voiceChannel.getName() + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy-KK__mm a")) + ".mp3";
                    Files.copy(Paths.get("data/" + fileName), Paths.get("data/" + newFileName));
                    delFile(); // delete .mp3 file
                    fileName = newFileName;
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

                FileUpload fileUpload = null;
                try{
                    fileUpload = new FileUpload(new File("data/" + fileName), debugger);
                } catch (IllegalStateException e1){
                    int i = 1;
                    while (i <= 10){
                        try{
                            fileUpload = new FileUpload(new File("data/" + fileName), debugger);
                        } catch (IllegalStateException e2){
                            i++;
                        }
                    }
                }

                if (fileUpload == null){
                    channel.sendMessageEmbeds(new Embed("Error", "Cannot upload file! Maximum tries (10) exceeded!", Color.RED).build()).queue();
                    shouldDelete = false;
                    return;
                }

                channel.sendMessage(fileUpload.getFileUrl() != null ? fileUpload.getFileUrl() : "There was an error when creating the link to the uploaded file!").queue();
            }
        }

        delFile(); // delete final .mp3 file

    }

    private void compressFileToMp3(TextChannel channel){
        Runtime runtime = Runtime.getRuntime();

        Process ffmpeg;

        String execString = "ffmpeg -y -i \"" + fileToUpload.getAbsolutePath() + "\" \"" + Paths.get(fileToUpload.getAbsolutePath()).getParent().toAbsolutePath() + "/" + fileName.replace(".wav", ".mp3") + "\"";
        // System.out.println(execString);
        try {
            debugger.debug("Running ffmpeg command: " + execString);
            ffmpeg = runtime.exec(execString);
        } catch (IOException e) {
            debugger.error("ERROR RUNNING FFMPEG");
            e.printStackTrace();
            channel.sendMessageEmbeds(new Embed("Error", "Could not run ffmpeg to compress file! Not deleting file!", Color.RED).build()).queue();
            shouldDelete = false;
            return;
        }


        while(ffmpeg.isAlive()){
            debugger.debug("FFMPEG is alive: " + ffmpeg.isAlive());
            // debugger.debug(helper.getInputStreamContent(ffmpeg.getErrorStream())); // writes output to stderr ?????

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        debugger.debug("FFMPEG has finished");

        String output = null, error = null;

        try {
            output = IOUtils.toString(ffmpeg.getInputStream(), Charset.defaultCharset());
            error = IOUtils.toString(ffmpeg.getErrorStream(), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }

        debugger.debug("FFMPEG exit code: " + ffmpeg.exitValue());

        if (ffmpeg.exitValue() != 0){
            try{
                channel.sendMessageEmbeds(new Embed("Error", "FFMPEG exited with non-zero exit code: " + ffmpeg.exitValue() + "\n```" + error + "```", Color.RED).build()).queue();
            } catch (IllegalArgumentException e){
                // embed too big
                channel.sendMessageEmbeds(new Embed("Error", "FFMPEG exited with non-zero exit code: " + ffmpeg.exitValue(), Color.RED).build()).queue();
                helper.sendFile(channel, error, "ffmpeg-error.txt");
            }

            delFile();
            return;
        }
        delFile(); // delete original .wav file


        fileName = fileName.replace(".wav", ".mp3");

        debugger.debug("Finished compressToMp3()");
    }

    private void delFile(){
        debugger.debug("delFile() : " + shouldDelete);
        try {
            if (shouldDelete)
                Files.delete(Paths.get("data/" + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
