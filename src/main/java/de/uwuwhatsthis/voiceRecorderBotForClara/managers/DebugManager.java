package de.uwuwhatsthis.voiceRecorderBotForClara.managers;

import de.uwuwhatsthis.voiceRecorderBotForClara.customObjects.Debugger;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;

public class DebugManager {
    private final HashMap<Long, Debugger> debuggerHashMap;

    public DebugManager(){
        debuggerHashMap = new HashMap<>();
    }

    public Debugger getDebugger(Guild guild){
        Debugger debugger = debuggerHashMap.get(guild.getIdLong());

        if (debugger == null){
            debugger = new Debugger(guild);
            debuggerHashMap.put(guild.getIdLong(), debugger);
        }

        return debugger;
    }

}
