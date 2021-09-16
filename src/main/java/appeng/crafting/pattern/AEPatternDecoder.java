package appeng.crafting.pattern;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetailsDecoder;
import appeng.core.AELog;
import appeng.items.misc.EncodedPatternItem;

public class AEPatternDecoder implements IPatternDetailsDecoder {
    public static final AEPatternDecoder INSTANCE = new AEPatternDecoder();

    @Override
    public boolean isEncodedPattern(ItemStack stack) {
        return stack.getItem() instanceof EncodedPatternItem && stack.hasTag();
    }

    @Nullable
    @Override
    public IAEPatternDetails decodePattern(ItemStack stack, Level level, boolean autoRecovery) {
        if (stack == null || level == null || !isEncodedPattern(stack)) {
            return null;
        }

        // The recipe ids encoded in a pattern can go stale. This code attempts to find
        // the new id based on the stored inputs/outputs if that happens.
        if (AEPatternHelper.isCrafting(stack.getTag())) {
            var recipeId = AEPatternHelper.getRecipeId(stack.getTag());
            Recipe<?> recipe = level.getRecipeManager().byType(RecipeType.CRAFTING).get(recipeId);
            if (!(recipe instanceof CraftingRecipe) && (!autoRecovery || !attemptRecovery(stack, level))) {
                return null;
            }
        }

        try {
            if (AEPatternHelper.isCrafting(stack.getTag())) {
                return new AECraftingPattern(stack, level);
            } else {
                return new AEProcessingPattern(stack);
            }
        } catch (IllegalStateException e) {
            AELog.warn("Could not decode an invalid pattern %s: %s", stack, e);
            return null;
        }
    }

    // TODO: restore
    private boolean attemptRecovery(ItemStack itemStack, Level level) {
        /*
         * RecipeManager recipeManager = level.getRecipeManager();
         * 
         * List<IAEItemStack> ingredients = patternItem.getIngredients(itemStack); List<IAEItemStack> products =
         * patternItem.getProducts(itemStack); if (ingredients.size() < 9 || products.size() < 1) { return false; }
         * 
         * ResourceLocation currentRecipeId = patternItem.getCraftingRecipeId(itemStack);
         * 
         * // Fill a crafting inventory with the ingredients to find a suitable recipe CraftingContainer testInventory =
         * new CraftingContainer(new NullMenu(), 3, 3); for (int x = 0; x < 9; x++) { final IAEItemStack ais =
         * ingredients.get(x); final ItemStack gs = ais != null ? ais.createItemStack() : ItemStack.EMPTY;
         * testInventory.setItem(x, gs); }
         * 
         * CraftingRecipe potentialRecipe = recipeManager.getRecipeFor(RecipeType.CRAFTING, testInventory, level)
         * .orElse(null);
         * 
         * // Check that it matches the expected output if (potentialRecipe != null &&
         * products.get(0).isSameType(potentialRecipe.assemble(testInventory))) { // Yay we found a match, reencode the
         * pattern AELog.debug("Re-Encoding pattern from %s -> %s", currentRecipeId, potentialRecipe.getId());
         * ItemStack[] in = ingredients.stream().map(ais -> ais != null ? ais.createItemStack() : ItemStack.EMPTY)
         * .toArray(ItemStack[]::new); ItemStack out = products.get(0).createItemStack();
         * AEPatternHelper.encodeCraftingPattern(itemStack, potentialRecipe, in, out,
         * patternItem.allowsSubstitution(itemStack)); }
         * 
         * AELog.debug("Failed to recover encoded crafting pattern for recipe %s", currentRecipeId);
         */
        return false;
    }

    private static EncodedPatternItem getPatternItem(ItemStack itemStack) {
        if (itemStack.getItem() instanceof EncodedPatternItem) {
            return (EncodedPatternItem) itemStack.getItem();
        }
        return null;
    }
}
