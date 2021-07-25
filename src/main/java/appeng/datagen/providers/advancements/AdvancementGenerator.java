package appeng.datagen.providers.advancements;

import appeng.api.util.AEColor;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.stats.AdvancementTriggers;
import appeng.datagen.providers.IAE2DataProvider;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;

public class AdvancementGenerator implements IAE2DataProvider {

    public static final Tag.Named<Item> GLASS_CABLE = ItemTags.bind("appliedenergistics2:glass_cable");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final DataGenerator generator;

    public AdvancementGenerator(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void run(HashCache cache) {
        Path path = this.generator.getOutputFolder();
        Set<ResourceLocation> set = Sets.newHashSet();
        Consumer<Advancement> consumer = (advancement) -> {
            if (!set.add(advancement.getId())) {
                throw new IllegalStateException("Duplicate advancement " + advancement.getId());
            } else {
                Path path1 = createPath(path, advancement);

                try {
                    DataProvider.save(GSON, cache, advancement.deconstruct().serializeToJson(), path1);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        };

        generateAdvancements(consumer);
    }

    private void generateAdvancements(Consumer<Advancement> consumer) {

        var root = Advancement.Builder.advancement()
                .display(
                        AEItems.CERTUS_QUARTZ_CRYSTAL,
                        new TranslatableComponent("achievement.ae2.Root"),
                        new TranslatableComponent("achievement.ae2.Root.desc"),
                        AppEng.makeId("textures/block/sky_stone_brick.png"),
                        FrameType.TASK,
                        false /* showToast */,
                        false /* announceChat */,
                        false /* hidden */
                )
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.CERTUS_QUARTZ_CRYSTAL))
                .save(consumer, "appliedenergistics2:main/root");

        var chargedQuartz = Advancement.Builder.advancement()
                .display(
                        AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED,
                        new TranslatableComponent("achievement.ae2.ChargedQuartz"),
                        new TranslatableComponent("achievement.ae2.ChargedQuartz.desc"),
                        null /* background */,
                        FrameType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(root)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED))
                .save(consumer, "appliedenergistics2:main/charged_quartz");

        var charger = Advancement.Builder.advancement()
                .display(
                        AEBlocks.CHARGER,
                        new TranslatableComponent("achievement.ae2.Charger"),
                        new TranslatableComponent("achievement.ae2.Charger.desc"),
                        null /* background */,
                        FrameType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(chargedQuartz)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.CHARGER))
                .save(consumer, "appliedenergistics2:main/charger");

        var compass = Advancement.Builder.advancement()
                .display(
                        AEBlocks.SKY_COMPASS,
                        new TranslatableComponent("achievement.ae2.Compass"),
                        new TranslatableComponent("achievement.ae2.Compass.desc"),
                        null /* background */,
                        FrameType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(root)
                .addCriterion("compass", InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.SKY_COMPASS))
                .save(consumer, "appliedenergistics2:main/compass");

        var presses = Advancement.Builder.advancement()
                .display(
                        AEItems.LOGIC_PROCESSOR_PRESS,
                        new TranslatableComponent("achievement.ae2.Presses"),
                        new TranslatableComponent("achievement.ae2.Presses.desc"),
                        null /* background */,
                        FrameType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(root)
                .addCriterion("calc", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.CALCULATION_PROCESSOR_PRESS))
                .addCriterion("eng", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.ENGINEERING_PROCESSOR_PRESS))
                .addCriterion("logic", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.LOGIC_PROCESSOR_PRESS))
                .addCriterion("silicon", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.SILICON_PRESS))
                .requirements(RequirementsStrategy.OR)
                .save(consumer, "appliedenergistics2:main/presses");

        var controller = Advancement.Builder.advancement()
                .display(
                        AEBlocks.CONTROLLER,
                        new TranslatableComponent("achievement.ae2.Controller"),
                        new TranslatableComponent("achievement.ae2.Controller.desc"),
                        null /* background */,
                        FrameType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(presses)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.CONTROLLER))
                .save(consumer, "appliedenergistics2:main/controller");

        var storageCell = Advancement.Builder.advancement()
                .display(
                        AEItems.CELL64K,
                        new TranslatableComponent("achievement.ae2.StorageCell"),
                        new TranslatableComponent("achievement.ae2.StorageCell.desc"),
                        null /* background */,
                        FrameType.TASK,
                        false,
                        false,
                        false
                )
                .parent(controller)
                .addCriterion("c1k", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.CELL1K))
                .addCriterion("c4k", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.CELL4K))
                .addCriterion("c16k", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.CELL16K))
                .addCriterion("c64k", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.CELL64K))
                .requirements(RequirementsStrategy.OR)
                .save(consumer, "appliedenergistics2:main/storage_cell");

        var ioport = Advancement.Builder.advancement()
                .display(
                        AEBlocks.IO_PORT,
                        new TranslatableComponent("achievement.ae2.IOPort"),
                        new TranslatableComponent("achievement.ae2.IOPort.desc"),
                        null /* background */,
                        FrameType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(storageCell)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.IO_PORT))
                .save(consumer, "appliedenergistics2:main/ioport");

        var craftingTerminal = Advancement.Builder.advancement()
                .display(
                        AEParts.CRAFTING_TERMINAL,
                        new TranslatableComponent("achievement.ae2.CraftingTerminal"),
                        new TranslatableComponent("achievement.ae2.CraftingTerminal.desc"),
                        null /* background */,
                        FrameType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(controller)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEParts.CRAFTING_TERMINAL))
                .save(consumer, "appliedenergistics2:main/crafting_terminal");

        var patternTerminal = Advancement.Builder.advancement()
                .display(
                        AEParts.PATTERN_TERMINAL,
                        new TranslatableComponent("achievement.ae2.PatternTerminal"),
                        new TranslatableComponent("achievement.ae2.PatternTerminal.desc"),
                        null /* background */,
                        FrameType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(craftingTerminal)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEParts.PATTERN_TERMINAL))
                .save(consumer, "appliedenergistics2:main/pattern_terminal");

        var craftingCpu = Advancement.Builder.advancement()
                .display(
                        AEBlocks.CRAFTING_STORAGE_64K,
                        new TranslatableComponent("achievement.ae2.CraftingCPU"),
                        new TranslatableComponent("achievement.ae2.CraftingCPU.desc"),
                        null /* background */,
                        FrameType.TASK,
                        false,
                        false,
                        false
                )
                .parent(patternTerminal)
                .addCriterion("c1k", InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.CRAFTING_STORAGE_1K))
                .addCriterion("c4k", InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.CRAFTING_STORAGE_4K))
                .addCriterion("c16k", InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.CRAFTING_STORAGE_16K))
                .addCriterion("c64k", InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.CRAFTING_STORAGE_64K))
                .requirements(RequirementsStrategy.OR)
                .save(consumer, "appliedenergistics2:main/crafting_cpu");

        var fluix = Advancement.Builder.advancement()
                .display(
                        AEItems.FLUIX_CRYSTAL,
                        new TranslatableComponent("achievement.ae2.Fluix"),
                        new TranslatableComponent("achievement.ae2.Fluix.desc"),
                        null /* background */,
                        FrameType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(chargedQuartz)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.FLUIX_CRYSTAL))
                .save(consumer, "appliedenergistics2:main/fluix");

        var glassCable = Advancement.Builder.advancement()
                .display(
                        AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT),
                        new TranslatableComponent("achievement.ae2.GlassCable"),
                        new TranslatableComponent("achievement.ae2.GlassCable.desc"),
                        null /* background */,
                        FrameType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(fluix)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(GLASS_CABLE).build()))
                .save(consumer, "appliedenergistics2:main/glass_cable");

        var facade = Advancement.Builder.advancement()
                .display(
                        AEItems.FACADE.asItem().createFacadeForItemUnchecked(new ItemStack(Items.STONE)),
                        new TranslatableComponent("achievement.ae2.Facade"),
                        new TranslatableComponent("achievement.ae2.Facade.desc"),
                        null /* background */,
                        FrameType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(glassCable)
                .addCriterion("facade", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.FACADE))
                .save(consumer, "appliedenergistics2:main/facade");

        var growthAccelerator = Advancement.Builder.advancement()
                .display(
                        AEBlocks.QUARTZ_GROWTH_ACCELERATOR,
                        new TranslatableComponent("achievement.ae2.CrystalGrowthAccelerator"),
                        new TranslatableComponent("achievement.ae2.CrystalGrowthAccelerator.desc"),
                        null /* background */,
                        FrameType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(fluix)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.QUARTZ_GROWTH_ACCELERATOR))
                .save(consumer, "appliedenergistics2:main/growth_accelerator");

        var iface = Advancement.Builder.advancement()
                .display(
                        AEBlocks.ITEM_INTERFACE,
                        new TranslatableComponent("achievement.ae2.Recursive"),
                        new TranslatableComponent("achievement.ae2.Recursive.desc"),
                        null /* background */,
                        FrameType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(glassCable)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.ITEM_INTERFACE))
                .save(consumer, "appliedenergistics2:main/interface");

        var network1 = Advancement.Builder.advancement()
                .display(
                        AEParts.COVERED_CABLE.item(AEColor.TRANSPARENT),
                        new TranslatableComponent("achievement.ae2.Networking1"),
                        new TranslatableComponent("achievement.ae2.Networking1.desc"),
                        null /* background */,
                        FrameType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(glassCable)
                .addCriterion("cable", AdvancementTriggers.NETWORK_APPRENTICE.instance())
                .save(consumer, "appliedenergistics2:main/network1");

        var network2 = Advancement.Builder.advancement()
                .display(
                        AEParts.SMART_CABLE.item(AEColor.TRANSPARENT),
                        new TranslatableComponent("achievement.ae2.Networking2"),
                        new TranslatableComponent("achievement.ae2.Networking2.desc"),
                        null /* background */,
                        FrameType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(network1)
                .addCriterion("cable", AdvancementTriggers.NETWORK_ENGINEER.instance())
                .save(consumer, "appliedenergistics2:main/network2");

        var network3 = Advancement.Builder.advancement()
                .display(
                        AEParts.SMART_DENSE_CABLE.item(AEColor.TRANSPARENT),
                        new TranslatableComponent("achievement.ae2.Networking3"),
                        new TranslatableComponent("achievement.ae2.Networking3.desc"),
                        null /* background */,
                        FrameType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(network2)
                .addCriterion("cable", AdvancementTriggers.NETWORK_ADMIN.instance())
                .save(consumer, "appliedenergistics2:main/network3");

        var networkTool = Advancement.Builder.advancement()
                .display(
                        AEItems.NETWORK_TOOL,
                        new TranslatableComponent("achievement.ae2.NetworkTool"),
                        new TranslatableComponent("achievement.ae2.NetworkTool.desc"),
                        null /* background */,
                        FrameType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(controller)
                .addCriterion("network_tool", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.NETWORK_TOOL))
                .save(consumer, "appliedenergistics2:main/network_tool");

        var p2p = Advancement.Builder.advancement()
                .display(
                        AEParts.ME_P2P_TUNNEL,
                        new TranslatableComponent("achievement.ae2.P2P"),
                        new TranslatableComponent("achievement.ae2.P2P.desc"),
                        null /* background */,
                        FrameType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(glassCable)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEParts.ME_P2P_TUNNEL))
                .save(consumer, "appliedenergistics2:main/p2p");

        var portableCell = Advancement.Builder.advancement()
                .display(
                        AEItems.PORTABLE_CELL1K,
                        new TranslatableComponent("achievement.ae2.PortableCell"),
                        new TranslatableComponent("achievement.ae2.PortableCell.desc"),
                        null /* background */,
                        FrameType.TASK,
                        false,
                        false,
                        false
                )
                .parent(storageCell)
                .addCriterion("pc_1k", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.PORTABLE_CELL1K))
                .addCriterion("pc_4k", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.PORTABLE_CELL4k))
                .addCriterion("pc_16k", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.PORTABLE_CELL16K))
                .addCriterion("pc_64k", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.PORTABLE_CELL64K))
                .requirements(RequirementsStrategy.OR)
                .save(consumer, "appliedenergistics2:main/portable_cell");

        var qnb = Advancement.Builder.advancement()
                .display(
                        AEBlocks.QUANTUM_LINK,
                        new TranslatableComponent("achievement.ae2.QNB"),
                        new TranslatableComponent("achievement.ae2.QNB.desc"),
                        null /* background */,
                        FrameType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(p2p)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.QUANTUM_LINK))
                .save(consumer, "appliedenergistics2:main/qnb");

        var spatialIoport = Advancement.Builder.advancement()
                .display(
                        AEBlocks.SPATIAL_IO_PORT,
                        new TranslatableComponent("achievement.ae2.SpatialIO"),
                        new TranslatableComponent("achievement.ae2.SpatialIO.desc"),
                        null /* background */,
                        FrameType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(ioport)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.SPATIAL_IO_PORT))
                .save(consumer, "appliedenergistics2:main/spatial_ioport");

        var spatialExplorer = Advancement.Builder.advancement()
                .display(
                        AEItems.SPATIAL_128_CELL_COMPONENT,
                        new TranslatableComponent("achievement.ae2.SpatialIOExplorer"),
                        new TranslatableComponent("achievement.ae2.SpatialIOExplorer.desc"),
                        null /* background */,
                        FrameType.TASK,
                        false,
                        false,
                        false
                )
                .parent(spatialIoport)
                .addCriterion("explorer", AdvancementTriggers.SPATIAL_EXPLORER.instance())
                .save(consumer, "appliedenergistics2:main/spatial_explorer");

        var storageBus = Advancement.Builder.advancement()
                .display(
                        AEParts.ITEM_STORAGE_BUS,
                        new TranslatableComponent("achievement.ae2.StorageBus"),
                        new TranslatableComponent("achievement.ae2.StorageBus.desc"),
                        null /* background */,
                        FrameType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(iface)
                .addCriterion("part", InventoryChangeTrigger.TriggerInstance.hasItems(AEParts.ITEM_STORAGE_BUS))
                .save(consumer, "appliedenergistics2:main/storage_bus");


    }

    private static Path createPath(Path basePath, Advancement advancement) {
        return basePath.resolve("data/" + advancement.getId().getNamespace()
                + "/advancements/" + advancement.getId().getPath() + ".json");
    }

    @Override
    public String getName() {
        return "Advancements";
    }
}
