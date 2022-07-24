package appeng.api.integrations.igtooltip;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public record InGameTooltipContext(CompoundTag serverData, Vec3 hitLocation, Player player) {
}
