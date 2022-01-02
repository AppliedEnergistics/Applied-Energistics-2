package appeng.client.gui;

import javax.annotation.Nullable;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.implementations.*;
import appeng.container.interfaces.IJEIGhostIngredients;
import appeng.container.slot.SlotFake;
import appeng.fluids.client.gui.widgets.GuiFluidSlot;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class AEGuiHandler implements IAdvancedGuiHandler<AEBaseGui>, IGhostIngredientHandler<AEBaseGui>
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
        List<IAEItemStack> visual;
        int guiSlotIdx;
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

    @Override
    public <I> List<Target<I>> getTargets( AEBaseGui gui, I ingredient, boolean doStart )
    {
        ArrayList<Target<I>> targets = new ArrayList<>();
        if( gui instanceof IJEIGhostIngredients )
        {
            IJEIGhostIngredients g = (IJEIGhostIngredients) gui;
            List<Target<?>> phantomTargets = g.getPhantomTargets( ingredient );
            targets.addAll( (List<Target<I>>) (Object) phantomTargets );
        }
        if( doStart && GuiScreen.isShiftKeyDown() && Mouse.isButtonDown( 0 ) )
        {
            if( gui instanceof GuiUpgradeable || gui instanceof GuiPatternTerm || gui instanceof GuiExpandedProcessingPatternTerm )
            {
                IJEIGhostIngredients ghostGui = ( (IJEIGhostIngredients) gui );
                for( Target<I> target : targets )
                {
                    if( ghostGui.getFakeSlotTargetMap().get( target ) instanceof SlotFake )
                    {
                        if( ( (SlotFake) ghostGui.getFakeSlotTargetMap().get( target ) ).getStack().isEmpty() )
                        {
                            target.accept( ingredient );
                            break;
                        }
                    }
                    else if( ghostGui.getFakeSlotTargetMap().get( target ) instanceof GuiFluidSlot )
                    {
                        if( ( (GuiFluidSlot) ghostGui.getFakeSlotTargetMap().get( target ) ).getFluidStack() == null )
                        {
                            target.accept( ingredient );
                            break;
                        }
                    }
                }
            }
        }
        return targets;
    }

    @Override
    public void onComplete(){
    }

    @Override
    public boolean shouldHighlightTargets()
    {
        return true;
    }

}
