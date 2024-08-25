package appeng.client.guidebook.compiler.tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.block.LytBlock;
import appeng.client.guidebook.document.block.LytBlockContainer;
import appeng.client.guidebook.document.block.recipes.LytChargerRecipe;
import appeng.client.guidebook.document.block.recipes.LytCraftingRecipe;
import appeng.client.guidebook.document.block.recipes.LytInscriberRecipe;
import appeng.client.guidebook.document.block.recipes.LytSmeltingRecipe;
import appeng.client.guidebook.document.block.recipes.LytSmithingRecipe;
import appeng.client.guidebook.document.block.recipes.LytTransformRecipe;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;
import appeng.libs.mdast.model.MdAstNode;
import appeng.recipes.AERecipeTypes;
import appeng.util.Platform;

/**
 * Shows a Recipe-Book-Like representation of the recipe needed to craft a given item.
 */
public class RecipeCompiler extends BlockTagCompiler {
    private final List<RecipeTypeMapping<?, ?>> mappings = List.of(
            new RecipeTypeMapping<>(RecipeType.CRAFTING, LytCraftingRecipe::new),
            new RecipeTypeMapping<>(RecipeType.SMELTING, LytSmeltingRecipe::new),
            new RecipeTypeMapping<>(RecipeType.SMITHING, LytSmithingRecipe::new),
            new RecipeTypeMapping<>(AERecipeTypes.INSCRIBER, LytInscriberRecipe::new),
            new RecipeTypeMapping<>(AERecipeTypes.CHARGER, LytChargerRecipe::new),
            new RecipeTypeMapping<>(AERecipeTypes.TRANSFORM, LytTransformRecipe::new));

    @Override
    public Set<String> getTagNames() {
        return Set.of("Recipe", "RecipeFor");
    }

    @Override
    protected void compile(PageCompiler compiler, LytBlockContainer parent, MdxJsxElementFields el) {
        // Find the recipe
        var recipeManager = Platform.getClientRecipeManager();
        if (recipeManager == null) {
            parent.appendError(compiler, "Cannot show recipe while not in-game", el);
            return;
        }

        if ("RecipeFor".equals(el.name())) {
            var itemAndId = MdxAttrs.getRequiredItemAndId(compiler, parent, el, "id");
            if (itemAndId == null) {
                return;
            }

            var id = itemAndId.getLeft();
            var item = itemAndId.getRight();

            for (var mapping : mappings) {
                var block = mapping.tryCreate(recipeManager, item);
                if (block != null) {
                    block.setSourceNode((MdAstNode) el);
                    parent.append(block);
                    return;
                }
            }

            // TODO This *can* be legit if there's no recipe due to datapacks
            parent.appendError(compiler, "Couldn't find recipe for " + id, el);
        } else {
            var recipeId = MdxAttrs.getRequiredId(compiler, parent, el, "id");
            if (recipeId == null) {
                return;
            }

            var recipe = recipeManager.byKey(recipeId).orElse(null);
            if (recipe == null) {
                parent.appendError(compiler, "Couldn't find recipe " + recipeId, el);
                return;
            }

            for (var mapping : mappings) {
                var block = mapping.tryCreate(recipe);
                if (block != null) {
                    block.setSourceNode((MdAstNode) el);
                    parent.append(block);
                    return;
                }
            }

            parent.appendError(compiler, "Couldn't find a handler for recipe " + recipeId, el);
        }
    }

    /**
     * Maps a recipe type to a factory that can create a layout block to display it.
     */
    private record RecipeTypeMapping<T extends Recipe<C>, C extends RecipeInput>(
            RecipeType<T> recipeType,
            Function<RecipeHolder<T>, LytBlock> factory) {
        @Nullable
        LytBlock tryCreate(RecipeManager recipeManager, Item resultItem) {
            var registryAccess = Platform.getClientRegistryAccess();

            // We try to find non-special recipes first then fall back to special
            List<RecipeHolder<T>> fallbackCandidates = new ArrayList<>();
            for (var recipe : recipeManager.byType(recipeType)) {
                if (recipe.value().isSpecial()) {
                    fallbackCandidates.add(recipe);
                    continue;
                }

                if (recipe.value().getResultItem(registryAccess).getItem() == resultItem) {
                    return factory.apply(recipe);
                }
            }

            for (var recipe : fallbackCandidates) {
                if (recipe.value().getResultItem(registryAccess).getItem() == resultItem) {
                    return factory.apply(recipe);
                }
            }

            return null;
        }

        @SuppressWarnings("unchecked")
        @Nullable
        LytBlock tryCreate(RecipeHolder<?> recipe) {
            if (recipeType == recipe.value().getType()) {
                return factory.apply((RecipeHolder<T>) recipe);
            }

            return null;
        }
    }
}
