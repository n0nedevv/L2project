package net.sf.l2j.gameserver.votesystem.Model;


/**
 * @author l2.topgameserver.net
 *
 */
public class individualVote
{
	private String _voterIp;
	private long _diffTime;
	private long _votingTimeSite;
	private int _voteSite;
	private boolean _alreadyRewarded;
	
	
	public individualVote(String voterIp, long diffTime, long votingTimeSite, int voteSite, boolean alreadyRewarded){
		_voterIp = voterIp;
		_diffTime = diffTime;
		_votingTimeSite = votingTimeSite;
		_voteSite = voteSite;
		_alreadyRewarded = alreadyRewarded;
	}
	
	public individualVote(){
		
	}
	
	public void setVoterIp(String voterIp) {
		_voterIp = voterIp;
	}
	
	public void setDiffTime(long diffTime) {
		_diffTime = diffTime;
	}
	
	public void setVotingTimeSite(long votingTimeSite) {
		_votingTimeSite = votingTimeSite;
	}
	
	public void setVoteSite(int voteSite) {
		_voteSite = voteSite;
	}
	
	public void setAlreadyRewarded(boolean alreadyRewarded) {
		_alreadyRewarded = alreadyRewarded;
	}
	
	public String getVoterIp() {
		return _voterIp;
	}
	
	public long getDiffTime() {
		return _diffTime;
	}
	
	public long  getVotingTimeSite() {
		return _votingTimeSite;
	}
	
	public int getVoteSite() {
		return _voteSite;
	}
	
	public boolean getAlreadyRewarded() {
		return _alreadyRewarded;
	}
	
}