package appeng.init.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.api.client.AEStackRendering;
import appeng.api.client.AmountFormat;
import appeng.api.client.IAEStackRenderHandler;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.client.gui.me.common.StackSizeRenderer;
import appeng.client.gui.me.fluids.FluidStackSizeRenderer;
import appeng.client.gui.style.FluidBlitter;
import appeng.util.Platform;

public class InitStackRenderHandlers {
    private InitStackRenderHandlers() {
    }

    public static void init() {
        var itemSSRenderer = new StackSizeRenderer();
        AEStackRendering.register(StorageChannels.items(), new IAEStackRenderHandler<>() {
            @Override
            public void drawRepresentation(AbstractContainerScreen<?> screen, PoseStack poseStack, int x, int y,
                    IAEItemStack stack) {
                ItemStack displayStack = stack.asItemStackRepresentation();
                // The item renderer uses this global stack, so we have to apply the current transform to it.
                var globalStack = RenderSystem.getModelViewStack();
                globalStack.pushPose();
                globalStack.mulPoseMatrix(poseStack.last().pose());
                Minecraft.getInstance().getItemRenderer().renderGuiItem(displayStack, x, y);
                globalStack.popPose();
                // Ensure the global state is correctly reset.
                RenderSystem.applyModelViewMatrix();
            }

            @Override
            public Component getDisplayName(IAEItemStack stack) {
                return stack.getDefinition().getHoverName();
            }

            @Override
            public String formatAmount(long amount, AmountFormat format) {
                return switch (format) {
                    case FULL -> String.valueOf(amount);
                    case PREVIEW_REGULAR -> itemSSRenderer.getToBeRenderedStackSize(amount, false);
                    case PREVIEW_LARGE_FONT -> itemSSRenderer.getToBeRenderedStackSize(amount, true);
                };
            }

            @Override
            public String getModid(IAEItemStack stack) {
                return Platform.getModId(stack);
            }
        });
        var fluidSSRenderer = new FluidStackSizeRenderer();
        AEStackRendering.register(StorageChannels.fluids(), new IAEStackRenderHandler<>() {
            @Override
            public void drawRepresentation(AbstractContainerScreen<?> screen, PoseStack poseStack, int x, int y,
                    IAEFluidStack stack) {
                FluidBlitter.create(IAEStack.copy(stack, 1).getFluidStack())
                        .dest(x, y, 16, 16)
                        .blit(poseStack, 0);
            }

            @Override
            public Component getDisplayName(IAEFluidStack stack) {
                return IAEStack.copy(stack, 1).getFluidStack().getDisplayName();
            }

            @Override
            public String formatAmount(long amount, AmountFormat format) {
                return switch (format) {
                    case FULL -> Platform.formatFluidAmount(amount);
                    case PREVIEW_REGULAR -> fluidSSRenderer.getToBeRenderedStackSize(amount, false);
                    case PREVIEW_LARGE_FONT -> fluidSSRenderer.getToBeRenderedStackSize(amount, true);
                };
            }

            @Override
            public String getModid(IAEFluidStack stack) {
                return Platform.getModId(stack);
            }
        });
    }
}
