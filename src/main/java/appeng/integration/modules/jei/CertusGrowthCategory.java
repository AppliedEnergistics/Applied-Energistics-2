package appeng.integration.modules.jei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.localization.ItemModText;
import appeng.decorative.solid.BuddingCertusQuartzBlock;
import appeng.integration.modules.jei.widgets.View;
import appeng.integration.modules.jei.widgets.Widget;
import appeng.integration.modules.jei.widgets.WidgetFactory;

/**
 * Displays information about growing quartz from {@link appeng.decorative.solid.BuddingCertusQuartzBlock}.
 */
public class CertusGrowthCategory extends ViewBasedCategory<CertusGrowthCategory.Page> {

    public static RecipeType<Page> TYPE = RecipeType.create(AppEng.MOD_ID, "certus_growth", Page.class);

    private final List<ItemStack> BUDDING_QUARTZ_VARIANTS = List.of(
            AEBlocks.DAMAGED_BUDDING_QUARTZ.stack(),
            AEBlocks.CHIPPED_BUDDING_QUARTZ.stack(),
            AEBlocks.FLAWED_BUDDING_QUARTZ.stack(),
            AEBlocks.FLAWLESS_BUDDING_QUARTZ.stack());

    private final List<Item> BUDDING_QUARTZ_DECAY_ORDER = List.of(
            AEBlocks.QUARTZ_BLOCK.asItem(),
            AEBlocks.DAMAGED_BUDDING_QUARTZ.asItem(),
            AEBlocks.CHIPPED_BUDDING_QUARTZ.asItem(),
            AEBlocks.FLAWED_BUDDING_QUARTZ.asItem());

    private final List<ItemStack> BUD_GROWTH_STAGES = List.of(
            AEBlocks.SMALL_QUARTZ_BUD.stack(),
            AEBlocks.MEDIUM_QUARTZ_BUD.stack(),
            AEBlocks.LARGE_QUARTZ_BUD.stack(),
            AEBlocks.QUARTZ_CLUSTER.stack());

    /**
     * Default is 4. Fortune applies, though.
     */
    private final ItemStack END_RESULT = AEItems.CERTUS_QUARTZ_CRYSTAL.stack(4);

    private final IDrawable background;
    private final IDrawable slotBackground;
    private final IDrawable icon;
    private int centerX;

    public CertusGrowthCategory(IJeiHelpers helpers) {
        super(helpers);
        var guiHelper = helpers.getGuiHelper();
        this.background = guiHelper.createBlankDrawable(150, 60);
        this.slotBackground = guiHelper.getSlotDrawable();
        this.icon = CyclingDrawable.forItems(
                guiHelper,
                AEBlocks.SMALL_QUARTZ_BUD,
                AEBlocks.MEDIUM_QUARTZ_BUD,
                AEBlocks.LARGE_QUARTZ_BUD,
                AEBlocks.QUARTZ_CLUSTER);
        this.centerX = background.getWidth() / 2;
    }

    @Override
    public RecipeType<Page> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return ItemModText.CERTUS_QUARTZ_GROWTH.text();
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    protected View getView(Page page) {
        return switch (page) {
            /*
             * This page explains that buds grow on budding quartz.
             */
            case BUD_GROWTH -> new View() {
                @Override
                public void createWidgets(WidgetFactory factory, List<Widget> widgets) {
                    widgets.add(factory.label(centerX, 0, ItemModText.QUARTZ_BUDS_GROW_ON_BUDDING_QUARTZ.text())
                            .bodyText()
                            .maxWidth(background.getWidth()));

                    widgets.add(factory.unfilledArrow(centerX - 12, 25));
                }

                @Override
                public void buildSlots(IRecipeLayoutBuilder builder) {
                    builder.addSlot(RecipeIngredientRole.CATALYST, centerX - 40, 25)
                            .setBackground(slotBackground, -1, -1)
                            .addItemStacks(BUDDING_QUARTZ_VARIANTS);

                    builder.addSlot(RecipeIngredientRole.OUTPUT, centerX + 40 - 18, 25)
                            .setBackground(slotBackground, -1, -1)
                            .addItemStacks(BUD_GROWTH_STAGES);
                }
            };
            /*
             * These pages explain the loot for buds and crystal clusters, and that fortune is useful.
             */
            case BUD_LOOT, CLUSTER_LOOT -> new View() {
                @Override
                public void createWidgets(WidgetFactory factory, List<Widget> widgets) {
                    Component text;
                    if (page == Page.BUD_LOOT) {
                        text = ItemModText.BUDS_DROP_DUST_WHEN_NOT_FULLY_GROWN.text();
                    } else {
                        text = ItemModText.FULLY_GROWN_BUDS_DROP_CRYSTALS.text();
                    }
                    widgets.add(factory.label(centerX, 0, text)
                            .bodyText()
                            .maxWidth(background.getWidth()));

                    widgets.add(factory.unfilledArrow(centerX - 12, 25));

                    if (page == Page.CLUSTER_LOOT) {
                        widgets.add(factory.label(centerX, 50, ItemModText.FORTUNE_APPLIES.text())
                                .bodyText());
                    }
                }

                @Override
                public void buildSlots(IRecipeLayoutBuilder builder) {
                    List<ItemStack> input;
                    if (page == Page.BUD_LOOT) {
                        input = List.of(
                                AEBlocks.SMALL_QUARTZ_BUD.stack(),
                                AEBlocks.MEDIUM_QUARTZ_BUD.stack(),
                                AEBlocks.LARGE_QUARTZ_BUD.stack());
                    } else {
                        input = List.of(
                                AEBlocks.QUARTZ_CLUSTER.stack());
                    }

                    builder.addSlot(RecipeIngredientRole.INPUT, centerX - 40, 25)
                            .setBackground(slotBackground, -1, -1)
                            .addItemStacks(input);

                    ItemStack finalResult;
                    if (page == Page.BUD_LOOT) {
                        finalResult = AEItems.CERTUS_QUARTZ_DUST.stack();
                    } else {
                        finalResult = AEItems.CERTUS_QUARTZ_CRYSTAL.stack(4);
                    }
                    var outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, centerX + 40 - 18, 25)
                            .setBackground(slotBackground, -1, -1)
                            .addItemStack(finalResult);
                }
            };
            /*
             * This page explains that budding quartz decays when buds grow on it.
             */
            case BUDDING_QUARTZ_DECAY -> new View() {

                @Override
                public void createWidgets(WidgetFactory factory, List<Widget> widgets) {
                    widgets.add(factory.label(centerX, 0, ItemModText.IMPERFECT_BUDDING_QUARTZ_DECAYS.text())
                            .bodyText()
                            .maxWidth(background.getWidth()));

                    widgets.add(factory.unfilledArrow(centerX - 12, 30));

                    var decayChancePct = 100 / BuddingCertusQuartzBlock.DECAY_CHANCE;
                    widgets.add(factory.label(centerX, 50, ItemModText.DECAY_CHANCE.text(decayChancePct))
                            .bodyText());
                }

                @Override
                public void buildSlots(IRecipeLayoutBuilder builder) {
                    var decayingVariants = new ArrayList<>(BUDDING_QUARTZ_VARIANTS);
                    decayingVariants.removeIf(is -> !BUDDING_QUARTZ_DECAY_ORDER.contains(is.getItem()));

                    var slot1 = builder.addSlot(RecipeIngredientRole.INPUT, centerX - 40, 30)
                            .setBackground(slotBackground, -1, -1)
                            .addItemStacks(decayingVariants);

                    var slot2 = builder.addSlot(RecipeIngredientRole.OUTPUT, centerX + 40 - 18, 30)
                            .setBackground(slotBackground, -1, -1)
                            .addItemStacks(BUDDING_QUARTZ_DECAY_ORDER.stream().map(ItemStack::new).toList());

                    builder.createFocusLink(slot1, slot2);
                }
            };
            /*
             * This page explains how budding quartz can be moved.
             */
            case BUDDING_QUARTZ_MOVING -> new View() {
                @Override
                public void buildSlots(IRecipeLayoutBuilder builder) {
                    builder.addSlot(RecipeIngredientRole.INPUT, centerX - 40, 22)
                            .setBackground(slotBackground, -1, -1)
                            .addItemStacks(BUDDING_QUARTZ_VARIANTS);

                    builder.addSlot(RecipeIngredientRole.OUTPUT, centerX + 40 - 18, 22)
                            .setBackground(slotBackground, -1, -1)
                            .addItemStack(AEBlocks.QUARTZ_BLOCK.stack());
                }

                @Override
                public void createWidgets(WidgetFactory factory, List<Widget> widgets) {
                    widgets.add(factory.label(centerX, 0, ItemModText.BUDDING_QUARTZ_DECAYS_WHEN_BROKEN.text())
                            .bodyText()
                            .maxWidth(background.getWidth()));

                    widgets.add(factory.unfilledArrow(centerX - 12, 22));

                    widgets.add(factory.label(centerX, 42, ItemModText.SILK_TOUCH_PREVENTS_DECAY_FOR_IMPERFECT.text())
                            .bodyText());
                    widgets.add(factory.label(centerX, 53, ItemModText.SPATIAL_IO_NEVER_CAUSES_ANY_DECAY.text())
                            .bodyText());
                }
            };
            /*
             * This page explains how budding quartz can be made or be found.
             */
            case GETTING_BUDDING_QUARTZ -> new View() {
                @Override
                public void createWidgets(WidgetFactory factory, List<Widget> widgets) {
                    widgets.add(factory.label(22, 13, ItemModText.BUDDING_QUARTZ_CREATION_AND_WORLDGEN.text())
                            .bodyText()
                            .alignLeft()
                            .maxWidth(background.getWidth() - 20));
                }

                @Override
                public void buildSlots(IRecipeLayoutBuilder builder) {
                    // Also include quartz blocks in the list, since those can spawn in meteorites
                    var decayingVariants = BUDDING_QUARTZ_DECAY_ORDER.stream()
                            .map(ItemStack::new)
                            .toList();

                    builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                            .setBackground(slotBackground, -1, -1)
                            .addItemStacks(decayingVariants);

                    builder.addSlot(RecipeIngredientRole.INPUT, 1, 22)
                            .setBackground(slotBackground, -1, -1)
                            .addItemStack(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.stack());

                    builder.addSlot(RecipeIngredientRole.CATALYST, 1, 43)
                            .setBackground(slotBackground, -1, -1)
                            .addItemStack(AEItems.METEORITE_COMPASS.stack());
                }
            };
            /*
             * This page explains what flawless budding quartz is.
             */
            case FLAWLESS_BUDDING_QUARTZ -> new View() {
                @Override
                public void createWidgets(WidgetFactory factory, List<Widget> widgets) {
                    widgets.add(factory.label(22, 13, ItemModText.FLAWLESS_BUDDING_QUARTZ_DESCRIPTION.text())
                            .bodyText()
                            .alignLeft()
                            .maxWidth(background.getWidth() - 20));
                }

                @Override
                public void buildSlots(IRecipeLayoutBuilder builder) {
                    builder.addSlot(RecipeIngredientRole.CATALYST, 1, 13)
                            .setBackground(slotBackground, -1, -1)
                            .addItemStack(AEBlocks.FLAWLESS_BUDDING_QUARTZ.stack());

                    builder.addSlot(RecipeIngredientRole.INPUT, 1, 33)
                            .setBackground(slotBackground, -1, -1)
                            .addItemStack(AEItems.METEORITE_COMPASS.stack());
                }
            };
            /*
             * This page explains how budding quartz can be initially found.
             */
            case BUDDING_QUARTZ_ACCELERATION -> new View() {
                @Override
                public void createWidgets(WidgetFactory factory, List<Widget> widgets) {
                    var centerX = background.getWidth() / 2;

                    widgets.add(factory.label(centerX, 0, ItemModText.CRYSTAL_GROWTH_ACCELERATORS_EFFECT.text())
                            .bodyText()
                            .maxWidth(background.getWidth()));

                    widgets.add(factory.label(centerX, 45, Component.literal("+")));
                }

                @Override
                public void buildSlots(IRecipeLayoutBuilder builder) {
                    builder.addSlot(RecipeIngredientRole.INPUT, centerX - 8 - 16, 40)
                            .setBackground(slotBackground, -1, -1)
                            .addItemStacks(BUDDING_QUARTZ_VARIANTS);

                    builder.addSlot(RecipeIngredientRole.CATALYST, centerX + 8, 40)
                            .setBackground(slotBackground, -1, -1)
                            .addItemStack(AEBlocks.QUARTZ_GROWTH_ACCELERATOR.stack());
                }
            };
        };
    }

    public enum Page {
        BUD_GROWTH,
        BUD_LOOT,
        CLUSTER_LOOT,
        BUDDING_QUARTZ_DECAY,
        BUDDING_QUARTZ_MOVING,
        GETTING_BUDDING_QUARTZ,
        FLAWLESS_BUDDING_QUARTZ,
        BUDDING_QUARTZ_ACCELERATION
    }
}
