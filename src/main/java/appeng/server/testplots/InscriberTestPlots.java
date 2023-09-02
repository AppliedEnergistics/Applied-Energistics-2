package appeng.server.testplots;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import appeng.api.stacks.AEItemKey;
import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.items.materials.NamePressItem;

public final class InscriberTestPlots {
    private InscriberTestPlots() {
    }

    @TestPlotGenerator
    public static void generateInscriberRecipePlots(TestPlotCollection tests) {

        var namePlate = AEItems.NAME_PRESS.stack();
        namePlate.addTagElement(NamePressItem.TAG_INSCRIBE_NAME, StringTag.valueOf("HELLO WORLD"));
        var ironIngots = new ItemStack(Items.IRON_INGOT, 2);
        var namedIngots = ironIngots.copy();
        namedIngots.setHoverName(Component.literal("HELLO WORLD"));

        addTest(
                "nameplate", tests,
                namePlate, ironIngots, ItemStack.EMPTY,
                namePlate, ItemStack.EMPTY, ItemStack.EMPTY,
                namedIngots);

        addPrintTest(tests, AEItems.SILICON, AEItems.SILICON_PRESS, AEItems.SILICON_PRINT);
        addPrintTest(tests, Items.GOLD_INGOT, AEItems.LOGIC_PROCESSOR_PRESS, AEItems.LOGIC_PROCESSOR_PRINT);
        addPrintTest(tests, Items.DIAMOND, AEItems.ENGINEERING_PROCESSOR_PRESS, AEItems.ENGINEERING_PROCESSOR_PRINT);
        addPrintTest(tests, AEItems.CERTUS_QUARTZ_CRYSTAL, AEItems.CALCULATION_PROCESSOR_PRESS,
                AEItems.CALCULATION_PROCESSOR_PRINT);
    }

    private static void addPrintTest(TestPlotCollection tests, ItemLike ingredient, ItemLike press,
            ItemLike expectedResult) {

        var suffix = AEItemKey.of(ingredient).getId().getPath() + "_print";

        addTest(
                "inscriber_" + suffix,
                tests,
                new ItemStack(press),
                new ItemStack(ingredient, 2),
                ItemStack.EMPTY,
                new ItemStack(press),
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                new ItemStack(expectedResult, 2));

    }

    private static void addTest(String suffix, TestPlotCollection tests, ItemStack topSlot, ItemStack middleSlot,
            ItemStack bottomSlot,
            ItemStack expectedTopSlot, ItemStack expectedMiddleSlot, ItemStack expectedBottomSlot,
            ItemStack expectedResult) {
        tests.add("inscriber_recipe_" + suffix, plot -> {
            plot.creativeEnergyCell(BlockPos.ZERO.below());
            plot.blockEntity(BlockPos.ZERO, AEBlocks.INSCRIBER, be -> {
                be.getUpgrades().addItems(AEItems.SPEED_CARD.stack());
                be.getUpgrades().addItems(AEItems.SPEED_CARD.stack());
                be.getUpgrades().addItems(AEItems.SPEED_CARD.stack());
                be.getUpgrades().addItems(AEItems.SPEED_CARD.stack());
                be.getUpgrades().addItems(AEItems.SPEED_CARD.stack());

                var inv = be.getInternalInventory();
                inv.insertItem(0, topSlot.copy(), false);
                inv.insertItem(1, bottomSlot.copy(), false);
                inv.insertItem(2, middleSlot.copy(), false);
            });
        }, plotTestHelper -> {
            plotTestHelper.startSequence()
                    .thenWaitUntil(() -> {
                        var inscriber = (InscriberBlockEntity) plotTestHelper.getBlockEntity(BlockPos.ZERO);
                        var inv = inscriber.getInternalInventory();
                        plotTestHelper.check(
                                ItemStack.isSameItemSameTags(inv.getStackInSlot(0), expectedTopSlot),
                                "Top slot is not as expected",
                                BlockPos.ZERO);
                        plotTestHelper.check(
                                ItemStack.isSameItemSameTags(inv.getStackInSlot(1), expectedBottomSlot),
                                "Bottom slot is not as expected",
                                BlockPos.ZERO);
                        plotTestHelper.check(
                                ItemStack.isSameItemSameTags(inv.getStackInSlot(2), expectedMiddleSlot),
                                "Middle slot is not as expected",
                                BlockPos.ZERO);
                        plotTestHelper.check(
                                ItemStack.isSameItemSameTags(inv.getStackInSlot(3), expectedResult),
                                "Result slot is not as expected",
                                BlockPos.ZERO);
                    })
                    .thenSucceed();
        });
    }

}
