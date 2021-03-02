package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.xml.ItemData;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.votesystem.Enum.voteSite;
import net.sf.l2j.gameserver.votesystem.Handler.voteManager;
import net.sf.l2j.gameserver.votesystem.Model.Reward;
import net.sf.l2j.gameserver.votesystem.VoteUtil.VoteSiteXml;

/**
 * @author l2.topgameserver.net
 *
 */
public class NpcVote extends Folk
{

	/**
	 * @param objectId
	 * @param template
	 */
	public NpcVote(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(command == null) {
			return;
		}
		int Ordinalsite = Integer.parseInt(command);
		voteManager.getInatance().getReward(player, Ordinalsite);
		showChatWindow(player,0);
		super.onBypassFeedback(player, command);
	}
	
	@Override
	public void showChatWindow(Player player, int val) {
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		StringBuilder sb = new StringBuilder();
		html.setFile(getHtmlPath(getNpcId(), 0));
		for(voteSite vs : voteSite.values()) {
				sb.append("<table bgcolor=000000 width=280><tr>");
				sb.append("<td width=42><img src=\"icon.etc_treasure_box_i08\" width=32 height=32></td>");
				sb.append("<td width=220><table width=220>");
			    sb.append("<tr><td><table width=220><tr><td width=145>On "+String.format("%s",VoteSiteXml.getInstance().getSiteName(vs.ordinal()))+"</td>");
			    if(voteManager.getInatance().checkIndividualAvailableVote(player, vs.ordinal())) {
				sb.append("<td width=75>"+String.format("<button value=\"Get reward\" action=\"bypass -h vote_%s_site %s\" height=17 width=64 back=\"sek.cbui94\" fore=\"sek.cbui92\">",getObjectId(),vs.ordinal())+"</td>");
			    }else {
			    sb.append(String.format("<td width=75 align=center><font color=C68E00>%s</font></td>", voteManager.getInatance().getTimeRemainingWithSampleFormat(player, vs.ordinal())));
			    }
				sb.append("</tr></table></td></tr>");
				sb.append("<tr><td><table width=220><tr>");
				int i=0;
				for(Reward r : VoteSiteXml.getInstance().getRewards(vs.ordinal())) {
					sb.append(String.format("<td width=110 height=32 align=center><font color=BFAF00>%s x%s</font></td>",ItemData.getInstance().getTemplate(r.getItemId()).getName(), r.getItemCount()));
					i++;
						if(i%2==0) {
							sb.append("</tr><tr>");
						}
				}
				sb.append("</tr></table></td></tr></table></td></tr></table><br>");
		}
				html.replace("%everyXtime%",Config.INTERVAL_TO_NEXT_VOTE/(3600*1000));
				html.replace("%enablevote%", sb.toString());
				html.replace("%accountName%",player.getName());
				player.sendPacket(html);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename = "";
		if (val == 0)
			filename = "" + npcId;
		else
			filename = npcId + "-" + val;
		
		return "data/html/mods/votesystem/" + filename + ".html";
	}
	
}