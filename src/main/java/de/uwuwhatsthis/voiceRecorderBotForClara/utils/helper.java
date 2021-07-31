package de.uwuwhatsthis.voiceRecorderBotForClara.utils;

import de.uwuwhatsthis.voiceRecorderBotForClara.customObjects.Status;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class helper {

    public static VoiceChannel getVoiceChannelById(Guild guild, String input){
        if (input == null) return null;
        VoiceChannel vc = null;

        try{
            vc = guild.getVoiceChannelById(input);
        } catch (NumberFormatException noted){
            try{
                input = input.split("#")[1];
                input = input.replace(">", "");
                vc = guild.getVoiceChannelById(input);

            } catch (IndexOutOfBoundsException noted2){
                return null;
            }

        }

        return vc;
    }

    public static TextChannel getTextChannelById(Guild guild, String input){
        if (input == null) return null;
        TextChannel vc = null;

        try{
            vc = guild.getTextChannelById(input);
        } catch (NumberFormatException noted){
            try{
                input = input.split("#")[1];
                input = input.replace(">", "");
                vc = guild.getTextChannelById(input);

            } catch (IndexOutOfBoundsException noted2){
                return null;
            }

        }

        return vc;
    }

    public static byte[] convertObjectArrayToByteArray(List<byte[]> data){
        int size = 0;
        for (byte[] bs : data) {
            size+=bs.length;
        }
        byte[] decodedData = new byte[size];
        int i=0;
        for (byte[] bs : data) {
            for (int j = 0; j < bs.length; j++) {
                decodedData[i++]=bs[j];
            }
        }

        return decodedData;
    }

    public static void getWavFile(File outFile, byte[] decodedData) throws IOException {
        AudioFormat format = new AudioFormat(48_000, 16, 2, true, true);
        AudioSystem.write(new AudioInputStream(new ByteArrayInputStream(
                decodedData), format, decodedData.length), AudioFileFormat.Type.WAVE, outFile);
    }

    public static void setRecordingStatus(Guild guild, Status status){
        Member bot = guild.retrieveMember(guild.getJDA().getSelfUser()).complete();
        if (bot == null){
            System.err.println("Somehow could not get the bot itself per guild.retrieveMember() for the guild \"" + guild.getName() + "\"");
            return;
        }

        bot.modifyNickname(status.getText()).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_PERMISSIONS, e -> {
            System.err.println("No permissions to modify the nickname on guild \"" + guild.getName() + "\"!");
        }));
    }

    public static boolean hasConsented(VoiceChannel voiceChannel, User member){
        if (!Constants.USER_ALLOWED_IN_CHANNEL.containsKey(voiceChannel)){
            return false;
        }

        List<Long> userAllowed = Constants.USER_ALLOWED_IN_CHANNEL.get(voiceChannel);

       return userAllowed.contains(member.getIdLong());
    }

    public synchronized static void addMemberToAllowedChannelList(VoiceChannel voiceChannel, User member){
        if (!Constants.USER_ALLOWED_IN_CHANNEL.containsKey(voiceChannel)){
            Constants.USER_ALLOWED_IN_CHANNEL.put(voiceChannel, new ArrayList<Long>(){{
                add(member.getIdLong());
            }});
            return;
        }

        ArrayList<Long> list = Constants.USER_ALLOWED_IN_CHANNEL.get(voiceChannel);

        list.add(member.getIdLong());

        Constants.USER_ALLOWED_IN_CHANNEL.put(voiceChannel, list);
    }

    public synchronized static void removeMemberFromAllowedChannelList(VoiceChannel voiceChannel, User member){
        if (!Constants.USER_ALLOWED_IN_CHANNEL.containsKey(voiceChannel)){
            Constants.USER_ALLOWED_IN_CHANNEL.put(voiceChannel, new ArrayList<>());
            return;
        }

        ArrayList<Long> list = Constants.USER_ALLOWED_IN_CHANNEL.get(voiceChannel);

        list.remove(member.getIdLong());

        Constants.USER_ALLOWED_IN_CHANNEL.put(voiceChannel, list);
    }

    public static String getInputStreamContent(InputStream inputStream){
        return new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));
    }

    public static void sendFile(MessageChannel channel, String content, String filename){
        File file = new File("data/temp.txt");
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(content);
            fw.flush();
            fw.close();
            channel.sendFile(file, filename).queue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Files.delete(Paths.get("data/temp.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
