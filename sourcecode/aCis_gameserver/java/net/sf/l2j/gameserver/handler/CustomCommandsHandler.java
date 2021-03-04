package net.sf.l2j.gameserver.handler;

import java.util.Map;

import net.sf.l2j.gameserver.handler.customcommandHandler.VoteCommandHandler;

import java.util.HashMap;

/**
 * @author l2.topgameserver.net
 *
 */
public class CustomCommandsHandler {
	private final Map<Integer, ICustomCommandsHandler> _datatable = new HashMap<>();



	protected CustomCommandsHandler() {
		registerCustomCommandHandler(new VoteCommandHandler());
	}

	public void registerCustomCommandHandler(ICustomCommandsHandler handler) {
		for (String id : handler.getCustomCommandList())
			_datatable.put(id.hashCode(), handler);
	}

	public ICustomCommandsHandler getCustomCommandHandler(String customCommand) {
		return _datatable.get(customCommand.hashCode());
	}

	public int size() {
		return _datatable.size();
	}

	public static CustomCommandsHandler getInstance() {
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder {
		protected static final CustomCommandsHandler _instance = new CustomCommandsHandler();
	}
}