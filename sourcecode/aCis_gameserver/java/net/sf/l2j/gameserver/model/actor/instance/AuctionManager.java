package net.sf.l2j.gameserver.model.actor.instance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.pool.ConnectionPool;

import net.sf.l2j.gameserver.data.sql.AuctionTable;
import net.sf.l2j.gameserver.data.sql.IconsTable;
import net.sf.l2j.gameserver.data.xml.ItemData;
import net.sf.l2j.gameserver.enums.actors.OperateType;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.auction.AuctionItem;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.trade.TradeItem;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.TeleportToLocation;

/**
 * @author n0nedevv
 *
 */
public class AuctionManager extends Folk
{
	public AuctionManager(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("auction"))
		{
			try
			{
				String[] data = command.substring(8).split(" - ");
				int page = Integer.parseInt(data[0]);
				String search = data[1];
				showAuction(player, page, search);
			}
			catch (Exception e)
			{
				showChatWindow(player);
				player.sendMessage("Invalid input. Please try again.");
				return;
			}
		}
		else if (command.startsWith("buy"))
		{
			int auctionId = Integer.parseInt(command.substring(4));
			AuctionItem item = AuctionTable.getInstance().getItem(auctionId);
			
			if (item == null)
			{
				showChatWindow(player);
				player.sendMessage("Invalid choice. Please try again.");
				return;
			}
			
			if (player.getInventory().getItemByItemId(item.getCostId()) == null || player.getInventory().getItemByItemId(item.getCostId()).getCount() < item.getCostCount())
			{
				showChatWindow(player);
				player.sendMessage("Incorrect item count.");
				return;
			}
			
			player.destroyItemByItemId("auction", item.getCostId(), item.getCostCount(), this, true);
			
			final Player owner = World.getInstance().getPlayer(item.getOwnerId());
			if (owner != null && owner.isOnline())
			{
				owner.addItem("auction", item.getCostId(), item.getCostCount(), null, true);
				owner.sendMessage("You have sold an item in the Auction Shop.");
			}
			else
			{
				addItemToOffline(item.getOwnerId(), item.getCostId(), item.getCostCount());
			}
			
			ItemInstance i = player.addItem("auction", item.getItemId(), item.getCount(), this, true);
			i.setEnchantLevel(item.getEnchant());
			player.sendPacket(new InventoryUpdate());
			player.sendMessage("You have purchased an item from the Auction Shop.");
			
			AuctionTable.getInstance().deleteItem(item);
			
			showChatWindow(player);
		}
		else if (command.startsWith("addpanel"))
		{
			int page = Integer.parseInt(command.substring(9));
			
			showAddPanel(player, page);
		}
		else if (command.startsWith("additem"))
		{
			int itemId = Integer.parseInt(command.substring(8));
			
			if (player.getInventory().getItemByObjectId(itemId) == null)
			{
				showChatWindow(player);
				player.sendMessage("Invalid item. Please try again.");
				return;
			}
			
			showAddPanel2(player, itemId);
		}
		else if (command.startsWith("addit2"))
		{
			try
			{
				String[] data = command.substring(7).split(" ");
				int itemId = Integer.parseInt(data[0]);
				String costitemtype = data[1];
				int costCount = Integer.parseInt(data[2]);
				int itemAmount = Integer.parseInt(data[3]);
				
				if (player.getInventory().getItemByObjectId(itemId) == null)
				{
					showChatWindow(player);
					player.sendMessage("Invalid item. Please try again.");
					return;
				}
				if (player.getInventory().getItemByObjectId(itemId).getCount() < itemAmount)
				{
					showChatWindow(player);
					player.sendMessage("Invalid item. Please try again.");
					return;
				}
				if (!player.getInventory().getItemByObjectId(itemId).isTradable())
				{
					showChatWindow(player);
					player.sendMessage("Invalid item. Please try again.");
					return;
				}
				
				int costId = 0;
				if (costitemtype.equals("Adena"))
				{
					costId = 57;
				}
				
				AuctionTable.getInstance().addItem(new AuctionItem(AuctionTable.getInstance().getNextAuctionId(), player.getObjectId(), player.getInventory().getItemByObjectId(itemId).getItemId(), itemAmount, player.getInventory().getItemByObjectId(itemId).getEnchantLevel(), costId, costCount));
				
				player.destroyItem("auction", itemId, itemAmount, this, true);
				player.sendPacket(new InventoryUpdate());
				player.sendMessage("You have added an item for sale in the Auction Shop.");
				showChatWindow(player);
			}
			catch (Exception e)
			{
				showChatWindow(player);
				player.sendMessage("Invalid input. Please try again.");
				return;
			}
		}
		else if (command.startsWith("myitems"))
		{
			int page = Integer.parseInt(command.substring(8));
			showMyItems(player, page);
		}
		else if (command.startsWith("remove"))
		{
			int auctionId = Integer.parseInt(command.substring(7));
			AuctionItem item = AuctionTable.getInstance().getItem(auctionId);
			
			if (item == null)
			{
				showChatWindow(player);
				player.sendMessage("Invalid choice. Please try again.");
				return;
			}
			
			AuctionTable.getInstance().deleteItem(item);
			
			ItemInstance i = player.addItem("auction", item.getItemId(), item.getCount(), this, true);
			i.setEnchantLevel(item.getEnchant());
			player.sendPacket(new InventoryUpdate());
			player.sendMessage("You have removed an item from the Auction Shop.");
			showChatWindow(player);
		}
		else if (command.startsWith("offlinebuy"))
		{
			try
			{
				//String[] data = command.substring(8).split(" - ");
				int page = 1;
				String search = "";
				showOfflineBuy(player, page, search);
			}
			catch (Exception e)
			{
				showChatWindow(player);
				player.sendMessage("Invalid input. Please try again.");
				return;
			}
		}
		else if (command.startsWith("offlinesell"))
		{
			try
			{
				//String[] data = command.substring(8).split(" - ");
				int page = 1;
				String search = "";
				showOfflineSell(player, page, search);
			}
			catch (Exception e)
			{
				showChatWindow(player);
				player.sendMessage("Invalid input. Please try again.");
				return;
			}
		}
		else if (command.startsWith("teleport"))
		{
			Connection con = null;
			try {
				con = ConnectionPool.getConnection();
				PreparedStatement select = con.prepareStatement("SELECT owner_id FROM items WHERE object_id=?");
				
				select.setInt(1, World.getInstance().getObject(Integer.parseInt(command.substring(9))).getObjectId());
				try (ResultSet rs = select.executeQuery())
			    {
					if(rs.next()) {
						Player owner = World.getInstance().getPlayer(Integer.parseInt(rs.getString(1)));                 
						player.teleportTo(owner.getX(), owner.getY(), owner.getZ(), 0); 
					}
			    }
				catch (Exception e) {
					e.printStackTrace();
				}
				
				
			}
			catch (Exception e) {
				try
				{
					con.close();
				}
				catch (SQLException e1)
				{
					e1.printStackTrace();
				}
			} finally {
				try {
					con.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	private void showMyItems(Player player, int page)
	{
		HashMap<Integer, ArrayList<AuctionItem>> items = new HashMap<>();
		int curr = 1;
		int counter = 0;
		
		ArrayList<AuctionItem> temp = new ArrayList<>();
		for (Map.Entry<Integer, AuctionItem> entry : AuctionTable.getInstance().getItems().entrySet())
		{
			if (entry.getValue().getOwnerId() == player.getObjectId())
			{
				temp.add(entry.getValue());
 
				counter++;
 
				if (counter == 10)
				{
					items.put(curr, temp);
					temp = new ArrayList<>();
					curr++;
					counter = 0;
				}
			}
		}
		items.put(curr, temp);
		
		if (!items.containsKey(page))
		{
			showChatWindow(player);
			player.sendMessage("Invalid page. Please try again.");
			return;
		}
		
		String html = "";
		html += "<html><title>Auction Shop</title><body><center><br1>";
		html += "<table width=310 bgcolor=000000 border=1>";
		html += "<tr><td>Item</td><td>Cost</td><td></td></tr>";
		for (AuctionItem item : items.get(page))
		{
			html += "<tr>";
			html += "<td><img src=\""+IconsTable.getInstance().getIcon(item.getItemId())+"\" width=32 height=32 align=center></td>";
		html += "<td>Item: "+(item.getEnchant() > 0 ? "+"+item.getEnchant()+" "+ItemData.getInstance().getTemplate(item.getItemId()).getName()+" - "+item.getCount() : ItemData.getInstance().getTemplate(item.getItemId()).getName()+" - "+item.getCount());
			html += "<br1>Cost: "+item.getCostCount()+" "+ItemData.getInstance().getTemplate(item.getCostId()).getName();
			html += "</td>";
			html += "<td fixwidth=71><button value=\"Remove\" action=\"bypass -h npc_"+getObjectId()+"_remove "+item.getAuctionId()+"\" width=70 height=21 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\">";
			html += "</td></tr>";
		}
		html += "</table><br><br>";
		
		html += "Page: "+page;
		html += "<br1>";
		
		if (items.keySet().size() > 1)
		{
			if (page > 1)
				html += "<a action=\"bypass -h npc_"+getObjectId()+"_myitems "+(page-1)+"\"><- Prev</a>";
			
			if (items.keySet().size() > page)
				html += "<a action=\"bypass -h npc_"+getObjectId()+"_myitems "+(page+1)+"\">Next -></a>";
		}
		
		html += "</center></body></html>";
		
		NpcHtmlMessage htm = new NpcHtmlMessage(getObjectId());
		htm.setHtml(html);
		player.sendPacket(htm);
	}
	
	private void showAddPanel2(Player player, int itemId)
	{
		ItemInstance item = player.getInventory().getItemByObjectId(itemId);
		
		String html = "";
		html += "<html><title>Auction Shop</title><body><center><br1>";
		html += "<img src=\""+IconsTable.getInstance().getIcon(item.getItemId())+"\" width=32 height=32 align=center>";
		html += "Item: "+(item.getEnchantLevel() > 0 ? "+"+item.getEnchantLevel()+" "+item.getName() : item.getName());
		
		if (item.isStackable())
		{
			html += "<br>Set amount of items to sell:";
			html += "<edit var=amm type=number width=120 height=17>";
		}
		
		html += "<br>Select price:";
		html += "<br><combobox width=120 height=17 var=ebox list=Adena;>";
		html += "<br><edit var=count type=number width=120 height=17>";
		html += "<br><button value=\"Add item\" action=\"bypass -h npc_"+getObjectId()+"_addit2 "+itemId+" $ebox $count "+(item.isStackable() ? "$amm" : "1")+"\" width=70 height=21 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\">";
		html += "</center></body></html>";
		
		NpcHtmlMessage htm = new NpcHtmlMessage(getObjectId());
		htm.setHtml(html);
		player.sendPacket(htm);
	}
	
	private void showAddPanel(Player player, int page)
	{
		HashMap<Integer, ArrayList<ItemInstance>> items = new HashMap<>();
		int curr = 1;
		int counter = 0;
		
		ArrayList<ItemInstance> temp = new ArrayList<>();
		for (ItemInstance item : player.getInventory().getItems())
		{
			if (item.getItemId() != 57 && item.isTradable() && !item.isEquipped())
			{
				temp.add(item);
				
				counter++;
				
				if (counter == 10)
				{
					items.put(curr, temp);
					temp = new ArrayList<>();
					curr++;
					counter = 0;
				}
			}
		}
		items.put(curr, temp);
		
		if (!items.containsKey(page))
		{
			showChatWindow(player);
			player.sendMessage("Invalid page. Please try again.");
			return;
		}
		
		String html = "";
		html += "<html><title>Auction Shop</title><body><center><br1>";
		html += "Select item:";
		html += "<br><table width=310 bgcolor=000000 border=1>";
		
		for (ItemInstance item : items.get(page))
		{
			html += "<tr>";
			html += "<td>";
			html += "<img src=\""+IconsTable.getInstance().getIcon(item.getItemId())+"\" width=32 height=32 align=center></td>";
			html += "<td>"+(item.getEnchantLevel() > 0 ? "+"+item.getEnchantLevel()+" "+item.getName() : item.getName());
			html += "</td>";
			html += "<td><button value=\"Select\" action=\"bypass -h npc_"+getObjectId()+"_additem "+item.getObjectId()+"\" width=70 height=21 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\">";
			html += "</td>";
			html += "</tr>";
		}
		html += "</table><br><br>";
		
		html += "Page: "+page;
		html += "<br1>";
		
		if (items.keySet().size() > 1)
		{
			if (page > 1)
				html += "<a action=\"bypass -h npc_"+getObjectId()+"_addpanel "+(page-1)+"\"><- Prev</a>";
			
			if (items.keySet().size() > page)
				html += "<a action=\"bypass -h npc_"+getObjectId()+"_addpanel "+(page+1)+"\">Next -></a>";
		}
		
		html += "</center></body></html>";
		
		NpcHtmlMessage htm = new NpcHtmlMessage(getObjectId());
		htm.setHtml(html);
		player.sendPacket(htm);
	}
	
	private static void addItemToOffline(int playerId, int itemId, int count)
	{
		try (Connection con = ConnectionPool.getConnection();
		    PreparedStatement select = con.prepareStatement("SELECT count FROM items WHERE owner_id=? AND item_id=?"))
		{
		    select.setInt(1, playerId);
		    select.setInt(2, itemId);
		    try (ResultSet rs = select.executeQuery())
		    {
		        if (rs.next())
		        {
		            try (PreparedStatement update = con.prepareStatement("UPDATE items SET count=? WHERE owner_id=? AND item_id=?"))
		            {
		                update.setInt(1, rs.getInt("count") + count);
		                update.setInt(2, playerId);
		                update.setInt(3, itemId);
		                update.execute();
		            }
		        }
		        else
		        {
		            try (PreparedStatement insert = con.prepareStatement("INSERT INTO items VALUES (?,?,?,?,?,?,?,?,?,?,?)"))
		            {
		                insert.setInt(1, playerId);
		                insert.setInt(2, IdFactory.getInstance().getNextId());
		                insert.setInt(3, itemId);
		                insert.setInt(4, count);
		                insert.setInt(5, 0);
		                insert.setString(6, "INVENTORY");
		                insert.setInt(7, 0);
		                insert.setInt(8, 0);
		                insert.setInt(9, 0);
		                insert.setInt(10, -60);
		                insert.setLong(11, System.currentTimeMillis());
		                insert.execute();
		            }
		        }
		    }
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private void showAuction(Player player, int page, String search)
	{
		boolean src = !search.equals("*null*");
 
		HashMap<Integer, ArrayList<AuctionItem>> items = new HashMap<>();
		int curr = 1;
		int counter = 0;
 
		ArrayList<AuctionItem> temp = new ArrayList<>();
		for (Map.Entry<Integer, AuctionItem> entry : AuctionTable.getInstance().getItems().entrySet())
		{
			if (entry.getValue().getOwnerId() != player.getObjectId() && (!src || (src && ItemData.getInstance().getTemplate(entry.getValue().getItemId()).getName().contains(search))))
			{
				temp.add(entry.getValue());
 
				counter++;
 
				if (counter == 10)
				{
					items.put(curr, temp);
					temp = new ArrayList<>();
					curr++;
					counter = 0;
				}
			}
		}
		items.put(curr, temp);
 
		if (!items.containsKey(page))
		{
			showChatWindow(player);
			player.sendMessage("Invalid page. Please try again.");
			return;
		}
 
		String html = "<html><title>Auction Shop</title><body><center><br1>";
		html += "<multiedit var=srch width=150 height=20><br1>";
		html += "<button value=\"Search\" action=\"bypass -h npc_"+getObjectId()+"_auction 1 - $srch\" width=70 height=21 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\">";
		html += "<br><table width=310 bgcolor=000000 border=1>";
		html += "<tr><td>Item</td><td>Cost</td><td></td></tr>";
		for (AuctionItem item : items.get(page))
		{
			html += "<tr>";
			html += "<td><img src=\""+IconsTable.getInstance().getIcon(item.getItemId())+"\" width=32 height=32 align=center></td>";
			html += "<td>Item: "+(item.getEnchant() > 0 ? "+"+item.getEnchant()+" "+ItemData.getInstance().getTemplate(item.getItemId()).getName()+" - "+item.getCount() : ItemData.getInstance().getTemplate(item.getItemId()).getName()+" - "+item.getCount());
			html += "<br1>Cost: "+StringUtil.formatNumber(item.getCostCount())+" "+ItemData.getInstance().getTemplate(item.getCostId()).getName();
			html += "</td>";
			html += "<td fixwidth=71><button value=\"Buy\" action=\"bypass -h npc_"+getObjectId()+"_buy "+item.getAuctionId()+"\" width=70 height=21 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\">";
			html += "</td></tr>";
		}
		html += "</table><br><br>";
 
		html += "Page: "+page;
		html += "<br1>";
 
		if (items.keySet().size() > 1)
		{
			if (page > 1)
				html += "<a action=\"bypass -h npc_"+getObjectId()+"_auction "+(page-1)+" - "+search+"\"><- Prev</a>";
 
			if (items.keySet().size() > page)
				html += "<a action=\"bypass -h npc_"+getObjectId()+"_auction "+(page+1)+" - "+search+"\">Next -></a>";
		}
 
		html += "</center></body></html>";
 
		NpcHtmlMessage htm = new NpcHtmlMessage(getObjectId());
		htm.setHtml(html);
		player.sendPacket(htm);
	}
	
	private void showOfflineBuy(Player player, int page, String search)
	{
		boolean src = !search.equals("*null*");
		 
		HashMap<Integer, List<TradeItem>> items = new HashMap<>();
		int curr = 1; //curr pagina (?)
		int counter = 0;
		
		ArrayList<TradeItem> temp = new ArrayList<>();
		for (Map.Entry<Player, List<TradeItem>> entry : getItemsBuyShops().entrySet())
		{
			for(TradeItem item : entry.getValue()) {
				if(entry.getKey().getObjectId() != player.getObjectId() && (!src || (src && item.getItem().getName().contains(search)))) 
				{
					temp.add(item);
					counter ++;
					
					if (counter == 10)
					{
						items.put(curr, temp);
						temp = new ArrayList<>();
						curr++;
						counter = 0;
					}
				}
			}
			
		}
		items.put(curr, temp);
 
		if (!items.containsKey(page))
		{
			showChatWindow(player);
			player.sendMessage("Invalid page. Please try again.");
			return;
		}
 
		String html = "<html><title>Auction Shop</title><body><center><br1>";
		html += "<multiedit var=srch width=150 height=20><br1>";
		html += "<button value=\"Search\" action=\"bypass -h npc_"+getObjectId()+"_auction 1 - $srch\" width=70 height=21 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\">";
		html += "<br><table width=310 bgcolor=000000 border=1>";
		html += "<tr><td>Item</td><td>Cost</td><td></td></tr>";
		for (TradeItem item : items.get(page))
		{	
			html += "<tr>";
			html += "<td><img src=\""+IconsTable.getInstance().getIcon(item.getItem().getItemId())+"\" width=32 height=32 align=center></td>";
			html += "<td>Item: "+(item.getEnchant() > 0 ? "+"+item.getEnchant()+" "+ItemData.getInstance().getTemplate(item.getItem().getItemId()).getName()+" - "+item.getCount() : ItemData.getInstance().getTemplate(item.getItem().getItemId()).getName()+" - "+item.getCount());
			html += "<br1>Cost: "+StringUtil.formatNumber(item.getPrice())+" "+ItemData.getInstance().getTemplate(item.getItem().getItemId()).getName();
			html += "</td>";
			html += "<td fixwidth=71><button value=\"Go to\" action=\"bypass -h npc_"+getObjectId()+"_teleport " +item.getObjectId() + "\" width=70 height=21 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\">";
			html += "</td></tr>";
		}
		html += "</table><br><br>";
 
		html += "Page: "+page;
		html += "<br1>";
 
		if (items.keySet().size() > 1)
		{
			if (page > 1)
				html += "<a action=\"bypass -h npc_"+getObjectId()+"_auction "+(page-1)+" - "+search+"\"><- Prev</a>";
 
			if (items.keySet().size() > page)
				html += "<a action=\"bypass -h npc_"+getObjectId()+"_auction "+(page+1)+" - "+search+"\">Next -></a>";
		}
 
		html += "</center></body></html>";
 
		NpcHtmlMessage htm = new NpcHtmlMessage(getObjectId());
		htm.setHtml(html);
		player.sendPacket(htm);
	}
	
	
	private void showOfflineSell(Player player, int page, String search)
	{
		boolean src = !search.equals("*null*");
		 
		HashMap<Integer, List<TradeItem>> items = new HashMap<>();
		int curr = 1; //curr pagina (?)
		int counter = 0;
		
		ArrayList<TradeItem> temp = new ArrayList<>();
		for (Map.Entry<Player, List<TradeItem>> entry : getItemsSellShops().entrySet())
		{
			for(TradeItem item : entry.getValue()) {
				if(entry.getKey().getObjectId() != player.getObjectId() && (!src || (src && item.getItem().getName().contains(search)))) 
				{
					temp.add(item);
					counter ++;
					
					if (counter == 10)
					{
						items.put(curr, temp);
						temp = new ArrayList<>();
						curr++;
						counter = 0;
					}
				}
			}
			
		}
		items.put(curr, temp);
 
		if (!items.containsKey(page))
		{
			showChatWindow(player);
			player.sendMessage("Invalid page. Please try again.");
			return;
		}
 
		String html = "<html><title>Auction Shop</title><body><center><br1>";
		html += "<multiedit var=srch width=150 height=20><br1>";
		html += "<button value=\"Search\" action=\"bypass -h npc_"+getObjectId()+"_auction 1 - $srch\" width=70 height=21 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\">";
		html += "<br><table width=310 bgcolor=000000 border=1>";
		html += "<tr><td>Item</td><td>Cost</td><td></td></tr>";
		for (TradeItem item : items.get(page))
		{	
			html += "<tr>";
			html += "<td><img src=\""+IconsTable.getInstance().getIcon(item.getItem().getItemId())+"\" width=32 height=32 align=center></td>";
			html += "<td>Item: "+(item.getEnchant() > 0 ? "+"+item.getEnchant()+" "+ItemData.getInstance().getTemplate(item.getItem().getItemId()).getName()+" - "+item.getCount() : ItemData.getInstance().getTemplate(item.getItem().getItemId()).getName()+" - "+item.getCount());
			html += "<br1>Cost: "+StringUtil.formatNumber(item.getPrice())+" "+ItemData.getInstance().getTemplate(item.getItem().getItemId()).getName();
			html += "</td>";
			html += "<td fixwidth=71><button value=\"Go to\" action=\"bypass -h npc_"+getObjectId()+"_teleport " +item.getObjectId() + "\" width=70 height=21 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\">";
			html += "</td></tr>";
		}
		html += "</table><br><br>";
 
		html += "Page: "+page;
		html += "<br1>";
 
		if (items.keySet().size() > 1)
		{
			if (page > 1)
				html += "<a action=\"bypass -h npc_"+getObjectId()+"_auction "+(page-1)+" - "+search+"\"><- Prev</a>";
 
			if (items.keySet().size() > page)
				html += "<a action=\"bypass -h npc_"+getObjectId()+"_auction "+(page+1)+" - "+search+"\">Next -></a>";
		}
 
		html += "</center></body></html>";
 
		NpcHtmlMessage htm = new NpcHtmlMessage(getObjectId());
		htm.setHtml(html);
		player.sendPacket(htm);
	}

	
	public static HashMap<Player, List<TradeItem>> getItemsBuyShops() 
	{
		HashMap<Player, List<TradeItem>> _map = new HashMap<>();
		
		for(final Player player : World.getInstance().getPlayers()) 
		{
			try 
			{				
				if(player.getOperateType() == OperateType.BUY) {
					List<TradeItem> tmp = new ArrayList<>();
					for (final TradeItem i : player.getBuyList())
					{	
						tmp.add(i);
						
					}
					_map.put(player, tmp);										
				}				
			}
			catch (final Exception e) {
				
			}
		}
		
		return _map;
	}
	
	
	public static HashMap<Player, List<TradeItem>> getItemsSellShops() 
	{
		HashMap<Player, List<TradeItem>> _map = new HashMap<>();
		
		for(final Player player : World.getInstance().getPlayers()) 
		{
			try 
			{				
				if(player.getOperateType() == OperateType.SELL) {
					List<TradeItem> tmp = new ArrayList<>();
					for (final TradeItem i : player.getSellList())
					{	
						tmp.add(i);
						
						
					}
					_map.put(player, tmp);										
				}				
			}
			catch (final Exception e) {
				
			}
		}
		
		return _map;
	}
	
	
	
    @Override
	public String getHtmlPath(int npcId, int val)
    {
        String pom = "";
        if (val == 0)
            pom = "" + npcId;
        else
            pom = npcId + "-" + val;
               
        return "data/html/mods/auction/" + pom + ".htm";
    }
}