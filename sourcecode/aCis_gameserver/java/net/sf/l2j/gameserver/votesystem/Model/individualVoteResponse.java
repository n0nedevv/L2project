package net.sf.l2j.gameserver.votesystem.Model;

/**
 * @author l2.topgameserver.net
 *
 */
public class individualVoteResponse
{
	private boolean _isVoted;
	private long _diffTime;
	private long _voteSiteTime;
	
	public individualVoteResponse() {
		
	}
	
	public void setIsVoted(boolean isVoted) {
		_isVoted = isVoted;
	}
	public void setDiffTime(long diffTime) {
		_diffTime = diffTime;
	}
	public void setVoteSiteTime(long voteSiteTime) {
		_voteSiteTime = voteSiteTime;
	}
	
	public boolean getIsVoted() {
		return _isVoted;
	}
	public long getDiffTime() {
		return  _diffTime;
	}
	public long getVoteSiteTime() {
		return _voteSiteTime;
	}
}