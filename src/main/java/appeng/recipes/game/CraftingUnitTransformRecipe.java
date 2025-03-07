package appeng.recipes.game;

import appeng.recipes.AERecipeTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Used to handle upgrading and removal of upgrades for crafting units (in-world).
 */
public class CraftingUnitTransformRecipe implements Recipe<RecipeInput> {
    public static final MapCodec<CraftingUnitTransformRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder
            .group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("upgraded_block")
                            .forGetter(CraftingUnitTransformRecipe::getUpgradedBlock),
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("upgrade_item")
                            .forGetter(CraftingUnitTransformRecipe::getUpgradeItem))
            .apply(builder, CraftingUnitTransformRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingUnitTransformRecipe> STREAM_CODEC = StreamCodec
            .composite(
                    ByteBufCodecs.registry(BuiltInRegistries.BLOCK.key()),
                    CraftingUnitTransformRecipe::getUpgradedBlock,
                    ByteBufCodecs.registry(BuiltInRegistries.ITEM.key()),
                    CraftingUnitTransformRecipe::getUpgradeItem,
                    CraftingUnitTransformRecipe::new);

    private final Block upgradedBlock;
    private final Item upgradeItem;

    public CraftingUnitTransformRecipe(Block upgradedBlock, Item upgradeItem) {
        this.upgradedBlock = upgradedBlock;
        this.upgradeItem = upgradeItem;
    }

    public Block getUpgradedBlock() {
        return this.upgradedBlock;
    }

    public Item getUpgradeItem() {
        return upgradeItem;
    }

    /**
     * Gets the upgrade that would be returned from removing an upgrade from the given crafting block.
     *
     * @return Empty stack if no upgrade removal is possible.
     */
    public static ItemStack getRemovedUpgrade(ServerLevel level, Block upgradedBlock) {
        var recipeManager = level.recipeAccess();

        for (var holder : recipeManager.recipeMap().byType(AERecipeTypes.CRAFTING_UNIT_TRANSFORM)) {
            if (holder.value().upgradedBlock == upgradedBlock) {
                return holder.value().upgradeItem.getDefaultInstance();
            }
        }

        return ItemStack.EMPTY;
    }

    /**
     * Search for the resulting upgraded block when upgrading a crafting unit with the given upgrade item.
     */
    public static Block getUpgradedBlock(ServerLevel level, ItemStack upgradeItem) {
        for (var holder : level.recipeAccess().recipeMap().byType(AERecipeTypes.CRAFTING_UNIT_TRANSFORM)) {
            if (upgradeItem.is(holder.value().getUpgradeItem())) {
                return holder.value().upgradedBlock;
            }
        }
        return null;
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(
                new CraftingUnitTransformDisplay(upgradedBlock, new SlotDisplay.ItemSlotDisplay(upgradeItem))
        );
    }

    @Override
    public RecipeSerializer<CraftingUnitTransformRecipe> getSerializer() {
        return CraftingUnitTransformRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<CraftingUnitTransformRecipe> getType() {
        return AERecipeTypes.CRAFTING_UNIT_TRANSFORM;
    }
}
