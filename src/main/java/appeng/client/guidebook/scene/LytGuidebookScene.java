package appeng.client.guidebook.scene;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;

import appeng.client.guidebook.color.ColorValue;
import appeng.client.guidebook.color.SymbolicColor;
import appeng.client.guidebook.document.LytPoint;
import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.LytSize;
import appeng.client.guidebook.document.block.LytBlock;
import appeng.client.guidebook.document.block.LytBox;
import appeng.client.guidebook.document.block.LytVBox;
import appeng.client.guidebook.document.block.LytVisitor;
import appeng.client.guidebook.document.interaction.ContentTooltip;
import appeng.client.guidebook.document.interaction.GuideTooltip;
import appeng.client.guidebook.document.interaction.InteractiveElement;
import appeng.client.guidebook.document.interaction.LytWidget;
import appeng.client.guidebook.extensions.ExtensionCollection;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.RenderContext;
import appeng.client.guidebook.scene.annotation.InWorldAnnotation;
import appeng.client.guidebook.scene.annotation.InWorldBoxAnnotation;
import appeng.client.guidebook.scene.annotation.SceneAnnotation;
import appeng.client.guidebook.screen.GuideIconButton;
import appeng.client.guidebook.screen.GuideScreen;
import appeng.core.AEConfig;
import appeng.siteexport.OffScreenRenderer;

/**
 * Shows a pseudo-in-world scene within the guidebook.
 */
public class LytGuidebookScene extends LytBox {
    @Nullable
    private GuidebookScene scene;
    private boolean interactive;

    @Nullable
    private ColorValue background;

    private final ExtensionCollection extensions;

    private boolean fullWidth;

    private final LytVBox toolbar = new LytVBox();
    private final Viewport viewport = new Viewport();

    private SavedCameraSettings initialCameraSettings = new SavedCameraSettings();

    private final LytWidget hideAnnotationsButton;
    private final LytWidget zoomInButton;
    private final LytWidget zoomOutButton;
    private final LytWidget resetViewButton;

    public LytGuidebookScene(ExtensionCollection extensions) {
        this.extensions = extensions;

        setPadding(5); // Default padding

        append(viewport);

        // Build the toolbar
        hideAnnotationsButton = new LytWidget(new GuideIconButton(0, 0, GuideIconButton.Role.HIDE_ANNOTATIONS, btn -> {
            viewport.setHideAnnotations(!viewport.isHideAnnotations());
            if (viewport.isHideAnnotations()) {
                btn.setRole(GuideIconButton.Role.SHOW_ANNOTATIONS);
            } else {
                btn.setRole(GuideIconButton.Role.HIDE_ANNOTATIONS);
            }
        }));
        zoomInButton = new LytWidget(new GuideIconButton(0, 0, GuideIconButton.Role.ZOOM_IN, () -> {
            if (scene != null) {
                var currentZoom = scene.getCameraSettings().getZoom();
                currentZoom = Mth.clamp(currentZoom + 0.5f, 0.1f, 8f);
                scene.getCameraSettings().setZoom(currentZoom);
            }
        }));
        zoomOutButton = new LytWidget(new GuideIconButton(0, 0, GuideIconButton.Role.ZOOM_OUT, () -> {
            if (scene != null) {
                var currentZoom = scene.getCameraSettings().getZoom();
                currentZoom = Mth.clamp(currentZoom - 0.5f, 0.1f, 8f);
                scene.getCameraSettings().setZoom(currentZoom);
            }
        }));
        resetViewButton = new LytWidget(new GuideIconButton(0, 0, GuideIconButton.Role.RESET_VIEW, () -> {
            if (scene != null) {
                scene.getCameraSettings().restore(initialCameraSettings);
            }
        }));
    }

    @Nullable
    public GuidebookScene getScene() {
        return scene;
    }

    public void setScene(@Nullable GuidebookScene scene) {
        this.scene = scene;
        viewport.setHoveredAnnotation(null);
        if (scene != null) {
            initialCameraSettings = scene.getCameraSettings().save();
        } else {
            initialCameraSettings = new SavedCameraSettings();
        }

        updateToolbar();
    }

    private void updateToolbar() {
        toolbar.clearContent();
        if (scene == null) {
            return;
        }

        if (!scene.getInWorldAnnotations().isEmpty() || !scene.getOverlayAnnotations().isEmpty()) {
            this.toolbar.append(hideAnnotationsButton);
        }
        this.toolbar.append(zoomInButton);
        this.toolbar.append(zoomOutButton);
        this.toolbar.append(resetViewButton);
    }

    @Override
    protected LytRect computeBoxLayout(LayoutContext context, int x, int y, int availableWidth) {
        var prefSceneSize = viewport.getPreferredSize();

        prefSceneSize = new LytSize(
                prefSceneSize.width() + paddingLeft + paddingRight,
                prefSceneSize.height() + paddingTop + paddingBottom);

        // Clamp width to available width
        var sceneWidth = fullWidth ? availableWidth : Math.min(prefSceneSize.width(), availableWidth);
        var sceneHeight = prefSceneSize.height();

        // We have to layout twice to get the preferred size
        var toolbarBounds = toolbar.layout(context, x, y, 0);
        // If the space isn't enough for both, reduce the scene width
        if (sceneWidth + toolbarBounds.width() > availableWidth) {
            sceneWidth = availableWidth - toolbarBounds.width();
        }
        toolbarBounds = toolbar.layout(context, x + sceneWidth, y, availableWidth - sceneWidth);

        // Enforce a minimum width
        if (sceneWidth < 10) {
            sceneWidth = 10;
        }

        var viewportBounds = new LytRect(x, y, sceneWidth, sceneHeight);
        viewport.setBounds(viewportBounds);

        return LytRect.union(
                viewportBounds,
                toolbarBounds);
    }

    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
        if (interactive) {
            append(toolbar);
        } else {
            removeChild(toolbar);
        }
    }

    public boolean isInteractive() {
        return interactive;
    }

    public void setBackground(ColorValue background) {
        this.background = background;
    }

    public boolean isFullWidth() {
        return fullWidth;
    }

    public void setFullWidth(boolean fullWidth) {
        this.fullWidth = fullWidth;
    }

    public LytSize getPreferredSize() {
        return viewport.getPreferredSize();
    }

    public byte[] exportAsPng(float scale, boolean hideAnnotations) {
        if (scene == null) {
            return null;
        }

        var prefSize = viewport.getPreferredSize();
        if (prefSize.width() <= 0 || prefSize.height() <= 0) {
            return null;
        }

        // We only scale the viewport, not scaling the view matrix means the scene will still fill it
        var width = (int) Math.max(1, prefSize.width() * scale);
        var height = (int) Math.max(1, prefSize.height() * scale);

        try (var osr = new OffScreenRenderer(width, height)) {
            return osr.captureAsPng(() -> {
                var renderer = GuidebookLevelRenderer.getInstance();
                scene.getCameraSettings().setViewportSize(prefSize);
                var annotations = hideAnnotations ? Collections.<InWorldAnnotation>emptyList()
                        : scene.getInWorldAnnotations();
                renderer.render(scene.getLevel(), scene.getCameraSettings(), annotations);
            });
        }
    }

    @Override
    protected LytVisitor.Result visitChildren(LytVisitor visitor, boolean includeOutOfTreeContent) {
        var result = super.visitChildren(visitor, includeOutOfTreeContent);
        if (result == LytVisitor.Result.STOP) {
            return result;
        }

        // Visit content hidden in tooltips, if requested
        if (includeOutOfTreeContent && scene != null) {
            if (visitAnnotations(scene.getInWorldAnnotations(), visitor) == LytVisitor.Result.STOP) {
                return LytVisitor.Result.STOP;
            }
            if (visitAnnotations(scene.getOverlayAnnotations(), visitor) == LytVisitor.Result.STOP) {
                return LytVisitor.Result.STOP;
            }
        }

        return result;
    }

    /**
     * Visits the out-of-tree content within the given annotations.
     */
    private LytVisitor.Result visitAnnotations(Collection<? extends SceneAnnotation> annotations, LytVisitor visitor) {
        for (var annotation : annotations) {
            if (annotation.getTooltip() instanceof ContentTooltip contentTooltip) {
                if (contentTooltip.getContent().visit(visitor, true) == LytVisitor.Result.STOP) {
                    return LytVisitor.Result.STOP;
                }
            }
        }

        return LytVisitor.Result.CONTINUE;
    }

    class Viewport extends LytBlock implements InteractiveElement {
        @Nullable
        private SceneAnnotation hoveredAnnotation;

        private boolean hideAnnotations;

        // Indicates that hoveredAnnotation should be removed from the scene when it is no longer the hovered annotation
        private boolean transientHoveredAnnotation;

        // State for camera control
        private int buttonDown;
        private Vector2i pointDown;
        private float initialRotY;
        private float initialRotX;
        private float initialTransX;
        private float initialTransY;

        @Override
        protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
            return bounds;
        }

        @Override
        protected void onLayoutMoved(int deltaX, int deltaY) {
        }

        @Override
        public void renderBatch(RenderContext context, MultiBufferSource buffers) {
        }

        @Override
        public void render(RenderContext context) {
            if (background != null) {
                context.fillRect(bounds, background);
            }

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

            Collection<InWorldAnnotation> inWorldAnnotations;
            if (hideAnnotations) {
                // We still show transient annotations even if static annotations are hidden
                if (transientHoveredAnnotation
                        && hoveredAnnotation instanceof InWorldAnnotation hoveredInWorldAnnotation) {
                    inWorldAnnotations = Collections.singletonList(hoveredInWorldAnnotation);
                } else {
                    inWorldAnnotations = Collections.emptyList();
                }
            } else {
                inWorldAnnotations = scene.getInWorldAnnotations();
            }
            renderer.render(scene.getLevel(), scene.getCameraSettings(), inWorldAnnotations);

            renderDebugCrosshairs();

            RenderSystem.viewport(0, 0, window.getWidth(), window.getHeight());

            context.pushScissor(bounds);

            if (!hideAnnotations) {
                renderOverlayAnnotations(scene, context);
            }

            context.popScissor();
        }

        /**
         * Render one in 2D space at 0,0. And render one in 3D space at 0,0,0.
         */
        private void renderDebugCrosshairs() {
            if (!AEConfig.instance().isShowDebugGuiOverlays()) {
                return;
            }

            RenderSystem.renderCrosshair(16);

            RenderSystem.backupProjectionMatrix();
            RenderSystem.setProjectionMatrix(scene.getCameraSettings().getProjectionMatrix(),
                    VertexSorting.ORTHOGRAPHIC_Z);
            var modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushMatrix();
            modelViewStack.identity();
            modelViewStack.mul(scene.getCameraSettings().getViewMatrix());
            RenderSystem.applyModelViewMatrix();

            RenderSystem.renderCrosshair(2);
            modelViewStack.popMatrix();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.restoreProjectionMatrix();
        }

        @Override
        public Optional<GuideTooltip> getTooltip(float x, float y) {
            if (scene == null || bounds.isEmpty()) {
                setHoveredAnnotation(null);
                return Optional.empty();
            }

            var docPoint = new LytPoint(x, y);

            // This is tricky since a transient annotation should *NOT* be considered for hit-testing
            SceneAnnotation annotation = null;
            if (!hideAnnotations) {
                if (hoveredAnnotation != null && transientHoveredAnnotation) {
                    scene.removeAnnotation(hoveredAnnotation);
                    annotation = scene.pickAnnotation(docPoint, bounds, SceneAnnotation::hasTooltip);
                    scene.addAnnotation(hoveredAnnotation);
                } else {
                    annotation = scene.pickAnnotation(docPoint, bounds, SceneAnnotation::hasTooltip);
                }

                // Prioritize picking annotation boxes over blocks
                if (annotation != null && annotation.getTooltip() != null) {
                    setHoveredAnnotation(annotation);
                    return Optional.of(annotation.getTooltip());
                }
            }

            var hitResult = scene.pickBlock(docPoint, bounds);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                var blockState = scene.getLevel().getBlockState(hitResult.getBlockPos());

                for (var strategy : extensions.get(ImplicitAnnotationStrategy.EXTENSION_POINT)) {
                    annotation = strategy.getAnnotation(scene.getLevel(), blockState, hitResult);
                    if (annotation != null) {
                        break;
                    }
                }

                if (annotation == null) {
                    annotation = InWorldBoxAnnotation.forBlock(hitResult.getBlockPos(),
                            SymbolicColor.IN_WORLD_BLOCK_HIGHLIGHT);
                    annotation.setTooltipContent(Component.translatable(blockState.getBlock().getDescriptionId()));
                }
                setTransientHoveredAnnotation(annotation);

                if (annotation.getTooltip() != null) {
                    return Optional.of(annotation.getTooltip());
                } else {
                    return Optional.empty();
                }
            }

            setHoveredAnnotation(null);
            return Optional.empty();
        }

        @Override
        public boolean mouseClicked(GuideScreen screen, int x, int y, int button) {
            if (interactive) {
                if (button == 0 || button == 1) {
                    var cameraSettings = scene.getCameraSettings();
                    buttonDown = button;
                    pointDown = new Vector2i(x, y);
                    initialRotX = cameraSettings.getRotationX();
                    initialRotY = cameraSettings.getRotationY();
                    initialTransX = cameraSettings.getOffsetX();
                    initialTransY = cameraSettings.getOffsetY();
                    screen.captureMouse(this);
                }
            }
            return true;
        }

        @Override
        public boolean mouseReleased(GuideScreen screen, int x, int y, int button) {
            pointDown = null;
            return true;
        }

        @Override
        public void mouseCaptureLost() {
            pointDown = null;
        }

        @Override
        public boolean mouseMoved(GuideScreen screen, int x, int y) {
            if (interactive && pointDown != null) {
                var dx = x - pointDown.x;
                var dy = y - pointDown.y;
                if (buttonDown == 0) {
                    scene.getCameraSettings().setRotationY(initialRotY + dx);
                    scene.getCameraSettings().setRotationX(initialRotX + dy);
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

        public boolean isHideAnnotations() {
            return hideAnnotations;
        }

        public void setHideAnnotations(boolean hideAnnotations) {
            this.hideAnnotations = hideAnnotations;
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
                        if (scene != null) {
                            scene.removeAnnotation(hoveredAnnotation);
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

        public void setBounds(LytRect bounds) {
            this.bounds = bounds;
            if (scene != null) {
                scene.getCameraSettings().setViewportSize(bounds.size());
            }
        }

        public LytSize getPreferredSize() {
            if (scene == null) {
                return LytSize.empty();
            }

            // Compute bounds using the *initial* camera settings
            var current = scene.getCameraSettings().save();
            scene.getCameraSettings().restore(initialCameraSettings);
            var screenBounds = scene.getScreenBounds();
            scene.getCameraSettings().restore(current);

            var width = (int) Math.ceil(Math.abs(screenBounds.z - screenBounds.x));
            var height = (int) Math.ceil(Math.abs(screenBounds.w - screenBounds.y));
            return new LytSize(width, height);
        }

        private void renderOverlayAnnotations(GuidebookScene scene, RenderContext context) {
            for (var annotation : scene.getOverlayAnnotations()) {
                // Determine where it would be on screen
                annotation.render(scene, context, bounds);
            }
        }
    }
}
