package appeng.util.item;

import net.minecraft.nbt.NBTTagCompound;
import appeng.util.Platform;

public class SharedSearchObject
{

	int def;
	int hash;
	NBTTagCompound compound;
	public AESharedNBT shared;

	public SharedSearchObject(int itemID, int damageValue, NBTTagCompound tagCompound) {
		def = (damageValue << Platform.DEF_OFFSET) | itemID;
		hash = Platform.NBTOrderlessHash( tagCompound );
		compound = tagCompound;
	}

	@Override
	public boolean equals(Object obj)
	{
		if ( obj == null )
			return false;
		SharedSearchObject b = (SharedSearchObject) obj;
		if ( def == b.def && hash == b.hash )
		{
			return Platform.NBTEqualityTest( compound, b.compound );
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return def ^ hash;
	}

}
