package appeng.server.testplots;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import appeng.api.ids.AEComponents;
import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

@TestPlotClass
public final class InscriberTestPlots {
    private InscriberTestPlots() {
    }

    @TestPlotGenerator
    public static void generateInscriberRecipePlots(TestPlotCollection tests) {

        var namePlate = AEItems.NAME_PRESS
                .template(patch -> patch.set(AEComponents.NAME_PRESS_NAME, Component.literal("HELLO WORLD")));
        ;
        var ironIngots = new ItemStackTemplate(Items.IRON_INGOT, 2);
        var namedIngots = new ItemStackTemplate(Items.IRON_INGOT, 2, DataComponentPatch.builder()
                .set(DataComponents.CUSTOM_NAME, Component.literal("HELLO WORLD"))
                .build());

        addTest(
                "nameplate", tests,
                namePlate, ironIngots, null,
                namePlate, null, null,
                namedIngots);

        addPrintTest(tests, AEItems.SILICON, AEItems.SILICON_PRESS, AEItems.SILICON_PRINT);
        addPrintTest(tests, Items.GOLD_INGOT, AEItems.LOGIC_PROCESSOR_PRESS, AEItems.LOGIC_PROCESSOR_PRINT);
        addPrintTest(tests, Items.DIAMOND, AEItems.ENGINEERING_PROCESSOR_PRESS, AEItems.ENGINEERING_PROCESSOR_PRINT);
        addPrintTest(tests, AEItems.CERTUS_QUARTZ_CRYSTAL, AEItems.CALCULATION_PROCESSOR_PRESS,
                AEItems.CALCULATION_PROCESSOR_PRINT);
    }

    private static void addPrintTest(TestPlotCollection tests, ItemLike ingredient, ItemLike press,
            ItemLike expectedResult) {

        var suffix = BuiltInRegistries.ITEM.getKey(ingredient.asItem()).getPath() + "_print";

        addTest(
                "inscriber_" + suffix,
                tests,
                new ItemStackTemplate(press.asItem()),
                new ItemStackTemplate(ingredient.asItem(), 2),
                null,
                new ItemStackTemplate(press.asItem()),
                null,
                null,
                new ItemStackTemplate(expectedResult.asItem(), 2));

    }

    private static void addTest(String suffix,
            TestPlotCollection tests,
            @Nullable ItemStackTemplate topSlot,
            ItemStackTemplate middleSlot,
            @Nullable ItemStackTemplate bottomSlot,
            @Nullable ItemStackTemplate expectedTopSlot,
            @Nullable ItemStackTemplate expectedMiddleSlot,
            @Nullable ItemStackTemplate expectedBottomSlot,
            ItemStackTemplate expectedResult) {
        tests.add("inscriber_recipe_" + suffix, plot -> {
            plot.creativeEnergyCell(BlockPos.ZERO.below());
            plot.blockEntity(BlockPos.ZERO, AEBlocks.INSCRIBER, be -> {
                be.getUpgrades().addItems(AEItems.SPEED_CARD.stack());
                be.getUpgrades().addItems(AEItems.SPEED_CARD.stack());
                be.getUpgrades().addItems(AEItems.SPEED_CARD.stack());
                be.getUpgrades().addItems(AEItems.SPEED_CARD.stack());
                be.getUpgrades().addItems(AEItems.SPEED_CARD.stack());

                var inv = be.getInternalInventory();
                inv.insertItem(0, topSlot != null ? topSlot.create() : ItemStack.EMPTY, false);
                inv.insertItem(1, bottomSlot != null ? bottomSlot.create() : ItemStack.EMPTY, false);
                inv.insertItem(2, middleSlot != null ? middleSlot.create() : ItemStack.EMPTY, false);
            });
        }, plotTestHelper -> {
            plotTestHelper.startSequence()
                    .thenWaitUntil(() -> {
                        var inscriber = plotTestHelper.getBlockEntity(BlockPos.ZERO, InscriberBlockEntity.class);
                        var inv = inscriber.getInternalInventory();
                        plotTestHelper.check(
                                ItemStack.isSameItemSameComponents(inv.getStackInSlot(0), expectedTopSlot),
                                "Top slot is not as expected",
                                BlockPos.ZERO);
                        plotTestHelper.check(
                                ItemStack.isSameItemSameComponents(inv.getStackInSlot(1), expectedBottomSlot),
                                "Bottom slot is not as expected",
                                BlockPos.ZERO);
                        plotTestHelper.check(
                                ItemStack.isSameItemSameComponents(inv.getStackInSlot(2), expectedMiddleSlot),
                                "Middle slot is not as expected",
                                BlockPos.ZERO);
                        plotTestHelper.check(
                                ItemStack.isSameItemSameComponents(inv.getStackInSlot(3), expectedResult),
                                "Result slot is not as expected",
                                BlockPos.ZERO);
                    })
                    .thenSucceed();
        });
    }

}
