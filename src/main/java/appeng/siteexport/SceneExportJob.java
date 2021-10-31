package appeng.siteexport;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.RenderShape;

public class SceneExportJob {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Path assetFolder;
    private final List<Scene> scenes;
    private final FabricClientCommandSource source;
    private final Minecraft client;
    private final ClientLevel clientLevel;
    private final ServerLevel serverLevel;
    private int currentScene = -1;
    private int waitTicks = 0;

    enum SceneRenderingState {
        BEFORE_SERVER_SETUP,
        AFTER_SERVER_SETUP,
        RENDER
    }

    private SceneRenderingState state;

    public SceneExportJob(List<Scene> scenes, FabricClientCommandSource source, Path assetFolder) {
        this.scenes = scenes;
        this.source = source;
        this.assetFolder = assetFolder;

        client = Minecraft.getInstance();
        clientLevel = client.level;
        serverLevel = client.getSingleplayerServer().getLevel(clientLevel.dimension());
    }

    public void tick() {
        if (state == null) {
            currentScene++;
            state = SceneRenderingState.BEFORE_SERVER_SETUP;
        }
        if (isAtEnd()) {
            return;
        }

        if (state == SceneRenderingState.BEFORE_SERVER_SETUP) {
            var scene = scenes.get(currentScene);
            scene.clearArea(serverLevel);
            scene.setUp(serverLevel);
            state = SceneRenderingState.AFTER_SERVER_SETUP;
            waitTicks = scene.waitTicks;
        } else if (state == SceneRenderingState.AFTER_SERVER_SETUP) {
            if (--waitTicks > 0) {
                return;
            }
            state = SceneRenderingState.RENDER;
        }
    }

    public void render() throws Exception {
        if (state == SceneRenderingState.RENDER) {
            var scene = scenes.get(currentScene);

            Path sceneOutput = assetFolder.resolve(scene.filename);
            Files.createDirectories(sceneOutput.getParent());

            renderScene(sceneOutput, scene);

            currentScene++;
            state = SceneRenderingState.BEFORE_SERVER_SETUP;
        }
    }

    public boolean isAtEnd() {
        return currentScene >= scenes.size();
    }

    private void renderScene(Path outputPath, Scene scene) throws Exception {
        var blockRenderer = client.getBlockRenderer();
        var rand = new Random(0);

        // Set up the world
        scene.clearLighting(clientLevel);

        var beRenderer = client.getBlockEntityRenderDispatcher();

        SceneRenderSettings settings = scene.settings;
        try (var renderer = new OffScreenRenderer(settings.width, settings.height)) {
            if (settings.ortographic) {
                renderer.setupOrtographicRendering();
            } else {
                renderer.setupPerspectiveRendering(
                        3.3f /* zoom */,
                        65 /* fov */,
                        new Vector3f(2f, 2.5f, -3f),
                        new Vector3f(0.5f, 0.5f, 0.5f));
            }

            var random = new Random(12345);

            var min = scene.getMin();

            if (!settings.ortographic) {
                var cameraEntity = new Zombie(clientLevel);
                cameraEntity.setPos(min.getX(), min.getY(), min.getZ());
                beRenderer.camera.setup(clientLevel, cameraEntity, false, false, 0);
            }

            try {
                renderer.captureAsPng(() -> {

                    var worldMat = new PoseStack();
                    worldMat.translate(
                            -scene.centerOn.x(),
                            -scene.centerOn.y(),
                            -scene.centerOn.z());

                    RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

                    var buffers = client.renderBuffers().bufferSource();

                    for (var rt : new RenderType[] {
                            RenderType.solid(),
                            RenderType.cutout(),
                            RenderType.cutoutMipped()
                    }) {
                        var buffer = buffers.getBuffer(rt);

                        for (var pos : scene.blocks.keySet()) {
                            var state = clientLevel.getBlockState(pos);
                            if (ItemBlockRenderTypes.getChunkRenderType(state) == rt) {
                                worldMat.pushPose();
                                worldMat.translate(pos.getX(), pos.getY(), pos.getZ());
                                state.getBlock().animateTick(state, clientLevel, pos, random);
                                if (state.getRenderShape() != RenderShape.INVISIBLE) {
                                    if (state.getRenderShape() == RenderShape.MODEL) {
                                        blockRenderer.renderBatched(state, pos, clientLevel, worldMat, buffer, false,
                                                rand);
                                    }
                                    var be = clientLevel.getBlockEntity(pos);
                                    if (be != null) {
                                        beRenderer.render(be, 0, worldMat, buffers);
                                    }
                                }
                                worldMat.popPose();
                            }
                        }
                        buffers.endBatch();
                    }
                }, outputPath);
            } finally {
                client.setCameraEntity(client.player);
            }
        }
    }

    public void sendFeedback(Component text) {
        source.sendFeedback(text);
    }

    public void sendError(Component text) {
        source.sendError(text);
    }
}
