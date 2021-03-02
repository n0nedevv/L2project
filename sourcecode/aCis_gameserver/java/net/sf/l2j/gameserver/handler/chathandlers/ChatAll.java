package net.sf.l2j.gameserver.handler.chathandlers;

import net.sf.l2j.gameserver.enums.FloodProtector;
import net.sf.l2j.gameserver.enums.SayType;
import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedCommandHandler;
import java.util.StringTokenizer;


public class ChatAll implements IChatHandler
{
	private static final SayType[] COMMAND_IDS =
	{
		SayType.ALL
	};
	
	@Override
	public void handleChat(SayType type, Player player, String target, String text)
	{
		if (!player.getClient().performAction(FloodProtector.GLOBAL_CHAT))
			return;
		
		if (text.startsWith("."))
		{
			StringTokenizer st = new StringTokenizer(text);
			IVoicedCommandHandler cch;
			String command = "";
			
			if (st.countTokens() > 1)
			{
				command = st.nextToken().substring(1);
				target = text.substring(command.length() + 2);
				cch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
			}
			else
			{
				command = text.substring(1);
				cch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
			}
			
			if (cch != null)
			{
				cch.useVoicedCommand(command, player);
				return;
			}
		}
		
		
		final CreatureSay cs = new CreatureSay(player, type, text);
		for (Player knownPlayer : player.getKnownTypeInRadius(Player.class, 1250))
			knownPlayer.sendPacket(cs);
		
		player.sendPacket(cs);
	}
	
	@Override
	public SayType[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}