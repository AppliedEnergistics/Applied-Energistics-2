package appeng.api.integrations.igtooltip;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

@ApiStatus.Experimental
public record TooltipContext(CompoundTag serverData, Vec3 hitLocation, Player player) {
}
