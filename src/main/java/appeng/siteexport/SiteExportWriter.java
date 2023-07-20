package appeng.siteexport;

import appeng.client.guidebook.Guide;
import appeng.client.guidebook.compiler.MdAstNodeAdapter;
import appeng.client.guidebook.compiler.ParsedGuidePage;
import appeng.client.guidebook.indices.ItemIndex;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.JsonTreeWriter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.material.Fluid;

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
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class SiteExportWriter {

    private abstract static class WriteOnlyTypeAdapter<T> extends TypeAdapter<T> {
        @Override
        public T read(JsonReader in) throws IOException {
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
            .registerTypeAdapter(Item.class, new WriteOnlyTypeAdapter<Item>() {
                @Override
                public void write(JsonWriter out, Item value) throws IOException {
                    out.value(BuiltInRegistries.ITEM.getKey(value).toString());
                }
            })
            .registerTypeAdapter(Fluid.class, new WriteOnlyTypeAdapter<Fluid>() {
                @Override
                public void write(JsonWriter out, Fluid value) throws IOException {
                    out.value(BuiltInRegistries.FLUID.getKey(value).toString());
                }
            })
            .create();
    private final SiteExportJson siteExport = new SiteExportJson();

    public SiteExportWriter(Guide guide) {
        siteExport.defaultNamespace = guide.getDefaultNamespace();
        siteExport.navigationRootNodes = guide.getNavigationTree().getRootNodes()
                .stream()
                .map(node -> NavigationNodeJson.of(this, node))
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

    public void addFluid(String id, FluidVariant fluid, String texturePath, float u1, float v1, float u2, float v2) {
        var fluidInfo = new FluidInfoJson();
        fluidInfo.id = id;
        fluidInfo.texture = texturePath;
        fluidInfo.u1 = u1;
        fluidInfo.v1 = v1;
        fluidInfo.u2 = u2;
        fluidInfo.v2 = v2;
        fluidInfo.color = "#" + HexFormat.of().toHexDigits(FluidVariantRendering.getColor(fluid), 8);
        fluidInfo.displayName = FluidVariantRendering.getTooltip(fluid).get(0).getString();
        siteExport.fluids.put(fluidInfo.id, fluidInfo);
    }

    public void addRecipe(CraftingRecipe recipe) {
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

        addRecipe(recipe.getId(), fields);
    }

    public void addRecipe(InscriberRecipe recipe) {
        var json = new InscriberRecipeJson();
        json.id = recipe.getId().toString();

        json.top = convertIngredient(recipe.getTopOptional());
        json.middle = convertIngredie nt(recipe.getMiddleInput());
        json.bottom = convertIngredient(recipe.getBottomOptional());
        json.resultItem = BuiltInRegistries.ITEM.getKey(recipe.getResultItem().getItem()).toString();
        json.resultCount = recipe.getResultItem().getCount();
        json.consumesTopAndBottom = recipe.getProcessType() == InscriberProcessType.PRESS;

        siteExport.inscriberRecipes.put(json.id, json);
    }

    public void addRecipe(AbstractCookingRecipe recipe) {
        var json = new SmeltingRecipeJson();
        json.id = recipe.getId().toString();

        json.resultItem = BuiltInRegistries.ITEM.getKey(recipe.getResultItem(null).getItem()).toString();

        var ingredients = recipe.getIngredients();
        json.ingredient = convertIngredient(ingredients.get(0));

        siteExport.smeltingRecipes.put(json.id, json);
    }

    public void addRecipe(TransformRecipe recipe) {
        var json = new TransformRecipeJson();
        json.id = recipe.getId().toString();

        json.resultItem = BuiltInRegistries.ITEM.getKey(recipe.getResultItem(null).getItem()).toString();

        var ingredients = recipe.getIngredients();
        json.ingredients = ingredients.stream().map(this::convertIngredient).toArray(String[][]::new);

        json.circumstance = recipe.circumstance.toJson();

        siteExport.transformRecipes.put(json.id, json);
    }

    public void addRecipe(EntropyRecipe recipe) {
        var json = new TransformRecipeJson();
        json.id = recipe.getId().toString();

        json.resultItem = BuiltInRegistries.ITEM.getKey(recipe.getResultItem(null).getItem()).toString();

        var ingredients = recipe.getIngredients();
        json.ingredients = ingredients.stream().map(this::convertIngredient).toArray(String[][]::new);

        json.circumstance = recipe.circumstance.toJson();

        siteExport.transformRecipes.put(json.id, json);
    }

    public void addRecipe(MatterCannonAmmo recipe) {
        var json = new TransformRecipeJson();
        json.id = recipe.getId().toString();

        json.resultItem = BuiltInRegistries.ITEM.getKey(recipe.getResultItem(null).getItem()).toString();

        var ingredients = recipe.getIngredients();
        json.ingredients = ingredients.stream().map(this::convertIngredient).toArray(String[][]::new);

        json.circumstance = recipe.circumstance.toJson();

        siteExport.transformRecipes.put(json.id, json);
    }

    public void addRecipe(ChargerRecipe recipe) {
        var json = new TransformRecipeJson();
        json.id = recipe.getId().toString();

        json.resultItem = BuiltInRegistries.ITEM.getKey(recipe.getResultItem(null).getItem()).toString();

        var ingredients = recipe.getIngredients();
        json.ingredients = ingredients.stream().map(this::convertIngredient).toArray(String[][]::new);

        json.circumstance = recipe.circumstance.toJson();

        siteExport.transformRecipes.put(json.id, json);
    }

    public void addRecipe(SmithingTransformRecipe recipe) {
        var json = new SmithingTransformRecipeJson();
        json.id = recipe.getId().toString();
        json.resultItem = BuiltInRegistries.ITEM.getKey(recipe.getResultItem(null).getItem()).toString();
        json.base = convertIngredient(recipe.base);
        json.addition = convertIngredient(recipe.addition);
        json.template = convertIngredient(recipe.template);
        siteExport.smithingTransformRecipes.put(json.id, json);
    }

    public void addRecipe(SmithingTrimRecipe recipe) {
        var json = new SmithingTrimRecipeJson();
        json.id = recipe.getId().toString();
        json.base = convertIngredient(recipe.base);
        json.addition = convertIngredient(recipe.addition);
        json.template = convertIngredient(recipe.template);
        siteExport.smithingTrimRecipes.put(json.id, json);
    }

    public void addRecipe(StonecutterRecipe recipe) {
        var json = new StonecutterRecipeJson();
        json.id = recipe.getId().toString();

        var resultItem = BuiltInRegistries.ITEM.getKey(recipe.getResultItem(null).getItem());

        var ingredients = recipe.getIngredients();
        json.ingredient = convertIngredient(ingredients.get(0));

        addRecipe(
                recipe.getId(),
                Map.of(
                    "resultItem", resultItem,
                    "ingredient", ingredients.get(0)
                )
        );
    }

    public void addRecipe(ResourceLocation id, Map<String, Object> element) {
        // Auto-transform ingredients

        var jsonElement = GSON.toJsonTree(element);

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
        exportedPage.title = "";
        exportedPage.astRoot = page.getAstRoot();
        exportedPage.frontmatter.putAll(page.getFrontmatter().additionalProperties());

        siteExport.pages.put(page.getId(), exportedPage);
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

    public void addIndex(Guide guide, Class<ItemIndex> indexClass) {
        try (var jsonWriter = new JsonTreeWriter()) {
            var index = guide.getIndex(indexClass);
            index.export(jsonWriter);
            siteExport.pageIndices.put(indexClass.getName(), jsonWriter.get());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
