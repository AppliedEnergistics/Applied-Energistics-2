package appeng.init;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;

import team.reborn.energy.api.EnergyStorage;

import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.inventories.AEApis;
import appeng.api.inventories.PartApiLookup;
import appeng.blockentity.AEBaseInvBlockEntity;
import appeng.blockentity.powersink.AEBasePoweredBlockEntity;
import appeng.blockentity.storage.ChestBlockEntity;
import appeng.core.definitions.AEBlockEntities;
import appeng.items.tools.powered.powersink.PoweredItemCapabilities;
import appeng.parts.crafting.PatternProviderPart;
import appeng.parts.misc.InterfacePart;
import appeng.parts.networking.EnergyAcceptorPart;
import appeng.parts.p2p.CapabilityP2PTunnelPart;
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
        initItemInterface();
        initPatternProvider();
        initCondenser();
        initMEChest();
        initMisc();
        initEnergyAcceptors();
        initP2P();
        initPoweredItem();

        ItemStorage.SIDED.registerFallback((world, pos, state, blockEntity, direction) -> {
            if (blockEntity instanceof AEBaseInvBlockEntity baseInvBlockEntity) {
                return baseInvBlockEntity.getExposedInventoryForSide(direction).toStorage();
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
        PartApiLookup.register(ItemStorage.SIDED, CapabilityP2PTunnelPart::getExposedApi, ItemP2PTunnelPart.class);
        PartApiLookup.register(EnergyStorage.SIDED, CapabilityP2PTunnelPart::getExposedApi, FEP2PTunnelPart.class);
        PartApiLookup.register(FluidStorage.SIDED, CapabilityP2PTunnelPart::getExposedApi, FluidP2PTunnelPart.class);
    }

    private static void initEnergyAcceptors() {
        PartApiLookup.register(EnergyStorage.SIDED, EnergyAcceptorPart::getEnergyAdapter,
                EnergyAcceptorPart.class);
        // The block version is handled by the generic fallback registration for AEBasePoweredBlockEntity
    }

    private static void initItemInterface() {
        PartApiLookup.register(ItemStorage.SIDED,
                part -> part.getInterfaceLogic().getLocalItemStorage(),
                InterfacePart.class);
        PartApiLookup.register(FluidStorage.SIDED, part -> part.getInterfaceLogic().getLocalFluidStorage(),
                InterfacePart.class);
        ItemStorage.SIDED.registerForBlockEntity((blockEntity, context) -> {
            return blockEntity.getInterfaceLogic().getLocalItemStorage();
        }, AEBlockEntities.INTERFACE);
        FluidStorage.SIDED.registerForBlockEntity((blockEntity, context) -> {
            return blockEntity.getInterfaceLogic().getLocalFluidStorage();
        }, AEBlockEntities.INTERFACE);
        PartApiLookup.register(AEApis.STORAGE_ACCESSOR,
                part -> part.getInterfaceLogic().getGridStorageAccessor(), InterfacePart.class);
        AEApis.STORAGE_ACCESSOR.registerForBlockEntity((blockEntity, context) -> {
            return blockEntity.getInterfaceLogic().getGridStorageAccessor();
        }, AEBlockEntities.INTERFACE);
    }

    private static void initPatternProvider() {
        PartApiLookup.register(ItemStorage.SIDED, part -> part.getDuality().getReturnInv().getItemStorage(),
                PatternProviderPart.class);
        ItemStorage.SIDED.registerForBlockEntity(
                (blockEntity, direction) -> blockEntity.getDuality().getReturnInv().getItemStorage(),
                AEBlockEntities.PATTERN_PROVIDER);

        PartApiLookup.register(FluidStorage.SIDED,
                part -> part.getDuality().getReturnInv().getFluidStorage(), PatternProviderPart.class);
        FluidStorage.SIDED.registerForBlockEntity(
                (blockEntity, direction) -> blockEntity.getDuality().getReturnInv().getFluidStorage(),
                AEBlockEntities.PATTERN_PROVIDER);
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
        AEApis.STORAGE_ACCESSOR.registerForBlockEntity((blockEntity, context) -> {
            return blockEntity.getMEHandler();
        }, AEBlockEntities.CONDENSER);
    }

    private static void initMEChest() {
        FluidStorage.SIDED.registerForBlockEntity(ChestBlockEntity::getFluidHandler, AEBlockEntities.CHEST);
        AEApis.STORAGE_ACCESSOR.registerForBlockEntity(ChestBlockEntity::getMEHandler,
                AEBlockEntities.CHEST);
    }

    private static void initMisc() {
        AEApis.CRAFTING_MACHINE.registerSelf(AEBlockEntities.MOLECULAR_ASSEMBLER);
        ItemStorage.SIDED.registerForBlockEntity((blockEntity, context) -> {
            return blockEntity.getItemHandler();
        }, AEBlockEntities.DEBUG_ITEM_GEN);
        EnergyStorage.SIDED.registerSelf(AEBlockEntities.DEBUG_ENERGY_GEN);
    }

    private static void initPoweredItem() {
        EnergyStorage.ITEM.registerFallback((itemStack, context) -> {
            if (itemStack.getItem() instanceof IAEItemPowerStorage) {
                return new PoweredItemCapabilities(context);
            }
            return null;
        });
    }

}
