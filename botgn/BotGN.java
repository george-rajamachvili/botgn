package botgn;

import java.awt.Color;
import java.util.EventListener;

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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.internal.utils.PermissionUtil;

/* Bot.GN current TODO:
 * FIX player.getPlayingTrack() to be NOT null!
 * Queuing tracks (hard)
 * Moving commands to a separate class (moderate-hard)
 * Actually chart the structure of Bot.GN and add documentation
 */

public class BotGN extends ListenerAdapter
{
	// VARIABLE DECLARATION
	Message message;
	String content;
	String link;
	AudioPlayer player;
	boolean isInChannel = false;

	AudioPlayerManager playerManager;
	TrackScheduler trackScheduler;
	AudioManager audioManager;
	
	static JDA botgn;

	// EDITABLE ROLES (CONFIG)
	String prefix = "*";
	String djRoleName = "DJ";

	public static void main(String[] args) throws LoginException
	{
		// NOTE: Put in token, as I remove it when pushing to Git
		botgn = JDABuilder.createDefault("").addEventListeners(new BotGN()).build();
	}
	
	// This method is ran by default when anything is put into chat. It basically is
	// an idle listener.
	public void onMessageReceived(MessageReceivedEvent event)
	{
		// Note: Put in server, as I remove it when pushing to Git
		Guild guild = botgn.getGuildById("");

		// Audio player manager
		playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playerManager);

		// Retrieve the AudioManager
		audioManager = guild.getAudioManager();
		player = playerManager.createPlayer();

		// MESSAGE RECIEVING
		// Bot doesn't respond to bots, to prevent singularity
		if (event.getAuthor().isBot())
			return;

		// Gets the message in any channel w/ access and then converts it into a better
		// format for processing
		message = event.getMessage();
		content = message.getContentRaw();

		// The server this bot is currently servicing

		// Get the person who called the command in the first place
		Member botgnMember = guild.getMember(event.getAuthor());

		// Get the VC the caller is in, if applicable
		VoiceChannel vc = botgnMember.getVoiceState().getChannel();

		// Check rights (see if the command user can do things like manage the server or
		// use the music bot)
		boolean manageServer = false;
		boolean isDJ = false;

		// If the user has manage server rights, let them use the manageServer commands
		if (PermissionUtil.checkPermission(event.getMember(), Permission.MANAGE_SERVER) == true)
			manageServer = true;
		// If the user has Admin rights, let them use the DJ thing
		if (PermissionUtil.checkPermission(event.getMember(), Permission.ADMINISTRATOR) == true)
			isDJ = true;
		else
		{
			for (int i = 0; i < botgnMember.getRoles().size(); i++)
			{
				if (botgnMember.getRoles().get(i).getName().equals(djRoleName))
				{
					isDJ = true;
					break;
				}
			}
		}

		// FUNCTIONALITY
		// This is the ping-pong function. For testing purposes.
		if (content.toLowerCase().equals(prefix + "ping"))
		{
			MessageChannel channel = event.getChannel();
			channel.sendMessage(sendEmbed("pong")).queue();
		}

		// If the message has the prefix as the first character
		if (content.toLowerCase().substring(0, 1).equals(prefix))
		{
			MessageChannel channel = event.getChannel();

			// !play
			if (content.toLowerCase().contains(prefix + "play"))
			{
				if (content.toLowerCase().equals(prefix + "play"))
				{
					if (isDJ == true)
					{
						if (player.isPaused())
						{
							player.setPaused(false);
							channel.sendMessage(sendEmbed("Unpaused current track.")).queue();
						}
					} else
					{
						channel.sendMessage(sendEmbed("You are not a DJ :(")).queue();
					}

				} else
				{
					if (isDJ == true)
					{
						// STEP 1: Join the VC
						if (vc == null)
						{
							channel.sendMessage(BotGN
									.sendEmbed("User of " + prefix + "play command must be in a Voice Channel first."))
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

						// Step 2: Play the music/audio/funny (OR queue the music/audio/funny if
						// something else is playing)
						if (isInChannel = true && vc != null)
						{
							link = content.substring(5);
							link = link.replaceAll("\\s+", "");
							System.out.println(link);

							// Creates AudioPlayerSendHandler object (LavaPlayer stuff)
							AudioPlayerSendHandler sendHandler = new AudioPlayerSendHandler(player);
							audioManager.setSendingHandler(sendHandler);

							// Open the gates!
							audioManager.openAudioConnection(vc);

							trackScheduler = new TrackScheduler(player);

							player.addListener(trackScheduler);
							playerManager.loadItem(link, new AudioLoadResultHandler()
							{
								@Override
								public void trackLoaded(AudioTrack track)
								{
									String title = track.getInfo().title;
									String author = track.getInfo().author;
									String user = message.getAuthor().getAsMention();

									if (player.getPlayingTrack() == null)
									{
										channel.sendMessage(BotGN.sendEmbed("Now Playing:",
												"[" + title + "](" + link + ") by " + author + "\n[" + user + "]"))
												.queue();
										trackScheduler.queue(track);
									} else
									{
										channel.sendMessage(BotGN.sendEmbed("Now Queued:",
												"[" + title + "](" + link + ") by " + author + "\n[" + user + "]"))
												.queue();
										trackScheduler.queue(track);
									}
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
									channel.sendMessage(BotGN
											.sendEmbed("Search function isn't a thing yet, go complain to @basspath"))
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

					} else
						channel.sendMessage(BotGN.sendEmbed("You are not a DJ :(")).queue();
				}
			}
		}

		// !testSwap (DEBUG)
//		if (content.toLowerCase().equals(prefix + "testSwap"))
//		{
//			MessageChannel channel = event.getChannel();
//			channel.sendMessage(sendEmbed("Swapped Role 1 and 2")).queue();
//
//			guild.modifyRolePositions().selectPosition(1).swapPosition(2).queue();
//		}

		// !pause
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

		
		// !np
		if (content.toLowerCase().equals(prefix + "np"))
		{
			MessageChannel channel = event.getChannel();
			System.out.println(player.getPlayingTrack());
			if(player.getPlayingTrack() == null)
			{
				channel.sendMessage(sendEmbed("There is no track playing!")).queue();
			}
			else
			{
				channel.sendMessage(BotGN.sendEmbed("Currently Playing:",
						"[" + player.getPlayingTrack().getInfo().title + "](" + link + ") by " + player.getPlayingTrack().getInfo().author +
						"\n[" + event.getAuthor() + "]\n (" + player.getPlayingTrack().getPosition()))
						.queue();
			}
		}
		
		// !skip
		if (content.toLowerCase().equals(prefix + "skip"))
		{
			MessageChannel channel = event.getChannel();
			if (isDJ == true)
			{
				if (player.getPlayingTrack() == null)
				{
					channel.sendMessage(sendEmbed("Nothing to skip.")).queue();
					
				} else
				{
					trackScheduler.nextTrack();
					channel.sendMessage(sendEmbed("Track has been skipped.")).queue();
				}

			} else
			{
				channel.sendMessage(sendEmbed("You are not a DJ :(")).queue();
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

	// The Change Prefix Command (!prefix [PREFIX])
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
}
