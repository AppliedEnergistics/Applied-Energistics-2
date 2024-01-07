package appeng.siteexport;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.JsonTreeWriter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import appeng.client.guidebook.Guide;
import appeng.client.guidebook.compiler.MdAstNodeAdapter;
import appeng.client.guidebook.compiler.ParsedGuidePage;
import appeng.client.guidebook.indices.PageIndex;
import appeng.items.tools.powered.MatterCannonItem;
import appeng.libs.mdast.MdAstVisitor;
import appeng.libs.mdast.model.MdAstHeading;
import appeng.libs.mdast.model.MdAstNode;
import appeng.recipes.entropy.EntropyRecipe;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.mattercannon.MatterCannonAmmo;
import appeng.recipes.transform.TransformRecipe;
import appeng.siteexport.model.ExportedPageJson;
import appeng.siteexport.model.FluidInfoJson;
import appeng.siteexport.model.ItemInfoJson;
import appeng.siteexport.model.NavigationNodeJson;
import appeng.siteexport.model.P2PTypeInfo;
import appeng.siteexport.model.SiteExportJson;

public class SiteExportWriter {
    private static final Logger LOG = LoggerFactory.getLogger(SiteExportWriter.class);

    private abstract static class WriteOnlyTypeAdapter<T> extends TypeAdapter<T> {
        @Override
        public T read(JsonReader in) {
            throw new UnsupportedOperationException();
        }
    }

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .registerTypeHierarchyAdapter(MdAstNode.class, new MdAstNodeAdapter())
            // Serialize ResourceLocation as strings
            .registerTypeAdapter(ResourceLocation.class, new WriteOnlyTypeAdapter<ResourceLocation>() {
                @Override
                public void write(JsonWriter out, ResourceLocation value) throws IOException {
                    out.value(value.toString());
                }
            })
            // Serialize Ingredient as arrays of the corresponding item IDs
            .registerTypeAdapter(Ingredient.class, new WriteOnlyTypeAdapter<Ingredient>() {
                @Override
                public void write(JsonWriter out, Ingredient value) throws IOException {
                    out.beginArray();
                    for (var item : value.getItems()) {
                        var itemId = BuiltInRegistries.ITEM.getKey(item.getItem());
                        out.value(itemId.toString());
                    }
                    out.endArray();
                }
            })
            // Serialize Items & Fluids using their registered ID
            .registerTypeHierarchyAdapter(Item.class, new WriteOnlyTypeAdapter<Item>() {
                @Override
                public void write(JsonWriter out, Item value) throws IOException {
                    if (value == null) {
                        out.nullValue();
                    } else {
                        out.value(BuiltInRegistries.ITEM.getKey(value).toString());
                    }
                }
            })
            .registerTypeHierarchyAdapter(Fluid.class, new WriteOnlyTypeAdapter<Fluid>() {
                @Override
                public void write(JsonWriter out, Fluid value) throws IOException {
                    if (value == null) {
                        out.nullValue();
                    } else {
                        out.value(BuiltInRegistries.FLUID.getKey(value).toString());
                    }
                }
            })
            // ItemStacks use the Item, and a normalized NBT format
            .registerTypeAdapter(ItemStack.class, new WriteOnlyTypeAdapter<ItemStack>() {
                @Override
                public void write(JsonWriter out, ItemStack value) throws IOException {
                    if (value == null || value.isEmpty()) {
                        out.nullValue();
                    } else {
                        out.value(BuiltInRegistries.ITEM.getKey(value.getItem()).toString());
                    }
                }
            })
            // Boolean
            .registerTypeAdapter(Boolean.class, new WriteOnlyTypeAdapter<Boolean>() {
                @Override
                public void write(JsonWriter out, Boolean value) throws IOException {
                    out.value(value.booleanValue());
                }
            })
            .create();
    private final SiteExportJson siteExport = new SiteExportJson();

    public SiteExportWriter(Guide guide) {
        siteExport.defaultNamespace = guide.getDefaultNamespace();
        siteExport.navigationRootNodes = guide.getNavigationTree().getRootNodes()
                .stream()
                .map(NavigationNodeJson::of)
                .toList();
    }

    public void addItem(String id, ItemStack stack, String iconPath) {
        var itemInfo = new ItemInfoJson();
        itemInfo.id = id;
        itemInfo.icon = iconPath;
        itemInfo.displayName = stack.getHoverName().getString();
        itemInfo.rarity = stack.getRarity().name().toLowerCase(Locale.ROOT);

        siteExport.items.put(itemInfo.id, itemInfo);
    }

    public void addFluid(String id, FluidStack fluid, String iconPath) {
        var fluidInfo = new FluidInfoJson();
        fluidInfo.id = id;
        fluidInfo.icon = iconPath;
        fluidInfo.displayName = fluid.getDisplayName().getString();
        siteExport.fluids.put(fluidInfo.id, fluidInfo);
    }

    public void addRecipe(ResourceLocation id, CraftingRecipe recipe) {
        Map<String, Object> fields = new HashMap<>();
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            fields.put("shapeless", false);
            fields.put("width", shapedRecipe.getWidth());
            fields.put("height", shapedRecipe.getHeight());
        } else {
            fields.put("shapeless", true);
        }

        ItemStack resultItem = recipe.getResultItem(null);
        fields.put("resultItem", resultItem);
        fields.put("resultCount", resultItem.getCount());
        fields.put("ingredients", recipe.getIngredients());

        addRecipe(id, recipe, fields);
    }

    public void addRecipe(ResourceLocation id, InscriberRecipe recipe) {
        var resultItem = recipe.getResultItem();
        addRecipe(id, recipe, Map.of(
                "top", recipe.getTopOptional(),
                "middle", recipe.getMiddleInput(),
                "bottom", recipe.getBottomOptional(),
                "resultItem", resultItem.getItem(),
                "resultCount", resultItem.getCount(),
                "consumesTopAndBottom", recipe.getProcessType() == InscriberProcessType.PRESS));
    }

    public void addRecipe(ResourceLocation id, AbstractCookingRecipe recipe) {
        addRecipe(id, recipe, Map.of(
                "resultItem", recipe.getResultItem(null),
                "ingredient", recipe.getIngredients().get(0)));
    }

    public void addRecipe(ResourceLocation id, TransformRecipe recipe) {

        Map<String, Object> circumstanceJson = new HashMap<>();
        var circumstance = recipe.circumstance;
        if (circumstance.isExplosion()) {
            circumstanceJson.put("type", "explosion");
        } else if (circumstance.isFluid()) {
            circumstanceJson.put("type", "fluid");

            // Special-case water since a lot of mods add their fluids to the tag
            if (recipe.circumstance.isFluidTag(FluidTags.WATER)) {
                circumstanceJson.put("fluids", List.of(Fluids.WATER));
            } else {
                circumstanceJson.put("fluids", circumstance.getFluidsForRendering());
            }
        } else {
            throw new IllegalStateException("Unknown circumstance: " + circumstance.toJson());
        }

        addRecipe(id, recipe, Map.of(
                "resultItem", recipe.getResultItem(null),
                "ingredients", recipe.getIngredients(),
                "circumstance", circumstanceJson));
    }

    public void addRecipe(ResourceLocation id, EntropyRecipe recipe) {
        addRecipe(id, recipe, Map.of(
                "mode", recipe.getMode().name().toLowerCase(Locale.ROOT)));
    }

    public void addRecipe(ResourceLocation id, MatterCannonAmmo recipe) {
        addRecipe(id, recipe, Map.of(
                "ammo", recipe.getAmmo(),
                "damage", MatterCannonItem.getDamageFromPenetration(recipe.getWeight())));
    }

    public void addRecipe(ResourceLocation id, ChargerRecipe recipe) {
        addRecipe(id, recipe, Map.of(
                "resultItem", recipe.getResultItem(),
                "ingredient", recipe.getIngredient()));
    }

    public void addRecipe(ResourceLocation id, SmithingTransformRecipe recipe) {
        addRecipe(id, recipe, Map.of(
                "resultItem", recipe.getResultItem(null),
                "base", recipe.base,
                "addition", recipe.addition,
                "template", recipe.template));
    }

    public void addRecipe(ResourceLocation id, SmithingTrimRecipe recipe) {
        addRecipe(id, recipe, Map.of(
                "base", recipe.base,
                "addition", recipe.addition,
                "template", recipe.template));
    }

    public void addRecipe(ResourceLocation id, StonecutterRecipe recipe) {
        addRecipe(id, recipe,
                Map.of(
                        "resultItem", recipe.getResultItem(null),
                        "ingredient", recipe.getIngredients().get(0)));
    }

    public void addRecipe(ResourceLocation id, Recipe<?> recipe, Map<String, Object> element) {
        // Auto-transform ingredients

        var jsonElement = GSON.toJsonTree(element);

        var type = BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType()).toString();
        jsonElement.getAsJsonObject().addProperty("type", type);

        if (siteExport.recipes.put(id.toString(), jsonElement) != null) {
            throw new RuntimeException("Duplicate recipe id " + id);
        }
    }

    public byte[] toByteArray() throws IOException {
        var bout = new ByteArrayOutputStream();
        try (var out = new GZIPOutputStream(bout);
                var writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            GSON.toJson(siteExport, writer);
        }
        return bout.toByteArray();
    }

    public void addP2PType(P2PTypeInfo typeInfo) {
        siteExport.p2pTunnelTypes.add(typeInfo);
    }

    public void addColoredVersion(Item baseItem, DyeColor color, Item coloredItem) {
        var baseItemId = BuiltInRegistries.ITEM.getKey(baseItem).toString();
        var coloredItemId = BuiltInRegistries.ITEM.getKey(coloredItem).toString();
        var coloredVersions = siteExport.coloredVersions.computeIfAbsent(baseItemId, key -> new HashMap<>());
        coloredVersions.put(color, coloredItemId);
    }

    public void addPage(ParsedGuidePage page) {
        var exportedPage = new ExportedPageJson();
        // Default to the title found in navigation when linking to this page,
        // but use the extracted h1-page title instead, otherwise
        if (page.getFrontmatter().navigationEntry() != null) {
            exportedPage.title = page.getFrontmatter().navigationEntry().title();
        } else {
            exportedPage.title = extractPageTitle(page);
            if (exportedPage.title.isEmpty()) {
                LOG.warn("Unable to determine page title for {}: {}", page.getId(), exportedPage.title);
            }
        }
        exportedPage.astRoot = page.getAstRoot();
        exportedPage.frontmatter.putAll(page.getFrontmatter().additionalProperties());

        siteExport.pages.put(page.getId(), exportedPage);
    }

    private String extractPageTitle(ParsedGuidePage page) {
        var pageTitle = new StringBuilder();
        page.getAstRoot().visit(new MdAstVisitor() {
            @Override
            public Result beforeNode(MdAstNode node) {
                if (node instanceof MdAstHeading heading) {
                    if (heading.depth == 1) {
                        pageTitle.append(heading.toText());
                    }
                    return Result.STOP;
                }
                return Result.CONTINUE;
            }
        });
        return pageTitle.toString();
    }

    public String addItem(ItemStack stack) {
        var itemId = stack.getItem().builtInRegistryHolder().key().location().toString().replace(':', '-');
        if (stack.getTag() == null) {
            return itemId;
        }

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        try (var out = new DataOutputStream(new DigestOutputStream(OutputStream.nullOutputStream(), digest))) {
            NbtIo.write(stack.getTag(), out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return itemId + "-" + HexFormat.of().formatHex(digest.digest());
    }

    public void addIndex(Guide guide, Class<? extends PageIndex> indexClass) {
        try (var jsonWriter = new JsonTreeWriter()) {
            var index = guide.getIndex(indexClass);
            index.export(jsonWriter);
            siteExport.pageIndices.put(indexClass.getName(), jsonWriter.get());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
