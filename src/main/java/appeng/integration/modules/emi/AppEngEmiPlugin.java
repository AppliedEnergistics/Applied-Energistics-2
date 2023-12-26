package appeng.integration.modules.emi;

import appeng.api.config.CondenserOutput;
import appeng.api.features.P2PTunnelAttunementInternal;
import appeng.api.integrations.emi.EmiStackConverters;
import appeng.api.integrations.rei.IngredientConverters;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.emi.transfer.EmiEncodePatternHandler;
import appeng.integration.modules.emi.transfer.EmiUseCraftingRecipeHandler;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.recipes.entropy.EntropyRecipe;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.transform.TransformRecipe;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@EmiEntrypoint
public class AppEngEmiPlugin implements EmiPlugin {
    static final ResourceLocation TEXTURE = AppEng.makeId("textures/guis/jei.png");

    @Override
    public void register(EmiRegistry registry) {

        EmiStackConverters.register(new EmiItemStackConverter());
        EmiStackConverters.register(new EmiFluidStackConverter());

        // Recipe transfer
        registry.addRecipeHandler(PatternEncodingTermMenu.TYPE, new EmiEncodePatternHandler<>(PatternEncodingTermMenu.class));
        registry.addRecipeHandler(CraftingTermMenu.TYPE, new EmiUseCraftingRecipeHandler<>(CraftingTermMenu.class));

        // Inscriber
        registry.addCategory(EmiInscriberRecipe.CATEGORY);
        registry.addWorkstation(EmiInscriberRecipe.CATEGORY, EmiStack.of(AEBlocks.INSCRIBER));
        adaptRecipeType(registry, InscriberRecipe.TYPE, EmiInscriberRecipe::new);

        // Charger
        registry.addCategory(EmiChargerRecipe.CATEGORY);
        registry.addWorkstation(EmiChargerRecipe.CATEGORY, EmiStack.of(AEBlocks.CHARGER));
        adaptRecipeType(registry, ChargerRecipe.TYPE, EmiChargerRecipe::new);

        // P2P attunement
        registry.addCategory(EmiP2PAttunementRecipe.CATEGORY);
        registry.addDeferredRecipes(this::registerP2PAttunements);

        // Condenser
        registry.addCategory(EmiCondenserRecipe.CATEGORY);
        registry.addWorkstation(EmiCondenserRecipe.CATEGORY, EmiStack.of(AEBlocks.CONDENSER));
        registry.addRecipe(new EmiCondenserRecipe(CondenserOutput.MATTER_BALLS));
        registry.addRecipe(new EmiCondenserRecipe(CondenserOutput.SINGULARITY));

        // Entropy Manipulator
        registry.addCategory(EmiEntropyRecipe.CATEGORY);
        registry.addWorkstation(EmiEntropyRecipe.CATEGORY, EmiStack.of(AEItems.ENTROPY_MANIPULATOR));
        adaptRecipeType(registry, EntropyRecipe.TYPE, EmiEntropyRecipe::new);

        // In-World Transformation
        registry.addCategory(EmiTransformRecipe.CATEGORY);
        adaptRecipeType(registry, TransformRecipe.TYPE, EmiTransformRecipe::new);

        // Facades
        registry.addDeferredRecipes(this::registerFacades);
    }

    private static <C extends Container, T extends Recipe<C>> void adaptRecipeType(EmiRegistry registry,
                                                                                   RecipeType<T> recipeType,
                                                                                   Function<RecipeHolder<T>, ? extends EmiRecipe> adapter) {
        registry.getRecipeManager().getAllRecipesFor(recipeType)
                .stream()
                .map(adapter)
                .forEach(registry::addRecipe);
    }

    private void registerP2PAttunements(Consumer<EmiRecipe> recipeConsumer) {

        var all = EmiApi.getIndexStacks();
        for (var entry : P2PTunnelAttunementInternal.getApiTunnels()) {
            var inputs = all.stream().filter(stack -> entry.stackPredicate().test(stack.getItemStack()))
                    .toList();
            if (inputs.isEmpty()) {
                continue;
            }
            recipeConsumer.accept(
                    new EmiP2PAttunementRecipe(
                            EmiIngredient.of(inputs),
                            EmiStack.of(entry.tunnelType()),
                            ItemModText.P2P_API_ATTUNEMENT.text().append("\n").append(entry.description())
                    )
            );
        }

        for (var entry : P2PTunnelAttunementInternal.getTagTunnels().entrySet()) {
            var ingredient = EmiIngredient.of(entry.getKey());
            if (ingredient.isEmpty()) {
                continue;
            }
            recipeConsumer.accept(
                    new EmiP2PAttunementRecipe(
                            ingredient,
                            EmiStack.of(entry.getValue()),
                            ItemModText.P2P_TAG_ATTUNEMENT.text()
                    )
            );
        }
    }

    private void registerFacades(Consumer<EmiRecipe> recipeConsumer) {
        var generator = new EmiFacadeGenerator();
        EmiApi.getIndexStacks().stream()
                .map(generator::getRecipeFor)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(recipeConsumer);
    }
}
