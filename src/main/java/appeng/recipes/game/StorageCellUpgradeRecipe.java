package appeng.recipes.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
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
    public static final RecipeSerializer<StorageCellUpgradeRecipe> SERIALIZER = new Serializer();

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

    private static final Codec<StorageCellUpgradeRecipe> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("input_cell")
                    .forGetter(StorageCellUpgradeRecipe::getInputCell),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("input_component")
                    .forGetter(StorageCellUpgradeRecipe::getInputComponent),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("result_cell")
                    .forGetter(StorageCellUpgradeRecipe::getResultCell),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("result_component")
                    .forGetter(StorageCellUpgradeRecipe::getResultComponent))
            .apply(builder, StorageCellUpgradeRecipe::new));

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
    public boolean matches(CraftingContainer container, Level level) {
        var cellsFound = 0;
        var componentsFound = 0;

        for (int i = 0; i < container.getContainerSize(); i++) {
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
    public ItemStack getResultItem(RegistryAccess registries) {
        return new ItemStack(resultCell);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registries) {
        ItemStack foundCell = ItemStack.EMPTY;
        var componentsFound = 0;

        for (int i = 0; i < container.getContainerSize(); i++) {
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
            var result = new ItemStack(resultCell, 1, foundCell.serializeAttachments());
            var oldTag = foundCell.getTag();
            if (oldTag != null) {
                result.setTag(oldTag.copy());
            }
            return result;
        }
    }

    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        var remainder = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < remainder.size(); ++i) {
            var stack = inv.getItem(i);
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
        return SERIALIZER;
    }

    private static class Serializer implements RecipeSerializer<StorageCellUpgradeRecipe> {
        @Override
        public Codec<StorageCellUpgradeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StorageCellUpgradeRecipe fromNetwork(FriendlyByteBuf buffer) {
            var inputCell = buffer.readById(BuiltInRegistries.ITEM);
            var inputComponent = buffer.readById(BuiltInRegistries.ITEM);
            var resultCell = buffer.readById(BuiltInRegistries.ITEM);
            var resultComponent = buffer.readById(BuiltInRegistries.ITEM);

            return new StorageCellUpgradeRecipe(
                    inputCell,
                    inputComponent,
                    resultCell,
                    resultComponent);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, StorageCellUpgradeRecipe recipe) {
            buffer.writeId(BuiltInRegistries.ITEM, recipe.inputCell);
            buffer.writeId(BuiltInRegistries.ITEM, recipe.inputComponent);
            buffer.writeId(BuiltInRegistries.ITEM, recipe.resultCell);
            buffer.writeId(BuiltInRegistries.ITEM, recipe.resultComponent);
        }
    }
}
