/**
 * @author n0nedevv
 * 
 */
package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.gameserver.model.actor.Player;

public interface IVoicedCommandHandler
{
    public boolean useVoicedCommand(String command, Player activeChar);
    
    public String[] getVoicedCommandList();
}