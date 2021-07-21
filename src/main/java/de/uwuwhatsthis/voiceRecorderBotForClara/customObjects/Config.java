package de.uwuwhatsthis.voiceRecorderBotForClara.customObjects;

import de.uwuwhatsthis.voiceRecorderBotForClara.utils.JsonStuff;

public class Config {
    private final String filePath;
    private String token;
    private String prefix;
    private String preMessagePath;
    private boolean debug;

    public Config(String filePath){
        this.filePath = filePath;
        init();
    }

    private void init(){
        token = JsonStuff.getStringFromJson(filePath, "token");
        prefix = JsonStuff.getStringFromJson(filePath, "prefix");
        preMessagePath = JsonStuff.getStringFromJson(filePath, "pre_recording_message_path");
        debug = JsonStuff.getBoolFromJson(filePath, "debug", false);
    }

    public String getFilePath() {
        return filePath;
    }

    public String getToken() {
        return token;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getPreMessagePath() {
        return preMessagePath;
    }

    public boolean isDebug() {
        return debug;
    }
}
