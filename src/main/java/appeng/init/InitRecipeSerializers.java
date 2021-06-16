package appeng.init;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.core.api.definitions.ApiItems;
import appeng.items.parts.FacadeItem;
import appeng.recipes.entropy.EntropyRecipeSerializer;
import appeng.recipes.game.DisassembleRecipe;
import appeng.recipes.game.FacadeRecipe;
import appeng.recipes.handlers.GrinderRecipeSerializer;
import appeng.recipes.handlers.InscriberRecipeSerializer;

public final class InitRecipeSerializers {

    private InitRecipeSerializers() {
    }

    public static void init(IForgeRegistry<IRecipeSerializer<?>> registry) {
        FacadeItem facadeItem = (FacadeItem) ApiItems.FACADE.item();
        registry.registerAll(
                DisassembleRecipe.SERIALIZER,
                GrinderRecipeSerializer.INSTANCE,
                InscriberRecipeSerializer.INSTANCE,
                FacadeRecipe.getSerializer(facadeItem),
                EntropyRecipeSerializer.INSTANCE);
    }

}
