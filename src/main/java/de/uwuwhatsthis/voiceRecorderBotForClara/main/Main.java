package de.uwuwhatsthis.voiceRecorderBotForClara.main;

import de.uwuwhatsthis.voiceRecorderBotForClara.customObjects.Config;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

public class Main {
    public static Config config;

    public static void main(String[] args) {
        config = new Config("config/config.json");

        JDABuilder jdaBuilder = JDABuilder.createDefault(config.getToken());
        jdaBuilder.addEventListeners(new CommandManager());

        try{
            jdaBuilder.build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }
}
