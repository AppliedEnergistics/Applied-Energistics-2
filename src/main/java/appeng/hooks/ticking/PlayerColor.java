package appeng.hooks.ticking;

import appeng.api.util.AEColor;
import appeng.core.sync.packets.PaintedEntityPacket;

/**
 * Handles how long the color overlay for a player is valid
 */
public class PlayerColor {

    private final AEColor myColor;
    private final int myEntity;
    private int ticksLeft;

    public PlayerColor(final int id, final AEColor col, final int ticks) {
        this.myEntity = id;
        this.myColor = col;
        this.ticksLeft = ticks;
    }

    public PaintedEntityPacket getPacket() {
        return new PaintedEntityPacket(this.myEntity, this.myColor, this.ticksLeft);
    }

    public AEColor getColor() {
        return myColor;
    }

    /**
     * Tick this player color once.
     */
    void tick() {
        this.ticksLeft--;
    }

    /**
     * Indicates that this color is done and can be removed.
     *
     * @return true once done.
     */
    boolean isDone() {
        return this.ticksLeft <= 0;
    }

}
