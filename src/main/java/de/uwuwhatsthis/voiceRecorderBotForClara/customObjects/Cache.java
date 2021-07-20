package de.uwuwhatsthis.voiceRecorderBotForClara.customObjects;

import de.uwuwhatsthis.voiceRecorderBotForClara.managers.CacheManager;
import org.json.JSONException;
import org.json.JSONObject;

public class Cache {
    private String recordingLogChannelId;
    private JSONObject data;
    private final String guildID;

    public Cache(JSONObject data, String guildID){
        this.data = data;
        if (data == null){
            data = new JSONObject();
        }

        CacheManager.allCaches.add(this);

        recordingLogChannelId = getJsonStringKey("recordingLogChannelId");
        this.guildID = guildID;
    }

    private String getJsonStringKey(String key){
        try{
            return data.getString(key);
        } catch (JSONException e){
            return null;
        }
    }

    public String getRecordingLogChannelId() {
        return recordingLogChannelId;
    }

    public void setRecordingLogChannelId(String recordingLogChannelId) {
        data.put("recordingLogChannelId", recordingLogChannelId);
        this.recordingLogChannelId = recordingLogChannelId;
    }

    public JSONObject getData() {
        return data;
    }

    public String getGuildID() {
        return guildID;
    }
}
