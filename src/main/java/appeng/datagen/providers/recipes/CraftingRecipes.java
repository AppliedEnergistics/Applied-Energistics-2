
package appeng.datagen.providers.recipes;

import java.util.function.Consumer;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import appeng.api.ids.AEItemIds;
import appeng.api.ids.AETags;
import appeng.api.stacks.AEKeyType;
import appeng.api.util.AEColor;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.ItemDefinition;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.items.tools.powered.PortableCellItem;

public class CraftingRecipes extends AE2RecipeProvider {
    public CraftingRecipes(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildAE2CraftingRecipes(Consumer<FinishedRecipe> consumer) {

        // ====================================================
        // Basic Cards
        // ====================================================
        ShapedRecipeBuilder.shaped(AEItems.BASIC_CARD, 2)
                .pattern("ab ")
                .pattern("cdb")
                .pattern("ab ")
                .define('a', ConventionTags.GOLD_INGOT)
                .define('b', ConventionTags.IRON_INGOT)
                .define('c', ConventionTags.REDSTONE)
                .define('d', AEItems.CALCULATION_PROCESSOR)
                .unlockedBy("has_calculation_processor", has(AEItems.CALCULATION_PROCESSOR))
                .save(consumer, AppEng.makeId("materials/basiccard"));
        ShapelessRecipeBuilder.shapeless(AEItems.CAPACITY_CARD)
                .requires(ConventionTags.ALL_CERTUS_QUARTZ)
                .requires(AEItems.BASIC_CARD)
                .unlockedBy("has_basic_card", has(AEItems.BASIC_CARD))
                .save(consumer, AppEng.makeId("materials/cardcapacity"));
        ShapelessRecipeBuilder.shapeless(AEItems.CRAFTING_CARD)
                .requires(Items.CRAFTING_TABLE)
                .requires(AEItems.BASIC_CARD)
                .unlockedBy("has_basic_card", has(AEItems.BASIC_CARD))
                .save(consumer, AppEng.makeId("materials/cardcrafting"));
        ShapelessRecipeBuilder.shapeless(AEItems.REDSTONE_CARD)
                .requires(Items.REDSTONE_TORCH)
                .requires(AEItems.BASIC_CARD)
                .unlockedBy("has_basic_card", has(AEItems.BASIC_CARD))
                .save(consumer, AppEng.makeId("materials/cardredstone"));
        ShapelessRecipeBuilder.shapeless(AEItems.VOID_CARD)
                .requires(AEItems.CALCULATION_PROCESSOR)
                .requires(AEItems.BASIC_CARD)
                .unlockedBy("has_basic_card", has(AEItems.BASIC_CARD))
                .save(consumer, AppEng.makeId("materials/cardvoid"));

        // ====================================================
        // Advanced Cards
        // ====================================================
        ShapedRecipeBuilder.shaped(AEItems.ADVANCED_CARD, 2)
                .pattern("ab ")
                .pattern("cdb")
                .pattern("ab ")
                .define('a', ConventionTags.DIAMOND)
                .define('b', ConventionTags.IRON_INGOT)
                .define('c', ConventionTags.REDSTONE)
                .define('d', AEItems.CALCULATION_PROCESSOR)
                .unlockedBy("has_calculation_processor", has(AEItems.CALCULATION_PROCESSOR))
                .save(consumer, AppEng.makeId("materials/advancedcard"));
        ShapelessRecipeBuilder.shapeless(AEItems.FUZZY_CARD)
                .requires(AEItems.ADVANCED_CARD)
                .requires(ItemTags.WOOL)
                .unlockedBy("has_advanced_card", has(AEItems.ADVANCED_CARD))
                .save(consumer, AppEng.makeId("materials/cardfuzzy"));
        ShapelessRecipeBuilder.shapeless(AEItems.INVERTER_CARD)
                .requires(Items.REDSTONE_TORCH)
                .requires(AEItems.ADVANCED_CARD)
                .unlockedBy("has_advanced_card", has(AEItems.ADVANCED_CARD))
                .save(consumer, AppEng.makeId("materials/cardinverter"));
        ShapelessRecipeBuilder.shapeless(AEItems.SPEED_CARD)
                .requires(AEItems.ADVANCED_CARD)
                .requires(ConventionTags.ALL_FLUIX)
                .unlockedBy("has_advanced_card", has(AEItems.ADVANCED_CARD))
                .save(consumer, AppEng.makeId("materials/cardspeed"));
        ShapelessRecipeBuilder.shapeless(AEItems.EQUAL_DISTRIBUTION_CARD)
                .requires(AEItems.ADVANCED_CARD)
                .requires(AEItems.CALCULATION_PROCESSOR)
                .unlockedBy("has_advanced_card", has(AEItems.ADVANCED_CARD))
                .save(consumer, AppEng.makeId("materials/carddistribution"));
        ShapedRecipeBuilder.shaped(AEItems.ENERGY_CARD)
                .pattern("ab")
                .define('a', AEBlocks.DENSE_ENERGY_CELL)
                .define('b', AEItems.ADVANCED_CARD)
                .unlockedBy("has_advanced_card", has(AEItems.ADVANCED_CARD))
                .save(consumer, AppEng.makeId("materials/cardenergy"));

        // ====================================================
        // Misc Materials
        // ====================================================
        ShapedRecipeBuilder.shaped(AEItems.ANNIHILATION_CORE, 2)
                .pattern("abc")
                .define('a', ConventionTags.ALL_NETHER_QUARTZ)
                .define('b', ConventionTags.FLUIX_DUST)
                .define('c', AEItems.LOGIC_PROCESSOR)
                .unlockedBy("has_logic_processor", has(AEItems.LOGIC_PROCESSOR))
                .save(consumer, AppEng.makeId("materials/annihilationcore"));
        ShapedRecipeBuilder.shaped(AEItems.FORMATION_CORE, 2)
                .pattern("abc")
                .define('a', ConventionTags.ALL_CERTUS_QUARTZ)
                .define('b', ConventionTags.FLUIX_DUST)
                .define('c', AEItems.LOGIC_PROCESSOR)
                .unlockedBy("has_logic_processor", has(AEItems.LOGIC_PROCESSOR))
                .save(consumer, AppEng.makeId("materials/formationcore"));

        // ====================================================
        // recipes/misc
        // ====================================================

        ShapedRecipeBuilder.shaped(AEBlocks.SKY_STONE_CHEST)
                .pattern("aaa")
                .pattern("a a")
                .pattern("aaa")
                .define('a', AEBlocks.SKY_STONE_BLOCK)
                .unlockedBy("has_sky_stone_block", has(AEBlocks.SKY_STONE_BLOCK))
                .save(consumer, AppEng.makeId("misc/chests_sky_stone"));
        ShapedRecipeBuilder.shaped(AEBlocks.SMOOTH_SKY_STONE_CHEST)
                .pattern("aaa")
                .pattern("a a")
                .pattern("aaa")
                .define('a', AEBlocks.SMOOTH_SKY_STONE_BLOCK)
                .unlockedBy("has_smooth_sky_stone_block", has(AEBlocks.SMOOTH_SKY_STONE_BLOCK))
                .save(consumer, AppEng.makeId("misc/chests_smooth_sky_stone"));
        ShapedRecipeBuilder.shaped(AEBlocks.SKY_STONE_TANK)
                .pattern("aaa")
                .pattern("aba")
                .pattern("aaa")
                .define('a', AEBlocks.SKY_STONE_BLOCK)
                .define('b', AEBlocks.QUARTZ_GLASS)
                .unlockedBy("has_sky_stone_block", has(AEBlocks.SKY_STONE_BLOCK))
                .save(consumer, AppEng.makeId("misc/tank_sky_stone"));

        ShapelessRecipeBuilder.shapeless(AEItems.CERTUS_QUARTZ_CRYSTAL, 4)
                .requires(AEBlocks.CHISELED_QUARTZ_BLOCK)
                .unlockedBy("has_chiseled_quartz_block", has(AEBlocks.CHISELED_QUARTZ_BLOCK))
                .save(consumer, AppEng.makeId("misc/deconstruction_certus_chiseled_quartz"));
        ShapelessRecipeBuilder.shapeless(AEItems.CERTUS_QUARTZ_CRYSTAL, 4)
                .requires(AEBlocks.QUARTZ_BLOCK)
                .unlockedBy("has_quartz_block", has(AEBlocks.QUARTZ_BLOCK))
                .save(consumer, AppEng.makeId("misc/deconstruction_certus_quartz_block"));
        ShapelessRecipeBuilder.shapeless(AEItems.CERTUS_QUARTZ_CRYSTAL, 4)
                .requires(AEBlocks.SMOOTH_QUARTZ_BLOCK)
                .unlockedBy("has_smooth_quartz_block", has(AEBlocks.SMOOTH_QUARTZ_BLOCK))
                .save(consumer, AppEng.makeId("misc/deconstruction_smooth_certus_quartz_block"));
        ShapelessRecipeBuilder.shapeless(AEItems.CERTUS_QUARTZ_CRYSTAL, 4)
                .requires(AEBlocks.QUARTZ_BRICKS)
                .unlockedBy("has_quartz_bricks", has(AEBlocks.QUARTZ_BRICKS))
                .save(consumer, AppEng.makeId("misc/deconstruction_certus_quartz_bricks"));
        ShapelessRecipeBuilder.shapeless(AEItems.CERTUS_QUARTZ_CRYSTAL, 4)
                .requires(AEBlocks.QUARTZ_PILLAR)
                .unlockedBy("has_quartz_pillar", has(AEBlocks.QUARTZ_PILLAR))
                .save(consumer, AppEng.makeId("misc/deconstruction_certus_quartz_pillar"));
        ShapelessRecipeBuilder.shapeless(AEItems.FLUIX_CRYSTAL, 4)
                .requires(AEBlocks.FLUIX_BLOCK)
                .unlockedBy("has_fluix_block", has(AEBlocks.FLUIX_BLOCK))
                .save(consumer, AppEng.makeId("misc/deconstruction_fluix_block"));
        ShapedRecipeBuilder.shaped(AEItems.FLUIX_PEARL)
                .pattern("aba")
                .pattern("bcb")
                .pattern("aba")
                .define('a', ConventionTags.FLUIX_DUST)
                .define('b', ConventionTags.ALL_FLUIX)
                .define('c', ConventionTags.ENDER_PEARL)
                .unlockedBy("has_dusts/fluix", has(ConventionTags.FLUIX_DUST))
                .unlockedBy("has_crystals/fluix", has(ConventionTags.ALL_FLUIX))
                .save(consumer, AppEng.makeId("misc/fluixpearl"));
        ShapedRecipeBuilder.shaped(AEBlocks.TINY_TNT)
                .pattern("ab")
                .pattern("ba")
                .define('a', ConventionTags.ALL_QUARTZ_DUST)
                .define('b', Items.GUNPOWDER)
                .unlockedBy("has_gunpowder", has(Items.GUNPOWDER))
                .unlockedBy("has_dusts/quartz", has(ConventionTags.ALL_QUARTZ_DUST))
                .save(consumer, AppEng.makeId("misc/tiny_tnt"));
        ShapedRecipeBuilder.shaped(Items.COMPARATOR)
                .pattern(" a ")
                .pattern("aba")
                .pattern("ccc")
                .define('a', Items.REDSTONE_TORCH)
                .define('b', ConventionTags.ALL_NETHER_QUARTZ)
                .define('c', ConventionTags.STONE)
                .unlockedBy("has_quartz", has(ConventionTags.ALL_NETHER_QUARTZ))
                .save(consumer, AppEng.makeId("misc/vanilla_comparator"));
        ShapedRecipeBuilder.shaped(Items.DAYLIGHT_DETECTOR)
                .pattern("aaa")
                .pattern("bbb")
                .pattern("ccc")
                .define('a', ConventionTags.GLASS)
                .define('b', ConventionTags.ALL_NETHER_QUARTZ)
                .define('c', ItemTags.WOODEN_SLABS)
                .unlockedBy("has_quartz", has(ConventionTags.ALL_NETHER_QUARTZ))
                .save(consumer, AppEng.makeId("misc/vanilla_daylight_detector"));

        // ====================================================
        // recipes/network
        // ====================================================

        ShapedRecipeBuilder.shaped(AEBlocks.WIRELESS_ACCESS_POINT)
                .pattern("a")
                .pattern("b")
                .pattern("c")
                .define('a', AEItems.WIRELESS_RECEIVER)
                .define('b', AEItems.CALCULATION_PROCESSOR)
                .define('c', AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT))
                .unlockedBy("has_wireless_receiver", has(AEItems.WIRELESS_RECEIVER))
                .save(consumer, AppEng.makeId("network/wireless_access_point"));
        ShapedRecipeBuilder.shaped(AEItems.WIRELESS_BOOSTER, 2)
                .pattern("abc")
                .pattern("ddd")
                .define('a', ConventionTags.FLUIX_DUST)
                .define('b', ConventionTags.ALL_CERTUS_QUARTZ)
                .define('c', ConventionTags.ENDER_PEARL_DUST)
                .define('d', ConventionTags.IRON_INGOT)
                .unlockedBy("has_dusts/ender", has(ConventionTags.ENDER_PEARL_DUST))
                .save(consumer, AppEng.makeId("network/wireless_booster"));
        ShapedRecipeBuilder.shaped(AEItems.WIRELESS_RECEIVER)
                .pattern(" a ")
                .pattern("bcb")
                .pattern(" b ")
                .define('a', AEItems.FLUIX_PEARL)
                .define('b', ConventionTags.IRON_INGOT)
                .define('c', AEParts.QUARTZ_FIBER)
                .unlockedBy("has_fluix_pearl", has(AEItems.FLUIX_PEARL))
                .save(consumer, AppEng.makeId("network/wireless_part"));
        ShapedRecipeBuilder.shaped(AEItems.WIRELESS_TERMINAL)
                .pattern("a")
                .pattern("b")
                .pattern("c")
                .define('a', AEItems.WIRELESS_RECEIVER)
                .define('b', AEParts.TERMINAL)
                .define('c', AEBlocks.DENSE_ENERGY_CELL)
                .unlockedBy("has_terminal", has(AEParts.TERMINAL))
                .unlockedBy("has_dense_energy_cell", has(AEBlocks.DENSE_ENERGY_CELL))
                .unlockedBy("has_wireless_receiver", has(AEItems.WIRELESS_RECEIVER))
                .save(consumer, AppEng.makeId("network/wireless_terminal"));
        ShapedRecipeBuilder.shaped(AEItems.WIRELESS_CRAFTING_TERMINAL)
                .pattern("a")
                .pattern("b")
                .pattern("c")
                .define('a', AEItems.WIRELESS_RECEIVER)
                .define('b', AEParts.CRAFTING_TERMINAL)
                .define('c', AEBlocks.DENSE_ENERGY_CELL)
                .unlockedBy("has_terminal", has(AEParts.CRAFTING_TERMINAL))
                .unlockedBy("has_dense_energy_cell", has(AEBlocks.DENSE_ENERGY_CELL))
                .unlockedBy("has_wireless_receiver", has(AEItems.WIRELESS_RECEIVER))
                .save(consumer, AppEng.makeId("network/wireless_crafting_terminal"));
        ShapelessRecipeBuilder.shapeless(AEItems.WIRELESS_CRAFTING_TERMINAL)
                .requires(AEItems.WIRELESS_TERMINAL)
                .requires(Items.CRAFTING_TABLE)
                .requires(AEItems.CALCULATION_PROCESSOR)
                .unlockedBy("has_terminal", has(AEItems.WIRELESS_TERMINAL))
                .unlockedBy("has_calculation_processor", has(AEItems.CALCULATION_PROCESSOR))
                .save(consumer, AppEng.makeId("network/upgrade_wireless_crafting_terminal"));

        // ====================================================
        // recipes/network/blocks
        // ====================================================

        ShapedRecipeBuilder.shaped(AEBlocks.CELL_WORKBENCH)
                .pattern("aba")
                .pattern("cdc")
                .pattern("ccc")
                .define('a', ItemTags.WOOL)
                .define('b', AEItems.CALCULATION_PROCESSOR)
                .define('c', ConventionTags.IRON_INGOT)
                .define('d', ConventionTags.CHEST)
                .unlockedBy("has_calculation_processor", has(AEItems.CALCULATION_PROCESSOR))
                .save(consumer, AppEng.makeId("network/blocks/cell_workbench"));
        ShapedRecipeBuilder.shaped(AEBlocks.CONTROLLER)
                .pattern("aba")
                .pattern("bcb")
                .pattern("aba")
                .define('a', AEBlocks.SMOOTH_SKY_STONE_BLOCK)
                .define('b', AEItems.FLUIX_CRYSTAL)
                .define('c', AEItems.ENGINEERING_PROCESSOR)
                .unlockedBy("has_purified_fluix_crystal", has(AEItems.FLUIX_CRYSTAL))
                .unlockedBy("has_engineering_processor", has(AEItems.ENGINEERING_PROCESSOR))
                .save(consumer, AppEng.makeId("network/blocks/controller"));
        ShapedRecipeBuilder.shaped(AEBlocks.CHARGER)
                .pattern("aba")
                .pattern("a  ")
                .pattern("aba")
                .define('a', ConventionTags.IRON_INGOT)
                .define('b', ConventionTags.COPPER_INGOT)
                .unlockedBy("has_crystals/fluix", has(ConventionTags.ALL_FLUIX))
                .save(consumer, AppEng.makeId("network/blocks/crystal_processing_charger"));
        ShapedRecipeBuilder.shaped(AEBlocks.CRANK)
                .pattern("aaa")
                .pattern("  a")
                .pattern("  b")
                .define('a', ConventionTags.WOOD_STICK)
                .define('b', ConventionTags.COPPER_INGOT)
                .unlockedBy("has_stick", has(ConventionTags.WOOD_STICK))
                .unlockedBy("has_copper_ingot", has(ConventionTags.COPPER_INGOT))
                .save(consumer, AppEng.makeId("network/blocks/crank"));
        ShapedRecipeBuilder.shaped(AEBlocks.QUARTZ_GROWTH_ACCELERATOR)
                .pattern("aba")
                .pattern("cdc")
                .pattern("aba")
                .define('a', ConventionTags.IRON_INGOT)
                .define('b', AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT))
                .define('c', AEBlocks.QUARTZ_GLASS)
                .define('d', AEBlocks.FLUIX_BLOCK)
                .unlockedBy("has_fluix_block", has(AEBlocks.FLUIX_BLOCK))
                .unlockedBy("has_glass_cable", has(AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT)))
                .unlockedBy("has_quartz_glass", has(AEBlocks.QUARTZ_GLASS))
                .save(consumer, AppEng.makeId("network/blocks/crystal_processing_quartz_growth_accelerator"));
        ShapedRecipeBuilder.shaped(AEBlocks.DENSE_ENERGY_CELL)
                .pattern("aaa")
                .pattern("aba")
                .pattern("aaa")
                .define('a', AEBlocks.ENERGY_CELL)
                .define('b', AEItems.CALCULATION_PROCESSOR)
                .unlockedBy("has_energy_cell", has(AEBlocks.ENERGY_CELL))
                .save(consumer, AppEng.makeId("network/blocks/energy_dense_energy_cell"));
        ShapedRecipeBuilder.shaped(AEBlocks.ENERGY_ACCEPTOR)
                .pattern("aba")
                .pattern("bcb")
                .pattern("aba")
                .define('a', ConventionTags.IRON_INGOT)
                .define('b', AEBlocks.QUARTZ_GLASS)
                .define('c', ConventionTags.COPPER_INGOT)
                .unlockedBy("has_crystals/fluix", has(ConventionTags.ALL_FLUIX))
                .save(consumer, AppEng.makeId("network/blocks/energy_energy_acceptor"));
        ShapelessRecipeBuilder.shapeless(AEBlocks.ENERGY_ACCEPTOR)
                .requires(AEParts.ENERGY_ACCEPTOR)
                .unlockedBy("has_cable_energy_acceptor", has(AEParts.ENERGY_ACCEPTOR))
                .save(consumer, AppEng.makeId("network/blocks/energy_energy_acceptor_alt"));
        ShapedRecipeBuilder.shaped(AEBlocks.ENERGY_CELL)
                .pattern("aba")
                .pattern("bcb")
                .pattern("aba")
                .define('a', ConventionTags.ALL_CERTUS_QUARTZ)
                .define('b', ConventionTags.FLUIX_DUST)
                .define('c', AEBlocks.QUARTZ_GLASS)
                .unlockedBy("has_quartz_glass", has(AEBlocks.QUARTZ_GLASS))
                .unlockedBy("has_dusts/fluix", has(ConventionTags.FLUIX_DUST))
                .unlockedBy("has_crystals/certus", has(ConventionTags.ALL_CERTUS_QUARTZ))
                .save(consumer, AppEng.makeId("network/blocks/energy_energy_cell"));
        ShapedRecipeBuilder.shaped(AEBlocks.VIBRATION_CHAMBER)
                .pattern("aaa")
                .pattern("aba")
                .pattern("aca")
                .define('a', ConventionTags.IRON_INGOT)
                .define('b', Items.FURNACE)
                .define('c', AEBlocks.ENERGY_ACCEPTOR)
                .unlockedBy("has_energy_acceptor", has(AEBlocks.ENERGY_ACCEPTOR))
                .save(consumer, AppEng.makeId("network/blocks/energy_vibration_chamber"));
        ShapedRecipeBuilder.shaped(AEBlocks.INSCRIBER)
                .pattern("aba")
                .pattern("c a")
                .pattern("aba")
                .define('a', ConventionTags.IRON_INGOT)
                .define('b', Items.STICKY_PISTON)
                .define('c', ConventionTags.COPPER_INGOT)
                .unlockedBy("has_crystals/fluix", has(ConventionTags.ALL_FLUIX))
                .save(consumer, AppEng.makeId("network/blocks/inscribers"));
        ShapedRecipeBuilder.shaped(AEBlocks.CONDENSER)
                .pattern("aba")
                .pattern("bcb")
                .pattern("aba")
                .define('a', ConventionTags.IRON_INGOT)
                .define('b', ConventionTags.GLASS)
                .define('c', ConventionTags.FLUIX_DUST)
                .unlockedBy("has_dusts/fluix", has(ConventionTags.FLUIX_DUST))
                .save(consumer, AppEng.makeId("network/blocks/io_condenser"));
        ShapedRecipeBuilder.shaped(AEBlocks.IO_PORT)
                .pattern("aaa")
                .pattern("bcb")
                .pattern("ded")
                .define('a', ConventionTags.GLASS)
                .define('b', AEBlocks.DRIVE)
                .define('c', AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT))
                .define('d', ConventionTags.IRON_INGOT)
                .define('e', AEItems.LOGIC_PROCESSOR)
                .unlockedBy("has_drive", has(AEBlocks.DRIVE))
                .save(consumer, AppEng.makeId("network/blocks/io_port"));
        ShapedRecipeBuilder.shaped(AEBlocks.PATTERN_PROVIDER)
                .pattern("aba")
                .pattern("c d")
                .pattern("aba")
                .define('a', ConventionTags.IRON_INGOT)
                .define('b', Items.CRAFTING_TABLE)
                .define('c', AEItems.ANNIHILATION_CORE)
                .define('d', AEItems.FORMATION_CORE)
                .unlockedBy("has_annihilation_core", has(AEItems.ANNIHILATION_CORE))
                .unlockedBy("has_formation_core", has(AEItems.FORMATION_CORE))
                .save(consumer, AppEng.makeId("network/blocks/pattern_providers_interface"));
        ShapelessRecipeBuilder.shapeless(AEBlocks.PATTERN_PROVIDER)
                .requires(AEParts.PATTERN_PROVIDER)
                .unlockedBy("has_cable_pattern_provider", has(AEParts.PATTERN_PROVIDER))
                .save(consumer, AppEng.makeId("network/blocks/pattern_providers_interface_alt"));
        ShapelessRecipeBuilder.shapeless(AEParts.PATTERN_PROVIDER)
                .requires(AEBlocks.PATTERN_PROVIDER)
                .unlockedBy("has_pattern_provider", has(AEBlocks.PATTERN_PROVIDER))
                .save(consumer, AppEng.makeId("network/blocks/pattern_providers_interface_part"));
        ShapedRecipeBuilder.shaped(AEBlocks.INTERFACE)
                .pattern("aba")
                .pattern("c d")
                .pattern("aba")
                .define('a', ConventionTags.IRON_INGOT)
                .define('b', ConventionTags.GLASS)
                .define('c', AEItems.ANNIHILATION_CORE)
                .define('d', AEItems.FORMATION_CORE)
                .unlockedBy("has_annihilation_core", has(AEItems.ANNIHILATION_CORE))
                .unlockedBy("has_formation_core", has(AEItems.FORMATION_CORE))
                .save(consumer, AppEng.makeId("network/blocks/interfaces_interface"));
        ShapelessRecipeBuilder.shapeless(AEBlocks.INTERFACE)
                .requires(AEParts.INTERFACE)
                .unlockedBy("has_cable_interface", has(AEParts.INTERFACE))
                .save(consumer, AppEng.makeId("network/blocks/interfaces_interface_alt"));
        ShapelessRecipeBuilder.shapeless(AEParts.INTERFACE)
                .requires(AEBlocks.INTERFACE)
                .unlockedBy("has_interface", has(AEBlocks.INTERFACE))
                .save(consumer, AppEng.makeId("network/blocks/interfaces_interface_part"));
        ShapedRecipeBuilder.shaped(AEBlocks.QUANTUM_LINK)
                .pattern("aba")
                .pattern("b b")
                .pattern("aba")
                .define('a', AEBlocks.QUARTZ_GLASS)
                .define('b', AEItems.FLUIX_PEARL)
                .unlockedBy("has_fluix_pearl", has(AEItems.FLUIX_PEARL))
                .save(consumer, AppEng.makeId("network/blocks/quantum_link"));
        ShapedRecipeBuilder.shaped(AEBlocks.QUANTUM_RING)
                .pattern("aba")
                .pattern("cde")
                .pattern("aba")
                .define('a', ConventionTags.IRON_INGOT)
                .define('b', AEItems.LOGIC_PROCESSOR)
                .define('c', AEItems.ENGINEERING_PROCESSOR)
                .define('d', AEBlocks.ENERGY_CELL)
                .define('e', ConventionTags.SMART_DENSE_CABLE)
                .unlockedBy("has_fluix_smart_dense_cable", has(ConventionTags.SMART_DENSE_CABLE))
                .unlockedBy("has_energy_cell", has(AEBlocks.ENERGY_CELL))
                .unlockedBy("has_logic_processor", has(AEItems.LOGIC_PROCESSOR))
                .unlockedBy("has_engineering_processor", has(AEItems.ENGINEERING_PROCESSOR))
                .save(consumer, AppEng.makeId("network/blocks/quantum_ring"));
        ShapedRecipeBuilder.shaped(AEBlocks.SECURITY_STATION)
                .pattern("aba")
                .pattern("cdc")
                .pattern("aea")
                .define('a', ConventionTags.IRON_INGOT)
                .define('b', AEBlocks.CHEST)
                .define('c', AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT))
                .define('d', AEItems.CELL_COMPONENT_16K)
                .define('e', AEItems.ENGINEERING_PROCESSOR)
                .unlockedBy("has_engineering_processor", has(AEItems.ENGINEERING_PROCESSOR))
                .save(consumer, AppEng.makeId("network/blocks/security_station"));
        ShapedRecipeBuilder.shaped(AEBlocks.SPATIAL_ANCHOR)
                .pattern("aaa")
                .pattern("bcb")
                .pattern("ded")
                .define('a', AEBlocks.SPATIAL_PYLON)
                .define('b', AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT))
                .define('c', AEItems.SPATIAL_128_CELL_COMPONENT)
                .define('d', ConventionTags.IRON_INGOT)
                .define('e', AEItems.ENGINEERING_PROCESSOR)
                .unlockedBy("has_128_cubed_spatial_cell_component", has(AEItems.SPATIAL_128_CELL_COMPONENT))
                .unlockedBy("has_spatial_pylon", has(AEBlocks.SPATIAL_PYLON))
                .save(consumer, AppEng.makeId("network/blocks/spatial_anchor"));
        ShapedRecipeBuilder.shaped(AEBlocks.SPATIAL_IO_PORT)
                .pattern("aaa")
                .pattern("bcb")
                .pattern("ded")
                .define('a', ConventionTags.GLASS)
                .define('b', AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT))
                .define('c', AEBlocks.IO_PORT)
                .define('d', ConventionTags.IRON_INGOT)
                .define('e', AEItems.ENGINEERING_PROCESSOR)
                .unlockedBy("has_io_port", has(AEBlocks.IO_PORT))
                .unlockedBy("has_engineering_processor", has(AEItems.ENGINEERING_PROCESSOR))
                .save(consumer, AppEng.makeId("network/blocks/spatial_io_port"));
        ShapedRecipeBuilder.shaped(AEBlocks.SPATIAL_PYLON)
                .pattern("aba")
                .pattern("cdc")
                .pattern("aba")
                .define('a', AEBlocks.QUARTZ_GLASS)
                .define('b', AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT))
                .define('c', ConventionTags.FLUIX_DUST)
                .define('d', ConventionTags.ALL_FLUIX)
                .unlockedBy("has_crystals/fluix", has(ConventionTags.ALL_FLUIX))
                .unlockedBy("has_glass_cable", has(AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT)))
                .save(consumer, AppEng.makeId("network/blocks/spatial_io_pylon"));
        ShapedRecipeBuilder.shaped(AEBlocks.CHEST)
                .pattern("aba")
                .pattern("c c")
                .pattern("ded")
                .define('a', ConventionTags.GLASS)
                .define('b', AEParts.TERMINAL)
                .define('c', AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT))
                .define('d', ConventionTags.IRON_INGOT)
                .define('e', ConventionTags.COPPER_INGOT)
                .unlockedBy("has_glass_cable", has(AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT)))
                .unlockedBy("has_terminal", has(AEParts.TERMINAL))
                .unlockedBy("has_crystals/fluix", has(ConventionTags.ALL_FLUIX))
                .save(consumer, AppEng.makeId("network/blocks/storage_chest"));
        ShapedRecipeBuilder.shaped(AEBlocks.DRIVE)
                .pattern("aba")
                .pattern("c c")
                .pattern("aba")
                .define('a', ConventionTags.IRON_INGOT)
                .define('b', AEItems.ENGINEERING_PROCESSOR)
                .define('c', AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT))
                .unlockedBy("has_engineering_processor", has(AEItems.ENGINEERING_PROCESSOR))
                .save(consumer, AppEng.makeId("network/blocks/storage_drive"));

        addCables(consumer);

        // ====================================================
        // recipes/network/cells
        // ====================================================

        ShapedRecipeBuilder.shaped(AEItems.ITEM_CELL_HOUSING)
                .pattern("aba")
                .pattern("b b")
                .pattern("ccc")
                .define('a', AEBlocks.QUARTZ_GLASS)
                .define('b', ConventionTags.REDSTONE)
                .define('c', ConventionTags.IRON_INGOT)
                .unlockedBy("has_dusts/redstone", has(ConventionTags.REDSTONE))
                .save(consumer, AppEng.makeId("network/cells/item_cell_housing"));
        ShapedRecipeBuilder.shaped(AEItems.FLUID_CELL_HOUSING)
                .pattern("aba")
                .pattern("b b")
                .pattern("ccc")
                .define('a', AEBlocks.QUARTZ_GLASS)
                .define('b', ConventionTags.REDSTONE)
                .define('c', ConventionTags.COPPER_INGOT)
                .unlockedBy("has_dusts/redstone", has(ConventionTags.REDSTONE))
                .save(consumer, AppEng.makeId("network/cells/fluid_cell_housing"));

        addFluidCells(consumer);
        addItemCells(consumer);
        addSpatialCells(consumer);

        ShapedRecipeBuilder.shaped(AEItems.VIEW_CELL)
                .pattern("aba")
                .pattern("bcb")
                .pattern("ddd")
                .define('a', AEBlocks.QUARTZ_GLASS)
                .define('b', ConventionTags.REDSTONE)
                .define('c', ConventionTags.ALL_CERTUS_QUARTZ)
                .define('d', ConventionTags.IRON_INGOT)
                .unlockedBy("has_terminal", has(AEParts.TERMINAL))
                .save(consumer, AppEng.makeId("network/cells/view_cell"));
        ShapelessRecipeBuilder.shapeless(AEItems.VIEW_CELL)
                .requires(AEItems.ITEM_CELL_HOUSING)
                .requires(ConventionTags.ALL_CERTUS_QUARTZ)
                .unlockedBy("has_terminal", has(AEParts.TERMINAL))
                .save(consumer, AppEng.makeId("network/cells/view_cell_storage"));

        // ====================================================
        // recipes/network/crafting
        // ====================================================

        ShapedRecipeBuilder.shaped(AEBlocks.CRAFTING_UNIT)
                .pattern("aba")
                .pattern("cdc")
                .pattern("aba")
                .define('a', ConventionTags.IRON_INGOT)
                .define('b', AEItems.CALCULATION_PROCESSOR)
                .define('c', AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT))
                .define('d', AEItems.LOGIC_PROCESSOR)
                .unlockedBy("has_calculation_processor", has(AEItems.CALCULATION_PROCESSOR))
                .save(consumer, AppEng.makeId("network/crafting/cpu_crafting_unit"));

        ShapelessRecipeBuilder.shapeless(AEBlocks.CRAFTING_STORAGE_1K)
                .requires(AEBlocks.CRAFTING_UNIT)
                .requires(AEItems.CELL_COMPONENT_1K)
                .unlockedBy("has_crafting_unit", has(AEBlocks.CRAFTING_UNIT))
                .save(consumer, AppEng.makeId("network/crafting/1k_cpu_crafting_storage"));
        ShapelessRecipeBuilder.shapeless(AEBlocks.CRAFTING_STORAGE_4K)
                .requires(AEItems.CELL_COMPONENT_4K)
                .requires(AEBlocks.CRAFTING_UNIT)
                .unlockedBy("has_crafting_unit", has(AEBlocks.CRAFTING_UNIT))
                .save(consumer, AppEng.makeId("network/crafting/4k_cpu_crafting_storage"));
        ShapelessRecipeBuilder.shapeless(AEBlocks.CRAFTING_STORAGE_16K)
                .requires(AEItems.CELL_COMPONENT_16K)
                .requires(AEBlocks.CRAFTING_UNIT)
                .unlockedBy("has_crafting_unit", has(AEBlocks.CRAFTING_UNIT))
                .save(consumer, AppEng.makeId("network/crafting/16k_cpu_crafting_storage"));
        ShapelessRecipeBuilder.shapeless(AEBlocks.CRAFTING_STORAGE_64K)
                .requires(AEBlocks.CRAFTING_UNIT)
                .requires(AEItems.CELL_COMPONENT_64K)
                .unlockedBy("has_crafting_unit", has(AEBlocks.CRAFTING_UNIT))
                .save(consumer, AppEng.makeId("network/crafting/64k_cpu_crafting_storage"));
        ShapelessRecipeBuilder.shapeless(AEBlocks.CRAFTING_STORAGE_256K)
                .requires(AEItems.CELL_COMPONENT_256K)
                .requires(AEBlocks.CRAFTING_UNIT)
                .unlockedBy("has_crafting_unit", has(AEBlocks.CRAFTING_UNIT))
                .save(consumer, AppEng.makeId("network/crafting/256k_cpu_crafting_storage"));
        ShapelessRecipeBuilder.shapeless(AEBlocks.CRAFTING_ACCELERATOR)
                .requires(AEItems.ENGINEERING_PROCESSOR)
                .requires(AEBlocks.CRAFTING_UNIT)
                .unlockedBy("has_crafting_unit", has(AEBlocks.CRAFTING_UNIT))
                .save(consumer, AppEng.makeId("network/crafting/cpu_crafting_accelerator"));
        ShapelessRecipeBuilder.shapeless(AEBlocks.CRAFTING_MONITOR)
                .requires(AEParts.STORAGE_MONITOR)
                .requires(AEBlocks.CRAFTING_UNIT)
                .unlockedBy("has_crafting_unit", has(AEBlocks.CRAFTING_UNIT))
                .save(consumer, AppEng.makeId("network/crafting/cpu_crafting_monitor"));

        ShapedRecipeBuilder.shaped(AEBlocks.MOLECULAR_ASSEMBLER)
                .pattern("aba")
                .pattern("cde")
                .pattern("aba")
                .define('a', ConventionTags.IRON_INGOT)
                .define('b', AEBlocks.QUARTZ_GLASS)
                .define('c', AEItems.ANNIHILATION_CORE)
                .define('d', Items.CRAFTING_TABLE)
                .define('e', AEItems.FORMATION_CORE)
                .unlockedBy("has_formation_core", has(AEItems.FORMATION_CORE))
                .unlockedBy("has_annihilation_core", has(AEItems.ANNIHILATION_CORE))
                .save(consumer, AppEng.makeId("network/crafting/molecular_assembler"));
        ShapedRecipeBuilder.shaped(AEItems.BLANK_PATTERN, 2)
                .pattern("aba")
                .pattern("bcb")
                .pattern("ddd")
                .define('a', AEBlocks.QUARTZ_GLASS)
                .define('b', ConventionTags.GLOWSTONE)
                .define('c', ConventionTags.ALL_CERTUS_QUARTZ)
                .define('d', ConventionTags.IRON_INGOT)
                .unlockedBy("has_pattern_encoding_terminal", has(AEParts.PATTERN_ENCODING_TERMINAL))
                .save(consumer, AppEng.makeId("network/crafting/patterns_blank"));

        // ====================================================
        // recipes/network/parts
        // ====================================================

        ShapelessRecipeBuilder.shapeless(AEParts.CABLE_ANCHOR, 4)
                .requires(AETags.METAL_INGOTS)
                .requires(ConventionTags.QUARTZ_KNIFE)
                .unlockedBy("has_knife", has(ConventionTags.QUARTZ_KNIFE))
                .save(consumer, AppEng.makeId("network/parts/cable_anchor"));
        ShapelessRecipeBuilder.shapeless(AEParts.ENERGY_ACCEPTOR)
                .requires(AEBlocks.ENERGY_ACCEPTOR)
                .unlockedBy("has_energy_acceptor", has(AEBlocks.ENERGY_ACCEPTOR))
                .save(consumer, AppEng.makeId("network/parts/energy_acceptor"));
        ShapedRecipeBuilder.shaped(AEParts.ANNIHILATION_PLANE)
                .pattern("aaa")
                .pattern("bcb")
                .define('a', ConventionTags.ALL_FLUIX)
                .define('b', ConventionTags.IRON_INGOT)
                .define('c', AEItems.ANNIHILATION_CORE)
                .unlockedBy("has_annihilation_core", has(AEItems.ANNIHILATION_CORE))
                .save(consumer, AppEng.makeId("network/parts/annihilation_plane_alt"));
        ShapedRecipeBuilder.shaped(AEParts.ANNIHILATION_PLANE)
                .pattern("ab")
                .pattern("cb")
                .pattern("ab")
                .define('a', ConventionTags.IRON_INGOT)
                .define('b', ConventionTags.ALL_FLUIX)
                .define('c', AEItems.ANNIHILATION_CORE)
                .unlockedBy("has_annihilation_core", has(AEItems.ANNIHILATION_CORE))
                .save(consumer, AppEng.makeId("network/parts/annihilation_plane_alt2"));
        ShapedRecipeBuilder.shaped(AEParts.EXPORT_BUS)
                .pattern("aba")
                .pattern(" c ")
                .define('a', ConventionTags.IRON_INGOT)
                .define('b', AEItems.FORMATION_CORE)
                .define('c', Items.PISTON)
                .unlockedBy("has_formation_core", has(AEItems.FORMATION_CORE))
                .save(consumer, AppEng.makeId("network/parts/export_bus"));
        ShapedRecipeBuilder.shaped(AEParts.FORMATION_PLANE)
                .pattern("aaa")
                .pattern("bcb")
                .define('a', ConventionTags.ALL_FLUIX)
                .define('b', ConventionTags.IRON_INGOT)
                .define('c', AEItems.FORMATION_CORE)
                .unlockedBy("has_formation_core", has(AEItems.FORMATION_CORE))
                .save(consumer, AppEng.makeId("network/parts/formation_plane"));
        ShapedRecipeBuilder.shaped(AEParts.FORMATION_PLANE)
                .pattern("ab")
                .pattern("cb")
                .pattern("ab")
                .define('a', ConventionTags.IRON_INGOT)
                .define('b', ConventionTags.ALL_FLUIX)
                .define('c', AEItems.FORMATION_CORE)
                .unlockedBy("has_formation_core", has(AEItems.FORMATION_CORE))
                .save(consumer, AppEng.makeId("network/parts/formation_plane_alt"));
        ShapedRecipeBuilder.shaped(AEParts.IMPORT_BUS)
                .pattern(" a ")
                .pattern("bcb")
                .define('a', AEItems.ANNIHILATION_CORE)
                .define('b', ConventionTags.IRON_INGOT)
                .define('c', Items.STICKY_PISTON)
                .unlockedBy("has_annihilation_core", has(AEItems.ANNIHILATION_CORE))
                .save(consumer, AppEng.makeId("network/parts/import_bus"));
        ShapelessRecipeBuilder.shapeless(AEParts.LEVEL_EMITTER)
                .requires(Items.REDSTONE_TORCH)
                .requires(AEItems.CALCULATION_PROCESSOR)
                .unlockedBy("has_calculation_processor", has(AEItems.CALCULATION_PROCESSOR))
                .save(consumer, AppEng.makeId("network/parts/level_emitter"));
        ShapelessRecipeBuilder.shapeless(AEParts.ENERGY_LEVEL_EMITTER)
                .requires(Items.REDSTONE_TORCH)
                .requires(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED)
                .requires(AEItems.CALCULATION_PROCESSOR)
                .unlockedBy("has_calculation_processor", has(AEItems.CALCULATION_PROCESSOR))
                .save(consumer, AppEng.makeId("network/parts/energy_level_emitter"));
        ShapelessRecipeBuilder.shapeless(AEParts.STORAGE_BUS)
                .requires(Items.STICKY_PISTON)
                .requires(ConventionTags.INTERFACE)
                .requires(Items.PISTON)
                .unlockedBy("has_interface", has(ConventionTags.INTERFACE))
                .save(consumer, AppEng.makeId("network/parts/storage_bus"));
        ShapelessRecipeBuilder.shapeless(AEParts.CONVERSION_MONITOR)
                .requires(AEItems.FORMATION_CORE)
                .requires(AEParts.STORAGE_MONITOR)
                .requires(AEItems.ANNIHILATION_CORE)
                .unlockedBy("has_storage_monitor", has(AEParts.STORAGE_MONITOR))
                .save(consumer, AppEng.makeId("network/parts/monitors_conversion"));
        ShapelessRecipeBuilder.shapeless(AEParts.STORAGE_MONITOR)
                .requires(AEParts.LEVEL_EMITTER)
                .requires(ConventionTags.ILLUMINATED_PANEL)
                .unlockedBy("has_illuminated_panel", has(ConventionTags.ILLUMINATED_PANEL))
                .unlockedBy("has_level_emitter", has(AEParts.LEVEL_EMITTER))
                .save(consumer, AppEng.makeId("network/parts/monitors_storage"));
        ShapelessRecipeBuilder.shapeless(AEParts.DARK_MONITOR)
                .requires(AEParts.MONITOR)
                .unlockedBy("has_monitor", has(AEParts.MONITOR))
                .save(consumer, AppEng.makeId("network/parts/panels_dark_monitor"));
        ShapelessRecipeBuilder.shapeless(AEParts.MONITOR)
                .requires(AEParts.SEMI_DARK_MONITOR)
                .unlockedBy("has_semi_dark_monitor", has(AEParts.SEMI_DARK_MONITOR))
                .save(consumer, AppEng.makeId("network/parts/panels_monitor"));
        ShapedRecipeBuilder.shaped(AEParts.SEMI_DARK_MONITOR, 3)
                .pattern(" ab")
                .pattern("cdb")
                .pattern(" ab")
                .define('a', ConventionTags.GLOWSTONE)
                .define('b', AEBlocks.QUARTZ_GLASS)
                .define('c', ConventionTags.IRON_INGOT)
                .define('d', ConventionTags.REDSTONE)
                .unlockedBy("has_quartz_glass", has(AEBlocks.QUARTZ_GLASS))
                .save(consumer, AppEng.makeId("network/parts/panels_semi_dark_monitor"));
        ShapelessRecipeBuilder.shapeless(AEParts.SEMI_DARK_MONITOR)
                .requires(AEParts.DARK_MONITOR)
                .unlockedBy("has_dark_monitor", has(AEParts.DARK_MONITOR))
                .save(consumer, AppEng.makeId("network/parts/panels_semi_dark_monitor_alt"));
        ShapedRecipeBuilder.shaped(AEParts.QUARTZ_FIBER, 3)
                .pattern("aaa")
                .pattern("bbb")
                .pattern("aaa")
                .define('a', ConventionTags.GLASS)
                .define('b', ConventionTags.ALL_QUARTZ_DUST)
                .unlockedBy("has_dusts/quartz", has(ConventionTags.ALL_QUARTZ_DUST))
                .save(consumer, AppEng.makeId("network/parts/quartz_fiber_part"));
        ShapelessRecipeBuilder.shapeless(AEParts.TERMINAL)
                .requires(AEItems.FORMATION_CORE)
                .requires(ConventionTags.ILLUMINATED_PANEL)
                .requires(AEItems.LOGIC_PROCESSOR)
                .requires(AEItems.ANNIHILATION_CORE)
                .unlockedBy("has_formation_core", has(AEItems.FORMATION_CORE))
                .unlockedBy("has_illuminated_panel", has(ConventionTags.ILLUMINATED_PANEL))
                .unlockedBy("has_logic_processor", has(AEItems.LOGIC_PROCESSOR))
                .unlockedBy("has_annihilation_core", has(AEItems.ANNIHILATION_CORE))
                .save(consumer, AppEng.makeId("network/parts/terminals"));
        ShapelessRecipeBuilder.shapeless(AEParts.CRAFTING_TERMINAL)
                .requires(AEParts.TERMINAL)
                .requires(Items.CRAFTING_TABLE)
                .requires(AEItems.CALCULATION_PROCESSOR)
                .unlockedBy("has_terminal", has(AEParts.TERMINAL))
                .unlockedBy("has_calculation_processor", has(AEItems.CALCULATION_PROCESSOR))
                .save(consumer, AppEng.makeId("network/parts/terminals_crafting"));
        ShapelessRecipeBuilder.shapeless(AEParts.PATTERN_ACCESS_TERMINAL)
                .requires(ConventionTags.ILLUMINATED_PANEL)
                .requires(AEItems.ENGINEERING_PROCESSOR)
                .requires(ConventionTags.PATTERN_PROVIDER)
                .unlockedBy("has_pattern_provider", has(ConventionTags.PATTERN_PROVIDER))
                .save(consumer, AppEng.makeId("network/parts/terminals_pattern_access"));
        ShapelessRecipeBuilder.shapeless(AEParts.PATTERN_ENCODING_TERMINAL)
                .requires(AEItems.ENGINEERING_PROCESSOR)
                .requires(AEParts.CRAFTING_TERMINAL)
                .unlockedBy("has_crafting_terminal", has(AEParts.CRAFTING_TERMINAL))
                .save(consumer, AppEng.makeId("network/parts/terminals_pattern_encoding"));
        ShapedRecipeBuilder.shaped(AEParts.TOGGLE_BUS)
                .pattern(" a ")
                .pattern("bcb")
                .pattern(" a ")
                .define('a', ConventionTags.REDSTONE)
                .define('b', AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT))
                .define('c', Items.LEVER)
                .unlockedBy("has_glass_cable", has(AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT)))
                .save(consumer, AppEng.makeId("network/parts/toggle_bus"));
        ShapelessRecipeBuilder.shapeless(AEParts.TOGGLE_BUS)
                .requires(AEParts.INVERTED_TOGGLE_BUS)
                .unlockedBy("has_inverted_toggle_bus", has(AEParts.INVERTED_TOGGLE_BUS))
                .save(consumer, AppEng.makeId("network/parts/toggle_bus_alt"));
        ShapelessRecipeBuilder.shapeless(AEParts.INVERTED_TOGGLE_BUS)
                .requires(AEParts.TOGGLE_BUS)
                .unlockedBy("has_toggle_bus", has(AEParts.TOGGLE_BUS))
                .save(consumer, AppEng.makeId("network/parts/toggle_bus_inverted_alt"));
        ShapedRecipeBuilder.shaped(AEParts.ME_P2P_TUNNEL)
                .pattern(" a ")
                .pattern("aba")
                .pattern("ccc")
                .define('a', ConventionTags.IRON_INGOT)
                .define('b', AEItems.ENGINEERING_PROCESSOR)
                .define('c', ConventionTags.ALL_FLUIX)
                .unlockedBy("has_engineering_processor", has(AEItems.ENGINEERING_PROCESSOR))
                .save(consumer, AppEng.makeId("network/parts/tunnels_me"));

        // ====================================================
        // recipes/tools
        // ====================================================

        addQuartzTools(consumer);

        ShapedRecipeBuilder.shaped(AEItems.MATTER_CANNON)
                .pattern("aab")
                .pattern("cd ")
                .pattern("a  ")
                .define('a', ConventionTags.IRON_INGOT)
                .define('b', AEItems.FORMATION_CORE)
                .define('c', AEItems.CELL_COMPONENT_4K)
                .define('d', AEBlocks.ENERGY_CELL)
                .unlockedBy("has_formation_core", has(AEItems.FORMATION_CORE))
                .save(consumer, AppEng.makeId("tools/matter_cannon"));
        ShapedRecipeBuilder.shaped(AEItems.CHARGED_STAFF)
                .pattern("a  ")
                .pattern(" b ")
                .pattern("  b")
                .define('a', AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED)
                .define('b', ConventionTags.IRON_INGOT)
                .unlockedBy("has_charged_certus_quartz_crystal", has(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED))
                .save(consumer, AppEng.makeId("tools/misctools_charged_staff"));
        ShapedRecipeBuilder.shaped(AEItems.ENTROPY_MANIPULATOR)
                .pattern("ab ")
                .pattern("cd ")
                .pattern("  d")
                .define('a', ConventionTags.ALL_FLUIX)
                .define('b', AEBlocks.ENERGY_CELL)
                .define('c', AEItems.ENGINEERING_PROCESSOR)
                .define('d', ConventionTags.IRON_INGOT)
                .unlockedBy("has_engineering_processor", has(AEItems.ENGINEERING_PROCESSOR))
                .save(consumer, AppEng.makeId("tools/misctools_entropy_manipulator"));

        portableCell(consumer, AEItems.PORTABLE_ITEM_CELL1K);
        portableCell(consumer, AEItems.PORTABLE_ITEM_CELL4K);
        portableCell(consumer, AEItems.PORTABLE_ITEM_CELL16K);
        portableCell(consumer, AEItems.PORTABLE_ITEM_CELL64K);
        portableCell(consumer, AEItems.PORTABLE_ITEM_CELL256K);
        portableCell(consumer, AEItems.PORTABLE_FLUID_CELL1K);
        portableCell(consumer, AEItems.PORTABLE_FLUID_CELL4K);
        portableCell(consumer, AEItems.PORTABLE_FLUID_CELL16K);
        portableCell(consumer, AEItems.PORTABLE_FLUID_CELL64K);
        portableCell(consumer, AEItems.PORTABLE_FLUID_CELL256K);

        ShapedRecipeBuilder.shaped(AEItems.BIOMETRIC_CARD)
                .pattern("abb")
                .pattern("cdc")
                .define('a', AEItems.ENGINEERING_PROCESSOR)
                .define('b', ConventionTags.IRON_INGOT)
                .define('c', ConventionTags.GOLD_INGOT)
                .define('d', ConventionTags.REDSTONE)
                .unlockedBy("has_security_station", has(AEBlocks.SECURITY_STATION))
                .save(consumer, AppEng.makeId("tools/network_biometric_card"));
        ShapedRecipeBuilder.shaped(AEItems.COLOR_APPLICATOR)
                .pattern("ab ")
                .pattern("bc ")
                .pattern("  d")
                .define('a', AEItems.FORMATION_CORE)
                .define('b', ConventionTags.IRON_INGOT)
                .define('c', AEItems.CELL_COMPONENT_4K)
                .define('d', AEBlocks.ENERGY_CELL)
                .unlockedBy("has_formation_core", has(AEItems.FORMATION_CORE))
                .unlockedBy("has_energy_cell", has(AEBlocks.ENERGY_CELL))
                .save(consumer, AppEng.makeId("tools/network_color_applicator"));

        ShapedRecipeBuilder.shaped(AEItems.MEMORY_CARDS.item(AEColor.TRANSPARENT))
                .pattern("abb")
                .pattern("cdc")
                .define('a', AEItems.CALCULATION_PROCESSOR)
                .define('b', ConventionTags.IRON_INGOT)
                .define('c', ConventionTags.GOLD_INGOT)
                .define('d', ConventionTags.REDSTONE)
                .unlockedBy("has_calculation_processor", has(AEItems.CALCULATION_PROCESSOR))
                .save(consumer, AppEng.makeId("tools/network_memory_card"));

        for (var entry : AEItemIds.MEMORY_CARDS.entrySet()) {
            if (entry.getKey().dye != null) {
                ShapelessRecipeBuilder.shapeless(AEItems.MEMORY_CARDS.item(entry.getKey()))
                        .requires(ConventionTags.MEMORY_CARDS)
                        .requires(ConventionTags.dye(entry.getKey().dye))
                        .unlockedBy("has_memory_card", has(ConventionTags.MEMORY_CARDS))
                        .save(consumer, AppEng.makeId("tools/network_memory_card_" + entry.getKey().registryPrefix));
            }
        }

        ShapelessRecipeBuilder.shapeless(AEItems.MEMORY_CARDS.item(AEColor.TRANSPARENT))
                .requires(ConventionTags.MEMORY_CARDS)
                .requires(ConventionTags.CAN_REMOVE_COLOR)
                .unlockedBy("has_memory_card", has(ConventionTags.MEMORY_CARDS))
                .save(consumer, AppEng.makeId("network/cables/network_memory_card_clean"));

        ShapelessRecipeBuilder.shapeless(AEItems.NETWORK_TOOL)
                .requires(ConventionTags.ILLUMINATED_PANEL)
                .requires(ConventionTags.CHEST)
                .requires(ConventionTags.QUARTZ_WRENCH)
                .requires(AEItems.CALCULATION_PROCESSOR)
                .unlockedBy("has_quartz_wrench", has(ConventionTags.QUARTZ_WRENCH))
                .unlockedBy("has_calculation_processor", has(AEItems.CALCULATION_PROCESSOR))
                .save(consumer, AppEng.makeId("tools/network_tool"));

        addPaintBalls(consumer);

    }

    private void portableCell(Consumer<FinishedRecipe> consumer, ItemDefinition<PortableCellItem> cell) {
        ItemDefinition<?> housing;
        if (cell.asItem().getKeyType() == AEKeyType.items()) {
            housing = AEItems.ITEM_CELL_HOUSING;
        } else if (cell.asItem().getKeyType() == AEKeyType.fluids()) {
            housing = AEItems.FLUID_CELL_HOUSING;
        } else {
            throw new RuntimeException("No housing known for " + cell.asItem().getKeyType());
        }

        var component = cell.asItem().getTier().componentSupplier().get();
        ShapelessRecipeBuilder.shapeless(cell)
                .requires(AEBlocks.CHEST)
                .requires(component)
                .requires(AEBlocks.ENERGY_CELL)
                .requires(housing)
                .unlockedBy("has_" + housing.id().getPath(), has(housing))
                .unlockedBy("has_energy_cell", has(AEBlocks.ENERGY_CELL))
                .save(consumer, cell.asItem().getRecipeId());
    }

    private void addSpatialCells(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(AEItems.SPATIAL_2_CELL_COMPONENT)
                .pattern("aba")
                .pattern("bcb")
                .pattern("aba")
                .define('a', ConventionTags.GLOWSTONE)
                .define('b', AEItems.FLUIX_PEARL)
                .define('c', AEItems.ENGINEERING_PROCESSOR)
                .unlockedBy("has_engineering_processor", has(AEItems.ENGINEERING_PROCESSOR))
                .unlockedBy("has_fluix_pearl", has(AEItems.FLUIX_PEARL))
                .save(consumer, AppEng.makeId("network/cells/spatial_components"));
        ShapedRecipeBuilder.shaped(AEItems.SPATIAL_16_CELL_COMPONENT)
                .pattern("aba")
                .pattern("bcb")
                .pattern("aba")
                .define('a', ConventionTags.GLOWSTONE)
                .define('b', AEItems.SPATIAL_2_CELL_COMPONENT)
                .define('c', AEItems.ENGINEERING_PROCESSOR)
                .unlockedBy("has_2_cubed_spatial_cell_component", has(AEItems.SPATIAL_2_CELL_COMPONENT))
                .save(consumer, AppEng.makeId("network/cells/spatial_components_0"));
        ShapedRecipeBuilder.shaped(AEItems.SPATIAL_128_CELL_COMPONENT)
                .pattern("aba")
                .pattern("bcb")
                .pattern("aba")
                .define('a', ConventionTags.GLOWSTONE)
                .define('b', AEItems.SPATIAL_16_CELL_COMPONENT)
                .define('c', AEItems.ENGINEERING_PROCESSOR)
                .unlockedBy("has_16_cubed_spatial_cell_component", has(AEItems.SPATIAL_16_CELL_COMPONENT))
                .save(consumer, AppEng.makeId("network/cells/spatial_components_1"));

        ShapedRecipeBuilder.shaped(AEItems.SPATIAL_CELL2)
                .pattern("aba")
                .pattern("bcb")
                .pattern("ddd")
                .define('a', AEBlocks.QUARTZ_GLASS)
                .define('b', ConventionTags.REDSTONE)
                .define('c', AEItems.SPATIAL_2_CELL_COMPONENT)
                .define('d', ConventionTags.IRON_INGOT)
                .unlockedBy("has_2_cubed_spatial_cell_component", has(AEItems.SPATIAL_2_CELL_COMPONENT))
                .save(consumer, AppEng.makeId("network/cells/spatial_storage_cell_2_cubed"));
        ShapelessRecipeBuilder.shapeless(AEItems.SPATIAL_CELL2)
                .requires(AEItems.ITEM_CELL_HOUSING)
                .requires(AEItems.SPATIAL_2_CELL_COMPONENT)
                .unlockedBy("has_2_cubed_spatial_cell_component", has(AEItems.SPATIAL_2_CELL_COMPONENT))
                .save(consumer, AppEng.makeId("network/cells/spatial_storage_cell_2_cubed_storage"));
        ShapedRecipeBuilder.shaped(AEItems.SPATIAL_CELL16)
                .pattern("aba")
                .pattern("bcb")
                .pattern("ddd")
                .define('a', AEBlocks.QUARTZ_GLASS)
                .define('b', ConventionTags.REDSTONE)
                .define('c', AEItems.SPATIAL_16_CELL_COMPONENT)
                .define('d', ConventionTags.IRON_INGOT)
                .unlockedBy("has_16_cubed_spatial_cell_component", has(AEItems.SPATIAL_16_CELL_COMPONENT))
                .save(consumer, AppEng.makeId("network/cells/spatial_storage_cell_16_cubed"));
        ShapelessRecipeBuilder.shapeless(AEItems.SPATIAL_CELL16)
                .requires(AEItems.ITEM_CELL_HOUSING)
                .requires(AEItems.SPATIAL_16_CELL_COMPONENT)
                .unlockedBy("has_16_cubed_spatial_cell_component", has(AEItems.SPATIAL_16_CELL_COMPONENT))
                .save(consumer, AppEng.makeId("network/cells/spatial_storage_cell_16_cubed_storage"));

        ShapedRecipeBuilder.shaped(AEItems.SPATIAL_CELL128)
                .pattern("aba")
                .pattern("bcb")
                .pattern("ddd")
                .define('a', AEBlocks.QUARTZ_GLASS)
                .define('b', ConventionTags.REDSTONE)
                .define('c', AEItems.SPATIAL_128_CELL_COMPONENT)
                .define('d', ConventionTags.IRON_INGOT)
                .unlockedBy("has_128_cubed_spatial_cell_component", has(AEItems.SPATIAL_128_CELL_COMPONENT))
                .save(consumer, AppEng.makeId("network/cells/spatial_storage_cell_128_cubed"));
        ShapelessRecipeBuilder.shapeless(AEItems.SPATIAL_CELL128)
                .requires(AEItems.ITEM_CELL_HOUSING)
                .requires(AEItems.SPATIAL_128_CELL_COMPONENT)
                .unlockedBy("has_128_cubed_spatial_cell_component", has(AEItems.SPATIAL_128_CELL_COMPONENT))
                .save(consumer, AppEng.makeId("network/cells/spatial_storage_cell_128_cubed_storage"));
    }

    private void addItemCells(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(AEItems.CELL_COMPONENT_1K)
                .pattern("aba")
                .pattern("bcb")
                .pattern("aba")
                .define('a', ConventionTags.REDSTONE)
                .define('b', ConventionTags.ALL_CERTUS_QUARTZ)
                .define('c', AEItems.LOGIC_PROCESSOR)
                .unlockedBy("has_logic_processor", has(AEItems.LOGIC_PROCESSOR))
                .save(consumer, AppEng.makeId("network/cells/item_storage_components_cell_1k_part"));
        ShapedRecipeBuilder.shaped(AEItems.CELL_COMPONENT_4K)
                .pattern("aba")
                .pattern("cdc")
                .pattern("aca")
                .define('a', ConventionTags.REDSTONE)
                .define('b', AEItems.CALCULATION_PROCESSOR)
                .define('c', AEItems.CELL_COMPONENT_1K)
                .define('d', AEBlocks.QUARTZ_GLASS)
                .unlockedBy("has_cell_component_1k", has(AEItems.CELL_COMPONENT_1K))
                .save(consumer, AppEng.makeId("network/cells/item_storage_components_cell_4k_part"));
        ShapedRecipeBuilder.shaped(AEItems.CELL_COMPONENT_16K)
                .pattern("aba")
                .pattern("cdc")
                .pattern("aca")
                .define('a', ConventionTags.GLOWSTONE)
                .define('b', AEItems.CALCULATION_PROCESSOR)
                .define('c', AEItems.CELL_COMPONENT_4K)
                .define('d', AEBlocks.QUARTZ_GLASS)
                .unlockedBy("has_cell_component_4k", has(AEItems.CELL_COMPONENT_4K))
                .save(consumer, AppEng.makeId("network/cells/item_storage_components_cell_16k_part"));
        ShapedRecipeBuilder.shaped(AEItems.CELL_COMPONENT_64K)
                .pattern("aba")
                .pattern("cdc")
                .pattern("aca")
                .define('a', ConventionTags.GLOWSTONE)
                .define('b', AEItems.CALCULATION_PROCESSOR)
                .define('c', AEItems.CELL_COMPONENT_16K)
                .define('d', AEBlocks.QUARTZ_GLASS)
                .unlockedBy("has_cell_component_16k", has(AEItems.CELL_COMPONENT_16K))
                .save(consumer, AppEng.makeId("network/cells/item_storage_components_cell_64k_part"));
        ShapedRecipeBuilder.shaped(AEItems.CELL_COMPONENT_256K)
                .pattern("aba")
                .pattern("cdc")
                .pattern("aca")
                .define('a', AEItems.SKY_DUST)
                .define('b', AEItems.CALCULATION_PROCESSOR)
                .define('c', AEItems.CELL_COMPONENT_64K)
                .define('d', AEBlocks.QUARTZ_GLASS)
                .unlockedBy("has_cell_component_64k", has(AEItems.CELL_COMPONENT_64K))
                .save(consumer, AppEng.makeId("network/cells/item_storage_components_cell_256k_part"));

        ShapedRecipeBuilder.shaped(AEItems.ITEM_CELL_1K)
                .pattern("aba")
                .pattern("bcb")
                .pattern("ddd")
                .define('a', AEBlocks.QUARTZ_GLASS)
                .define('b', ConventionTags.REDSTONE)
                .define('c', AEItems.CELL_COMPONENT_1K)
                .define('d', ConventionTags.IRON_INGOT)
                .unlockedBy("has_cell_component_1k", has(AEItems.CELL_COMPONENT_1K))
                .save(consumer, AppEng.makeId("network/cells/item_storage_cell_1k"));
        ShapelessRecipeBuilder.shapeless(AEItems.ITEM_CELL_1K)
                .requires(AEItems.ITEM_CELL_HOUSING)
                .requires(AEItems.CELL_COMPONENT_1K)
                .unlockedBy("has_cell_component_1k", has(AEItems.CELL_COMPONENT_1K))
                .unlockedBy("has_item_cell_housing", has(AEItems.ITEM_CELL_HOUSING))
                .save(consumer, AppEng.makeId("network/cells/item_storage_cell_1k_storage"));

        ShapedRecipeBuilder.shaped(AEItems.ITEM_CELL_4K)
                .pattern("aba")
                .pattern("bcb")
                .pattern("ddd")
                .define('a', AEBlocks.QUARTZ_GLASS)
                .define('b', ConventionTags.REDSTONE)
                .define('c', AEItems.CELL_COMPONENT_4K)
                .define('d', ConventionTags.IRON_INGOT)
                .unlockedBy("has_cell_component_4k", has(AEItems.CELL_COMPONENT_4K))
                .save(consumer, AppEng.makeId("network/cells/item_storage_cell_4k"));
        ShapelessRecipeBuilder.shapeless(AEItems.ITEM_CELL_4K)
                .requires(AEItems.ITEM_CELL_HOUSING)
                .requires(AEItems.CELL_COMPONENT_4K)
                .unlockedBy("has_cell_component_4k", has(AEItems.CELL_COMPONENT_4K))
                .save(consumer, AppEng.makeId("network/cells/item_storage_cell_4k_storage"));

        ShapedRecipeBuilder.shaped(AEItems.ITEM_CELL_16K)
                .pattern("aba")
                .pattern("bcb")
                .pattern("ddd")
                .define('a', AEBlocks.QUARTZ_GLASS)
                .define('b', ConventionTags.REDSTONE)
                .define('c', AEItems.CELL_COMPONENT_16K)
                .define('d', ConventionTags.IRON_INGOT)
                .unlockedBy("has_cell_component_16k", has(AEItems.CELL_COMPONENT_16K))
                .save(consumer, AppEng.makeId("network/cells/item_storage_cell_16k"));
        ShapelessRecipeBuilder.shapeless(AEItems.ITEM_CELL_16K)
                .requires(AEItems.CELL_COMPONENT_16K)
                .requires(AEItems.ITEM_CELL_HOUSING)
                .unlockedBy("has_cell_component_16k", has(AEItems.CELL_COMPONENT_16K))
                .save(consumer, AppEng.makeId("network/cells/item_storage_cell_16k_storage"));

        ShapedRecipeBuilder.shaped(AEItems.ITEM_CELL_64K)
                .pattern("aba")
                .pattern("bcb")
                .pattern("ddd")
                .define('a', AEBlocks.QUARTZ_GLASS)
                .define('b', ConventionTags.REDSTONE)
                .define('c', AEItems.CELL_COMPONENT_64K)
                .define('d', ConventionTags.IRON_INGOT)
                .unlockedBy("has_cell_component_64k", has(AEItems.CELL_COMPONENT_64K))
                .save(consumer, AppEng.makeId("network/cells/item_storage_cell_64k"));
        ShapelessRecipeBuilder.shapeless(AEItems.ITEM_CELL_64K)
                .requires(AEItems.ITEM_CELL_HOUSING)
                .requires(AEItems.CELL_COMPONENT_64K)
                .unlockedBy("has_cell_component_64k", has(AEItems.CELL_COMPONENT_64K))
                .save(consumer, AppEng.makeId("network/cells/item_storage_cell_64k_storage"));

        ShapedRecipeBuilder.shaped(AEItems.ITEM_CELL_256K)
                .pattern("aba")
                .pattern("bcb")
                .pattern("ddd")
                .define('a', AEBlocks.QUARTZ_GLASS)
                .define('b', ConventionTags.REDSTONE)
                .define('c', AEItems.CELL_COMPONENT_256K)
                .define('d', ConventionTags.IRON_INGOT)
                .unlockedBy("has_cell_component_256k", has(AEItems.CELL_COMPONENT_256K))
                .save(consumer, AppEng.makeId("network/cells/item_storage_cell_256k"));
        ShapelessRecipeBuilder.shapeless(AEItems.ITEM_CELL_256K)
                .requires(AEItems.ITEM_CELL_HOUSING)
                .requires(AEItems.CELL_COMPONENT_256K)
                .unlockedBy("has_cell_component_256k", has(AEItems.CELL_COMPONENT_256K))
                .save(consumer, AppEng.makeId("network/cells/item_storage_cell_256k_storage"));
    }

    private void addFluidCells(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(AEItems.FLUID_CELL_1K)
                .pattern("aba")
                .pattern("bcb")
                .pattern("ddd")
                .define('a', AEBlocks.QUARTZ_GLASS)
                .define('b', ConventionTags.REDSTONE)
                .define('c', AEItems.CELL_COMPONENT_1K)
                .define('d', ConventionTags.COPPER_INGOT)
                .unlockedBy("has_cell_component_1k", has(AEItems.CELL_COMPONENT_1K))
                .save(consumer, AppEng.makeId("network/cells/fluid_storage_cell_1k"));
        ShapelessRecipeBuilder.shapeless(AEItems.FLUID_CELL_1K)
                .requires(AEItems.FLUID_CELL_HOUSING)
                .requires(AEItems.CELL_COMPONENT_1K)
                .unlockedBy("has_item_cell_housing", has(AEItems.FLUID_CELL_HOUSING))
                .unlockedBy("has_cell_component_1k", has(AEItems.CELL_COMPONENT_1K))
                .save(consumer, AppEng.makeId("network/cells/fluid_storage_cell_1k_storage"));

        ShapedRecipeBuilder.shaped(AEItems.FLUID_CELL_4K)
                .pattern("aba")
                .pattern("bcb")
                .pattern("ddd")
                .define('a', AEBlocks.QUARTZ_GLASS)
                .define('b', ConventionTags.REDSTONE)
                .define('c', AEItems.CELL_COMPONENT_4K)
                .define('d', ConventionTags.COPPER_INGOT)
                .unlockedBy("has_cell_component_4k", has(AEItems.CELL_COMPONENT_4K))
                .save(consumer, AppEng.makeId("network/cells/fluid_storage_cell_4k"));
        ShapelessRecipeBuilder.shapeless(AEItems.FLUID_CELL_4K)
                .requires(AEItems.FLUID_CELL_HOUSING)
                .requires(AEItems.CELL_COMPONENT_4K)
                .unlockedBy("has_cell_component_4k", has(AEItems.CELL_COMPONENT_4K))
                .save(consumer, AppEng.makeId("network/cells/fluid_storage_cell_4k_storage"));

        ShapedRecipeBuilder.shaped(AEItems.FLUID_CELL_16K)
                .pattern("aba")
                .pattern("bcb")
                .pattern("ddd")
                .define('a', AEBlocks.QUARTZ_GLASS)
                .define('b', ConventionTags.REDSTONE)
                .define('c', AEItems.CELL_COMPONENT_16K)
                .define('d', ConventionTags.COPPER_INGOT)
                .unlockedBy("has_cell_component_16k", has(AEItems.CELL_COMPONENT_16K))
                .save(consumer, AppEng.makeId("network/cells/fluid_storage_cell_16k"));
        ShapelessRecipeBuilder.shapeless(AEItems.FLUID_CELL_16K)
                .requires(AEItems.FLUID_CELL_HOUSING)
                .requires(AEItems.CELL_COMPONENT_16K)
                .unlockedBy("has_cell_component_16k", has(AEItems.CELL_COMPONENT_16K))
                .save(consumer, AppEng.makeId("network/cells/fluid_storage_cell_16k_storage"));

        ShapedRecipeBuilder.shaped(AEItems.FLUID_CELL_64K)
                .pattern("aba")
                .pattern("bcb")
                .pattern("ddd")
                .define('a', AEBlocks.QUARTZ_GLASS)
                .define('b', ConventionTags.REDSTONE)
                .define('c', AEItems.CELL_COMPONENT_64K)
                .define('d', ConventionTags.COPPER_INGOT)
                .unlockedBy("has_cell_component_64k", has(AEItems.CELL_COMPONENT_64K))
                .save(consumer, AppEng.makeId("network/cells/fluid_storage_cell_64k"));
        ShapelessRecipeBuilder.shapeless(AEItems.FLUID_CELL_64K)
                .requires(AEItems.FLUID_CELL_HOUSING)
                .requires(AEItems.CELL_COMPONENT_64K)
                .unlockedBy("has_cell_component_64k", has(AEItems.CELL_COMPONENT_64K))
                .save(consumer, AppEng.makeId("network/cells/fluid_storage_cell_64k_storage"));

        ShapedRecipeBuilder.shaped(AEItems.FLUID_CELL_256K)
                .pattern("aba")
                .pattern("bcb")
                .pattern("ddd")
                .define('a', AEBlocks.QUARTZ_GLASS)
                .define('b', ConventionTags.REDSTONE)
                .define('c', AEItems.CELL_COMPONENT_256K)
                .define('d', ConventionTags.COPPER_INGOT)
                .unlockedBy("has_cell_component_256k", has(AEItems.CELL_COMPONENT_256K))
                .save(consumer, AppEng.makeId("network/cells/fluid_storage_cell_256k"));
        ShapelessRecipeBuilder.shapeless(AEItems.FLUID_CELL_256K)
                .requires(AEItems.FLUID_CELL_HOUSING)
                .requires(AEItems.CELL_COMPONENT_256K)
                .unlockedBy("has_cell_component_256k", has(AEItems.CELL_COMPONENT_256K))
                .save(consumer, AppEng.makeId("network/cells/fluid_storage_cell_256k_storage"));
    }

    private void addQuartzTools(Consumer<FinishedRecipe> consumer) {
        // Certus Quartz Vanilla-Like Tools
        ShapedRecipeBuilder.shaped(AEItems.CERTUS_QUARTZ_AXE)
                .pattern("aa")
                .pattern("ab")
                .pattern(" b")
                .define('a', ConventionTags.CERTUS_QUARTZ)
                .define('b', ConventionTags.WOOD_STICK)
                .unlockedBy("has_certus_quartz", has(ConventionTags.CERTUS_QUARTZ))
                .save(consumer, AppEng.makeId("tools/certus_quartz_axe"));
        ShapedRecipeBuilder.shaped(AEItems.CERTUS_QUARTZ_HOE)
                .pattern("aa")
                .pattern(" b")
                .pattern(" b")
                .define('a', ConventionTags.CERTUS_QUARTZ)
                .define('b', ConventionTags.WOOD_STICK)
                .unlockedBy("has_certus_quartz", has(ConventionTags.CERTUS_QUARTZ))
                .save(consumer, AppEng.makeId("tools/certus_quartz_hoe"));
        ShapedRecipeBuilder.shaped(AEItems.CERTUS_QUARTZ_PICK)
                .pattern("aaa")
                .pattern(" b ")
                .pattern(" b ")
                .define('a', ConventionTags.CERTUS_QUARTZ)
                .define('b', ConventionTags.WOOD_STICK)
                .unlockedBy("has_certus_quartz", has(ConventionTags.CERTUS_QUARTZ))
                .save(consumer, AppEng.makeId("tools/certus_quartz_pickaxe"));
        ShapedRecipeBuilder.shaped(AEItems.CERTUS_QUARTZ_SHOVEL)
                .pattern("a")
                .pattern("b")
                .pattern("b")
                .define('a', ConventionTags.CERTUS_QUARTZ)
                .define('b', ConventionTags.WOOD_STICK)
                .unlockedBy("has_certus_quartz", has(ConventionTags.CERTUS_QUARTZ))
                .save(consumer, AppEng.makeId("tools/certus_quartz_spade"));
        ShapedRecipeBuilder.shaped(AEItems.CERTUS_QUARTZ_SWORD)
                .pattern("a")
                .pattern("a")
                .pattern("b")
                .define('a', ConventionTags.CERTUS_QUARTZ)
                .define('b', ConventionTags.WOOD_STICK)
                .unlockedBy("has_certus_quartz", has(ConventionTags.CERTUS_QUARTZ))
                .save(consumer, AppEng.makeId("tools/certus_quartz_sword"));

        // Nether Quartz Vanilla-Like Tools
        ShapedRecipeBuilder.shaped(AEItems.NETHER_QUARTZ_AXE)
                .pattern("aa")
                .pattern("ab")
                .pattern(" b")
                .define('a', ConventionTags.NETHER_QUARTZ)
                .define('b', ConventionTags.WOOD_STICK)
                .unlockedBy("has_nether_quartz", has(ConventionTags.NETHER_QUARTZ))
                .save(consumer, AppEng.makeId("tools/nether_quartz_axe"));
        ShapedRecipeBuilder.shaped(AEItems.NETHER_QUARTZ_HOE)
                .pattern("aa")
                .pattern(" b")
                .pattern(" b")
                .define('a', ConventionTags.NETHER_QUARTZ)
                .define('b', ConventionTags.WOOD_STICK)
                .unlockedBy("has_nether_quartz", has(ConventionTags.NETHER_QUARTZ))
                .save(consumer, AppEng.makeId("tools/nether_quartz_hoe"));
        ShapedRecipeBuilder.shaped(AEItems.NETHER_QUARTZ_PICK)
                .pattern("aaa")
                .pattern(" b ")
                .pattern(" b ")
                .define('a', ConventionTags.NETHER_QUARTZ)
                .define('b', ConventionTags.WOOD_STICK)
                .unlockedBy("has_nether_quartz", has(ConventionTags.NETHER_QUARTZ))
                .save(consumer, AppEng.makeId("tools/nether_quartz_pickaxe"));
        ShapedRecipeBuilder.shaped(AEItems.NETHER_QUARTZ_SHOVEL)
                .pattern("a")
                .pattern("b")
                .pattern("b")
                .define('a', ConventionTags.NETHER_QUARTZ)
                .define('b', ConventionTags.WOOD_STICK)
                .unlockedBy("has_nether_quartz", has(ConventionTags.NETHER_QUARTZ))
                .save(consumer, AppEng.makeId("tools/nether_quartz_spade"));
        ShapedRecipeBuilder.shaped(AEItems.NETHER_QUARTZ_SWORD)
                .pattern("a")
                .pattern("a")
                .pattern("b")
                .define('a', ConventionTags.NETHER_QUARTZ)
                .define('b', ConventionTags.WOOD_STICK)
                .unlockedBy("has_nether_quartz", has(ConventionTags.NETHER_QUARTZ))
                .save(consumer, AppEng.makeId("tools/nether_quartz_sword"));

        // Knives
        ShapedRecipeBuilder.shaped(AEItems.CERTUS_QUARTZ_KNIFE)
                .pattern("  a")
                .pattern("ba ")
                .pattern("cc ")
                .define('a', ConventionTags.WOOD_STICK)
                .define('b', ConventionTags.IRON_INGOT)
                .define('c', ConventionTags.CERTUS_QUARTZ)
                .unlockedBy("has_certus_quartz", has(ConventionTags.CERTUS_QUARTZ))
                .save(consumer, AppEng.makeId("tools/certus_quartz_cutting_knife"));
        ShapedRecipeBuilder.shaped(AEItems.NETHER_QUARTZ_KNIFE)
                .pattern("  a")
                .pattern("ba ")
                .pattern("cc ")
                .define('a', ConventionTags.WOOD_STICK)
                .define('b', ConventionTags.IRON_INGOT)
                .define('c', ConventionTags.NETHER_QUARTZ)
                .unlockedBy("has_nether_quartz", has(ConventionTags.NETHER_QUARTZ))
                .save(consumer, AppEng.makeId("tools/nether_quartz_cutting_knife"));

        // Wrenches
        ShapedRecipeBuilder.shaped(AEItems.CERTUS_QUARTZ_WRENCH)
                .pattern("a a")
                .pattern(" a ")
                .pattern("a a")
                .define('a', ConventionTags.CERTUS_QUARTZ)
                .unlockedBy("has_certus_quartz", has(ConventionTags.CERTUS_QUARTZ))
                .save(consumer, AppEng.makeId("tools/certus_quartz_wrench"));
        ShapedRecipeBuilder.shaped(AEItems.NETHER_QUARTZ_WRENCH)
                .pattern("a a")
                .pattern(" a ")
                .pattern("a a")
                .define('a', ConventionTags.NETHER_QUARTZ)
                .unlockedBy("has_nether_quartz", has(ConventionTags.NETHER_QUARTZ))
                .save(consumer, AppEng.makeId("tools/nether_quartz_wrench"));
    }

    // ====================================================
    // recipes/network/cables
    // ====================================================
    private static void addCables(Consumer<FinishedRecipe> consumer) {
        for (var color : AEColor.VALID_COLORS) {
            ShapedRecipeBuilder.shaped(AEParts.COVERED_CABLE.item(color), 8)
                    .pattern("aaa")
                    .pattern("aba")
                    .pattern("aaa")
                    .define('a', AEParts.COVERED_CABLE.item(AEColor.TRANSPARENT))
                    .define('b', ConventionTags.dye(color.dye))
                    .unlockedBy("has_fluix_covered_cable", has(AEParts.COVERED_CABLE.item(AEColor.TRANSPARENT)))
                    .save(consumer, AppEng.makeId("network/cables/covered_" + color.registryPrefix));
        }
        // Remove color from any colored cable
        ShapelessRecipeBuilder.shapeless(AEParts.COVERED_CABLE.item(AEColor.TRANSPARENT))
                .requires(ConventionTags.COVERED_CABLE)
                .requires(ConventionTags.CAN_REMOVE_COLOR)
                .unlockedBy("has_covered_cable", has(ConventionTags.COVERED_CABLE))
                .save(consumer, AppEng.makeId("network/cables/covered_fluix_clean"));
        // Craft the actual colored cable initially
        ShapelessRecipeBuilder.shapeless(AEParts.COVERED_CABLE.item(AEColor.TRANSPARENT))
                .requires(ItemTags.WOOL)
                .requires(AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT))
                .unlockedBy("has_glass_cable", has(AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT)))
                .save(consumer, AppEng.makeId("network/cables/covered_fluix"));

        for (var color : AEColor.VALID_COLORS) {
            ShapedRecipeBuilder.shaped(AEParts.COVERED_DENSE_CABLE.item(color), 8)
                    .pattern("aaa")
                    .pattern("aba")
                    .pattern("aaa")
                    .define('a', AEParts.COVERED_DENSE_CABLE.item(AEColor.TRANSPARENT))
                    .define('b', ConventionTags.dye(color.dye))
                    .unlockedBy("has_fluix_covered_dense_cable",
                            has(AEParts.COVERED_DENSE_CABLE.item(AEColor.TRANSPARENT)))
                    .unlockedBy("has_dyes/black", has(ConventionTags.dye(color.dye)))
                    .save(consumer, AppEng.makeId("network/cables/dense_covered_" + color.registryPrefix));
        }
        ShapelessRecipeBuilder.shapeless(AEParts.COVERED_DENSE_CABLE.item(AEColor.TRANSPARENT))
                .requires(AEParts.COVERED_CABLE.item(AEColor.TRANSPARENT))
                .requires(AEParts.COVERED_CABLE.item(AEColor.TRANSPARENT))
                .requires(AEParts.COVERED_CABLE.item(AEColor.TRANSPARENT))
                .requires(AEParts.COVERED_CABLE.item(AEColor.TRANSPARENT))
                .unlockedBy("has_fluix_covered_cable", has(AEParts.COVERED_CABLE.item(AEColor.TRANSPARENT)))
                .save(consumer, AppEng.makeId("network/cables/dense_covered_fluix"));
        ShapelessRecipeBuilder.shapeless(AEParts.COVERED_DENSE_CABLE.item(AEColor.TRANSPARENT))
                .requires(ConventionTags.COVERED_DENSE_CABLE)
                .requires(ConventionTags.CAN_REMOVE_COLOR)
                .unlockedBy("has_covered_dense_cable", has(ConventionTags.COVERED_DENSE_CABLE))
                .save(consumer, AppEng.makeId("network/cables/dense_covered_fluix_clean"));

        for (var color : AEColor.VALID_COLORS) {
            ShapedRecipeBuilder.shaped(AEParts.SMART_DENSE_CABLE.item(color), 8)
                    .pattern("aaa")
                    .pattern("aba")
                    .pattern("aaa")
                    .define('a', AEParts.SMART_DENSE_CABLE.item(AEColor.TRANSPARENT))
                    .define('b', ConventionTags.dye(color.dye))
                    .unlockedBy("has_dyes/black", has(ConventionTags.dye(color.dye)))
                    .unlockedBy("has_fluix_smart_dense_cable", has(AEParts.SMART_DENSE_CABLE.item(AEColor.TRANSPARENT)))
                    .save(consumer, AppEng.makeId("network/cables/dense_smart_" + color.registryPrefix));
        }
        ShapelessRecipeBuilder.shapeless(AEParts.SMART_DENSE_CABLE.item(AEColor.TRANSPARENT))
                .requires(AEParts.COVERED_DENSE_CABLE.item(AEColor.TRANSPARENT))
                .requires(ConventionTags.REDSTONE)
                .requires(ConventionTags.GLOWSTONE)
                .unlockedBy("has_fluix_covered_dense_cable", has(AEParts.COVERED_DENSE_CABLE.item(AEColor.TRANSPARENT)))
                .unlockedBy("has_dusts/glowstone", has(ConventionTags.GLOWSTONE))
                .unlockedBy("has_dusts/redstone", has(ConventionTags.REDSTONE))
                .save(consumer, AppEng.makeId("network/cables/dense_smart_fluix"));
        ShapelessRecipeBuilder.shapeless(AEParts.SMART_DENSE_CABLE.item(AEColor.TRANSPARENT))
                .requires(AEParts.SMART_CABLE.item(AEColor.TRANSPARENT))
                .requires(AEParts.SMART_CABLE.item(AEColor.TRANSPARENT))
                .requires(AEParts.SMART_CABLE.item(AEColor.TRANSPARENT))
                .requires(AEParts.SMART_CABLE.item(AEColor.TRANSPARENT))
                .unlockedBy("has_fluix_smart_cable", has(AEParts.SMART_CABLE.item(AEColor.TRANSPARENT)))
                .save(consumer, AppEng.makeId("network/cables/dense_smart_from_smart"));
        ShapelessRecipeBuilder.shapeless(AEParts.SMART_DENSE_CABLE.item(AEColor.TRANSPARENT))
                .requires(ConventionTags.SMART_DENSE_CABLE)
                .requires(ConventionTags.CAN_REMOVE_COLOR)
                .unlockedBy("has_smart_dense_cable", has(ConventionTags.SMART_DENSE_CABLE))
                .save(consumer, AppEng.makeId("network/cables/dense_smart_fluix_clean"));

        for (var color : AEColor.VALID_COLORS) {
            ShapedRecipeBuilder.shaped(AEParts.GLASS_CABLE.item(color), 8)
                    .pattern("aaa")
                    .pattern("aba")
                    .pattern("aaa")
                    .define('a', AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT))
                    .define('b', ConventionTags.dye(color.dye))
                    .unlockedBy("has_dyes/black", has(ConventionTags.dye(color.dye)))
                    .unlockedBy("has_fluix_glass_cable", has(AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT)))
                    .save(consumer, AppEng.makeId("network/cables/glass_" + color.registryPrefix));
        }

        ShapelessRecipeBuilder.shapeless(AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT), 4)
                .requires(AEParts.QUARTZ_FIBER)
                .requires(ConventionTags.ALL_FLUIX)
                .requires(ConventionTags.ALL_FLUIX)
                .unlockedBy("has_quartz_fiber", has(AEParts.QUARTZ_FIBER))
                .unlockedBy("has_crystals/fluix", has(ConventionTags.ALL_FLUIX))
                .save(consumer, AppEng.makeId("network/cables/glass_fluix"));
        ShapelessRecipeBuilder.shapeless(AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT))
                .requires(ConventionTags.GLASS_CABLE)
                .requires(ConventionTags.CAN_REMOVE_COLOR)
                .unlockedBy("has_glass_cable", has(ConventionTags.GLASS_CABLE))
                .save(consumer, AppEng.makeId("network/cables/glass_fluix_clean"));

        for (var color : AEColor.VALID_COLORS) {
            ShapedRecipeBuilder.shaped(AEParts.SMART_CABLE.item(color), 8)
                    .pattern("aaa")
                    .pattern("aba")
                    .pattern("aaa")
                    .define('a', AEParts.SMART_CABLE.item(AEColor.TRANSPARENT))
                    .define('b', ConventionTags.dye(color.dye))
                    .unlockedBy("has_dyes/black", has(ConventionTags.dye(color.dye)))
                    .unlockedBy("has_fluix_smart_cable", has(AEParts.SMART_CABLE.item(AEColor.TRANSPARENT)))
                    .save(consumer, AppEng.makeId("network/cables/smart_" + color.registryPrefix));
        }

        ShapelessRecipeBuilder.shapeless(AEParts.SMART_CABLE.item(AEColor.TRANSPARENT))
                .requires(AEParts.COVERED_CABLE.item(AEColor.TRANSPARENT))
                .requires(ConventionTags.REDSTONE)
                .requires(ConventionTags.GLOWSTONE)
                .unlockedBy("has_dusts/redstone", has(ConventionTags.REDSTONE))
                .unlockedBy("has_dusts/glowstone", has(ConventionTags.GLOWSTONE))
                .unlockedBy("has_fluix_covered_cable", has(AEParts.COVERED_CABLE.item(AEColor.TRANSPARENT)))
                .save(consumer, AppEng.makeId("network/cables/smart_fluix"));
        ShapelessRecipeBuilder.shapeless(AEParts.SMART_CABLE.item(AEColor.TRANSPARENT))
                .requires(ConventionTags.SMART_CABLE)
                .requires(ConventionTags.CAN_REMOVE_COLOR)
                .unlockedBy("has_smart_cable", has(ConventionTags.SMART_CABLE))
                .save(consumer, AppEng.makeId("network/cables/smart_fluix_clean"));
    }

    private void addPaintBalls(Consumer<FinishedRecipe> consumer) {
        for (var color : AEColor.VALID_COLORS) {
            ShapedRecipeBuilder.shaped(AEItems.COLORED_PAINT_BALL.item(color), 8)
                    .pattern("aaa")
                    .pattern("aba")
                    .pattern("aaa")
                    .define('a', AEItems.MATTER_BALL)
                    .define('b', ConventionTags.dye(color.dye))
                    .unlockedBy("has_matter_ball", has(AEItems.MATTER_BALL))
                    .save(consumer, AppEng.makeId("tools/paintballs_" + color.registryPrefix));

            ShapedRecipeBuilder.shaped(AEItems.COLORED_LUMEN_PAINT_BALL.item(color), 8)
                    .pattern("aaa")
                    .pattern("aba")
                    .pattern("aaa")
                    .define('a', AEItems.COLORED_PAINT_BALL.item(color))
                    .define('b', ConventionTags.GLOWSTONE)
                    .unlockedBy("has_paint_ball", has(ConventionTags.PAINT_BALLS))
                    .save(consumer, AppEng.makeId("tools/paintballs_lumen_" + color.registryPrefix));
        }
    }

}
