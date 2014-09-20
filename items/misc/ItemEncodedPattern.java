package appeng.items.misc;

import java.util.EnumSet;
import java.util.List;
import java.util.WeakHashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import appeng.api.AEApi;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.render.items.ItemEncodedPatternRenderer;
import appeng.core.CommonHelper;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.helpers.PatternHelper;
import appeng.items.AEBaseItem;
import appeng.util.Platform;

public class ItemEncodedPattern extends AEBaseItem implements ICraftingPatternItem
{

	public ItemEncodedPattern() {
		super( ItemEncodedPattern.class );
		setfeature( EnumSet.of( AEFeature.Patterns ) );
		setMaxStackSize( 1 );
		if ( Platform.isClient() )
			MinecraftForgeClient.registerItemRenderer( this, new ItemEncodedPatternRenderer() );
	}

	private boolean clearPattern(ItemStack stack, EntityPlayer player)
	{
		if ( player.isSneaking() )
		{
			if ( Platform.isClient() )
				return false;

			InventoryPlayer inv = player.inventory;

			for (int s = 0; s < player.inventory.getSizeInventory(); s++)
			{
				if ( inv.getStackInSlot( s ) == stack )
				{
					inv.setInventorySlotContents( s, AEApi.instance().materials().materialBlankPattern.stack( stack.stackSize ) );
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		return clearPattern( stack, player );
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World w, EntityPlayer player)
	{
		clearPattern( stack, player );
		return stack;
	}

	@Override
	public void addInformation(ItemStack is, EntityPlayer p, List l, boolean more)
	{
		ICraftingPatternDetails details = getPatternForItem( is, p.worldObj );

		if ( details == null )
		{
			l.add( EnumChatFormatting.RED + GuiText.InvalidPattern.getLocal() );
			return;
		}

		boolean isCrafting = details.isCraftable();

		IAEItemStack[] in = details.getCondencedInputs();
		IAEItemStack[] out = details.getCondensedOutputs();

		String label = (isCrafting ? GuiText.Crafts.getLocal() : GuiText.Creates.getLocal()) + ": ";
		String and = " " + GuiText.And.getLocal() + " ";
		String with = GuiText.With.getLocal() + ": ";

		boolean first = true;
		for (int x = 0; x < out.length; x++)
		{
			if ( out[x] == null )
				continue;

			l.add( (first ? label : and) + out[x].getStackSize() + " " + Platform.getItemDisplayName( out[x] ) );
			first = false;
		}

		first = true;
		for (int x = 0; x < in.length; x++)
		{
			if ( in[x] == null )
				continue;

			l.add( (first ? with : and) + in[x].getStackSize() + " " + Platform.getItemDisplayName( in[x] ) );
			first = false;
		}
	}

	// rather simple client side cacheing.
	static WeakHashMap<ItemStack, ItemStack> simpleCache = new WeakHashMap<ItemStack, ItemStack>();

	public ItemStack getOutput(ItemStack item)
	{
		ItemStack out = simpleCache.get( item );
		if ( out != null )
			return out;

		World w = CommonHelper.proxy.getWorld();
		if ( w == null )
			return null;

		ICraftingPatternDetails details = getPatternForItem( item, w );

		if ( details == null )
			return null;

		simpleCache.put( item, out = details.getCondensedOutputs()[0].getItemStack() );
		return out;
	}

	@Override
	public ICraftingPatternDetails getPatternForItem(ItemStack is, World w)
	{
		try
		{
			return new PatternHelper( is, w );
		}
		catch (Throwable t)
		{
			return null;
		}
	}

}
