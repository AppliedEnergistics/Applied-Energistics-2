package appeng.parts.reporting;

import net.minecraft.item.ItemStack;
import appeng.api.implementations.IPartStorageMonitor;
import appeng.api.storage.data.IAEStack;
import appeng.client.texture.CableBusTextures;

public class PartStorageMonitor extends PartMonitor implements IPartStorageMonitor
{

	protected PartStorageMonitor(Class myClass, ItemStack is) {
		super( myClass, is );
	}

	public PartStorageMonitor(ItemStack is) {
		super( PartStorageMonitor.class, is );
		frontBright = CableBusTextures.PartStorageMonitor_Bright;
		frontColored = CableBusTextures.PartStorageMonitor_Colored;
		frontDark = CableBusTextures.PartStorageMonitor_Dark;
		frontSolid = CableBusTextures.PartStorageMonitor_Solid;
	}

	@Override
	public IAEStack getDisplayed()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLocked()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
