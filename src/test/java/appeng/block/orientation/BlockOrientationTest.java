package appeng.block.orientation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import net.minecraft.core.Direction;

class BlockOrientationTest {

    /**
     * Clockwise rotation around the Z- axis when looking at the north-face with UP as the starting point.
     */
    @CsvSource({
            "0,UP",
            "1,WEST",
            "2,DOWN",
            "3,EAST"
    })
    @ParameterizedTest
    public void testNorthDirectionSpin(int spin, Direction rotatedUp) {
        var rotation = BlockOrientation.get(Direction.NORTH, spin);
        assertEquals(rotatedUp, rotation.rotate(Direction.UP));
    }

    /**
     * Clockwise rotation around the Z+ axis when looking at the south-face with UP as the starting point.
     */
    @CsvSource({
            "0,UP",
            "1,WEST",
            "2,DOWN",
            "3,EAST"
    })
    @ParameterizedTest
    public void testSouthDirectionSpin(int spin, Direction rotatedUp) {
        var rotation = BlockOrientation.get(Direction.SOUTH, spin);
        assertEquals(rotatedUp, rotation.rotate(Direction.UP));
    }

    /**
     * Clockwise rotation around the X- axis when looking at the north-face with UP as the starting point.
     */
    @CsvSource({
            "0,UP",
            "1,SOUTH",
            "2,DOWN",
            "3,NORTH"
    })
    @ParameterizedTest
    public void testWestDirectionSpin(int spin, Direction rotatedUp) {
        var rotation = BlockOrientation.get(Direction.WEST, spin);
        assertEquals(rotatedUp, rotation.rotate(Direction.UP));
    }

    /**
     * Clockwise rotation around the X+ axis when looking at the south-face with UP as the starting point.
     */
    @CsvSource({
            "0,UP",
            "1,NORTH",
            "2,DOWN",
            "3,SOUTH"
    })
    @ParameterizedTest
    public void testEastDirectionSpin(int spin, Direction rotatedUp) {
        var rotation = BlockOrientation.get(Direction.EAST, spin);
        assertEquals(rotatedUp, rotation.rotate(Direction.UP));
    }

    /**
     * Clockwise rotation around the Y+ axis when looking at the up-face with north as the starting point.
     */
    @CsvSource({
            "0,NORTH",
            "1,EAST",
            "2,SOUTH",
            "3,WEST"
    })
    @ParameterizedTest
    public void testUpDirectionSpin(int spin, Direction rotatedUp) {
        var rotation = BlockOrientation.get(Direction.UP, spin);
        assertEquals(rotatedUp, rotation.rotate(Direction.UP));
    }

    /**
     * Clockwise rotation around the Y- axis when looking at the down-face with north as the starting point.
     */
    @CsvSource({
            "0,NORTH",
            "1,WEST",
            "2,SOUTH",
            "3,EAST"
    })
    @ParameterizedTest
    public void testDownDirectionSpin(int spin, Direction rotatedUp) {
        var rotation = BlockOrientation.get(Direction.DOWN, spin);
        assertEquals(rotatedUp, rotation.rotate(Direction.UP));
    }

}
