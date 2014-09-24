package appeng.core.features;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import appeng.api.util.AEItemDefinition;

public class WrappedDamageItemDefinition implements AEItemDefinition
{

	final AEItemDefinition baseItem;
	final int damage;

	public WrappedDamageItemDefinition(AEItemDefinition def, int dmg) {
		baseItem = def;
		damage = dmg;
	}

	@Override
	public Block block()
	{
		return baseItem.block();
	}

	@Override
	public Item item()
	{
		return baseItem.item();
	}

	@Override
	public Class<? extends TileEntity> entity()
	{
		return baseItem.entity();
	}

	@Override
	public ItemStack stack(int stackSize)
	{
		if ( baseItem == null )
			return null;

		return new ItemStack( baseItem.block(), stackSize, damage );
	}

	@Override
	public boolean sameAsStack(ItemStack comparableItem)
	{
		if ( comparableItem == null )
			return false;

		return comparableItem.getItem() == baseItem.item() && comparableItem.getItemDamage() == damage;
	}

	@Override
	public boolean sameAsBlock(IBlockAccess world, int x, int y, int z)
	{
		if ( block() != null )
			return world.getBlock( x, y, z ) == block() && world.getBlockMetadata( x, y, z ) == damage;
		return false;
	}

}
