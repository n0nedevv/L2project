package net.sf.l2j.gameserver.handler.customcommandHandler;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.ICustomCommandsHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.votesystem.DB.individualVoteDB;
import net.sf.l2j.gameserver.votesystem.Enum.voteSite;
import net.sf.l2j.gameserver.votesystem.Handler.voteManager;

/**
 * @author l2.topgameserver.net
 *
 */
public class VoteCommandHandler implements ICustomCommandsHandler
{

	@Override
	public void useCustomCommand(String command, Player player)
	{
		
		if (command.equalsIgnoreCase(Config.VOTING_COMMAND))
		{	
			if(player.isInJail()) {
				player.sendMessage("You cannot use this function while you are jailed");
				return;
			}
			if(!Config.ENABLE_VOTE_SYSTEM) {
				player.sendMessage("The rewards system has been disabled by your administrator");
				return;
			}
			if(!Config.ENABLE_INDIVIDUAL_VOTE) {
				player.sendMessage("The individual reward system is disabled");
				return;
			}
			if(!Config.ENABLE_VOTING_COMMAND) {
				player.sendMessage("Voting command reward is disabled");
				return;
			}
			
			for(voteSite vs : voteSite.values()) {
				new Thread(()->{
					voteManager.getInatance().getReward(player, vs.ordinal());
				}).start();
			}
			
		}
		if(player.isGM() && command.equalsIgnoreCase(".reloadinvotes")) {
			individualVoteDB.getInstance().loadVotes();
			player.sendMessage("All individual votes has been recharged");
		}
	}
	
	
	@Override
	public String[] getCustomCommandList()
	{
		return new String[]
		{
			Config.VOTING_COMMAND,
			".reloadinvotes",
		};
	}
	public static VoteCommandHandler getInstance() 
	{
		return SingletonHolder._instance;
	}
	private static class SingletonHolder
	{
		protected static final VoteCommandHandler _instance = new VoteCommandHandler();
	}
}