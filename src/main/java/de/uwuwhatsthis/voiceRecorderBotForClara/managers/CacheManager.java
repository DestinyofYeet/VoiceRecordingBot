package de.uwuwhatsthis.voiceRecorderBotForClara.managers;

import de.uwuwhatsthis.voiceRecorderBotForClara.customObjects.Cache;
import de.uwuwhatsthis.voiceRecorderBotForClara.utils.JsonStuff;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CacheManager {
    public static List<Cache> allCaches = new ArrayList<>();
    private final String cacheFilePath;

    public CacheManager (String filePath){
        this.cacheFilePath = filePath;

        if (!Files.exists(Paths.get(filePath))){
            try {
                createCacheFile();
            } catch (IOException e) {
                System.err.println("Could not create new cache file!");
                e.printStackTrace();
            }
        }
    }

    private void createCacheFile() throws IOException{
        File file = new File(cacheFilePath);
        FileWriter fw = new FileWriter(file);
        fw.write("{}");
        fw.flush();
        fw.close();
    }

    public Cache getCacheForServer(String serverId){
        JSONObject file = new JSONObject(JsonStuff.getFileContent(cacheFilePath));
        JSONObject data;
        try{
            data = file.getJSONObject(serverId);
            return new Cache(data, serverId);
        } catch (JSONException e){
            return createNewCacheForServer(serverId);
        }
    }

    private Cache createNewCacheForServer(String serverId){
        return new Cache(new JSONObject(), serverId);
    }

    public void saveCaches(){
        JSONObject data = new JSONObject();
        for (Cache cache: allCaches){
            data.put(cache.getGuildID(), cache.getData());
        }

        JsonStuff.writeToJsonFile(cacheFilePath, data.toString(2));
    }
}
