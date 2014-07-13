package appeng.client.me;

import net.minecraft.util.StatCollector;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.ItemSorters;

public class ClientDCInternalInv implements Comparable<ClientDCInternalInv>
{

	final public String unlocalizedName;
	final public AppEngInternalInventory inv;
	final public long id;
	final public long sortBy;

	public ClientDCInternalInv(int size, long id, long sortBy, String unlocalizedName) {
		inv = new AppEngInternalInventory( null, size );
		this.unlocalizedName = unlocalizedName;
		this.id = id;
		this.sortBy = sortBy;
	}

	public String getName()
	{
		String s = StatCollector.translateToLocal( unlocalizedName + ".name" );
		if ( s.equals( unlocalizedName + ".name" ) )
			return StatCollector.translateToLocal( unlocalizedName );
		return s;
	}

	@Override
	public int compareTo(ClientDCInternalInv o)
	{
		return ItemSorters.compareLong( sortBy, o.sortBy );
	}

}