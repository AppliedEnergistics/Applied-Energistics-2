/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

class EnumCyclerTest {

    @Test
    void testRotateEnumForwardWithOnlySomeValidOptions() {
        EnumSet<RotateTestEnum> validOptions = EnumSet.of(RotateTestEnum.A, RotateTestEnum.C, RotateTestEnum.E);
        assertThat(EnumCycler.rotateEnum(RotateTestEnum.A, false, validOptions)).isEqualTo(RotateTestEnum.C);
        assertThat(EnumCycler.rotateEnum(RotateTestEnum.C, false, validOptions)).isEqualTo(RotateTestEnum.E);
        assertThat(EnumCycler.rotateEnum(RotateTestEnum.E, false, validOptions)).isEqualTo(RotateTestEnum.A);
    }

    @Test
    void testRotateEnumBackwardsWithOnlySomeValidOptions() {
        EnumSet<RotateTestEnum> validOptions = EnumSet.of(RotateTestEnum.A, RotateTestEnum.C, RotateTestEnum.E);
        assertThat(EnumCycler.rotateEnum(RotateTestEnum.A, true, validOptions)).isEqualTo(RotateTestEnum.E);
        assertThat(EnumCycler.rotateEnum(RotateTestEnum.C, true, validOptions)).isEqualTo(RotateTestEnum.A);
        assertThat(EnumCycler.rotateEnum(RotateTestEnum.E, true, validOptions)).isEqualTo(RotateTestEnum.C);
    }

    /**
     * When there are no valid options, the function should reject the arguments.
     */
    @Test
    void testRotateEnumNoValidOptions() {
        assertThrows(IllegalArgumentException.class, () -> {
            EnumCycler.rotateEnum(TestEnum.A, false, EnumSet.noneOf(TestEnum.class));
        });
    }

    /**
     * When the current enum literal is not part of the valid options, it should just skip to the next valid option
     * instead.
     */
    @Test
    void testRotateEnumCurrentIsNotAValidOption() {
        assertThat(EnumCycler.rotateEnum(TestEnum.B, false, EnumSet.of(TestEnum.A))).isEqualTo(TestEnum.A);
        assertThat(EnumCycler.rotateEnum(TestEnum.B, true, EnumSet.of(TestEnum.A))).isEqualTo(TestEnum.A);
    }

    /**
     * When there's only one valid option, it should just rotate back to it.
     */
    @Test
    void testRotateEnumOnlyOneValidOption() {
        assertThat(EnumCycler.rotateEnum(TestEnum.A, false, EnumSet.of(TestEnum.A))).isEqualTo(TestEnum.A);
        assertThat(EnumCycler.rotateEnum(TestEnum.A, true, EnumSet.of(TestEnum.A))).isEqualTo(TestEnum.A);
    }

    @Test
    void testNext() {
        assertThat(EnumCycler.next(TestEnum.A)).isEqualTo(TestEnum.B);
        assertThat(EnumCycler.next(TestEnum.B)).isEqualTo(TestEnum.C);
        assertThat(EnumCycler.next(TestEnum.C)).isEqualTo(TestEnum.A);
        assertThat(EnumCycler.next(SingleLiteralEnum.A)).isEqualTo(SingleLiteralEnum.A);
    }

    @Test
    void testPrev() {
        assertThat(EnumCycler.prev(TestEnum.A)).isEqualTo(TestEnum.C);
        assertThat(EnumCycler.prev(TestEnum.B)).isEqualTo(TestEnum.A);
        assertThat(EnumCycler.prev(TestEnum.C)).isEqualTo(TestEnum.B);
        assertThat(EnumCycler.prev(SingleLiteralEnum.A)).isEqualTo(SingleLiteralEnum.A);
    }

    enum TestEnum {
        A,
        B,
        C
    }

    enum RotateTestEnum {
        A,
        B,
        C,
        D,
        E
    }

    enum SingleLiteralEnum {
        A
    }

}