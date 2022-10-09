package appeng.crafting.pattern;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.Level;

import appeng.api.stacks.AEItemKey;
import appeng.core.AELog;

/**
 * An item that contains an encoded {@link AESmithingTablePattern}.
 */
public class SmithingTablePatternItem extends EncodedPatternItem {
    public SmithingTablePatternItem(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public AESmithingTablePattern decode(ItemStack stack, Level level, boolean tryRecovery) {
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
    public AESmithingTablePattern decode(AEItemKey what, Level level) {
        if (what == null || !what.hasTag()) {
            return null;
        }

        try {
            return new AESmithingTablePattern(what, level);
        } catch (Exception e) {
            AELog.warn("Could not decode an invalid crafting pattern %s: %s", what.getTag(), e);
            return null;
        }
    }

    public ItemStack encode(UpgradeRecipe recipe, AEItemKey base, AEItemKey addition, AEItemKey out,
            boolean allowSubstitutes) {
        var stack = new ItemStack(this);
        SmithingTablePatternEncoding.encode(stack.getOrCreateTag(), recipe, base, addition, out, allowSubstitutes);
        return stack;
    }

    private boolean attemptRecovery(CompoundTag tag, Level level) {
        RecipeManager recipeManager = level.getRecipeManager();

        var base = SmithingTablePatternEncoding.getBase(tag);
        var addition = SmithingTablePatternEncoding.getAddition(tag);
        var output = SmithingTablePatternEncoding.getOutput(tag);
        if (base == null || addition == null || output == null) {
            return false; // Either input or output item was removed
        }

        var recipeId = SmithingTablePatternEncoding.getRecipeId(tag);

        // Fill a crafting inventory with the ingredients to find suitable recipes
        var testInventory = new SimpleContainer(2);
        testInventory.setItem(0, base.toStack());
        testInventory.setItem(1, addition.toStack());

        // Multiple recipes can match for stonecutting
        var recipe = recipeManager.getRecipeFor(RecipeType.SMITHING, testInventory, level).orElse(null);
        if (recipe == null) {
            AELog.info("Failed to recover encoded stonecutting pattern for recipe %s (no recipe for inputs)", recipeId);
            return false;
        }

        // Try to find the output in the potential recipe list
        if (!AEItemKey.matches(output, recipe.getResultItem())) {
            AELog.info("Failed to recover encoded stonecutting pattern for recipe %s (output mismatch)", recipeId);
            return false;
        }

        // Yay we found a match, reencode the pattern
        AELog.debug("Re-Encoding pattern from %s -> %s", recipeId, recipe.getId());
        SmithingTablePatternEncoding.encode(tag, recipe, base, addition, output,
                SmithingTablePatternEncoding.canSubstitute(tag));
        return true;
    }
}
