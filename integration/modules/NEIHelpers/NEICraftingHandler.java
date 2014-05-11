package appeng.integration.modules.NEIHelpers;

import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketNEIRecipe;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.IRecipeHandler;

public class NEICraftingHandler implements IOverlayHandler
{

	public NEICraftingHandler(int x, int y) {
		offsetx = x;
		offsety = y;
	}

	int offsetx;
	int offsety;

	// @override
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

	// @override
	public void overlayRecipe(GuiContainer gui, List<PositionedStack> ingredients, boolean shift)
	{
		try
		{
			NBTTagCompound recipe = new NBTTagCompound();

			if ( gui instanceof GuiCraftingTerm )
			{
				for (int i = 0; i < ingredients.size(); i++)// identify slots
				{
					PositionedStack pstack = ingredients.get( i );
					int col = (pstack.relx - 25) / 18;
					int row = (pstack.rely - 6) / 18;
					if ( pstack.item != null )
					{
						for (Slot slot : (List<Slot>) gui.inventorySlots.inventorySlots)
						{
							if ( slot instanceof SlotCraftingMatrix )
							{
								SlotCraftingMatrix ctSlot = (SlotCraftingMatrix) slot;
								if ( ctSlot.getSlotIndex() == col + row * 3 )
								{
									NBTTagCompound inbt = new NBTTagCompound();
									pstack.item.writeToNBT( inbt );
									recipe.setTag( "#" + ((SlotCraftingMatrix) slot).getSlotIndex(), inbt );
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
