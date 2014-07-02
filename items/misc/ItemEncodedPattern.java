package appeng.items.misc;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import appeng.api.AEApi;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.client.render.items.ItemEncodedPatternRenderer;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.helpers.PatternHelper;
import appeng.items.AEBaseItem;
import appeng.util.Platform;

public class ItemEncodedPattern extends AEBaseItem implements ICraftingPatternItem
{

	public ItemEncodedPattern() {
		super( ItemEncodedPattern.class );
		setfeature( EnumSet.of( AEFeature.Crafting ) );
		setMaxStackSize( 1 );
		if ( Platform.isClient() )
			MinecraftForgeClient.registerItemRenderer( this, new ItemEncodedPatternRenderer() );
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		InventoryPlayer inv = player.inventory;

		for (int s = 0; s < player.inventory.getSizeInventory(); s++)
		{
			if ( inv.getStackInSlot( s ) == stack )
			{
				inv.setInventorySlotContents( s, AEApi.instance().materials().materialBlankPattern.stack( stack.stackSize ) );
				return true;
			}
		}

		return false;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World w, EntityPlayer p)
	{
		return AEApi.instance().materials().materialBlankPattern.stack( stack.stackSize );
	}

	@Override
	public void addInformation(ItemStack is, EntityPlayer p, List l, boolean more)
	{
		NBTTagCompound encodedValue = is.getTagCompound();

		if ( encodedValue == null )
			return;

		NBTTagList inTag = encodedValue.getTagList( "in", 10 );
		NBTTagList outTag = encodedValue.getTagList( "out", 10 );
		boolean isCrafting = encodedValue.getBoolean( "crafting" );

		// kinda needs info..
		if ( inTag.tagCount() == 0 || outTag.tagCount() == 0 )
			return;

		ItemStack[] in = new ItemStack[inTag.tagCount()];
		ItemStack[] out = new ItemStack[outTag.tagCount()];

		readItems( in, inTag );
		readItems( out, outTag );

		String label = (isCrafting ? GuiText.Crafts.getLocal() : GuiText.Creates.getLocal()) + ": ";
		String and = " " + GuiText.And.getLocal() + " ";
		String with = GuiText.With.getLocal() + ": ";

		boolean first = true;
		for (int x = 0; x < out.length; x++)
		{
			if ( out[x] == null )
				continue;

			l.add( (first ? label : and) + out[x].stackSize + " " + Platform.getItemDisplayName( out[x] ) );
			first = false;
		}

		first = true;
		for (int x = 0; x < in.length; x++)
		{
			if ( in[x] == null )
				continue;

			l.add( (first ? with : and) + in[x].stackSize + " " + Platform.getItemDisplayName( in[x] ) );
			first = false;
		}
	}

	public ItemStack getOutput(ItemStack item)
	{
		NBTTagCompound encodedValue = item.getTagCompound();

		if ( encodedValue == null )
			return null;

		NBTTagList outTag = encodedValue.getTagList( "out", 10 );

		if ( outTag.tagCount() == 0 )
			return null;

		ItemStack out = null;

		for (int x = 0; x < outTag.tagCount(); x++)
		{
			ItemStack readItem = ItemStack.loadItemStackFromNBT( outTag.getCompoundTagAt( x ) );
			if ( readItem != null )
			{
				if ( out == null )
					out = readItem;
				else if ( out != null && Platform.isSameItemPrecise( readItem, out ) )
					out.stackSize += readItem.stackSize;
			}
		}

		return out;
	}

	private void readItems(ItemStack[] itemList, NBTTagList tagSrc)
	{
		for (int x = 0; x < itemList.length; x++)
		{
			ItemStack readItem = ItemStack.loadItemStackFromNBT( tagSrc.getCompoundTagAt( x ) );
			if ( readItem != null )
			{
				boolean used = false;

				for (int y = 0; y < x; y++)
				{
					if ( itemList[y] != null && Platform.isSameItemPrecise( readItem, itemList[y] ) )
					{
						itemList[y].stackSize += readItem.stackSize;
						used = true;
					}
				}

				if ( !used )
					itemList[x] = readItem;
			}
		}
	}

	@Override
	public ICraftingPatternDetails getPatternForItem(ItemStack is, World w)
	{
		if ( is.hasTagCompound() )
			return new PatternHelper( is, w );
		return null;
	}

}
