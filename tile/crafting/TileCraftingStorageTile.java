package appeng.tile.crafting;

public class TileCraftingStorageTile extends TileCraftingTile
{

	public boolean isAccelerator()
	{
		return false;
	}

	public boolean isStorage()
	{
		return true;
	}

	public int getStorageBytes()
	{
		switch (worldObj.getBlockMetadata( xCoord, yCoord, zCoord ) & 3)
		{
		default:
		case 0:
			return 1 * 1024;
		case 1:
			return 4 * 1024;
		case 2:
			return 16 * 1024;
		case 3:
			return 64 * 1024;
		}
	}
}
