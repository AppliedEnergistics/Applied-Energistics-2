package appeng.client.guidebook;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluids;

import guideme.siteexport.RecipeExporter;
import guideme.siteexport.ResourceExporter;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.items.tools.powered.MatterCannonItem;
import appeng.recipes.entropy.EntropyRecipe;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.mattercannon.MatterCannonAmmo;
import appeng.recipes.transform.TransformRecipe;

public class AERecipeExporter implements RecipeExporter {
    @Override
    public @Nullable Map<String, Object> convertToJson(ResourceKey<Recipe<?>> id, Recipe<?> recipe,
            ResourceExporter exporter) {

        if (recipe instanceof InscriberRecipe inscriberRecipe) {
            exporter.referenceItem(AEBlocks.INSCRIBER); // Ref items used as icons
            return exportRecipe(inscriberRecipe);
        } else if (recipe instanceof TransformRecipe transformRecipe) {
            // Ref items used as icons
            exporter.referenceFluid(Fluids.WATER);
            exporter.referenceFluid(Fluids.LAVA);
            exporter.referenceItem(Items.TNT);
            return exportRecipe(transformRecipe);
        } else if (recipe instanceof EntropyRecipe entropyRecipe) {
            exporter.referenceItem(AEItems.ENTROPY_MANIPULATOR); // Ref items used as icons
            return exportRecipe(entropyRecipe);
        } else if (recipe instanceof MatterCannonAmmo matterCannonAmmo) {
            exporter.referenceItem(AEItems.MATTER_CANNON); // Ref items used as icons
            return exportRecipe(matterCannonAmmo);
        } else if (recipe instanceof ChargerRecipe chargerRecipe) {
            exporter.referenceItem(AEBlocks.CHARGER); // Ref items used as icons
            return exportRecipe(chargerRecipe);
        }

        return null;
    }

    private Map<String, Object> exportRecipe(InscriberRecipe recipe) {
        var resultItem = recipe.getResultItem();
        return Map.of(
                "top", unwrapIngredient(recipe.getTopOptional()),
                "middle", recipe.getMiddleInput(),
                "bottom", unwrapIngredient(recipe.getBottomOptional()),
                "resultItem", resultItem.getItem(),
                "resultCount", resultItem.getCount(),
                "consumesTopAndBottom", recipe.getProcessType() == InscriberProcessType.PRESS);
    }

    private Map<String, Object> exportRecipe(TransformRecipe recipe) {

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

        return Map.of(
                "resultItem", recipe.getResultItem(),
                "ingredients", recipe.getIngredients(),
                "circumstance", circumstanceJson);
    }

    private Map<String, Object> exportRecipe(EntropyRecipe recipe) {
        return Map.of(
                "mode", recipe.getMode().name().toLowerCase(Locale.ROOT));
    }

    private Map<String, Object> exportRecipe(MatterCannonAmmo recipe) {
        return Map.of(
                "ammo", recipe.getAmmo(),
                "damage", MatterCannonItem.getDamageFromPenetration(recipe.getWeight()));
    }

    private Map<String, Object> exportRecipe(ChargerRecipe recipe) {
        return Map.of(
                "resultItem", recipe.getResultItem(),
                "ingredient", recipe.getIngredient());
    }

    private Object unwrapIngredient(Optional<Ingredient> ingredient) {
        return ingredient.isPresent() ? ingredient.get() : List.of();
    }
}
