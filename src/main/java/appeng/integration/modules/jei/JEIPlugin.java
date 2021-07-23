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

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

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
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.GrinderScreen;
import appeng.client.gui.implementations.InscriberScreen;
import appeng.container.me.items.CraftingTermContainer;
import appeng.container.me.items.PatternTermContainer;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.ItemDefinition;
import appeng.core.localization.GuiText;
import appeng.integration.abstraction.JEIFacade;
import appeng.items.parts.FacadeItem;
import appeng.recipes.handlers.GrinderRecipe;
import appeng.recipes.handlers.InscriberRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    private static final net.minecraft.resources.ResourceLocation ID = new ResourceLocation(AppEng.MOD_ID, "core");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration subtypeRegistry) {
        subtypeRegistry.useNbtForSubtypes(AEItems.FACADE.asItem());
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

        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        registration.addRecipes(recipeManager.byType(GrinderRecipe.TYPE).values(), GrinderRecipeCategory.UID);
        registration.addRecipes(recipeManager.byType(InscriberRecipe.TYPE).values(), InscriberRecipeCategory.UID);
        registration.addRecipes(ImmutableList.of(CondenserOutput.MATTER_BALLS, CondenserOutput.SINGULARITY),
                CondenserCategory.UID);

        registerDescriptions(registration);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        net.minecraft.world.item.ItemStack grindstone = AEBlocks.GRINDSTONE.stack();
        registration.addRecipeCatalyst(grindstone, GrinderRecipeCategory.UID);

        net.minecraft.world.item.ItemStack condenser = AEBlocks.CONDENSER.stack();
        registration.addRecipeCatalyst(condenser, CondenserCategory.UID);

        net.minecraft.world.item.ItemStack inscriber = AEBlocks.INSCRIBER.stack();
        registration.addRecipeCatalyst(inscriber, InscriberRecipeCategory.UID);
    }

    private void registerDescriptions(IRecipeRegistration registry) {

        final net.minecraft.network.chat.Component[] message;
        if (AEConfig.instance().isGenerateQuartzOre()) {
            // " " Used to enforce a new paragraph
            message = new net.minecraft.network.chat.Component[] { GuiText.ChargedQuartz.text(), new TextComponent(" "),
                    GuiText.ChargedQuartzFind.text() };
        } else {
            message = new net.minecraft.network.chat.Component[] { GuiText.ChargedQuartz.text() };
        }
        this.addDescription(registry, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, message);

        if (AEConfig.instance().isGenerateMeteorites()) {
            this.addDescription(registry, AEItems.LOGIC_PROCESSOR_PRESS,
                    GuiText.inWorldCraftingPresses.text());
            this.addDescription(registry, AEItems.CALCULATION_PROCESSOR_PRESS,
                    GuiText.inWorldCraftingPresses.text());
            this.addDescription(registry, AEItems.ENGINEERING_PROCESSOR_PRESS,
                    GuiText.inWorldCraftingPresses.text());
        }

        if (AEConfig.instance().isInWorldFluixEnabled()) {
            this.addDescription(registry, AEItems.FLUIX_CRYSTAL, GuiText.inWorldFluix.text());
        }

        if (AEConfig.instance().isInWorldSingularityEnabled()) {
            this.addDescription(registry, AEItems.QUANTUM_ENTANGLED_SINGULARITY, GuiText.inWorldSingularity.text());
        }

        if (AEConfig.instance().isInWorldPurificationEnabled()) {
            this.addDescription(registry, AEItems.PURIFIED_CERTUS_QUARTZ_CRYSTAL,
                    GuiText.inWorldPurificationCertus.text());
            this.addDescription(registry, AEItems.PURIFIED_NETHER_QUARTZ_CRYSTAL,
                    GuiText.inWorldPurificationNether.text());
            this.addDescription(registry, AEItems.PURIFIED_FLUIX_CRYSTAL,
                    GuiText.inWorldPurificationFluix.text());
        }

    }

    private void addDescription(IRecipeRegistration registry, ItemDefinition<?> itemDefinition,
            net.minecraft.network.chat.Component... message) {
        registry.addIngredientInfo(itemDefinition.stack(), VanillaTypes.ITEM, message);
    }

    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {

        if (AEConfig.instance().isShowFacadesInJEIEnabled()) {
            FacadeItem itemFacade = AEItems.FACADE.asItem();
            net.minecraft.world.item.ItemStack cableAnchor = AEParts.CABLE_ANCHOR.stack();
            registration.addRecipeManagerPlugin(new FacadeRegistryPlugin(itemFacade, cableAnchor));
        }

    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(AEBaseScreen.class, new IGuiContainerHandler<AbstractContainerScreen<?>>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(AbstractContainerScreen containerScreen) {
                if (containerScreen instanceof AEBaseScreen) {
                    return ((AEBaseScreen<?>) containerScreen).getExclusionZones();
                } else {
                    return Collections.emptyList();
                }
            }

            @Nullable
            @Override
            public Object getIngredientUnderMouse(AbstractContainerScreen<?> containerScreen, double mouseX, double mouseY) {
                // The following code allows the player to show recipes involving fluids in AE fluid terminals or AE
                // fluid tanks shown in fluid interfaces and other UI.
                if (containerScreen instanceof AEBaseScreen) {
                    AEBaseScreen<?> baseScreen = (AEBaseScreen<?>) containerScreen;
                    return baseScreen.getIngredientUnderMouse(mouseX, mouseY);
                }

                return null;
            }

            @Override
            public Collection<IGuiClickableArea> getGuiClickableAreas(AbstractContainerScreen<?> containerScreen, double mouseX,
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
        if (!AEConfig.instance().isDebugToolsEnabled()) {
            Collection<ItemStack> toRemove = new ArrayList<>();

            // We use the internal API here as exception as debug tools are not part of the public one by design.
            toRemove.add(AEBlocks.DEBUG_CUBE_GEN.stack());
            toRemove.add(AEBlocks.DEBUG_CHUNK_LOADER.stack());
            toRemove.add(AEBlocks.DEBUG_ENERGY_GEN.stack());
            toRemove.add(AEBlocks.DEBUG_ITEM_GEN.stack());
            toRemove.add(AEBlocks.DEBUG_PHANTOM_NODE.stack());

            toRemove.add(AEItems.DEBUG_CARD.stack());
            toRemove.add(AEItems.DEBUG_ERASER.stack());
            toRemove.add(AEItems.DEBUG_METEORITE_PLACER.stack());
            toRemove.add(AEItems.DEBUG_REPLICATOR_CARD.stack());
            toRemove.add(AEItems.DEBUG_PART_PLACER.stack());

            jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(mezz.jei.api.constants.VanillaTypes.ITEM,
                    toRemove);
        }

    }

}
