package appeng.helpers;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import appeng.parts.encoding.PatternEncodingLogic;
import org.jetbrains.annotations.Nullable;

public interface IPatternTerminalLogicHost {
    PatternEncodingLogic getLogic();

    @Nullable
    ServerLevel getServerLevel();

    void markForSave();
}
