package net.sf.l2j.gameserver.data.sql;

import java.util.HashMap;
import java.util.Map;
import java.nio.file.Path;


import net.sf.l2j.commons.data.StatSet;
import net.sf.l2j.commons.data.xml.IXmlReader;
 
import org.w3c.dom.Document;


public class IconsTable implements IXmlReader
{
    public static final Map<Integer, String> _icons = new HashMap<>();


    public IconsTable()
    {
        load();
    }
    
    
    
    @Override
	public void load()
    {
        parseFile("./data/xml/icons.xml");
        LOGGER.info("Loaded {} icon data.", _icons.size());
    }

    @Override
    public void parseDocument(Document doc, Path path)
    {
        forEach(doc, "list", listNode -> forEach(listNode, "icon", iconNode ->
        {
            final StatSet set = parseAttributes(iconNode);
            _icons.put(set.getInteger("Id"), set.getString("value", "icon.noimage"));
        }));
    }
    
    @SuppressWarnings("static-method")
	public final String getIcon(int itemId)
    {
        return _icons.get(itemId);
    }

    public static final IconsTable getInstance()
    {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder
    {
        protected static final IconsTable _instance = new IconsTable();
    }
}