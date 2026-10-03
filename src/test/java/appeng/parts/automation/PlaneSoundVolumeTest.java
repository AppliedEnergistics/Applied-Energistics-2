package appeng.parts.automation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PlaneSoundVolumeTest {

    @ParameterizedTest
    @CsvSource({
            "1.0, 1.0, 1.0",
            "1.0, 0.0, -1.0",
            "1.0, 0.5, 0.0",
            "0.0, 1.0, 0.0",
            "0.0, 0.5, -0.5",
            "0.0, 0.0, -1.0",
    })
    void scaleReturnsExpectedRedirectValue(float originalVolume, float multiplier, float expected) {
        assertThat(PlaneSoundVolume.scale(originalVolume, multiplier)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "1.0, 1.0",
            "1.0, 0.5",
            "1.0, 0.0",
            "0.6, 0.75",
            "0.0, 0.25",
    })
    void finalBroadcastVolumeIsMultiplierTimesOriginal(float originalVolume, float multiplier) {
        var scaled = PlaneSoundVolume.scale(originalVolume, multiplier);

        var actualFinalVolume = (scaled + 1.0F) / 2.0F;
        var expectedFinalVolume = multiplier * (originalVolume + 1.0F) / 2.0F;

        assertThat(actualFinalVolume).isCloseTo(expectedFinalVolume, within(1e-6F));
    }
}
