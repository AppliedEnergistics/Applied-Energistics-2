package appeng.util.item;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import appeng.util.Platform;

public class SharedSearchObject
{

	int def;
	int hash;
	NBTTagCompound compound;
	public AESharedNBT shared;

	public SharedSearchObject(Item itemID, int damageValue, NBTTagCompound tagCompound) {
		def = (damageValue << Platform.DEF_OFFSET) | Item.itemRegistry.getIDForObject( itemID );
		hash = Platform.NBTOrderlessHash( tagCompound );
		compound = tagCompound;
	}

	@Override
	public boolean equals(Object obj)
	{
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		SharedSearchObject other = (SharedSearchObject) obj;
		if ( def == other.def && hash == other.hash )
		{
			return Platform.NBTEqualityTest( compound, other.compound );
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return def ^ hash;
	}

}
