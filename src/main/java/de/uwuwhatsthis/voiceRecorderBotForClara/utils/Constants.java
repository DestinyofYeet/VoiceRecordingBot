package de.uwuwhatsthis.voiceRecorderBotForClara.utils;

import java.util.HashMap;

public class Constants {

    public final static HashMap<String, String> commandMap = new HashMap<String, String>(){{
        put("record-start", "RecordStart");
        put("record-stop", "RecordStop");
        put("recording-channel", "RecordingChannel");
    }};
}
