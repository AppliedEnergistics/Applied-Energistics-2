package appeng.siteexport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import com.mojang.serialization.Lifecycle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
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

    private static final int FB_WIDTH = 128;
    private static final int FB_HEIGHT = 128;

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

        // Set up GL state for GUI rendering
        RenderSystem.setProjectionMatrix(Matrix4f.orthographic(0, 16, 0, 16, 1000, 3000));

        var poseStack = RenderSystem.getModelViewStack();
        poseStack.setIdentity();
        poseStack.translate(0.0F, 0.0F, -2000.0F);
        Lighting.setupFor3DItems();

        RenderSystem.applyModelViewMatrix();

        var nativeImage = new NativeImage(FB_WIDTH, FB_HEIGHT, true);
        var fb = new TextureTarget(FB_WIDTH, FB_HEIGHT, true, false);
        fb.setClearColor(0, 0, 0, 0);
        fb.clear(true);

        // Iterate over all Applied Energistics items
        var stacks = new ArrayList<ItemStack>();
        for (Item item : Registry.ITEM) {
            if (item.getRegistryName().getNamespace().equals(AppEng.MOD_ID)) {
                stacks.add(new ItemStack(item));
            }
        }

        // Also add Vanilla items
        for (Item usedVanillaItem : usedVanillaItems) {
            stacks.add(new ItemStack(usedVanillaItem));
        }

        Path iconsFolder = outputFolder.resolve("icons");
        if (Files.exists(iconsFolder)) {
            MoreFiles.deleteRecursively(iconsFolder, RecursiveDeleteOption.ALLOW_INSECURE);
        }

        for (ItemStack stack : stacks) {
            // Render the item normally
            fb.bindWrite(true);
            GlStateManager._clear(GL12.GL_COLOR_BUFFER_BIT | GL12.GL_DEPTH_BUFFER_BIT, false);
            client.getItemRenderer().renderAndDecorateFakeItem(stack, 0, 0);
            fb.unbindWrite();

            // Load the rendered item back into CPU memory
            fb.bindRead();
            nativeImage.downloadTexture(0, false);
            nativeImage.flipY();
            fb.unbindRead();

            // Save the rendered icon
            String itemId = stack.getItem().getRegistryName().toString();
            var iconPath = iconsFolder.resolve(itemId.replace(':', '/') + ".png");
            Files.createDirectories(iconPath.getParent());
            nativeImage.writeToFile(iconPath);

            siteExport.addItem(stack, outputFolder.relativize(iconPath).toString());
        }

        try {
            siteExport.write(outputFolder.resolve("site_export.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        nativeImage.close();
        fb.destroyBuffers();
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
