package appeng.items.tools.fluix;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

import appeng.datagen.providers.tags.ConventionTags;

public enum FluixToolType {
    FLUIX("fluix", ConventionTags.FLUIX_CRYSTAL),
    ;

    private final String name;
    private final TagKey<Item> repairIngredient;
    private final ToolMaterial material;

    FluixToolType(String name, TagKey<Item> repairIngredient) {
        this.name = name;
        this.repairIngredient = repairIngredient;
        this.material = new ToolMaterial(
                BlockTags.INCORRECT_FOR_IRON_TOOL, 250 * 3, 6.0F * 1.2F, 2.0F * 1.2F, 14, ItemTags.IRON_TOOL_MATERIALS);
    }

    public final String getName() {
        return name;
    }

    public TagKey<Item> getRepairIngredient() {
        return repairIngredient;
    }

    public final ToolMaterial getMaterial() {
        return material;
    }
}
