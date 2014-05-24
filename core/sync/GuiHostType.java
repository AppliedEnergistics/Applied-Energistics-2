package appeng.core.sync;

public enum GuiHostType
{
	ITEM_OR_WORLD, ITEM, WORLD;

	public boolean isItem()
	{
		return this != WORLD;
	}

	public boolean isTile()
	{
		return this != ITEM;
	}
}
