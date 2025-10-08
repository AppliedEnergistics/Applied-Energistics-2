package appeng.client.renderer.keytypes;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import appeng.api.stacks.AEItemKey;
import appeng.client.api.AEKeyRenderer;

public class ItemKeyRenderer implements AEKeyRenderer<AEItemKey, ItemStackRenderState> {
    private final ItemModelResolver itemModelResolver;

    public ItemKeyRenderer() {
        itemModelResolver = Minecraft.getInstance().getItemModelResolver();
    }

    @Override
    public void drawInGui(Minecraft minecraft, GuiGraphics guiGraphics, int x, int y, AEItemKey stack) {
        var poseStack = guiGraphics.pose();
        poseStack.pushMatrix();

        var displayStack = stack.getReadOnlyStack();
        guiGraphics.renderItem(displayStack, x, y);
        guiGraphics.renderItemDecorations(minecraft.font, displayStack, x, y, "");

        poseStack.popMatrix();
    }

    @Override
    public Class<ItemStackRenderState> stateClass() {
        return ItemStackRenderState.class;
    }

    @Override
    public ItemStackRenderState createState() {
        return new ItemStackRenderState();
    }

    @Override
    public void extract(ItemStackRenderState state, AEItemKey what, @Nullable Level level, int seed) {
        itemModelResolver.updateForTopItem(
                state,
                what.getReadOnlyStack(),
                ItemDisplayContext.GUI,
                level,
                new ItemOwner() {
                    @Override
                    public Level level() {
                        return level;
                    }

                    @Override
                    public Vec3 position() {
                        return Vec3.ZERO;
                    }

                    @Override
                    public float getVisualRotationYInDegrees() {
                        return 0;
                    }
                },
                seed);
    }

    @Override
    public void submit(PoseStack poseStack, ItemStackRenderState state, SubmitNodeCollector nodes, int lightCoords) {
        poseStack.pushPose();
        // Push it out of the block face a bit to avoid z-fighting
        poseStack.translate(0, 0, 0.01f);
        // The Z-scaling by 0.001 causes the model to be visually "flattened"
        // This cannot replace a proper projection, but it's cheap and gives the desired effect.
        // We don't scale the normal matrix to avoid lighting issues.
        poseStack.mulPose(new Matrix4f().scale(1, 1, 0.001f));
        // Rotate the normal matrix a little for nicer lighting.
        poseStack.last().normal().rotateX(Mth.DEG_TO_RAD * -45f);

        state.submit(poseStack, nodes, lightCoords, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }

    @Override
    public List<Component> getTooltip(AEItemKey stack) {
        return stack.getReadOnlyStack().getTooltipLines(
                Item.TooltipContext.of(Minecraft.getInstance().level),
                Minecraft.getInstance().player,
                Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED
                        : TooltipFlag.Default.NORMAL);
    }
}
