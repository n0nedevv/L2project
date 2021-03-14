package net.sf.l2j.gameserver.data.xml;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.commons.data.StatSet;
import net.sf.l2j.commons.data.xml.IXmlReader;
 
import org.w3c.dom.Document;
 
public final class IconData implements IXmlReader
{
    private final Map<Integer, String> _icons = new HashMap<>();
    
    public IconData()
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
            _icons.put(set.getInteger("id"), set.getString("value", "icon.noimage"));
        }));
    }
    
    public final String getIcon(int itemId)
    {
        return _icons.get(itemId);
    }
    
    public static IconData getInstance()
    {
        return SingletonHolder.INSTANCE;
    }
    
    private static class SingletonHolder
    {
        protected static final IconData INSTANCE = new IconData();
    }
}