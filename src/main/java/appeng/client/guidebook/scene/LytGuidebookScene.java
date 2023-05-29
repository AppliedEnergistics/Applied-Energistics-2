package appeng.client.guidebook.scene;

import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.HitResult;

import appeng.client.guidebook.color.ColorValue;
import appeng.client.guidebook.color.SymbolicColor;
import appeng.client.guidebook.document.LytPoint;
import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.block.LytBlock;
import appeng.client.guidebook.document.interaction.ContentTooltip;
import appeng.client.guidebook.document.interaction.GuideTooltip;
import appeng.client.guidebook.document.interaction.InteractiveElement;
import appeng.client.guidebook.document.interaction.TextTooltip;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.RenderContext;
import appeng.client.guidebook.scene.annotation.InWorldBoxAnnotation;
import appeng.client.guidebook.scene.annotation.SceneAnnotation;
import appeng.client.guidebook.screen.GuideScreen;

/**
 * Shows a pseudo-in-world scene within the guidebook.
 */
public class LytGuidebookScene extends LytBlock implements InteractiveElement {
    @Nullable
    private GuidebookScene scene;
    private boolean interactive;
    @Nullable
    private SceneAnnotation hoveredAnnotation;
    // Indicates that hoveredAnnotation should be removed from the scene when it is no longer the hovered annotation
    private boolean transientHoveredAnnotation;

    public LytGuidebookScene() {
    }

    @Nullable
    public GuidebookScene getScene() {
        return scene;
    }

    public void setScene(@Nullable GuidebookScene scene) {
        this.scene = scene;
        setHoveredAnnotation(null);
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
                screenBounds);

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
                (int) (viewport.height() * window.getGuiScale()));

        var renderer = GuidebookLevelRenderer.getInstance();

        renderer.render(scene.getLevel(), scene.getCameraSettings(), scene.getInWorldAnnotations());

        RenderSystem.viewport(0, 0, window.getWidth(), window.getHeight());

        context.pushScissor(bounds);

        renderOverlayAnnotations(scene, context);

        context.popScissor();
    }

    private void renderOverlayAnnotations(GuidebookScene scene, RenderContext context) {
        for (var annotation : scene.getOverlayAnnotations()) {
            // Determine where it would be on screen
            annotation.render(scene, context, bounds);
        }
    }

    @Override
    public Optional<GuideTooltip> getTooltip(float x, float y) {
        if (!interactive || scene == null || bounds.isEmpty()) {
            setHoveredAnnotation(null);
            return Optional.empty();
        }

        var docPoint = new LytPoint(x, y);

        // This is tricky since a transient annotation should *NOT* be considered for hit-testing
        SceneAnnotation annotation;
        if (hoveredAnnotation != null && transientHoveredAnnotation) {
            scene.removeAnnotation(hoveredAnnotation);
            annotation = scene.pickAnnotation(docPoint, bounds);
            scene.addAnnotation(hoveredAnnotation);
        } else {
            annotation = scene.pickAnnotation(docPoint, bounds);
        }

        // Prioritize picking annotation boxes over blocks
        if (annotation != null && annotation.getContent() != null) {
            setHoveredAnnotation(annotation);
            return Optional.of(new ContentTooltip(annotation.getContent()));
        }

        var hitResult = scene.pickBlock(docPoint, bounds);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            var blockState = scene.getLevel().getBlockState(hitResult.getBlockPos());

            annotation = InWorldBoxAnnotation.forBlock(hitResult.getBlockPos(),
                    (ColorValue) SymbolicColor.IN_WORLD_BLOCK_HIGHLIGHT);
            setTransientHoveredAnnotation(annotation);

            var text = Component.translatable(blockState.getBlock().getDescriptionId());
            return Optional.of(
                    new TextTooltip(text));
        }

        setHoveredAnnotation(null);
        return Optional.empty();
    }

    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    public boolean isInteractive() {
        return interactive;
    }

    private int buttonDown;
    private Vector2i pointDown;
    private float initialRotX;
    private float initialRotY;
    private float initialTransX;
    private float initialTransY;

    @Override
    public boolean mouseClicked(GuideScreen screen, int x, int y, int button) {
        if (button == 0 || button == 1) {
            buttonDown = button;
            pointDown = new Vector2i(x, y);
            initialRotX = scene.getCameraSettings().getRotationX();
            initialRotY = scene.getCameraSettings().getRotationY();
            initialTransX = scene.getCameraSettings().getOffsetX();
            initialTransY = scene.getCameraSettings().getOffsetY();
        }
        return true;
    }

    @Override
    public boolean mouseReleased(GuideScreen screen, int x, int y, int button) {
        pointDown = null;
        return true;
    }

    @Override
    public boolean mouseMoved(GuideScreen screen, int x, int y) {
        if (pointDown != null) {
            var dx = x - pointDown.x;
            var dy = y - pointDown.y;
            if (buttonDown == 0) {
                scene.getCameraSettings().setRotationX(initialRotX + dy);
                scene.getCameraSettings().setRotationY(initialRotY + dx);
            } else if (buttonDown == 1) {
                scene.getCameraSettings().setOffsetX(initialTransX + dx);
                scene.getCameraSettings().setOffsetY(initialTransY - dy);
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onMouseLeave() {
        setHoveredAnnotation(null);
    }

    // Sets an annotation as the hovered annotation and adds it to the scene
    // Will remove the annotation once another annotation becomes hovered
    private void setTransientHoveredAnnotation(@Nullable SceneAnnotation annotation) {
        setHoveredAnnotation(null);
        setHoveredAnnotation(annotation);
        if (scene != null) {
            scene.addAnnotation(annotation);
        }
        transientHoveredAnnotation = true;
    }

    private void setHoveredAnnotation(@Nullable SceneAnnotation annotation) {
        if (this.hoveredAnnotation != annotation) {
            if (this.hoveredAnnotation != null) {
                this.hoveredAnnotation.setHovered(false);
                if (transientHoveredAnnotation) {
                    if (this.scene != null) {
                        this.scene.removeAnnotation(hoveredAnnotation);
                    }
                    transientHoveredAnnotation = false;
                }
            }
            this.hoveredAnnotation = annotation;
            if (this.hoveredAnnotation != null) {
                this.hoveredAnnotation.setHovered(true);
            }
        }
    }
}
