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

import appeng.api.storage.data.IAEItemStack;
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

        return decode(stack.getOrCreateTag(), level, tryRecovery);
    }

    @Override
    public AECraftingPattern decode(CompoundTag tag, Level level, boolean tryRecovery) {
        // The recipe ids encoded in a pattern can go stale. This code attempts to find
        // the new id based on the stored inputs/outputs if that happens.
        var recipeId = AEPatternHelper.getRecipeId(tag);
        var recipe = level.getRecipeManager().byType(RecipeType.CRAFTING).get(recipeId);
        if (!(recipe instanceof CraftingRecipe) && (!tryRecovery || !attemptRecovery(tag, level))) {
            return null;
        }

        try {
            return new AECraftingPattern(tag.copy(), level);
        } catch (IllegalStateException e) {
            AELog.warn("Could not decode an invalid crafting pattern %s: %s", tag, e);
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
        IAEItemStack product = IAEItemStack.of(AEPatternHelper.getCraftingResult(tag));
        if (product == null) {
            return false;
        }

        ResourceLocation currentRecipeId = AEPatternHelper.getRecipeId(tag);

        // Fill a crafting inventory with the ingredients to find a suitable recipe
        CraftingContainer testInventory = new CraftingContainer(new NullMenu(), 3, 3);
        for (int x = 0; x < 9; x++) {
            var ais = x < ingredients.length ? ingredients[x] : null;
            var gs = ais != null ? ais.createItemStack() : ItemStack.EMPTY;
            testInventory.setItem(x, gs);
        }

        CraftingRecipe potentialRecipe = recipeManager.getRecipeFor(RecipeType.CRAFTING, testInventory, level)
                .orElse(null);

        // Check that it matches the expected output
        if (potentialRecipe != null && product.isSameType(potentialRecipe.assemble(testInventory))) {
            // Yay we found a match, reencode the pattern
            AELog.debug("Re-Encoding pattern from %s -> %s", currentRecipeId, potentialRecipe.getId());
            ItemStack[] in = Arrays.stream(ingredients)
                    .map(ais -> ais != null ? ais.createItemStack() : ItemStack.EMPTY)
                    .toArray(ItemStack[]::new);
            ItemStack out = product.createItemStack();
            AEPatternHelper.encodeCraftingPattern(tag, potentialRecipe, in, out,
                    AEPatternHelper.canSubstitute(tag), AEPatternHelper.canSubstituteFluids(tag));
        }

        AELog.debug("Failed to recover encoded crafting pattern for recipe %s", currentRecipeId);
        return false;
    }
}
