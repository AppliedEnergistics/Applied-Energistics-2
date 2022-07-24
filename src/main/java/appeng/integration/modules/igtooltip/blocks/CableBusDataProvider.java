package appeng.integration.modules.igtooltip.blocks;

import appeng.api.integrations.igtooltip.InGameTooltipBuilder;
import appeng.api.integrations.igtooltip.InGameTooltipContext;
import appeng.api.integrations.igtooltip.InGameTooltipProvider;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.integration.modules.igtooltip.InGameTooltipProviders;
import appeng.util.Platform;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CableBusDataProvider implements InGameTooltipProvider<CableBusBlockEntity> {
    @Override
    public @Nullable Component getName(CableBusBlockEntity object, InGameTooltipContext context) {
        var selected = getPart(object, context.hitLocation());

        if (selected.facade != null) {
            return selected.facade.getItemStack().getHoverName();
        } else if (selected.part != null) {
            return selected.part.getPartItem().asItem().getDescription();
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ItemStack getIcon(CableBusBlockEntity object, InGameTooltipContext context) {
        var selected = getPart(object, context.hitLocation());
        if (selected.facade != null) {
            return selected.facade.getItemStack();
        } else if (selected.part != null) {
            return new ItemStack(selected.part.getPartItem());
        } else {
            return null;
        }
    }

    @Override
    public void buildTooltip(CableBusBlockEntity object, InGameTooltipContext context, InGameTooltipBuilder tooltip) {
        // Pick the part the cursor is on
        var selected = getPart(object, context.hitLocation());
        if (selected.part != null) {
            // Then pick the data for that particular part
            var partTag = context.serverData().getCompound(getPartDataName(selected.side));

            buildPartTooltip(selected.part, partTag, context, tooltip);
        }
    }

    private <T extends IPart> void buildPartTooltip(T part,
                                                    CompoundTag partTag,
                                                    InGameTooltipContext blockContext,
                                                    InGameTooltipBuilder tooltip) {
        var partContext = new InGameTooltipContext(partTag, blockContext.hitLocation(), blockContext.player());

        for (var provider : InGameTooltipProviders.getPartProviders(part)) {
            provider.buildTooltip(part, partContext, tooltip);
        }
    }

    @Override
    public void provideServerData(ServerPlayer player, CableBusBlockEntity object, CompoundTag serverData) {
        var partTag = new CompoundTag();
        for (var location : Platform.DIRECTIONS_WITH_NULL) {
            var part = object.getPart(location);
            if (part == null) {
                continue;
            }

            for (var provider : InGameTooltipProviders.getPartProviders(part)) {
                provider.provideServerData(player, part, partTag);
            }

            // Send it to the client if there's some data for it
            if (!partTag.isEmpty()) {
                serverData.put(getPartDataName(location), partTag);
                partTag = new CompoundTag();
            }
        }

    }

    private static String getPartDataName(@Nullable Direction location) {
        return "cableBusPart" + (location == null ? "center" : location.name());
    }

    /**
     * Hits a {@link IPartHost} with {@link net.minecraft.core.BlockPos}.
     * <p/>
     * You can derive the looked at {@link IPart} by doing that. If a facade is being looked at, it is defined as being
     * absent.
     *
     * @return maybe the looked at {@link IPart}
     */
    private static SelectedPart getPart(IPartHost partHost, Vec3 hitLocation) {
        return partHost.selectPartWorld(hitLocation);
    }
}
