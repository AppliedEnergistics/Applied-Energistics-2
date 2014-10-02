package appeng.integration.modules.NEIHelpers;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketNEIRecipe;
import appeng.util.Platform;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.IRecipeHandler;

public class NEICraftingHandler implements IOverlayHandler
{

	public NEICraftingHandler(int x, int y)
	{
		offsetX = x;
		offsetY = y;
	}

	final int offsetX;
	final int offsetY;

	@Override
	public void overlayRecipe(GuiContainer gui, IRecipeHandler recipe, int recipeIndex, boolean shift)
	{
		try
		{
			List ingredients = recipe.getIngredientStacks( recipeIndex );
			overlayRecipe( gui, ingredients, shift );
		}
		catch (Exception err)
		{
		}
		catch (Error err)
		{
		}
	}

	public void overlayRecipe(GuiContainer gui, List<PositionedStack> ingredients, boolean shift)
	{
		try
		{
			NBTTagCompound recipe = new NBTTagCompound();

			if ( gui instanceof GuiCraftingTerm || gui instanceof GuiPatternTerm )
			{
				for (PositionedStack positionedStack : ingredients)
				{
					int col = (positionedStack.relx - 25) / 18;
					int row = (positionedStack.rely - 6) / 18;
					if ( positionedStack.items != null && positionedStack.items.length > 0 )
					{
						for (Slot slot : (List<Slot>) gui.inventorySlots.inventorySlots)
						{
							if ( slot instanceof SlotCraftingMatrix || slot instanceof SlotFakeCraftingMatrix )
							{
								if ( slot.getSlotIndex() == col + row * 3 )
								{
									NBTTagList tags = new NBTTagList();
									List<ItemStack> list = new LinkedList<ItemStack>();

									// prefer pure crystals.
									for (int x = 0; x < positionedStack.items.length; x++)
									{
										if ( Platform.isRecipePrioritized( positionedStack.items[x] ) )
										{
											list.add( 0, positionedStack.items[x] );
										}
										else
										{
											list.add( positionedStack.items[x] );
										}
									}

									for (ItemStack is : list)
									{
										NBTTagCompound tag = new NBTTagCompound();
										is.writeToNBT( tag );
										tags.appendTag( tag );
									}

									recipe.setTag( "#" + slot.getSlotIndex(), tags );
									break;
								}
							}
						}
					}
				}

				NetworkHandler.instance.sendToServer( new PacketNEIRecipe( recipe ) );
			}
		}
		catch (Exception err)
		{
		}
		catch (Error err)
		{
		}
	}
}
