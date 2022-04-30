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
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import dev.architectury.event.CompoundEventResult;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.ButtonArea;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.displays.DefaultInformationDisplay;

import appeng.api.config.CondenserOutput;
import appeng.api.features.P2PTunnelAttunementInternal;
import appeng.api.integrations.rei.IngredientConverters;
import appeng.api.util.AEColor;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.InscriberScreen;
import appeng.core.AEConfig;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.ItemDefinition;
import appeng.core.localization.GuiText;
import appeng.integration.abstraction.REIFacade;
import appeng.integration.modules.jei.throwinginwater.ThrowingInWaterCategory;
import appeng.integration.modules.jei.throwinginwater.ThrowingInWaterDisplay;
import appeng.integration.modules.jei.transfer.EncodePatternTransferHandler;
import appeng.integration.modules.jei.transfer.UseCraftingRecipeTransfer;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.recipes.handlers.InscriberRecipe;

public class ReiPlugin implements REIClientPlugin {

    // Will be hidden if developer items are disabled in the config
    private List<Predicate<ItemStack>> developerItems;
    // Will be hidden if colored cables are hidden
    private List<Predicate<ItemStack>> coloredCables;

    public ReiPlugin() {
        IngredientConverters.register(new ItemIngredientConverter());
        IngredientConverters.register(new FluidIngredientConverter());

        REIFacade.setInstance(new ReiRuntimeAdapter());
    }

    @Override
    public String getPluginProviderName() {
        return "AE2";
    }

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new ThrowingInWaterCategory());
        registry.add(new CondenserCategory());
        registry.add(new InscriberRecipeCategory());
        registry.add(new FacadeRecipeCategory());
        registry.add(new AttunementCategory());

        registry.removePlusButton(ThrowingInWaterCategory.ID);
        registry.removePlusButton(InscriberRecipeCategory.ID);
        registry.removePlusButton(CondenserCategory.ID);

        registerWorkingStations(registry);
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerRecipeFiller(InscriberRecipe.class, InscriberRecipe.TYPE, InscriberRecipeWrapper::new);

        registry.add(new CondenserOutputDisplay(CondenserOutput.MATTER_BALLS));
        registry.add(new CondenserOutputDisplay(CondenserOutput.SINGULARITY));
        P2PTunnelAttunementInternal.REI.add(registry);

        registerDescriptions(registry);

        registry.registerGlobalDisplayGenerator(new FacadeRegistryGenerator());

        // Add displays for crystal growth
        if (AEConfig.instance().isInWorldCrystalGrowthEnabled()) {
            registry.add(
                    new ThrowingInWaterDisplay(
                            List.of(
                                    Ingredient.of(AEItems.CERTUS_CRYSTAL_SEED)),
                            AEItems.CERTUS_QUARTZ_CRYSTAL.stack(),
                            true));
            registry.add(
                    new ThrowingInWaterDisplay(
                            List.of(
                                    Ingredient.of(AEItems.FLUIX_CRYSTAL_SEED)),
                            AEItems.FLUIX_CRYSTAL.stack(),
                            true));
        }
        if (AEConfig.instance().isInWorldFluixEnabled()) {
            registry.add(
                    new ThrowingInWaterDisplay(
                            List.of(
                                    Ingredient.of(Items.REDSTONE),
                                    Ingredient.of(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED),
                                    Ingredient.of(Items.QUARTZ)),
                            AEItems.FLUIX_DUST.stack(2),
                            false));
        }
    }

    @Override
    public void registerTransferHandlers(TransferHandlerRegistry registry) {
        // Allow recipe transfer from JEI to crafting and pattern terminal
        registry.register(new EncodePatternTransferHandler<>(PatternEncodingTermMenu.class));
        registry.register(new UseCraftingRecipeTransfer<>(CraftingTermMenu.class));
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerDraggableStackVisitor(new GhostIngredientHandler());
        registry.registerFocusedStack((screen, mouse) -> {
            if (screen instanceof AEBaseScreen<?>aeScreen) {
                var stack = aeScreen.getStackUnderMouse(mouse.x, mouse.y);
                if (stack != null) {
                    for (var converter : IngredientConverters.getConverters()) {
                        var entryStack = converter.getIngredientFromStack(stack);
                        if (entryStack != null) {
                            return CompoundEventResult.interruptTrue(entryStack);
                        }
                    }
                }
            }

            return CompoundEventResult.pass();
        });
        registry.registerContainerClickArea(
                new Rectangle(82, 39, 26, 16),
                InscriberScreen.class,
                InscriberRecipeCategory.ID);
    }

    @Override
    public void registerEntries(EntryRegistry registry) {
        // Will be hidden if developer items are disabled in the config
        developerItems = ImmutableList.of(
                AEBlocks.DEBUG_CUBE_GEN::isSameAs,
                AEBlocks.DEBUG_CHUNK_LOADER::isSameAs,
                AEBlocks.DEBUG_ENERGY_GEN::isSameAs,
                AEBlocks.DEBUG_ITEM_GEN::isSameAs,
                AEBlocks.DEBUG_PHANTOM_NODE::isSameAs,

                AEItems.DEBUG_CARD::isSameAs,
                AEItems.DEBUG_ERASER::isSameAs,
                AEItems.DEBUG_METEORITE_PLACER::isSameAs,
                AEItems.DEBUG_REPLICATOR_CARD::isSameAs);

        // Will be hidden if colored cables are hidden
        List<Predicate<ItemStack>> predicates = new ArrayList<>();

        for (AEColor color : AEColor.values()) {
            if (color == AEColor.TRANSPARENT) {
                continue; // Keep the Fluix variant
            }
            predicates.add(stack -> stack.getItem() == AEParts.COVERED_CABLE.item(color));
            predicates.add(stack -> stack.getItem() == AEParts.COVERED_DENSE_CABLE.item(color));
            predicates.add(stack -> stack.getItem() == AEParts.GLASS_CABLE.item(color));
            predicates.add(stack -> stack.getItem() == AEParts.SMART_CABLE.item(color));
            predicates.add(stack -> stack.getItem() == AEParts.SMART_DENSE_CABLE.item(color));
        }
        coloredCables = ImmutableList.copyOf(predicates);

        registry.removeEntryIf(this::shouldEntryBeHidden);
    }

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(AEBaseScreen.class, screen -> {
            return screen != null ? mapRects(screen.getExclusionZones()) : Collections.emptyList();
        });

    }

    private static List<Rectangle> mapRects(List<Rect2i> exclusionZones) {
        return exclusionZones.stream()
                .map(ez -> new Rectangle(ez.getX(), ez.getY(), ez.getWidth(), ez.getHeight()))
                .collect(Collectors.toList());
    }

    private void registerWorkingStations(CategoryRegistry registry) {
        ItemStack condenser = AEBlocks.CONDENSER.stack();
        registry.addWorkstations(CondenserCategory.ID, EntryStacks.of(condenser));

        ItemStack inscriber = AEBlocks.INSCRIBER.stack();
        registry.addWorkstations(InscriberRecipeCategory.ID, EntryStacks.of(inscriber));
        registry.setPlusButtonArea(InscriberRecipeCategory.ID, ButtonArea.defaultArea());

        ItemStack craftingTerminal = AEParts.CRAFTING_TERMINAL.stack();
        registry.addWorkstations(BuiltinPlugin.CRAFTING, EntryStacks.of(craftingTerminal));

        ItemStack wirelessCraftingTerminal = AEItems.WIRELESS_CRAFTING_TERMINAL.stack();
        registry.addWorkstations(BuiltinPlugin.CRAFTING, EntryStacks.of(wirelessCraftingTerminal));
    }

    private void registerDescriptions(DisplayRegistry registry) {

        if (AEConfig.instance().isGenerateQuartzOre()) {
            addDescription(registry, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, GuiText.ChargedQuartz.getTranslationKey());
        }

        if (AEConfig.instance().isSpawnPressesInMeteoritesEnabled()) {
            addDescription(registry, AEItems.LOGIC_PROCESSOR_PRESS, GuiText.inWorldCraftingPresses.getTranslationKey());
            addDescription(registry, AEItems.CALCULATION_PROCESSOR_PRESS,
                    GuiText.inWorldCraftingPresses.getTranslationKey());
            addDescription(registry, AEItems.ENGINEERING_PROCESSOR_PRESS,
                    GuiText.inWorldCraftingPresses.getTranslationKey());
            addDescription(registry, AEItems.SILICON_PRESS, GuiText.inWorldCraftingPresses.getTranslationKey());
        }

        if (AEConfig.instance().isInWorldSingularityEnabled()) {
            addDescription(registry, AEItems.QUANTUM_ENTANGLED_SINGULARITY,
                    GuiText.inWorldSingularity.getTranslationKey());
        }
    }

    private static void addDescription(DisplayRegistry registry, ItemDefinition<?> itemDefinition, String... message) {
        DefaultInformationDisplay info = DefaultInformationDisplay.createFromEntry(EntryStacks.of(itemDefinition),
                itemDefinition.asItem().getDescription());
        info.lines(Arrays.stream(message).map(TranslatableComponent::new).collect(Collectors.toList()));
        registry.add(info);
    }

    private boolean shouldEntryBeHidden(EntryStack<?> entryStack) {
        if (entryStack.getType() != VanillaEntryTypes.ITEM) {
            return false;
        }
        ItemStack stack = entryStack.castValue();

        if (AEItems.WRAPPED_GENERIC_STACK.isSameAs(stack)
                || AEItems.FACADE.isSameAs(stack) // REI will add a broken facade with no NBT
                || AEBlocks.CABLE_BUS.isSameAs(stack)
                || AEBlocks.MATRIX_FRAME.isSameAs(stack)
                || AEBlocks.PAINT.isSameAs(stack)) {
            return true;
        }

        if (!AEConfig.instance().isDebugToolsEnabled()) {
            for (var developerItem : developerItems) {
                if (developerItem.test(stack)) {
                    return true;
                }
            }
        }

        if (AEConfig.instance().isDisableColoredCableRecipesInJEI()) {
            for (var predicate : coloredCables) {
                if (predicate.test(stack)) {
                    return true;
                }
            }
        }

        return false;
    }

}
