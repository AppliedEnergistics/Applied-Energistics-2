package appeng.core.features.registries;

import net.minecraft.tileentity.TileEntity;

public class WirelessRangeResult
{

	public WirelessRangeResult(TileEntity t, float d) {
		dist = d;
		te = t;
	}

	final public float dist;
	final public TileEntity te;

}
