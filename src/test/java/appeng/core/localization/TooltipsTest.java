package appeng.core.localization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.DecimalFormatSymbols;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TooltipsTest {

    /**
     * Expected digits use '.' as the decimal separator, which is replaced by the separator of the current locale, since
     * {@link Tooltips} formats using the default locale.
     */
    @ParameterizedTest
    @CsvSource(value = {
            "0|0|",
            "1|1|",
            "1023|1023|",
            "1024|1|k",
            "1536|1.5|k",
            "10240|10|k",
            "12345|12.06|k",
            "123456|120.6|k",
            "1022976|999|k", // 999 KiB
            "1024000|0.977|M", // 1000 KiB switches to the next unit
            "1047552|0.999|M", // 1023 KiB
            "1048576|1|M",
            "1073741824|1|G",
            "1610612736|1.5|G",
            "1099511627776|1|T",
            "2748779069440|2.5|T",
            "1098412116148224|999|T", // 999 TiB
            "1099511627776000|0.977|P", // 1000 TiB switches to the next unit
            "1125899906842624|1|P",
            "1152921504606846976|1|E",
            "9223372036854775807|8|E", // Long.MAX_VALUE
    }, delimiter = '|')
    void testGetByteAmount(long amount, String expectedDigit, String expectedUnit) {
        var sep = DecimalFormatSymbols.getInstance().getDecimalSeparator();
        var expected = new Tooltips.Amount(
                expectedDigit.replace('.', sep),
                expectedUnit == null ? "" : expectedUnit);

        assertEquals(expected, Tooltips.getByteAmount(amount));
    }
}
