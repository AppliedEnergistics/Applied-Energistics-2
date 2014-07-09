package appeng.client.me;

import net.minecraft.util.StatCollector;
import appeng.tile.inventory.AppEngInternalInventory;

public class ClientDCInternalInv
{

	final public String unlocalizedName;
	final public long id;
	final public AppEngInternalInventory inv;

	public ClientDCInternalInv(int size, long id, String unlocalizedName) {
		inv = new AppEngInternalInventory( null, size );
		this.unlocalizedName = unlocalizedName;
		this.id = id;
	}

	public String getName()
	{
		String s = StatCollector.translateToLocal( unlocalizedName + ".name" );
		if ( s.equals( unlocalizedName + ".name" ) )
			return StatCollector.translateToLocal( unlocalizedName );
		return s;
	}

}