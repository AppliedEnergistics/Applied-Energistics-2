package appeng.recipes.game;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import appeng.core.AppEng;

/**
 * Allows swapping out the storage component of a cell without having to empty it first.
 */
public class StorageCellUpgradeRecipe extends CustomRecipe {
    public static final ResourceLocation SERIALIZER_ID = AppEng.makeId("storage_cell_upgrade");

    private final Item inputCell;
    private final Item inputComponent;
    private final Item resultCell;
    private final Item resultComponent;

    public StorageCellUpgradeRecipe(Item inputCell, Item inputComponent, Item resultCell, Item resultComponent) {
        super(CraftingBookCategory.MISC);
        this.inputCell = inputCell;
        this.inputComponent = inputComponent;
        this.resultCell = resultCell;
        this.resultComponent = resultComponent;
    }

    public static final MapCodec<StorageCellUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("input_cell")
                    .forGetter(StorageCellUpgradeRecipe::getInputCell),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("input_component")
                    .forGetter(StorageCellUpgradeRecipe::getInputComponent),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("result_cell")
                    .forGetter(StorageCellUpgradeRecipe::getResultCell),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("result_component")
                    .forGetter(StorageCellUpgradeRecipe::getResultComponent))
            .apply(builder, StorageCellUpgradeRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StorageCellUpgradeRecipe> STREAM_CODEC = StreamCodec
            .composite(
                    ByteBufCodecs.registry(Registries.ITEM), StorageCellUpgradeRecipe::getInputCell,
                    ByteBufCodecs.registry(Registries.ITEM), StorageCellUpgradeRecipe::getInputComponent,
                    ByteBufCodecs.registry(Registries.ITEM), StorageCellUpgradeRecipe::getResultCell,
                    ByteBufCodecs.registry(Registries.ITEM), StorageCellUpgradeRecipe::getResultComponent,
                    StorageCellUpgradeRecipe::new);

    public Item getInputCell() {
        return inputCell;
    }

    public Item getInputComponent() {
        return inputComponent;
    }

    public Item getResultCell() {
        return resultCell;
    }

    public Item getResultComponent() {
        return resultComponent;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, Ingredient.of(inputCell), Ingredient.of(inputComponent));
    }

    @Override
    public boolean matches(CraftingInput container, Level level) {
        var cellsFound = 0;
        var componentsFound = 0;

        for (int i = 0; i < container.size(); i++) {
            var stack = container.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.is(inputCell)) {
                    // Also bail out if somehow someone managed to stack cells (since we replace it with a component)
                    cellsFound += stack.getCount();
                } else if (stack.is(inputComponent)) {
                    componentsFound++;
                } else {
                    return false;
                }

                if (cellsFound > 1 || componentsFound > 1) {
                    return false;
                }
            }
        }

        return cellsFound == 1 && componentsFound == 1;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(resultCell);
    }

    @Override
    public ItemStack assemble(CraftingInput container, HolderLookup.Provider registries) {
        ItemStack foundCell = ItemStack.EMPTY;
        var componentsFound = 0;

        for (int i = 0; i < container.size(); i++) {
            var stack = container.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.is(inputCell)) {
                    if (stack.getCount() > 1 || !foundCell.isEmpty()) {
                        return ItemStack.EMPTY; // More than one cell found
                    }
                    foundCell = stack;
                } else if (stack.is(inputComponent)) {
                    if (++componentsFound > 1) {
                        return ItemStack.EMPTY; // More than one component found
                    }
                } else {
                    return ItemStack.EMPTY; // Other item found
                }
            }
        }

        if (foundCell.isEmpty() || componentsFound == 0) {
            return ItemStack.EMPTY;
        } else {
            return foundCell.transmuteCopy(resultCell, 1);
        }
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        var remainder = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        for (int i = 0; i < remainder.size(); ++i) {
            var stack = input.getItem(i);
            if (stack.is(inputCell)) {
                // We replace the cell with the component since it is unstackable and forced to be in match
                remainder.set(i, new ItemStack(resultComponent));
            } else {
                remainder.set(i, stack.getCraftingRemainingItem());
            }
        }

        return remainder;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return StorageCellUpgradeRecipeSerializer.INSTANCE;
    }

}
