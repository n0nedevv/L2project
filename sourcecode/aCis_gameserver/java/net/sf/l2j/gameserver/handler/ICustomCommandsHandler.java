package net.sf.l2j.gameserver.handler;

import net.sf.l2j.gameserver.model.actor.Player;

/**
 * @author l2.topgameserver.net
 *
 */
public interface ICustomCommandsHandler
{
	void useCustomCommand(String command, Player activeChar);
	public String[] getCustomCommandList();
}