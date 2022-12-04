package appeng.siteexport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;

import appeng.api.features.P2PTunnelAttunement;
import appeng.api.features.P2PTunnelAttunementInternal;
import appeng.api.util.AEColor;
import appeng.core.AppEng;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.ColoredItemDefinition;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.siteexport.model.P2PTypeInfo;

/**
 * Exports a data package for use by the website.
 */
@Environment(EnvType.CLIENT)
public final class SiteExporter {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final int ICON_DIMENSION = 128;

    private static volatile SceneExportJob job;

    public static void initialize() {
        WorldRenderEvents.AFTER_SETUP.register(context -> {
            continueJob(SceneExportJob::render);
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            continueJob(SceneExportJob::tick);
        });
    }

    public static void export(FabricClientCommandSource source) {
        source.sendFeedback(Component.literal("AE2 Site-Export started"));
        job = null;
        try {
            startExport(Minecraft.getInstance(), source);
        } catch (Exception e) {
            LOGGER.error("AE2 site export failed.", e);
            source.sendError(Component.literal(e.toString()));
        }
    }

    @FunctionalInterface
    interface JobFunction {
        void accept(SceneExportJob job) throws Exception;
    }

    private static void continueJob(JobFunction event) {
        if (job != null) {
            try {
                event.accept(job);
                if (job.isAtEnd()) {
                    job.sendFeedback(Component.literal("AE2 game data exported to ")
                            .append(Component.literal("[site-export]")
                                    .withStyle(style -> style
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, "site-export"))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                    Component.literal("Click to open export folder")))
                                            .applyFormats(ChatFormatting.UNDERLINE, ChatFormatting.GREEN))));
                    job = null;
                }
            } catch (Exception e) {
                LOGGER.error("AE2 site export failed.", e);
                job.sendError(Component.literal(e.toString()));
                job = null;
            }
        }
    }

    private static void startExport(Minecraft client, FabricClientCommandSource source) throws Exception {
        if (!client.hasSingleplayerServer()) {
            throw new IllegalStateException("Only run this command from single-player.");
        }

        Path outputFolder = Paths.get("site-export").toAbsolutePath();
        if (Files.isDirectory(outputFolder)) {
            MoreFiles.deleteDirectoryContents(outputFolder, RecursiveDeleteOption.ALLOW_INSECURE);
        } else {
            Files.createDirectories(outputFolder);
        }

        var siteExport = new SiteExportWriter();

        var usedVanillaItems = new HashSet<Item>();

        dumpRecipes(client.level.getRecipeManager(), usedVanillaItems, siteExport);

        dumpP2PTypes(usedVanillaItems, siteExport);

        // Used by compass in charger recipe
        usedVanillaItems.add(Items.COMPASS);

        dumpColoredItems(siteExport);

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

        processItems(client, siteExport, stacks, assetFolder);

        Path dataFolder = outputFolder.resolve("data");
        Files.createDirectories(dataFolder);
        siteExport.write(dataFolder.resolve("game-data.json"));

        job = new SceneExportJob(SiteExportScenes.createScenes(), source, assetFolder);
    }

    /**
     * Dumps the P2P tunnel types and how they can be obtained.
     */
    private static void dumpP2PTypes(Set<Item> usedVanillaItems, SiteExportWriter siteExport) {

        var tunnelTypes = new ItemLike[] {
                P2PTunnelAttunement.ME_TUNNEL,
                P2PTunnelAttunement.ENERGY_TUNNEL,
                P2PTunnelAttunement.ITEM_TUNNEL,
                P2PTunnelAttunement.FLUID_TUNNEL,
                P2PTunnelAttunement.REDSTONE_TUNNEL,
                P2PTunnelAttunement.LIGHT_TUNNEL
        };

        for (var tunnelItem : tunnelTypes) {
            var typeInfo = new P2PTypeInfo();
            typeInfo.tunnelItemId = getItemId(tunnelItem.asItem()).toString();

            Set<Item> items = new HashSet<>();
            for (var entry : P2PTunnelAttunementInternal.getTagTunnels().entrySet()) {
                if (entry.getValue() == tunnelItem.asItem()) {
                    Registry.ITEM.getTagOrEmpty(entry.getKey()).forEach(h -> items.add(h.value()));
                }
            }
            items.stream().map(i -> getItemId(i).toString()).forEach(typeInfo.attunementItemIds::add);

            // Export attunement info
            var attunementInfo = P2PTunnelAttunementInternal.getAttunementInfo(tunnelItem);
            attunementInfo.apis().stream().map(lookup -> lookup.apiClass().getName())
                    .forEach(typeInfo.attunementApiClasses::add);

            usedVanillaItems.addAll(items);
            siteExport.addP2PType(typeInfo);
        }
    }

    /**
     * Dumps a table that describes the relationship between items that are just colored variants of each other.
     */
    private static void dumpColoredItems(SiteExportWriter siteExport) {
        for (var coloredPart : AEParts.COLORED_PARTS) {
            dumpColoredItem(coloredPart, siteExport);
        }
    }

    private static void dumpColoredItem(ColoredItemDefinition itemDefinition, SiteExportWriter siteExport) {
        var baseItem = itemDefinition.item(AEColor.TRANSPARENT);
        if (baseItem == null) {
            return;
        }

        for (var color : AEColor.values()) {
            if (color.dye == null) {
                continue;
            }

            var coloredItem = itemDefinition.item(color);
            if (coloredItem != null) {
                siteExport.addColoredVersion(baseItem, color.dye, coloredItem);
            }
        }
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

    private static ResourceLocation getItemId(Item item) {
        return Registry.ITEM.getKey(item);
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

            if (!recipe.getResultItem().isEmpty()) {
                addVanillaItem(vanillaItems, recipe.getResultItem());
            }
            for (Ingredient ingredient : recipe.getIngredients()) {
                for (ItemStack item : ingredient.getItems()) {
                    addVanillaItem(vanillaItems, item);
                }
            }

            if (recipe instanceof CraftingRecipe craftingRecipe) {
                if (craftingRecipe.isSpecial()) {
                    continue;
                }
                siteExport.addRecipe(craftingRecipe);
            } else if (recipe instanceof AbstractCookingRecipe cookingRecipe) {
                siteExport.addRecipe(cookingRecipe);
            } else if (recipe instanceof InscriberRecipe inscriberRecipe) {
                siteExport.addRecipe(inscriberRecipe);
            }
        }

    }

}
