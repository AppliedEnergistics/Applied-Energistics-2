package appeng.integration.modules.jei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
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
import appeng.api.features.P2PTunnelAttunementInternal;
import appeng.api.integrations.jei.IngredientConverters;
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
import appeng.integration.abstraction.JEIFacade;
import appeng.integration.modules.jei.transfer.EncodePatternTransferHandler;
import appeng.integration.modules.jei.transfer.UseCraftingRecipeTransfer;
import appeng.items.parts.FacadeItem;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.me.items.WirelessCraftingTermMenu;
import appeng.recipes.entropy.EntropyRecipe;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.transform.TransformRecipe;

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
                new TransformCategory(jeiHelpers),
                new CondenserCategory(jeiHelpers.getGuiHelper()),
                new InscriberRecipeCategory(jeiHelpers.getGuiHelper()),
                new ChargerCategory(jeiHelpers),
                new AttunementCategory(jeiHelpers),
                new CertusGrowthCategory(jeiHelpers),
                new EntropyManipulatorCategory(jeiHelpers));
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        // Allow vanilla crafting recipe transfer from JEI to crafting terminal
        registration.addRecipeTransferHandler(
                new UseCraftingRecipeTransfer<>(CraftingTermMenu.class, CraftingTermMenu.TYPE,
                        registration.getTransferHelper()),
                RecipeTypes.CRAFTING);
        registration.addRecipeTransferHandler(
                new UseCraftingRecipeTransfer<>(WirelessCraftingTermMenu.class, WirelessCraftingTermMenu.TYPE,
                        registration.getTransferHelper()),
                RecipeTypes.CRAFTING);

        // Universal handler for processing to try and handle all IRecipe
        registration.addUniversalRecipeTransferHandler(new EncodePatternTransferHandler<>(
                PatternEncodingTermMenu.TYPE,
                PatternEncodingTermMenu.class,
                registration.getTransferHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        registration.addRecipes(InscriberRecipeCategory.RECIPE_TYPE,
                List.copyOf(recipeManager.byType(InscriberRecipe.TYPE).values()));
        registration.addRecipes(ChargerCategory.RECIPE_TYPE,
                List.copyOf(recipeManager.byType(ChargerRecipe.TYPE).values()));
        registration.addRecipes(CondenserCategory.RECIPE_TYPE,
                ImmutableList.of(CondenserOutput.MATTER_BALLS, CondenserOutput.SINGULARITY));
        registration.addRecipes(EntropyManipulatorCategory.TYPE,
                List.copyOf(recipeManager.byType(EntropyRecipe.TYPE).values()));
        registration.addRecipes(TransformCategory.RECIPE_TYPE,
                List.copyOf(recipeManager.byType(TransformRecipe.TYPE).values()));

        registerP2PAttunement(registration);
        registerDescriptions(registration);

        registration.addItemStackInfo(
                AEBlocks.CRANK.stack(),
                ItemModText.CRANK_DESCRIPTION.text());

        registration.addRecipes(CertusGrowthCategory.TYPE, List.of(CertusGrowthCategory.Page.values()));
    }

    private void registerP2PAttunement(IRecipeRegistration registration) {

        List<AttunementDisplay> attunementRecipes = new ArrayList<>();
        for (var entry : P2PTunnelAttunementInternal.getApiTunnels()) {
            attunementRecipes.add(
                    new AttunementDisplay(
                            Ingredient.of(Registry.ITEM.stream()
                                    .map(ItemStack::new)
                                    .filter(entry.stackPredicate())
                                    .toArray(ItemStack[]::new)),
                            entry.tunnelType(),
                            ItemModText.P2P_API_ATTUNEMENT.text(),
                            entry.description()));
        }

        for (var entry : P2PTunnelAttunementInternal.getTagTunnels().entrySet()) {
            attunementRecipes.add(new AttunementDisplay(
                    Ingredient.of(entry.getKey()),
                    entry.getValue(),
                    ItemModText.P2P_TAG_ATTUNEMENT.text()));
        }

        // Remove attunements with empty ingredients
        attunementRecipes.removeIf(a -> a.inputs().isEmpty());

        registration.addRecipes(AttunementCategory.TYPE, attunementRecipes);

    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        var condenser = AEBlocks.CONDENSER.stack();
        registration.addRecipeCatalyst(condenser, CondenserCategory.RECIPE_TYPE);

        var inscriber = AEBlocks.INSCRIBER.stack();
        registration.addRecipeCatalyst(inscriber, InscriberRecipeCategory.RECIPE_TYPE);

        var craftingTerminal = AEParts.CRAFTING_TERMINAL.stack();
        registration.addRecipeCatalyst(craftingTerminal, RecipeTypes.CRAFTING);

        var wirelessCraftingTerminal = AEItems.WIRELESS_CRAFTING_TERMINAL.stack();
        registration.addRecipeCatalyst(wirelessCraftingTerminal, RecipeTypes.CRAFTING);

        // Both the charger and crank will be used as catalysts here to make it more discoverable
        registration.addRecipeCatalyst(AEBlocks.CHARGER.stack(), ChargerCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(AEBlocks.CRANK.stack(), ChargerCategory.RECIPE_TYPE);

        registration.addRecipeCatalyst(AEItems.ENTROPY_MANIPULATOR.stack(), EntropyManipulatorCategory.TYPE);
    }

    private void registerDescriptions(IRecipeRegistration registry) {
        this.addDescription(registry, AEItems.LOGIC_PROCESSOR_PRESS,
                GuiText.inWorldCraftingPresses.text());
        this.addDescription(registry, AEItems.CALCULATION_PROCESSOR_PRESS,
                GuiText.inWorldCraftingPresses.text());
        this.addDescription(registry, AEItems.ENGINEERING_PROCESSOR_PRESS,
                GuiText.inWorldCraftingPresses.text());
        this.addDescription(registry, AEItems.SILICON_PRESS,
                GuiText.inWorldCraftingPresses.text());
    }

    private void addDescription(IRecipeRegistration registry, ItemDefinition<?> itemDefinition,
            Component... message) {
        registry.addIngredientInfo(itemDefinition.stack(), VanillaTypes.ITEM_STACK, message);
    }

    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {
        if (AEConfig.instance().isEnableFacadeRecipesInJEI()) {
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
                                    IGuiClickableArea.createBasic(82, 39, 26, 16, InscriberRecipeCategory.RECIPE_TYPE));
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

        if (!AEConfig.instance().isEnableFacadesInJEI()) {
            jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK,
                    FacadeItemGroup.INSTANCE.getSubTypes());
        }
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

            jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK,
                    toRemove);
        }

    }

    // Copy-pasted from JEI since it doesn't seem to expose these
    public static void drawHoveringText(PoseStack poseStack, List<Component> textLines, int x, int y) {
        var minecraft = Minecraft.getInstance();
        var screen = minecraft.screen;
        if (screen == null) {
            return;
        }

        screen.renderTooltip(poseStack, textLines, Optional.empty(), x, y);
    }
}
