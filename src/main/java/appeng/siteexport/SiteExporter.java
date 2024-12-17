package appeng.siteexport;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import com.mojang.blaze3d.platform.NativeImage;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.ChatFormatting;
import net.minecraft.DetectedVersion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidStack;

import appeng.api.features.P2PTunnelAttunement;
import appeng.api.features.P2PTunnelAttunementInternal;
import appeng.api.util.AEColor;
import appeng.client.guidebook.Guide;
import appeng.client.guidebook.GuidePage;
import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.ParsedGuidePage;
import appeng.client.guidebook.indices.CategoryIndex;
import appeng.client.guidebook.indices.ItemIndex;
import appeng.client.guidebook.navigation.NavigationNode;
import appeng.core.AppEngClient;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.ColoredItemDefinition;
import appeng.recipes.entropy.EntropyRecipe;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.mattercannon.MatterCannonAmmo;
import appeng.recipes.transform.TransformRecipe;
import appeng.siteexport.mdastpostprocess.PageExportPostProcessor;
import appeng.siteexport.model.P2PTypeInfo;
import appeng.util.CraftingRecipeUtil;
import appeng.util.Platform;

/**
 * Exports a data package for use by the website.
 */
@OnlyIn(Dist.CLIENT)
public final class SiteExporter implements ResourceExporter {

    private static final Logger LOG = LoggerFactory.getLogger(SiteExporter.class);

    private static final int ICON_DIMENSION = 128;

    private final Minecraft client;
    private final Map<ResourceLocation, String> exportedTextures = new HashMap<>();

    private final Path outputFolder;

    private final Guide guide;

    private ParsedGuidePage currentPage;

    private final Set<RecipeHolder<?>> recipes = new HashSet<>();

    private final Set<Item> items = new HashSet<>();

    private final Set<Fluid> fluids = new HashSet<>();

    public SiteExporter(Minecraft client, Path outputFolder, Guide guide) {
        this.client = client;
        this.outputFolder = outputFolder;
        this.guide = guide;

        // Ref items used as icons
        referenceItem(Items.FURNACE);
        referenceItem(AEBlocks.INSCRIBER);
        referenceFluid(Fluids.WATER);
        referenceFluid(Fluids.LAVA);
        referenceItem(Items.TNT);
        referenceItem(Blocks.SMITHING_TABLE);
    }

    public static void initialize() {
        // Automatically run the export once the client has started and then exit
        if (Boolean.getBoolean("appeng.runGuideExportAndExit")) {
            Path outputFolder = Paths.get(System.getProperty("appeng.guideExportFolder"));

            NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post evt) -> {
                var client = Minecraft.getInstance();
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
                                    .withClickEvent(
                                            new ClickEvent(ClickEvent.Action.OPEN_FILE, outputFolder.toString()))
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
            if (!stack.getComponentsPatch().isEmpty()) {
                LOG.error("Couldn't handle stack with NBT tag: {}", stack);
            }
        }
    }

    @Override
    public void referenceFluid(Fluid fluid) {
        fluids.add(fluid);
    }

    private void referenceIngredient(Ingredient ingredient) {
        for (var item : ingredient.getItems()) {
            referenceItem(item);
        }
    }

    @Override
    public void referenceRecipe(RecipeHolder<?> holder) {
        if (!recipes.add(holder)) {
            return; // Already added
        }

        var recipe = holder.value();

        var registryAccess = Platform.getClientRegistryAccess();
        var resultItem = recipe.getResultItem(registryAccess);
        if (!resultItem.isEmpty()) {
            referenceItem(resultItem);
        }
        for (var ingredient : CraftingRecipeUtil.getIngredients(recipe)) {
            referenceIngredient(ingredient);
        }
    }

    private void dumpRecipes(SiteExportWriter writer) {
        for (var holder : recipes) {
            var id = holder.id();
            var recipe = holder.value();

            if (recipe instanceof CraftingRecipe craftingRecipe) {
                if (craftingRecipe.isSpecial()) {
                    continue;
                }
                writer.addRecipe(id, craftingRecipe);
            } else if (recipe instanceof AbstractCookingRecipe cookingRecipe) {
                writer.addRecipe(id, cookingRecipe);
            } else if (recipe instanceof InscriberRecipe inscriberRecipe) {
                writer.addRecipe(id, inscriberRecipe);
            } else if (recipe instanceof TransformRecipe transformRecipe) {
                writer.addRecipe(id, transformRecipe);
            } else if (recipe instanceof SmithingTransformRecipe smithingTransformRecipe) {
                writer.addRecipe(id, smithingTransformRecipe);
            } else if (recipe instanceof SmithingTrimRecipe smithingTrimRecipe) {
                writer.addRecipe(id, smithingTrimRecipe);
            } else if (recipe instanceof StonecutterRecipe stonecutterRecipe) {
                writer.addRecipe(id, stonecutterRecipe);
            } else if (recipe instanceof EntropyRecipe entropyRecipe) {
                writer.addRecipe(id, entropyRecipe);
            } else if (recipe instanceof MatterCannonAmmo ammoRecipe) {
                writer.addRecipe(id, ammoRecipe);
            } else if (recipe instanceof ChargerRecipe chargerRecipe) {
                writer.addRecipe(id, chargerRecipe);
            } else {
                LOG.warn("Unable to handle recipe {} of type {}", holder.id(), recipe.getType());
            }
        }
    }

    @Override
    public Path copyResource(ResourceLocation id) {
        try {
            var pagePath = getPathForWriting(id);
            byte[] bytes = guide.loadAsset(id);
            if (bytes == null) {
                throw new IllegalArgumentException("Couldn't find asset " + id);
            }
            return CacheBusting.writeAsset(pagePath, bytes);
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
        return ResourceLocation.fromNamespaceAndPath(currentPage.getId().getNamespace(), path + "_" + suffix);
    }

    @Override
    public Path getPageSpecificPathForWriting(String suffix) {
        // Build filename
        var pageFilename = currentPage.getId().getPath();
        var filename = FilenameUtils.getBaseName(pageFilename) + "_" + suffix;

        var pagePath = resolvePath(currentPage.getId());
        var path = pagePath.resolveSibling(filename);

        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return path;
    }

    @Override
    public @Nullable ResourceLocation getCurrentPageId() {
        return currentPage != null ? currentPage.getId() : null;
    }

    private void export() throws Exception {
        if (Files.isDirectory(outputFolder)) {
            MoreFiles.deleteDirectoryContents(outputFolder, RecursiveDeleteOption.ALLOW_INSECURE);
        } else {
            Files.createDirectories(outputFolder);
        }

        // Load data packs if needed
        if (client.level == null) {
            LOG.info("Reloading datapacks to get recipes");
            Guide.runDatapackReload();
            LOG.info("Completed datapack reload");
        }

        // Reference all navigation node icons
        guide.getNavigationTree().getRootNodes().forEach(this::visitNavigationNodeIcons);

        var indexWriter = new SiteExportWriter(guide);

        for (var page : guide.getPages()) {
            currentPage = page;

            LOG.debug("Compiling {}", page);
            var compiledPage = PageCompiler.compile(guide, guide.getExtensions(), page);

            processPage(indexWriter, page, compiledPage);

            // Post-Process the parsed Markdown AST and export it as JSON into the index directly
            ExportableResourceProvider.visit(compiledPage.document(), SiteExporter.this);
        }

        dumpRecipes(indexWriter);

        processItems(client, indexWriter, outputFolder);
        processFluids(client, indexWriter, outputFolder);

        indexWriter.addIndex(guide, ItemIndex.class);
        indexWriter.addIndex(guide, CategoryIndex.class);

        var guideContent = outputFolder.resolve("guide.json.gz");
        byte[] content = indexWriter.toByteArray();

        guideContent = CacheBusting.writeAsset(guideContent, content);

        // Write an uncompressed summary
        writeSummary(guideContent.getFileName().toString());
    }

    private void visitNavigationNodeIcons(NavigationNode navigationNode) {
        referenceItem(navigationNode.icon());
        navigationNode.children().forEach(this::visitNavigationNodeIcons);
    }

    private void processPage(SiteExportWriter exportWriter,
            ParsedGuidePage page,
            GuidePage compiledPage) {

        // Run post-processors on the AST
        PageExportPostProcessor.postprocess(this, page, compiledPage);

        exportWriter.addPage(page);
    }

    private void writeSummary(String guideDataFilename) throws IOException {
        var modVersion = System.getProperty("appeng.version", "unknown");
        var generated = Instant.now().toEpochMilli();
        var gameVersion = DetectedVersion.tryDetectVersion().getName();

        // This file is not accessed via the CDN and thus doesn't need a cache-busting name
        try (var writer = Files.newBufferedWriter(outputFolder.resolve("index.json"), StandardCharsets.UTF_8)) {
            var jsonWriter = SiteExportWriter.GSON.newJsonWriter(writer);
            jsonWriter.beginObject();
            jsonWriter.name("format").value(1);
            jsonWriter.name("generated").value(generated);
            jsonWriter.name("gameVersion").value(gameVersion);
            jsonWriter.name("modVersion").value(modVersion);
            jsonWriter.name("guideDataPath").value(guideDataFilename);
            jsonWriter.endObject();
        }
    }

    private Path resolvePath(ResourceLocation id) {
        return outputFolder.resolve(id.getNamespace() + "/" + id.getPath());
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
                    BuiltInRegistries.ITEM.getTagOrEmpty(entry.getKey()).forEach(h -> items.add(h.value()));
                }
            }
            items.stream().map(i -> getItemId(i).toString()).forEach(typeInfo.attunementItemIds::add);

            // Export attunement info
            var attunementInfo = P2PTunnelAttunementInternal.getAttunementInfo(tunnelItem);
            attunementInfo.apis().stream().map(c -> c.name().toString())
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
        var iconsFolder = outputFolder.resolve("!items");
        if (Files.exists(iconsFolder)) {
            MoreFiles.deleteRecursively(iconsFolder, RecursiveDeleteOption.ALLOW_INSECURE);
        }

        try (var renderer = new OffScreenRenderer(ICON_DIMENSION, ICON_DIMENSION)) {
            var guiGraphics = new GuiGraphics(client, client.renderBuffers().bufferSource());

            renderer.setupItemRendering();

            LOG.info("Exporting items...");
            for (var item : items) {
                var stack = new ItemStack(item);

                var itemId = getItemId(stack.getItem()).toString();
                var baseName = "!items/" + itemId.replace(':', '/');

                // Guess used sprites from item model
                var itemModel = client.getItemRenderer().getModel(stack, null, null, 0);
                var sprites = guessSprites(Set.of(itemModel));

                var iconPath = renderAndWrite(renderer, baseName, () -> {
                    guiGraphics.renderItem(stack, 0, 0);
                    guiGraphics.renderItemDecorations(client.font, stack, 0, 0, "");
                }, sprites, true);

                String absIconUrl = "/" + outputFolder.relativize(iconPath).toString().replace('\\', '/');
                siteExport.addItem(itemId, stack, absIconUrl);
            }
        }
    }

    private Set<TextureAtlasSprite> guessSprites(Collection<BakedModel> models) {
        var result = Collections.newSetFromMap(new IdentityHashMap<TextureAtlasSprite, Boolean>());
        var randomSource = new SingleThreadedRandomSource(0);

        for (var model : models) {
            for (var quad : model.getQuads(null, null, randomSource)) {
                result.add(quad.getSprite());
            }
        }

        return result;
    }

    private void processFluids(Minecraft client,
            SiteExportWriter siteExport,
            Path outputFolder) throws IOException {
        var fluidsFolder = outputFolder.resolve("!fluids");
        if (Files.exists(fluidsFolder)) {
            MoreFiles.deleteRecursively(fluidsFolder, RecursiveDeleteOption.ALLOW_INSECURE);
        }

        try (var renderer = new OffScreenRenderer(ICON_DIMENSION, ICON_DIMENSION)) {
            var guiGraphics = new GuiGraphics(client, client.renderBuffers().bufferSource());

            renderer.setupItemRendering();

            LOG.info("Exporting fluids...");
            for (var fluid : fluids) {
                var fluidVariant = new FluidStack(fluid, 1);
                String fluidId = BuiltInRegistries.FLUID.getKey(fluid).toString();

                var props = IClientFluidTypeExtensions.of(fluidVariant.getFluid());

                var sprite = Minecraft.getInstance()
                        .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                        .apply(props.getStillTexture(fluidVariant));
                var color = props.getTintColor(fluidVariant);

                var baseName = "!fluids/" + fluidId.replace(':', '/');
                var iconPath = renderAndWrite(
                        renderer,
                        baseName,
                        () -> {
                            if (sprite != null) {
                                var r = FastColor.ARGB32.red(color) / 255.f;
                                var g = FastColor.ARGB32.green(color) / 255.f;
                                var b = FastColor.ARGB32.blue(color) / 255.f;
                                var a = FastColor.ARGB32.alpha(color) / 255.f;
                                guiGraphics.blit(0, 0, 0, 16, 16, sprite, r, g, b, a);
                            }
                        },
                        sprite != null ? Set.of(sprite) : Set.of(),
                        false /*
                               * no alpha for fluids since water is translucent but there's nothing behind it in our
                               * icons
                               */
                );

                String absIconUrl = "/" + outputFolder.relativize(iconPath).toString().replace('\\', '/');
                siteExport.addFluid(fluidId, fluidVariant, absIconUrl);
            }
        }

    }

    @Override
    public Path renderAndWrite(OffScreenRenderer renderer,
            String baseName,
            Runnable renderRunnable,
            Collection<TextureAtlasSprite> sprites,
            boolean withAlpha) throws IOException {
        String extension;
        byte[] content;
        if (renderer.isAnimated(sprites)) {
            extension = ".webp";
            content = renderer.captureAsWebp(
                    renderRunnable,
                    sprites,
                    withAlpha ? WebPExporter.Format.LOSSLESS_ALPHA : WebPExporter.Format.LOSSLESS);
        } else {
            extension = ".png";
            content = renderer.captureAsPng(renderRunnable);
        }

        var iconPath = outputFolder.resolve(baseName + extension);
        Files.createDirectories(iconPath.getParent());
        return CacheBusting.writeAsset(iconPath, content);
    }

    @Override
    public String exportTexture(ResourceLocation textureId) {
        var exportedPath = exportedTextures.get(textureId);
        if (exportedPath != null) {
            return exportedPath;
        }

        ResourceLocation id = textureId;
        if (!id.getPath().endsWith(".png")) {
            id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + ".png");
        }

        var outputPath = getPathForWriting(id);

        var texture = Minecraft.getInstance().getTextureManager().getTexture(textureId);

        if (texture instanceof TextureAtlas textureAtlas) {
            for (var sprite : textureAtlas.sprites) {
                if (sprite.animatedTexture != null) {
                }
            }
        }

        texture.bind();
        int w, h;
        int[] intResult = new int[1];
        GL11.glGetTexLevelParameteriv(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH, intResult);
        w = intResult[0];
        GL11.glGetTexLevelParameteriv(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT, intResult);
        h = intResult[0];

        byte[] imageContent;
        try (var nativeImage = new NativeImage(w, h, false)) {
            nativeImage.downloadTexture(0, false);
            imageContent = nativeImage.asByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            outputPath = CacheBusting.writeAsset(outputPath, imageContent);
        } catch (IOException e) {
            throw new RuntimeException("Failed to export texture " + textureId, e);
        }
        exportedPath = getPathRelativeFromOutputFolder(outputPath);
        exportedTextures.put(textureId, exportedPath);
        return exportedPath;
    }

    private static ResourceLocation getItemId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    private static ResourceLocation getFluidId(Fluid fluid) {
        return BuiltInRegistries.FLUID.getKey(fluid);
    }

}
