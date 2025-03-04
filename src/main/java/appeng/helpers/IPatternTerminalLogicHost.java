package appeng.helpers;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.level.ServerLevel;

import appeng.parts.encoding.PatternEncodingLogic;

public interface IPatternTerminalLogicHost {
    PatternEncodingLogic getLogic();

    @Nullable
    ServerLevel getServerLevel();

    void markForSave();
}
