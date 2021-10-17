package de.uwuwhatsthis.voiceRecorderBotForClara.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.uwuwhatsthis.voiceRecorderBotForClara.customObjects.*;
import de.uwuwhatsthis.voiceRecorderBotForClara.main.Main;
import de.uwuwhatsthis.voiceRecorderBotForClara.utils.helper;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ReceiveAndHandleAudioForChannel implements Runnable{
    private final VoiceChannel voiceChannel;
    private final ArrayList<byte[]> voiceData = new ArrayList<>();
    private String fileName;
    private boolean shouldDelete = true;
    private MessageReceivedEvent event;
    private Debugger debugger;
    private File fileToUpload;
    private File audioFile;
    private boolean ffmpegSuccessful = false;
    private final Thread thread;
    private boolean hasStartedRecording;

    public ReceiveAndHandleAudioForChannel(VoiceChannel voiceChannel, MessageReceivedEvent event){
        this.voiceChannel = voiceChannel;
        this.event = event;

        fileName = "audio_" + voiceChannel.getId() + ".wav";
        hasStartedRecording = false;

        thread = new Thread(this);
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
            // the recording was stopped while the time wasn't over yet
            return;
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
        debugger.debug("Pre message url: " + Main.config.getPreMessageURL());
        playerManager.loadAndPlay(voiceChannel.getGuild(), Main.config.getPreMessageURL(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
               debugger.debug("Successfully playing the pre-recording message");
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

        try {
            Thread.sleep(1000); // wait for the player to load the message
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
        this.hasStartedRecording = true;

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
        if (!this.hasStartedRecording){
            thread.interrupt();
            return;
        }
        audioFile = new File("data/" + fileName);

        byte[] byteData = helper.convertObjectArrayToByteArray(voiceData);

        try {
           helper.getWavFile(audioFile, byteData);
           debugger.debug("Saved file to " + audioFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Cache cache = Main.cacheManager.getCacheForServer(voiceChannel.getGuild().getId());
        TextChannel channel = helper.getTextChannelById(voiceChannel.getGuild(), cache.getRecordingLogChannelId());

        if (channel != null){
            compressFileToMp3(channel);

            String newFileName = voiceChannel.getName() + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy-KK_mm a")) + ".mp3";
            newFileName = newFileName.replaceAll(" ", "_");

            debugger.debug("Uploading file");

            try{
                channel.sendFile(audioFile, newFileName).complete();
                debugger.debug("Uploaded file to textchannel");
            } catch (IllegalArgumentException e){
                // file too big -> upload to cloud

                FileUpload fileUpload = null;
                try{
                    fileUpload = new FileUpload(audioFile, debugger);
                } catch (IllegalStateException e1){
                    debugger.debug("Failed to upload file to cloud...Entering while loop");
                    int i = 0;
                    while (i < 10){
                        try{
                            debugger.debug("Upload attempt " + i + ".....");
                            fileUpload = new FileUpload(audioFile, debugger);
                            debugger.debug("Upload attempt " + i + ": Successful");
                        } catch (IllegalStateException e2){
                            debugger.debug("Upload attempt " + i + ": Failed...Retrying");
                            i++;
                        }
                    }
                    debugger.debug("Maximum tries reached!");
                }

                if (fileUpload == null){
                    debugger.debug("Failed to upload file to cloud!");
                    channel.sendMessageEmbeds(new Embed("Error", "Cannot upload file! Maximum tries (10) exceeded!", Color.RED).build()).queue();
                    return;

                } else{
                    debugger.debug("Uploaded file to cloud!");
                }

                channel.sendMessage(fileUpload.getFileUrl() != null ? fileUpload.getFileUrl() : "There was an error when creating the link to the uploaded file!").queue();
            }
        }

        delFile(); // delete final after upload

    }

    /**
     * Compresses the current .wav file to a .mp3 file
     * @param channel The channel to send messages generated in this function in
     */
    private void compressFileToMp3(TextChannel channel){

        Process ffmpeg;
        ProcessBuilder ffmpegProcessBuilder = new ProcessBuilder("ffmpeg", "-y", "-i", audioFile.getAbsolutePath(), Paths.get(audioFile.getAbsolutePath()).getParent().toAbsolutePath() + "/" + fileName.replace(".wav", ".mp3"));

        String execString = "ffmpeg -y -i \"" + audioFile.getAbsolutePath() + "\" \"" + Paths.get(audioFile.getAbsolutePath()).getParent().toAbsolutePath() + "/" + fileName.replace(".wav", ".mp3") + "\"";
        // System.out.println(execString);

        audioFileCheck();

        try {
            debugger.debug("Running ffmpeg command: " + execString);
            debugger.debug("Process builder commands: " + ffmpegProcessBuilder.command().toString());
            ffmpeg = ffmpegProcessBuilder.start();
        } catch (IOException e) {
            debugger.error("ERROR RUNNING FFMPEG");
            e.printStackTrace();
            channel.sendMessageEmbeds(new Embed("Error", "Could not run ffmpeg to compress file! Not deleting file!", Color.RED).build()).queue();
            return;
        }

        long secondsTimeSpentCompiling = 0;
        while(ffmpeg.isAlive()){
            if (secondsTimeSpentCompiling >= 10*60){
                debugger.error("Compilation took over 10 minutes, killing process!");
                ffmpeg.destroyForcibly();
                try {
                    ffmpeg.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            }
            debugger.debug("FFMPEG is alive: " + ffmpeg.isAlive());
            // debugger.debug(helper.getInputStreamContent(ffmpeg.getErrorStream())); // writes output to stderr ?????

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            secondsTimeSpentCompiling += 1;
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

            ffmpegError(channel, error, ffmpeg.exitValue());
            return;
        }
        ffmpegSuccessful = true;
        delFile(); // delete original .wav file


        fileName = fileName.replace(".wav", ".mp3");
        audioFile = new File("data/" + fileName);

        audioFileCheck();

        debugger.debug("Finished compressToMp3()");
    }

    private void delFile(){
        debugger.debug("delFile() : " + shouldDelete);
        if (shouldDelete){
            boolean result = audioFile.delete();
            debugger.debug((result ? "Successfully deleted " : "Failed to delete ") + " file " + audioFile.getAbsolutePath());
        }
        shouldDelete = true;
    }

    private void audioFileCheck(){
        debugger.debug("Audiofile: " + audioFile.getAbsolutePath());
        debugger.debug("Audiofile exists: " + (audioFile.exists() ? "yes" : "no"));
        debugger.debug("Audiofile readable: " + (audioFile.canRead() ? "yes": "no"));
        debugger.debug("Audiofile writeable: " + (audioFile.canWrite() ? "yes": "no"));
    }

    private synchronized void ffmpegError(MessageChannel channel, String error, int exitValue){
        try{
            channel.sendMessageEmbeds(new Embed("Error", "FFMPEG exited with non-zero exit code: " + exitValue + "\n```" + error + "```", Color.RED).build()).queue();
        } catch (IllegalArgumentException e){
            // embed too big
            channel.sendMessageEmbeds(new Embed("Error", "FFMPEG exited with non-zero exit code: " + exitValue, Color.RED).build()).queue();
            helper.sendFile(channel, error, "ffmpeg-error.txt");
        }
    }
}
