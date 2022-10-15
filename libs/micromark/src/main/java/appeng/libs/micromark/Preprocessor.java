package appeng.libs.micromark;

import appeng.libs.micromark.symbol.Codes;
import appeng.libs.micromark.symbol.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

final class Preprocessor {
    private static final Pattern SEARCH = Pattern.compile("[\0\t\n\r]");

    private int column = 1;
    private String buffer = "";
    private boolean start = true;
    private boolean atCarriageReturn;

    public List<Object> preprocess(String value, boolean end) {
        List<Object> chunks = new ArrayList<>();
        int startPosition;
        int endPosition;
        int code;

        value = buffer + value;
        startPosition = 0;
        buffer = "";

        if (start) {
            if (value.charAt(0) == Codes.byteOrderMarker) {
                startPosition++;
            }

            start = false;
        }

        var matcher = SEARCH.matcher(value);

        while (startPosition < value.length()) {
            var foundMatch = matcher.find(startPosition);

            if (!foundMatch) {
                buffer = value.substring(startPosition);
                break;
            }

            endPosition = matcher.start();
            code = value.charAt(endPosition);

            if (
                    code == Codes.lf &&
                            startPosition == endPosition &&
                            atCarriageReturn
            ) {
                chunks.add(Codes.carriageReturnLineFeed);
                atCarriageReturn = false;
            } else {
                if (atCarriageReturn) {
                    chunks.add(Codes.carriageReturn);
                    atCarriageReturn = false;
                }

                if (startPosition < endPosition) {
                    chunks.add(value.substring(startPosition, endPosition));
                    column += endPosition - startPosition;
                }

                switch (code) {
                    case Codes.nul -> {
                        chunks.add(Codes.replacementCharacter);
                        column++;
                    }
                    case Codes.ht -> {
                        var next = (column + (Constants.tabSize - 1)) / Constants.tabSize * Constants.tabSize;
                        chunks.add(Codes.horizontalTab);
                        while (column++ < next) chunks.add(Codes.virtualSpace);
                    }
                    case Codes.lf -> {
                        chunks.add(Codes.lineFeed);
                        column = 1;
                    }
                    default -> {
                        atCarriageReturn = true;
                        column = 1;
                    }
                }
            }

            startPosition = endPosition + 1;
        }

        if (end) {
            if (atCarriageReturn)
                chunks.add(Codes.carriageReturn);
            if (!buffer.isEmpty())
                chunks.add(buffer);
            chunks.add(Codes.eof);
        }

        return chunks;
    }
}
