package appeng.items.tools.fluix;

import java.util.function.Supplier;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import appeng.core.AppEng;
import appeng.datagen.providers.tags.ConventionTags;

public enum FluixToolType {
    FLUIX("fluix", () -> Ingredient.of(ConventionTags.FLUIX_CRYSTAL)),
    ;

    private final String name;
    private final Tier toolTier;

    FluixToolType(String name, Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.toolTier = new Tier() {
            @Override
            public int getUses() {
                return Tiers.IRON.getUses() * 3;
            }

            @Override
            public float getSpeed() {
                return Tiers.IRON.getSpeed() * 1.2F;
            }

            @Override
            public float getAttackDamageBonus() {
                return Tiers.IRON.getAttackDamageBonus() * 1.2F;
            }

            @Override
            public TagKey<Block> getIncorrectBlocksForDrops() {
                return Tiers.IRON.getIncorrectBlocksForDrops();
            }

            @Override
            public int getEnchantmentValue() {
                return Tiers.IRON.getEnchantmentValue();
            }

            @Override
            public Ingredient getRepairIngredient() {
                return repairIngredient.get();
            }

            // This allows mods like LevelZ to identify our tools.
            @Override
            public String toString() {
                return AppEng.MOD_ID + ":" + name;
            }
        };
    }

    public final String getName() {
        return name;
    }

    public final Tier getToolTier() {
        return toolTier;
    }
}
