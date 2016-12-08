package appeng.client.gui;


import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


/**
 * A proxy for a slot that will always return an itemstack with size 1, if there is an item in the slot.
 * Used to prevent the default item count from rendering.
 */
class Size1Slot extends Slot
{

	private final Slot delegate;

	public Size1Slot( Slot delegate )
	{
		super( delegate.inventory, delegate.getSlotIndex(), delegate.xPos, delegate.yPos );
		this.delegate = delegate;
	}

	@Nullable
	@Override
	public ItemStack getStack()
	{
		ItemStack orgStack = delegate.getStack();
		if( orgStack != null )
		{
			ItemStack modifiedStack = orgStack.copy();
			modifiedStack.setCount( 1 );
			return modifiedStack;
		}

		return null;
	}

	@Override
	public boolean getHasStack()
	{
		return delegate.getHasStack();
	}

	@Override
	public boolean isHere( IInventory inv, int slotIn )
	{
		return delegate.isHere( inv, slotIn );
	}

	@Override
	public int getSlotStackLimit()
	{
		return delegate.getSlotStackLimit();
	}

	@Override
	public int getItemStackLimit( ItemStack stack )
	{
		return delegate.getItemStackLimit( stack );
	}

	@Override
	@Nullable
	@SideOnly( Side.CLIENT)
	public String getSlotTexture()
	{
		return delegate.getSlotTexture();
	}

	@Override
	public boolean canTakeStack( EntityPlayer playerIn )
	{
		return delegate.canTakeStack( playerIn );
	}

	@Override
	@SideOnly( Side.CLIENT)
	public boolean canBeHovered()
	{
		return delegate.canBeHovered();
	}

	@Override
	@SideOnly( Side.CLIENT)
	public ResourceLocation getBackgroundLocation()
	{
		return delegate.getBackgroundLocation();
	}

	@Override
	@SideOnly( Side.CLIENT)
	public TextureAtlasSprite getBackgroundSprite()
	{
		return delegate.getBackgroundSprite();
	}

	@Override
	public int getSlotIndex()
	{
		return delegate.getSlotIndex();
	}

	@Override
	public boolean isSameInventory( Slot other )
	{
		return delegate.isSameInventory( other );
	}
}
