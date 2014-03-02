package appeng.hooks;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.items.tools.powered.ToolMassCannon;
import appeng.util.Platform;

final public class DispenserMatterCannon extends BehaviorDefaultDispenseItem
{

	@Override
	protected ItemStack dispenseStack(IBlockSource dispenser, ItemStack dispensedItem)
	{
		Item i = dispensedItem.getItem();
		if ( i instanceof ToolMassCannon )
		{
			EnumFacing enumfacing = BlockDispenser.func_149937_b( dispenser.getBlockMetadata() );
			ForgeDirection dir = ForgeDirection.UNKNOWN;
			for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS)
			{
				if ( enumfacing.getFrontOffsetX() == d.offsetX && enumfacing.getFrontOffsetY() == d.offsetY && enumfacing.getFrontOffsetZ() == d.offsetZ )
					dir = d;
			}

			ToolMassCannon tm = (ToolMassCannon) i;

			World w = dispenser.getWorld();
			if ( w instanceof WorldServer )
			{
				EntityPlayer p = Platform.getPlayer( (WorldServer) w );
				Platform.configurePlayer( p, dir, dispenser.getBlockTileEntity() );

				p.posX += dir.offsetX;
				p.posY += dir.offsetY;
				p.posZ += dir.offsetZ;

				dispensedItem = tm.onItemRightClick( dispensedItem, w, p );
			}
		}
		return dispensedItem;
	}
}
