package appeng.libs.micromark.gfm.tables;

import appeng.libs.micromark.Token;
import appeng.libs.micromark.html.CompileContext;
import appeng.libs.micromark.html.HtmlContextProperty;
import appeng.libs.micromark.html.HtmlExtension;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GfmTableHtml {

    private static final HtmlContextProperty<List<Align>> TABLE_ALIGN = new HtmlContextProperty<>();
    private static final HtmlContextProperty<Integer> TABLE_COLUMN = new HtmlContextProperty<>();

    public static final HtmlExtension EXTENSION = HtmlExtension.builder()
            .enter("table", GfmTableHtml::enterTable)
            .enter("tableBody", GfmTableHtml::enterTableBody)
            .enter("tableData", GfmTableHtml::enterTableData)
            .enter("tableHead", GfmTableHtml::enterTableHead)
            .enter("tableHeader", GfmTableHtml::enterTableHeader)
            .enter("tableRow", GfmTableHtml::enterTableRow)
            // Overwrite the default code text data handler to unescape escaped pipes when
            // they are in tables.
            .exit("codeTextData", GfmTableHtml::exitCodeTextData)
            .exit("table", GfmTableHtml::exitTable)
            .exit("tableBody", GfmTableHtml::exitTableBody)
            .exit("tableData", GfmTableHtml::exitTableData)
            .exit("tableHead", GfmTableHtml::exitTableHead)
            .exit("tableHeader", GfmTableHtml::exitTableHeader)
            .exit("tableRow", GfmTableHtml::exitTableRow)
            .build();

    private GfmTableHtml() {
    }

    private static void enterTable(CompileContext context, Token token) {
        var tableAlign = token.get(GfmTable.ALIGN);
        context.lineEndingIfNeeded();
        context.tag("<table>");
        context.set(TABLE_ALIGN, tableAlign);
    }

    private static void enterTableBody(CompileContext context, Token token) {
        // Clear slurping line ending from the delimiter row.
        context.setSlurpOneLineEnding(false);
        context.tag("<tbody>");
    }

    private static void enterTableData(CompileContext context, Token token) {
        var tableAlign = context.get(TABLE_ALIGN);
        var tableColumn = context.get(TABLE_COLUMN);

        if (tableColumn >= tableAlign.size()) {
            // Capture results to ignore them.
            context.buffer();
        } else {
            context.lineEndingIfNeeded();
            var align = getAlignmentAttr(tableAlign.get(tableColumn));
            context.tag("<td" + align + '>');
        }
    }

    private static void enterTableHead(CompileContext context, Token token) {
        context.lineEndingIfNeeded();
        context.tag("<thead>");
    }

    private static void enterTableHeader(CompileContext context, Token token) {
        var tableAlign = context.get(TABLE_ALIGN);
        var tableColumn = context.get(TABLE_COLUMN);
        var align = getAlignmentAttr(tableAlign.get(tableColumn));

        context.lineEndingIfNeeded();
        context.tag("<th" + align + '>');
    }

    private static void enterTableRow(CompileContext context, Token token) {
        context.set(TABLE_COLUMN, 0);
        context.lineEndingIfNeeded();
        context.tag("<tr>");
    }

    private static final Pattern PIPE_PATTERN = Pattern.compile("\\\\([\\\\|])");

    // Overwrite the default code text data handler to unescape escaped pipes when
    // they are in tables.
    private static void exitCodeTextData(CompileContext context, Token token) {
        var value = context.sliceSerialize(token);

        if (context.has(TABLE_ALIGN)) {
            value = PIPE_PATTERN.matcher(value).replaceAll(mr -> {
                // Pipes work, backslashes don’t (but can’t escape pipes).
                var g = mr.group(1);
                return Matcher.quoteReplacement(g.equals("|") ? g : mr.group());
            });
        }

        context.raw(context.encode(value));
    }

    private static void exitTable(CompileContext context, Token token) {
        context.remove(TABLE_ALIGN);
        // If there was no table body, make sure the slurping from the delimiter row
        // is cleared.
        context.setSlurpAllLineEndings(false);
        context.lineEndingIfNeeded();
        context.tag("</table>");
    }

    private static void exitTableBody(CompileContext context, Token token) {
        context.lineEndingIfNeeded();
        context.tag("</tbody>");
    }

    private static void exitTableData(CompileContext context, Token token) {
        var tableAlign = context.get(TABLE_ALIGN);
        var tableColumn = context.get(TABLE_COLUMN);

        if (tableColumn < tableAlign.size()) {
            context.tag("</td>");
            context.set(TABLE_COLUMN, tableColumn + 1);
        } else {
            // Stop capturing.
            context.resume();
        }
    }

    private static void exitTableHead(CompileContext context, Token token) {
        context.lineEndingIfNeeded();
        context.tag("</thead>");
        context.setSlurpOneLineEnding(true);
        // Slurp the line ending from the delimiter row.
    }

    private static void exitTableHeader(CompileContext context, Token token) {
        var tableColumn = context.get(TABLE_COLUMN);
        context.tag("</th>");
        context.set(TABLE_COLUMN, tableColumn + 1);
    }

    private static void exitTableRow(CompileContext context, Token token) {
        var tableAlign = context.get(TABLE_ALIGN);
        var tableColumn = context.get(TABLE_COLUMN);

        while (tableColumn < tableAlign.size()) {
            context.lineEndingIfNeeded();
            context.tag("<td" + getAlignmentAttr(tableAlign.get(tableColumn)) + "></td>");
            tableColumn++;
        }

        context.set(TABLE_COLUMN, tableColumn);
        context.lineEndingIfNeeded();
        context.tag("</tr>");
    }


    private static String getAlignmentAttr(Align align) {
        return switch (align) {
            case NONE -> "";
            case LEFT -> " align=\"left\"";
            case RIGHT -> " align=\"right\"";
            case CENTER -> " align=\"center\"";
        };
    }

}
