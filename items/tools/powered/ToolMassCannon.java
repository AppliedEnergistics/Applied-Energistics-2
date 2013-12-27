package appeng.items.tools.powered;

import java.util.EnumSet;

import appeng.core.features.AEFeature;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;

public class ToolMassCannon extends AEBasePoweredItem
{

	public ToolMassCannon() {
		super( ToolMassCannon.class, null );
		setfeature( EnumSet.of( AEFeature.MatterCannon, AEFeature.PoweredTools ) );
	}

}
