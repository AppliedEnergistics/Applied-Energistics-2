/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.api.features;

public enum AEFeature {
    // stuff that has no reason for ever being turned off, or that
    // is just flat out required by tons of
    // important stuff.
    CORE("Core", null, false) {
        @Override
        public boolean isVisible() {
            return false;
        }
    },

    /**
     * Worldgen related
     */
    CERTUS_QUARTZ_WORLD_GEN("CertusQuartzWorldGen", Constants.CATEGORY_WORLD, true),

    METEORITE_WORLD_GEN("MeteoriteWorldGen", Constants.CATEGORY_WORLD, true),

    DECORATIVE_LIGHTS("DecorativeLights", Constants.CATEGORY_WORLD, false),

    DECORATIVE_BLOCKS("DecorativeBlocks", Constants.CATEGORY_WORLD, false,
            "Blocks that are not used in any essential recipes, also slabs and stairs."),

    SKY_STONE_CHESTS("SkyStoneChests", Constants.CATEGORY_WORLD, false),

    SPAWN_PRESSES_IN_METEORITES("SpawnPressesInMeteorites", Constants.CATEGORY_WORLD, true),

    FLOUR("Flour", Constants.CATEGORY_WORLD, false), CHEST_LOOT("ChestLoot", Constants.CATEGORY_WORLD, false),

    VILLAGER_TRADING("VillagerTrading", Constants.CATEGORY_WORLD, true),

    TINY_TNT("TinyTNT", Constants.CATEGORY_WORLD, false),

    CERTUS_ORE("CertusOre", Constants.CATEGORY_WORLD, false),

    CHARGED_CERTUS_ORE("ChargedCertusOre", Constants.CATEGORY_WORLD, false),

    /**
     * Machines
     */
    GRIND_STONE("GrindStone", Constants.CATEGORY_MACHINES, false),

    INSCRIBER("Inscriber", Constants.CATEGORY_MACHINES, false),

    CHARGER("Charger", Constants.CATEGORY_MACHINES, false),

    CRYSTAL_GROWTH_ACCELERATOR("CrystalGrowthAccelerator", Constants.CATEGORY_MACHINES, false),

    POWER_GEN("VibrationChamber", Constants.CATEGORY_MACHINES, false),

    /**
     * Tools
     */
    POWERED_TOOLS("PoweredTools", Constants.CATEGORY_TOOLS_CLASSIFICATIONS, false),

    CERTUS_QUARTZ_TOOLS("CertusQuartzTools", Constants.CATEGORY_TOOLS_CLASSIFICATIONS, false),

    NETHER_QUARTZ_TOOLS("NetherQuartzTools", Constants.CATEGORY_TOOLS_CLASSIFICATIONS, false),

    QUARTZ_HOE("QuartzHoe", Constants.CATEGORY_TOOLS, false),

    QUARTZ_SPADE("QuartzSpade", Constants.CATEGORY_TOOLS, false),

    QUARTZ_SWORD("QuartzSword", Constants.CATEGORY_TOOLS, false),

    QUARTZ_PICKAXE("QuartzPickaxe", Constants.CATEGORY_TOOLS, false),

    QUARTZ_AXE("QuartzAxe", Constants.CATEGORY_TOOLS, false),

    QUARTZ_KNIFE("QuartzKnife", Constants.CATEGORY_TOOLS, false),

    QUARTZ_WRENCH("QuartzWrench", Constants.CATEGORY_TOOLS, false),

    CHARGED_STAFF("ChargedStaff", Constants.CATEGORY_TOOLS, false),

    ENTROPY_MANIPULATOR("EntropyManipulator", Constants.CATEGORY_TOOLS, false),

    MATTER_CANNON("MatterCannon", Constants.CATEGORY_TOOLS, false),

    WIRELESS_ACCESS_TERMINAL("WirelessAccessTerminal", Constants.CATEGORY_TOOLS, false),

    COLOR_APPLICATOR("ColorApplicator", Constants.CATEGORY_TOOLS, false),

    PAINT_BALLS("PaintBalls", Constants.CATEGORY_TOOLS, false),

    METEORITE_COMPASS("MeteoriteCompass", Constants.CATEGORY_TOOLS, false),

    LIGHT_DETECTOR("LightDetector", Constants.CATEGORY_MISC, false),

    /**
     * Network
     */
    SECURITY("Security", Constants.CATEGORY_NETWORK_FEATURES, false),

    SPATIAL_IO("SpatialIO", Constants.CATEGORY_NETWORK_FEATURES, false),

    QUANTUM_NETWORK_BRIDGE("QuantumNetworkBridge", Constants.CATEGORY_NETWORK_FEATURES, false),

    CHANNELS("Channels", Constants.CATEGORY_NETWORK_FEATURES, false),

    /**
     * Buses and parts
     */
    INTERFACE("Interface", Constants.CATEGORY_NETWORK_BUSES, false),

    FLUID_INTERFACE("FluidInterface", Constants.CATEGORY_NETWORK_BUSES, false),

    LEVEL_EMITTER("LevelEmitter", Constants.CATEGORY_NETWORK_BUSES, false),

    FLUID_LEVEL_EMITTER("FluidLevelEmitter", Constants.CATEGORY_NETWORK_BUSES, false),

    FLUID_TERMINAL("FluidTerminal", Constants.CATEGORY_NETWORK_BUSES, false),

    CRAFTING_TERMINAL("CraftingTerminal", Constants.CATEGORY_NETWORK_BUSES, false),

    TERMINAL("Terminal", Constants.CATEGORY_NETWORK_BUSES, false),

    STORAGE_MONITOR("StorageMonitor", Constants.CATEGORY_NETWORK_BUSES, false),

    P2P_TUNNEL("P2PTunnel", Constants.CATEGORY_NETWORK_BUSES, false),

    FORMATION_PLANE("FormationPlane", Constants.CATEGORY_NETWORK_BUSES, false),

    FLUID_FORMATION_PLANE("FluidFormationPlane", Constants.CATEGORY_NETWORK_BUSES, false),

    ANNIHILATION_PLANE("AnnihilationPlane", Constants.CATEGORY_NETWORK_BUSES, false),

    IDENTITY_ANNIHILATION_PLANE("IdentityAnnihilationPlane", Constants.CATEGORY_NETWORK_BUSES, false),

    FLUID_ANNIHILATION_PLANE("FluidAnnihilationPlane", Constants.CATEGORY_NETWORK_BUSES, false),

    IMPORT_BUS("ImportBus", Constants.CATEGORY_NETWORK_BUSES, false),

    FLUID_IMPORT_BUS("FluidImportBus", Constants.CATEGORY_NETWORK_BUSES, false),

    EXPORT_BUS("ExportBus", Constants.CATEGORY_NETWORK_BUSES, false),

    FLUID_EXPORT_BUS("FluidExportBus", Constants.CATEGORY_NETWORK_BUSES, false),

    STORAGE_BUS("StorageBus", Constants.CATEGORY_NETWORK_BUSES, false),

    FLUID_STORAGE_BUS("FluidStorageBus", Constants.CATEGORY_NETWORK_BUSES, false),

    PART_CONVERSION_MONITOR("PartConversionMonitor", Constants.CATEGORY_NETWORK_BUSES, false),

    TOGGLE_BUS("ToggleBus", Constants.CATEGORY_NETWORK_BUSES, false),

    PANELS("Panels", Constants.CATEGORY_NETWORK_BUSES, false),

    QUARTZ_FIBER("QuartzFiber", Constants.CATEGORY_NETWORK_BUSES, false),

    CABLE_ANCHOR("CableAnchor", Constants.CATEGORY_NETWORK_BUSES, false),

    /**
     * Portable cells
     */
    PORTABLE_CELL("PortableCell", Constants.CATEGORY_PORTABLE_CELL, false),

    /**
     * Storage
     */
    STORAGE_CELLS("StorageCells", Constants.CATEGORY_STORAGE, false),

    ME_CHEST("MEChest", Constants.CATEGORY_STORAGE, false),

    ME_DRIVE("MEDrive", Constants.CATEGORY_STORAGE, false),

    IO_PORT("IOPort", Constants.CATEGORY_STORAGE, false),

    CONDENSER("Condenser", Constants.CATEGORY_STORAGE, false),

    /**
     * Network tools
     */
    NETWORK_TOOL("NetworkTool", Constants.CATEGORY_NETWORK_TOOL, false),

    MEMORY_CARD("MemoryCard", Constants.CATEGORY_NETWORK_TOOL, false),

    /**
     * Cables
     */
    GLASS_CABLES("GlassCables", Constants.CATEGORY_CABLES, false),

    COVERED_CABLES("CoveredCables", Constants.CATEGORY_CABLES, false),

    SMART_CABLES("SmartCables", Constants.CATEGORY_CABLES, false),

    DENSE_CABLES("DenseCables", Constants.CATEGORY_CABLES, false),

    /**
     * Energy cells
     */
    ENERGY_CELLS("EnergyCells", Constants.CATEGORY_ENERGY, false),

    ENERGY_ACCEPTOR("EnergyAcceptor", Constants.CATEGORY_ENERGY, false),

    DENSE_ENERGY_CELLS("DenseEnergyCells", Constants.CATEGORY_ENERGY, false),

    /**
     * P2P Tunnels
     */
    P2P_TUNNEL_ME("P2PTunnelME", Constants.CATEGORY_P2P_TUNNELS, false),

    P2P_TUNNEL_ITEMS("P2PTunnelItems", Constants.CATEGORY_P2P_TUNNELS, false),

    P2P_TUNNEL_REDSTONE("P2PTunnelRedstone", Constants.CATEGORY_P2P_TUNNELS, false),

    P2P_TUNNEL_EU("P2PTunnelEU", Constants.CATEGORY_P2P_TUNNELS, false),

    P2P_TUNNEL_FE("P2PTunnelFE", Constants.CATEGORY_P2P_TUNNELS, false),

    P2P_TUNNEL_FLUIDS("P2PTunnelFluids", Constants.CATEGORY_P2P_TUNNELS, false),

    P2P_TUNNEL_LIGHT("P2PTunnelLight", Constants.CATEGORY_P2P_TUNNELS, false),

    P2P_TUNNEL_OPEN_COMPUTERS("P2PTunnelOpenComputers", Constants.CATEGORY_P2P_TUNNELS, false),

    P2P_TUNNEL_PRESSURE("P2PTunnelPressure", Constants.CATEGORY_P2P_TUNNELS, false),

    /**
     * Block damage by items or blocks
     */
    MASS_CANNON_BLOCK_DAMAGE("MassCannonBlockDamage", Constants.CATEGORY_BLOCK_FEATURES, false),

    TINY_TNT_BLOCK_DAMAGE("TinyTNTBlockDamage", Constants.CATEGORY_BLOCK_FEATURES, false),

    /**
     * Facades
     */
    FACADES("Facades", Constants.CATEGORY_FACADES, false),
    TILE_ENTITY_FACADES("TileEntityFacades", Constants.CATEGORY_FACADES, false, true,
            "Unsupported: Allows whitelisting TileEntity as facades. Could work, have render issues, or corrupt your world. USE AT YOUR OWN RISK."),

    /**
     * Debug
     */
    UNSUPPORTED_DEVELOPER_TOOLS("UnsupportedDeveloperTools", Constants.CATEGORY_MISC, false, true),

    CREATIVE("Creative", Constants.CATEGORY_MISC, false),

    GRINDER_LOGGING("GrinderLogging", Constants.CATEGORY_MISC, false, false),

    LOGGING("Logging", Constants.CATEGORY_MISC, false),

    INTEGRATION_LOGGING("IntegrationLogging", Constants.CATEGORY_MISC, false, true),

    WEBSITE_RECIPES("WebsiteRecipes", Constants.CATEGORY_MISC, false, true),

    LOG_SECURITY_AUDITS("LogSecurityAudits", Constants.CATEGORY_MISC, false, true),

    ACHIEVEMENTS("Achievements", Constants.CATEGORY_MISC, false),

    UPDATE_LOGGING("UpdateLogging", Constants.CATEGORY_MISC, false, true),

    PACKET_LOGGING("PacketLogging", Constants.CATEGORY_MISC, false, true),

    CRAFTING_LOG("CraftingLog", Constants.CATEGORY_MISC, false, true),

    DEBUG_LOGGING("DebugLogging", Constants.CATEGORY_MISC, false, true),

    /**
     * Crafting
     */
    ENABLE_FACADE_CRAFTING("EnableFacadeCrafting", Constants.CATEGORY_CRAFTING, true),

    IN_WORLD_SINGULARITY("InWorldSingularity", Constants.CATEGORY_CRAFTING, true),

    IN_WORLD_FLUIX("InWorldFluix", Constants.CATEGORY_CRAFTING, true),

    IN_WORLD_PURIFICATION("InWorldPurification", Constants.CATEGORY_CRAFTING, true),

    INTERFACE_TERMINAL("InterfaceTerminal", Constants.CATEGORY_CRAFTING, false),

    ENABLE_DISASSEMBLY_CRAFTING("EnableDisassemblyCrafting", Constants.CATEGORY_CRAFTING, true),

    /**
     * Rendering
     */
    ALPHA_PASS("AlphaPass", Constants.CATEGORY_RENDERING, true),

    /**
     * Crafting machines
     */
    MOLECULAR_ASSEMBLER("MolecularAssembler", Constants.CATEGORY_CRAFTING_FEATURES, false),

    PATTERNS("Patterns", Constants.CATEGORY_CRAFTING_FEATURES, false),

    CRAFTING_CPU("CraftingCPU", Constants.CATEGORY_CRAFTING_FEATURES, false),

    /**
     * Upgrades
     */
    BASIC_CARDS("BasicCards", Constants.CATEGORY_UPGRADES, false),

    ADVANCED_CARDS("AdvancedCards", Constants.CATEGORY_UPGRADES, false),

    VIEW_CELL("ViewCell", Constants.CATEGORY_UPGRADES, false),

    CRYSTAL_SEEDS("CrystalSeeds", Constants.CATEGORY_MATERIALS, false),

    PURE_CRYSTALS("PureCrystals", Constants.CATEGORY_MATERIALS, false),

    /**
     * Materials. Items mostly for crafting
     */
    CERTUS("Certus", Constants.CATEGORY_MATERIALS, false),

    FLUIX("Fluix", Constants.CATEGORY_MATERIALS, false),

    SILICON("Silicon", Constants.CATEGORY_MATERIALS, false),

    DUSTS("Dusts", Constants.CATEGORY_MATERIALS, false),

    NUGGETS("Nuggets", Constants.CATEGORY_MATERIALS, false),

    QUARTZ_GLASS("QuartzGlass", Constants.CATEGORY_MATERIALS, false),

    SKY_STONE("SkyStone", Constants.CATEGORY_MATERIALS, false),

    PROCESSORS("Processors", Constants.CATEGORY_COMPONENTS, false),

    PRINTED_CIRCUITS("PrintedCircuits", Constants.CATEGORY_COMPONENTS, false),

    PRESSES("Presses", Constants.CATEGORY_COMPONENTS, false),

    MATTER_BALL("MatterBall", Constants.CATEGORY_COMPONENTS, false),

    CORES("Cores", Constants.CATEGORY_COMPONENTS, false),

    /**
     * Server commands
     */
    CHUNK_LOGGER_TRACE("ChunkLoggerTrace", Constants.CATEGORY_COMMANDS, false, true);

    private final String key;
    private final String category;
    private final boolean enabled;
    private final String comment;
    private final boolean config;

    AEFeature(final String key, final String cat, boolean config) {
        this(key, cat, true, config);
    }

    AEFeature(final String key, final String cat, boolean config, final String comment) {
        this(key, cat, true, config, comment);
    }

    AEFeature(final String key, final String cat, final boolean enabled, boolean config) {
        this(key, cat, enabled, config, null);
    }

    AEFeature(final String key, final String cat, final boolean enabled, boolean config, final String comment) {
        this.key = key;
        this.category = cat;
        this.enabled = enabled;
        this.comment = comment;
        this.config = config;
    }

    /**
     * override to set visibility
     *
     * @return default true
     */
    public boolean isVisible() {
        return true;
    }

    public String key() {
        return this.key;
    }

    public String category() {
        return this.category;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * If exposed as config option.
     */
    public boolean isConfig() {
        return config;
    }

    public String comment() {
        return this.comment;
    }

    private enum Constants {
        ;

        private static final String CATEGORY_MISC = "Misc";
        private static final String CATEGORY_CRAFTING = "Crafting";
        private static final String CATEGORY_WORLD = "World";
        private static final String CATEGORY_MACHINES = "Machines";
        private static final String CATEGORY_TOOLS = "Tools";
        private static final String CATEGORY_TOOLS_CLASSIFICATIONS = "ToolsClassifications";
        private static final String CATEGORY_NETWORK_BUSES = "NetworkBuses";
        private static final String CATEGORY_P2P_TUNNELS = "P2PTunnels";
        private static final String CATEGORY_BLOCK_FEATURES = "BlockFeatures";
        private static final String CATEGORY_CRAFTING_FEATURES = "CraftingFeatures";
        private static final String CATEGORY_STORAGE = "Storage";
        private static final String CATEGORY_CABLES = "Cables";
        private static final String CATEGORY_NETWORK_FEATURES = "NetworkFeatures";
        private static final String CATEGORY_COMMANDS = "Commands";
        private static final String CATEGORY_RENDERING = "Rendering";
        private static final String CATEGORY_FACADES = "Facades";
        private static final String CATEGORY_NETWORK_TOOL = "NetworkTool";
        private static final String CATEGORY_PORTABLE_CELL = "PortableCell";
        private static final String CATEGORY_ENERGY = "Energy";
        private static final String CATEGORY_UPGRADES = "Upgrades";
        private static final String CATEGORY_MATERIALS = "Materials";
        private static final String CATEGORY_COMPONENTS = "CraftingComponents";
    }
}
