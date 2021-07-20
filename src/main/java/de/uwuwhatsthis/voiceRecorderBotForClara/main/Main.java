package de.uwuwhatsthis.voiceRecorderBotForClara.main;

import de.uwuwhatsthis.voiceRecorderBotForClara.customObjects.Config;
import de.uwuwhatsthis.voiceRecorderBotForClara.events.GuildVoiceLeaveEventListener;
import de.uwuwhatsthis.voiceRecorderBotForClara.managers.CacheManager;
import de.uwuwhatsthis.voiceRecorderBotForClara.managers.CommandManager;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

public class Main {
    public static Config config;
    public static CacheManager cacheManager;

    public static void main(String[] args) {
        config = new Config("config/config.json");
        cacheManager = new CacheManager("config/cache.json");

        JDABuilder jdaBuilder = JDABuilder.createDefault(config.getToken());
        jdaBuilder.addEventListeners(new CommandManager());
        jdaBuilder.addEventListeners(new GuildVoiceLeaveEventListener());

        try{
            jdaBuilder.build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }
}
