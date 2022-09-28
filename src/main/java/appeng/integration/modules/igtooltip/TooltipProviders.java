package appeng.integration.modules.igtooltip;

import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appeng.api.integrations.igtooltip.BaseClassRegistration;
import appeng.api.integrations.igtooltip.ClientRegistration;
import appeng.api.integrations.igtooltip.CommonRegistration;
import appeng.api.integrations.igtooltip.PartTooltips;
import appeng.api.integrations.igtooltip.TooltipProvider;
import appeng.api.parts.IPart;
import appeng.block.AEBaseEntityBlock;
import appeng.block.crafting.CraftingMonitorBlock;
import appeng.block.misc.ChargerBlock;
import appeng.block.networking.CableBusBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.crafting.CraftingMonitorBlockEntity;
import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.integration.modules.igtooltip.blocks.ChargerDataProvider;
import appeng.integration.modules.igtooltip.blocks.CraftingMonitorDataProvider;
import appeng.integration.modules.igtooltip.blocks.GridNodeStateDataProvider;
import appeng.integration.modules.igtooltip.blocks.PowerStorageDataProvider;
import appeng.integration.modules.igtooltip.parts.AnnihilationPlaneDataProvider;
import appeng.integration.modules.igtooltip.parts.ChannelDataProvider;
import appeng.integration.modules.igtooltip.parts.GridNodeStateProvider;
import appeng.integration.modules.igtooltip.parts.P2PStateDataProvider;
import appeng.integration.modules.igtooltip.parts.PartHostTooltips;
import appeng.integration.modules.igtooltip.parts.StorageMonitorDataProvider;
import appeng.parts.AEBasePart;
import appeng.parts.automation.AnnihilationPlanePart;
import appeng.parts.networking.IUsedChannelProvider;
import appeng.parts.p2p.P2PTunnelPart;
import appeng.parts.reporting.AbstractMonitorPart;

public final class TooltipProviders implements TooltipProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(TooltipProviders.class);

    public static final ServiceLoader<TooltipProvider> LOADER = ServiceLoader.load(TooltipProvider.class);

    static {
        // We just have to do this once
        PartTooltips.addBody(IUsedChannelProvider.class, new ChannelDataProvider());
        PartTooltips.addServerData(IUsedChannelProvider.class, new ChannelDataProvider());
        PartTooltips.addBody(AbstractMonitorPart.class, new StorageMonitorDataProvider());
        PartTooltips.addBody(AnnihilationPlanePart.class, new AnnihilationPlaneDataProvider());
        PartTooltips.addServerData(AnnihilationPlanePart.class, new AnnihilationPlaneDataProvider());
        PartTooltips.addBody(IPart.class, new GridNodeStateProvider());
        PartTooltips.addServerData(IPart.class, new GridNodeStateProvider());
        PartTooltips.addBody(P2PTunnelPart.class, new P2PStateDataProvider());
        PartTooltips.addServerData(P2PTunnelPart.class, new P2PStateDataProvider());
        PartTooltips.addBody(AEBasePart.class, DebugProvider::providePartBody, DEBUG_PRIORITY);
        PartTooltips.addServerData(AEBasePart.class, DebugProvider::providePartData, DEBUG_PRIORITY);
    }

    public static void loadCommon(CommonRegistration registration) {
        var baseClasses = new BaseClassRegistrationImpl();

        for (var provider : TooltipProviders.LOADER) {
            provider.registerCommon(registration);
            provider.registerBlockEntityBaseClasses(baseClasses);
        }

        for (var clazz : baseClasses.getBaseClasses()) {
            LOGGER.debug("Registering default-data for BE {} and sub-classes", clazz);
            registration.addBlockEntityData(clazz.blockEntity(), new GridNodeStateDataProvider());
            registration.addBlockEntityData(clazz.blockEntity(), new PowerStorageDataProvider());
            registration.addBlockEntityData(clazz.blockEntity(), DebugProvider::provideBlockEntityData);
        }

        for (var clazz : baseClasses.getPartHostClasses()) {
            LOGGER.debug("Registering part host provider for {} and sub-classes", clazz);
            registration.addBlockEntityData(clazz.blockEntity(), PartHostTooltips::provideServerData);
        }
    }

    public static void loadClient(ClientRegistration registration) {
        var baseClasses = new BaseClassRegistrationImpl();

        for (var provider : TooltipProviders.LOADER) {
            provider.registerClient(registration);
            provider.registerBlockEntityBaseClasses(baseClasses);
        }

        for (var clazz : baseClasses.getBaseClasses()) {
            LOGGER.debug("Registering default client providers for BE {} and sub-classes", clazz);
            registration.addBlockEntityBody(
                    clazz.blockEntity(),
                    clazz.block(),
                    TooltipIds.POWER_STORAGE,
                    new PowerStorageDataProvider());
            registration.addBlockEntityBody(
                    clazz.blockEntity(),
                    clazz.block(),
                    TooltipIds.GRID_NODE_STATE,
                    new GridNodeStateDataProvider());
            registration.addBlockEntityBody(
                    clazz.blockEntity(),
                    clazz.block(),
                    TooltipIds.DEBUG,
                    DebugProvider::provideBlockEntityBody,
                    TooltipProvider.DEBUG_PRIORITY);
        }

        for (var clazz : baseClasses.getPartHostClasses()) {
            LOGGER.debug("Registering part host provider for {} and sub-classes", clazz);
            registration.addBlockEntityName(
                    clazz.blockEntity(),
                    clazz.block(),
                    TooltipIds.PART_NAME,
                    PartHostTooltips::getName);
            registration.addBlockEntityIcon(
                    clazz.blockEntity(),
                    clazz.block(),
                    TooltipIds.PART_ICON,
                    PartHostTooltips::getIcon);
            registration.addBlockEntityBody(
                    clazz.blockEntity(),
                    clazz.block(),
                    TooltipIds.PART_TOOLTIP,
                    PartHostTooltips::buildTooltip);
            registration.addBlockEntityModName(
                    clazz.blockEntity(),
                    clazz.block(),
                    TooltipIds.PART_MOD_NAME,
                    PartHostTooltips::getModName);
        }
    }

    @Override
    public void registerClient(ClientRegistration registration) {
        registration.addBlockEntityBody(
                ChargerBlockEntity.class,
                ChargerBlock.class,
                TooltipIds.CHARGER,
                new ChargerDataProvider());
        registration.addBlockEntityBody(
                CraftingMonitorBlockEntity.class,
                CraftingMonitorBlock.class,
                TooltipIds.CRAFTING_MONITOR,
                new CraftingMonitorDataProvider());
    }

    @Override
    public void registerBlockEntityBaseClasses(BaseClassRegistration registration) {
        registration.addBaseBlockEntity(AEBaseBlockEntity.class, AEBaseEntityBlock.class);
        registration.addPartHost(CableBusBlockEntity.class, CableBusBlock.class);
    }
}
