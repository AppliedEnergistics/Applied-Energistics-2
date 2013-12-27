package appeng.items.parts;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.AEApi;
import appeng.block.solids.OreQuartz;
import appeng.client.render.BusRenderer;
import appeng.core.features.AEFeature;
import appeng.facade.FacadePart;
import appeng.facade.IFacadeItem;
import appeng.items.AEBaseItem;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemFacade extends AEBaseItem implements IFacadeItem
{

	public ItemFacade() {
		super( ItemFacade.class );
		setfeature( EnumSet.of( AEFeature.Facades ) );
		setHasSubtypes( true );
		if ( Platform.isClient() )
			MinecraftForgeClient.registerItemRenderer( this.itemID, BusRenderer.instance );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getSpriteNumber()
	{
		return 0;
	}

	@Override
	public boolean onItemUse(ItemStack is, EntityPlayer player, World w, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		return AEApi.instance().partHelper().placeBus( is, x, y, z, side, player, w );
	}

	@Override
	public FacadePart createPartFromItemStack(ItemStack is, ForgeDirection side)
	{
		ItemStack in = getTextureItem( is );
		if ( in != null )
			return new FacadePart( is, side );
		return null;
	}

	List<ItemStack> subTypes = null;

	@Override
	public void getSubItems(int number, CreativeTabs tab, List list)
	{
		calculateSubTypes();
		list.addAll( subTypes );
	}

	public ItemStack createFromInts(int[] ids)
	{
		ItemStack is = new ItemStack( AEApi.instance().items().itemFacade.item() );
		NBTTagCompound data = new NBTTagCompound();
		data.setIntArray( "x", ids );
		is.setTagCompound( data );
		return is;
	}

	@Override
	public ItemStack getTextureItem(ItemStack is)
	{
		Block blk = getBlock( is );
		if ( blk != null )
			return new ItemStack( blk, 1, getMeta( is ) );
		return null;
	}

	private void calculateSubTypes()
	{
		if ( subTypes == null )
		{
			subTypes = new ArrayList();
			for (Block b : Block.blocksList)
			{
				if ( b != null && (b.isOpaqueCube() && !b.getTickRandomly() && !(b instanceof OreQuartz)) || b instanceof BlockGlass )
				{
					try
					{
						List<ItemStack> tmpList = new ArrayList();
						b.getSubBlocks( b.blockID, b.getCreativeTabToDisplayOn(), tmpList );
						for (ItemStack l : tmpList)
						{
							ItemStack is = new ItemStack( this );
							NBTTagCompound data = new NBTTagCompound();
							int[] ds = new int[2];
							ds[0] = l.itemID;
							ds[1] = l.getItem().getMetadata( l.getItemDamage() );
							data.setIntArray( "x", ds );
							is.setTagCompound( data );

							subTypes.add( is );
						}
					}
					catch (Throwable t)
					{
						// just absorb..
					}
				}
			}
		}
	}

	@Override
	public Block getBlock(ItemStack is)
	{
		NBTTagCompound data = is.getTagCompound();
		if ( data != null )
		{
			int[] blk = data.getIntArray( "x" );
			if ( blk != null && blk.length == 2 )
				return Block.blocksList[blk[0]];
		}
		return Block.glass;
	}

	@Override
	public int getMeta(ItemStack is)
	{
		NBTTagCompound data = is.getTagCompound();
		if ( data != null )
		{
			int[] blk = data.getIntArray( "x" );
			if ( blk != null && blk.length == 2 )
				return blk[1];
		}
		return 0;
	}

	@Override
	public String getItemDisplayName(ItemStack is)
	{
		try
		{
			ItemStack in = getTextureItem( is );
			if ( in != null )
			{
				return super.getItemDisplayName( is ) + " - " + in.getDisplayName();
			}
		}
		catch (Throwable t)
		{

		}

		return super.getItemDisplayName( is );
	}

}
