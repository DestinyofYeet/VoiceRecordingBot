package de.uwuwhatsthis.voiceRecorderBotForClara.utils;

import de.uwuwhatsthis.voiceRecorderBotForClara.audio.PlayerManager;
import de.uwuwhatsthis.voiceRecorderBotForClara.audio.ReceiveAndHandleAudioForChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.ArrayList;
import java.util.HashMap;

public class Constants {

    public final static HashMap<String, String> COMMAND_MAP = new HashMap<String, String>(){{
        put("record-start", "RecordStart");
        put("record-stop", "RecordStop");
        put("recording-channel", "RecordingChannel");
        put("upload-test", "UploadTest");
    }};

    public final static HashMap<VoiceChannel, ReceiveAndHandleAudioForChannel> AUDIO_MAP = new HashMap<>();

    public final static HashMap<VoiceChannel, ArrayList<Long>> USER_ALLOWED_IN_CHANNEL = new HashMap<>();

}
