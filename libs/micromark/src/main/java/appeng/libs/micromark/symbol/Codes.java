package appeng.libs.micromark.symbol;

/**
 * Character codes.
 * <p>
 * This module is compiled away!
 * <p>
 * micromark works based on character codes.
 * This module contains constants for the ASCII block and the replacement
 * character.
 * A couple of them are handled in a special way, such as the line endings
 * (CR, LF, and CR+LF, commonly known as end-of-line: EOLs), the tab (horizontal
 * tab) and its expansion based on what column it’s at (virtual space),
 * and the end-of-file (eof) character.
 * As values are preprocessed before handling them, the actual characters LF,
 * CR, HT, and NUL (which is present as the replacement character), are
 * guaranteed to not exist.
 * <p>
 * Unicode basic latin block.
 */
public final class Codes {
    private Codes() {
    }

    public static final int carriageReturn = -5;
    public static final int lineFeed = -4;
    public static final int carriageReturnLineFeed = -3;
    public static final int horizontalTab = -2;
    public static final int virtualSpace = -1;
    public static final int eof = Integer.MIN_VALUE;
    public static final int nul = 0;
    public static final int soh = 1;
    public static final int stx = 2;
    public static final int etx = 3;
    public static final int eot = 4;
    public static final int enq = 5;
    public static final int ack = 6;
    public static final int bel = 7;
    public static final int bs = 8;
    public static final int ht = 9; // `\t`
    public static final int lf = 10; // `\n`
    public static final int vt = 11; // `\v`
    public static final int ff = 12; // `\f`
    public static final int cr = 13; // `\r`
    public static final int so = 14;
    public static final int si = 15;
    public static final int dle = 16;
    public static final int dc1 = 17;
    public static final int dc2 = 18;
    public static final int dc3 = 19;
    public static final int dc4 = 20;
    public static final int nak = 21;
    public static final int syn = 22;
    public static final int etb = 23;
    public static final int can = 24;
    public static final int em = 25;
    public static final int sub = 26;
    public static final int esc = 27;
    public static final int fs = 28;
    public static final int gs = 29;
    public static final int rs = 30;
    public static final int us = 31;
    public static final int space = 32;
    public static final int exclamationMark = 33; // `!`
    public static final int quotationMark = 34; // `"`
    public static final int numberSign = 35; // `#`
    public static final int dollarSign = 36; // `$`
    public static final int percentSign = 37; // `%`
    public static final int ampersand = 38; // `&`
    public static final int apostrophe = 39; // `'`
    public static final int leftParenthesis = 40; // `(`
    public static final int rightParenthesis = 41; // `)`
    public static final int asterisk = 42; // `*`
    public static final int plusSign = 43; // `+`
    public static final int comma = 44; // `,`
    public static final int dash = 45; // `-`
    public static final int dot = 46; // `.`
    public static final int slash = 47; // `/`
    public static final int digit0 = 48; // `0`
    public static final int digit1 = 49; // `1`
    public static final int digit2 = 50; // `2`
    public static final int digit3 = 51; // `3`
    public static final int digit4 = 52; // `4`
    public static final int digit5 = 53; // `5`
    public static final int digit6 = 54; // `6`
    public static final int digit7 = 55; // `7`
    public static final int digit8 = 56; // `8`
    public static final int digit9 = 57; // `9`
    public static final int colon = 58; // `:`
    public static final int semicolon = 59; // `;`
    public static final int lessThan = 60; // `<`
    public static final int equalsTo = 61; // `=`
    public static final int greaterThan = 62; // `>`
    public static final int questionMark = 63; // `?`
    public static final int atSign = 64; // `@`
    public static final int uppercaseA = 65; // `A`
    public static final int uppercaseB = 66; // `B`
    public static final int uppercaseC = 67; // `C`
    public static final int uppercaseD = 68; // `D`
    public static final int uppercaseE = 69; // `E`
    public static final int uppercaseF = 70; // `F`
    public static final int uppercaseG = 71; // `G`
    public static final int uppercaseH = 72; // `H`
    public static final int uppercaseI = 73; // `I`
    public static final int uppercaseJ = 74; // `J`
    public static final int uppercaseK = 75; // `K`
    public static final int uppercaseL = 76; // `L`
    public static final int uppercaseM = 77; // `M`
    public static final int uppercaseN = 78; // `N`
    public static final int uppercaseO = 79; // `O`
    public static final int uppercaseP = 80; // `P`
    public static final int uppercaseQ = 81; // `Q`
    public static final int uppercaseR = 82; // `R`
    public static final int uppercaseS = 83; // `S`
    public static final int uppercaseT = 84; // `T`
    public static final int uppercaseU = 85; // `U`
    public static final int uppercaseV = 86; // `V`
    public static final int uppercaseW = 87; // `W`
    public static final int uppercaseX = 88; // `X`
    public static final int uppercaseY = 89; // `Y`
    public static final int uppercaseZ = 90; // `Z`
    public static final int leftSquareBracket = 91; // `[`
    public static final int backslash = 92; // `\`
    public static final int rightSquareBracket = 93; // `]`
    public static final int caret = 94; // `^`
    public static final int underscore = 95; // `_`
    public static final int graveAccent = 96; // `` ` ``
    public static final int lowercaseA = 97; // `a`
    public static final int lowercaseB = 98; // `b`
    public static final int lowercaseC = 99; // `c`
    public static final int lowercaseD = 100; // `d`
    public static final int lowercaseE = 101; // `e`
    public static final int lowercaseF = 102; // `f`
    public static final int lowercaseG = 103; // `g`
    public static final int lowercaseH = 104; // `h`
    public static final int lowercaseI = 105; // `i`
    public static final int lowercaseJ = 106; // `j`
    public static final int lowercaseK = 107; // `k`
    public static final int lowercaseL = 108; // `l`
    public static final int lowercaseM = 109; // `m`
    public static final int lowercaseN = 110; // `n`
    public static final int lowercaseO = 111; // `o`
    public static final int lowercaseP = 112; // `p`
    public static final int lowercaseQ = 113; // `q`
    public static final int lowercaseR = 114; // `r`
    public static final int lowercaseS = 115; // `s`
    public static final int lowercaseT = 116; // `t`
    public static final int lowercaseU = 117; // `u`
    public static final int lowercaseV = 118; // `v`
    public static final int lowercaseW = 119; // `w`
    public static final int lowercaseX = 120; // `x`
    public static final int lowercaseY = 121; // `y`
    public static final int lowercaseZ = 122; // `z`
    public static final int leftCurlyBrace = 123; // `{`
    public static final int verticalBar = 124; // `|`
    public static final int rightCurlyBrace = 125; // `}`
    public static final int tilde = 126; // `~`
    public static final int del = 127;
    // Unicode Specials block.
    public static final int byteOrderMarker = 65279;
    // Unicode Specials block.
    public static final int replacementCharacter = 65533; // `�`
}
