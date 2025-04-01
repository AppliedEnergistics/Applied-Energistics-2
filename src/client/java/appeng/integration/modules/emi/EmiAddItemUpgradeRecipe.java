package appeng.integration.modules.emi;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiStack;

import appeng.api.upgrades.IUpgradeableItem;
import appeng.core.AppEng;

public class EmiAddItemUpgradeRecipe extends EmiCraftingRecipe {
    public EmiAddItemUpgradeRecipe(IUpgradeableItem baseItem, Item upgrade) {
        super(
                List.of(EmiStack.of(baseItem), EmiStack.of(upgrade)),
                makeUpgraded(baseItem, upgrade),
                makeId(baseItem, upgrade),
                true);
    }

    private static ResourceLocation makeId(IUpgradeableItem baseItem, Item upgrade) {
        var baseItemId = BuiltInRegistries.ITEM.getKey(baseItem.asItem());
        var upgradeId = BuiltInRegistries.ITEM.getKey(upgrade.asItem());
        return AppEng.makeId("/add_item_upgrade/" + baseItemId.getPath() + "/" + upgradeId.getPath());
    }

    private static EmiStack makeUpgraded(IUpgradeableItem baseItem, Item upgrade) {
        var upgraded = new ItemStack(baseItem);
        baseItem.getUpgrades(upgraded).addItems(new ItemStack(upgrade));
        return EmiStack.of(upgraded);
    }
}
