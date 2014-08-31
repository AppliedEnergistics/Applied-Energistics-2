package appeng.items.tools.quartz;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.implementations.items.IAEWrench;
import appeng.api.util.DimensionalCoord;
import appeng.core.features.AEFeature;
import appeng.items.AEBaseItem;
import appeng.transformer.annotations.integration.Interface;
import appeng.util.Platform;
import buildcraft.api.tools.IToolWrench;

@Interface(iface = "buildcraft.api.tools.IToolWrench", iname = "BC")
public class ToolQuartzWrench extends AEBaseItem implements IAEWrench, IToolWrench
{

	public ToolQuartzWrench(AEFeature type) {
		super( ToolQuartzWrench.class, type.name() );
		setfeature( EnumSet.of( type, AEFeature.QuartzWrench ) );
		setMaxStackSize( 1 );
	}

	@Override
	public boolean onItemUseFirst(ItemStack is, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		Block b = world.getBlock( x, y, z );
		if ( b != null && !player.isSneaking() && Platform.hasPermissions( new DimensionalCoord( world, x, y, z ), player ) )
		{
			if ( Platform.isClient() )
				return true;

			ForgeDirection mySide = ForgeDirection.getOrientation( side );
			if ( b.rotateBlock( world, x, y, z, mySide ) )
			{
				b.onNeighborBlockChange( world, x, y, z, Platform.air );
				player.swingItem();
				return !world.isRemote;
			}
		}
		return false;
	}

	@Override
	// public boolean shouldPassSneakingClickToBlock(World w, int x, int y, int z)
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
	{
		return true;
	}

	@Override
	public boolean canWrench(ItemStack is, EntityPlayer player, int x, int y, int z)
	{
		return true;
	}

	@Override
	public boolean canWrench(EntityPlayer player, int x, int y, int z)
	{
		return true;
	}

	@Override
	public void wrenchUsed(EntityPlayer player, int x, int y, int z)
	{
		player.swingItem();
	}

}
