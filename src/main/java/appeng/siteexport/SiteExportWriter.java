package appeng.siteexport;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import net.minecraft.DetectedVersion;
import net.minecraft.core.Registry;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class SiteExportWriter {

    private final SiteExportJson siteExport = new SiteExportJson();

    public SiteExportWriter() {
        siteExport.modVersion = ModVersion.get();
        siteExport.generated = Instant.now().toString();
        siteExport.gameVersion = DetectedVersion.tryDetectVersion().getName();
    }

    public void addSpriteSheet(String sheetPath, int width, int height, int rows, int cols, int margin) {
        var spriteSheet = new SpriteSheetJson();
        spriteSheet.filename = sheetPath;
        spriteSheet.width = width;
        spriteSheet.height = height;
        spriteSheet.cols = cols;
        spriteSheet.rows = rows;
        spriteSheet.margin = margin;
        siteExport.spriteSheet = spriteSheet;
    }

    public void add(ItemStack stack, int x, int y) {
        SpriteIndexEntryJson entry = new SpriteIndexEntryJson(stack, x, y);
        siteExport.spriteSheet.sprites.add(entry);
    }

    public void addRecipe(CraftingRecipe recipe) {
        RecipeJson json = new RecipeJson();
        json.id = recipe.getId().toString();
        json.shapeless = recipe instanceof ShapelessRecipe;

        json.resultItem = Registry.ITEM.getKey(recipe.getResultItem().getItem()).toString();
        json.resultCount = recipe.getResultItem().getCount();

        var ingredients = recipe.getIngredients();
        json.ingredients = new String[9][];
        for (int i = 0; i < json.ingredients.length; i++) {
            if (i < ingredients.size()) {
                json.ingredients[i] = Arrays.stream(ingredients.get(i).getItems())
                        .map(is -> Registry.ITEM.getKey(is.getItem()).toString())
                        .toArray(String[]::new);
            } else {
                json.ingredients[i] = new String[0];
            }
        }

        siteExport.recipes.put(json.id, json);
    }

    public void write(Path file) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(siteExport, writer);
        }
    }

    private static class SiteExportJson {
        public String generated;

        public String gameVersion;

        public String modVersion;

        public SpriteSheetJson spriteSheet;

        public Map<String, RecipeJson> recipes = new HashMap<>();
    }

    private static class RecipeJson {
        public boolean shapeless;
        public String id;
        public String[][] ingredients;
        public String resultItem;
        public int resultCount;
    }

    private static class SpriteSheetJson {
        public String filename;
        public int width;
        public int height;
        public int cols;
        public int rows;
        public int margin;
        public List<SpriteIndexEntryJson> sprites = new ArrayList<>();
    }

    private static class SpriteIndexEntryJson {
        private String itemId;
        private JsonElement tag;
        private int x;
        private int y;

        public SpriteIndexEntryJson(ItemStack is, int x, int y) {
            this.itemId = Registry.ITEM.getKey(is.getItem()).toString();
            if (is.hasTag()) {
                this.tag = NbtOps.INSTANCE.convertMap(JsonOps.INSTANCE, is.getTag());
            }
            this.x = x;
            this.y = y;
        }

        public String getItemId() {
            return itemId;
        }

        public JsonElement getTag() {
            return tag;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

    }

}
