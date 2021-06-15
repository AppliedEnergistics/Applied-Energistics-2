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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;

import appeng.api.config.CondenserOutput;
import appeng.api.definitions.IItemDefinition;
import appeng.api.features.AEFeature;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.GrinderScreen;
import appeng.client.gui.implementations.InscriberScreen;
import appeng.container.me.items.CraftingTermContainer;
import appeng.container.me.items.PatternTermContainer;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.api.definitions.ApiBlocks;
import appeng.core.api.definitions.ApiItems;
import appeng.core.api.definitions.ApiMaterials;
import appeng.core.api.definitions.ApiParts;
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
        final Optional<Item> maybeFacade = ApiItems.facade().maybeItem();
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

        // Allow vanilla crafting recipe transfer from JEI to crafting terminal
        registration.addRecipeTransferHandler(
                new CraftingRecipeTransferHandler(CraftingTermContainer.class, registration.getTransferHelper()),
                VanillaRecipeCategoryUid.CRAFTING);

        // Universal handler for processing to try and handle all IRecipe
        registration.addUniversalRecipeTransferHandler(
                new PatternRecipeTransferHandler(PatternTermContainer.class, registration.getTransferHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {

        RecipeManager recipeManager = Minecraft.getInstance().world.getRecipeManager();
        registration.addRecipes(recipeManager.getRecipes(GrinderRecipe.TYPE).values(), GrinderRecipeCategory.UID);
        registration.addRecipes(recipeManager.getRecipes(InscriberRecipe.TYPE).values(), InscriberRecipeCategory.UID);
        registration.addRecipes(ImmutableList.of(CondenserOutput.MATTER_BALLS, CondenserOutput.SINGULARITY),
                CondenserCategory.UID);

        registerDescriptions(registration);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        ItemStack grindstone = ApiBlocks.grindstone().stack(1);
        registration.addRecipeCatalyst(grindstone, GrinderRecipeCategory.UID);

        ItemStack condenser = ApiBlocks.condenser().stack(1);
        registration.addRecipeCatalyst(condenser, CondenserCategory.UID);

        ItemStack inscriber = ApiBlocks.inscriber().stack(1);
        registration.addRecipeCatalyst(inscriber, InscriberRecipeCategory.UID);
    }

    private void registerDescriptions(IRecipeRegistration registry) {

        final ITextComponent[] message;
        if (AEConfig.instance().isFeatureEnabled(AEFeature.CERTUS_QUARTZ_WORLD_GEN)) {
            // " " Used to enforce a new paragraph
            message = new ITextComponent[] { GuiText.ChargedQuartz.text(), new StringTextComponent(" "),
                    GuiText.ChargedQuartzFind.text() };
        } else {
            message = new ITextComponent[] { GuiText.ChargedQuartz.text() };
        }
        this.addDescription(registry, ApiMaterials.certusQuartzCrystalCharged(), message);

        if (AEConfig.instance().isFeatureEnabled(AEFeature.METEORITE_WORLD_GEN)) {
            this.addDescription(registry, ApiMaterials.logicProcessorPress(),
                    GuiText.inWorldCraftingPresses.text());
            this.addDescription(registry, ApiMaterials.calcProcessorPress(),
                    GuiText.inWorldCraftingPresses.text());
            this.addDescription(registry, ApiMaterials.engProcessorPress(),
                    GuiText.inWorldCraftingPresses.text());
        }

        if (AEConfig.instance().isFeatureEnabled(AEFeature.IN_WORLD_FLUIX)) {
            this.addDescription(registry, ApiMaterials.fluixCrystal(), GuiText.inWorldFluix.text());
        }

        if (AEConfig.instance().isFeatureEnabled(AEFeature.IN_WORLD_SINGULARITY)) {
            this.addDescription(registry, ApiMaterials.qESingularity(), GuiText.inWorldSingularity.text());
        }

        if (AEConfig.instance().isFeatureEnabled(AEFeature.IN_WORLD_PURIFICATION)) {
            this.addDescription(registry, ApiMaterials.purifiedCertusQuartzCrystal(),
                    GuiText.inWorldPurificationCertus.text());
            this.addDescription(registry, ApiMaterials.purifiedNetherQuartzCrystal(),
                    GuiText.inWorldPurificationNether.text());
            this.addDescription(registry, ApiMaterials.purifiedFluixCrystal(),
                    GuiText.inWorldPurificationFluix.text());
        }

    }

    private void addDescription(IRecipeRegistration registry, IItemDefinition itemDefinition,
            ITextComponent... message) {
        registry.addIngredientInfo(itemDefinition.stack(1), VanillaTypes.ITEM, message);
    }

    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {

        if (AEConfig.instance().isFeatureEnabled(AEFeature.ENABLE_FACADE_CRAFTING)) {
            FacadeItem itemFacade = (FacadeItem) ApiItems.facade().item();
            ItemStack cableAnchor = ApiParts.cableAnchor().stack(1);
            registration.addRecipeManagerPlugin(new FacadeRegistryPlugin(itemFacade, cableAnchor));
        }

    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(AEBaseScreen.class, new IGuiContainerHandler<ContainerScreen<?>>() {
            @Override
            public List<Rectangle2d> getGuiExtraAreas(ContainerScreen containerScreen) {
                if (containerScreen instanceof AEBaseScreen) {
                    return ((AEBaseScreen<?>) containerScreen).getExclusionZones();
                } else {
                    return Collections.emptyList();
                }
            }

            @Nullable
            @Override
            public Object getIngredientUnderMouse(ContainerScreen<?> containerScreen, double mouseX, double mouseY) {
                // The following code allows the player to show recipes involving fluids in AE fluid terminals or AE
                // fluid tanks shown in fluid interfaces and other UI.
                if (containerScreen instanceof AEBaseScreen) {
                    AEBaseScreen<?> baseScreen = (AEBaseScreen<?>) containerScreen;
                    return baseScreen.getIngredientUnderMouse(mouseX, mouseY);
                }

                return null;
            }

            @Override
            public Collection<IGuiClickableArea> getGuiClickableAreas(ContainerScreen<?> containerScreen, double mouseX,
                    double mouseY) {
                if (containerScreen instanceof GrinderScreen) {
                    return Arrays.asList(
                            IGuiClickableArea.createBasic(18, 34, 55, 22, GrinderRecipeCategory.UID),
                            IGuiClickableArea.createBasic(103, 40, 55, 22, GrinderRecipeCategory.UID));
                } else if (containerScreen instanceof InscriberScreen) {
                    return Collections.singletonList(
                            IGuiClickableArea.createBasic(82, 39, 26, 16, InscriberRecipeCategory.UID));
                }

                return Collections.emptyList();
            }
        });

        registration.addGhostIngredientHandler(AEBaseScreen.class, new GhostIngredientHandler());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        JEIFacade.setInstance(new JeiRuntimeAdapter(jeiRuntime));
        this.hideDebugTools(jeiRuntime);
    }

    private void hideDebugTools(IJeiRuntime jeiRuntime) {
        Collection<ItemStack> toRemove = new ArrayList<>();

        // We use the internal API here as exception as debug tools are not part of the public one by design.

        if (!AEConfig.instance().isFeatureEnabled(AEFeature.UNSUPPORTED_DEVELOPER_TOOLS)) {
            toRemove.add(ApiBlocks.cubeGenerator().stack(1));
            toRemove.add(ApiBlocks.chunkLoader().stack(1));
            toRemove.add(ApiBlocks.energyGenerator().stack(1));
            toRemove.add(ApiBlocks.itemGen().stack(1));
            toRemove.add(ApiBlocks.phantomNode().stack(1));

            toRemove.add(ApiItems.toolDebugCard().stack(1));
            toRemove.add(ApiItems.toolEraser().stack(1));
            toRemove.add(ApiItems.toolMeteoritePlacer().stack(1));
            toRemove.add(ApiItems.toolReplicatorCard().stack(1));
        }

        jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(mezz.jei.api.constants.VanillaTypes.ITEM,
                toRemove);
    }

}
