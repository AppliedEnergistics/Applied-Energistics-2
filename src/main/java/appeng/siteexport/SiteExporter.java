package appeng.siteexport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mojang.serialization.Lifecycle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

import appeng.core.AppEng;

/**
 * Exports a data package for use by the website.
 */
@OnlyIn(Dist.CLIENT)
public final class SiteExporter {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final int ICON_DIMENSION = 128;

    private static volatile boolean processing;
    private static Path outputFolder;

    public static void initialize() {
        if (!"true".equals(System.getProperty("ae2.siteexport.run"))) {
            return;
        }

        outputFolder = getOutputFolder();

        LOGGER.info("Will run AE2 site export and exit...");
        MinecraftForge.EVENT_BUS.addListener(SiteExporter::onRenderTick);
    }

    private static Path getOutputFolder() {
        String folder = System.getProperty("ae2.siteexport.output", "site-export");
        Path folderPath = Paths.get(folder);
        try {
            Files.createDirectories(folderPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return folderPath;
    }

    private static void onRenderTick(TickEvent.ClientTickEvent.RenderTickEvent evt) {
        if (evt.phase != TickEvent.Phase.START) {
            return;
        }

        if (processing) {
            // Loading the level will recurse
            return;
        }
        processing = true;

        Minecraft client = Minecraft.getInstance();
        try {
            runExport(client);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // Close next tick
            GLFW.glfwSetWindowShouldClose(client.getWindow().getWindow(), true);
        }
    }

    private static void runExport(Minecraft client) throws Exception {

        var siteExport = new SiteExportWriter();

        var usedVanillaItems = new HashSet<Item>();
        processRecipes(client, usedVanillaItems, siteExport);

        // Iterate over all Applied Energistics items
        var stacks = new ArrayList<ItemStack>();
        for (Item item : Registry.ITEM) {
            if (getItemId(item).getNamespace().equals(AppEng.MOD_ID)) {
                stacks.add(new ItemStack(item));
            }
        }

        // Also add Vanilla items
        for (Item usedVanillaItem : usedVanillaItems) {
            stacks.add(new ItemStack(usedVanillaItem));
        }

        // All files in this folder will be directly served from the root of the web-site
        Path assetFolder = outputFolder.resolve("public");

        while (true) {
            try {
                renderScenes(assetFolder);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.yield();
            if (1 == 99) {
                break;
            }
        }

        processItems(client, siteExport, stacks, assetFolder);

        Path dataFolder = outputFolder.resolve("data");
        Files.createDirectories(dataFolder);
        siteExport.write(dataFolder.resolve("game-data.json"));
    }

    private static void processItems(Minecraft client,
            SiteExportWriter siteExport,
            List<ItemStack> items,
            Path assetFolder) throws IOException {
        Path iconsFolder = assetFolder.resolve("icons");
        if (Files.exists(iconsFolder)) {
            MoreFiles.deleteRecursively(iconsFolder, RecursiveDeleteOption.ALLOW_INSECURE);
        }

        try (var itemRenderer = new OffScreenRenderer(ICON_DIMENSION, ICON_DIMENSION)) {
            itemRenderer.setupItemRendering();

            for (ItemStack stack : items) {
                String itemId = getItemId(stack.getItem()).toString();
                var iconPath = iconsFolder.resolve(itemId.replace(':', '/') + ".png");
                Files.createDirectories(iconPath.getParent());

                itemRenderer.captureAsPng(() -> {
                    client.getItemRenderer().renderAndDecorateFakeItem(stack, 0, 0);
                }, iconPath);

                String absIconUrl = "/" + assetFolder.relativize(iconPath).toString().replace('\\', '/');
                siteExport.addItem(itemId, stack, absIconUrl);
            }
        }
    }

    private static void renderScenes(Path outputFolder) throws Exception {
        // Dirty hack to get to the frame of the AE2 controller texture we want
        TextureManager textureManager = Minecraft.getInstance().textureManager;
        for (int i = 0; i < 20; i++) {
            textureManager.tick();
        }

        for (Scene scene : SiteExportScenes.createScenes()) {
            Path sceneOutput = outputFolder.resolve(scene.filename);
            Files.createDirectories(sceneOutput.getParent());

            renderScene(sceneOutput, scene);
        }
    }

    private static void renderScene(Path outputPath, Scene scene) throws Exception {
        var blockRenderer = Minecraft.getInstance().getBlockRenderer();
        var rand = new Random(0);

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

            renderer.captureAsPng(() -> {
                var world = scene.world;
                var worldMat = new PoseStack();
                RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

                var buffers = Minecraft.getInstance().renderBuffers().bufferSource();

                for (var rt : new RenderType[] {
                        RenderType.solid(),
                        RenderType.cutout(),
                        RenderType.cutoutMipped()
                }) {
                    var buffer = buffers.getBuffer(rt);

                    for (var entry : world.blocks.entrySet()) {
                        var pos = entry.getKey();
                        var state = entry.getValue();
                        if (ItemBlockRenderTypes.canRenderInLayer(state, rt)) {
                            worldMat.pushPose();
                            worldMat.translate(pos.getX(), pos.getY(), pos.getZ());
                            blockRenderer.renderBatched(state, pos, world, worldMat, buffer, false, rand);
                            worldMat.popPose();
                        }
                    }
                    buffers.endBatch();
                }
            }, outputPath);
        }
    }

    private static ResourceLocation getItemId(Item item) {
        return Registry.ITEM.getKey(item);
    }

    private static void processRecipes(Minecraft client, Set<Item> usedVanillaItems, SiteExportWriter siteExport)
            throws Exception {

        // Fake a level in a temporary folder
        var tempLevel = Files.createTempDirectory("templevel");

        var registryAccess = RegistryAccess.builtin();
        var levelStorageSource = new LevelStorageSource(tempLevel, tempLevel, DataFixers.getDataFixer());
        var levelAccess = levelStorageSource.createAccess("siteexport");

        // Save a barebones level.dat
        var levelsettings = MinecraftServer.DEMO_SETTINGS;
        var worldgensettings = WorldGenSettings.demoSettings(registryAccess);
        levelAccess.saveDataTag(registryAccess,
                new PrimaryLevelData(levelsettings, worldgensettings, Lifecycle.stable()));

        try (var stem = client.makeServerStem(registryAccess, Minecraft::loadDataPacks, Minecraft::loadWorldData, false,
                levelAccess)) {
            var recipeManager = stem.serverResources().getRecipeManager();

            dumpRecipes(recipeManager, usedVanillaItems, siteExport);
        } finally {
            MoreFiles.deleteRecursively(tempLevel, RecursiveDeleteOption.ALLOW_INSECURE);
        }
    }

    private static void addVanillaItem(Set<Item> items, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        var item = stack.getItem();
        if ("minecraft".equals(Registry.ITEM.getKey(item).getNamespace())) {
            items.add(item);
        }
    }

    private static void dumpRecipes(RecipeManager recipeManager, Set<Item> vanillaItems, SiteExportWriter siteExport) {

        for (Recipe<?> recipe : recipeManager.getRecipes()) {
            // Only consider our recipes
            if (!recipe.getId().getNamespace().equals(AppEng.MOD_ID)) {
                continue;
            }

            if (recipe instanceof CraftingRecipe craftingRecipe) {
                if (craftingRecipe.isSpecial() || craftingRecipe.getResultItem().isEmpty()) {
                    continue;
                }

                addVanillaItem(vanillaItems, craftingRecipe.getResultItem());

                for (Ingredient ingredient : craftingRecipe.getIngredients()) {
                    for (ItemStack item : ingredient.getItems()) {
                        addVanillaItem(vanillaItems, item);
                    }
                }

                siteExport.addRecipe(craftingRecipe);
            }
        }

    }

}
