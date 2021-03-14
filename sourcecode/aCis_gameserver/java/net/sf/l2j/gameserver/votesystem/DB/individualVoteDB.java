package net.sf.l2j.gameserver.votesystem.DB;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.votesystem.Enum.voteSite;


import net.sf.l2j.gameserver.votesystem.Model.individualVote;

/**
 * @author l2.topgameserver.net
 *
 */
public class individualVoteDB
{
	private static final Logger LOGGER = Logger.getLogger(individualVoteDB.class.getName());
	private Map<String,individualVote[]> _votes;
	private Statement st;
	
	
	private individualVoteDB() {
		_votes = new HashMap<>();
		loadVotes();
	}
	
	public void loadVotes() {
		
		_votes.clear();
		try(Connection con = ConnectionPool.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT voterIp,voteSite,diffTime,votingTimeSite,alreadyRewarded FROM individualvotes");
			ResultSet rs = ps.executeQuery();)
		{
			individualVote[] ivs = new individualVote[voteSite.values().length];
			while(rs.next()) {
				individualVote iv = new individualVote();
				iv.setVoterIp(rs.getString("voterIp"));
				iv.setVoteSite(rs.getInt("voteSite"));
				iv.setDiffTime(rs.getLong("diffTime"));
				iv.setVotingTimeSite(rs.getLong("votingTimeSite"));
				iv.setAlreadyRewarded(rs.getBoolean("alreadyRewarded"));
				
				if(_votes.containsKey(iv.getVoterIp())) {
					if(_votes.get(iv.getVoterIp())[iv.getVoteSite()] == null) {
						ivs[iv.getVoteSite()] = iv;
						_votes.replace(iv.getVoterIp(), ivs);
					}
				}else {
					ivs[iv.getVoteSite()] = iv;
					_votes.put(iv.getVoterIp(), ivs);
					
					
				}
			}
			
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public void SaveVotes(Map<String,individualVote[]> votes) {
		
		if(votes == null)
			return;
		if(votes.size() == 0) {
			return;
		}
			try(Connection con = ConnectionPool.getConnection();
				PreparedStatement ps = con.prepareStatement("INSERT INTO individualvotes(voterIp,voteSite,diffTime,votingTimeSite,alreadyRewarded) VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE "
				+ "voterIp = VALUES(voterIp), voteSite = VALUES(voteSite), diffTime = VALUES(diffTime), votingTimeSite = VALUES(votingTimeSite),alreadyRewarded = VALUES(alreadyRewarded)");)
			{
				
				for(Map.Entry<String, individualVote[]> ivm : votes.entrySet()) {
					for(individualVote iv : ivm.getValue()) {
						if(iv == null)
							continue;
					ps.setString(1, iv.getVoterIp());
					ps.setInt(2, iv.getVoteSite());
					ps.setLong(3, iv.getDiffTime());
					ps.setLong(4, iv.getVotingTimeSite());
					ps.setBoolean(5, iv.getAlreadyRewarded());
					ps.addBatch();
					}
				}
				ps.executeBatch();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
	}
	
		public void SaveVote(individualVote vote) {
		
		if(vote == null)
			return;
		
			try(
				Connection con = ConnectionPool.getConnection();
				PreparedStatement ps = con.prepareStatement("INSERT INTO individualvotes(voterIp,voteSite,diffTime,votingTimeSite,alreadyRewarded) VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE"
				+ "voterIp = VALUES(voterIp), voteSite = VALUES(voteSite), diffTime = VALUES(diffTime), votingTimeSite = VALUES(votingTimeSite), alreadyRewarded = VALUES(alreadyRewarded)");)
			{
					ps.setString(1, vote.getVoterIp());
					ps.setInt(2, vote.getVoteSite());
					ps.setLong(3, vote.getDiffTime());
					ps.setLong(4, vote.getVotingTimeSite());
					ps.setBoolean(5, vote.getAlreadyRewarded());
					ps.executeUpdate();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
	}

	public void DeleteVotes(HashSet<individualVote> deleteVotes) {
		if(deleteVotes == null) {
			return;
		}
		if(deleteVotes.size() == 0) {
			return;
		}
		try(Connection con = ConnectionPool.getConnection();) {
			st = con.createStatement();
			for(individualVote iv : deleteVotes) {
			String sql = String.format("Delete from individualvotes where voterIp = '%s' AND voteSite = %s", iv.getVoterIp(),iv.getVoteSite());
			st.addBatch(sql);
			}
			int[] result = st.executeBatch();
			st.close();
			con.close();
			LOGGER.info(result.length+" Innecesary votes has been deleted");
			
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Map<String,individualVote[]> getVotesDB(){
		return _votes;
	}

	public static final individualVoteDB getInstance()
	{
		return SingleHolder.INSTANCE;
	}
	
	private static final class SingleHolder {
		protected static final individualVoteDB INSTANCE = new individualVoteDB();
	}
}