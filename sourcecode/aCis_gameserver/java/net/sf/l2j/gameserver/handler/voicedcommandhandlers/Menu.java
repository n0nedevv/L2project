/**
 * @author n0nedevv
 * 
 */
package net.sf.l2j.gameserver.handler.voicedcommandhandlers;



import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;


public class Menu implements IVoicedCommandHandler
{
    private static final String[] _voicedCommands =
    {
        "menu",
        "setPartyRefuse",
        "setTradeRefuse",    
        "setbuffsRefuse",
        "setMessageRefuse",
        "setxpnot",
        "setSsprot"
    };
    
    private static final String ACTIVED = "<font color=00FF00>ON</font>";
    private static final String DESATIVED = "<font color=FF0000>OFF</font>";
    
    @Override
    public boolean useVoicedCommand(String command, Player activeChar)
    {
        if (command.equals("menu"))
            showHtml(activeChar);        
                
        else if (command.equals("setPartyRefuse"))
        {
            if (activeChar.isPartyInvProt())
                activeChar.setIsPartyInvProt(false);
            else
                activeChar.setIsPartyInvProt(true);            
            showHtml(activeChar);
        }    
        else if (command.equals("setTradeRefuse"))
        {
            if (activeChar.isInTradeProt())
                activeChar.setIsInTradeProt(false);
            else
                activeChar.setIsInTradeProt(true);
            showHtml(activeChar);
        }        
        else if (command.equals("setMessageRefuse"))
        {        
            if (activeChar.getMessageRefusal())
                activeChar.setMessageRefusal(false);
            else
                activeChar.setMessageRefusal(true);
            showHtml(activeChar);
        }
        else if (command.equals("setbuffsRefuse"))
        {        
            if (activeChar.isBuffProtected())
                activeChar.setIsBuffProtected(false);
            else
                activeChar.setIsBuffProtected(true);
                activeChar.sendMessage("Buff protection.");
            showHtml(activeChar);
        }
        
        else if (command.equals("setxpnot"))
        {        
            if (activeChar.cantGainXP())
                activeChar.cantGainXP(false);
            else
                activeChar.cantGainXP(true);
                activeChar.sendMessage(" Xp effects.");
            showHtml(activeChar);
        }        
        else if (command.equals("setSsprot"))
        {        
            if (activeChar.isSSDisabled())
                activeChar.setIsSSDisabled(false);
            else
                activeChar.setIsSSDisabled(true);
                activeChar.sendMessage("Soulshots effects.");
            showHtml(activeChar);
        }        
        return true;
    }
    
    private static void showHtml(Player activeChar)
    {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/mods/menu/menu.htm");
        GameServer.getInstance();
		html.replace("%online%", GameServer.getAllPlayers());    
        html.replace("%partyRefusal%", activeChar.isPartyInvProt() ? ACTIVED : DESATIVED);
        html.replace("%tradeRefusal%", activeChar.isInTradeProt() ? ACTIVED : DESATIVED);
        html.replace("%buffsRefusal%", activeChar.isBuffProtected() ? ACTIVED : DESATIVED);
        html.replace("%messageRefusal%", activeChar.getMessageRefusal() ? ACTIVED : DESATIVED);    
        html.replace("%cantGainXP%", activeChar.cantGainXP() ? ACTIVED : DESATIVED);
        html.replace("%Eff.Ss%", activeChar.isSSDisabled() ? ACTIVED : DESATIVED);    
        
        activeChar.sendPacket(html);
    }
    
    @Override
    public String[] getVoicedCommandList()
    {
        return _voicedCommands;
    }

}