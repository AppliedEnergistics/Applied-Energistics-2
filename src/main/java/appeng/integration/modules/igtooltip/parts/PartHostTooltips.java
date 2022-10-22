package appeng.integration.modules.igtooltip.parts;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.util.Platform;

public final class PartHostTooltips {

    private PartHostTooltips() {
    }

    public static @Nullable Component getName(BlockEntity object, TooltipContext context) {
        return getName((IPartHost) object, context);
    }

    public static @Nullable Component getName(IPartHost object, TooltipContext context) {
        var selected = getPart(object, context.hitLocation());

        if (selected.facade != null) {
            return selected.facade.getItemStack().getHoverName();
        } else if (selected.part != null) {
            for (var provider : PartTooltipProviders.getProviders(selected.part).nameProviders()) {
                var name = provider.getName(selected.part, context);
                if (name != null) {
                    return name;
                }
            }

            return selected.part.getPartItem().asItem().getDescription();
        } else {
            return null;
        }
    }

    public static @Nullable String getModName(BlockEntity blockEntity, TooltipContext context) {
        return getModName((IPartHost) blockEntity, context);
    }

    public static @Nullable String getModName(IPartHost object, TooltipContext context) {
        var selected = getPart(object, context.hitLocation());

        Item item;
        if (selected.facade != null) {
            item = selected.facade.getItemStack().getItem();
        } else if (selected.part != null) {
            item = selected.part.getPartItem().asItem();
        } else {
            return null;
        }

        return Platform.getModName(BuiltInRegistries.ITEM.getKey(item).getNamespace());
    }

    public static @Nullable ItemStack getIcon(BlockEntity object, TooltipContext context) {
        return getIcon((IPartHost) object, context);
    }

    public static @Nullable ItemStack getIcon(IPartHost object, TooltipContext context) {
        var selected = getPart(object, context.hitLocation());
        if (selected.facade != null) {
            return selected.facade.getItemStack();
        } else if (selected.part != null) {
            for (var provider : PartTooltipProviders.getProviders(selected.part).iconProviders()) {
                var icon = provider.getIcon(selected.part, context);
                if (icon != null) {
                    return icon;
                }
            }

            return new ItemStack(selected.part.getPartItem());
        } else {
            return null;
        }
    }

    public static void buildTooltip(BlockEntity object, TooltipContext context, TooltipBuilder tooltip) {
        buildTooltip((IPartHost) object, context, tooltip);
    }

    public static void buildTooltip(IPartHost object, TooltipContext context,
            TooltipBuilder tooltip) {
        // Pick the part the cursor is on
        var selected = getPart(object, context.hitLocation());
        if (selected.part != null) {
            // Then pick the data for that particular part
            var partTag = context.serverData().getCompound(getPartDataName(selected.side));

            buildPartTooltip(selected.part, partTag, context, tooltip);
        }
    }

    private static <T extends IPart> void buildPartTooltip(T part,
            CompoundTag partTag,
            TooltipContext blockContext,
            TooltipBuilder tooltip) {
        var partContext = new TooltipContext(partTag, blockContext.hitLocation(), blockContext.player());

        for (var provider : PartTooltipProviders.getProviders(part).bodyProviders()) {
            provider.buildTooltip(part, partContext, tooltip);
        }
    }

    public static void provideServerData(ServerPlayer player, BlockEntity object, CompoundTag serverData) {
        provideServerData(player, (IPartHost) object, serverData);
    }

    public static void provideServerData(ServerPlayer player, IPartHost object, CompoundTag serverData) {
        var partTag = new CompoundTag();
        for (var location : Platform.DIRECTIONS_WITH_NULL) {
            var part = object.getPart(location);
            if (part == null) {
                continue;
            }

            for (var provider : PartTooltipProviders.getProviders(part).serverDataProviders()) {
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
