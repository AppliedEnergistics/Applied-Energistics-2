package appeng.libs.micromark.extensions.gfm;

import appeng.libs.micromark.Assert;
import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.Construct;
import appeng.libs.micromark.ContentType;
import appeng.libs.micromark.Extension;
import appeng.libs.micromark.ListUtils;
import appeng.libs.micromark.State;
import appeng.libs.micromark.Token;
import appeng.libs.micromark.TokenProperty;
import appeng.libs.micromark.TokenizeContext;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.factory.FactorySpace;
import appeng.libs.micromark.symbol.Codes;
import appeng.libs.micromark.symbol.Constants;

import java.util.ArrayList;
import java.util.List;

public class GfmTableSyntax extends Extension {
    public static final TokenProperty<List<Align>> ALIGN = new TokenProperty<>();

    public static final Extension INSTANCE = new GfmTableSyntax();

    private final Construct nextPrefixedOrBlank;


    public GfmTableSyntax() {
        var construct = new Construct();
        construct.tokenize = this::tokenizeTable;
        construct.resolve = this::resolveTable;
        flow.put(Codes.eof, List.of(construct));

        this.nextPrefixedOrBlank = new Construct();
        this.nextPrefixedOrBlank.tokenize = this::tokenizeNextPrefixedOrBlank;
        this.nextPrefixedOrBlank.partial = true;
    }

    private List<Tokenizer.Event> resolveTable(List<Tokenizer.Event> events, TokenizeContext context) {
        int index = -1;
        boolean inHead = false;
        boolean inDelimiterRow = false;
        boolean inRow = false;
        int contentStart = -1;
        int contentEnd = -1;
        int cellStart = -1;
        boolean seenCellInRow = false;

        while (++index < events.size()) {
             var event = events.get(index);
             var token = event.token();

            if (inRow) {
                if (token.type.equals("temporaryTableCellContent")) {
                    if (contentStart == -1) {
                        contentStart = index;
                    }
                    contentEnd = index;
                }

                if (
                    // Combine separate content parts into one.
                        (token.type.equals("tableCellDivider") || token.type.equals("tableRow")) &&
                                contentEnd != -1
                ) {
                    Assert.check(
                            contentStart != -1,
                    "expected `contentStart` to be defined if `contentEnd` is"
        );
        var content = new Token();
        content.type =  "tableContent";
        content.start = events.get(contentStart).token().start;
        content.end = events.get(contentEnd).token().end;
        
        var text = new Token();
        text.type = Types.chunkText;
        text.start = content.start;
        text.end = content.end;
        text.contentType = ContentType.TEXT;

                    Assert.check(
                            contentStart != -1,
                            "expected `contentStart` to be defined if `contentEnd` is"
                    );

                    ListUtils.splice(events,
                            contentStart,
                            contentEnd - contentStart + 1,
                            List.of(
                                    Tokenizer.Event.enter(content, context),
                                    Tokenizer.Event.enter(text, context),
                                    Tokenizer.Event.exit(text, context),
                                    Tokenizer.Event.exit(content, context)
                            )
                    );

                    index -= contentEnd - contentStart - 3;
                    contentStart = -1;
                    contentEnd = -1;
                }
            }

            if (
                    events.get(index).isExit() &&
                            cellStart != -1 &&
                            cellStart + (seenCellInRow ? 0 : 1) < index &&
                            (token.type.equals("tableCellDivider") ||
                                    (token.type.equals("tableRow") &&
                                            (cellStart + 3 < index ||
                                                    !events.get(cellStart).token().type.equals(Types.whitespace))))
            ) {

                var cell = new Token();
                cell.type = inDelimiterRow ? "tableDelimiter" : inHead ? "tableHeader" : "tableData";
                cell.start = events.get(cellStart).token().start;
                cell.end = events.get(index).token().end;

                ListUtils.splice(
                    events,
                    index + (token.type.equals("tableCellDivider") ? 1 : 0),
                    0,
                    List.of(Tokenizer.Event.exit(cell, context))
                );
                ListUtils.splice(
                        events,
                        cellStart,
                        0,
                        List.of(Tokenizer.Event.enter(cell, context))
                );
                index += 2;
                cellStart = index + 1;
                seenCellInRow = true;
            }

            if (token.type.equals("tableRow")) {
                inRow = events.get(index).isEnter();

                if (inRow) {
                    cellStart = index + 1;
                    seenCellInRow = false;
                }
            }

            if (token.type.equals("tableDelimiterRow")) {
                inDelimiterRow = events.get(index).isEnter();

                if (inDelimiterRow) {
                    cellStart = index + 1;
                    seenCellInRow = false;
                }
            }

            if (token.type.equals("tableHead")) {
                inHead = events.get(index).isEnter();
            }
        }

        return events;
    }

    private State tokenizeTable(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {
        class StateMachine {
            final List<Align> align = new ArrayList<>();
            int tableHeaderCount = 0;
            boolean seenDelimiter;
            boolean hasDash;

            State start(int code) {
                // @ts-expect-error Custom.
                effects.enter("table").set(ALIGN, align);
                effects.enter("tableHead");
                effects.enter("tableRow");

                // If we start with a pipe, we open a cell marker.
                if (code == Codes.verticalBar) {
                    return cellDividerHead(code);
                }

                tableHeaderCount++;
                effects.enter("temporaryTableCellContent");
                // Can’t be space or eols at the start of a construct, so we’re in a cell.
                Assert.check(!CharUtil.markdownLineEndingOrSpace(code),"expected non-space");
                return inCellContentHead(code);
            }

            State cellDividerHead(int code) {
                Assert.check(code == Codes.verticalBar,"expected `|`");
                effects.enter("tableCellDivider");
                effects.consume(code);
                effects.exit("tableCellDivider");
                seenDelimiter = true;
                return this::cellBreakHead;
            }

            State cellBreakHead(int code) {
                if (code == Codes.eof || CharUtil.markdownLineEnding(code)) {
                    return atRowEndHead(code);
                }

                if (CharUtil.markdownSpace(code)) {
                    effects.enter(Types.whitespace);
                    effects.consume(code);
                    return this::inWhitespaceHead;
                }

                if (seenDelimiter) {
                    seenDelimiter = false;
                    tableHeaderCount++;
                }

                if (code == Codes.verticalBar) {
                    return cellDividerHead(code);
                }

                // Anything else is cell content.
                effects.enter("temporaryTableCellContent");
                return inCellContentHead(code);
            }

            State inWhitespaceHead(int code) {
                if (CharUtil.markdownSpace(code)) {
                    effects.consume(code);
                    return this::inWhitespaceHead;
                }

                effects.exit(Types.whitespace);
                return cellBreakHead(code);
            }

            State inCellContentHead(int code) {
                // EOF, whitespace, pipe
                if (
                        code == Codes.eof ||
                                code == Codes.verticalBar ||
                                CharUtil.markdownLineEndingOrSpace(code)
                ) {
                    effects.exit("temporaryTableCellContent");
                    return cellBreakHead(code);
                }

                effects.consume(code);
                return code == Codes.backslash
                        ? this::inCellContentEscapeHead
                        : this::inCellContentHead;
            }

            State inCellContentEscapeHead(int code) {
                if (code == Codes.backslash || code == Codes.verticalBar) {
                    effects.consume(code);
                    return this::inCellContentHead;
                }

                // Anything else.
                return inCellContentHead(code);
            }

            State atRowEndHead(int code) {
                if (code == Codes.eof) {
                    return nok.step(code);
                }

                Assert.check(CharUtil.markdownLineEnding(code),"expected eol");
                effects.exit("tableRow");
                effects.exit("tableHead");
                var originalInterrupt = context.isInterrupt();
                context.setInterrupt(true);

                var construct = new Construct();
                construct.tokenize = this::tokenizeRowEnd;
                construct.partial = true;

                return effects.attempt.hook(
                        construct,
                c -> {
                    context.setInterrupt(originalInterrupt);
                    effects.enter("tableDelimiterRow");
                    return atDelimiterRowBreak(c);
                },
                c -> {
                    context.setInterrupt(originalInterrupt);
                    return nok.step(c);
                }
                ).step(code);
            }

            State atDelimiterRowBreak(int code) {
                if (code == Codes.eof || CharUtil.markdownLineEnding(code)) {
                    return rowEndDelimiter(code);
                }

                if (CharUtil.markdownSpace(code)) {
                    effects.enter(Types.whitespace);
                    effects.consume(code);
                    return this::inWhitespaceDelimiter;
                }

                if (code == Codes.dash) {
                    effects.enter("tableDelimiterFiller");
                    effects.consume(code);
                    hasDash = true;
                    align.add(Align.NONE);
                    return this::inFillerDelimiter;
                }

                if (code == Codes.colon) {
                    effects.enter("tableDelimiterAlignment");
                    effects.consume(code);
                    effects.exit("tableDelimiterAlignment");
                    align.add(Align.LEFT);
                    return this::afterLeftAlignment;
                }

                // If we start with a pipe, we open a cell marker.
                if (code == Codes.verticalBar) {
                    effects.enter("tableCellDivider");
                    effects.consume(code);
                    effects.exit("tableCellDivider");
                    return this::atDelimiterRowBreak;
                }

                return nok.step(code);
            }

            State inWhitespaceDelimiter(int code) {
                if (CharUtil.markdownSpace(code)) {
                    effects.consume(code);
                    return this::inWhitespaceDelimiter;
                }

                effects.exit(Types.whitespace);
                return atDelimiterRowBreak(code);
            }

            State inFillerDelimiter(int code) {
                if (code == Codes.dash) {
                    effects.consume(code);
                    return this::inFillerDelimiter;
                }

                effects.exit("tableDelimiterFiller");

                if (code == Codes.colon) {
                    effects.enter("tableDelimiterAlignment");
                    effects.consume(code);
                    effects.exit("tableDelimiterAlignment");


                    align.set(align.size() - 1,
                            align.get(align.size() - 1) == Align.LEFT ? Align.CENTER : Align.RIGHT);

                    return this::afterRightAlignment;
                }

                return atDelimiterRowBreak(code);
            }

            State afterLeftAlignment(int code) {
                if (code == Codes.dash) {
                    effects.enter("tableDelimiterFiller");
                    effects.consume(code);
                    hasDash = true;
                    return this::inFillerDelimiter;
                }

                // Anything else is not ok.
                return nok.step(code);
            }

            State afterRightAlignment(int code) {
                if (code == Codes.eof || CharUtil.markdownLineEnding(code)) {
                    return rowEndDelimiter(code);
                }

                if (CharUtil.markdownSpace(code)) {
                    effects.enter(Types.whitespace);
                    effects.consume(code);
                    return this::inWhitespaceDelimiter;
                }

                // `|`
                if (code == Codes.verticalBar) {
                    effects.enter("tableCellDivider");
                    effects.consume(code);
                    effects.exit("tableCellDivider");
                    return this::atDelimiterRowBreak;
                }

                return nok.step(code);
            }

            State rowEndDelimiter(int code) {
                effects.exit("tableDelimiterRow");

                // Exit if there was no dash at all, or if the header cell count is not the
                // delimiter cell count.
                if (!hasDash || tableHeaderCount != align.size()) {
                    return nok.step(code);
                }

                if (code == Codes.eof) {
                    return tableClose(code);
                }

                Assert.check(CharUtil.markdownLineEnding(code),"expected eol");

                var construct = new Construct();
                construct.tokenize = this::tokenizeRowEnd;
                construct.partial = true;

                return effects.check.hook(
                        nextPrefixedOrBlank,
                        this::tableClose,
                        effects.attempt.hook(
                                construct,
                                FactorySpace.create(effects, this::bodyStart, Types.linePrefix, Constants.tabSize),
                                this::tableClose
                        )
                ).step(code);
            }

            State tableClose(int code) {
                effects.exit("table");
                return ok.step(code);
            }

            State bodyStart(int code) {
                effects.enter("tableBody");
                return rowStartBody(code);
            }

            State rowStartBody(int code) {
                effects.enter("tableRow");

                // If we start with a pipe, we open a cell marker.
                if (code == Codes.verticalBar) {
                    return cellDividerBody(code);
                }

                effects.enter("temporaryTableCellContent");
                // Can’t be space or eols at the start of a construct, so we’re in a cell.
                return inCellContentBody(code);
            }

            State cellDividerBody(int code) {
                Assert.check(code == Codes.verticalBar,"expected `|`");
                effects.enter("tableCellDivider");
                effects.consume(code);
                effects.exit("tableCellDivider");
                return this::cellBreakBody;
            }

            State cellBreakBody(int code) {
                if (code == Codes.eof || CharUtil.markdownLineEnding(code)) {
                    return atRowEndBody(code);
                }

                if (CharUtil.markdownSpace(code)) {
                    effects.enter(Types.whitespace);
                    effects.consume(code);
                    return this::inWhitespaceBody;
                }

                // `|`
                if (code == Codes.verticalBar) {
                    return cellDividerBody(code);
                }

                // Anything else is cell content.
                effects.enter("temporaryTableCellContent");
                return inCellContentBody(code);
            }

            State inWhitespaceBody(int code) {
                if (CharUtil.markdownSpace(code)) {
                    effects.consume(code);
                    return this::inWhitespaceBody;
                }

                effects.exit(Types.whitespace);
                return cellBreakBody(code);
            }

            State inCellContentBody(int code) {
                // EOF, whitespace, pipe
                if (
                        code == Codes.eof ||
                                code == Codes.verticalBar ||
                                CharUtil.markdownLineEndingOrSpace(code)
                ) {
                    effects.exit("temporaryTableCellContent");
                    return cellBreakBody(code);
                }

                effects.consume(code);
                return code == Codes.backslash
                        ? this::inCellContentEscapeBody
                        : this::inCellContentBody;
            }

            State inCellContentEscapeBody(int code) {
                if (code == Codes.backslash || code == Codes.verticalBar) {
                    effects.consume(code);
                    return this::inCellContentBody;
                }

                // Anything else.
                return inCellContentBody(code);
            }

            State atRowEndBody(int code) {
                effects.exit("tableRow");

                if (code == Codes.eof) {
                    return tableBodyClose(code);
                }

                var construct = new Construct();
                construct.tokenize = this::tokenizeRowEnd;
                construct.partial = true;

                return effects.check.hook(
                        nextPrefixedOrBlank,
                        this::tableBodyClose,
                        effects.attempt.hook(
                                construct,
                FactorySpace.create(
                        effects,
                        this::rowStartBody,
                        Types.linePrefix,
                        Constants.tabSize
                ),
                        this::tableBodyClose
      )
    ).step(code);
            }

            State tableBodyClose(int code) {
                effects.exit("tableBody");
                return tableClose(code);
            }

            State tokenizeRowEnd(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {
                class RowEndStateMachine {
                    State start(int code) {
                        Assert.check(CharUtil.markdownLineEnding(code),"expected eol");
                        effects.enter(Types.lineEnding);
                        effects.consume(code);
                        effects.exit(Types.lineEnding);
                        return FactorySpace.create(effects, this::prefixed, Types.linePrefix);
                    }

                    State prefixed(int code) {
                        // Blank or interrupting line.
                        if (
                                context.isOnLazyLine() ||
                                        code == Codes.eof ||
                                        CharUtil.markdownLineEnding(code)
                        ) {
                            return nok.step(code);
                        }

                        var tail = context.getLastEvent();

                        // Indented code can interrupt delimiter and body rows.
                        if (
                                !context.getParser().constructs.nullDisable.contains("codeIndented") &&
                                tail != null &&
                                        tail.token().type.equals(Types.linePrefix) &&
                                tail.context().sliceSerialize(tail.token(), true).length() >= Constants.tabSize
                   ) {
                            return nok.step(code);
                        }

                        context.setGfmTableDynamicInterruptHack(true);

                        return effects.check.hook(
                                context.getParser().constructs.flow,
                                c -> {
                                    context.setGfmTableDynamicInterruptHack(false);
                                    return nok.step(c);
                                },
                                c -> {
                                    context.setGfmTableDynamicInterruptHack(false);
                                    return ok.step(c);
                                }
                        ).step(code);
                    }
                }

                return new RowEndStateMachine()::start;
            }
        }

        return new StateMachine()::start;
    }

    private State tokenizeNextPrefixedOrBlank(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {
        class StateMachine {
            int size;

            State start(int code) {
                // This is a check, so we don’t care about tokens, but we open a bogus one
                // so we’re valid.
                effects.enter("check");
                // EOL.
                effects.consume(code);
                return this::whitespace;
            }

            State whitespace(int code) {
                if (code == Codes.virtualSpace || code == Codes.space) {
                    effects.consume(code);
                    size++;
                    return size == Constants.tabSize ? ok : this::whitespace;
                }

                // EOF or whitespace
                if (code == Codes.eof || CharUtil.markdownLineEndingOrSpace(code)) {
                    return ok.step(code);
                }

                // Anything else.
                return nok.step(code);
            }
        }

        return new StateMachine()::start;
    }

}

