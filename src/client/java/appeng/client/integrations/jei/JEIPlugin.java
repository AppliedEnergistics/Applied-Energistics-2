package appeng.client.integrations.jei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IJeiRuntime;

import appeng.api.config.CondenserOutput;
import appeng.api.features.P2PTunnelAttunementInternal;
import appeng.api.ids.AEComponents;
import appeng.client.AppEngClient;
import appeng.client.api.integrations.jei.IngredientConverters;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.InscriberScreen;
import appeng.client.integrations.jei.transfer.EncodePatternTransferHandler;
import appeng.client.integrations.jei.transfer.FilterTransferHandler;
import appeng.client.integrations.jei.transfer.UseCraftingRecipeTransfer;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.FacadeCreativeTab;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.ItemDefinition;
import appeng.core.localization.GuiText;
import appeng.core.localization.ItemModText;
import appeng.integration.abstraction.ItemListMod;
import appeng.integration.abstraction.ItemListModAdapter;
import appeng.menu.implementations.IOBusMenu;
import appeng.menu.implementations.InterfaceMenu;
import appeng.menu.implementations.StorageBusMenu;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.me.items.WirelessCraftingTermMenu;
import appeng.recipes.AERecipeTypes;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    public static final Identifier TEXTURE = AppEng.makeId("textures/guis/jei.png");

    private static final Identifier ID = AppEng.makeId("core");

    private static IJeiRuntime jeiRuntime;

    public JEIPlugin() {
        IngredientConverters.register(new ItemIngredientConverter());
        IngredientConverters.register(new FluidIngredientConverter());
    }

    @Override
    public Identifier getPluginUid() {
        return ID;
    }

    private static ISubtypeInterpreter<ItemStack> subTypeInterpreterForIngredientList(DataComponentType<?>... types) {
        return (ingredient, context) -> {
            if (context == UidContext.Recipe) {
                return null;
            }
            if (types.length == 1) {
                return ingredient.get(types[0]);
            } else {
                var result = new ArrayList<@Nullable Object>(types.length);
                for (var type : types) {
                    result.add(ingredient.get(type));
                }
                return result;
            }
        };
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration subtypeRegistry) {
        subtypeRegistry.registerFromDataComponentTypes(AEItems.FACADE.asItem(), AEComponents.FACADE_ITEM);
        subtypeRegistry.registerSubtypeInterpreter(AEItems.COLOR_APPLICATOR.asItem(),
                subTypeInterpreterForIngredientList(AEComponents.STORED_ENERGY));
        subtypeRegistry.registerSubtypeInterpreter(AEItems.CHARGED_STAFF.asItem(),
                subTypeInterpreterForIngredientList(AEComponents.STORED_ENERGY));
        subtypeRegistry.registerSubtypeInterpreter(AEItems.MATTER_CANNON.asItem(),
                subTypeInterpreterForIngredientList(AEComponents.STORED_ENERGY));
        subtypeRegistry.registerSubtypeInterpreter(AEItems.ENTROPY_MANIPULATOR.asItem(),
                subTypeInterpreterForIngredientList(AEComponents.STORED_ENERGY));
        subtypeRegistry.registerSubtypeInterpreter(AEItems.WIRELESS_TERMINAL.asItem(),
                subTypeInterpreterForIngredientList(AEComponents.STORED_ENERGY));
        subtypeRegistry.registerSubtypeInterpreter(AEItems.WIRELESS_CRAFTING_TERMINAL.asItem(),
                subTypeInterpreterForIngredientList(AEComponents.STORED_ENERGY));
        subtypeRegistry.registerSubtypeInterpreter(AEBlocks.ENERGY_CELL.asItem(),
                subTypeInterpreterForIngredientList(AEComponents.STORED_ENERGY));
        subtypeRegistry.registerSubtypeInterpreter(AEBlocks.DENSE_ENERGY_CELL.asItem(),
                subTypeInterpreterForIngredientList(AEComponents.STORED_ENERGY));
        subtypeRegistry.registerSubtypeInterpreter(AEParts.ANNIHILATION_PLANE.asItem(),
                subTypeInterpreterForIngredientList(DataComponents.ENCHANTMENTS));
        subtypeRegistry.registerSubtypeInterpreter(AEItems.PORTABLE_ITEM_CELL1K.asItem(),
                subTypeInterpreterForIngredientList(AEComponents.STORED_ENERGY));
        subtypeRegistry.registerSubtypeInterpreter(AEItems.PORTABLE_ITEM_CELL4K.asItem(),
                subTypeInterpreterForIngredientList(AEComponents.STORED_ENERGY));
        subtypeRegistry.registerSubtypeInterpreter(AEItems.PORTABLE_ITEM_CELL16K.asItem(),
                subTypeInterpreterForIngredientList(AEComponents.STORED_ENERGY));
        subtypeRegistry.registerSubtypeInterpreter(AEItems.PORTABLE_ITEM_CELL64K.asItem(),
                subTypeInterpreterForIngredientList(AEComponents.STORED_ENERGY));
        subtypeRegistry.registerSubtypeInterpreter(AEItems.PORTABLE_ITEM_CELL256K.asItem(),
                subTypeInterpreterForIngredientList(AEComponents.STORED_ENERGY));
        subtypeRegistry.registerSubtypeInterpreter(AEItems.PORTABLE_FLUID_CELL1K.asItem(),
                subTypeInterpreterForIngredientList(AEComponents.STORED_ENERGY));
        subtypeRegistry.registerSubtypeInterpreter(AEItems.PORTABLE_FLUID_CELL4K.asItem(),
                subTypeInterpreterForIngredientList(AEComponents.STORED_ENERGY));
        subtypeRegistry.registerSubtypeInterpreter(AEItems.PORTABLE_FLUID_CELL16K.asItem(),
                subTypeInterpreterForIngredientList(AEComponents.STORED_ENERGY));
        subtypeRegistry.registerSubtypeInterpreter(AEItems.PORTABLE_FLUID_CELL64K.asItem(),
                subTypeInterpreterForIngredientList(AEComponents.STORED_ENERGY));
        subtypeRegistry.registerSubtypeInterpreter(AEItems.PORTABLE_FLUID_CELL256K.asItem(),
                subTypeInterpreterForIngredientList(AEComponents.STORED_ENERGY));
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
        // Some additional universal handlers for filtered ME parts.
        registration.addUniversalRecipeTransferHandler(new FilterTransferHandler<>(
                InterfaceMenu.TYPE,
                InterfaceMenu.class,
                registration.getTransferHelper()));
        registration.addUniversalRecipeTransferHandler(new FilterTransferHandler<>(
                StorageBusMenu.TYPE,
                StorageBusMenu.class,
                registration.getTransferHelper()));
        registration.addUniversalRecipeTransferHandler(new FilterTransferHandler<>(
                IOBusMenu.EXPORT_TYPE,
                IOBusMenu.class,
                registration.getTransferHelper()));

    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        addSyncedRecipes(registration, InscriberRecipeCategory.RECIPE_TYPE, AERecipeTypes.INSCRIBER);
        addSyncedRecipes(registration, ChargerCategory.RECIPE_TYPE, AERecipeTypes.CHARGER);
        addSyncedRecipes(registration, EntropyManipulatorCategory.TYPE, AERecipeTypes.ENTROPY);
        addSyncedRecipes(registration, TransformCategory.RECIPE_TYPE, AERecipeTypes.TRANSFORM);
        registration.addRecipes(CondenserCategory.RECIPE_TYPE,
                List.of(CondenserOutput.MATTER_BALLS, CondenserOutput.SINGULARITY));

        registerP2PAttunement(registration);
        registerDescriptions(registration);

        registration.addItemStackInfo(
                AEBlocks.CRANK.stack(),
                ItemModText.CRANK_DESCRIPTION.text());

        registration.addRecipes(CertusGrowthCategory.TYPE, List.of(CertusGrowthCategory.Page.values()));
    }

    private static <I extends RecipeInput, T extends Recipe<I>> void addSyncedRecipes(IRecipeRegistration registration,
            IRecipeType<RecipeHolder<T>> recipeType, RecipeType<T> vanillaRecipeType) {
        var recipes = AppEngClient.instance().getRecipeMapForType(Minecraft.getInstance().level, vanillaRecipeType);
        var recipeHolders = List.copyOf(recipes.byType(vanillaRecipeType));
        registration.addRecipes(recipeType, recipeHolders);
    }

    private void registerP2PAttunement(IRecipeRegistration registration) {

        List<AttunementDisplay> attunementRecipes = new ArrayList<>();
        for (var entry : P2PTunnelAttunementInternal.getApiTunnels()) {
            attunementRecipes.add(
                    new AttunementDisplay(
                            Ingredient.of(BuiltInRegistries.ITEM.stream()
                                    .filter(i -> entry.stackPredicate().test(i.getDefaultInstance()))
                                    .toArray(Item[]::new)),
                            entry.tunnelType(),
                            ItemModText.P2P_API_ATTUNEMENT.text(),
                            entry.description()));
        }

        for (var entry : P2PTunnelAttunementInternal.getTagTunnels().entrySet()) {
            attunementRecipes.add(new AttunementDisplay(
                    Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(entry.getKey())),
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
        registration.addCraftingStation(CondenserCategory.RECIPE_TYPE, condenser);

        var inscriber = AEBlocks.INSCRIBER.stack();
        registration.addCraftingStation(InscriberRecipeCategory.RECIPE_TYPE, inscriber);

        var craftingTerminal = AEParts.CRAFTING_TERMINAL.stack();
        registration.addCraftingStation(RecipeTypes.CRAFTING, craftingTerminal);

        var wirelessCraftingTerminal = AEItems.WIRELESS_CRAFTING_TERMINAL.stack();
        registration.addCraftingStation(RecipeTypes.CRAFTING, wirelessCraftingTerminal);

        // Both the charger and crank will be used as catalysts here to make it more discoverable
        registration.addCraftingStation(ChargerCategory.RECIPE_TYPE, AEBlocks.CHARGER.stack());
        registration.addCraftingStation(ChargerCategory.RECIPE_TYPE, AEBlocks.CRANK.stack());

        registration.addCraftingStation(EntropyManipulatorCategory.TYPE, AEItems.ENTROPY_MANIPULATOR.stack());
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
        if (AEConfig.instance().isEnableFacadeRecipesInRecipeViewer()) {
            registration.addRecipeManagerPlugin(new FacadeRegistryPlugin());
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

                    @Override
                    public Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(
                            IClickableIngredientFactory builder, AEBaseScreen<?> screen, double mouseX, double mouseY) {
                        // The following code allows the player to show recipes involving fluids in AE fluid terminals
                        // or AE fluid tanks shown in fluid interfaces and other UI.
                        var stackWithBounds = screen.getStackUnderMouse(mouseX, mouseY);
                        if (stackWithBounds != null) {
                            var ingredient = GenericEntryStackHelper.stackToIngredient(
                                    jeiRuntime.getIngredientManager(),
                                    stackWithBounds.stack());
                            if (ingredient == null) {
                                return Optional.empty();
                            }

                            return builder.createBuilder(ingredient).buildWithArea(stackWithBounds.bounds());
                        }

                        return Optional.empty();
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
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        JEIPlugin.jeiRuntime = jeiRuntime;
        ItemListMod.setAdapter(new JeiItemListModAdapter(jeiRuntime));
        this.hideDebugTools(jeiRuntime);

        if (!AEConfig.instance().isEnableFacadesInRecipeViewer()) {
            jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK,
                    FacadeCreativeTab.getDisplayItems());
        }
    }

    @Override
    public void onRuntimeUnavailable() {
        // Free memory potentially held by JEI
        ItemListMod.setAdapter(ItemListModAdapter.none());
    }

    private void hideDebugTools(IJeiRuntime jeiRuntime) {
        if (!AEConfig.instance().isDebugToolsEnabled()) {
            Collection<ItemStack> toRemove = new ArrayList<>();

            // We use the internal API here as exception as debug tools are not part of the public one by design.
            toRemove.add(AEBlocks.DEBUG_CUBE_GEN.stack());
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
    public static void drawHoveringText(GuiGraphicsExtractor guiGraphics, List<Component> textLines, int x, int y) {
        var font = Minecraft.getInstance().font;
        guiGraphics.setTooltipForNextFrame(font, textLines, Optional.empty(), ItemStack.EMPTY, x, y);
    }

    public static IJeiRuntime instance() {
        return jeiRuntime;
    }
}
