package appeng.siteexport;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

import guideme.Guide;
import guideme.internal.siteexport.SiteExporter;

import appeng.core.definitions.AEBlocks;
import appeng.items.tools.powered.MatterCannonItem;
import appeng.recipes.entropy.EntropyRecipe;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.mattercannon.MatterCannonAmmo;
import appeng.recipes.transform.TransformRecipe;

public class AESiteExporter extends SiteExporter {
    public AESiteExporter(Minecraft client, Path outputFolder, Guide guide) {
        super(client, outputFolder, guide);

        // Ref items used as icons
        referenceItem(Items.FURNACE);
        referenceItem(AEBlocks.INSCRIBER);
        referenceFluid(Fluids.WATER);
        referenceFluid(Fluids.LAVA);
        referenceItem(Items.TNT);
        referenceItem(Blocks.SMITHING_TABLE);
    }

    @Nullable
    protected Map<String, Object> getCustomRecipeFields(ResourceLocation id, Recipe<?> recipe) {
        if (recipe instanceof InscriberRecipe inscriberRecipe) {
            return addRecipe(inscriberRecipe);
        } else if (recipe instanceof TransformRecipe transformRecipe) {
            return addRecipe(transformRecipe);
        } else if (recipe instanceof EntropyRecipe entropyRecipe) {
            return addRecipe(entropyRecipe);
        } else if (recipe instanceof MatterCannonAmmo ammoRecipe) {
            return addRecipe(ammoRecipe);
        } else if (recipe instanceof ChargerRecipe chargerRecipe) {
            return addRecipe(chargerRecipe);
        }
        return null;
    }

    private Map<String, Object> addRecipe(InscriberRecipe recipe) {
        var resultItem = recipe.getResultItem();
        return Map.of(
                "top", recipe.getTopOptional(),
                "middle", recipe.getMiddleInput(),
                "bottom", recipe.getBottomOptional(),
                "resultItem", resultItem.getItem(),
                "resultCount", resultItem.getCount(),
                "consumesTopAndBottom", recipe.getProcessType() == InscriberProcessType.PRESS);
    }

    private Map<String, Object> addRecipe(TransformRecipe recipe) {

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
                "resultItem", recipe.getResultItem(null),
                "ingredients", recipe.getIngredients(),
                "circumstance", circumstanceJson);
    }

    private Map<String, Object> addRecipe(EntropyRecipe recipe) {
        return Map.of(
                "mode", recipe.getMode().name().toLowerCase(Locale.ROOT));
    }

    private Map<String, Object> addRecipe(MatterCannonAmmo recipe) {
        return Map.of(
                "ammo", recipe.getAmmo(),
                "damage", MatterCannonItem.getDamageFromPenetration(recipe.getWeight()));
    }

    private Map<String, Object> addRecipe(ChargerRecipe recipe) {
        return Map.of(
                "resultItem", recipe.getResultItem(),
                "ingredient", recipe.getIngredient());
    }
}
