package appeng.libs.micromark;

import appeng.libs.mdast.model.MdAstPosition;
import appeng.libs.unist.UnistPoint;

public class ParseException extends RuntimeException {
    private final UnistPoint from;
    private final UnistPoint to;
    private final String code;

    public ParseException(String message, UnistPoint where, String code) {
        super(message);
        this.from = where;
        this.to = where;
        this.code = code;
    }

    public ParseException(String message, MdAstPosition position, String code) {
        this(message, position != null ? position.start() : null, position != null ? position.end() : null, code);
    }

    public ParseException(String message, UnistPoint from, UnistPoint to, String code) {
        super(message);
        this.from = from;
        this.to = to;
        this.code = code;
    }

    public UnistPoint getFrom() {
        return from;
    }

    public UnistPoint getTo() {
        return to;
    }

    public String getCode() {
        return code;
    }
}
