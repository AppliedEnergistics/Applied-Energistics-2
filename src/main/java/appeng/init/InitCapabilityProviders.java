package appeng.init;

import java.util.function.Function;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import appeng.api.AECapabilities;
import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.parts.RegisterPartCapabilitiesEvent;
import appeng.api.parts.RegisterPartCapabilitiesEventInternal;
import appeng.blockentity.AEBaseInvBlockEntity;
import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.blockentity.misc.GrowthAcceleratorBlockEntity;
import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.blockentity.powersink.AEBasePoweredBlockEntity;
import appeng.blockentity.storage.ChestBlockEntity;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.helpers.externalstorage.GenericStackFluidStorage;
import appeng.helpers.externalstorage.GenericStackItemStorage;
import appeng.items.tools.powered.powersink.PoweredItemCapabilities;
import appeng.parts.crafting.PatternProviderPart;
import appeng.parts.encoding.PatternEncodingTerminalPart;
import appeng.parts.misc.InterfacePart;
import appeng.parts.networking.EnergyAcceptorPart;
import appeng.parts.p2p.FEP2PTunnelPart;
import appeng.parts.p2p.FluidP2PTunnelPart;
import appeng.parts.p2p.ItemP2PTunnelPart;

public final class InitCapabilityProviders {

    private InitCapabilityProviders() {
    }

    public static void register(RegisterCapabilitiesEvent event) {

        var partEvent = new RegisterPartCapabilitiesEvent();
        partEvent.addHostType(AEBlockEntities.CABLE_BUS);
        registerPartCapabilities(partEvent);
        ModLoader.get().postEvent(partEvent);
        RegisterPartCapabilitiesEventInternal.register(partEvent, event);

        initInterface(event);
        initPatternProvider(event);
        initCondenser(event);
        initMEChest(event);
        initMisc(event);
        initPoweredItem(event);
        initCrankable(event);

        for (var type : AEBlockEntities.getSubclassesOf(AEBaseInvBlockEntity.class)) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type,
                    AEBaseInvBlockEntity::getExposedItemHandler);
        }
        for (var type : AEBlockEntities.getSubclassesOf(AEBasePoweredBlockEntity.class)) {
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, type,
                    AEBasePoweredBlockEntity::getEnergyStorage);
        }
        for (var type : AEBlockEntities.getImplementorsOf(IInWorldGridNodeHost.class)) {
            event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, type,
                    (object, context) -> (IInWorldGridNodeHost) object);
        }
    }

    /**
     * This registration is called with the lowest possible priority to register adapters.
     */
    public static void registerGenericAdapters(RegisterCapabilitiesEvent event) {

        for (var block : BuiltInRegistries.BLOCK) {
            if (event.isBlockRegistered(AECapabilities.GENERIC_INTERNAL_INV, block)) {
                registerGenericInvAdapter(event, block, Capabilities.ItemHandler.BLOCK, GenericStackItemStorage::new);
                registerGenericInvAdapter(event, block, Capabilities.FluidHandler.BLOCK, GenericStackFluidStorage::new);
            }
        }

    }

    private static <T> void registerGenericInvAdapter(RegisterCapabilitiesEvent event,
            Block block,
            BlockCapability<T, Direction> capability,
            Function<GenericInternalInventory, T> adapter) {
        event.registerBlock(
                capability,
                (level, pos, state, blockEntity, context) -> {
                    var genericInv = level.getCapability(AECapabilities.GENERIC_INTERNAL_INV, pos, state,
                            blockEntity, context);
                    if (genericInv != null) {
                        return adapter.apply(genericInv);
                    }
                    return null;
                },
                block);
    }

    private static void initInterface(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                AECapabilities.GENERIC_INTERNAL_INV,
                AEBlockEntities.INTERFACE,
                (be, context) -> be.getInterfaceLogic().getStorage());

        event.registerBlockEntity(
                AECapabilities.ME_STORAGE,
                AEBlockEntities.INTERFACE,
                (blockEntity, context) -> {
                    return blockEntity.getInterfaceLogic().getInventory();
                });
    }

    private static void initPatternProvider(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                AECapabilities.GENERIC_INTERNAL_INV,
                AEBlockEntities.PATTERN_PROVIDER,
                (blockEntity, context) -> blockEntity.getLogic().getReturnInv());
    }

    private static void initCondenser(RegisterCapabilitiesEvent event) {
        // Condenser will always return its external inventory, even when context is null
        // (unlike the base class it derives from)
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, AEBlockEntities.CONDENSER, (blockEntity, context) -> {
            return blockEntity.getExternalInv().toItemHandler();
        });
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, AEBlockEntities.CONDENSER,
                ((blockEntity, context) -> {
                    return blockEntity.getFluidHandler();
                }));
        event.registerBlockEntity(AECapabilities.ME_STORAGE, AEBlockEntities.CONDENSER, (blockEntity, context) -> {
            return blockEntity.getMEStorage();
        });
    }

    private static void initMEChest(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, AEBlockEntities.CHEST,
                ChestBlockEntity::getFluidHandler);
        event.registerBlockEntity(AECapabilities.ME_STORAGE, AEBlockEntities.CHEST, ChestBlockEntity::getMEStorage);
    }

    private static void initMisc(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                AECapabilities.CRAFTING_MACHINE,
                AEBlockEntities.MOLECULAR_ASSEMBLER,
                (object, context) -> object);
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                AEBlockEntities.DEBUG_ITEM_GEN,
                (object, context) -> object.getItemHandler());
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                AEBlockEntities.DEBUG_ENERGY_GEN,
                (object, context) -> object);
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                AEBlockEntities.SKY_STONE_TANK,
                (object, context) -> object.getFluidHandler());
    }

    private static void initPoweredItem(RegisterCapabilitiesEvent event) {
        registerPowerStorageItem(event, AEItems.ENTROPY_MANIPULATOR);
        registerPowerStorageItem(event, AEItems.CHARGED_STAFF);
        registerPowerStorageItem(event, AEItems.COLOR_APPLICATOR);
        registerPowerStorageItem(event, AEItems.PORTABLE_ITEM_CELL1K);
        registerPowerStorageItem(event, AEItems.PORTABLE_ITEM_CELL4K);
        registerPowerStorageItem(event, AEItems.PORTABLE_ITEM_CELL16K);
        registerPowerStorageItem(event, AEItems.PORTABLE_ITEM_CELL64K);
        registerPowerStorageItem(event, AEItems.PORTABLE_ITEM_CELL256K);
        registerPowerStorageItem(event, AEItems.PORTABLE_FLUID_CELL1K);
        registerPowerStorageItem(event, AEItems.PORTABLE_FLUID_CELL4K);
        registerPowerStorageItem(event, AEItems.PORTABLE_FLUID_CELL16K);
        registerPowerStorageItem(event, AEItems.PORTABLE_FLUID_CELL64K);
        registerPowerStorageItem(event, AEItems.PORTABLE_FLUID_CELL256K);
        registerPowerStorageItem(event, AEItems.MATTER_CANNON);
        registerPowerStorageItem(event, AEItems.WIRELESS_TERMINAL);
        registerPowerStorageItem(event, AEItems.WIRELESS_CRAFTING_TERMINAL);
    }

    private static <T extends Item & IAEItemPowerStorage> void registerPowerStorageItem(RegisterCapabilitiesEvent event,
            ItemDefinition<T> definition) {
        IAEItemPowerStorage powerStorage = definition.asItem();

        event.registerItem(
                Capabilities.EnergyStorage.ITEM,
                (object, context) -> new PoweredItemCapabilities(object, powerStorage),
                definition);
    }

    private static void initCrankable(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(AECapabilities.CRANKABLE, AEBlockEntities.CHARGER,
                ChargerBlockEntity::getCrankable);
        event.registerBlockEntity(AECapabilities.CRANKABLE, AEBlockEntities.INSCRIBER,
                InscriberBlockEntity::getCrankable);
        event.registerBlockEntity(AECapabilities.CRANKABLE, AEBlockEntities.GROWTH_ACCELERATOR,
                GrowthAcceleratorBlockEntity::getCrankable);
    }

    private static void registerPartCapabilities(RegisterPartCapabilitiesEvent event) {
        event.register(Capabilities.ItemHandler.BLOCK,
                (part, direction) -> part.getLogic().getBlankPatternInv().toItemHandler(),
                PatternEncodingTerminalPart.class);
        event.register(AECapabilities.GENERIC_INTERNAL_INV, (part, context) -> part.getLogic().getReturnInv(),
                PatternProviderPart.class);
        event.register(AECapabilities.GENERIC_INTERNAL_INV,
                (part, context) -> part.getInterfaceLogic().getStorage(),
                InterfacePart.class);
        event.register(AECapabilities.ME_STORAGE,
                (part, context) -> part.getInterfaceLogic().getInventory(), InterfacePart.class);

        event.register(Capabilities.ItemHandler.BLOCK, (part, context) -> part.getExposedApi(),
                ItemP2PTunnelPart.class);
        event.register(Capabilities.EnergyStorage.BLOCK, (part, context) -> part.getExposedApi(),
                FEP2PTunnelPart.class);
        event.register(Capabilities.FluidHandler.BLOCK, (part, context) -> part.getExposedApi(),
                FluidP2PTunnelPart.class);

        event.register(Capabilities.EnergyStorage.BLOCK, (part, context) -> part.getEnergyStorage(),
                EnergyAcceptorPart.class);
    }

}
