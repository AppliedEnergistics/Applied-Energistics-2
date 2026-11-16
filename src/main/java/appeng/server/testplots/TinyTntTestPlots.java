package appeng.server.testplots;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import appeng.core.AEConfig;
import appeng.entity.TinyTNTPrimedEntity;
import appeng.server.testworld.PlotBuilder;
import appeng.server.testworld.PlotTestHelper;

@TestPlotClass
public final class TinyTntTestPlots {
    private TinyTntTestPlots() {
    }

    @TestPlot(value = "tiny_tnt_preserves_block_entity_loot", padding = 3)
    public static void preservesBlockEntityLoot(PlotBuilder plot) {
        var boxPos = BlockPos.ZERO;
        plot.block(boxPos, Blocks.SHULKER_BOX);

        plot.test(helper -> {
            var box = helper.getBlockEntity(boxPos, ShulkerBoxBlockEntity.class);
            var namedBox = new ItemStack(Items.SHULKER_BOX);
            namedBox.set(DataComponents.CUSTOM_NAME, Component.literal("Tiny TNT regression"));
            box.applyComponentsFromItemStack(namedBox);
            box.setItem(0, new ItemStack(Items.DIAMOND, 17));

            explode(helper, boxPos);

            helper.assertBlockPresent(Blocks.AIR, boxPos);
            var drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                    new AABB(helper.absolutePos(boxPos)).inflate(2),
                    entity -> entity.getItem().is(Items.SHULKER_BOX));
            helper.check(drops.size() == 1, "Expected exactly one dropped shulker box", boxPos);
            var droppedBox = drops.getFirst().getItem();
            helper.check(droppedBox.getCount() == 1, "Shulker box was duplicated", boxPos);
            var contents = droppedBox.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
                    .nonEmptyItemCopyStream().toList();
            helper.check(contents.size() == 1 && contents.getFirst().is(Items.DIAMOND)
                    && contents.getFirst().getCount() == 17,
                    "Explosion loot lost or duplicated the block entity's inventory", boxPos);
            helper.check(Component.literal("Tiny TNT regression").equals(droppedBox.get(DataComponents.CUSTOM_NAME)),
                    "Explosion loot lost the block entity's custom name", boxPos);
            helper.succeed();
        });
    }

    @TestPlot(value = "tiny_tnt_block_damage", padding = 3)
    public static void blockDamage(PlotBuilder plot) {
        var origin = BlockPos.ZERO;
        plot.block(origin, Blocks.STONE);
        plot.block(origin.east(), Blocks.OBSIDIAN);
        plot.block(origin.west(), Blocks.BEDROCK);
        plot.block(origin.south(3), Blocks.STONE);

        plot.test(helper -> {
            explode(helper, origin);
            helper.assertBlockPresent(Blocks.AIR, origin);
            helper.assertBlockPresent(Blocks.OBSIDIAN, origin.east());
            helper.assertBlockPresent(Blocks.BEDROCK, origin.west());
            helper.assertBlockPresent(Blocks.STONE, origin.south(3));
            helper.succeed();
        });
    }

    @TestPlot(value = "tiny_tnt_primes_vanilla_tnt", padding = 3)
    public static void primesVanillaTnt(PlotBuilder plot) {
        var tntPos = BlockPos.ZERO;
        plot.block(tntPos, Blocks.TNT);

        plot.test(helper -> {
            explode(helper, tntPos);
            helper.assertBlockPresent(Blocks.AIR, tntPos);
            var primed = helper.getLevel().getEntitiesOfClass(PrimedTnt.class,
                    new AABB(helper.absolutePos(tntPos)).inflate(2));
            helper.check(primed.size() == 1, "Expected one primed TNT from the explosion callback", tntPos);
            primed.forEach(PrimedTnt::discard);
            helper.succeed();
        });
    }

    private static void explode(PlotTestHelper helper, BlockPos pos) {
        helper.check(AEConfig.instance().isTinyTntBlockDamageEnabled(), "Tiny TNT block damage must be enabled");
        var center = Vec3.atCenterOf(helper.absolutePos(pos));
        var tnt = new TinyTNTPrimedEntity(helper.getLevel(), center.x, center.y, center.z, null);
        tnt.setDeltaMovement(Vec3.ZERO);
        tnt.setFuse(0);
        // Run the real entity tick synchronously so newly dropped items are not damaged by later explosions.
        tnt.tick();
    }
}
