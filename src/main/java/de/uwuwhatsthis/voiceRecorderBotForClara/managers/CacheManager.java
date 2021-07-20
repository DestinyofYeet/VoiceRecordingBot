package de.uwuwhatsthis.voiceRecorderBotForClara.managers;

import de.uwuwhatsthis.voiceRecorderBotForClara.customObjects.Cache;
import de.uwuwhatsthis.voiceRecorderBotForClara.utils.JsonStuff;
import org.json.JSONArray;
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
    private final String filePath;

    public CacheManager (String filePath){
        this.filePath = filePath;

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
        File file = new File(filePath);
        FileWriter fw = new FileWriter(file);
        fw.write("{}");
    }

    public Cache getCacheForServer(String serverId){
        JSONObject file = new JSONObject(JsonStuff.getFileContent(filePath));
        JSONObject data = file.getJSONObject(serverId);

        return new Cache(data, serverId);
    }

    private Cache createNewCacheForServer(String serverId){
        return new Cache(new JSONObject(), serverId);
    }

    public void saveCaches(){
        JSONObject data = new JSONObject();
        for (Cache cache: allCaches){
            data.put(cache.getGuildID(), cache.getData());
        }

        JsonStuff.writeToJsonFile(filePath, data.toString(2));
    }
}
