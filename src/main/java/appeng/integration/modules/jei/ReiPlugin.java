/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.integration.modules.jei;

import appeng.api.AEApi;
import appeng.api.config.CondenserOutput;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IMaterials;
import appeng.api.features.AEFeature;
import appeng.container.implementations.CraftingTermContainer;
import appeng.container.implementations.PatternTermContainer;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.integration.abstraction.ReiFacade;
import appeng.recipes.handlers.GrinderRecipe;
import appeng.recipes.handlers.InscriberRecipe;
import com.google.common.collect.ImmutableList;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import me.shedaniel.rei.plugin.information.DefaultInformationDisplay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ReiPlugin implements REIPluginV0 {
    private static final Identifier ID = new Identifier(AppEng.MOD_ID, "core");

    @Override
    public Identifier getPluginIdentifier() {
        return ID;
    }

    // FIXME FABRIC   @Override
    // FIXME FABRIC   public void registerItemSubtypes(ISubtypeRegistration subtypeRegistry) {
    // FIXME FABRIC       final Optional<Item> maybeFacade = AEApi.instance().definitions().items().facade().maybeItem();
    // FIXME FABRIC       maybeFacade.ifPresent(subtypeRegistry::useNbtForSubtypes);
    // FIXME FABRIC   }


    @Override
    public void registerPluginCategories(RecipeHelper recipeHelper) {
        recipeHelper.registerCategory(new GrinderRecipeCategory());
        recipeHelper.registerCategory(new CondenserCategory());
        recipeHelper.registerCategory(new InscriberRecipeCategory());
    }

    @Override
    public void registerRecipeDisplays(RecipeHelper recipeHelper) {
        recipeHelper.registerRecipes(GrinderRecipeCategory.UID, GrinderRecipe.class, GrinderRecipeWrapper::new);
        recipeHelper.registerRecipes(InscriberRecipeCategory.UID, InscriberRecipe.class, InscriberRecipeWrapper::new);

        recipeHelper.registerDisplay(new CondenserOutputDisplay(CondenserOutput.MATTER_BALLS));
        recipeHelper.registerDisplay(new CondenserOutputDisplay(CondenserOutput.SINGULARITY));
    }

    @Override
    public void registerOthers(RecipeHelper recipeHelper) {
        // Allow recipe transfer from JEI to crafting and pattern terminal
        recipeHelper.registerAutoCraftingHandler(new RecipeTransferHandler<>(CraftingTermContainer.class));
        recipeHelper.registerAutoCraftingHandler(new RecipeTransferHandler<>(PatternTermContainer.class));

        recipeHelper.removeAutoCraftButton(GrinderRecipeCategory.UID);
        recipeHelper.removeAutoCraftButton(InscriberRecipeCategory.UID);
        recipeHelper.removeAutoCraftButton(CondenserCategory.UID);

        registerWorkingStations(recipeHelper);
    }

    @Override
    public void postRegister() {
        IDefinitions definitions = AEApi.instance().definitions();
        registerDescriptions(definitions);

        ReiFacade.setInstance(new ReiRuntimeAdapter());
    }

    private void registerWorkingStations(RecipeHelper registration) {
        IDefinitions definitions = AEApi.instance().definitions();

        ItemStack grindstone = definitions.blocks().grindstone().stack(1);
        registration.registerWorkingStations(GrinderRecipeCategory.UID, EntryStack.create(grindstone));

        ItemStack condenser = definitions.blocks().condenser().stack(1);
        registration.registerWorkingStations(CondenserCategory.UID, EntryStack.create(condenser));

        ItemStack inscriber = definitions.blocks().inscriber().stack(1);
        registration.registerWorkingStations(InscriberRecipeCategory.UID, EntryStack.create(inscriber));
    }

    private void registerDescriptions(IDefinitions definitions) {
        IMaterials materials = definitions.materials();

        final String[] message;
        if (AEConfig.instance().isFeatureEnabled(AEFeature.CERTUS_QUARTZ_WORLD_GEN)) {
            message = new String[]{GuiText.ChargedQuartz.getTranslationKey(), "",
                    GuiText.ChargedQuartzFind.getTranslationKey()};
        } else {
            message = new String[]{GuiText.ChargedQuartzFind.getTranslationKey()};
        }
        addDescription(materials.certusQuartzCrystalCharged(), message);

        if (AEConfig.instance().isFeatureEnabled(AEFeature.METEORITE_WORLD_GEN)) {
            addDescription(materials.logicProcessorPress(),
                    GuiText.inWorldCraftingPresses.getTranslationKey());
            addDescription(materials.calcProcessorPress(),
                    GuiText.inWorldCraftingPresses.getTranslationKey());
            addDescription(materials.engProcessorPress(),
                    GuiText.inWorldCraftingPresses.getTranslationKey());
        }

        if (AEConfig.instance().isFeatureEnabled(AEFeature.IN_WORLD_FLUIX)) {
            addDescription(materials.fluixCrystal(), GuiText.inWorldFluix.getTranslationKey());
        }

        if (AEConfig.instance().isFeatureEnabled(AEFeature.IN_WORLD_SINGULARITY)) {
            addDescription(materials.qESingularity(), GuiText.inWorldSingularity.getTranslationKey());
        }

        if (AEConfig.instance().isFeatureEnabled(AEFeature.IN_WORLD_PURIFICATION)) {
            addDescription(materials.purifiedCertusQuartzCrystal(),
                    GuiText.inWorldPurificationCertus.getTranslationKey());
            addDescription(materials.purifiedNetherQuartzCrystal(),
                    GuiText.inWorldPurificationNether.getTranslationKey());
            addDescription(materials.purifiedFluixCrystal(),
                    GuiText.inWorldPurificationFluix.getTranslationKey());
        }

    }

    private static void addDescription(IItemDefinition itemDefinition, String... message) {
        DefaultInformationDisplay info = DefaultInformationDisplay.createFromEntry(
                EntryStack.create(itemDefinition),
                itemDefinition.item().getName()
        );
        info.lines(Arrays.stream(message).map(TranslatableText::new).collect(Collectors.toList()));
        RecipeHelper.getInstance().registerDisplay(info);
    }

// FIXME FABRIC    @Override
// FIXME FABRIC    public void registerAdvanced(IAdvancedRegistration registration) {
// FIXME FABRIC
// FIXME FABRIC        IDefinitions definitions = AEApi.instance().definitions();
// FIXME FABRIC
// FIXME FABRIC        if (AEConfig.instance().isFeatureEnabled(AEFeature.ENABLE_FACADE_CRAFTING)) {
// FIXME FABRIC            FacadeItem itemFacade = (FacadeItem) definitions.items().facade().item();
// FIXME FABRIC            ItemStack cableAnchor = definitions.parts().cableAnchor().stack(1);
// FIXME FABRIC            registration.addRecipeManagerPlugin(new FacadeRegistryPlugin(itemFacade, cableAnchor));
// FIXME FABRIC        }
// FIXME FABRIC    }
// FIXME FABRIC
// FIXME FABRIC    @Override
// FIXME FABRIC    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
// FIXME FABRIC        JEIFacade.setInstance(new JeiRuntimeAdapter(jeiRuntime));
// FIXME FABRIC        this.hideDebugTools(jeiRuntime);
// FIXME FABRIC
// FIXME FABRIC    }
// FIXME FABRIC
// FIXME FABRIC    private void hideDebugTools(IJeiRuntime jeiRuntime) {
// FIXME FABRIC        Collection<ItemStack> toRemove = new ArrayList<>();
// FIXME FABRIC
// FIXME FABRIC        // We use the internal API here as exception as debug tools are not part of the
// FIXME FABRIC        // public one by design.
// FIXME FABRIC        toRemove.add(Api.INSTANCE.definitions().items().dummyFluidItem().maybeStack(1).orElse(null));
// FIXME FABRIC
// FIXME FABRIC        if (!AEConfig.instance().isFeatureEnabled(AEFeature.UNSUPPORTED_DEVELOPER_TOOLS)) {
// FIXME FABRIC            toRemove.add(Api.INSTANCE.definitions().blocks().cubeGenerator().maybeStack(1).orElse(null));
// FIXME FABRIC            toRemove.add(Api.INSTANCE.definitions().blocks().chunkLoader().maybeStack(1).orElse(null));
// FIXME FABRIC            toRemove.add(Api.INSTANCE.definitions().blocks().energyGenerator().maybeStack(1).orElse(null));
// FIXME FABRIC            toRemove.add(Api.INSTANCE.definitions().blocks().itemGen().maybeStack(1).orElse(null));
// FIXME FABRIC            toRemove.add(Api.INSTANCE.definitions().blocks().phantomNode().maybeStack(1).orElse(null));
// FIXME FABRIC
// FIXME FABRIC            toRemove.add(Api.INSTANCE.definitions().items().toolDebugCard().maybeStack(1).orElse(null));
// FIXME FABRIC            toRemove.add(Api.INSTANCE.definitions().items().toolEraser().maybeStack(1).orElse(null));
// FIXME FABRIC            toRemove.add(Api.INSTANCE.definitions().items().toolMeteoritePlacer().maybeStack(1).orElse(null));
// FIXME FABRIC            toRemove.add(Api.INSTANCE.definitions().items().toolReplicatorCard().maybeStack(1).orElse(null));
// FIXME FABRIC        }
// FIXME FABRIC
// FIXME FABRIC        jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(mezz.jei.api.constants.VanillaTypes.ITEM,
// FIXME FABRIC                toRemove);
// FIXME FABRIC    }

}
