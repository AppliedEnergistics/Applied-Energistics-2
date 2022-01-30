package appeng.helpers;

import appeng.api.storage.ITerminalHost;
import appeng.parts.encoding.PatternEncodingLogic;

/**
 * Interface implemented by an object that can serve as the host for
 * {@link appeng.menu.me.items.PatternEncodingTermMenu}
 */
public interface IPatternTerminalMenuHost extends ITerminalHost {
    PatternEncodingLogic getLogic();
}
