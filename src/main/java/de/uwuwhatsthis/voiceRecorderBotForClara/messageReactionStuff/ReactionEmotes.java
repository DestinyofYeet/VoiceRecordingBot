package de.uwuwhatsthis.voiceRecorderBotForClara.messageReactionStuff;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageReaction;

public class ReactionEmotes {

    private static JDA jdaStatic;

    private static final String CHECK_MARK = "✅", RED_CROSS = "❌";

    public static MessageReaction.ReactionEmote CHECK_MARK_EMOTE, RED_CROSS_EMOTE;

    private static MessageReaction.ReactionEmote loadEmote(String emote){
        return MessageReaction.ReactionEmote.fromUnicode(emote, jdaStatic);
    }

    public static void init(JDA jda){
        jdaStatic = jda;
        CHECK_MARK_EMOTE = loadEmote(CHECK_MARK);
        RED_CROSS_EMOTE = loadEmote(RED_CROSS);
    }
}
