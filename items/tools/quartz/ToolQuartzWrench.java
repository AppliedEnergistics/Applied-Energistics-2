package appeng.items.tools.quartz;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.implementations.items.IAEWrench;
import appeng.core.features.AEFeature;
import appeng.items.AEBaseItem;
import buildcraft.api.tools.IToolWrench;
import cpw.mods.fml.common.Optional.Interface;

@Interface(iface = "buildcraft.api.tools.IToolWrench", modid = "BuildCraftAPI|core")
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
		int id = world.getBlockId( x, y, z );
		if ( id > 0 && !player.isSneaking() )
		{
			Block b = Block.blocksList[id];
			if ( b != null )
			{
				if ( b.rotateBlock( world, x, y, z, ForgeDirection.getOrientation( side ) ) )
				{
					player.swingItem();
					return !world.isRemote;
				}
			}
		}
		return false;
	}

	@Override
	public boolean shouldPassSneakingClickToBlock(World w, int x, int y, int z)
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
