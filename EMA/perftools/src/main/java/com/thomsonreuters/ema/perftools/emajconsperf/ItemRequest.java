package com.thomsonreuters.ema.perftools.emajconsperf;

import com.thomsonreuters.ema.perftools.common.ItemInfo;

/** Item request information. */
public class ItemRequest
{
	private int _requestState;	/* Item state. */
	private String _itemName;	/* Storage for the item's name. */
	private ItemInfo _itemInfo;	/* Structure containing item information. */
	
	{
		_itemInfo = new ItemInfo();
	}
	
	/** Clears an ItemRequest. */
	public void clear()
	{
		_itemInfo.clear();
		_requestState = ItemRequestState.NOT_REQUESTED;
	}

	/** Item state. */
	public int requestState()
	{
		return _requestState;
	}

	/** Item state. */
	public void requestState(int requestState)
	{
		_requestState = requestState;
	}

	/** Storage for the item's name. */
	public String itemName()
	{
		return _itemName;
	}

	/** Storage for the item's name. */
	public void itemName(String itemName)
	{
		_itemName = itemName;
	}

	/** Structure containing item information. */
	public ItemInfo itemInfo()
	{
		return _itemInfo;
	}

	/** Structure containing item information. */
	public void itemInfo(ItemInfo itemInfo)
	{
		_itemInfo = itemInfo;
	}
}
