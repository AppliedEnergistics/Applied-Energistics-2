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
import appeng.api.features.IGrinderRecipe;
import appeng.api.features.IInscriberRecipe;
import appeng.client.gui.AEGuiHandler;
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.container.implementations.ContainerExpandedProcessingPatternTerm;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.integration.Integrations;
import appeng.items.parts.ItemFacade;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.config.Constants;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@mezz.jei.api.JEIPlugin
public class JEIPlugin implements IModPlugin {
    public static IJeiRuntime runtime;
    public static AEGuiHandler aeGuiHandler;

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        final Optional<Item> maybeFacade = AEApi.instance().definitions().items().facade().maybeItem();
        maybeFacade.ifPresent(subtypeRegistry::useNbtForSubtypes);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new GrinderRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
        registry.addRecipeCategories(new CondenserCategory(registry.getJeiHelpers().getGuiHelper()));
        registry.addRecipeCategories(new InscriberRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void register(IModRegistry registry) {
        IDefinitions definitions = AEApi.instance().definitions();

        this.registerFacadeRecipe(definitions, registry);

        this.registerInscriberRecipes(definitions, registry);

        this.registerCondenserRecipes(definitions, registry);

        this.registerGrinderRecipes(definitions, registry);

        this.registerDescriptions(definitions, registry);

        // Allow recipe transfer from JEI to crafting and pattern terminal
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(new RecipeTransferHandler<>(ContainerCraftingTerm.class), VanillaRecipeCategoryUid.CRAFTING);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(new RecipeTransferHandler<>(ContainerPatternTerm.class), Constants.UNIVERSAL_RECIPE_TRANSFER_UID);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(new RecipeTransferHandler<>(ContainerExpandedProcessingPatternTerm.class), Constants.UNIVERSAL_RECIPE_TRANSFER_UID);

        aeGuiHandler = new AEGuiHandler();
        registry.addAdvancedGuiHandlers(aeGuiHandler);
        registry.addGhostIngredientHandler(aeGuiHandler.getGuiContainerClass(), aeGuiHandler);
    }

    private void registerDescriptions(IDefinitions definitions, IModRegistry registry) {
        IMaterials materials = definitions.materials();

        final String message;
        if (AEConfig.instance().isFeatureEnabled(AEFeature.CERTUS_QUARTZ_WORLD_GEN)) {
            message = GuiText.ChargedQuartz.getLocal() + "\n\n" + GuiText.ChargedQuartzFind.getLocal();
        } else {
            message = GuiText.ChargedQuartzFind.getLocal();
        }
        this.addDescription(registry, materials.certusQuartzCrystalCharged(), message);

        if (AEConfig.instance().isFeatureEnabled(AEFeature.METEORITE_WORLD_GEN)) {
            this.addDescription(registry, materials.logicProcessorPress(), GuiText.inWorldCraftingPresses.getLocal());
            this.addDescription(registry, materials.calcProcessorPress(), GuiText.inWorldCraftingPresses.getLocal());
            this.addDescription(registry, materials.engProcessorPress(), GuiText.inWorldCraftingPresses.getLocal());
        }

        if (AEConfig.instance().isFeatureEnabled(AEFeature.IN_WORLD_FLUIX)) {
            this.addDescription(registry, materials.fluixCrystal(), GuiText.inWorldFluix.getLocal());
        }

        if (AEConfig.instance().isFeatureEnabled(AEFeature.IN_WORLD_SINGULARITY)) {
            this.addDescription(registry, materials.qESingularity(), GuiText.inWorldSingularity.getLocal());
        }

        if (AEConfig.instance().isFeatureEnabled(AEFeature.IN_WORLD_PURIFICATION)) {
            this.addDescription(registry, materials.purifiedCertusQuartzCrystal(), GuiText.inWorldPurificationCertus.getLocal());
            this.addDescription(registry, materials.purifiedNetherQuartzCrystal(), GuiText.inWorldPurificationNether.getLocal());
            this.addDescription(registry, materials.purifiedFluixCrystal(), GuiText.inWorldPurificationFluix.getLocal());
        }

    }

    private void addDescription(IModRegistry registry, IItemDefinition itemDefinition, String message) {
        itemDefinition.maybeStack(1).ifPresent(itemStack -> registry.addIngredientInfo(itemStack, ItemStack.class, message));
    }

    private void registerGrinderRecipes(IDefinitions definitions, IModRegistry registry) {

        ItemStack grindstone = definitions.blocks().grindstone().maybeStack(1).orElse(ItemStack.EMPTY);

        if (grindstone.isEmpty()) {
            return;
        }

        registry.handleRecipes(IGrinderRecipe.class, new GrinderRecipeHandler(), GrinderRecipeCategory.UID);
        registry.addRecipes(Lists.newArrayList(AEApi.instance().registries().grinder().getRecipes()), GrinderRecipeCategory.UID);
        registry.addRecipeCatalyst(grindstone, GrinderRecipeCategory.UID);
    }

    private void registerCondenserRecipes(IDefinitions definitions, IModRegistry registry) {

        ItemStack condenser = definitions.blocks().condenser().maybeStack(1).orElse(ItemStack.EMPTY);
        if (condenser.isEmpty()) {
            return;
        }

        ItemStack matterBall = definitions.materials().matterBall().maybeStack(1).orElse(ItemStack.EMPTY);
        if (!matterBall.isEmpty()) {
            registry.addRecipes(ImmutableList.of(CondenserOutput.MATTER_BALLS), CondenserCategory.UID);
        }

        ItemStack singularity = definitions.materials().singularity().maybeStack(1).orElse(ItemStack.EMPTY);
        if (!singularity.isEmpty()) {
            registry.addRecipes(ImmutableList.of(CondenserOutput.SINGULARITY), CondenserCategory.UID);
        }

        if (!matterBall.isEmpty() || !singularity.isEmpty()) {
            registry.addRecipeCatalyst(condenser, CondenserCategory.UID);
            registry.handleRecipes(CondenserOutput.class, new CondenserOutputHandler(registry.getJeiHelpers().getGuiHelper(), matterBall, singularity),
                    CondenserCategory.UID);
        }
    }

    private void registerInscriberRecipes(IDefinitions definitions, IModRegistry registry) {
        registry.handleRecipes(IInscriberRecipe.class, new InscriberRecipeHandler(), InscriberRecipeCategory.UID);

        // Register the inscriber as the crafting item for the inscription category
        definitions.blocks().inscriber().maybeStack(1).ifPresent(inscriber ->
        {
            registry.addRecipeCatalyst(inscriber, InscriberRecipeCategory.UID);
        });

        List<IInscriberRecipe> inscriberRecipes = new ArrayList<>(AEApi.instance().registries().inscriber().getRecipes());
        registry.addRecipes(inscriberRecipes, InscriberRecipeCategory.UID);
    }

    // Handle the generic crafting recipe for patterns in JEI
    private void registerFacadeRecipe(IDefinitions definitions, IModRegistry registry) {
        Optional<Item> itemFacade = definitions.items().facade().maybeItem();
        Optional<ItemStack> cableAnchor = definitions.parts().cableAnchor().maybeStack(1);
        if (itemFacade.isPresent() && cableAnchor.isPresent() && AEConfig.instance().isFeatureEnabled(AEFeature.ENABLE_FACADE_CRAFTING)) {
            registry.addRecipeRegistryPlugin(new FacadeRegistryPlugin((ItemFacade) itemFacade.get(), cableAnchor.get()));
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        JEIModule jeiModule = (JEIModule) Integrations.jei();
        jeiModule.setJei(new JeiRuntimeAdapter(jeiRuntime));
        runtime = jeiRuntime;
    }
}
