package appeng.client.guidebook.scene;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.block.LytBlock;
import appeng.client.guidebook.document.interaction.GuideTooltip;
import appeng.client.guidebook.document.interaction.InteractiveElement;
import appeng.client.guidebook.document.interaction.TextTooltip;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Shows a pseudo-in-world scene within the guidebook.
 */
public class LytGuidebookScene extends LytBlock implements InteractiveElement {
    @Nullable
    private GuidebookScene scene;

    public LytGuidebookScene() {
    }

    @Nullable
    public GuidebookScene getScene() {
        return scene;
    }

    public void setScene(@Nullable GuidebookScene scene) {
        this.scene = scene;
    }

    @Override
    protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {

        if (scene == null) {
            return LytRect.empty();
        }

        var screenBounds = scene.getScreenBounds();

        var width = (int) Math.ceil(Math.abs(screenBounds.z - screenBounds.x));
        var height = (int) Math.ceil(Math.abs(screenBounds.w - screenBounds.y));

        scene.getCameraSettings().setViewport(
                screenBounds
        );

        return new LytRect(x, y, width, height);
    }

    @Override
    public void renderBatch(RenderContext context, MultiBufferSource buffers) {
    }

    @Override
    public void render(RenderContext context) {
        if (scene == null) {
            return;
        }

        var window = Minecraft.getInstance().getWindow();

        // transform our document viewport into physical screen coordinates
        var viewport = bounds.transform(context.poseStack().last().pose());
        RenderSystem.viewport(
                (int) (viewport.x() * window.getGuiScale()),
                (int) (window.getHeight() - viewport.bottom() * window.getGuiScale()),
                (int) (viewport.width() * window.getGuiScale()),
                (int) (viewport.height() * window.getGuiScale())
        );

        var renderer = GuidebookLevelRenderer.getInstance();

        renderer.render(scene.getLevel(), scene.getCameraSettings(), scene.getHighlights());

        RenderSystem.viewport(0, 0, window.getWidth(), window.getHeight());
    }

    @Override
    public Optional<GuideTooltip> getTooltip(float x, float y) {
        if (scene == null || bounds.isEmpty()) {
            return Optional.empty();
        }

        var localX = (x - bounds.x()) / bounds.width() * 2 - 1;
        var localY = -((y - bounds.y()) / bounds.height() * 2 - 1);

        var hitResult = scene.pickBlock(localX, localY);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            var blockState = scene.getLevel().getBlockState(hitResult.getBlockPos());

            scene.addHighlight(new BlockHighlight(hitResult.getBlockPos(), 0.6f, 0.6f, 0.6f, .8f));

            var text = Component.translatable(blockState.getBlock().getDescriptionId());
            return Optional.of(
                    new TextTooltip(text)
            );
        } else {
            scene.clearHighlights();
        }

        return Optional.of(new TextTooltip(String.format("%f %f", localX, localY)));
    }
}
