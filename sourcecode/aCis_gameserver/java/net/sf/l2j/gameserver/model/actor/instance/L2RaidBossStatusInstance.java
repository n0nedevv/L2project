/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Calendar;

import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class L2RaidBossStatusInstance extends L2NpcInstance
{
	private static final int[] RBOSSES =
	{
		29001,29006,29014,29019,29020,29022,29028,25325,25163,25322,25276,25252,25527,25220,25296,25299,25336
	};
	private static int MBOSS = 25126;
	
	public L2RaidBossStatusInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		generateFirstWindow(player);
	}
	
	private void generateFirstWindow(L2PcInstance activeChar)
	{
		final StringBuilder sb = new StringBuilder();
		
		for (int rboss : RBOSSES)
		{
			
			long delay = RaidBossSpawnManager.getInstance().getRespawntime(rboss);
			String name = NpcTable.getInstance().getTemplate(rboss).getName().toUpperCase();
			
			if (delay == 0)
			{
				sb.append("" + name + "&nbsp;<font color=\"FFFF00\">IS ALIVE!</font><br1>");
			}
			else if (delay < 0)
			{
				sb.append("&nbsp;" + name + "&nbsp;<font color=\"FF0000\">IS DEAD</font><br1>");
			}
			else
			{
				delay = RaidBossSpawnManager.getInstance().getRespawntime(rboss) - Calendar.getInstance().getTimeInMillis();
				sb.append("" + name + "&nbsp;<font color=\"b09979\">:&nbsp;" + ConverTime(delay) + "</font><br1>");
			}
		}
		
		long m_delay = RaidBossSpawnManager.getInstance().getRespawntime(MBOSS);
		String m_name = NpcTable.getInstance().getTemplate(MBOSS).getName().toUpperCase();
		
		String mainBossInfo = "";
		
		if (m_delay == 0)
		{
			mainBossInfo = "WE SHOULD HAVE ACTED<br1><font color=\"FFFF00\">" + m_name + "&nbsp;IS ALIVE!</font><br1>";
		}
		else if (m_delay < 0)
		{
			mainBossInfo = "IT'S ALL OVER<br1><font color=\"FF0000\">&nbsp;" + m_name + "&nbsp;IS DEAD</font><br1>";
		}
		else
		{
			m_delay = m_delay - Calendar.getInstance().getTimeInMillis();
			mainBossInfo = "<font color=\"b09979\">" + ConverTime(m_delay) + "</font><br1>UNTIL OBLIVION OPEN!";
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(getHtmlPath(getNpcId(), 0));
		html.replace("%objectId%", getObjectId());
		html.replace("%bosslist%", sb.toString());
		html.replace("%mboss%", mainBossInfo);
		activeChar.sendPacket(html);
	}
	
	private static String ConverTime(long mseconds)
	{
		long remainder = mseconds;
		
		long hours = (long) Math.ceil((mseconds / (60 * 60 * 1000)));
		remainder = mseconds - (hours * 60 * 60 * 1000);
		
		long minutes = (long) Math.ceil((remainder / (60 * 1000)));
		remainder = remainder - (minutes * (60 * 1000));
		
		long seconds = (long) Math.ceil((remainder / 1000));
		
		return hours + ":" + minutes + ":" + seconds;
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename;
		
		if (val == 0)
			filename = "data/html/mods/RaidBossStatus/" + npcId + ".htm";
		else
			filename = "data/html/mods/RaidBossStatus/" + npcId + "-" + val + ".htm";
		
		if (HtmCache.getInstance().isLoadable(filename))
			return filename;
		
		return "data/html/mods/RaidBossStatus/" + npcId + ".htm";
	}
}