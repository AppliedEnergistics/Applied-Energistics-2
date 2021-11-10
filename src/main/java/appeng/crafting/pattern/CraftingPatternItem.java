package appeng.crafting.pattern;

import java.util.Arrays;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import appeng.api.storage.data.AEItemKey;
import appeng.core.AELog;
import appeng.menu.NullMenu;

/**
 * An item that contains an encoded {@link AECraftingPattern}.
 */
public class CraftingPatternItem extends EncodedPatternItem {
    public CraftingPatternItem(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public AECraftingPattern decode(ItemStack stack, Level level, boolean tryRecovery) {
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
    public AECraftingPattern decode(AEItemKey what, Level level) {
        if (what == null || !what.hasTag()) {
            return null;
        }

        try {
            return new AECraftingPattern(what, level);
        } catch (Exception e) {
            AELog.warn("Could not decode an invalid crafting pattern %s: %s", what.getTag(), e);
            return null;
        }
    }

    public ItemStack encode(CraftingRecipe recipe, ItemStack[] in, ItemStack out, boolean allowSubstitutes,
            boolean allowFluidSubstitutes) {
        var stack = new ItemStack(this);
        AEPatternHelper.encodeCraftingPattern(stack.getOrCreateTag(), recipe, in, out, allowSubstitutes,
                allowFluidSubstitutes);
        return stack;
    }

    private boolean attemptRecovery(CompoundTag tag, Level level) {
        RecipeManager recipeManager = level.getRecipeManager();

        var ingredients = AEPatternHelper.getCraftingInputs(tag);
        var product = AEPatternHelper.getCraftingResult(tag);
        if (product.isEmpty()) {
            return false;
        }

        ResourceLocation currentRecipeId = AEPatternHelper.getRecipeId(tag);

        // Fill a crafting inventory with the ingredients to find a suitable recipe
        CraftingContainer testInventory = new CraftingContainer(new NullMenu(), 3, 3);
        for (int x = 0; x < 9; x++) {
            var ais = x < ingredients.length ? ingredients[x] : null;
            if (ais.what() instanceof AEItemKey itemKey) {
                testInventory.setItem(x, itemKey.toStack());
            }
        }

        CraftingRecipe potentialRecipe = recipeManager.getRecipeFor(RecipeType.CRAFTING, testInventory, level)
                .orElse(null);

        // Check that it matches the expected output
        if (potentialRecipe != null && ItemStack.isSameItemSameTags(product, potentialRecipe.assemble(testInventory))) {
            // Yay we found a match, reencode the pattern
            AELog.debug("Re-Encoding pattern from %s -> %s", currentRecipeId, potentialRecipe.getId());
            ItemStack[] in = Arrays.stream(ingredients)
                    .map(stack -> stack.what() instanceof AEItemKey itemKey ? itemKey.toStack() : ItemStack.EMPTY)
                    .toArray(ItemStack[]::new);
            AEPatternHelper.encodeCraftingPattern(tag, potentialRecipe, in, product,
                    AEPatternHelper.canSubstitute(tag), AEPatternHelper.canSubstituteFluids(tag));
        }

        AELog.debug("Failed to recover encoded crafting pattern for recipe %s", currentRecipeId);
        return false;
    }
}
