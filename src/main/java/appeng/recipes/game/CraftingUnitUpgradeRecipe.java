package appeng.recipes.game;

import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.recipes.AERecipeTypes;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootParams;

import java.util.List;
import java.util.Optional;

public class CraftingUnitUpgradeRecipe implements Recipe<RecipeInput> {
    @Deprecated(forRemoval = true, since = "1.21.1")
    public static final ResourceLocation TYPE_ID = AppEng.makeId("crafting_unit_upgrade");

    @Deprecated(forRemoval = true, since = "1.21.1")
    public static final RecipeType<CraftingUnitUpgradeRecipe> TYPE = AERecipeTypes.UNIT_UPGRADE;

    public static final MapCodec<CraftingUnitUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) -> {
        return builder.group(
            ResourceLocation.CODEC.fieldOf("block").forGetter(CraftingUnitUpgradeRecipe::getBlock),
            BuiltInRegistries.ITEM.byNameCodec().listOf().optionalFieldOf("upgrade_items").forGetter(it -> Optional.ofNullable(it.getUpgradeItems())),
            ItemStack.CODEC.listOf().optionalFieldOf("disassembly_items").forGetter(it -> Optional.ofNullable(it.getDisassemblyItems())),
            ResourceLocation.CODEC.optionalFieldOf("disassembly_loot_table").forGetter(it -> Optional.ofNullable(it.getDisassemblyLootTable()))
        ).apply(builder, (block, upgradeItems, disassemblyItems, disassemblyLoot) ->
            new CraftingUnitUpgradeRecipe(block, upgradeItems.orElse(null), disassemblyItems.orElse(null), disassemblyLoot.orElse(null))
        );
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingUnitUpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC,
        CraftingUnitUpgradeRecipe::getBlock,
        ByteBufCodecs.registry(BuiltInRegistries.ITEM.key()).apply(ByteBufCodecs.list()).apply(ByteBufCodecs::optional),
        it -> Optional.ofNullable(it.getUpgradeItems()),
        ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs::optional),
        it -> Optional.ofNullable(it.getDisassemblyItems()),
        ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional),
        it -> Optional.ofNullable(it.getDisassemblyLootTable()),
        (block, upgradeItems, disassemblyItems, disassemblyLoot) ->
            new CraftingUnitUpgradeRecipe(block, upgradeItems.orElse(null), disassemblyItems.orElse(null), disassemblyLoot.orElse(null))
    );

    private final ResourceLocation disassemblyLootTable;
    private final List<ItemStack> disassemblyItems;
    private final List<Item> upgradeItems;
    private final ResourceLocation block;

    public CraftingUnitUpgradeRecipe(ResourceLocation block, List<Item> upgradeItems, List<ItemStack> disassemblyItems, ResourceLocation lootTable) {
        this.upgradeItems = ImmutableList.copyOf(upgradeItems);
        this.disassemblyItems = disassemblyItems;
        this.disassemblyLootTable = lootTable;
        this.block = block;
    }

    public ResourceLocation getBlock() {
        return this.block;
    }

    public List<Item> getUpgradeItems() {
        return upgradeItems;
    }

    public List<ItemStack> getDisassemblyItems() {
        return disassemblyItems.stream().map(ItemStack::copy).toList();
    }

    public ResourceLocation getDisassemblyLootTable() {
        return this.disassemblyLootTable;
    }

    public List<ItemStack> getDisassemblyLoot(Level level, LootParams params) {
        if (this.disassemblyLootTable == null || level.isClientSide()) return null;
        return level
            .getServer()
            .reloadableRegistries()
            .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, disassemblyLootTable))
            .getRandomItems(params, level.getRandom());
    }

    /**
     * @return True when any Disassembly Output is specified.
     */
    public boolean canDisassemble() {
        return this.disassemblyLootTable != null || (this.disassemblyItems != null && !this.disassemblyItems.isEmpty());
    }

    /**
     * @return True when Disassembly Items aren't specified in the recipe.
     */
    public boolean useLootTable() {
        return this.disassemblyItems == null || this.disassemblyItems.isEmpty();
    }

    /**
     * @return True when Upgrade Items are specified in the recipe.
     */
    public boolean canUpgrade() {
        return this.upgradeItems != null && !this.upgradeItems.isEmpty();
    }

    /**
     * @param stack ItemStack to compare
     * @return True if this recipe matches the provided stack, otherwise false.
     */
    public boolean canUpgradeWith(ItemStack stack) {
        return canUpgrade() && this.upgradeItems.contains(stack.getItem());
    }

    /**
     * Used to get the disassembly recipe based on the provided ResourceLocation. If not found will do a lookup for recipes that specify provided block.
     * @param level
     * @param location ResourceLocation of the recipe to get.
     * @param block Fallback ResourceLocation to look for.
     * @return If a single recipe is found - CraftingUnitUpgradeRecipe, otherwise null.
     */
    public static CraftingUnitUpgradeRecipe getDisassemblyRecipe(Level level, ResourceLocation location, ResourceLocation block) {
        var recipeManager = level.getRecipeManager();
        var recipeHolder = recipeManager.byKey(location);
        if (recipeHolder.isEmpty()) {
            var recipes = recipeManager.byType(AERecipeTypes.UNIT_UPGRADE).stream().filter(it -> it.value().getBlock() == block && it.value().canDisassemble()).toList();
            if (recipes.size() != 1) {
                if (recipes.size() > 1) {
                    AELog.debug("Multiple disassembly recipes found for {}. Disassembly is impossible.", block);
                    recipes.forEach(recipe -> AELog.debug("Recipe: {}", recipe.id()));
                }
                return null;
            }
            return recipes.getFirst().value();
        }

        if (recipeHolder.get().value() instanceof CraftingUnitUpgradeRecipe recipe) return recipe;
        return null;
    }

    /**
     * Used to get the upgrade recipe for the provided ItemStack.
     * @param level
     * @param upgradeItem ItemStack to upgrade with.
     * @return If a single recipe is found - CraftingUnitUpgradeRecipe, otherwise null.
     */
    public static CraftingUnitUpgradeRecipe getUpgradeRecipe(Level level, ItemStack upgradeItem) {
        List<RecipeHolder<CraftingUnitUpgradeRecipe>> recipes = level.getRecipeManager()
            .byType(AERecipeTypes.UNIT_UPGRADE)
            .stream()
            .filter(it -> it.value().canUpgradeWith(upgradeItem))
            .toList();

        if (recipes.size() != 1) {
            if (recipes.size() > 1) {
                AELog.debug("Multiple upgrade recipes found for item {}. Upgrade is impossible.", upgradeItem.getItem());
                recipes.forEach(recipe -> AELog.debug("Recipe: {}", recipe.id()));
            }
            return null;
        }

        return recipes.getFirst().value();
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
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CraftingUnitUpgradeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }
}
