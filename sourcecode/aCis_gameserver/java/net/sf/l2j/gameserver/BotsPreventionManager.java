package net.sf.l2j.gameserver.instancemanager;

import java.io.File;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Future;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.model.actor.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.PledgeCrest;

public class BotsPreventionManager
{
	private class PlayerData
	{
		public PlayerData()
		{
			firstWindow = true;
		}
		
		public int mainpattern;
		public List<Integer> options = new ArrayList<>();
		public boolean firstWindow;
		public int patternid;
	}
	
	protected Random _randomize;
	protected static Map<Integer, Integer> _monsterscounter;
	protected static Map<Integer, Future<?>> _beginvalidation;
	protected static Map<Integer, PlayerData> _validation;
	protected static Map<Integer, byte[]> _images;
	protected int WINDOW_DELAY = 3; // delay used to generate new window if previous have been closed.
	protected int VALIDATION_TIME = Config.VALIDATION_TIME * 1000;
	
	public static final BotsPreventionManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	BotsPreventionManager()
	{
		_randomize = new Random();
		_monsterscounter = new HashMap<>();
		_beginvalidation = new HashMap<>();
		_validation = new HashMap<>();
		_images = new HashMap<>();
		_beginvalidation = new HashMap<>();
		
		getimages();
	}
	
	public void updatecounter(L2Character player, L2Character monster)
	{
		if ((player instanceof L2PcInstance) && (monster instanceof L2MonsterInstance))
		{
			L2PcInstance killer = (L2PcInstance) player;
			
			if (_validation.get(killer.getObjectId()) != null)
			{
				return;
			}
			
			int count = 1;
			if (_monsterscounter.get(killer.getObjectId()) != null)
			{
				count = _monsterscounter.get(killer.getObjectId()) + 1;
			}
			
			int next = _randomize.nextInt(Config.KILLS_COUNTER_RANDOMIZATION);
			if (Config.KILLS_COUNTER + next < count)
			{
				validationtasks(killer);
				_monsterscounter.remove(killer.getObjectId());
			}
			else
			{
				_monsterscounter.put(killer.getObjectId(), count);
			}
		}
	}
	
	private static void getimages()
	{
		String CRESTS_DIR = "data/html/mods/prevention";
		
		final File directory = new File(CRESTS_DIR);
		directory.mkdirs();
		
		int i = 0;
		for (File file : directory.listFiles())
		{
			if (!file.getName().endsWith(".dds"))
				continue;
			
			byte[] data;
			
			try (RandomAccessFile f = new RandomAccessFile(file, "r"))
			{
				data = new byte[(int) f.length()];
				f.readFully(data);
			}
			catch (Exception e)
			{
				continue;
			}
			_images.put(i, data);
			i++;
		}
	}
	
	public void prevalidationwindow(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		StringBuilder tb = new StringBuilder();
		StringUtil.append(tb, "<html>");
		StringUtil.append(tb, "<title>Antibot Prevention System</title>");
		StringUtil.append(tb, "<body><center><br><font color=\"01A9DB\">Antibot prevention system is enabled.</font><br1>Captcha verification is required!");
		StringUtil.append(tb, "<br><font color=\"00ffff\">You have 1 attempt to pass the captcha verification!</font>");
		StringUtil.append(tb, "<br>You have: <font color=\"FAFF00\">60 Seconds!</font>");
		StringUtil.append(tb, "<br1>You will be informed at: <font color=\"FAFF00\">30/10/5 Seconds!</font>");
		StringUtil.append(tb, "<br><font color=\"b09979\">If captcha is incorrect or no action is made<br1>Server is going to punish character instantly!</font>");
		StringUtil.append(tb, "<br><br><button value=\"Verify\" action=\"bypass report_continue\" width=\"75\" height=\"21\" back=\"L2UI_CH3.Btn1_normal\" fore=\"L2UI_CH3.Btn1_normal\">");
		StringUtil.append(tb, "</center></body>");
		StringUtil.append(tb, "</html>");
		html.setHtml(tb.toString());
		player.sendPacket(html);
	}
	
	private static void validationwindow(L2PcInstance player)
	{
		PlayerData container = _validation.get(player.getObjectId());
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		
		StringBuilder tb = new StringBuilder();
		StringUtil.append(tb, "<html>");
		StringUtil.append(tb, "<title>Antibot Prevention System</title>");
		StringUtil.append(tb, "<body><center><br>");
		StringUtil.append(tb, "<br><br><font color=\"a2a0a2\">In order to prove you are a human being<br1></font> <font color=\"b09979\">Match colors within generated pattern:</font>");
		
		// generated main pattern.
		StringUtil.append(tb, "<br><br><img src=\"Crest.crest_" + Config.SERVER_ID + "_" + (_validation.get(player.getObjectId()).patternid) + "\" width=\"32\" height=\"32\"></td></tr>");
		StringUtil.append(tb, "<br><br><font color=b09979>click-on pattern of your choice beneath:</font>");
		
		// generate random colors.
		StringUtil.append(tb, "<table><tr>");
		for (int i = 0; i < container.options.size(); i++)
		{
			StringUtil.append(tb, "<td><button action=\"bypass -h report_" + i + "\" width=32 height=32 back=\"Crest.crest_" + Config.SERVER_ID + "_" + (container.options.get(i) + 1500) + "\" fore=\"Crest.crest_" + Config.SERVER_ID + "_" + (container.options.get(i) + 1500) + "\"></td>");
		}
		StringUtil.append(tb, "</tr></table>");
		StringUtil.append(tb, "</center></body>");
		StringUtil.append(tb, "</html>");
		
		html.setHtml(tb.toString());
		player.sendPacket(html);
	}
	
	public void punishmentnwindow(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		StringBuilder tb = new StringBuilder();
		StringUtil.append(tb, "<html>");
		StringUtil.append(tb, "<title>Antibot Prevention System</title>");
		StringUtil.append(tb, "<body><center><br>");
		StringUtil.append(tb, "<font color=\"a2a0a2\">You failed passing through Antibot Prevention System!");
		StringUtil.append(tb, "<br><font color=\"b09979\">You have been teleported to the nearest town.</font>");
		StringUtil.append(tb, "</center></body>");
		StringUtil.append(tb, "</html>");
		html.setHtml(tb.toString());
		player.sendPacket(html);
	}
	
	public void validationtasks(L2PcInstance player)
	{
		PlayerData container = new PlayerData();
		randomizeimages(container, player);
		
		for (int i = 0; i < container.options.size(); i++)
		{
			PledgeCrest packet = new PledgeCrest((container.options.get(i) + 1500), _images.get(container.options.get(i)));
			player.sendPacket(packet);
			
		}
		
		PledgeCrest packet = new PledgeCrest(container.patternid, _images.get(container.options.get(container.mainpattern)));
		player.sendPacket(packet);
		
		_validation.put(player.getObjectId(), container);
		
		Future<?> newTask = ThreadPoolManager.getInstance().scheduleGeneral(new ReportCheckTask(player), VALIDATION_TIME);
		ThreadPoolManager.getInstance().scheduleGeneral(new countdown(player, VALIDATION_TIME / 1000), 0);
		_beginvalidation.put(player.getObjectId(), newTask);
	}
	
	protected void randomizeimages(PlayerData container, L2PcInstance player)
	{
		int buttonscount = 4;
		int imagescount = _images.size();
		
		for (int i = 0; i < buttonscount; i++)
		{
			int next = _randomize.nextInt(imagescount);
			while (container.options.indexOf(next) > -1)
			{
				next = _randomize.nextInt(imagescount);
			}
			container.options.add(next);
		}
		
		int mainIndex = _randomize.nextInt(buttonscount);
		container.mainpattern = mainIndex;
		
		Calendar token = Calendar.getInstance();
		String uniquetoken = Integer.toString(token.get(Calendar.DAY_OF_MONTH)) + Integer.toString(token.get(Calendar.HOUR_OF_DAY)) + Integer.toString(token.get(Calendar.MINUTE)) + Integer.toString(token.get(Calendar.SECOND)) + Integer.toString(token.get(Calendar.MILLISECOND) / 100);
		container.patternid = Integer.parseInt(uniquetoken);
	}
	
	protected void banpunishment(L2PcInstance player)
	{
		_validation.remove(player.getObjectId());
		_beginvalidation.get(player.getObjectId()).cancel(true);
		_beginvalidation.remove(player.getObjectId());
		
		switch (Config.PUNISHMENT)
		{
			// 0 = move character to the closest village.
			// 1 = kick characters from the server.
			// 2 = put character to jail.
			// 3 = ban character from the server.
			case 0:
				player.stopMove(null);
				player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
				punishmentnwindow(player);
				break;
			case 1:
				if (player.isOnline())
				{
					player.logout(true);
				}
				break;
			case 2:
				jailpunishment(player, Config.PUNISHMENT_TIME * 60);
				break;
			case 3:
				// player.setAccessLevel(-100);
				changeaccesslevel(player, -100);
				break;
		}
		
		player.sendMessage("Unfortunately, colours doesn't match.");
	}
	
	private static void changeaccesslevel(L2PcInstance targetPlayer, int lvl)
	{
		if (targetPlayer.isOnline())
		{
			targetPlayer.setAccessLevel(lvl);
			targetPlayer.logout();
		}
		else
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("UPDATE characters SET accesslevel=? WHERE obj_id=?");
				statement.setInt(1, lvl);
				statement.setInt(2, targetPlayer.getObjectId());
				statement.execute();
				statement.close();
			}
			catch (SQLException se)
			{
				if (Config.DEBUG)
					se.printStackTrace();
			}
		}
	}
	
	private static void jailpunishment(L2PcInstance activeChar, int delay)
	{
		if (activeChar.isOnline())
		{
			activeChar.setPunishLevel(L2PcInstance.PunishLevel.JAIL, Config.PUNISHMENT_TIME);
		}
		else
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, punish_level=?, punish_timer=? WHERE obj_id=?");
				statement.setInt(1, -114356);
				statement.setInt(2, -249645);
				statement.setInt(3, -2984);
				statement.setInt(4, L2PcInstance.PunishLevel.JAIL.value());
				statement.setLong(5, (delay > 0 ? delay * Config.PUNISHMENT_TIME * 100 : 0));
				statement.setInt(6, activeChar.getObjectId());
				
				statement.execute();
				statement.close();
			}
			catch (SQLException se)
			{
				activeChar.sendMessage("SQLException while jailing player");
				if (Config.DEBUG)
					se.printStackTrace();
			}
		}
	}
	
	public void AnalyseBypass(String command, L2PcInstance player)
	{
		if (!_validation.containsKey(player.getObjectId()))
			return;
		
		String params = command.substring(command.indexOf("_") + 1);
		
		if (params.startsWith("continue"))
		{
			validationwindow(player);
			_validation.get(player.getObjectId()).firstWindow = false;
			return;
		}
		
		int choosenoption = -1;
		if (tryParseInt(params))
		{
			choosenoption = Integer.parseInt(params);
		}
		
		if (choosenoption > -1)
		{
			PlayerData playerData = _validation.get(player.getObjectId());
			if (choosenoption != playerData.mainpattern)
			{
				banpunishment(player);
			}
			else
			{
				player.sendMessage("Congratulations, colors match!");
				_validation.remove(player.getObjectId());
				_beginvalidation.get(player.getObjectId()).cancel(true);
				_beginvalidation.remove(player.getObjectId());
			}
		}
	}
	
	protected class countdown implements Runnable
	{
		private final L2PcInstance _player;
		private int _time;
		
		public countdown(L2PcInstance player, int time)
		{
			_time = time;
			_player = player;
		}
		
		@Override
		public void run()
		{
			if (_player.isOnline())
			{
				if (_validation.containsKey(_player.getObjectId()) && _validation.get(_player.getObjectId()).firstWindow)
				{
					if (_time % WINDOW_DELAY == 0)
					{
						prevalidationwindow(_player);
					}
				}
				
				switch (_time)
				{
					case 300:
					case 240:
					case 180:
					case 120:
					case 60:
						_player.sendMessage(_time / 60 + " minute(s) to match colors.");
						break;
					case 30:
					case 10:
					case 5:
					case 4:
					case 3:
					case 2:
					case 1:
						_player.sendMessage(_time + " second(s) to match colors!");
						break;
				}
				if (_time > 1 && _validation.containsKey(_player.getObjectId()))
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new countdown(_player, _time - 1), 1000);
				}
			}
		}
	}
	
	protected boolean tryParseInt(String value)
	{
		try
		{
			Integer.parseInt(value);
			return true;
		}
		
		catch (NumberFormatException e)
		{
			return false;
		}
	}
	
	public void CaptchaSuccessfull(L2PcInstance player)
	{
		if (_validation.get(player.getObjectId()) != null)
		{
			_validation.remove(player.getObjectId());
		}
	}
	
	public Boolean IsAlredyInReportMode(L2PcInstance player)
	{
		if (_validation.get(player.getObjectId()) != null)
		{
			return true;
		}
		return false;
	}
	
	private class ReportCheckTask implements Runnable
	{
		private final L2PcInstance _player;
		
		public ReportCheckTask(L2PcInstance player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			if (_validation.get(_player.getObjectId()) != null)
			{
				banpunishment(_player);
			}
		}
	}
	
	private static class SingletonHolder
	{
		protected static final BotsPreventionManager _instance = new BotsPreventionManager();
	}
}