package appeng.block.crafting;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.tile.crafting.TileCraftingTile;

public class BlockCraftingUnit extends AEBaseBlock
{

	public final static int BASE_DAMAGE = 0;
	public final static int BASE_MONITOR = 1;
	public final static int BASE_STORAGE = 2;
	public final static int BASE_ACCELERATOR = 3;

	public BlockCraftingUnit() {
		super( BlockCraftingUnit.class, Material.iron );
		hasSubtypes = true;
		setfeature( EnumSet.of( AEFeature.Crafting ) );
		setTileEntiy( TileCraftingTile.class );
	}

	public ItemStack getItemStack(World world, int x, int y, int z)
	{
		TileCraftingTile ct = getTileEntity( world, x, y, z );

		int meta = world.getBlockMetadata( x, y, z );
		if ( ct != null && meta == BASE_STORAGE )
		{
			return createStackForBytes( ct.getStorageBytes() );
		}

		return new ItemStack( this, 1, meta );
	}

	private ItemStack createStackForBytes(long storageBytes)
	{
		ItemStack itemDetails = new ItemStack( this, 1, BASE_STORAGE );
		NBTTagCompound tag = new NBTTagCompound();
		tag.setLong( "bytes", storageBytes );
		itemDetails.setTagCompound( tag );
		return itemDetails;
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
	{
		ArrayList<ItemStack> out = new ArrayList();

		ItemStack is = getItemStack( world, x, y, z );
		if ( is != null )
			out.add( is );

		return out;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		return getItemStack( world, x, y, z );
	}

	@Override
	public void getSubBlocks(Item i, CreativeTabs c, List l)
	{
		l.add( new ItemStack( this, 1, BASE_DAMAGE ) );
		l.add( new ItemStack( this, 1, BASE_MONITOR ) );
		l.add( new ItemStack( this, 1, BASE_ACCELERATOR ) );
		l.add( createStackForBytes( 1024 ) );
		l.add( createStackForBytes( 1024 * 4 ) );
		l.add( createStackForBytes( 1024 * 16 ) );
		l.add( createStackForBytes( 1024 * 64 ) );
	}

	@Override
	public Class getItemBlockClass()
	{
		return ItemBlockCraftingUnit.class;
	}

}
