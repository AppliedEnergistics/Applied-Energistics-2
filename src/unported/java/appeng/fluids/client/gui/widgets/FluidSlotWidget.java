package appeng.fluids.client.gui.widgets;

import java.util.Collections;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.widgets.CustomSlotWidget;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.FluidSlotPacket;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.IAEFluidTank;

public class FluidSlotWidget extends CustomSlotWidget {
    private final IAEFluidTank fluids;
    private final int slot;

    public FluidSlotWidget(final IAEFluidTank fluids, final int slot, final int id, final int x, final int y) {
        super(id, x, y);
        this.fluids = fluids;
        this.slot = slot;
    }

    @Override
    public void drawContent(final MinecraftClient mc, final int mouseX, final int mouseY, final float partialTicks) {
        final IAEFluidStack fs = this.getFluidStack();
        if (fs != null) {
            RenderSystem.disableBlend();
            final Fluid fluid = fs.getFluid();
            final FluidAttributes attributes = fluid.getAttributes();
            mc.getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
            final Sprite sprite = mc.getAtlasSpriteGetter(SpriteAtlasTexture.BLOCK_ATLAS_TEX)
                    .apply(attributes.getStillTexture(fs.getFluidStack()));

            // Set color for dynamic fluids
            // Convert int color to RGB
            final float red = (attributes.getColor() >> 16 & 255) / 255.0F;
            final float green = (attributes.getColor() >> 8 & 255) / 255.0F;
            final float blue = (attributes.getColor() & 255) / 255.0F;
            RenderSystem.color3f(red, green, blue);

            drawTexture(matrices, xPos(), yPos(), this.getZOffset(), getWidth(), getHeight(), sprite);
        }
    }

    @Override
    public boolean canClick(final PlayerEntity player) {
        final ItemStack mouseStack = player.inventory.getCursorStack();
        return mouseStack.isEmpty()
                || mouseStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent();
    }

    @Override
    public void slotClicked(final ItemStack clickStack, int mouseButton) {
        if (clickStack.isEmpty() || mouseButton == 1) {
            this.setFluidStack(null);
        } else if (mouseButton == 0) {
            final LazyOptional<FluidVolume> fluidOpt = FluidUtil.getFluidContained(clickStack);
            fluidOpt.ifPresent(fluid -> {
                this.setFluidStack(AEFluidStack.fromFluidVolume(fluid));
            });
        }
    }

    @Override
    public Text getMessage() {
        final IAEFluidStack fluid = this.getFluidStack();
        if (fluid != null) {
            return I18n.format(fluid.getFluidStack().getTranslationKey());
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
        NetworkHandler.instance()
                .sendToServer(new FluidSlotPacket(Collections.singletonMap(this.getId(), this.getFluidStack())));
    }
}
