package appeng.integration.modules.BCHelpers;

import net.minecraftforge.common.util.ForgeDirection;
import appeng.util.Platform;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

public class AERotatableBlockSchematic extends SchematicBlock
{

	@Override
	public void rotateLeft(IBuilderContext context)
	{
		if ( meta < 6 )
		{
			ForgeDirection d = Platform.rotateAround( ForgeDirection.values()[meta], ForgeDirection.DOWN );
			meta = d.ordinal();
		}
	}

}
