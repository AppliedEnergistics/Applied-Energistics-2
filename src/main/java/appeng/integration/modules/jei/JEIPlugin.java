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
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
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
import appeng.api.integrations.jei.IngredientConverters;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.InscriberScreen;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.ItemDefinition;
import appeng.core.localization.GuiText;
import appeng.integration.abstraction.JEIFacade;
import appeng.integration.modules.jei.throwinginwater.ThrowingInWaterCategory;
import appeng.integration.modules.jei.throwinginwater.ThrowingInWaterDisplay;
import appeng.integration.modules.jei.transfer.EncodePatternTransferHandler;
import appeng.integration.modules.jei.transfer.UseCraftingRecipeTransfer;
import appeng.items.parts.FacadeItem;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.me.items.WirelessCraftingTermMenu;
import appeng.recipes.entropy.EntropyRecipe;
import appeng.recipes.handlers.InscriberRecipe;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    public static final ResourceLocation TEXTURE = AppEng.makeId("textures/guis/jei.png");

    private static final ResourceLocation ID = new ResourceLocation(AppEng.MOD_ID, "core");

    public JEIPlugin() {
        IngredientConverters.register(new ItemIngredientConverter());
        IngredientConverters.register(new FluidIngredientConverter());
    }

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
        var jeiHelpers = registry.getJeiHelpers();
        registry.addRecipeCategories(
                new ThrowingInWaterCategory(jeiHelpers.getGuiHelper()),
                new CondenserCategory(jeiHelpers.getGuiHelper()),
                new InscriberRecipeCategory(jeiHelpers.getGuiHelper()),
                new EntropyManipulatorCategory(jeiHelpers));
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        // Allow vanilla crafting recipe transfer from JEI to crafting terminal
        registration.addRecipeTransferHandler(
                new UseCraftingRecipeTransfer<>(CraftingTermMenu.class, registration.getTransferHelper()),
                VanillaRecipeCategoryUid.CRAFTING);
        registration.addRecipeTransferHandler(
                new UseCraftingRecipeTransfer<>(WirelessCraftingTermMenu.class, registration.getTransferHelper()),
                VanillaRecipeCategoryUid.CRAFTING);

        // Universal handler for processing to try and handle all IRecipe
        registration.addUniversalRecipeTransferHandler(
                new EncodePatternTransferHandler<>(PatternEncodingTermMenu.class, registration.getTransferHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        registration.addRecipes(recipeManager.byType(InscriberRecipe.TYPE).values(), InscriberRecipeCategory.UID);
        registration.addRecipes(ImmutableList.of(CondenserOutput.MATTER_BALLS, CondenserOutput.SINGULARITY),
                CondenserCategory.UID);
        registration.addRecipes(recipeManager.byType(EntropyRecipe.TYPE).values(),
                EntropyManipulatorCategory.TYPE.getUid());

        registerDescriptions(registration);

        // Add displays for crystal growth
        var inWater = new ArrayList<ThrowingInWaterDisplay>();
        if (AEConfig.instance().isInWorldCrystalGrowthEnabled()) {
            inWater.add(
                    new ThrowingInWaterDisplay(
                            List.of(
                                    Ingredient.of(AEItems.CERTUS_CRYSTAL_SEED)),
                            AEItems.CERTUS_QUARTZ_CRYSTAL.stack(),
                            true));
            inWater.add(
                    new ThrowingInWaterDisplay(
                            List.of(
                                    Ingredient.of(AEItems.FLUIX_CRYSTAL_SEED)),
                            AEItems.FLUIX_CRYSTAL.stack(),
                            true));
        }
        if (AEConfig.instance().isInWorldFluixEnabled()) {
            inWater.add(
                    new ThrowingInWaterDisplay(
                            List.of(
                                    Ingredient.of(Items.REDSTONE),
                                    Ingredient.of(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED),
                                    Ingredient.of(Items.QUARTZ)),
                            AEItems.FLUIX_DUST.stack(2),
                            false));
        }
        registration.addRecipes(inWater, ThrowingInWaterCategory.ID);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        ItemStack condenser = AEBlocks.CONDENSER.stack();
        registration.addRecipeCatalyst(condenser, CondenserCategory.UID);

        ItemStack inscriber = AEBlocks.INSCRIBER.stack();
        registration.addRecipeCatalyst(inscriber, InscriberRecipeCategory.UID);

        ItemStack craftingTerminal = AEParts.CRAFTING_TERMINAL.stack();
        registration.addRecipeCatalyst(craftingTerminal, RecipeTypes.CRAFTING);

        ItemStack wirelessCraftingTerminal = AEItems.WIRELESS_CRAFTING_TERMINAL.stack();
        registration.addRecipeCatalyst(wirelessCraftingTerminal, RecipeTypes.CRAFTING);

        registration.addRecipeCatalyst(AEItems.ENTROPY_MANIPULATOR.stack(), EntropyManipulatorCategory.TYPE);
    }

    private void registerDescriptions(IRecipeRegistration registry) {
        this.addDescription(registry, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, GuiText.ChargedQuartz.text());

        this.addDescription(registry, AEItems.LOGIC_PROCESSOR_PRESS,
                GuiText.inWorldCraftingPresses.text());
        this.addDescription(registry, AEItems.CALCULATION_PROCESSOR_PRESS,
                GuiText.inWorldCraftingPresses.text());
        this.addDescription(registry, AEItems.ENGINEERING_PROCESSOR_PRESS,
                GuiText.inWorldCraftingPresses.text());

        if (AEConfig.instance().isInWorldSingularityEnabled()) {
            this.addDescription(registry, AEItems.QUANTUM_ENTANGLED_SINGULARITY, GuiText.inWorldSingularity.text());
        }
    }

    private void addDescription(IRecipeRegistration registry, ItemDefinition<?> itemDefinition,
            Component... message) {
        registry.addIngredientInfo(itemDefinition.stack(), VanillaTypes.ITEM, message);
    }

    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {
        if (AEConfig.instance().isEnableFacadesInJEI()) {
            FacadeItem itemFacade = AEItems.FACADE.asItem();
            ItemStack cableAnchor = AEParts.CABLE_ANCHOR.stack();
            registration.addRecipeManagerPlugin(new FacadeRegistryPlugin(itemFacade, cableAnchor));
        }
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(AEBaseScreen.class,
                new IGuiContainerHandler<AEBaseScreen<?>>() {
                    @Override
                    public List<Rect2i> getGuiExtraAreas(AEBaseScreen<?> screen) {
                        return screen.getExclusionZones();
                    }

                    @Nullable
                    @Override
                    public Object getIngredientUnderMouse(AEBaseScreen<?> screen, double mouseX, double mouseY) {
                        // The following code allows the player to show recipes involving fluids in AE fluid terminals
                        // or AE fluid tanks shown in fluid interfaces and other UI.
                        var stack = screen.getStackUnderMouse(mouseX, mouseY);
                        if (stack != null) {
                            return GenericEntryStackHelper.stackToIngredient(stack);
                        }

                        return null;
                    }

                    @Override
                    public Collection<IGuiClickableArea> getGuiClickableAreas(AEBaseScreen<?> screen, double mouseX,
                            double mouseY) {
                        if (screen instanceof InscriberScreen) {
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

            jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM,
                    toRemove);
        }

    }

}
