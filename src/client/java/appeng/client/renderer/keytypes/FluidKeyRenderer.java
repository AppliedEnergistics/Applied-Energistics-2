package appeng.client.renderer.keytypes;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

import appeng.api.stacks.AEFluidKey;
import appeng.client.api.AEKeyRenderer;
import appeng.client.gui.style.FluidBlitter;
import appeng.util.Platform;

public class FluidKeyRenderer implements AEKeyRenderer<AEFluidKey, FluidKeyRenderer.RenderState> {
    @Override
    public void drawInGui(Minecraft minecraft, GuiGraphics guiGraphics, int x, int y, AEFluidKey what) {
        FluidBlitter.create(what)
                .dest(x, y, 16, 16)
                .blit(guiGraphics);
    }

    @Override
    public Class<RenderState> stateClass() {
        return RenderState.class;
    }

    @Override
    public RenderState createState() {
        return new RenderState();
    }

    @Override
    public void extract(RenderState state, AEFluidKey what, @Nullable Level level, int seed) {

        var fluidStack = what.toStack(1);
        var renderProps = IClientFluidTypeExtensions.of(what.getFluid());
        var texture = renderProps.getStillTexture(fluidStack);
        var color = renderProps.getTintColor(fluidStack);
        var sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS)
                .getSprite(texture);

    }

    @Override
    public void submit(PoseStack poseStack, RenderState state, SubmitNodeCollector nodes, int lightCoords) {

        poseStack.pushPose();
        // Push it out of the block face a bit to avoid z-fighting
        poseStack.translate(0, 0, 0.01f);

// TODO 1.21.9        // In comparison to items, make it _slightly_ smaller because item icons
// TODO 1.21.9        // usually don't extend to the full size.
// TODO 1.21.9        scale -= 0.05f;
// TODO 1.21.9
// TODO 1.21.9        // y is flipped here
// TODO 1.21.9        var x0 = -scale / 2;
// TODO 1.21.9        var y0 = scale / 2;
// TODO 1.21.9        var x1 = scale / 2;
// TODO 1.21.9        var y1 = -scale / 2;

        nodes.submitCustomGeometry(new PoseStack(), RenderType.solid(), (pose, buffer) -> {
            // TODO 1.21.9 buffer.addVertex(pose, x0, y1, 0)
            // TODO 1.21.9 .setColor(color)
            // TODO 1.21.9 .setUv(sprite.getU0(), sprite.getV1())
            // TODO 1.21.9 .setOverlay(OverlayTexture.NO_OVERLAY)
            // TODO 1.21.9 .setLight(combinedLight)
            // TODO 1.21.9 .setNormal(0, 0, 1);
            // TODO 1.21.9 buffer.addVertex(pose, x1, y1, 0)
            // TODO 1.21.9 .setColor(color)
            // TODO 1.21.9 .setUv(sprite.getU1(), sprite.getV1())
            // TODO 1.21.9 .setOverlay(OverlayTexture.NO_OVERLAY)
            // TODO 1.21.9 .setLight(combinedLight)
            // TODO 1.21.9 .setNormal(0, 0, 1);
            // TODO 1.21.9 buffer.addVertex(pose, x1, y0, 0)
            // TODO 1.21.9 .setColor(color)
            // TODO 1.21.9 .setUv(sprite.getU1(), sprite.getV0())
            // TODO 1.21.9 .setOverlay(OverlayTexture.NO_OVERLAY)
            // TODO 1.21.9 .setLight(combinedLight)
            // TODO 1.21.9 .setNormal(0, 0, 1);
            // TODO 1.21.9 buffer.addVertex(pose, x0, y0, 0)
            // TODO 1.21.9 .setColor(color)
            // TODO 1.21.9 .setUv(sprite.getU0(), sprite.getV0())
            // TODO 1.21.9 .setOverlay(OverlayTexture.NO_OVERLAY)
            // TODO 1.21.9 .setLight(combinedLight)
            // TODO 1.21.9 .setNormal(0, 0, 1);
        });

        poseStack.popPose();
    }

    @Override
    public List<Component> getTooltip(AEFluidKey stack) {
        var tooltip = new ArrayList<Component>();
        tooltip.add(stack.toStack(1).getHoverName());

        // Heuristic: If the last line doesn't include the modname, add it ourselves
        var modName = Platform.formatModName(stack.getModId());
        if (!tooltip.getLast().getString().equals(modName)) {
            tooltip.add(Component.literal(modName));
        }

        return tooltip;
    }

    public record RenderState() {
    }
}
