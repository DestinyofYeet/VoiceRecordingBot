package de.uwuwhatsthis.voiceRecorderBotForClara.customObjects;

import de.uwuwhatsthis.voiceRecorderBotForClara.utils.JsonStuff;
import org.json.JSONObject;

public class Config {
    private final String filePath;
    private String token;
    private String prefix;
    private String preMessageURL;
    private String cloudDomain;
    private String cloudEmail;
    private String cloudPassword;
    private String cloudUploadPath;

    private boolean debug;

    public Config(String filePath){
        this.filePath = filePath;
        init();
    }

    private void init(){
        String fileContent = JsonStuff.getFileContent(filePath);
        if (fileContent == null){
            System.err.println("COULD NOT READ CONFIG.....EXITING");
            System.exit(1);
        }

        JSONObject jsonObject = new JSONObject(fileContent);
        token = jsonObject.getString("token");
        prefix = jsonObject.getString("prefix");
        preMessageURL = jsonObject.getString("pre_recording_message_url");
        debug = jsonObject.getBoolean("debug");
        cloudDomain = jsonObject.getString("cloud_domain");
        cloudEmail = jsonObject.getString("cloud_email");
        cloudPassword = jsonObject.getString("cloud_password");
        cloudUploadPath = jsonObject.getString("cloud_upload_path");
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

    public String getPreMessageURL() {
        return preMessageURL;
    }

    public boolean isDebug() {
        return debug;
    }

    public String getCloudEmail() {
        return cloudEmail;
    }

    public String getCloudPassword() {
        return cloudPassword;
    }

    public String getCloudDomain() {
        return cloudDomain;
    }

    public String getCloudUploadPath() {
        return cloudUploadPath;
    }
}
