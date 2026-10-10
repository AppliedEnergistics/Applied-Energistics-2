package appeng.client.guidebook;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.resources.Identifier;

import guideme.web.ExportedRecipe;
import guideme.web.RecipeWebRenderer;
import guideme.web.RecipeWebRenderingContext;
import guideme.web.html.HtmlNode;

import appeng.core.AppEng;

/**
 * Renders AE2 recipes for the website version of the guide.
 */
public class AERecipeWebRenderer implements RecipeWebRenderer {
    private static final Identifier INSCRIBER_ARROWS = AppEng
            .makeId("textures/gui/sprites/guide/inscriber_arrows_bg.png");

    @Override
    public Set<String> getSupportedTypes() {
        return Set.of(
                "ae2:inscriber",
                "ae2:transform",
                "ae2:charger");
    }

    @Override
    public void render(RecipeWebRenderingContext builder, ExportedRecipe recipe) {
        builder.requireStylesheet("appeng/web/recipes.css");
        switch (recipe.type()) {
            case "ae2:inscriber" -> renderInscriber(builder, recipe);
            case "ae2:transform" -> renderTransform(builder, recipe);
            case "ae2:charger" -> builder.recipeBox(machineItemId(builder, "charger"), "Charger", recipe.resultItem())
                    .slot(recipe.getIngredient("ingredient"))
                    .arrow()
                    .resultSlot()
                    .build();
            default -> throw new IllegalArgumentException("Unsupported recipe type " + recipe.type());
        }
    }

    /**
     * Uses the same layout as {@link LytInscriberRecipe}.
     */
    private static void renderInscriber(RecipeWebRenderingContext builder, ExportedRecipe recipe) {
        var layout = HtmlNode.tag("div")
                .setClassName("ae2-inscriber")
                .setStyles(Map.of("background-image", "url(\"" + builder.getAssetUrl(INSCRIBER_ARROWS) + "\")"))
                .append(inscriberSlot(builder, "top", recipe.getIngredient("top")))
                .append(inscriberSlot(builder, "middle", recipe.getIngredient("middle")))
                .append(inscriberSlot(builder, "bottom", recipe.getIngredient("bottom")))
                .append(inscriberSlot(builder, "result", List.of(recipe.resultItem())));

        builder.recipeBox(machineItemId(builder, "inscriber"), "Inscriber", recipe.resultItem())
                .append(layout)
                .build();
    }

    /**
     * Guides exported before Minecraft 1.18 used appliedenergistics2 as the namespace of AE2 items.
     */
    private static String machineItemId(RecipeWebRenderingContext builder, String path) {
        return builder.guide().getDefaultNamespace() + ":" + path;
    }

    private static HtmlNode inscriberSlot(RecipeWebRenderingContext builder, String position, List<String> itemIds) {
        return HtmlNode.tag("div")
                .setClassName("ae2-inscriber-" + position)
                .append(builder.slotHtml(itemIds));
    }

    @SuppressWarnings("unchecked")
    private static void renderTransform(RecipeWebRenderingContext builder, ExportedRecipe recipe) {
        var circumstance = (Map<String, Object>) recipe.fields().get("circumstance");
        var type = circumstance != null ? String.valueOf(circumstance.get("type")) : "";

        RecipeWebRenderingContext.RecipeBoxBuilder recipeBox;
        switch (type) {
            case "explosion" -> recipeBox = builder.recipeBox("minecraft:tnt", "Explode", recipe.resultItem());
            case "fluid" ->
                recipeBox = builder.recipeBox(fluidHeader(builder, (List<String>) circumstance.get("fluids")));
            default -> {
                builder.recipeBox(builder.compileError("Unsupported transform circumstance: " + type)).build();
                return;
            }
        }

        var ingredients = (List<List<String>>) recipe.fields().get("ingredients");
        recipeBox.shapelessSlots(ingredients != null ? ingredients : List.of())
                .arrow()
                .resultSlot()
                .build();
    }

    /**
     * Shows the fluids the transformation can be done in, cycling through them if there are multiple.
     */
    private static HtmlNode fluidHeader(RecipeWebRenderingContext builder, List<String> fluids) {
        if (fluids == null || fluids.isEmpty()) {
            return builder.compileError("No fluids in transform recipe");
        }

        var header = HtmlNode.tag("div");
        if (fluids.size() > 1) {
            header.setClassName("cycling");
        }
        for (var fluidId : fluids) {
            var fluidInfo = builder.guide().tryGetFluidInfo(fluidId);
            var fluidName = fluidInfo != null ? fluidInfo.displayName() : fluidId;
            header.append(HtmlNode.tag("span")
                    .append(builder.fluidIcon(fluidId))
                    .append(" Throw in " + fluidName));
        }
        return header;
    }
}
