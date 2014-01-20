package appeng.items.tools.powered;

import java.util.EnumSet;

import net.minecraft.item.ItemStack;
import appeng.core.features.AEFeature;
import appeng.helpers.IGuiItem;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;

public class ToolNetworkTool extends AEBasePoweredItem implements IGuiItem
{

	public ToolNetworkTool() {
		super( ToolNetworkTool.class, null );
		setfeature( EnumSet.of( AEFeature.NetworkTool, AEFeature.PoweredTools ) );
		maxStoredPower = 100000;
	}

	@Override
	public Object getGuiObject(ItemStack is)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
