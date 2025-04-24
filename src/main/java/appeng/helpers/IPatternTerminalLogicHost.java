package appeng.helpers;

import net.minecraft.world.level.Level;

import appeng.parts.encoding.PatternEncodingLogic;

public interface IPatternTerminalLogicHost {
    PatternEncodingLogic getLogic();

    Level getLevel();

    void markForSave();
}
