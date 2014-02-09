package appeng.helpers;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import appeng.entity.EntityTinyTNTPrimed;

final public class DispenserBehaviorTinyTNT extends BehaviorDefaultDispenseItem
{

	@Override
	protected ItemStack dispenseStack(IBlockSource dispenser, ItemStack dispensedItem)
	{
		EnumFacing enumfacing = BlockDispenser.func_149937_b( dispenser.getBlockMetadata() );
		World world = dispenser.getWorld();
		int i = dispenser.getXInt() + enumfacing.getFrontOffsetX();
		int j = dispenser.getYInt() + enumfacing.getFrontOffsetY();
		int k = dispenser.getZInt() + enumfacing.getFrontOffsetZ();
		EntityTinyTNTPrimed entitytntprimed = new EntityTinyTNTPrimed( world, (double) ((float) i + 0.5F), (double) ((float) j + 0.5F),
				(double) ((float) k + 0.5F), (EntityLiving) null );
		world.spawnEntityInWorld( entitytntprimed );
		--dispensedItem.stackSize;
		return dispensedItem;
	}

}
