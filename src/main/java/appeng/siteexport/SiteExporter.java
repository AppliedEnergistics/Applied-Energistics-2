package appeng.siteexport;

import appeng.api.features.P2PTunnelAttunement;
import appeng.api.features.P2PTunnelAttunementInternal;
import appeng.api.util.AEColor;
import appeng.client.guidebook.Guide;
import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.ParsedGuidePage;
import appeng.client.guidebook.indices.CategoryIndex;
import appeng.client.guidebook.indices.ItemIndex;
import appeng.core.AppEngClient;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.ColoredItemDefinition;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.siteexport.model.P2PTypeInfo;
import appeng.util.Platform;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Exports a data package for use by the website.
 */
@Environment(EnvType.CLIENT)
public final class SiteExporter implements ResourceExporter {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final int ICON_DIMENSION = 128;

    private final Minecraft client;

    private final Path outputFolder;

    private final Guide guide;

    private ParsedGuidePage currentPage;

    private final Set<Recipe<?>> recipes = new HashSet<>();

    private final Set<Item> items = new HashSet<>();

    public SiteExporter(Minecraft client, Path outputFolder, Guide guide) {
        this.client = client;
        this.outputFolder = outputFolder;
        this.guide = guide;
    }

    public static void initialize() {
        // Automatically run the export once the client has started and then exit
        if (Boolean.getBoolean("appeng.runGuideExportAndExit")) {
            Path outputFolder = Paths.get(System.getProperty("appeng.guideExportFolder"));

            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (client.getOverlay() instanceof LoadingOverlay) {
                    return; // Do nothing while it's loading
                }

                var guide = AppEngClient.instance().getGuide();
                try {
                    export(client, outputFolder, guide);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                System.exit(0);
            });
        }
    }

    public static void export(FabricClientCommandSource source) {
        var guide = AppEngClient.instance().getGuide();
        try {
            Path outputFolder = Paths.get("guide-export").toAbsolutePath();
            export(Minecraft.getInstance(), outputFolder, guide);

            source.sendFeedback(Component.literal("Guide data exported to ")
                    .append(Component.literal("[" + outputFolder.getFileName().toString() + "]")
                            .withStyle(style -> style
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, outputFolder.toString()))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.literal("Click to open export folder")))
                                    .applyFormats(ChatFormatting.UNDERLINE, ChatFormatting.GREEN))));
        } catch (Exception e) {
            e.printStackTrace();
            source.sendError(Component.literal(e.toString()));
        }
    }

    private static void export(Minecraft client, Path outputFolder, Guide guide) throws Exception {
        new SiteExporter(client, outputFolder, guide).export();
    }

    @Override
    public void referenceItem(ItemStack stack) {
        if (!stack.isEmpty()) {
            items.add(stack.getItem());
            if (stack.hasTag()) {
                LOGGER.error("Couldn't handle stack with NBT tag: {}", stack);
            }
        }
    }

    @Override
    public void referenceRecipe(Recipe<?> recipe) {
        if (!recipes.add(recipe)) {
            return; // Already added
        }

        var registryAccess = Platform.getClientRegistryAccess();
        var resultItem = recipe.getResultItem(registryAccess);
        if (!resultItem.isEmpty()) {
            referenceItem(resultItem);
        }
        for (var ingredient : recipe.getIngredients()) {
            for (var item : ingredient.getItems()) {
                referenceItem(item);
            }
        }
    }

    private void dumpRecipes(SiteExportWriter writer) {
        for (var recipe : recipes) {
            if (recipe instanceof CraftingRecipe craftingRecipe) {
                if (craftingRecipe.isSpecial()) {
                    continue;
                }
                writer.addRecipe(craftingRecipe);
            } else if (recipe instanceof AbstractCookingRecipe cookingRecipe) {
                writer.addRecipe(cookingRecipe);
            } else if (recipe instanceof InscriberRecipe inscriberRecipe) {
                writer.addRecipe(inscriberRecipe);
            }
        }
    }

    @Override
    public void copyResource(ResourceLocation id) {
        try {
            var pagePath = getPathForWriting(id);
            byte[] bytes = guide.loadAsset(id);
            if (bytes == null) {
                throw new IllegalArgumentException("Couldn't find asset " + id);
            }
            Files.write(pagePath, bytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy resource " + id, e);
        }
    }

    @Override
    public Path getPathForWriting(ResourceLocation assetId) {
        try {
            var path = resolvePath(assetId);
            Files.createDirectories(path.getParent());
            return path;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Path getOutputFolder() {
        return outputFolder;
    }

    @Override
    public ResourceLocation getPageSpecificResourceLocation(String suffix) {
        var path = currentPage.getId().getPath();
        var idx = path.lastIndexOf('.');
        if (idx != -1) {
            path = path.substring(0, idx);
        }
        return new ResourceLocation(currentPage.getId().getNamespace(), path + "_" + suffix);
    }

    private void export() throws Exception {
        if (Files.isDirectory(outputFolder)) {
            MoreFiles.deleteDirectoryContents(outputFolder, RecursiveDeleteOption.ALLOW_INSECURE);
        } else {
            Files.createDirectories(outputFolder);
        }

        // Load data packs if needed
        if (client.level == null) {
            LOGGER.info("Reloading datapacks to get recipes");
            Guide.runDatapackReload();
            LOGGER.info("Completed datapack reload");
        }

        var indexWriter = new SiteExportWriter(guide);

        for (var page : guide.getPages()) {
            currentPage = page;

            indexWriter.addPage(page);

            LOGGER.debug("Compiling {}", page);
            var compiledPage = PageCompiler.compile(guide, guide.getExtensions(), page);

            // Copy the page itself
            copyResource(page.getId());

            ExportableResourceProvider.visit(compiledPage.document(), SiteExporter.this);
        }

        dumpRecipes(indexWriter);

        processItems(client, indexWriter, outputFolder);

        indexWriter.addIndex(guide, ItemIndex.class);

        indexWriter.write(outputFolder.resolve("!index.json"));
//
//        Path dataFolder = outputFolder.resolve("data");
//        Files.createDirectories(dataFolder);
//        siteExport.write(dataFolder.resolve("game-data.json"));
    }

    private Path resolvePath(ResourceLocation id) {
        return outputFolder.resolve(id.getNamespace() + "/" + id.getPath());
    }

    /**
     * Dumps the P2P tunnel types and how they can be obtained.
     */
    private static void dumpP2PTypes(Set<Item> usedVanillaItems, SiteExportWriter siteExport) {

        var tunnelTypes = new ItemLike[]{
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
                    BuiltInRegistries.ITEM.getTagOrEmpty(entry.getKey()).forEach(h -> items.add(h.value()));
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

    private void processItems(Minecraft client,
                              SiteExportWriter siteExport,
                              Path outputFolder) throws IOException {
        var iconsFolder = outputFolder.resolve("!icons");
        if (Files.exists(iconsFolder)) {
            MoreFiles.deleteRecursively(iconsFolder, RecursiveDeleteOption.ALLOW_INSECURE);
        }

        try (var itemRenderer = new OffScreenRenderer(ICON_DIMENSION, ICON_DIMENSION)) {
            var guiGraphics = new GuiGraphics(client, client.renderBuffers().bufferSource());

            itemRenderer.setupItemRendering();

            LOGGER.info("Exporting items...");
            for (var item : items) {
                var stack = new ItemStack(item);

                String itemId = getItemId(stack.getItem()).toString();
                var iconPath = iconsFolder.resolve(itemId.replace(':', '/') + ".png");
                Files.createDirectories(iconPath.getParent());

                itemRenderer.captureAsPng(() -> {
                    guiGraphics.renderItem(stack, 0, 0);
                    guiGraphics.renderItemDecorations(client.font, stack, 0, 0, "");
                }, iconPath);

                String absIconUrl = "/" + outputFolder.relativize(iconPath).toString().replace('\\', '/');
                siteExport.addItem(itemId, stack, absIconUrl);
            }
        }
    }

    private static ResourceLocation getItemId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

}
