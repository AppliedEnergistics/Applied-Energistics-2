package appeng.siteexport;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.DetectedVersion;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import appeng.siteexport.model.CraftingRecipeJson;
import appeng.siteexport.model.ItemInfoJson;
import appeng.siteexport.model.SiteExportJson;

public class SiteExportWriter {

    private final SiteExportJson siteExport = new SiteExportJson();

    public SiteExportWriter() {
        siteExport.modVersion = ModVersion.get();
        siteExport.generated = Instant.now().toString();
        siteExport.gameVersion = DetectedVersion.tryDetectVersion().getName();
    }

    public void addItem(String id, ItemStack stack, String iconPath) {
        var tooltipLines = stack.getTooltipLines(null, TooltipFlag.Default.NORMAL);
        var itemInfo = new ItemInfoJson();
        itemInfo.id = id;
        itemInfo.icon = iconPath;
        itemInfo.displayName = stack.getHoverName().getString();
        itemInfo.tooltipLines = tooltipLines.stream().map(this::resolveLine).toList();

        siteExport.items.put(itemInfo.id, itemInfo);
    }

    private String resolveLine(Component component) {
        var builder = new StringBuilder();
        component.visit((style, text) -> {
            if (style.isEmpty() || text.isEmpty()) {
                builder.append(text);
            } else {
                var classes = new StringBuilder();
                if (style.isBold()) {
                    classes.append(" b");
                }
                if (style.isItalic()) {
                    classes.append(" i");
                }
                if (style.isUnderlined()) {
                    classes.append(" u");
                }
                if (style.isStrikethrough()) {
                    classes.append(" s");
                }
                var color = style.getColor();
                if (color != null) {
                    classes.append(" c-").append(color.serialize());
                }
                if (classes.length() > 0) {
                    classes.deleteCharAt(0);
                }
                builder.append("<span class=\"").append(classes).append("\">")
                        .append(text)
                        .append("</span>");
            }
            return Optional.empty();
        }, Style.EMPTY);

        return builder.toString();
    }

    public void addRecipe(CraftingRecipe recipe) {
        var json = new CraftingRecipeJson();
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

        siteExport.craftingRecipes.put(json.id, json);
    }

    public void write(Path file) throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(siteExport, writer);
        }
    }

}
