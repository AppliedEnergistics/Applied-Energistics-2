package appeng.integration.modules.helpers;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import appeng.container.slot.SlotCraftingMatrix;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.IRecipeHandler;
import cpw.mods.fml.common.network.PacketDispatcher;

public class NEICraftingTerminalOverlayHandler implements IOverlayHandler
{

	public NEICraftingTerminalOverlayHandler(int x, int y) {
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

			if ( gui instanceof GuiTerminal )
			{
				for (int i = 0; i < ingredients.size(); i++)// identify slots
				{
					LinkedList<Slot> recipeSlots = new LinkedList<Slot>();
					PositionedStack pstack = ingredients.get( i );
					if ( pstack.item != null )
					{
						for (Slot slot : (List<Slot>) gui.inventorySlots.inventorySlots)
						{
							if ( slot.xDisplayPosition == pstack.relx + offsetx && slot.yDisplayPosition == pstack.rely + offsety )
							{
								if ( slot instanceof SlotCraftingMatrix )
								{
									NBTTagCompound inbt = new NBTTagCompound();
									pstack.item.writeToNBT( inbt );
									recipe.setCompoundTag( "#" + ((SlotCraftingMatrix) slot).matrixID, inbt );
									break;
								}
							}
						}
					}
				}

				PacketDispatcher.sendPacketToServer( (new PacketNEIRecipe( recipe )).getPacket() );
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
