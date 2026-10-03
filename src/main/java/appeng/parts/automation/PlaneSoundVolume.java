package appeng.parts.automation;

/**
 * Shared volume-scaling algebra for the Formation Plane placement-sound mixin and the Annihilation Plane pickup-sound
 * packet, applying the {@code planeVolumeMultiplier} config option.
 */
public final class PlaneSoundVolume {

    private PlaneSoundVolume() {
    }

    // Returns x such that (x + 1.0F) / 2.0F == multiplier * (originalVolume + 1.0F) / 2.0F, matching the
    // (volume + 1.0F) / 2.0F broadcast-volume formula both call sites feed the result into.
    public static float scale(float originalVolume, float multiplier) {
        return multiplier * (originalVolume + 1.0F) - 1.0F;
    }

}
