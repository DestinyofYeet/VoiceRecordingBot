package de.uwuwhatsthis.voiceRecorderBotForClara.utils;

import de.uwuwhatsthis.voiceRecorderBotForClara.audio.ReceiveAndHandleAudioForChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.HashMap;

public class Constants {

    public final static HashMap<String, String> commandMap = new HashMap<String, String>(){{
        put("record-start", "RecordStart");
        put("record-stop", "RecordStop");
        put("recording-channel", "RecordingChannel");
    }};

    public final static HashMap<VoiceChannel, ReceiveAndHandleAudioForChannel> audioMap = new HashMap<>();

}
