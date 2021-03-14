package net.sf.l2j.gameserver.votesystem.Handler;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.votesystem.DB.globalVoteDB;
import net.sf.l2j.gameserver.votesystem.DB.individualVoteDB;
import net.sf.l2j.gameserver.votesystem.Enum.voteSite;
import net.sf.l2j.gameserver.votesystem.Model.Reward;
import net.sf.l2j.gameserver.votesystem.Model.globalVote;
import net.sf.l2j.gameserver.votesystem.Model.individualVote;
import net.sf.l2j.gameserver.votesystem.Model.individualVoteResponse;
import net.sf.l2j.gameserver.votesystem.VoteUtil.VoteSiteXml;
import net.sf.l2j.gameserver.votesystem.VoteUtil.VoteUtil;

/**
 * @author l2.topgameserver.net
 *
 */
public final class voteManager extends voteHandler{
       private ScheduledFuture<?> _saveGlobalVotes;
	private ScheduledFuture<?> _updateIndividualVotes;
	private ScheduledFuture<?> _autoGlobalVotesReward;
	
	private Map<String,individualVote[]> _foundVoters;
	private globalVote[] _globalVotes = new globalVote[voteSite.values().length];
	
	public voteManager() {
		_foundVoters = new ConcurrentHashMap<>();
		loadVotes();
		loadGlobalVotes();
		checkAllResponseGlobalVotes();
		stopAutoTasks();
		
		if(Config.ENABLE_INDIVIDUAL_VOTE && Config.ENABLE_VOTE_SYSTEM) {
			_updateIndividualVotes = ThreadPool.scheduleAtFixedRate(new AutoUpdateIndividualVotesTask(), 30000, 	Config.NEXT_TIME_TO_AUTO_UPDATE_INDIVIDUAL_VOTES);
		}
		if(Config.ENABLE_GLOBAL_VOTE && Config.ENABLE_VOTE_SYSTEM) {
			_autoGlobalVotesReward = ThreadPool.scheduleAtFixedRate(new AutoGlobalVoteRewardTask(), 10000, Config.NEXT_TIME_TO_CHECK_AUTO_GLOBAL_VOTES_REWARD);
			_saveGlobalVotes = ThreadPool.scheduleAtFixedRate(new AutoSaveGlobalVotesTask(), 30000, Config.NEXT_TIME_TO_AUTO_UPDATE_TOTAL_VOTE);		
		}
	}
	
	private void stopAutoTasks() {
		if(_saveGlobalVotes != null) {
			_saveGlobalVotes.cancel(true);
			_saveGlobalVotes = null;
		}
		if(_updateIndividualVotes != null) {
			_updateIndividualVotes.cancel(true);
			_updateIndividualVotes = null;
		}
		if(_autoGlobalVotesReward != null) {
			_autoGlobalVotesReward.cancel(true);
			_autoGlobalVotesReward = null;
		}
	}
	
	public void getReward(Player player, int ordinalSite) {
		String ip = existIp(player);
			if(ip == null) {
				return;
			}
			individualVoteResponse ivr = getIndividualVoteResponse(ordinalSite,ip,player.getAccountName());
			if(ivr == null) {
				player.sendMessage("We were unable to verify your vote with: "+VoteSiteXml.getInstance().getSiteName(ordinalSite)+", please try again");
				return;
			}
			if(!ivr.getIsVoted()) {
				player.sendMessage(String.format("You haven't vote on %s yet!", VoteSiteXml.getInstance().getSiteName(ordinalSite)));
				return;
		}
			if(!checkIndividualAvailableVote(player,ordinalSite)) {
				player.sendMessage(String.format("You can get the reward again on %s at %s", VoteSiteXml.getInstance().getSiteName(ordinalSite),getTimeRemainingWithSampleFormat(player,ordinalSite)));
				return;
			}
			individualVote iv = new individualVote(ip,ivr.getDiffTime(),ivr.getVoteSiteTime(),ordinalSite,false);

			individualVote[] aiv;
			if(!_foundVoters.containsKey(ip)) {
				 aiv = new individualVote[voteSite.values().length];
				 iv.setAlreadyRewarded(true);
				 aiv[ordinalSite] = iv;
				_foundVoters.put(ip, aiv);
				for(Reward reward : VoteSiteXml.getInstance().getRewards(ordinalSite)) {
					player.getInventory().addItem("VoteSystem", reward.getItemId(), reward.getItemCount(), player, null);
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(reward.getItemId()).addNumber(reward.getItemCount()));
					}
					player.sendMessage(String.format("%s: Thank you for voting for our server, your reward has been delivered.", VoteSiteXml.getInstance().getSiteName(ordinalSite)));
					player.sendPacket(new ItemList(player, true));
			}else {
				aiv = _foundVoters.get(ip);
				iv.setAlreadyRewarded(true);
				aiv[ordinalSite] = iv;
				_foundVoters.replace(ip, aiv);
				for(Reward reward : VoteSiteXml.getInstance().getRewards(ordinalSite)) {
					player.getInventory().addItem("VoteSystem", reward.getItemId(), reward.getItemCount(), player, null);
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(reward.getItemId()).addNumber(reward.getItemCount()));
					}
					player.sendMessage(String.format("%s: Thank you for voting for our server, your reward has been delivered.", VoteSiteXml.getInstance().getSiteName(ordinalSite)));
					player.sendPacket(new ItemList(player, true));
			}
	}

	public boolean checkIndividualAvailableVote(Player player, int ordinalSite) {
		String ip = existIp(player);
		if(_foundVoters.containsKey(ip)) {
			individualVote[] ivs=_foundVoters.get(ip);
			if(ivs[ordinalSite] == null) {
				return true;
			}
			if(ivs[ordinalSite] != null) {
				individualVote iv = ivs[ordinalSite];
				if(getTimeRemaining(iv)<0) {
					return true;
				}
			}
		}else {
			return true;
		}

		return false;
	}
	
	public static long getTimeRemaining(individualVote iv) {
		long timeRemaining = 0L;
			timeRemaining = (iv.getVotingTimeSite() + Config.INTERVAL_TO_NEXT_VOTE - (iv.getDiffTime()>0? iv.getDiffTime() : -1*iv.getDiffTime())) -System.currentTimeMillis();
		return timeRemaining;
	}
	
	public String getTimeRemainingWithSampleFormat(Player player, int ordinalSite) {
		String ip = existIp(player);
		String timeRemainingWithSampleFormat ="";
		if(_foundVoters.containsKey(ip)) {
			individualVote[] ivs=_foundVoters.get(ip);
			if(ivs[ordinalSite] != null) {
				individualVote iv = ivs[ordinalSite];
				long timeRemaining = getTimeRemaining(iv); 
				if(timeRemaining>0) {
					timeRemainingWithSampleFormat = CalculateTimeRemainingWithSampleDateFormat(timeRemaining);
					return timeRemainingWithSampleFormat;
				}
			}
		}
		return timeRemainingWithSampleFormat;
	}
	
	public static String CalculateTimeRemainingWithSampleDateFormat(long timeRemaining) {
		long t = timeRemaining/1000;
		int hours = Math.round((t/3600%24));
		 int minutes = Math.round((t/60)%60);
		 int seconds = Math.round(t%60);
		 return String.format("%sH:%sm:%ss", hours,minutes,seconds);
	}
	
	@SuppressWarnings("static-method")
	public String existIp(Player p) {
		 
		 GameClient client = p.getClient();
		 if(client.getConnection() != null && client.getPlayer() != null && !client.isDetached()) {
		 try
		{
			 return client.getConnection().getInetAddress().getHostAddress();
		}
		catch (Exception e)
		{
				e.printStackTrace();
		}
	}
		return null;
		 
	 }
	
	public final void loadVotes() {
		_foundVoters = individualVoteDB.getInstance().getVotesDB();
	}
	protected void loadGlobalVotes(){
		_globalVotes = globalVoteDB.getInstance().getGlobalVotes();
	}
	public void saveVotes() {
		individualVoteDB.getInstance().SaveVotes(_foundVoters);
	}
	
	protected void AutoGlobalVoteReward() {
		HashSet<String> ipList = new HashSet<>();
		for(voteSite vs : voteSite.values()) {
			
			new Thread(() -> {
				checkNewUpdate(vs.ordinal());
				if(_globalVotes[vs.ordinal()].getCurrentVotes() >= _globalVotes[vs.ordinal()].getVotesLastReward() + (vs.ordinal() == voteSite.L2SERVERS.ordinal() ? 25*Config.GLOBAL_VOTES_AMOUNT_TO_NEXT_REWARD : Config.GLOBAL_VOTES_AMOUNT_TO_NEXT_REWARD)) {
					_globalVotes[vs.ordinal()].setVotesLastReward(_globalVotes[vs.ordinal()].getVotesLastReward() + (vs.ordinal() == voteSite.L2SERVERS.ordinal() ? 25*Config.GLOBAL_VOTES_AMOUNT_TO_NEXT_REWARD : Config.GLOBAL_VOTES_AMOUNT_TO_NEXT_REWARD));
					for(Player player : World.getInstance().getPlayers()) {
						String ip = existIp(player);
						if(ip == null) {
							continue;
						}
						if(ipList.contains(ip)) {
							continue;
						}
						for(Reward reward : VoteSiteXml.getInstance().getRewards(11)) {
							player.getInventory().addItem("VoteSystem: ", reward.getItemId(), reward.getItemCount(), player, null);
							player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(reward.getItemId()).addNumber(reward.getItemCount()));
						}
						ipList.add(ip);
						player.sendPacket(new ItemList(player, true));
					}
					World.announceToOnlinePlayers(VoteUtil.Sites[vs.ordinal()]+": All players has been rewarded, please check your inventory", true);
				}else {
					String encourage =""; 
					int nextReward = _globalVotes[vs.ordinal()].getVotesLastReward() + (vs.ordinal() == voteSite.L2SERVERS.ordinal() ? 25*Config.GLOBAL_VOTES_AMOUNT_TO_NEXT_REWARD : Config.GLOBAL_VOTES_AMOUNT_TO_NEXT_REWARD);
					encourage = String.format("Vote for %s current Votes: %s, next quantity of votes to reward : %s, need votes to next reward: %s", VoteUtil.Sites[vs.ordinal()], _globalVotes[vs.ordinal()].getCurrentVotes(),nextReward, nextReward-_globalVotes[vs.ordinal()].getCurrentVotes() );
					World.announceToOnlinePlayers(encourage, true);
			}
			}).start();
			
		}
	}
	
	protected void AutoSaveGlobalVotes() {
		globalVoteDB.getInstance().saveGlobalVotes(_globalVotes);
	}
	
	protected synchronized void  AutoUpdateIndividualVotes() {
		AutoCleanInnecesaryIndividualVotes();
		individualVoteDB.getInstance().SaveVotes(_foundVoters);
	}
	
	protected synchronized void AutoCleanInnecesaryIndividualVotes() {
		HashSet<individualVote> removeVotes= new HashSet<>();
		for(Map.Entry<String, individualVote[]> ivs : _foundVoters.entrySet()) {
			for(individualVote individualvote : ivs.getValue()) {
				if(individualvote == null)
					continue;
				if(getTimeRemaining(individualvote) < 0) {
					removeVotes.add(individualvote);
					if(_foundVoters.containsKey(individualvote.getVoterIp())) {
						if(_foundVoters.get(individualvote.getVoterIp())[individualvote.getVoteSite()] != null) {
							_foundVoters.get(individualvote.getVoterIp())[individualvote.getVoteSite()] = null;
						}
					}
				}
			}
		}
		individualVoteDB.getInstance().DeleteVotes(removeVotes);
	}
	
	public void checkAllResponseGlobalVotes() {
		for(voteSite vs : voteSite.values()) {
			new Thread(()-> {
				checkNewUpdate(vs.ordinal());
			});
		}
	}
	
	public void checkNewUpdate(int ordinalSite) {
			int globalVotesResponse = getGlobalVotesResponse(ordinalSite); 
			if(globalVotesResponse == -1) {
				return;
			}
			_globalVotes[ordinalSite].setCurrentVotes(globalVotesResponse);
			int last = globalVotesResponse - (ordinalSite == voteSite.L2SERVERS.ordinal() ? 25 * Config.GLOBAL_VOTES_AMOUNT_TO_NEXT_REWARD : Config.GLOBAL_VOTES_AMOUNT_TO_NEXT_REWARD);
			if(last <0 ) {
				_globalVotes[ordinalSite].setVotesLastReward(0);
				return;
			} 
			if((_globalVotes[ordinalSite].getVotesLastReward() + (ordinalSite == voteSite.L2SERVERS.ordinal() ? 25 * Config.GLOBAL_VOTES_AMOUNT_TO_NEXT_REWARD : Config.GLOBAL_VOTES_AMOUNT_TO_NEXT_REWARD)) < globalVotesResponse) {
				_globalVotes[ordinalSite].setVotesLastReward(globalVotesResponse);
				return;
			}
	}
	
	public void Shutdown() {
		AutoSaveGlobalVotes();
		AutoCleanInnecesaryIndividualVotes();
		AutoUpdateIndividualVotes();
	}

	protected class AutoGlobalVoteRewardTask implements Runnable {

		@Override
		public void run()
		{
			AutoGlobalVoteReward();
			
		}
		
	}
	
	protected class AutoSaveGlobalVotesTask implements Runnable {
		
		@Override
		public void run()
		{
			AutoSaveGlobalVotes();
			
		}
	
	}
	
	protected class AutoUpdateIndividualVotesTask implements Runnable {

		@Override
		public void run()
		{
			AutoUpdateIndividualVotes();
			
		}
		
	}
	
	public static voteManager getInatance() {
		return SingleHolder.INSTANCE;
	}
	
	private static class SingleHolder {
		protected static final voteManager INSTANCE = new voteManager();
	}
}