package appeng.libs.mdast.gfm;

import appeng.libs.mdast.MdastContext;
import appeng.libs.mdast.MdastContextProperty;
import appeng.libs.mdast.MdastExtension;
import appeng.libs.mdast.gfm.model.GfmTable;
import appeng.libs.mdast.gfm.model.GfmTableCell;
import appeng.libs.mdast.gfm.model.GfmTableRow;
import appeng.libs.mdast.model.MdAstInlineCode;
import appeng.libs.micromark.Token;
import appeng.libs.micromark.extensions.gfm.GfmTableSyntax;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public final class MdastGfmTable {

    private static final MdastContextProperty<Boolean> IN_TABLE = new MdastContextProperty<>();

    public static final MdastExtension INSTANCE = MdastExtension.builder()
            .enter("table", MdastGfmTable::enterTable)
            .enter("tableData", MdastGfmTable::enterCell)
            .enter("tableHeader", MdastGfmTable::enterCell)
            .enter("tableRow", MdastGfmTable::enterRow)
            .exit("codeText", MdastGfmTable::exitCodeText)
            .exit("table", MdastGfmTable::exitTable)
            .exit("tableData", MdastGfmTable::exit)
            .exit("tableHeader", MdastGfmTable::exit)
            .exit("tableRow", MdastGfmTable::exit)
            .build();

    private MdastGfmTable() {
    }

    private static void enterTable(MdastContext context, Token token) {
        var align = token.get(GfmTableSyntax.ALIGN);

        var table = new GfmTable();
        table.align = align;

        context.enter(table, token);
        context.set(IN_TABLE, true);
    }

    private static void exitTable(MdastContext context, Token token) {
        context.exit(token);
        context.remove(IN_TABLE);
    }

    private static void enterRow(MdastContext context, Token token) {
        context.enter(new GfmTableRow(), token);
    }

    private static void exit(MdastContext context, Token token) {
        context.exit(token);
    }

    private static void enterCell(MdastContext context, Token token) {
        context.enter(new GfmTableCell(), token);
    }

    private static final Pattern ESCAPED_PIPE_PATERN = Pattern.compile("\\\\([\\\\|])");

    // Overwrite the default code text data handler to unescape escaped pipes when
    // they are in tables.
    private static void exitCodeText(MdastContext context, Token token) {
        var value = context.resume();

        if (Boolean.TRUE.equals(context.get(IN_TABLE))) {
            value = ESCAPED_PIPE_PATERN.matcher(value).replaceAll(MdastGfmTable::replace);
        }

        var stack = context.getStack();
        var node = (MdAstInlineCode) stack.get(stack.size() - 1);
        node.value = value;
        context.exit(token);
    }

    private static String replace(MatchResult result) {
        // Pipes work, backslashes don’t (but can’t escape pipes).
        return result.group(1).equals("|") ? "|" : result.group();
    }

}
