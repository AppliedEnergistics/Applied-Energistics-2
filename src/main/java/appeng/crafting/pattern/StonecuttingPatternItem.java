package appeng.crafting.pattern;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;

import appeng.api.stacks.AEItemKey;
import appeng.core.AELog;
import appeng.menu.AutoCraftingMenu;

/**
 * An item that contains an encoded {@link AEStonecuttingPattern}.
 */
public class StonecuttingPatternItem extends EncodedPatternItem {
    public StonecuttingPatternItem(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public AEStonecuttingPattern decode(ItemStack stack, Level level, boolean tryRecovery) {
        if (stack.getItem() != this || !stack.hasTag() || level == null) {
            return null;
        }

        var result = decode(AEItemKey.of(stack), level);

        if (tryRecovery && result == null) {
            var tag = stack.getOrCreateTag();
            if (attemptRecovery(tag, level)) {
                result = decode(stack, level, false);
            }
        }

        return result;
    }

    @Override
    public AEStonecuttingPattern decode(AEItemKey what, Level level) {
        if (what == null || !what.hasTag()) {
            return null;
        }

        try {
            return new AEStonecuttingPattern(what, level);
        } catch (Exception e) {
            AELog.warn("Could not decode an invalid crafting pattern %s: %s", what.getTag(), e);
            return null;
        }
    }

    public ItemStack encode(StonecutterRecipe recipe, AEItemKey in, AEItemKey out, boolean allowSubstitutes) {
        var stack = new ItemStack(this);
        StonecuttingPatternEncoding.encode(stack.getOrCreateTag(), recipe, in, out, allowSubstitutes);
        return stack;
    }

    private boolean attemptRecovery(CompoundTag tag, Level level) {
        RecipeManager recipeManager = level.getRecipeManager();

        var input = StonecuttingPatternEncoding.getInput(tag);
        var output = StonecuttingPatternEncoding.getOutput(tag);
        if (input == null || output == null) {
            return false; // Either input or output item was removed
        }

        var recipeId = StonecuttingPatternEncoding.getRecipeId(tag);

        // Fill a crafting inventory with the ingredients to find suitable recipes
        var testInventory = new CraftingContainer(new AutoCraftingMenu(), 1, 1);
        testInventory.setItem(0, input.toStack());

        // Multiple recipes can match for stonecutting
        var potentialRecipes = recipeManager.getRecipesFor(RecipeType.STONECUTTING, testInventory, level);

        // Try to find the output in the potential recipe list
        for (var potentialRecipe : potentialRecipes) {
            if (AEItemKey.matches(output, potentialRecipe.getResultItem())) {
                // Yay we found a match, reencode the pattern
                AELog.debug("Re-Encoding pattern from %s -> %s", recipeId, potentialRecipe.getId());
                StonecuttingPatternEncoding.encode(tag, potentialRecipe, input, output,
                        StonecuttingPatternEncoding.canSubstitute(tag));
            }
        }

        AELog.info("Failed to recover encoded stonecutting pattern for recipe %s", recipeId);
        return false;
    }
}
