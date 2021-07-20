package de.uwuwhatsthis.voiceRecorderBotForClara.utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.util.List;

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
}
