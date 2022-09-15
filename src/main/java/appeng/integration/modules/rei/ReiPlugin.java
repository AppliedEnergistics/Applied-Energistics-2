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

package appeng.integration.modules.rei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;

import dev.architectury.event.CompoundEventResult;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.ButtonArea;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPlugin;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.displays.DefaultInformationDisplay;

import appeng.api.config.CondenserOutput;
import appeng.api.features.P2PTunnelAttunementInternal;
import appeng.api.integrations.rei.IngredientConverters;
import appeng.api.util.AEColor;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.InscriberScreen;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.FacadeItemGroup;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.ItemDefinition;
import appeng.core.localization.GuiText;
import appeng.core.localization.ItemModText;
import appeng.integration.abstraction.REIFacade;
import appeng.integration.modules.rei.transfer.EncodePatternTransferHandler;
import appeng.integration.modules.rei.transfer.UseCraftingRecipeTransfer;
import appeng.items.parts.FacadeItem;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.transform.TransformRecipe;

@REIPlugin(Dist.CLIENT)
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
        registry.add(new TransformCategory());
        registry.add(new CondenserCategory());
        registry.add(new InscriberRecipeCategory());
        registry.add(new AttunementCategory());
        registry.add(new ChargerCategory());

        registerWorkingStations(registry);
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerRecipeFiller(InscriberRecipe.class, InscriberRecipe.TYPE, InscriberRecipeWrapper::new);
        registry.registerRecipeFiller(ChargerRecipe.class, ChargerRecipe.TYPE, ChargerDisplay::new);
        registry.registerRecipeFiller(TransformRecipe.class, TransformRecipe.TYPE, TransformRecipeWrapper::new);

        registry.add(new CondenserOutputDisplay(CondenserOutput.MATTER_BALLS));
        registry.add(new CondenserOutputDisplay(CondenserOutput.SINGULARITY));

        registerDescriptions(registry);

        if (AEConfig.instance().isEnableFacadeRecipesInJEI()) {
            registry.registerGlobalDisplayGenerator(new FacadeRegistryGenerator());
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
            predicates.add(stack -> stack.getItem() == AEItems.MEMORY_CARDS.item(color));
        }
        coloredCables = ImmutableList.copyOf(predicates);

        registry.removeEntryIf(this::shouldEntryBeHidden);

        if (AEConfig.instance().isEnableFacadesInJEI()) {
            registry.addEntries(EntryIngredients.ofItemStacks(new FacadeItemGroup().getSubTypes()));
        }
    }

    @Override
    public void registerCollapsibleEntries(CollapsibleEntryRegistry registry) {
        if (AEConfig.instance().isEnableFacadesInJEI()) {
            FacadeItem facadeItem = AEItems.FACADE.asItem();
            registry.group(AppEng.makeId("facades"), Component.translatable("itemGroup.ae2.facades"),
                    stack -> stack.getType() == VanillaEntryTypes.ITEM && stack.<ItemStack>castValue().is(facadeItem));
        }
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

        registry.addWorkstations(ChargerDisplay.ID, EntryStacks.of(AEBlocks.CHARGER.stack()));
        registry.addWorkstations(ChargerDisplay.ID, EntryStacks.of(AEBlocks.CRANK.stack()));
    }

    private void registerDescriptions(DisplayRegistry registry) {
        var all = EntryRegistry.getInstance().getEntryStacks().collect(EntryIngredient.collector());

        for (var entry : P2PTunnelAttunementInternal.getApiTunnels()) {
            var inputs = List.of(all.filter(
                    stack -> stack.getValue() instanceof ItemStack s && entry.stackPredicate().test(s)));
            if (inputs.isEmpty()) {
                continue;
            }

            registry.add(new AttunementDisplay(
                    inputs,
                    List.of(EntryIngredient.of(EntryStacks.of(entry.tunnelType()))),
                    ItemModText.P2P_API_ATTUNEMENT.text(),
                    entry.description()));
        }

        for (var entry : P2PTunnelAttunementInternal.getTagTunnels().entrySet()) {
            var ingredient = Ingredient.of(entry.getKey());
            if (ingredient.isEmpty()) {
                continue;
            }

            registry.add(new AttunementDisplay(List.of(EntryIngredients.ofIngredient(ingredient)),
                    List.of(EntryIngredient.of(EntryStacks.of(entry.getValue()))),
                    ItemModText.P2P_TAG_ATTUNEMENT.text()));
        }

        addDescription(registry, AEItems.CERTUS_QUARTZ_CRYSTAL, GuiText.CertusQuartzObtain.getTranslationKey());

        if (AEConfig.instance().isSpawnPressesInMeteoritesEnabled()) {
            addDescription(registry, AEItems.LOGIC_PROCESSOR_PRESS, GuiText.inWorldCraftingPresses.getTranslationKey());
            addDescription(registry, AEItems.CALCULATION_PROCESSOR_PRESS,
                    GuiText.inWorldCraftingPresses.getTranslationKey());
            addDescription(registry, AEItems.ENGINEERING_PROCESSOR_PRESS,
                    GuiText.inWorldCraftingPresses.getTranslationKey());
            addDescription(registry, AEItems.SILICON_PRESS, GuiText.inWorldCraftingPresses.getTranslationKey());
        }

        addDescription(registry, AEBlocks.CRANK, ItemModText.CRANK_DESCRIPTION.getTranslationKey());
    }

    private static void addDescription(DisplayRegistry registry, ItemDefinition<?> itemDefinition, String... message) {
        DefaultInformationDisplay info = DefaultInformationDisplay.createFromEntry(EntryStacks.of(itemDefinition),
                itemDefinition.asItem().getDescription());
        info.lines(Arrays.stream(message).map(Component::translatable).collect(Collectors.toList()));
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
