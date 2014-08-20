package appeng.integration.modules.NEIHelpers;

import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotFakeCraftingMatrix;
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
				for (int i = 0; i < ingredients.size(); i++)// identify slots
				{
					PositionedStack pstack = ingredients.get( i );
					int col = (pstack.relx - 25) / 18;
					int row = (pstack.rely - 6) / 18;
					if ( pstack.items != null && pstack.items.length > 0 )
					{
						for (Slot slot : (List<Slot>) gui.inventorySlots.inventorySlots)
						{
							if ( slot instanceof SlotCraftingMatrix || slot instanceof SlotFakeCraftingMatrix )
							{
								Slot ctSlot = (Slot) slot;
								if ( ctSlot.getSlotIndex() == col + row * 3 )
								{
									NBTTagList ilist = new NBTTagList();
									for (int x = 0; x < pstack.items.length; x++)
									{
										NBTTagCompound inbt = new NBTTagCompound();
										pstack.items[x].writeToNBT( inbt );
										ilist.appendTag( inbt );
									}
									recipe.setTag( "#" + ctSlot.getSlotIndex(), ilist );
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
