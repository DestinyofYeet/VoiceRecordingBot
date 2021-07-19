package de.uwuwhatsthis.voiceRecorderBotForClara.customObjects;

import de.uwuwhatsthis.voiceRecorderBotForClara.utils.JsonStuff;

public class Config {
    private final String filePath;
    private String token;
    private String prefix;

    public Config(String filePath){
        this.filePath = filePath;
        init();
    }

    private void init(){
        token = JsonStuff.getStringFromJson(filePath, "token");
        prefix = JsonStuff.getStringFromJson(filePath, "prefix");
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
}
