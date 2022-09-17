package appeng.fluids.client.gui.widgets;


import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.widgets.GuiCustomSlot;
import appeng.container.slot.IJEITargetSlot;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketFluidSlot;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.IAEFluidTank;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import java.util.Collections;


public class GuiFluidSlot extends GuiCustomSlot implements IJEITargetSlot {
    private final IAEFluidTank fluids;
    private final int slot;

    public GuiFluidSlot(final IAEFluidTank fluids, final int slot, final int id, final int x, final int y) {
        super(id, x, y);
        this.fluids = fluids;
        this.slot = slot;
    }

    @Override
    public void drawContent(final Minecraft mc, final int mouseX, final int mouseY, final float partialTicks) {
        final IAEFluidStack fs = this.getFluidStack();
        if (fs != null) {
            GlStateManager.disableLighting();
            GlStateManager.disableBlend();
            final Fluid fluid = fs.getFluid();
            mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            final TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(fluid.getStill().toString());

            // Set color for dynamic fluids
            // Convert int color to RGB
            final float red = (fluid.getColor() >> 16 & 255) / 255.0F;
            final float green = (fluid.getColor() >> 8 & 255) / 255.0F;
            final float blue = (fluid.getColor() & 255) / 255.0F;
            GlStateManager.color(red, green, blue);

            this.drawTexturedModalRect(this.xPos(), this.yPos(), sprite, this.getWidth(), this.getHeight());
        }
    }

    @Override
    public boolean canClick(final EntityPlayer player) {
        final ItemStack mouseStack = player.inventory.getItemStack();
        return mouseStack.isEmpty() || mouseStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
    }

    @Override
    public void slotClicked(final ItemStack clickStack, int mouseButton) {
        if (clickStack.isEmpty() || mouseButton == 1) {
            this.setFluidStack(null);
        } else if (mouseButton == 0) {
            final FluidStack fluid = FluidUtil.getFluidContained(clickStack);
            if (fluid != null) {
                this.setFluidStack(AEFluidStack.fromFluidStack(fluid));
            }
        }
    }

    @Override
    public String getMessage() {
        final IAEFluidStack fluid = this.getFluidStack();
        if (fluid != null) {
            return fluid.getFluidStack().getLocalizedName();
        }
        return null;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    public IAEFluidStack getFluidStack() {
        return this.fluids.getFluidInSlot(this.slot);
    }

    public void setFluidStack(final IAEFluidStack stack) {
        this.fluids.setFluidInSlot(this.slot, stack);
        NetworkHandler.instance().sendToServer(new PacketFluidSlot(Collections.singletonMap(this.getId(), this.getFluidStack())));
    }
}
