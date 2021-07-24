package de.uwuwhatsthis.voiceRecorderBotForClara.messageReactionStuff;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

public interface ReactionOperator {

    void execute(Message message, User user);
}
