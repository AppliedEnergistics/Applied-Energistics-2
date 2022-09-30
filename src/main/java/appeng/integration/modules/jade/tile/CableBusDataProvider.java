package appeng.integration.modules.jade.tile;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.HitResult;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

import appeng.api.integrations.waila.AEJadeIds;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.block.networking.CableBusBlock;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.integration.modules.jade.part.AnnihilationPlaneDataProvider;
import appeng.integration.modules.jade.part.ChannelDataProvider;
import appeng.integration.modules.jade.part.GridNodeStateProvider;
import appeng.integration.modules.jade.part.IPartDataProvider;
import appeng.integration.modules.jade.part.P2PStateDataProvider;
import appeng.integration.modules.jade.part.StorageMonitorDataProvider;
import appeng.util.Platform;

public final class CableBusDataProvider {

    private static final List<IPartDataProvider> PROVIDERS = List.of(
            new ChannelDataProvider(),
            new StorageMonitorDataProvider(),
            new AnnihilationPlaneDataProvider(),
            new GridNodeStateProvider(),
            new P2PStateDataProvider(),
            new DebugDataProvider());

    private static IElementHelper elementHelper;

    private CableBusDataProvider() {
    }

    public static void register(IWailaClientRegistration registrar) {
        CableBusDataProvider.elementHelper = registrar.getElementHelper();

        registrar.registerBlockIcon(new IconProvider(), CableBusBlock.class);
        registrar.registerBlockComponent(new NameProvider(), CableBusBlock.class);
        registrar.registerBlockComponent(
                new TooltipAdapter(AEJadeIds.PART_PROVIDER, IPartDataProvider::appendBodyTooltip),
                CableBusBlock.class);
    }

    public static void register(IWailaCommonRegistration registrar) {
        registrar.registerBlockDataProvider(new ServerDataAdapter(), CableBusBlockEntity.class);
    }

    private static class NameProvider implements IBlockComponentProvider {
        private static final ResourceLocation OBJECT_NAME = new ResourceLocation("waila:object_name");

        @Override
        public ResourceLocation getUid() {
            return AEJadeIds.PART_NAME_PROVIDER;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            var blockEntity = accessor.getBlockEntity();
            var hitResult = accessor.getHitResult();

            var selected = getPart(blockEntity, hitResult);

            Component name = null;
            if (selected.facade != null) {
                name = selected.facade.getItemStack().getHoverName();
            } else if (selected.part != null) {
                name = selected.part.getPartItem().asItem().getDescription();
            }

            // Replace the object name
            if (name != null) {
                tooltip.remove(Identifiers.CORE_OBJECT_NAME);
                tooltip.add(0, name.copy().withStyle(style -> {
                    // Don't overwrite a text color if one is present
                    if (style.getColor() == null) {
                        return style.withColor(ChatFormatting.WHITE);
                    } else {
                        return style;
                    }
                }), Identifiers.CORE_OBJECT_NAME);
            }
        }
    }

    private static class IconProvider implements IBlockComponentProvider {
        @Override
        public ResourceLocation getUid() {
            return AEJadeIds.PART_ICON_PROVIDER;
        }

        @Nullable
        @Override
        public IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
            var blockEntity = accessor.getBlockEntity();
            var hitResult = accessor.getHitResult();

            var selected = getPart(blockEntity, hitResult);
            if (selected.facade != null) {
                return elementHelper.item(selected.facade.getItemStack());
            } else if (selected.part != null) {
                var item = selected.part.getPartItem();
                return elementHelper.item(new ItemStack(item));
            }
            return currentIcon;
        }

        @Override
        public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        }
    }

    private record TooltipAdapter(ResourceLocation id, TooltipAppender appender) implements IBlockComponentProvider {
        @Override
        public ResourceLocation getUid() {
            return id;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            // Pick the part the cursor is on
            var selected = getPart(accessor.getBlockEntity(), accessor.getHitResult());
            if (selected.part != null) {
                // Then pick the data for that particular part
                var partTag = accessor.getServerData().getCompound(getPartDataName(selected.side));

                for (var provider : PROVIDERS) {
                    appender.append(provider, selected.part, partTag, tooltip);
                }
            }
        }
    }

    private static class ServerDataAdapter implements IServerDataProvider<BlockEntity> {
        @Override
        public ResourceLocation getUid() {
            return AEJadeIds.PART_DATA_PROVIDER;
        }

        @Override
        public void appendServerData(CompoundTag serverData, ServerPlayer serverPlayer, Level level,
                BlockEntity blockEntity, boolean showDetails) {

            if (!(blockEntity instanceof CableBusBlockEntity cableBus)) {
                return;
            }

            var partTag = new CompoundTag();
            for (var location : Platform.DIRECTIONS_WITH_NULL) {
                var part = cableBus.getPart(location);
                if (part == null) {
                    continue;
                }

                for (var provider : PROVIDERS) {
                    provider.appendServerData(serverPlayer, part, partTag);
                }

                // Send it to the client if there's some data for it
                if (!partTag.isEmpty()) {
                    serverData.put(getPartDataName(location), partTag);
                    partTag = new CompoundTag();
                }
            }

        }
    }

    private static String getPartDataName(@Nullable Direction location) {
        return "cableBusPart" + (location == null ? "center" : location.name());
    }

    @FunctionalInterface
    interface TooltipAppender {
        void append(IPartDataProvider provider, IPart part, CompoundTag partData, ITooltip tooltip);
    }

    /**
     * Hits a {@link IPartHost} with {@link net.minecraft.core.BlockPos}.
     * <p/>
     * You can derive the looked at {@link IPart} by doing that. If a facade is being looked at, it is defined as being
     * absent.
     *
     * @param blockEntity being looked at {@link BlockEntity}
     * @param hitResult   type of ray-trace
     * @return maybe the looked at {@link IPart}
     */
    private static SelectedPart getPart(BlockEntity blockEntity, HitResult hitResult) {
        if (blockEntity instanceof IPartHost host) {
            return host.selectPartWorld(hitResult.getLocation());
        }

        return new SelectedPart();
    }

}
