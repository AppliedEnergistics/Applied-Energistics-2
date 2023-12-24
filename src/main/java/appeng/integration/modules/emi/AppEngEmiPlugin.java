package appeng.integration.modules.emi;

import appeng.api.features.P2PTunnelAttunementInternal;
import appeng.core.definitions.AEBlocks;
import appeng.core.localization.ItemModText;
import appeng.recipes.handlers.InscriberRecipe;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

import java.util.function.Consumer;

@EmiEntrypoint
public class AppEngEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(EmiInscriberRecipe.CATEGORY);
        registry.addCategory(EmiP2PAttunementRecipe.CATEGORY);

        registry.addWorkstation(EmiInscriberRecipe.CATEGORY, EmiStack.of(AEBlocks.INSCRIBER));
        registry.getRecipeManager().getAllRecipesFor(InscriberRecipe.TYPE)
                .stream()
                .map(EmiInscriberRecipe::new)
                .forEach(registry::addRecipe);

        registry.addDeferredRecipes(this::registerP2PAttunements);
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
}
