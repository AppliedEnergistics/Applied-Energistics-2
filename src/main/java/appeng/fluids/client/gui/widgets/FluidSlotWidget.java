package appeng.fluids.client.gui.widgets;

import java.util.Collections;
import java.util.Optional;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
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
    public void drawContent(MatrixStack matrixStack, final Minecraft mc, final int mouseX, final int mouseY,
            final float partialTicks) {
        final IAEFluidStack fs = this.getFluidStack();
        if (fs != null) {
            RenderSystem.disableBlend();
            final Fluid fluid = fs.getFluid();
            final FluidAttributes attributes = fluid.getAttributes();
            mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            final TextureAtlasSprite sprite = mc.getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE)
                    .apply(attributes.getStillTexture(fs.getFluidStack()));

            // Set color for dynamic fluids
            // Convert int color to RGB
            final float red = (attributes.getColor() >> 16 & 255) / 255.0F;
            final float green = (attributes.getColor() >> 8 & 255) / 255.0F;
            final float blue = (attributes.getColor() & 255) / 255.0F;
            RenderSystem.color3f(red, green, blue);

            blit(matrixStack, getTooltipAreaX(), getTooltipAreaY(), this.getBlitOffset(), getTooltipAreaWidth(),
                    getTooltipAreaHeight(), sprite);
        }
    }

    @Override
    public boolean canClick(final PlayerEntity player) {
        final ItemStack mouseStack = player.inventory.getItemStack();
        return mouseStack.isEmpty()
                || mouseStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent();
    }

    @Override
    public void slotClicked(final ItemStack clickStack, int mouseButton) {
        if (clickStack.isEmpty() || mouseButton == 1) {
            this.setFluidStack(null);
        } else if (mouseButton == 0) {
            final Optional<FluidStack> fluidOpt = FluidUtil.getFluidContained(clickStack);
            fluidOpt.ifPresent(fluid -> {
                this.setFluidStack(AEFluidStack.fromFluidStack(fluid));
            });
        }
    }

    @Override
    public ITextComponent getTooltipMessage() {
        final IAEFluidStack fluid = this.getFluidStack();
        if (fluid != null) {
            return new TranslationTextComponent(fluid.getFluidStack().getTranslationKey());
        }
        return StringTextComponent.EMPTY;
    }

    @Override
    public boolean isTooltipAreaVisible() {
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
