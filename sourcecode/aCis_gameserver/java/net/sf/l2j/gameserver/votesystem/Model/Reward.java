package net.sf.l2j.gameserver.votesystem.Model;


import net.sf.l2j.commons.data.StatSet;

/**
 * @author l2.topgameserver.net
 *
 */
public class Reward
{
	private int _itemId;
	private int _itemCount;
	
	public Reward(StatSet set)
	{
		_itemId = set.getInteger("itemId");
		_itemCount = set.getInteger("itemCount");
	}

	public void setItemId(int itemId) {
		_itemId = itemId;
	}
	public void setItemCount(int itemCount) {
		_itemCount = itemCount;
	}
	public int getItemId() {
		return _itemId;
	}
	public int getItemCount() {
		return _itemCount;
	}
}