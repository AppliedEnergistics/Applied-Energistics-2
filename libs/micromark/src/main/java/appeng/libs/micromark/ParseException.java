package appeng.libs.micromark;

public class ParseException extends RuntimeException {
    private final Point from;
    private final Point to;
    private final String code;

    public ParseException(String message, Point where, String code) {
        super(message);
        this.from = where;
        this.to = where;
        this.code = code;
    }

    public ParseException(String message, Point from, Point to, String code) {
        super(message);
        this.from = from;
        this.to = to;
        this.code = code;
    }

    public Point getFrom() {
        return from;
    }

    public Point getTo() {
        return to;
    }

    public String getCode() {
        return code;
    }
}
