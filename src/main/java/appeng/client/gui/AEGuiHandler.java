package appeng.client.gui;

import javax.annotation.Nullable;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.implementations.GuiCraftAmount;
import appeng.client.gui.implementations.GuiCraftConfirm;
import appeng.client.gui.implementations.GuiCraftingCPU;
import appeng.client.gui.widgets.GuiCustomSlot;
import appeng.container.implementations.ContainerCraftAmount;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class AEGuiHandler implements IAdvancedGuiHandler<AEBaseGui>
{

    @Override
    public Class<AEBaseGui> getGuiContainerClass()
    {
        return AEBaseGui.class;
    }

    @Nullable
    @Override
    public List<Rectangle> getGuiExtraAreas( AEBaseGui guiContainer )
    {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public Object getIngredientUnderMouse( AEBaseGui guiContainer, int mouseX, int mouseY )
    {
        List<IAEItemStack> visual = new ArrayList<>();
        int guiSlotIdx = 0;
        Object result = null;
        if( guiContainer instanceof GuiCraftConfirm )
        {
            guiSlotIdx = getSlotidx( guiContainer, mouseX, mouseY, ( (GuiCraftConfirm) guiContainer ).getDisplayedRows() );
            visual = ( (GuiCraftConfirm) guiContainer ).getVisual();
            if( guiSlotIdx < visual.size() && guiSlotIdx != -1 )
            {
                result = visual.get( guiSlotIdx ).getDefinition();
            }
            else
            {
                return null;
            }
        }

        if( guiContainer instanceof GuiCraftingCPU )
        {
            guiSlotIdx = getSlotidx( guiContainer, mouseX, mouseY, ( (GuiCraftingCPU) guiContainer ).getDisplayedRows() );
            visual = ( (GuiCraftingCPU) guiContainer ).getVisual();
            if( guiSlotIdx < visual.size() && guiSlotIdx != -1 )
            {
                result = visual.get( guiSlotIdx ).getDefinition();
            }
            else
            {
                return null;
            }
        }
        
        if( guiContainer instanceof GuiCraftAmount )
        {
            if( guiContainer.getSlotUnderMouse() != null )
            {
                result = guiContainer.getSlotUnderMouse().getStack();
            }
        }
        return result;
    }

    private int getSlotidx(AEBaseGui guiContainer, int mouseX, int mouseY, int rows)
    {
        int guileft = guiContainer.getGuiLeft();
        int guitop = guiContainer.getGuiTop();
        int currentScroll = guiContainer.getScrollBar().getCurrentScroll();
        final int xo = 9;
        final int yo = 19;

        int guiSlotx = ( mouseX - guileft - xo ) / 67;
        if( guiSlotx > 2 || mouseX < guileft + xo ) return -1;
        int guiSloty = ( mouseY - guitop - yo ) / 23;
        if( guiSloty > ( rows - 1 ) || mouseY < guitop + yo ) return -1;
        return ( guiSloty * 3 ) + guiSlotx + ( currentScroll * 3 );
    }
}
