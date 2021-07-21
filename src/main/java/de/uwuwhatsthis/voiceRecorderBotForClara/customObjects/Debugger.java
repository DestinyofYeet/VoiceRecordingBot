package de.uwuwhatsthis.voiceRecorderBotForClara.customObjects;

import de.uwuwhatsthis.voiceRecorderBotForClara.main.Main;
import net.dv8tion.jda.api.entities.Guild;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Debugger {
    private final Guild guild;

    public Debugger(Guild guild){
        this.guild = guild;
    }

    public void debug(String message){
        if (Main.config.isDebug()){
            System.out.println("DEBUG - " + getFormat() + ": " + message);
        }
    }

    public void info(String message){
        System.out.println("INFO - " + getFormat() + ": " + message);
    }

    public void error(String error){
        System.err.println("ERROR - " + getFormat() + ": " + error);
    }

    private String getFormat(){
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("d.M.u H:m:s")) + " - " + guild.getName() + "(" + guild.getId() + ")";
    }

}
