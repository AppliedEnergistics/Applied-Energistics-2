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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;

import appeng.api.config.CondenserOutput;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IMaterials;
import appeng.api.features.AEFeature;
import appeng.container.implementations.CraftingTermContainer;
import appeng.container.implementations.PatternTermContainer;
import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.integration.abstraction.JEIFacade;
import appeng.items.parts.FacadeItem;
import appeng.recipes.handlers.GrinderRecipe;
import appeng.recipes.handlers.InscriberRecipe;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    private static final ResourceLocation ID = new ResourceLocation(AppEng.MOD_ID, "core");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration subtypeRegistry) {
        final Optional<Item> maybeFacade = Api.instance().definitions().items().facade().maybeItem();
        maybeFacade.ifPresent(subtypeRegistry::useNbtForSubtypes);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new GrinderRecipeCategory(registry.getJeiHelpers().getGuiHelper()),
                new CondenserCategory(registry.getJeiHelpers().getGuiHelper()),
                new InscriberRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        // Allow recipe transfer from JEI to crafting and pattern terminal
        registration.addRecipeTransferHandler(new RecipeTransferHandler<>(CraftingTermContainer.class),
                VanillaRecipeCategoryUid.CRAFTING);
        registration.addRecipeTransferHandler(new RecipeTransferHandler<>(PatternTermContainer.class),
                VanillaRecipeCategoryUid.CRAFTING);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {

        IDefinitions definitions = Api.instance().definitions();

        RecipeManager recipeManager = Minecraft.getInstance().world.getRecipeManager();
        registration.addRecipes(recipeManager.getRecipes(GrinderRecipe.TYPE).values(), GrinderRecipeCategory.UID);
        registration.addRecipes(recipeManager.getRecipes(InscriberRecipe.TYPE).values(), InscriberRecipeCategory.UID);
        registration.addRecipes(ImmutableList.of(CondenserOutput.MATTER_BALLS, CondenserOutput.SINGULARITY),
                CondenserCategory.UID);

        registerDescriptions(definitions, registration);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        IDefinitions definitions = Api.instance().definitions();

        ItemStack grindstone = definitions.blocks().grindstone().stack(1);
        registration.addRecipeCatalyst(grindstone, GrinderRecipeCategory.UID);

        ItemStack condenser = definitions.blocks().condenser().stack(1);
        registration.addRecipeCatalyst(condenser, CondenserCategory.UID);

        ItemStack inscriber = definitions.blocks().inscriber().stack(1);
        registration.addRecipeCatalyst(inscriber, InscriberRecipeCategory.UID);
    }

    private void registerDescriptions(IDefinitions definitions, IRecipeRegistration registry) {
        IMaterials materials = definitions.materials();

        final String[] message;
        if (AEConfig.instance().isFeatureEnabled(AEFeature.CERTUS_QUARTZ_WORLD_GEN)) {
            message = new String[] { GuiText.ChargedQuartz.getTranslationKey(), "",
                    GuiText.ChargedQuartzFind.getTranslationKey() };
        } else {
            message = new String[] { GuiText.ChargedQuartzFind.getTranslationKey() };
        }
        this.addDescription(registry, materials.certusQuartzCrystalCharged(), message);

        if (AEConfig.instance().isFeatureEnabled(AEFeature.METEORITE_WORLD_GEN)) {
            this.addDescription(registry, materials.logicProcessorPress(),
                    GuiText.inWorldCraftingPresses.getTranslationKey());
            this.addDescription(registry, materials.calcProcessorPress(),
                    GuiText.inWorldCraftingPresses.getTranslationKey());
            this.addDescription(registry, materials.engProcessorPress(),
                    GuiText.inWorldCraftingPresses.getTranslationKey());
        }

        if (AEConfig.instance().isFeatureEnabled(AEFeature.IN_WORLD_FLUIX)) {
            this.addDescription(registry, materials.fluixCrystal(), GuiText.inWorldFluix.getTranslationKey());
        }

        if (AEConfig.instance().isFeatureEnabled(AEFeature.IN_WORLD_SINGULARITY)) {
            this.addDescription(registry, materials.qESingularity(), GuiText.inWorldSingularity.getTranslationKey());
        }

        if (AEConfig.instance().isFeatureEnabled(AEFeature.IN_WORLD_PURIFICATION)) {
            this.addDescription(registry, materials.purifiedCertusQuartzCrystal(),
                    GuiText.inWorldPurificationCertus.getTranslationKey());
            this.addDescription(registry, materials.purifiedNetherQuartzCrystal(),
                    GuiText.inWorldPurificationNether.getTranslationKey());
            this.addDescription(registry, materials.purifiedFluixCrystal(),
                    GuiText.inWorldPurificationFluix.getTranslationKey());
        }

    }

    private void addDescription(IRecipeRegistration registry, IItemDefinition itemDefinition, String... message) {
        registry.addIngredientInfo(itemDefinition.stack(1), VanillaTypes.ITEM, message);
    }

    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {

        IDefinitions definitions = Api.instance().definitions();

        if (AEConfig.instance().isFeatureEnabled(AEFeature.ENABLE_FACADE_CRAFTING)) {
            FacadeItem itemFacade = (FacadeItem) definitions.items().facade().item();
            ItemStack cableAnchor = definitions.parts().cableAnchor().stack(1);
            registration.addRecipeManagerPlugin(new FacadeRegistryPlugin(itemFacade, cableAnchor));
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        JEIFacade.setInstance(new JeiRuntimeAdapter(jeiRuntime));
        this.hideDebugTools(jeiRuntime);

    }

    private void hideDebugTools(IJeiRuntime jeiRuntime) {
        Collection<ItemStack> toRemove = new ArrayList<>();

        // We use the internal API here as exception as debug tools are not part of the
        // public one by design.
        toRemove.add(Api.INSTANCE.definitions().items().dummyFluidItem().maybeStack(1).orElse(null));

        if (!AEConfig.instance().isFeatureEnabled(AEFeature.UNSUPPORTED_DEVELOPER_TOOLS)) {
            toRemove.add(Api.INSTANCE.definitions().blocks().cubeGenerator().maybeStack(1).orElse(null));
            toRemove.add(Api.INSTANCE.definitions().blocks().chunkLoader().maybeStack(1).orElse(null));
            toRemove.add(Api.INSTANCE.definitions().blocks().energyGenerator().maybeStack(1).orElse(null));
            toRemove.add(Api.INSTANCE.definitions().blocks().itemGen().maybeStack(1).orElse(null));
            toRemove.add(Api.INSTANCE.definitions().blocks().phantomNode().maybeStack(1).orElse(null));

            toRemove.add(Api.INSTANCE.definitions().items().toolDebugCard().maybeStack(1).orElse(null));
            toRemove.add(Api.INSTANCE.definitions().items().toolEraser().maybeStack(1).orElse(null));
            toRemove.add(Api.INSTANCE.definitions().items().toolMeteoritePlacer().maybeStack(1).orElse(null));
            toRemove.add(Api.INSTANCE.definitions().items().toolReplicatorCard().maybeStack(1).orElse(null));
        }

        jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(mezz.jei.api.constants.VanillaTypes.ITEM,
                toRemove);
    }

}
