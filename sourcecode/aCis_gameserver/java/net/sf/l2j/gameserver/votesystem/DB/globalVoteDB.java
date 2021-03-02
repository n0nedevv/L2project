package net.sf.l2j.gameserver.votesystem.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sf.l2j.commons.logging.CLogger;

import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.votesystem.Enum.voteSite;
import net.sf.l2j.gameserver.votesystem.Model.globalVote;


/**
 * @author l2.topgameserver.net
 *
 */
public class globalVoteDB
{
	public static final CLogger LOGGER = new CLogger(globalVoteDB.class.getName());
	private globalVote[] _globalVotes;
	private globalVoteDB() {
		_globalVotes = new globalVote[voteSite.values().length];
		loadGlobalVotes();
	}
	
	public void loadGlobalVotes() {
		try(Connection con = ConnectionPool.getConnection();
			PreparedStatement ps = con.prepareStatement("Select voteSite,lastRewardVotes from globalvotes");
			ResultSet rs = ps.executeQuery();){
			if(rs.getRow() == 0){
                for(voteSite vs : voteSite.values()){
                    globalVote gv = new globalVote();
                         gv.setVoteSite(vs.ordinal());
                         gv.setVotesLastReward(0);
                         _globalVotes[gv.getVoyeSite()] = gv;
                }
                return;
            }
			while(rs.next()) {
				globalVote gv = new globalVote();
				gv.setVoteSite(rs.getInt("voteSite"));
				gv.setVotesLastReward(rs.getInt("lastRewardVotes"));
				_globalVotes[gv.getVoyeSite()] = gv;
			}
			ps.close();
			con.close();
			
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	public void saveGlobalVote(globalVote gb) {
		try(Connection con = ConnectionPool.getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO globalvotes(voteSite,lastRewardVotes) VALUES(?,?)"
			+ "ON DUPLICATE KEY UPDATE voteSite = VALUES(voteSite), lastRewardVotes = VALUES(lastRewardVotes)"))
		
		{
			ps.setInt(1, gb.getVoyeSite());
			ps.setInt(2, gb.getVotesLastReward());
			ps.executeUpdate();
			
			ps.close();
			con.close();
			
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void saveGlobalVotes(globalVote[] globalVotes) {
		try(Connection con = ConnectionPool.getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO globalvotes(voteSite,lastRewardVotes) VALUES(?,?)"
			+ "ON DUPLICATE KEY UPDATE voteSite = VALUES(voteSite), lastRewardVotes = VALUES(lastRewardVotes)"))
		
		{
			for(voteSite vs : voteSite.values()) {
			globalVote gb = globalVotes[vs.ordinal()];
			ps.setInt(1, gb.getVoyeSite());
			ps.setInt(2, gb.getVotesLastReward());
			ps.addBatch();
			}
			ps.executeBatch();
			
			ps.close();
			con.close();
			
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public globalVote[] getGlobalVotes() {
		return _globalVotes;
	}
	public static final globalVoteDB  getInstance() {
		return SingleHolder.INSTANCE;
	}
	private static final class SingleHolder {
		protected static final globalVoteDB INSTANCE = new globalVoteDB();
	}
}