package de.uwuwhatsthis.voiceRecorderBotForClara.messageReactionStuff;

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;

public class ReactionUnit {

    private final Message message;
    private Emote emote = null;
    private MessageReaction.ReactionEmote reactionEmote = null;
    private final ReactionOperator lambda;

    private ReactionUnit(Message message, ReactionOperator lambda){
        this.message = message;
        this.lambda = lambda;
    }

    public ReactionUnit(Message message, Emote emote, ReactionOperator lambda){
        this(message, lambda);
        this.emote = emote;
    }

    public ReactionUnit(Message message, MessageReaction.ReactionEmote emote, ReactionOperator lambda){
        this(message, lambda);
        this.emote = null;
        reactionEmote = emote;
    }


    public Message getMessage() {
        return message;
    }

    public Emote getEmote() {
        return emote;
    }

    public ReactionOperator getLambda() {
        return lambda;
    }

    public MessageReaction.ReactionEmote getReactionEmote() {
        return reactionEmote;
    }
}
