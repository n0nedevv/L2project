package net.sf.l2j.gameserver.votesystem.VoteUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import net.sf.l2j.commons.logging.CLogger;


/**
 * @author l2.topgameserver.net
 *
 */
public final class VoteUtil
{
	public static final CLogger LOGGER = new CLogger(VoteUtil.class.getName());
	
	private static String voteTimeZones[] = {
		"topgameserver.net=Europe/Berlin",
		"itopz.com=America/New_York",
		"l2top.co=Europe/London",
		"l2votes.com=GMT",
		"hopzone.net=Europe/Athens",
		"l2network.eu=America/Chicago",
		"l2topservers.com=Europe/Athens",
		"top.l2jbrasil.com=America/Sao_Paulo",
		"mmotop.eu=America/Chicago",
		"l2topzone.com=America/Chicago",
		"l2servers.com=America/Chicago",
		};
		
		public static final long getTimeVotingSite(int ordinalSite) {
			LocalDateTime ldt = LocalDateTime.now(ZoneId.of(voteTimeZones[ordinalSite].split("=")[1]));
		    ZonedDateTime zdt = ldt.atZone(ZoneId.systemDefault());
		    long millis = zdt.toInstant().toEpochMilli();
		    return millis;
		}
		
		public static final String Sites[] =
		{
			"L2.TopGameServer.net",
			"ITopZ.com",
			"L2Top.co",
			"L2Votes.com",
			"L2.Hopzone.net",
			"L2Network.eu",
			"L2TopServers.com",
			"top.l2jbrasil.com",
			"MMOTOP.eu",
			"L2Topzone.com",
			"L2Servers.com"
		};
		
		public static final String getResponse(String Url, int ordinal) 
		{
			
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
						if(ordinal == 3) {
							if(inputLine.contains("Votes:")) {
								response.append(inputLine);
								break;
							}
						}
						if(ordinal == 7){
							if(inputLine.contains("<b>Entradas ")) {
								response.append(inputLine);
							break;
							}
						}
					}
					in.close();
					return response.toString();
				} 
			
			  }
			       catch (Exception e)
			       {
			    	   LOGGER.error(VoteUtil.Sites[ordinal]+ " Say: An error ocurred ", e.getMessage());
			           return "";
			       }

			return "";
		}
	
		public static final String between(String p1, String str, String p2){
	        String returnValue = "";
	        int i1 = str.indexOf(p1);
	        int i2 = str.indexOf(p2);
	        if(i1 != -1 && i2 != -1){
	            i1 = i1+p1.length();
	            returnValue = str.substring(i1,i2);
	        }
	        return returnValue;
	    }
		
		public static void goToVoteSite(String urlSiiteVotation) {
			/*try
			{
				URL url = new URL(urlSiiteVotation);
				try
				{
					Desktop.getDesktop().browse(url.toURI());
				}
				catch (IOException | URISyntaxException e)
				{
					e.printStackTrace();
				}
			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
			}*/
		}
}