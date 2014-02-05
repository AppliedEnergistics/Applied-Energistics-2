package appeng.container.slot;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.AchievementList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public class AppEngCraftingSlot extends AppEngSlot
{

	/** The craft matrix inventory linked to this result slot. */
	private final IInventory craftMatrix;

	/** The player that is using the GUI where this slot resides. */
	private EntityPlayer thePlayer;

	/**
	 * The number of items that have been crafted so far. Gets passed to ItemStack.onCrafting before being reset.
	 */
	private int amountCrafted;

	public AppEngCraftingSlot(EntityPlayer par1EntityPlayer, IInventory par2IInventory, IInventory par3IInventory, int par4, int par5, int par6) {
		super( par3IInventory, par4, par5, par6 );
		this.thePlayer = par1EntityPlayer;
		this.craftMatrix = par2IInventory;
	}

	/**
	 * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
	 */
	public boolean isItemValid(ItemStack par1ItemStack)
	{
		return false;
	}

	/**
	 * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
	 * stack.
	 */
	public ItemStack decrStackSize(int par1)
	{
		if ( this.getHasStack() )
		{
			this.amountCrafted += Math.min( par1, this.getStack().stackSize );
		}

		return super.decrStackSize( par1 );
	}

	/**
	 * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood. Typically increases an
	 * internal count then calls onCrafting(item).
	 */
	protected void onCrafting(ItemStack par1ItemStack, int par2)
	{
		this.amountCrafted += par2;
		this.onCrafting( par1ItemStack );
	}

	/**
	 * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood.
	 */
	protected void onCrafting(ItemStack par1ItemStack)
	{
		par1ItemStack.onCrafting( this.thePlayer.worldObj, this.thePlayer, this.amountCrafted );
		this.amountCrafted = 0;

		if ( par1ItemStack.itemID == Block.workbench.blockID )
		{
			this.thePlayer.addStat( AchievementList.buildWorkBench, 1 );
		}
		else if ( par1ItemStack.itemID == Item.pickaxeWood.itemID )
		{
			this.thePlayer.addStat( AchievementList.buildPickaxe, 1 );
		}
		else if ( par1ItemStack.itemID == Block.furnaceIdle.blockID )
		{
			this.thePlayer.addStat( AchievementList.buildFurnace, 1 );
		}
		else if ( par1ItemStack.itemID == Item.hoeWood.itemID )
		{
			this.thePlayer.addStat( AchievementList.buildHoe, 1 );
		}
		else if ( par1ItemStack.itemID == Item.bread.itemID )
		{
			this.thePlayer.addStat( AchievementList.makeBread, 1 );
		}
		else if ( par1ItemStack.itemID == Item.cake.itemID )
		{
			this.thePlayer.addStat( AchievementList.bakeCake, 1 );
		}
		else if ( par1ItemStack.itemID == Item.pickaxeStone.itemID )
		{
			this.thePlayer.addStat( AchievementList.buildBetterPickaxe, 1 );
		}
		else if ( par1ItemStack.itemID == Item.swordWood.itemID )
		{
			this.thePlayer.addStat( AchievementList.buildSword, 1 );
		}
		else if ( par1ItemStack.itemID == Block.enchantmentTable.blockID )
		{
			this.thePlayer.addStat( AchievementList.enchantments, 1 );
		}
		else if ( par1ItemStack.itemID == Block.bookShelf.blockID )
		{
			this.thePlayer.addStat( AchievementList.bookcase, 1 );
		}
	}

	public void onPickupFromSlot(EntityPlayer par1EntityPlayer, ItemStack par2ItemStack)
	{
		GameRegistry.onItemCrafted( par1EntityPlayer, par2ItemStack, craftMatrix );
		this.onCrafting( par2ItemStack );

		for (int i = 0; i < this.craftMatrix.getSizeInventory(); ++i)
		{
			ItemStack itemstack1 = this.craftMatrix.getStackInSlot( i );

			if ( itemstack1 != null )
			{
				this.craftMatrix.decrStackSize( i, 1 );

				if ( itemstack1.getItem().hasContainerItem() )
				{
					ItemStack itemstack2 = itemstack1.getItem().getContainerItemStack( itemstack1 );

					if ( itemstack2.isItemStackDamageable() && itemstack2.getItemDamage() > itemstack2.getMaxDamage() )
					{
						MinecraftForge.EVENT_BUS.post( new PlayerDestroyItemEvent( thePlayer, itemstack2 ) );
						itemstack2 = null;
					}

					if ( itemstack2 != null
							&& (!itemstack1.getItem().doesContainerItemLeaveCraftingGrid( itemstack1 ) || !this.thePlayer.inventory
									.addItemStackToInventory( itemstack2 )) )
					{
						if ( this.craftMatrix.getStackInSlot( i ) == null )
						{
							this.craftMatrix.setInventorySlotContents( i, itemstack2 );
						}
						else
						{
							this.thePlayer.dropPlayerItem( itemstack2 );
						}
					}
				}
			}
		}
	}
}
