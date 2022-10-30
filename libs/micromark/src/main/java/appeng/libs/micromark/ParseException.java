package appeng.libs.micromark;

public class ParseException extends RuntimeException {
    private final Point where;
    private final String code;

    public ParseException(String message, Point where, String code) {
        super(message);
        this.where = where;
        this.code = code;
    }

    public Point getWhere() {
        return where;
    }

    public String getCode() {
        return code;
    }
}
