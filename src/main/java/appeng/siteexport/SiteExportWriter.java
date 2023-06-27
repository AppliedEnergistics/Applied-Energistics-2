package appeng.siteexport;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.JsonTreeWriter;

import org.jetbrains.annotations.NotNull;

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

import appeng.client.guidebook.Guide;
import appeng.client.guidebook.compiler.MdAstNodeAdapter;
import appeng.client.guidebook.compiler.ParsedGuidePage;
import appeng.client.guidebook.indices.ItemIndex;
import appeng.core.AppEng;
import appeng.libs.mdast.model.MdAstNode;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.siteexport.model.CraftingRecipeJson;
import appeng.siteexport.model.ExportedPageJson;
import appeng.siteexport.model.InscriberRecipeJson;
import appeng.siteexport.model.ItemInfoJson;
import appeng.siteexport.model.NavigationNodeJson;
import appeng.siteexport.model.P2PTypeInfo;
import appeng.siteexport.model.SiteExportJson;
import appeng.siteexport.model.SmeltingRecipeJson;

public class SiteExportWriter {

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

    public void addRecipe(CraftingRecipe recipe) {
        var json = new CraftingRecipeJson();
        json.id = recipe.getId().toString();
        json.shapeless = true;
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            json.shapeless = false;
            json.width = shapedRecipe.getWidth();
            json.height = shapedRecipe.getHeight();
        }

        json.resultItem = BuiltInRegistries.ITEM.getKey(recipe.getResultItem(null).getItem()).toString();
        json.resultCount = recipe.getResultItem(null).getCount();

        var ingredients = recipe.getIngredients();
        json.ingredients = new String[ingredients.size()][];
        for (int i = 0; i < json.ingredients.length; i++) {
            json.ingredients[i] = convertIngredient(ingredients.get(i));
        }

        siteExport.craftingRecipes.put(json.id, json);
    }

    public void addRecipe(InscriberRecipe recipe) {
        var json = new InscriberRecipeJson();
        json.id = recipe.getId().toString();

        json.top = convertIngredient(recipe.getTopOptional());
        json.middle = convertIngredient(recipe.getMiddleInput());
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

    @NotNull
    private String[] convertIngredient(Ingredient ingredient) {
        return Arrays.stream(ingredient.getItems())
                .map(is -> BuiltInRegistries.ITEM.getKey(is.getItem()))
                .filter(k -> k.getNamespace().equals(AppEng.MOD_ID) || k.getNamespace().equals("minecraft"))
                .map(ResourceLocation::toString)
                .toArray(String[]::new);
    }

    public void write(Path file) throws IOException {
        var gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .registerTypeHierarchyAdapter(MdAstNode.class, new MdAstNodeAdapter())
                .create();

        try (var out = new GZIPOutputStream(Files.newOutputStream(file));
                var writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            gson.toJson(siteExport, writer);
        }
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

    public ExportedPageJson addPage(ParsedGuidePage page) {
        var exportedPage = new ExportedPageJson();
        exportedPage.title = "";
        exportedPage.astRoot = page.getAstRoot();
        exportedPage.frontmatter.putAll(page.getFrontmatter().additionalProperties());

        siteExport.pages.put(page.getId(), exportedPage);
        return exportedPage;
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
