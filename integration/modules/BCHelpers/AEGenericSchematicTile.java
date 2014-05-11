package appeng.integration.modules.BCHelpers;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;

public class AEGenericSchematicTile extends SchematicTile
{

	@Override
	public void writeRequirementsToSchematic(IBuilderContext context, int x, int y, int z)
	{
		TileEntity tile = context.world().getTileEntity( x, y, z );
		ArrayList<ItemStack> list = new ArrayList();
		if ( tile instanceof AEBaseTile )
		{
			AEBaseTile tcb = (AEBaseTile) tile;
			tcb.getDrops( tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord, list );
		}

		storedRequirements = list.toArray( new ItemStack[list.size()] );
	}

	@Override
	public void rotateLeft(IBuilderContext context)
	{
		if ( cpt.hasKey( "orientation_forward" ) && cpt.hasKey( "orientation_up" ) )
		{
			String forward = cpt.getString( "orientation_forward" );
			String up = cpt.getString( "orientation_up" );

			if ( forward != null && up != null )
			{
				try
				{
					ForgeDirection fdForward = ForgeDirection.valueOf( forward );
					ForgeDirection fdUp = ForgeDirection.valueOf( up );

					fdForward = Platform.rotateAround( fdForward, ForgeDirection.DOWN );
					fdUp = Platform.rotateAround( fdUp, ForgeDirection.DOWN );

					cpt.setString( "orientation_forward", fdForward.name() );
					cpt.setString( "orientation_up", fdUp.name() );
				}
				catch (Throwable t)
				{

				}
			}
		}
	}

}
