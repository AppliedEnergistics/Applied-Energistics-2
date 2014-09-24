package appeng.items.parts;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.parts.IAlphaPassItem;
import appeng.block.solids.OreQuartz;
import appeng.client.render.BusRenderer;
import appeng.core.FacadeConfig;
import appeng.core.features.AEFeature;
import appeng.facade.FacadePart;
import appeng.facade.IFacadeItem;
import appeng.items.AEBaseItem;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemFacade extends AEBaseItem implements IFacadeItem, IAlphaPassItem
{

	public ItemFacade() {
		super( ItemFacade.class );
		setFeature( EnumSet.of( AEFeature.Facades ) );
		setHasSubtypes( true );
		if ( Platform.isClient() )
			MinecraftForgeClient.registerItemRenderer( this, BusRenderer.instance );
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

	public List<ItemStack> getFacades()
	{
		calculateSubTypes();
		return subTypes;
	}

	public ItemStack getCreativeTabIcon()
	{
		calculateSubTypes();
		if ( subTypes.isEmpty() )
			return new ItemStack( Items.cake );
		return subTypes.get( 0 );
	}

	@Override
	public void getSubItems(Item number, CreativeTabs tab, List list)
	{
		calculateSubTypes();
		list.addAll( subTypes );
	}

	public ItemStack createFromInts(int[] ids)
	{
		ItemStack is = new ItemStack( AEApi.instance().items().itemFacade.item() );
		NBTTagCompound data = new NBTTagCompound();
		data.setIntArray( "x", ids.clone() );
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
			for (Object blk : Block.blockRegistry)
			{
				Block b = (Block) blk;
				try
				{
					Item item = Item.getItemFromBlock( b );

					List<ItemStack> tmpList = new ArrayList();
					b.getSubBlocks( item, b.getCreativeTabToDisplayOn(), tmpList );
					for (ItemStack l : tmpList)
					{
						ItemStack facade = createFacadeForItem( l, false );
						if ( facade != null )
							subTypes.add( facade );
					}
				}
				catch (Throwable t)
				{
					// just absorb..
				}
			}

			if ( FacadeConfig.instance.hasChanged() )
				FacadeConfig.instance.save();
		}

	}

	public ItemStack createFacadeForItem(ItemStack l, boolean returnItem)
	{
		if ( l == null )
			return null;

		Block b = Block.getBlockFromItem( l.getItem() );
		if ( b == null || l.hasTagCompound() )
			return null;

		int metadata = l.getItem().getMetadata( l.getItemDamage() );

		boolean hasTile = b.hasTileEntity( metadata );
		boolean enableGlass = b instanceof BlockGlass || b instanceof BlockStainedGlass;
		boolean disableOre = b instanceof OreQuartz;

		boolean defaultValue = (b.isOpaqueCube() && !b.getTickRandomly() && !hasTile && !disableOre) || enableGlass;
		if ( FacadeConfig.instance.checkEnabled( b, metadata, defaultValue ) )
		{
			if ( returnItem )
				return l;

			ItemStack is = new ItemStack( this );
			NBTTagCompound data = new NBTTagCompound();
			int[] ds = new int[2];
			ds[0] = Item.getIdFromItem( l.getItem() );
			ds[1] = metadata;
			data.setIntArray( "x", ds );
			UniqueIdentifier ui = GameRegistry.findUniqueIdentifierFor( l.getItem() );
			data.setString( "modid", ui.modId );
			data.setString( "itemname", ui.name );
			is.setTagCompound( data );
			return is;
		}
		return null;
	}

	@Override
	public Block getBlock(ItemStack is)
	{
		NBTTagCompound data = is.getTagCompound();
		if ( data != null )
		{
			if ( data.hasKey( "modid" ) && data.hasKey( "itemname" ) )
			{
				return GameRegistry.findBlock( data.getString( "modid" ), data.getString( "itemname" ) );
			}
			else
			{
				int[] blk = data.getIntArray( "x" );
				if ( blk != null && blk.length == 2 )
					return Block.getBlockById( blk[0] );
			}
		}
		return Blocks.glass;
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
	public String getItemStackDisplayName(ItemStack is)
	{
		try
		{
			ItemStack in = getTextureItem( is );
			if ( in != null )
			{
				return super.getItemStackDisplayName( is ) + " - " + in.getDisplayName();
			}
		}
		catch (Throwable t)
		{

		}

		return super.getItemStackDisplayName( is );
	}

	@Override
	public boolean useAlphaPass(ItemStack is)
	{
		ItemStack out = getTextureItem( is );

		if ( out == null || out.getItem() == null )
			return false;

		Block blk = Block.getBlockFromItem( out.getItem() );
		if ( blk != null && blk.canRenderInPass( 1 ) )
			return true;

		return false;
	}

}
