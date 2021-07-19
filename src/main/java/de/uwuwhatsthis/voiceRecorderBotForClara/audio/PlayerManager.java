package de.uwuwhatsthis.voiceRecorderBotForClara.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {
    private static PlayerManager INSTANCE;
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    private PlayerManager(){
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public synchronized GuildMusicManager getGuildMusicManager(Guild guild){
        long guildId = guild.getIdLong();
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null){
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public void loadAndPlay(final TextChannel channel, final String trackURL){
        final GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, trackURL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                /*
                // channel.sendMessageEmbeds(new Embed("Queued!", "Added \"" + audioTrack.getInfo().title + "\" to queue!", Color.GREEN).build()).queue();
                // SendMessage.sendMessage(channel, CreateEmbed.buildEmbed("Queued!", "Added " + audioTrack.getInfo().title + " to queue!", Color.GREEN), Constants.SECONDS_TO_DELETE_MESSAGES);
                 */

                play(musicManager, audioTrack);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                AudioTrack firstTrack = audioPlaylist.getSelectedTrack();

                if (firstTrack == null){
                    for (AudioTrack audioTrack: audioPlaylist.getTracks()){
                        play(musicManager, audioTrack);
                    }
                    // SendMessage.sendMessage(channel, new Embed("Play", "Playlist \"" + audioPlaylist.getName() + "\" loaded with " + audioPlaylist.getTracks().size() + " entries!", Color.GREEN).build(), Constants.SECONDS_TO_DELETE_MESSAGES);
                    return;
                }

                // SendMessage.sendMessage(channel, CreateEmbed.buildEmbed("Queued!", "Added \"" + firstTrack.getInfo().title + "\" to queue!", Color.GREEN), Constants.SECONDS_TO_DELETE_MESSAGES);

                play(musicManager, firstTrack);
            }

            @Override
            public void noMatches() {
                // SendMessage.sendMessage(channel, CreateEmbed.buildEmbed("Error", "Nothing found by searching for " + trackURL, Color.RED), Constants.SECONDS_TO_DELETE_MESSAGES);
            }

            @Override
            public void loadFailed(FriendlyException e) {
                // SendMessage.sendMessage(channel, CreateEmbed.buildEmbed("Error", "Could not play: " + e.getMessage(), Color.RED), Constants.SECONDS_TO_DELETE_MESSAGES);
            }
        });
    }

    private void play(GuildMusicManager musicManager, AudioTrack track){
        musicManager.scheduler.queue(track);
    }


    public static synchronized PlayerManager getInstance(){
        if (INSTANCE == null){
            INSTANCE = new PlayerManager();
        }

        return INSTANCE;
    }
}
