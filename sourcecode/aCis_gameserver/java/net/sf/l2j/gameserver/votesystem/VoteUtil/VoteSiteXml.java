package net.sf.l2j.gameserver.votesystem.VoteUtil;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.gameserver.votesystem.Model.Reward;
import net.sf.l2j.gameserver.votesystem.Model.VoteSite;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

/**
 * @author l2.topgameserver.net
 *
 */
public class VoteSiteXml implements IXmlReader
{

	private final Map<Integer,VoteSite> _voteSites = new HashMap<>();
	
	protected VoteSiteXml() {
		load();
	}
	
	@Override
	public void load()
	{
		parseFile("./data/xml/votesystem.xml");
		LOGGER.info("Loaded {} reward sites", _voteSites.size());
	}


	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "list", listNode -> {
			forEach(listNode, "votesite", votesiteNode -> {
				final VoteSite votesite = new VoteSite();
				final NamedNodeMap attrs = votesiteNode.getAttributes();
				votesite.setSiteOrdinal(parseInteger(attrs,"ordinal"));
				votesite.setSiteName(parseString(attrs,"name"));
				forEach(votesiteNode,"items", itemsNode -> forEach(itemsNode,"item",itemNode -> votesite.getRewardList().add(new Reward(parseAttributes(itemNode)))));
				_voteSites.put(votesite.getSiteOrdinal(),votesite);
			});
		});
	}
	
	public String getSiteName(int ordinal) {
		return _voteSites.get(ordinal).getSiteName();
	}
	
	public Collection<Reward> getRewards(int ordinal){
		return _voteSites.get(ordinal).getRewardList();
	}
	
	public static final VoteSiteXml getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		protected static final VoteSiteXml INSTANCE = new VoteSiteXml();
	}
	
}