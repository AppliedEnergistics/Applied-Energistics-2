package appeng.integration.modules.igtooltip;

import appeng.api.integrations.igtooltip.InGameTooltipProvider;
import appeng.api.parts.IPart;
import appeng.block.AEBaseEntityBlock;
import appeng.block.crafting.CraftingMonitorBlock;
import appeng.block.misc.ChargerBlock;
import appeng.block.networking.CableBusBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.crafting.CraftingMonitorBlockEntity;
import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.integration.modules.igtooltip.blocks.BlockDebugProvider;
import appeng.integration.modules.igtooltip.blocks.CableBusDataProvider;
import appeng.integration.modules.igtooltip.blocks.ChargerDataProvider;
import appeng.integration.modules.igtooltip.blocks.CraftingMonitorDataProvider;
import appeng.integration.modules.igtooltip.blocks.GridNodeStateDataProvider;
import appeng.integration.modules.igtooltip.blocks.PowerStorageDataProvider;
import appeng.integration.modules.igtooltip.part.AnnihilationPlaneDataProvider;
import appeng.integration.modules.igtooltip.part.ChannelDataProvider;
import appeng.integration.modules.igtooltip.part.GridNodeStateProvider;
import appeng.integration.modules.igtooltip.part.P2PStateDataProvider;
import appeng.integration.modules.igtooltip.part.PartDebugDataProvider;
import appeng.integration.modules.igtooltip.part.StorageMonitorDataProvider;
import appeng.parts.AEBasePart;
import appeng.parts.automation.AnnihilationPlanePart;
import appeng.parts.networking.IUsedChannelProvider;
import appeng.parts.networking.SmartCablePart;
import appeng.parts.networking.SmartDenseCablePart;
import appeng.parts.p2p.P2PTunnelPart;
import appeng.parts.reporting.AbstractMonitorPart;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

/**
 * Keeps track of all registered {@link InGameTooltipProvider}.
 *
 * @see appeng.api.integrations.igtooltip.InGameTooltipRegistry
 */
public final class InGameTooltipProviders {

    public static final int POSITION_INFO = 10000;
    public static final int POSITION_DEBUG = 1000000;

    private static final ProviderRegistrationMap<PartRegistration<?>> partProviders
            = new ProviderRegistrationMap<>();

    private static final ProviderRegistrationMap<BlockRegistration<?>> blockEntityProviders
            = new ProviderRegistrationMap<>();

    static {
        registerPart(SmartCablePart.class, new ChannelDataProvider<>(), POSITION_INFO);
        registerPart(SmartDenseCablePart.class, new ChannelDataProvider<>(), POSITION_INFO);
        registerPart(AbstractMonitorPart.class, new StorageMonitorDataProvider(), POSITION_INFO);
        registerPart(AnnihilationPlanePart.class, new AnnihilationPlaneDataProvider(), POSITION_INFO);
        registerPart(IPart.class, new GridNodeStateProvider(), POSITION_INFO);
        registerPart(P2PTunnelPart.class, new P2PStateDataProvider(), POSITION_INFO);
        registerPart(AEBasePart.class, new PartDebugDataProvider(), POSITION_DEBUG);

        registerBlockEntity(CableBusBlock.class, CableBusBlockEntity.class, new CableBusDataProvider());
        registerBlockEntity(ChargerBlock.class, ChargerBlockEntity.class, new ChargerDataProvider());
        registerBlockEntity(AEBaseEntityBlock.class, AEBaseBlockEntity.class, new PowerStorageDataProvider());
        registerBlockEntity(AEBaseEntityBlock.class, AEBaseBlockEntity.class, new GridNodeStateDataProvider());
        registerBlockEntity(CraftingMonitorBlock.class, CraftingMonitorBlockEntity.class, new CraftingMonitorDataProvider());
        registerBlockEntity(AEBaseEntityBlock.class, AEBaseBlockEntity.class, new BlockDebugProvider());
    }

    private InGameTooltipProviders() {
    }

    public static <T extends IPart> void registerPart(Class<T> partClass, InGameTooltipProvider<? super T> provider) {
        registerPart(partClass, provider, POSITION_INFO);
    }

    public static <T extends BlockEntity> void registerBlockEntity(Class<? extends Block> blockClass,
                                                                   Class<T> blockEntityClass,
                                                                   InGameTooltipProvider<? super T> provider) {
        registerBlockEntity(blockClass, blockEntityClass, provider, POSITION_INFO);
    }

    public static <T extends IPart> void registerPart(Class<T> partClass, InGameTooltipProvider<? super T> provider, int position) {
        partProviders.register(new PartRegistration<>(partClass, provider, position));
    }

    public static <T extends BlockEntity> void registerBlockEntity(Class<? extends Block> blockClass,
                                                                   Class<T> blockEntityClass,
                                                                   InGameTooltipProvider<? super T> provider,
                                                                   int position) {
        blockEntityProviders.register(new BlockRegistration<>(blockClass, blockEntityClass, provider, position));
    }

    /**
     * Gets a list of all tooltip providers applicable to the given part class.
     */
    @SuppressWarnings("unchecked")
    public static <T extends IPart> List<InGameTooltipProvider<? super T>> getPartProviders(T part) {
        return partProviders.getProviders((Class<T>) part.getClass());
    }

    public static <T extends BlockEntity> List<InGameTooltipProvider<? super T>> getBlockEntityProviders(Class<T> blockEntityClass) {
        return blockEntityProviders.getProviders(blockEntityClass);
    }

    public static List<BlockRegistration<?>> getBlockProviders() {
        return blockEntityProviders.getRegistrations();
    }

    private record PartRegistration<T extends IPart>(Class<T> partClass,
                                                     InGameTooltipProvider<? super T> provider,
                                                     int position) implements ProviderRegistrationMap.Registration {
        @Override
        public boolean supports(Class<?> objectClass) {
            return partClass.isAssignableFrom(objectClass);
        }

        @Override
        public InGameTooltipProvider<?> getProvider() {
            return provider;
        }

        @Override
        public int getPosition() {
            return position;
        }
    }

    public record BlockRegistration<T extends BlockEntity>(Class<? extends Block> blockClass,
                                                            Class<T> blockEntityClass,
                                                            InGameTooltipProvider<? super T> provider,
                                                            int position) implements ProviderRegistrationMap.Registration {
        @Override
        public boolean supports(Class<?> objectClass) {
            return blockEntityClass.isAssignableFrom(objectClass);
        }

        @Override
        public InGameTooltipProvider<?> getProvider() {
            return provider;
        }

        @Override
        public int getPosition() {
            return position;
        }
    }
}
