package net.sf.l2j.gameserver.votesystem.Handler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.votesystem.Enum.voteSite;
import net.sf.l2j.gameserver.votesystem.Model.individualVoteResponse;
import net.sf.l2j.gameserver.votesystem.VoteUtil.VoteSiteXml;
import net.sf.l2j.gameserver.votesystem.VoteUtil.VoteUtil;

/**
 * @author l2.topgameserver.net
 *
 */
public class voteHandler {
   public static final Logger LOGGER = Logger.getLogger(voteHandler.class.getName());
   
	protected static String getNetWorkResponse(String URL,int ordinal) {
		if ((ordinal == voteSite.L2NETWORK.ordinal()) && ("".equals(Config.VOTE_NETWORK_API_KEY) || "".equals(Config.VOTE_NETWORK_LINK) || "".equals(Config.VOTE_NETWORK_USER_NAME)))
		{
			return "";
		}
				
				StringBuffer response = new StringBuffer();
				try {
				String API_URL = Config.VOTE_NETWORK_LINK;
				String detail = URL;
				String postParameters = "";
				postParameters +="apiKey="+VoteUtil.between("apiKey=", detail, "&type=");
				postParameters += "&type="+VoteUtil.between("&type=", detail, "&player");
				String beginIndexPlayer = "&player=";
				String player = detail.substring(detail.indexOf(beginIndexPlayer)+beginIndexPlayer.length());
				
				if (player != null && !player.equals(""))
					postParameters += "&player=" + player;

				byte[] postData = postParameters.getBytes(Charset.forName("UTF-8"));
				URL url = new URL(API_URL);
				HttpURLConnection con = (HttpURLConnection)url.openConnection();
				con.setConnectTimeout(5000);
				con.setRequestMethod("POST");
				con.setRequestProperty("Content-Length", Integer.toString(postData.length));
				con.setRequestProperty("User-Agent", "Mozilla/5.0");
				con.setDoOutput(true);
				
				DataOutputStream os = new DataOutputStream(con.getOutputStream());
				os.write(postData);
				os.flush();
				os.close();
				
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
					
					return response.toString();
					
				} catch (Exception e) {
				LOGGER.warning(VoteUtil.Sites[ordinal]+ " Say: An error ocurred "+ e.getCause());
				return "";
				}
	}
	
	protected static String getResponse(String Url, int ordinal) 
	{
		if ((ordinal == voteSite.L2NETWORK.ordinal()) && ("".equals(Config.VOTE_NETWORK_API_KEY) || "".equals(Config.VOTE_NETWORK_LINK) || "".equals(Config.VOTE_NETWORK_USER_NAME)))
		{
			return "";
		}
                
		try
		  {
			int responseCode = 0;
			URL objUrl = new URL(Url);
			HttpURLConnection con = (HttpURLConnection) objUrl.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			con.setConnectTimeout(5000);
			responseCode = con.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				String inputLine;
				StringBuffer response = new StringBuffer();
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				return response.toString();
			} 
		
		  }
		       catch (Exception e)
		       {
		    	   LOGGER.warning(VoteSiteXml.getInstance().getSiteName(ordinal)+" Say: An error ocurred "+e.getCause());
		           return "";
		       }

		return "";
	}
	
	
	public static individualVoteResponse getIndividualVoteResponse(int ordinal,String ip, String AccountName) 
	{
		String response = "";
		boolean isVoted = false;
		long voteSiteTime = 0L, diffTime = 0L;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		individualVoteResponse ivr = new individualVoteResponse();
		
			switch(ordinal) 
			{
				case 0:
					response = getResponse(getIndividualUrl(ordinal,ip,null),ordinal);
					isVoted = (response == "") ? false : Boolean.parseBoolean(VoteUtil.between("\"already_voted\":", response, ",\"vote_time\""));
						if(isVoted) {
                           try
							{
								voteSiteTime = format.parse(VoteUtil.between("\"vote_time\":\"", response, "\",\"server_time\"")).getTime();
								diffTime = System.currentTimeMillis() - format.parse(VoteUtil.between("\"server_time\":\"", response, "\"}")).getTime();
							}
							catch (ParseException e)
							{
								e.printStackTrace();
							}					
						}
						break;
						
				case 1:
					response = getResponse(getIndividualUrl(ordinal,ip,null),ordinal);
					isVoted = (response == "") ? false : Boolean.parseBoolean(VoteUtil.between("\"isvoted\":", response.toString().toLowerCase().replaceAll("\n", "").replaceAll(" ", ""), ",\"votetime").replaceAll("\"", ""));
					if(isVoted) {
						try
						{
							voteSiteTime = (Long.parseLong(VoteUtil.between("\"votetime\":", response.toString().toLowerCase().replaceAll("\n", "").replaceAll(" ", ""), ",\"servertime")))*1000;
							diffTime = System.currentTimeMillis() - (Long.parseLong(VoteUtil.between("\"servertime\":", response.toLowerCase().replaceAll("\n", "").replaceAll(" ", ""), "}")))*1000;
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					break;
						
				case 2:
					response = getResponse(getIndividualUrl(ordinal,ip,null),ordinal);
					isVoted = (response == "") ? false :Boolean.parseBoolean(response);
					if(isVoted) {
							voteSiteTime = System.currentTimeMillis();
							diffTime = 0;
					}
					break;
					
				case 3:
					response = getResponse(getIndividualUrl(ordinal,ip,null),ordinal);
					isVoted = (VoteUtil.between("\"status\":\"", response, "\",\"date\"") != "" && Integer.parseInt(VoteUtil.between("\"status\":\"", response, "\",\"date\"")) == 1) ?  true : false;
					if(isVoted) {
							try
							{
								voteSiteTime = System.currentTimeMillis();
								diffTime = 0;
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
							
					}
					break;
				
				case 4:
					response = getResponse(getIndividualUrl(ordinal,ip,null),ordinal);
					isVoted = (response == "") ? false : Boolean.parseBoolean(VoteUtil.between("\"voted\":", response, ",\"voteTime\""));
					if(isVoted) {
					try
					{
						voteSiteTime = format.parse(VoteUtil.between("\"voteTime\":\"", response, "\",\"hopzoneServerTime\"")).getTime();
						diffTime = System.currentTimeMillis() - format.parse(VoteUtil.between("\"hopzoneServerTime\":\"", response, "\",\"status_code\":")).getTime();
					}
					catch (ParseException e)
					{
						e.printStackTrace();
					}
				}
					break;
					
				case 5:
					response = getResponse(getIndividualUrl(ordinal,ip,null),ordinal);
					isVoted = (!"".equals(response) && Integer.parseInt(response) == 1) ? true : false;
					if(isVoted) {
					voteSiteTime = System.currentTimeMillis();
					diffTime = 0;
					}
					break;
					
				case 6:
					response = getResponse(getIndividualUrl(ordinal,ip,null),ordinal);
					isVoted = ("".equals(response)) ? false : Boolean.parseBoolean(VoteUtil.between("\"voted\":", response, ",\"voteTime\""));
					if(isVoted) {
						try
						{
							voteSiteTime = System.currentTimeMillis();
							diffTime = 0;
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
						
					}
					break;
					
				case 7:
					response = getResponse(getIndividualUrl(ordinal,ip,null),ordinal);
					isVoted = (VoteUtil.between("\"status\":\"", response, "\",\"server_time\"") != "" && Integer.parseInt(VoteUtil.between("\"status\":\"", response, "\",\"server_time\"")) == 1) ? true : false;
					if(isVoted) {
							try
							{
								voteSiteTime = System.currentTimeMillis();
								diffTime = 0;
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
					}
					break;
					
				case 8:
					response = getResponse(getIndividualUrl(ordinal,ip,null),ordinal);
					isVoted = (response == "") ? false : Boolean.parseBoolean(VoteUtil.between("\"is_voted\":", response, ",\"vote_time\""));
					if(isVoted) {
						try
						{
							voteSiteTime = (Long.parseLong(VoteUtil.between("\"vote_time\":", response, ",\"server_time\"")))*1000;
							diffTime = System.currentTimeMillis() - Long.parseLong(VoteUtil.between("\"server_time\":",response,"}}"))*1000;
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					break;
					
				case 9:
					response = getResponse(getIndividualUrl(ordinal,ip,null),ordinal);
					isVoted = (response == "") ? false : Boolean.parseBoolean(VoteUtil.between("\"isVoted\": ", response, ",\"voteTime\""));
					if(isVoted) {
						voteSiteTime = Long.parseLong(VoteUtil.between("\"voteTime\": \"", response, "\",\"serverTime\""))*1000;
						diffTime = System.currentTimeMillis() - Long.parseLong(VoteUtil.between("\"serverTime\": ",response,"}}"))*1000;
					}
					break;
					
				case 10:
					response = getResponse(getIndividualUrl(ordinal,ip,null),ordinal);
					isVoted = (response == "") ? false : Boolean.parseBoolean(response);
					if(isVoted) {
							voteSiteTime = System.currentTimeMillis();
							diffTime = 0;
					}
					break;
					
			}
				if(!response.equals("")) {
				ivr.setIsVoted(isVoted);
				ivr.setDiffTime(diffTime);
				ivr.setVoteSiteTime(voteSiteTime);
				return ivr;
				}
				return null;
	}
	
	public int getGlobalVotesResponse(int ordinal) 
	{
		
		String response = "";
		int totalVotes = 0;
		
		switch(ordinal) 
		{
			case 0:
				response = getResponse(getGlobalUrl(ordinal), ordinal);
				response = VoteUtil.between("\"getVotes\":",response,"}");
				totalVotes = (!"".equals(response)) ? Integer.parseInt(response) : -1;
				break;
				
			case 1:
				response = getResponse(getGlobalUrl(ordinal), ordinal);
				response = VoteUtil.between("server_votes\":",response.replace(" ", ""),",\"server_rank");
				totalVotes = (!"".equals(response)) ? Integer.parseInt(response) : -1;
				break;
				
			case 2:
				response = getResponse(getGlobalUrl(ordinal), ordinal);
				totalVotes = (!"".equals(response)) ? Integer.parseInt(response) : -1;
				break;
				
			case 3:
				response = VoteUtil.getResponse(getGlobalUrl(ordinal), ordinal);
				response = VoteUtil.between("Votes:</th><th><a class='votes'>", response, "</a></th></tr><tr><th>Clicks:");
				totalVotes = (!"".equals(response)) ? Integer.parseInt(response) : -1;
				break;
				
			case 4:
				response = getResponse(getGlobalUrl(ordinal), ordinal);
				response = VoteUtil.between("\"totalvotes\":",response,",\"status_code\"");
				totalVotes = (!"".equals(response)) ? Integer.parseInt(response) : -1;
				break;
				
			case 5:
				String responseNetwork = getNetWorkResponse(getGlobalUrl(ordinal),ordinal);
				totalVotes = (!"".equals(responseNetwork)) ? Integer.parseInt(responseNetwork) : -1;
				break;
				
			case 6:
				response = VoteUtil.getResponse(getGlobalUrl(ordinal), ordinal);
				response = VoteUtil.between("VOTE <span>", response.toString().replaceAll("\n", ""), "</span>");
				
				totalVotes = (!"".equals(response)) ? Integer.parseInt(response) : -1;
				break;
				
			case 7:
				response = VoteUtil.getResponse(getGlobalUrl(ordinal), ordinal);
				response = VoteUtil.between("nicas:</b> ", response, "<br /><br />");
				totalVotes = (!"".equals(response)) ? Integer.parseInt(response) : -1;
				break;
				
			case 8:
				response = getResponse(getGlobalUrl(ordinal), ordinal);
				response = VoteUtil.between("\"monthly_votes\":",response,"}}");
				totalVotes = (!"".equals(response)) ? Integer.parseInt(response) : -1;
				break;
				
			case 9:
			  	response = getResponse(getGlobalUrl(ordinal), ordinal);
			  	response = VoteUtil.between("\"totalVotes\":\"", response, "\",\"serverRank\"");
				totalVotes = (!"".equals(response)) ? Integer.parseInt(response) : -1;
				break;
				
			case 10:
				response = getResponse(getGlobalUrl(ordinal), ordinal);
				totalVotes = (!"".equals(response)) ? Integer.parseInt(response) : -1;
				break;
		}
		
		
		return totalVotes;
	}
	
	public static String getIndividualUrl(int ordinal,String ip,String AccountName) 
	{
		String url = "";
		ip = (Config.TEST_IP.equalsIgnoreCase("off") || Config.TEST_IP.equalsIgnoreCase("") ) ? ip : Config.TEST_IP;
		switch(ordinal) {
			case 0:
			    //l2.topgameserver.net
				url =  String.format("%sAPI_KEY=%s/getData/%s", Config.VOTE_LINK_TGS, Config.TGS_API_KEY,ip); 
				break;
				
			case 1:
				//itopz.com
				url = String.format("%s%s/%s/%s", Config.VOTE_LINK_ITOPZ,Config.ITOPZ_API_KEY,Config.ITOPZ_SRV_ID,ip); 
				break;
				
			case 2:
				//l2top.co
				url = String.format("%sVoteCheck.php?id=%s&ip=%s", Config.VOTE_LINK_TOP_CO,Config.TOP_CO_SRV_ID,ip); 
				break;
				
			case 3:
				//l2votes.com
				url = String.format("%sapi.php?apiKey=%s&ip=%s", Config.VOTE_LINK_VTS,Config.VTS_API_KEY,ip); 
				break;
				
			case 4:
				//hopzone.net
				url = String.format("%svote?token=%s&ip_address=%s",Config.VOTE_LINK_HZ,Config.HZ_API_KEY,ip);
				break;
				
			case 5:
				//l2network.eu
				url = String.format("https://l2network.eu/index.php?a=in&u=%s&ipc=%s", Config.VOTE_NETWORK_USER_NAME,ip);
				break;
				
			case 6:
				//l2topservers.com
				url = String.format("%stoken=%s&ip=%s", Config.VOTE_LINK_TSS,Config.TSS_API_TOKEN,ip); 
				break;
				
			case 7:
				//top.l2jbrasil.com
				url = String.format("%susername=%s&ip=%s&type=json",Config.BRASIL_VOTE_LINK,Config.BRASIL_USER_NAME,ip); 
				break;
				
			case 8:
				//mmotop
				url = String.format("%s%s/%s", Config.VOTE_LINK_MMOTOP,Config.MMOTOP_API_KEY,ip); 
				break;
				
			case 9:
				//topzone.com
				url = String.format("%svote?token=%s&ip=%s", Config.VOTE_LINK_TZ,Config.TZ_API_KEY,ip); 
				break;
				
			case 10:
				//l2servers.com
				url = String.format("%scheckip.php?hash=%s&server_id=%s&ip=%s", Config.VOTE_LINK_SERVERS,Config.SERVERS_HASH_CODE,Config.SERVERS_SRV_ID,ip); 
				break;
		}
		
		return url;
	}
	
	public String getGlobalUrl(int ordinal) 
	{
		String url = "";
		
		switch(ordinal) {
			case 0:
			    //l2.topgameserver.net
				url = String.format("%sAPI_KEY=%s/getData", Config.VOTE_LINK_TGS, Config.TGS_API_KEY);
				break;
			
			case 1:
				//itopz.com
				url = String.format("%s%s/%s", Config.VOTE_LINK_ITOPZ,Config.ITOPZ_API_KEY,Config.ITOPZ_SRV_ID); 
				break;

			case 2:
				//l2top.co
				url = String.format("%sVoteCheck_Total.php?id=%s", Config.VOTE_LINK_TOP_CO,Config.TOP_CO_SRV_ID); 
				break;
				
			case 3:
				//l2votes.com
				url = String.format("%sserverPage.php?sid=%s",Config.VOTE_LINK_VTS,Config.VTS_SID); 
				break;
				
			case 4:
				//hopzone.net
				url = String.format("%svotes?token=%s", Config.VOTE_LINK_HZ,Config.HZ_API_KEY);
				break;
				
			case 5:
				//l2network.eu
				url = String.format("apiKey=%s&type=%s&player=",Config.VOTE_NETWORK_API_KEY,1);
				break;
				
			case 6:
				//l2topservers
				url = String.format("https://l2topservers.com/l2top/%s/%s", Config.TS_SRV_ID,Config.TS_DOMAIN_NAME); 
				break;
								
			case 7:
				//top.l2jbrasil.com
				url = String.format("https://top.l2jbrasil.com/index.php?a=stats&u=%s",Config.BRASIL_USER_NAME); 
				break;
				
			case 8:
				//mmotop.eu/l2/
				url = String.format("%s%s/info/", Config.VOTE_LINK_MMOTOP,Config.MMOTOP_API_KEY); 
				break;
				
			case 9:
				//l2topzone.com
				url = String.format("%sserver_%s/getServerData", Config.VOTE_LINK_TZ,Config.TZ_API_KEY); 
				break;
			
			case 10:
				//l2servers.com
				url = String.format("%syearlyvotes.php?server_id=%s", Config.VOTE_LINK_SERVERS,Config.SERVERS_SRV_ID);
				break;
		}
		
		return url;
	}
}