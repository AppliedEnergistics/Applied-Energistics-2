package appeng.libs.micromark;

import appeng.libs.micromark.factory.FactorySpace;
import appeng.libs.micromark.symbol.Codes;
import appeng.libs.micromark.symbol.Constants;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class InitializeDocument {
    private InitializeDocument() {
    }

    public static final InitialConstruct document;
    public static final Construct containerConstruct;

    static {
        document = new InitialConstruct();
        document.tokenize = (context, effects, ok, nok) -> new StateMachine(context, effects)::start;

        containerConstruct = new Construct();
        containerConstruct.tokenize = InitializeDocument::tokenizeContainer;
    }

    record StackItem(Construct construct, Tokenizer.ContainerState stackState) {
    }

    private static class StateMachine {
        private final TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final List<StackItem> stack = new ArrayList<>();
        private int continued = 0;
        @Nullable
        private TokenizeContext childFlow;
        @Nullable
        private Token childToken;
        private int lineStartOffset;

        public StateMachine(TokenizeContext context, Tokenizer.Effects effects) {
            this.context = context;
            this.effects = effects;
        }

        public State start(int code) {
            // First we iterate through the open blocks, starting with the root
            // document, and descending through last children down to the last open
            // block.
            // Each block imposes a condition that the line must satisfy if the block is
            // to remain open.
            // For example, a block quote requires a `>` character.
            // A paragraph requires a non-blank line.
            // In this phase we may match all or just some of the open blocks.
            // But we cannot close unmatched blocks yet, because we may have a lazy
            // continuation line.
            if (continued < stack.size()) {
                var item = stack.get(continued);
                context.setContainerState(item.stackState());
                if (item.construct.continuation == null) {
                    throw new IllegalStateException("expected 'continuation' to be defined on container construct");
                }
                return effects.attempt.hook(
                        item.construct().continuation,
                        this::documentContinue,
                        this::checkNewContainers
                ).step(code);
            }

            // Done.
            return checkNewContainers(code);
        }

        private State documentContinue(int code) {
            if (context.getContainerState() == null) {
                throw new IllegalStateException("expected 'containerState' to be defined after continuation");
            }

            continued++;

            // Note: this field is called `_closeFlow` but it also closes containers.
            // Perhaps a good idea to rename it but it’s already used in the wild by
            // extensions.
            if (Boolean.TRUE.equals(context.getContainerState().get("_closeFlow"))) {
                context.getContainerState().remove("_closeFlow");

                if (childFlow != null) {
                    closeFlow();
                }

                // Note: this algorithm for moving events around is similar to the
                // algorithm when dealing with lazy lines in `writeToChild`.
                var indexBeforeExits = context.getEvents().size();
                var indexBeforeFlow = indexBeforeExits;
                Point point = null;

                // Find the flow chunk.
                while (indexBeforeFlow-- > 0) {
                    var event = context.getEvents().get(indexBeforeFlow);
                    if (
                            event.type() == Tokenizer.EventType.EXIT &&
                                    event.token().type.equals(Types.chunkFlow)
                    ) {
                        point = event.token().end;
                        break;
                    }
                }

                if (point == null) {
                    throw new IllegalStateException("could not find previous flow chunk");
                }

                exitContainers(continued);

                // Fix positions.
                var index = indexBeforeExits;

                while (index < context.getEvents().size()) {
                    context.getEvents().get(index).token().end = point;
                    index++;
                }

                // Inject the exits earlier (they’re still also at the end).
                var eventsToMove = ListUtils.slice(context.getEvents(), indexBeforeExits);
                context.getEvents().addAll(
                        indexBeforeFlow + 1,
                        eventsToMove
                );

                // Discard the duplicate exits.
                ListUtils.setLength(context.getEvents(), index);

                return checkNewContainers(code);
            }

            return start(code);
        }

        private State checkNewContainers(int code) {
            // Next, after consuming the continuation markers for existing blocks, we
            // look for new block starts (e.g. `>` for a block quote).
            // If we encounter a new block start, we close any blocks unmatched in
            // step 1 before creating the new block as a child of the last matched
            // block.
            if (continued == stack.size()) {
                // No need to `check` whether there’s a container, of `exitContainers`
                // would be moot.
                // We can instead immediately `attempt` to parse one.
                if (childFlow == null) {
                    return documentContinued(code);
                }

                // If we have concrete content, such as block HTML or fenced code,
                // we can’t have containers “pierce” into them, so we can immediately
                // start.
                if (childFlow.getCurrentConstruct() != null && childFlow.getCurrentConstruct().concrete) {
                    return flowStart(code);
                }

                // If we do have flow, it could still be a blank line,
                // but we’d be interrupting it w/ a new container if there’s a current
                // construct.
                context.setInterrupt(childFlow.getCurrentConstruct() != null && !childFlow.isGfmTableDynamicInterruptHack());
            }

            // Check if there is a new container.
            context.setContainerState(new Tokenizer.ContainerState());
            return effects.check.hook(
                    containerConstruct,
                    this::thereIsANewContainer,
                    this::thereIsNoNewContainer
            ).step(code);
        }

        private State thereIsANewContainer(int code) {
            if (childFlow != null) closeFlow();
            exitContainers(continued);
            return documentContinued(code);
        }

        private State thereIsNoNewContainer(int code) {
            context.getParser().lazy.put(context.now().line(), continued != stack.size());
            lineStartOffset = context.now().offset();
            return flowStart(code);
        }

        private State documentContinued(int code) {
            // Try new containers.
            context.setContainerState(new Tokenizer.ContainerState());
            return effects.attempt.hook(
                    containerConstruct,
                    this::containerContinue,
                    this::flowStart
            ).step(code);
        }

        private State containerContinue(int code) {
            if (context.getCurrentConstruct() == null) {
                throw new IllegalStateException("expected 'currentConstruct' to be defined on tokenizer");
            }
            if (context.getContainerState() == null) {
                throw new IllegalStateException("expected 'containerState' to be defined on tokenizer");
            }
            continued++;
            stack.add(new StackItem(context.getCurrentConstruct(), context.getContainerState()));
            // Try another.
            return documentContinued(code);
        }

        private State flowStart(int code) {
            if (code == Codes.eof) {
                if (childFlow != null) closeFlow();
                exitContainers(0);
                effects.consume(code);
                return null;
            }

            if (childFlow == null) {
                childFlow = context.getParser().flow.create(context.now());
            }

            var token = new Token();
            token.contentType = ContentType.FLOW;
            token.previous = childToken;
            token._tokenizer = childFlow;

            effects.enter(Types.chunkFlow, token);

            return flowContinue(code);
        }

        private State flowContinue(int code) {
            if (code == Codes.eof) {
                writeToChild(effects.exit(Types.chunkFlow), true);
                exitContainers(0);
                effects.consume(code);
                return null;
            }

            if (CharUtil.markdownLineEnding(code)) {
                effects.consume(code);
                writeToChild(effects.exit(Types.chunkFlow), false);
                // Get ready for the next line.
                continued = 0;
                context.setInterrupt(false);
                return this::start;
            }

            effects.consume(code);
            return this::flowContinue;
        }

        private void writeToChild(Token token, boolean eof) {
            if (childFlow == null) {
                throw new IllegalStateException("expected 'childFlow' to be defined when continuing");
            }

            var stream = context.sliceStream(token);
            if (eof) {
                stream.add(Codes.eof);
            }
            token.previous = childToken;
            if (childToken != null)
                childToken.next = token;
            childToken = token;
            childFlow.defineSkip(token.start);
            childFlow.write(stream);

            // Alright, so we just added a lazy line:
            //
            // ```markdown
            // > a
            // b.
            //
            // Or:
            //
            // > ~~~c
            // d
            //
            // Or:
            //
            // > | e |
            // f
            // ```
            //
            // The construct in the second example (fenced code) does not accept lazy
            // lines, so it marked itself as done at the end of its first line, and
            // then the content construct parses `d`.
            // Most constructs in markdown match on the first line: if the first line
            // forms a construct, a non-lazy line can’t “unmake” it.
            //
            // The construct in the third example is potentially a GFM table, and
            // those are *weird*.
            // It *could* be a table, from the first line, if the following line
            // matches a condition.
            // In this case, that second line is lazy, which “unmakes” the first line
            // and turns the whole into one content block.
            //
            // We’ve now parsed the non-lazy and the lazy line, and can figure out
            // whether the lazy line started a new flow block.
            // If it did, we exit the current containers between the two flow blocks.
            if (context.getParser().isLazyLine(token.start.line())) {
                var index = childFlow.getEvents().size();

                while (index-- > 0) {
                    var childFlowToken = childFlow.getEvents().get(index).token();
                    if (
                        // The token starts before the line ending…
                            childFlowToken.start.offset() < lineStartOffset &&
                                    // …and either is not ended yet…
                                    (childFlowToken.end == null ||
                                            // …or ends after it.
                                            childFlowToken.end.offset() > lineStartOffset)
                    ) {
                        // Exit: there’s still something open, which means it’s a lazy line
                        // part of something.
                        return;
                    }
                }

                // Note: this algorithm for moving events around is similar to the
                // algorithm when closing flow in `documentContinue`.
                var indexBeforeExits = context.getEvents().size();
                var indexBeforeFlow = indexBeforeExits;
                boolean seen = false;
                Point point = null;

                // Find the previous chunk (the one before the lazy line).
                while (indexBeforeFlow-- > 0) {
                    var event = context.getEvents().get(indexBeforeFlow);
                    if (
                            event.type() == Tokenizer.EventType.EXIT &&
                                    event.token().type.equals(Types.chunkFlow)
                    ) {
                        if (seen) {
                            point = event.token().end;
                            break;
                        }

                        seen = true;
                    }
                }

                if (point == null) {
                    throw new IllegalStateException("could not find previous flow chunk");
                }

                exitContainers(continued);

                // Fix positions.
                index = indexBeforeExits;

                while (index < context.getEvents().size()) {
                    context.getEvents().get(index).token().end = point;
                    index++;
                }

                // Inject the exits earlier (they’re still also at the end).
                var eventsToMove = ListUtils.slice(context.getEvents(), indexBeforeExits);
                context.getEvents().addAll(indexBeforeFlow + 1, eventsToMove);
                // Discard the duplicate exits.
                ListUtils.setLength(context.getEvents(), index);
            }
        }

        private void exitContainers(int size) {
            var index = stack.size();

            // Exit open containers.
            while (index-- > size) {
                var entry = stack.get(index);
                context.setContainerState(entry.stackState());
                if (entry.construct.exit == null) {
                    throw new IllegalStateException("expected 'exit' to be defined on container construct");
                }
                entry.construct.exit.exit(context, effects);
            }

            ListUtils.setLength(stack, size);
        }

        private void closeFlow() {
            if (context.getContainerState() == null) {
                throw new IllegalStateException("expected 'containerState' to be defined when closing flow");
            }
            if (childFlow == null) {
                throw new IllegalStateException("expected 'childFlow' to be defined when closing it");
            }
            childFlow.write(List.of(Codes.eof));
            childToken = null;
            childFlow = null;
            context.getContainerState().remove("_closeFlow");
        }
    }

    private static State tokenizeContainer(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {
        return FactorySpace.create(
                effects,
                effects.attempt.hook(context.getParser().constructs.document, ok, nok),
                Types.linePrefix,
                context.getParser().constructs.nullDisable.contains(Types.codeIndented)
                        ? Integer.MAX_VALUE
                        : Constants.tabSize
        );
    }
}
