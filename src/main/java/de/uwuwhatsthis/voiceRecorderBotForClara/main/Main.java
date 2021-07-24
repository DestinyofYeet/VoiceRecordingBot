package de.uwuwhatsthis.voiceRecorderBotForClara.main;

import de.uwuwhatsthis.voiceRecorderBotForClara.customObjects.Config;
import de.uwuwhatsthis.voiceRecorderBotForClara.events.GuildVoiceJoinEventListener;
import de.uwuwhatsthis.voiceRecorderBotForClara.events.GuildVoiceLeaveEventListener;
import de.uwuwhatsthis.voiceRecorderBotForClara.managers.CacheManager;
import de.uwuwhatsthis.voiceRecorderBotForClara.managers.CommandManager;
import de.uwuwhatsthis.voiceRecorderBotForClara.managers.DebugManager;
import de.uwuwhatsthis.voiceRecorderBotForClara.messageReactionStuff.ReactionEmotes;
import de.uwuwhatsthis.voiceRecorderBotForClara.messageReactionStuff.ReactionManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

public class Main {
    public static Config config;
    public static CacheManager cacheManager;
    public static DebugManager debugManager;
    public static ReactionManager reactionManager;

    public static void main(String[] args) {
        config = new Config("config/config.json");
        cacheManager = new CacheManager("config/cache.json");
        debugManager = new DebugManager();
        reactionManager = new ReactionManager();

        JDABuilder jdaBuilder = JDABuilder.createDefault(config.getToken());
        jdaBuilder.addEventListeners(new CommandManager());
        jdaBuilder.addEventListeners(new GuildVoiceLeaveEventListener());
        jdaBuilder.addEventListeners(reactionManager);
        jdaBuilder.addEventListeners(new GuildVoiceJoinEventListener());

        try{
            JDA jda = jdaBuilder.build();
            ReactionEmotes.init(jda);
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }
}
