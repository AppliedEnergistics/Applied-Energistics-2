package appeng.api.features;

import net.minecraftforge.common.ToolAction;

/**
 * Tool actions defined by AE.
 */
public final class AEToolActions {
    private AEToolActions() {
    }

    /**
     * An action that is triggered by right-clicking a supported block or part, which will disassemble that block or
     * part into its item form.
     */
    public static final ToolAction WRENCH_DISASSEMBLE = ToolAction.get("wrench_disassemble");

    /**
     * An action that is triggered by shift-right-clicking a supported block or part, which will rotate that part.
     */
    public static final ToolAction WRENCH_ROTATE = ToolAction.get("wrench_rotate");
}
