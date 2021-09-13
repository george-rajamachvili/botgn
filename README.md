# botgn

Made by George Rajamachvili, for use in the Discord server '.gn'

A "bot" for the popular social platform Discord, currently being created for the purpose of server management, music playing in voice chats, and more.

Current functionality:
- Pongs when pinged
- Plays music in a voice channel when prompted via text channel (using LavaPlayer)
Future planned funationality:
- Role management

Command Index:
- !ping

  Returns "pong" to the channel where the command was issued
- !play

  Unpauses current playing track (if user has permission)
- !play [STRING]

  Joins the voice channel of the user issuing the command, and plays audio corresponding to the link in the [STRING] argument
- !pause

  Pauses/unpauses currently playing track
- !np

  Outputs information about currently playing track (title, link, author, time position in audio)
- !skip

  Skips the currently playing track

Settings Command Index:
- !settings

  Outputs current settings commands
- !prefix [STRING]

  Changes prefix of bot ('*' by default)

  
  
