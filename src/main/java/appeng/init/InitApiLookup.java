package appeng.init;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;

import team.reborn.energy.api.EnergyStorage;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.implementations.blockentities.ICrankable;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.inventories.PartApiLookup;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.blockentity.AEBaseInvBlockEntity;
import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.blockentity.misc.QuartzGrowthAcceleratorBlockEntity;
import appeng.blockentity.powersink.AEBasePoweredBlockEntity;
import appeng.blockentity.storage.ChestBlockEntity;
import appeng.blockentity.storage.SkyStoneTankBlockEntity;
import appeng.core.definitions.AEBlockEntities;
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

public final class InitApiLookup {

    private InitApiLookup() {
    }

    public static void init() {

        // Allow forwarding of API lookups to parts for the cable bus
        PartApiLookup.addHostType(AEBlockEntities.CABLE_BUS);

        // Forward to interfaces
        initInterface();
        initPatternProvider();
        initCondenser();
        initMEChest();
        initMisc();
        initEnergyAcceptors();
        initP2P();
        initPoweredItem();
        initCrankable();

        ItemStorage.SIDED.registerFallback((world, pos, state, blockEntity, direction) -> {
            if (blockEntity instanceof AEBaseInvBlockEntity baseInvBlockEntity) {
                return baseInvBlockEntity.getExposedInventoryForSide(direction).toStorage();
            }
            // Fall back to generic inv
            var genericInv = GenericInternalInventory.SIDED.find(world, pos, state, blockEntity, direction);
            if (genericInv != null) {
                return new GenericStackItemStorage(genericInv);
            }
            return null;
        });

        FluidStorage.SIDED.registerFallback((world, pos, state, blockEntity, direction) -> {
            // Fall back to generic inv
            var genericInv = GenericInternalInventory.SIDED.find(world, pos, state, blockEntity, direction);
            if (genericInv != null) {
                return new GenericStackFluidStorage(genericInv);
            }
            return null;
        });

        EnergyStorage.SIDED.registerFallback((world, pos, state, blockEntity, direction) -> {
            if (blockEntity instanceof AEBasePoweredBlockEntity poweredBlockEntity) {
                return poweredBlockEntity.getEnergyStorage(direction);
            }
            return null;
        });
    }

    private static void initP2P() {
        PartApiLookup.register(ItemStorage.SIDED, (part, context) -> part.getExposedApi(), ItemP2PTunnelPart.class);
        PartApiLookup.register(EnergyStorage.SIDED, (part, context) -> part.getExposedApi(), FEP2PTunnelPart.class);
        PartApiLookup.register(FluidStorage.SIDED, (part, context) -> part.getExposedApi(), FluidP2PTunnelPart.class);
    }

    private static void initEnergyAcceptors() {
        PartApiLookup.register(EnergyStorage.SIDED, (part, context) -> part.getEnergyAdapter(),
                EnergyAcceptorPart.class);
        // The block version is handled by the generic fallback registration for AEBasePoweredBlockEntity
    }

    private static void initInterface() {
        PartApiLookup.register(GenericInternalInventory.SIDED, (part, context) -> part.getInterfaceLogic().getStorage(),
                InterfacePart.class);
        GenericInternalInventory.SIDED.registerForBlockEntity(
                (blockEntity, context) -> blockEntity.getInterfaceLogic().getStorage(), AEBlockEntities.INTERFACE);

        PartApiLookup.register(IStorageMonitorableAccessor.SIDED,
                (part, context) -> part.getInterfaceLogic().getGridStorageAccessor(), InterfacePart.class);
        IStorageMonitorableAccessor.SIDED.registerForBlockEntity((blockEntity, context) -> {
            return blockEntity.getInterfaceLogic().getGridStorageAccessor();
        }, AEBlockEntities.INTERFACE);
    }

    private static void initPatternProvider() {
        PartApiLookup.register(GenericInternalInventory.SIDED, (part, context) -> part.getLogic().getReturnInv(),
                PatternProviderPart.class);
        GenericInternalInventory.SIDED.registerForBlockEntity(
                (blockEntity, context) -> blockEntity.getLogic().getReturnInv(), AEBlockEntities.PATTERN_PROVIDER);
    }

    private static void initCondenser() {
        // Condenser will always return its external inventory, even when context is null
        // (unlike the base class it derives from)
        ItemStorage.SIDED.registerForBlockEntity((blockEntity, context) -> {
            return blockEntity.getExternalInv().toStorage();
        }, AEBlockEntities.CONDENSER);
        FluidStorage.SIDED.registerForBlockEntity(((blockEntity, context) -> {
            return blockEntity.getFluidHandler();
        }), AEBlockEntities.CONDENSER);
        IStorageMonitorableAccessor.SIDED.registerForBlockEntity((blockEntity, context) -> {
            return blockEntity.getMEHandler();
        }, AEBlockEntities.CONDENSER);
    }

    private static void initMEChest() {
        FluidStorage.SIDED.registerForBlockEntity(ChestBlockEntity::getFluidHandler, AEBlockEntities.CHEST);
        IStorageMonitorableAccessor.SIDED.registerForBlockEntity(ChestBlockEntity::getMEHandler,
                AEBlockEntities.CHEST);
    }

    private static void initMisc() {
        ICraftingMachine.SIDED.registerSelf(AEBlockEntities.MOLECULAR_ASSEMBLER);
        ItemStorage.SIDED.registerForBlockEntity((blockEntity, context) -> {
            return blockEntity.getItemHandler();
        }, AEBlockEntities.DEBUG_ITEM_GEN);
        EnergyStorage.SIDED.registerSelf(AEBlockEntities.DEBUG_ENERGY_GEN);
        FluidStorage.SIDED.registerForBlockEntity(SkyStoneTankBlockEntity::getStorage, AEBlockEntities.SKY_STONE_TANK);
        PartApiLookup.register(ItemStorage.SIDED, (part, direction) -> part.getLogic().getBlankPatternInv().toStorage(),
                PatternEncodingTerminalPart.class);
    }

    private static void initPoweredItem() {
        EnergyStorage.ITEM.registerFallback((itemStack, context) -> {
            if (itemStack.getItem() instanceof IAEItemPowerStorage) {
                return new PoweredItemCapabilities(context);
            }
            return null;
        });
    }

    private static void initCrankable() {
        ICrankable.LOOKUP.registerForBlockEntity(ChargerBlockEntity::getCrankable, AEBlockEntities.CHARGER);
        ICrankable.LOOKUP.registerForBlockEntity(InscriberBlockEntity::getCrankable, AEBlockEntities.INSCRIBER);
        ICrankable.LOOKUP.registerForBlockEntity(QuartzGrowthAcceleratorBlockEntity::getCrankable,
                AEBlockEntities.QUARTZ_GROWTH_ACCELERATOR);
    }

}
