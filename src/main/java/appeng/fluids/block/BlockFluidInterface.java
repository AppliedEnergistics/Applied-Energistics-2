
package appeng.fluids.block;


import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseTileBlock;
import appeng.core.sync.GuiBridge;
import appeng.fluids.tile.TileFluidInterface;
import appeng.util.Platform;


public class BlockFluidInterface extends AEBaseTileBlock
{
	public BlockFluidInterface()
	{
		super( Material.IRON );
	}

	@Override
	public boolean onActivated( final World w, final BlockPos pos, final EntityPlayer p, final EnumHand hand, final @Nullable ItemStack heldItem, final EnumFacing side, final float hitX, final float hitY, final float hitZ )
	{
		if( p.isSneaking() )
		{
			return false;
		}

		final TileEntity tg = this.getTileEntity( w, pos );
		if( tg instanceof TileFluidInterface )
		{
			if( Platform.isServer() )
			{
				Platform.openGUI( p, tg, AEPartLocation.fromFacing( side ), GuiBridge.GUI_FLUID_INTERFACE );
			}
			return true;
		}
		return false;
	}
}
