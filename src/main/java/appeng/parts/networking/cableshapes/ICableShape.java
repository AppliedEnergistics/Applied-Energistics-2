package appeng.parts.networking.cableshapes;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.util.AECableType;

public interface ICableShape {

    AECableType getCableConnectionType();

    void addCableBoxes(
            Set<Direction> connections,
            @Nullable IPartHost host,
            @Nullable Level level,
            BlockPos pos,
            IPartCollisionHelper bch);

}
