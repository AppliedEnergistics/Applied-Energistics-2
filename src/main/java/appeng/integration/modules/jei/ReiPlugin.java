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
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import me.shedaniel.rei.api.BaseBoundsHandler;
import me.shedaniel.rei.api.DisplayHelper;
import me.shedaniel.rei.api.EntryRegistry;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import me.shedaniel.rei.plugin.information.DefaultInformationDisplay;

import appeng.api.config.CondenserOutput;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IMaterials;
import appeng.api.definitions.IParts;
import appeng.api.features.AEFeature;
import appeng.api.util.AEColor;
import appeng.client.gui.AEBaseScreen;
import appeng.container.implementations.CraftingTermContainer;
import appeng.container.implementations.PatternTermContainer;
import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.api.definitions.ApiBlocks;
import appeng.core.api.definitions.ApiItems;
import appeng.core.localization.GuiText;
import appeng.integration.abstraction.ReiFacade;
import appeng.items.parts.FacadeItem;
import appeng.recipes.handlers.GrinderRecipe;
import appeng.recipes.handlers.InscriberRecipe;

public class ReiPlugin implements REIPluginV0 {

    private static final ResourceLocation ID = new ResourceLocation(AppEng.MOD_ID, "core");

    private final ApiBlocks blocks = Api.INSTANCE.definitions().blocks();

    private final ApiItems items = Api.INSTANCE.definitions().items();

    // Will be hidden if developer items are disabled in the config
    private final List<Predicate<ItemStack>> developerItems = ImmutableList.of(blocks.cubeGenerator()::isSameAs,
            blocks.chunkLoader()::isSameAs,
            // FIXME FABRIC blocks.energyGenerator()::isSameAs,
            blocks.itemGen()::isSameAs, blocks.phantomNode()::isSameAs,

            items.toolDebugCard()::isSameAs, items.toolEraser()::isSameAs, items.toolMeteoritePlacer()::isSameAs,
            items.toolReplicatorCard()::isSameAs);

    // Will be hidden if colored cables are hidden
    private final List<Predicate<ItemStack>> coloredCables;

    public ReiPlugin() {
        List<Predicate<ItemStack>> predicates = new ArrayList<>();

        IParts parts = Api.instance().definitions().parts();
        for (AEColor color : AEColor.values()) {
            if (color == AEColor.TRANSPARENT) {
                continue; // Keep the Fluix variant
            }
            predicates.add(stack -> parts.cableCovered().sameAs(color, stack));
            predicates.add(stack -> parts.cableDenseCovered().sameAs(color, stack));
            predicates.add(stack -> parts.cableGlass().sameAs(color, stack));
            predicates.add(stack -> parts.cableSmart().sameAs(color, stack));
            predicates.add(stack -> parts.cableDenseSmart().sameAs(color, stack));
        }
        coloredCables = ImmutableList.copyOf(predicates);
    }

    @Override
    public ResourceLocation getPluginIdentifier() {
        return ID;
    }

    @Override
    public void registerPluginCategories(RecipeHelper recipeHelper) {
        recipeHelper.registerCategory(new GrinderRecipeCategory());
        recipeHelper.registerCategory(new CondenserCategory());
        recipeHelper.registerCategory(new InscriberRecipeCategory());
        recipeHelper.registerCategory(new FacadeRecipeCategory());
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
        recipeHelper.registerAutoCraftingHandler(new CraftingRecipeTransferHandler(CraftingTermContainer.class));
        recipeHelper.registerAutoCraftingHandler(new PatternRecipeTransferHandler(PatternTermContainer.class));

        recipeHelper.removeAutoCraftButton(GrinderRecipeCategory.UID);
        recipeHelper.removeAutoCraftButton(InscriberRecipeCategory.UID);
        recipeHelper.removeAutoCraftButton(CondenserCategory.UID);

        registerWorkingStations(recipeHelper);

        if (AEConfig.instance().isFeatureEnabled(AEFeature.ENABLE_FACADE_CRAFTING)) {
            IDefinitions definitions = Api.instance().definitions();
            recipeHelper.registerLiveRecipeGenerator(new FacadeRegistryGenerator(
                    (FacadeItem) definitions.items().facade().item(), definitions.parts().cableAnchor().stack(1)));
        }
    }

    @Override
    public void registerEntries(EntryRegistry entryRegistry) {
        entryRegistry.removeEntryIf(this::shouldEntryBeHidden);
    }

    @Override
    public void postRegister() {
        IDefinitions definitions = Api.instance().definitions();
        registerDescriptions(definitions);

        ReiFacade.setInstance(new ReiRuntimeAdapter());
    }

    @Override
    public void registerBounds(DisplayHelper displayHelper) {
        BaseBoundsHandler baseBoundsHandler = BaseBoundsHandler.getInstance();

        baseBoundsHandler.registerExclusionZones(AEBaseScreen.class, () -> {
            AEBaseScreen<?> screen = (AEBaseScreen<?>) Minecraft.getInstance().currentScreen;
            return screen != null ? screen.getExclusionZones() : Collections.emptyList();
        });

    }

    private void registerWorkingStations(RecipeHelper registration) {
        IDefinitions definitions = Api.instance().definitions();

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
            message = new String[] { GuiText.ChargedQuartz.getTranslationKey(), "",
                    GuiText.ChargedQuartzFind.getTranslationKey() };
        } else {
            message = new String[] { GuiText.ChargedQuartzFind.getTranslationKey() };
        }
        addDescription(materials.certusQuartzCrystalCharged(), message);

        if (AEConfig.instance().isFeatureEnabled(AEFeature.METEORITE_WORLD_GEN)) {
            addDescription(materials.logicProcessorPress(), GuiText.inWorldCraftingPresses.getTranslationKey());
            addDescription(materials.calcProcessorPress(), GuiText.inWorldCraftingPresses.getTranslationKey());
            addDescription(materials.engProcessorPress(), GuiText.inWorldCraftingPresses.getTranslationKey());
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
            addDescription(materials.purifiedFluixCrystal(), GuiText.inWorldPurificationFluix.getTranslationKey());
        }

    }

    private static void addDescription(IItemDefinition itemDefinition, String... message) {
        DefaultInformationDisplay info = DefaultInformationDisplay.createFromEntry(EntryStack.create(itemDefinition),
                itemDefinition.item().getName());
        info.lines(Arrays.stream(message).map(TranslationTextComponent::new).collect(Collectors.toList()));
        RecipeHelper.getInstance().registerDisplay(info);
    }

    private boolean shouldEntryBeHidden(EntryStack entryStack) {
        if (entryStack.getType() != EntryStack.Type.ITEM) {
            return false;
        }
        ItemStack stack = entryStack.getItemStack();

        if (items.dummyFluidItem().isSameAs(stack) || items.facade().isSameAs(stack) // REI will add a broken facade
        // with no NBT
                || blocks.multiPart().isSameAs(stack) || blocks.matrixFrame().isSameAs(stack)
                || blocks.paint().isSameAs(stack)) {
            return true;
        }

        if (!AEConfig.instance().isFeatureEnabled(AEFeature.UNSUPPORTED_DEVELOPER_TOOLS)) {
            for (Predicate<ItemStack> developerItem : developerItems) {
                if (developerItem.test(stack)) {
                    return true;
                }
            }
        }

        if (AEConfig.instance().isDisableColoredCableRecipesInJEI()) {
            for (Predicate<ItemStack> predicate : coloredCables) {
                if (predicate.test(stack)) {
                    return true;
                }
            }
        }

        return false;
    }

}
