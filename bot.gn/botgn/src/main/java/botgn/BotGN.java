package botgn;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.security.auth.login.LoginException;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.internal.utils.PermissionUtil;

// TODO
// Add *np functionality
// Make the queue a thing

public class BotGN extends ListenerAdapter
{
	// The BOT itself!
	static JDA botgn;

	// VARIABLE DECLARATION
	Message message;
	String content;
	String prefix = "*";
	String link;
	AudioPlayer player;
	boolean isInChannel = false;

	AudioPlayerManager playerManager;

	// Bot API construction
	public static void main(String args[]) throws LoginException
	{
		botgn = JDABuilder.createDefault("ODE4NTkxMDc0OTk3ODk1MTg4.YEaSWA.bpNmUMG9TJ6im4BS24XvpXybstA")
				.addEventListeners(new BotGN()).build();
	}

	// This method is ran by default when anything is put into chat. It basically is
	// an idle listener.
	@Override
	public void onMessageReceived(MessageReceivedEvent event)
	{
		// MESSAGE RECIEVING
		// Bot doesn't respond to bots, to prevent singularity
		if (event.getAuthor().isBot())
			return;

		// Gets the message in any channel w/ access and then converts it into a better
		// format for processing
		message = event.getMessage();
		content = message.getContentRaw();

		// FUNCTIONALITY
		// This is the ping-pong function. For testing purposes.
		if (content.toLowerCase().equals(prefix + "ping"))
		{
			MessageChannel channel = event.getChannel();
			channel.sendMessage(sendEmbed("pong")).queue();
		}

		if (content.toLowerCase().substring(0, 1).equals(prefix))
		{
			MessageChannel channel = event.getChannel();
			
			if (content.toLowerCase().contains(prefix + "play"))
			{
				if (content.toLowerCase().equals(prefix + "play"))
				{
					if (player.isPaused())
					{
						player.setPaused(false);
						channel.sendMessage(sendEmbed("Unpaused current track.")).queue();
					}
				} else
				{
					// Audio player manager
					playerManager = new DefaultAudioPlayerManager();
					AudioSourceManagers.registerRemoteSources(playerManager);

					// STEP 1: Get the voice channel of the person who called the command!
					Guild guild = botgn.getGuildById("752253337394610277");
					Member botgnMember = guild.getMember(event.getAuthor());
					VoiceChannel vc = botgnMember.getVoiceState().getChannel();

					// STEP 2: Retrieve the AudioManager
					AudioManager audioManager = guild.getAudioManager();
					player = playerManager.createPlayer();

					// STEP 3: Join the VC
					if (vc == null)
					{
						channel.sendMessage(
								BotGN.sendEmbed("User of " + prefix + "play command must be in a Voice Channel first."))
								.queue();
					} else if (botgnMember.hasPermission(vc, Permission.VOICE_CONNECT))
					{
						audioManager.openAudioConnection(vc);
						isInChannel = true;
					} else
					{
						channel.sendMessage(BotGN.sendEmbed("Cannot join " + vc + " due to lack of permissions."))
								.queue();
					}

					// Step 4: Play the music/audio/funny
					if (isInChannel = true && vc != null)
					{
						link = content.substring(5);
						link = link.replaceAll("\\s+", "");
						System.out.println(link);

						AudioPlayerSendHandler sendHandler = new AudioPlayerSendHandler(player);
						audioManager.setSendingHandler(sendHandler);

						// Open the gates!
						audioManager.openAudioConnection(vc);

						TrackScheduler trackScheduler = new TrackScheduler(player);
						player.addListener(trackScheduler);
						playerManager.loadItem(link, new AudioLoadResultHandler()
						{
							@Override
							public void trackLoaded(AudioTrack track)
							{
								String title = track.getInfo().title;
								String author = track.getInfo().author;
								String user = message.getAuthor().getAsMention();

								channel.sendMessage(BotGN.sendEmbed("Now Playing:",
										"[" + title + "](" + link + ") by " + author + "\n[" + user + "]")).queue();
								trackScheduler.queue(track);
								player.playTrack(track);
							}

							@Override
							public void playlistLoaded(AudioPlaylist playlist)
							{
								for (AudioTrack track : playlist.getTracks())
								{
									trackScheduler.queue(track);
									player.playTrack(track);
								}
							}

							@Override
							public void noMatches()
							{
								// Notify the user that we've got nothing
								channel.sendMessage(BotGN.sendEmbed("We don't have any matches for your search... :("))
										.queue();
							}

							@Override
							public void loadFailed(FriendlyException throwable)
							{
								// Notify the user that everything exploded
								channel.sendMessage(BotGN.sendEmbed("ITS ALL ON FIRE AAAAAAHHHH")).queue();
							}
						});
					}
				}
			}
		}

		if (content.toLowerCase().equals(prefix + "testSwap"))
		{
			MessageChannel channel = event.getChannel();
			channel.sendMessage(sendEmbed("Swapped Role 1 and 2")).queue();

			Guild guild = botgn.getGuildById("752253337394610277");
			guild.modifyRolePositions().selectPosition(1).swapPosition(2).queue();
		}

		if (content.toLowerCase().equals(prefix + "pause"))
		{
			MessageChannel channel = event.getChannel();

			if (player.isPaused())
			{
				player.setPaused(false);
				channel.sendMessage(sendEmbed("Unpaused current track.")).queue();
			} else
			{
				player.setPaused(true);
				channel.sendMessage(sendEmbed("Paused current track.")).queue();
			}
		}

		// ---------------- SETTINGS COMMANDS BELOW THIS POINT ----------------
		// This is the settings menu; it just shows a list of commands for settings
		if (content.toLowerCase().equals(prefix + "settings"))
		{
			MessageChannel channel = event.getChannel();
			channel.sendMessage(sendEmbed(
					"List of current bot.gn settings:\n- " + prefix + "prefix [PREFIX]\n- " + prefix + "rollRoles"))
					.queue();
			channel.sendMessage("").queue();
		}

		if (content.toLowerCase().contains(prefix + "prefix"))
		{
			// Calls the changePrefix() method to run the command
			changePrefix(event);
		}

//		// Unused rollRoles command
//		if (content.toLowerCase().contains(prefix + "rollroles"))
//		{
//			rollRoles(event);
//		}
	}

	// Overloading for the win
	public static MessageEmbed sendEmbed(String message)
	{
		EmbedBuilder embedMessage = new EmbedBuilder();
		embedMessage.setDescription(message);
		embedMessage.setColor(new Color(148, 0, 211));
		return embedMessage.build();
	}

	public static MessageEmbed sendEmbed(String title, String message)
	{
		EmbedBuilder embedMessage = new EmbedBuilder();
		embedMessage.setTitle(title);
		embedMessage.setDescription(message);
		embedMessage.setColor(new Color(148, 0, 211));
		return embedMessage.build();
	}

	// The Change Prefix Command
	public void changePrefix(MessageReceivedEvent event)
	{
		if (PermissionUtil.checkPermission(event.getMember(), Permission.MANAGE_SERVER) == true)
		{
			MessageChannel channel = event.getChannel();

			try
			{
				// Check prefix for spaces or blanks
				if (content.substring(((prefix.length() - 1) + 8)).contains(" "))
				{
					channel.sendMessage(sendEmbed("Prefix cannot be blank or contain blanks.")).queue();
				}
				// Check prefix for any letters or numbers
				if (content.substring(((prefix.length() - 1) + 8)) != null
						&& content.substring(((prefix.length() - 1) + 8)).chars().allMatch(Character::isLetterOrDigit))
				{
					channel.sendMessage(sendEmbed("Prefix cannot contain letters or numbers.")).queue();
				} else
				{
					prefix = content.substring(((prefix.length() - 1) + 8));
					channel.sendMessage(sendEmbed("Prefix has been changed to: " + prefix)).queue();
				}
			} catch (Exception StringIndexOutOfBoundsException)
			{
				channel.sendMessage(sendEmbed("Prefix assignment failed. Probably contains spaces.")).queue();
			}
		} else
		{
			MessageChannel channel = event.getChannel();
			channel.sendMessage(sendEmbed("Permission required: Manage Server. Get out, idjit")).queue();
		}
	}

	// This method is for siege.gn only. Meant to prevent inevitable server conflict
//	public void rollRoles(MessageReceivedEvent event)
//	{
//		MessageChannel channel = event.getChannel();
//		channel.sendMessage(sendEmbed("Rolled roles?")).queue();
//
//		/*// Create Random Array object (basically the "seed" array for the randomization)
//		ArrayList<Integer> seedArray = new ArrayList<Integer>();
//
//		// Generate and randomize seed array
//		for (int i = 1; i <= 6; i++)
//		{
//			seedArray.add(i);
//		}
//		Collections.shuffle(seedArray);
//
//		for (int i = 0; i < seedArray.size() - 1; i++)
//		{
//			System.out.print(seedArray.get(i) + " ");
//		}
//		System.out.print(seedArray.get(seedArray.size() - 1));
//		System.out.print("|");*/
//
//		// Sets 'guild' to the ID of the server (hard-coded for now)
//		Guild guild = botgn.getGuildById("752253337394610277");
//
//		// The shuffle algorithm itself
//		Random r = new Random(); 
//		for (int i = 5; i > 0; i--) 
//	    {
//	        int change = r.nextInt(5);
//	        
//	        guild.modifyRolePositions().selectPosition(i).swapPosition(change).queue();
//	    }
//
//		//seedArray.clear();
//	}

}
