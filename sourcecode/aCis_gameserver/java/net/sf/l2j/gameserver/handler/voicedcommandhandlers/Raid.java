/**
 * @author n0nedevv
 * 
 */
package net.sf.l2j.gameserver.handler.voicedcommandhandlers;


import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.manager.GrandBossManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.commons.data.StatSet;
import net.sf.l2j.gameserver.data.xml.NpcData;

public class Raid implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands = 
	{
		"Raids"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar)
	{
		StringBuilder tb = new StringBuilder();
		NpcHtmlMessage msg = new NpcHtmlMessage(5);
		tb.append("<html><title>Grand Boss</title><body><br><center>");
		tb.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><br>");

		for (final int boss : Config.LIST_RAID_BOSS_IDS)
		{
			String name = "";
			NpcTemplate template = null;
			if ((template = NpcData.getInstance().getTemplate(boss)) != null)
			{
				name = template.getName();
			}
			else
			{
				continue;
			}
			
			StatSet actual_boss_stat = null;
			long delay = 0;
			
			if (NpcData.getInstance().getTemplate(boss).isType("L2RaidBoss"))
			{
				//actual_boss_stat = RaidBossManager.getInstance().getStatus(boss);
				//if (actual_boss_stat != null)
					//delay = actual_boss_stat.getLong("respawnTime");
			}
			else if (NpcData.getInstance().getTemplate(boss).isType("L2GrandBoss"))
			{
				actual_boss_stat = GrandBossManager.getInstance().getStatSet(boss);
				if (actual_boss_stat != null)
					delay = actual_boss_stat.getLong("respawn_time");
			}
			else
				continue;
			
			if (delay <= System.currentTimeMillis())
			{
				tb.append("<font color=\"00C3FF\">" + name + "</color>: " + "<font color=\"9CC300\">Is Alive</color>" + "<br1>");
			}
			else
			{
				final int hours = (int) ((delay - System.currentTimeMillis()) / 1000 / 60 / 60);
				final int mins = (int) (((delay - (hours * 60 * 60 * 1000)) - System.currentTimeMillis()) / 1000 / 60);
				final int seconds = (int) (((delay - ((hours * 60 * 60 * 1000) + (mins * 60 * 1000))) - System.currentTimeMillis()) / 1000);
				tb.append("<font color=\"00C3FF\">" + name + "</color>" + "<font color=\"FFFFFF\">" + " " + "Respawn in :</color>" + " " + " <font color=\"32C332\">" + hours + " : " + mins + " : " + seconds + "</color><br1>");
			}
		}
				
		tb.append("<br><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br>");
		tb.append("</center></body></html>");
		msg.setHtml(tb.toString());
		activeChar.sendPacket(msg);
		return false;
	}
    
    @Override
    public String[] getVoicedCommandList()
    {
     return _voicedCommands;
    }
}