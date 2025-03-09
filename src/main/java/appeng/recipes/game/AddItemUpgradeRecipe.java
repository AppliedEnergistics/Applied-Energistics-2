package appeng.recipes.game;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import appeng.api.upgrades.IUpgradeableItem;

/**
 * Allows adding upgrades to upgradable items.
 */
public class AddItemUpgradeRecipe extends CustomRecipe {
    public static final AddItemUpgradeRecipe INSTANCE = new AddItemUpgradeRecipe();

    private AddItemUpgradeRecipe() {
        super(CraftingBookCategory.MISC);
    }

    public static final MapCodec<AddItemUpgradeRecipe> CODEC = MapCodec.unit(INSTANCE);

    public static final StreamCodec<RegistryFriendlyByteBuf, AddItemUpgradeRecipe> STREAM_CODEC = StreamCodec
            .unit(INSTANCE);

    private static ItemStack attemptUpgrade(CraftingInput input) {
        if (input.ingredientCount() < 2) {
            return ItemStack.EMPTY;
        }

        // Find the upgradable item
        for (int i = 0; i < input.size(); i++) {
            var stack = input.getItem(i);
            if (stack.getItem() instanceof IUpgradeableItem upgradableItem) {
                var upgraded = stack.copy();
                var upgrades = upgradableItem.getUpgrades(upgraded);

                for (int j = 0; j < input.size(); j++) {
                    if (j == i) {
                        continue;
                    }
                    var upgrade = input.getItem(j);
                    if (upgrade.isEmpty()) {
                        continue;
                    }
                    // Since we do not like unexpectedly adding all upgrades at once,
                    // we copy with count=1
                    upgrade = upgrade.copyWithCount(1);
                    // If we can't insert one of the items, break
                    if (!upgrades.addItems(upgrade).isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                }

                return upgraded;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean matches(CraftingInput container, Level level) {
        return !attemptUpgrade(container).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput container, HolderLookup.Provider registries) {
        return attemptUpgrade(container);
    }

    @Override
    public RecipeSerializer<AddItemUpgradeRecipe> getSerializer() {
        return AddItemUpgradeRecipeSerializer.INSTANCE;
    }

}
