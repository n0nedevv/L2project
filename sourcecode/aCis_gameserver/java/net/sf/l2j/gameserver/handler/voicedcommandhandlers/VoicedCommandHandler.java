/**
 * @author n0nedevv
 * 
 */
package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.commons.logging.CLogger;

public class VoicedCommandHandler
{
    CLogger LOGGER = new CLogger(VoicedCommandHandler.class.getName());
    private final Map<Integer, IVoicedCommandHandler> VOICED_COMMANDS;
    
    public static VoicedCommandHandler getInstance()
    {
        return SingletonHolder._instance;
    }
    
    protected VoicedCommandHandler()
    {
        VOICED_COMMANDS = new HashMap<>();
        // example how to register you handler:
        // registerVoicedCommandHandler(new YourHandler());
              
        registerVoicedCommandHandler(new Raid());
        registerVoicedCommandHandler(new CastleManagersVCMD());
        registerVoicedCommandHandler(new Menu());
        
        LOGGER.info("Loaded {} voiced command handlers.", size());
    }
    
    public void registerVoicedCommandHandler(IVoicedCommandHandler handler)
    {
        for (String id : handler.getVoicedCommandList())
            VOICED_COMMANDS.put(id.hashCode(), handler);
    }
    
    public IVoicedCommandHandler getVoicedCommandHandler(String voicedCommand)
    {
        String command = voicedCommand;
        
        if (voicedCommand.indexOf(" ") != -1)
            command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
        
        return VOICED_COMMANDS.get(command.hashCode());
    }
    
    public int size()
    {
        return VOICED_COMMANDS.size();
    }
    
    private static class SingletonHolder
    {
        protected static final VoicedCommandHandler _instance = new VoicedCommandHandler();
    }
}