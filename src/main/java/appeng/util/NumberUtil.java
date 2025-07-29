package appeng.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import appeng.client.gui.MathExpressionParser;

@SuppressWarnings("UnusedReturnValue")
public class NumberUtil {
    private static final String[] UNITS = { "", "K", "M", "G", "T", "P", "E", "Y", "Z", "R", "Q" };
    private static final DecimalFormat DF = new DecimalFormat("#.##");

    public static String formatNumber(double number) {
        if (number < 1000)
            return DF.format(number);
        int unit = Math.min((int) (Math.log10(number) / 3), UNITS.length - 1);
        return DF.format(number / Math.pow(1000, unit)) + UNITS[unit];
    }

    /**
     * Creates a Component displaying a percentage with coloring.
     *
     * @param current The current amount.
     * @param max     The maximum amount.
     * @return Colored Component based on percentage.
     */
    public static Component createPercentageComponent(double current, double max) {
        if (max <= 0)
            return Component.literal("0%").withStyle(ChatFormatting.GREEN);

        double percentage = Math.max(0.0, Math.min(1.0, current / max));
        String percentageText = String.format("%.2f%%", percentage * 100);

        int red, green, blue = 0;
        if (percentage <= 0.33) {
            double localPercentage = percentage / 0.33;
            red = (int) (localPercentage * 180);
            green = 180;
        } else if (percentage <= 0.66) {
            double localPercentage = (percentage - 0.33) / 0.33;
            red = 180;
            green = (int) (180 - (localPercentage * 90));
        } else {
            double localPercentage = (percentage - 0.66) / 0.34;
            red = (int) (180 + (localPercentage * 75));
            green = (int) (90 - (localPercentage * 90));
        }

        int color = (red << 16) | (green << 8) | blue;
        return Component.literal(percentageText)
                .withStyle(Style.EMPTY.withColor(color));
    }

    // THANK YOU GTNH FOR THIS
    /**
     * Matches any string that can be evaluated to a number. The pattern might be too generous, i.e., matches some
     * strings that do not evaluate to a valid value. See {@link #parse(String, Context)} for an explanation of the
     * syntax.
     */
    public static final Pattern EXPRESSION_PATTERN = Pattern
            .compile("[0-9., \u202F_’+\\-*/^()eEеЕkKкКmMмМgGгГbBtTтТsSсСiI%]*");
    // Character \u202F (' ') (non-breaking space) to support French locale thousands separator.

    private static final Context defaultContext = new Context();

    /**
     * Parses a mathematical expression using default settings, and returns the result value. See
     * {@link #parse(String, Context)}.
     *
     * @see #parse(String, Context)
     * @param expr String representation of expression to be parsed.
     * @return Value of the expression.
     */
    public static double parse(String expr) {
        return parse(expr, defaultContext);
    }

    /**
     * Parses a mathematical expression and returns the result value.
     * <p>
     * Supported concepts:
     * <ul>
     * <li>Decimal digits 0...9.</li>
     * <li>Locale-specific decimal separator: '.' or ','.</li>
     * <li>Locale-specific thousands separator: ',', '.', ' ', or ' ' (Non-breaking space, French locale).</li>
     * <li>Binary operations: '+', '-', '*', '/', '^'</li>
     * <li>Unary '-'.</li>
     * <li>Parentheses: '(', ')'.</li>
     * <li>Scientific notation: 'e', 'E'.</li>
     * <li>Suffixes denoting large values: 'k', 'K', 'm', 'M', 'b', 'B', 'g', 'G', 't', 'T'.</li>
     * <li>Percentage of maximum amount (specify maximum in the context instance): '%'.</li>
     * </ul>
     * </p>
     * <p>
     * All evaluation is done with <code>double</code> precision. Standard rules of operator priority are followed.
     * </p>
     * <p>
     * To further tune details of parsing, pass an instance of {@link Context}. See documentation of this class for
     * details of options.
     * </p>
     * <p>
     * After parsing finishes, calling {@link Context#wasSuccessful()} indicates whether parsing was successful or not.
     * In case parsing fails, {@link Context#getErrorMessage()} will try to give a description of what went wrong. Note
     * that this only handles syntax errors; arithmetic errors (such as division by zero) are not checked and will
     * return a value according to Java specification of the <code>double</code> type.
     * </p>
     *
     * @param expr String representation of expression to be parsed.
     * @param ctx  Context to use for parsing.
     * @return Value of the expression.
     */
    public static double parse(String expr, Context ctx) {
        if (expr == null) {
            ctx.success = true;
            ctx.errorMessage = "Success";
            return ctx.emptyValue;
        }

        // Strip all spaces and underscores from the input string.
        // This allows using them for readability and as thousands separators (using java convention 1_000_000).
        // This also correctly interprets numbers in the French locale typed by user using spaces as thousands
        // separators.
        // See: https://bugs.java.com/bugdatabase/view_bug?bug_id=4510618
        expr = expr.replace(" ", "").replace("_", "");

        if (expr.isEmpty()) {
            ctx.success = true;
            ctx.errorMessage = "Success";
            return ctx.emptyValue;
        }

        // Read the first numeric value, skip any further parsing if the string contains *only* one number.
        ParsePosition parsePos = new ParsePosition(0);
        Number value = ctx.numberFormat.parse(expr, parsePos);
        if (ctx.plainOnly) {
            // Skip any further parsing, only return what was found.
            if (value == null || parsePos.getIndex() == 0) {
                ctx.success = false;
                ctx.errorMessage = "Error: No number found";
                return ctx.errorValue;
            } else {
                ctx.success = true;
                ctx.errorMessage = "Success";
                return value.doubleValue();
            }
        }

        if (value != null && parsePos.getIndex() == expr.length()) {
            // The entire expr is just a single number. Skip the rest of parsing completely.
            ctx.success = true;
            ctx.errorMessage = "Success";
            return value.doubleValue();
        }

        // There are still characters to be read, continue with full parsing.
        List<StackElement> stack = new ArrayList<>();
        ctx.success = true;

        if (value != null) {
            double d = value.doubleValue();
            if (d < 0) {
                // Special case to fix a problem with operator priority:
                // Input "-5^2" needs to be parsed as (Operator.UNARY_MINUS) (5) (Operator.POWER) (2),
                // to be correctly evaluated as -(5^2).
                // Using value as it is would result in parsing this as (-5) (Operator.POWER) (2),
                // and evaluate incorrectly as (-5)^2.
                handleMinus(stack, ctx);
                handleNumber(stack, -d, ctx);
            } else {
                handleNumber(stack, d, ctx);
            }
        }

        for (int i = parsePos.getIndex(); i < expr.length(); ++i) {
            char c = expr.charAt(i);

            switch (c) {
                // Plus and minus need special handling, could be unary or binary:
                case '+':
                    handlePlus(stack, ctx);
                    break;
                case '-':
                    handleMinus(stack, ctx);
                    break;

                // Binary operators:
                case '*':
                    handleOperator(stack, Operator.MULTIPLY, ctx);
                    break;
                case '/':
                    handleOperator(stack, Operator.DIVIDE, ctx);
                    break;
                case '^':
                    handleOperator(stack, Operator.POWER, ctx);
                    break;
                case 'e':
                case 'E':
                case 'е':
                case 'Е':
                    handleOperator(stack, Operator.SCIENTIFIC, ctx);
                    break;

                // Suffixes:
                case 'k':
                case 'K':
                case 'к':
                case 'К':
                    handleSuffix(stack, Suffix.THOUSAND, c, ctx);
                    break;
                case 'm':
                case 'M':
                case 'м':
                case 'М':
                    handleSuffix(stack, Suffix.MILLION, c, ctx);
                    break;
                case 'b':
                case 'B':
                case 'g':
                case 'G':
                case 'г':
                case 'Г':
                    handleSuffix(stack, Suffix.BILLION, c, ctx);
                    break;
                case 't':
                case 'T':
                case 'т':
                case 'Т':
                    handleSuffix(stack, Suffix.TRILLION, c, ctx);
                    break;

                case 's':
                case 'S':
                case 'с':
                case 'С':
                    handleSuffix(stack, Suffix.STACK, c, ctx);
                    break;
                case 'i':
                case 'I':
                    handleSuffix(stack, Suffix.INGOT, c, ctx);
                    break;

                case '%':
                    handleSuffix(stack, Suffix.PERCENT, c, ctx);
                    break;

                // Brackets:
                case '(':
                    handleOpenBracket(stack, ctx);
                    break;
                case ')':
                    handleClosedBracket(stack, ctx);
                    break;

                // Otherwise, read the next number.
                default:
                    parsePos.setIndex(i);
                    value = ctx.numberFormat.parse(expr, parsePos);
                    if (value == null || parsePos.getIndex() == i) {
                        ctx.success = false;
                        ctx.errorMessage = "Error: Number expected";
                        return ctx.errorValue;
                    } else {
                        handleNumber(stack, value.doubleValue(), ctx);
                        i = parsePos.getIndex() - 1;
                    }
            }

            if (!ctx.success) {
                return ctx.errorValue;
            }
        }

        handleExpressionEnd(stack, ctx);

        if (!ctx.success) {
            return ctx.errorValue;
        }

        ctx.errorMessage = "Success";
        return stack.get(0).value;
    }

    /**
     * Adds a new operator to the top of the stack. If the top of the stack contains any operations with a priority
     * higher than or equal to this operator, they are evaluated first.
     *
     * @return True on success, false on failure.
     */
    private static boolean handleOperator(@NotNull List<StackElement> stack, Operator op, Context ctx) {
        if (stack.isEmpty()) {
            ctx.success = false;
            ctx.errorMessage = "Syntax error: no left-hand value for operator " + op;
            return false;
        }
        if (stack.get(stack.size() - 1).isOperator) {
            ctx.success = false;
            ctx.errorMessage = "Syntax error: two operators in a row: " + stack.get(stack.size() - 1).operator
                    + ", "
                    + op;
            return false;
        }
        // Evaluate any preceding operations with equal or higher priority than op.
        // Exponentiation is right-associative, so in a ^ b ^ c we do not evaluate a ^ b yet.
        evaluateStack(stack, op == Operator.POWER ? op.priority + 1 : op.priority);

        stack.add(new StackElement(op));
        return true;
    }

    /**
     * Special handling for plus, we need to determine whether this is a unary or binary plus. If the top of the stack
     * is a number, this is binary; if the stack is empty or the top is an operator, this is unary.
     *
     * @return True on success, false on failure.
     */
    private static boolean handlePlus(@NotNull List<StackElement> stack, Context ctx) {
        if (stack.isEmpty() || stack.get(stack.size() - 1).isOperator) {
            // Unary plus.
            stack.add(new StackElement(0));
            stack.add(new StackElement(Operator.UNARY_PLUS));
        } else {
            // Binary plus.
            return handleOperator(stack, Operator.PLUS, ctx);
        }
        return true;
    }

    /**
     * Special handling for minus, we need to determine whether this is a unary or binary minus. If the top of the stack
     * is a number, this is binary; if the stack is empty or the top is an operator, this is unary.
     *
     * @return True on success, false on failure.
     */
    private static boolean handleMinus(@NotNull List<StackElement> stack, Context ctx) {
        if (stack.isEmpty() || stack.get(stack.size() - 1).isOperator) {
            // Unary minus.
            stack.add(new StackElement(0));
            stack.add(new StackElement(Operator.UNARY_MINUS));
        } else {
            // Binary minus.
            return handleOperator(stack, Operator.MINUS, ctx);
        }
        return true;
    }

    /**
     * Handles adding a suffix on top of the stack. Suffixes are never actually added to the stack, since they have the
     * highest priority. Instead, the value on top of the stack is directly modified by the suffix.
     *
     * @param chr Character representing the suffix. This is used for error reporting, as the same suffix can be
     *            represented by multiple different characters (for example, k and K).
     * @return True on success, false on failure.
     */
    private static boolean handleSuffix(@NotNull List<StackElement> stack, Suffix suf, char chr, Context ctx) {
        if (stack.isEmpty()) {
            ctx.success = false;
            ctx.errorMessage = "Syntax error: no value for suffix " + chr;
            return false;
        }
        StackElement a = stack.get(stack.size() - 1);
        if (!a.isValue) {
            ctx.success = false;
            ctx.errorMessage = "Syntax error: suffix " + chr + " follows operator " + a.operator;
            return false;
        }
        stack.remove(stack.size() - 1);
        if (suf == Suffix.PERCENT) {
            // a% of hundredPercent
            stack.add(new StackElement(a.value * 0.01 * ctx.hundredPercent));
        } else {
            stack.add(new StackElement(a.value * suf.multiplier));
        }
        return true;
    }

    /**
     * Handle adding a number on the stack. Check that the top of the stack is an operator, then add the number.
     *
     * @return True on success, false on failure.
     */
    private static boolean handleNumber(@NotNull List<StackElement> stack, double value, Context ctx) {
        if (!stack.isEmpty() && stack.get(stack.size() - 1).isValue) {
            ctx.success = false;
            ctx.errorMessage = "Syntax error: Number " + stack.get(stack.size() - 1).value
                    + " followed by number "
                    + value;
            return false;
        }
        stack.add(new StackElement(value));
        return true;
    }

    /**
     * Handle an open bracket. If the bracket is immediately preceded by a number, interpret this as multiplication.
     * Otherwise, only add the bracket to the stack.
     *
     * @return True on success, false on failure.
     */
    private static boolean handleOpenBracket(@NotNull List<StackElement> stack, Context ctx) {
        if (!stack.isEmpty() && stack.get(stack.size() - 1).isValue) {
            if (!handleOperator(stack, Operator.MULTIPLY, ctx)) {
                return false;
            }
        }
        // Add a fake value to keep the stack always alternating between values and operators.
        stack.add(new StackElement(0));
        stack.add(new StackElement(Operator.OPEN_BRACKET));
        return true;
    }

    /**
     * Handle closed bracket on the stack: Evaluate everything up to the preceding open bracket.
     *
     * @return True on success, false on failure.
     */
    private static boolean handleClosedBracket(@NotNull List<StackElement> stack, Context ctx) {
        if (stack.isEmpty()) {
            ctx.success = false;
            ctx.errorMessage = "Syntax error: Mismatched closed bracket";
            return false;
        }
        if (stack.get(stack.size() - 1).isOperator) {
            ctx.success = false;
            ctx.errorMessage = "Syntax error: Closed bracket immediately after operator "
                    + stack.get(stack.size() - 1).operator;
            return false;
        }

        // Evaluate everything up to the last open bracket.
        evaluateStack(stack, Operator.OPEN_BRACKET.priority + 1);

        // Check for and remove matching open bracket.
        if (stack.size() < 2 || !stack.get(stack.size() - 2).isOperator
                || stack.get(stack.size() - 2).operator != Operator.OPEN_BRACKET) {
            ctx.success = false;
            ctx.errorMessage = "Syntax error: Mismatched closed bracket";
            return false;
        }
        // Open bracket is preceded by a fake value to always alternate between values and operators.
        // Remove both the bracket and this value.
        stack.remove(stack.size() - 2);
        stack.remove(stack.size() - 2);
        return true;
    }

    /**
     * Handle the end of expression. Evaluate everything, make sure that only one value is left.
     */
    private static boolean handleExpressionEnd(@NotNull List<StackElement> stack, Context ctx) {
        if (stack.isEmpty()) {
            // We should never get here, if the expression is empty parsing does not even begin.
            ctx.success = false;
            ctx.errorMessage = "Internal error: Evaluating empty expression";
            return false;
        }
        if (stack.get(stack.size() - 1).isOperator) {
            ctx.success = false;
            ctx.errorMessage = "Syntax error: no right-hand value for operator " + stack.get(stack.size() - 1).operator;
            return false;
        }

        // Evaluate the rest of the expression.
        // This will also automatically close any remaining open brackets,
        // since an open bracket is an "operator" that simply returns its right hand argument.
        evaluateStack(stack, -1);

        if (stack.size() > 1) {
            // This should never happen, there are still operators to be parsed?
            ctx.success = false;
            ctx.errorMessage = "Internal error: operators remaining after evaluating expression";
            return false;
        }
        return true;
    }

    /**
     * Evaluates operators from the top of the stack, which have a priority of at least minPriority. For example, if the
     * stack contains 1 + 2 * 3 ^ 4, and minPriority is the priority of division, the exponentiation and multiplication
     * are evaluated, but the addition is not.
     * <p>
     * This means that 1 + 2 * 3 ^ 4 / 5 gets correctly parsed as 1 + ((2 * (3 ^ 4)) / 5).
     */
    private static void evaluateStack(@NotNull List<StackElement> stack, int minPriority) {
        // The invariant is that values and operators always alternate on the stack.
        // This loop must preserve it for the internals of the stack.
        while (stack.size() >= 3) {
            StackElement op = stack.get(stack.size() - 2);

            if (op.operator.priority >= minPriority) {
                StackElement right = stack.remove(stack.size() - 1);
                stack.remove(stack.size() - 1); // op
                StackElement left = stack.remove(stack.size() - 1);
                stack.add(new StackElement(op.operator.evaluate(left.value, right.value)));
                // Removed value - operator - value, added value. Invariant is preserved.
            } else {
                break;
            }
        }
    }

    private static class StackElement {

        public Operator operator;
        public double value;
        public boolean isValue;
        public boolean isOperator;

        public StackElement(Operator operator) {
            this.operator = operator;
            this.isValue = false;
            this.isOperator = true;
        }

        public StackElement(double value) {
            this.value = value;
            this.isValue = true;
            this.isOperator = false;
        }

        @Override
        public String toString() {
            if (isValue && isOperator) {
                return "Error! Stack element incorrectly set to both value and operator.";
            }
            if (isValue) {
                return "Value: " + value;
            }
            if (isOperator) {
                return "Operator: " + operator;
            }
            return "Error! Stack element incorrectly set to neither value nor operator.";
        }
    }

    private enum Operator {

        PLUS('+', 10, (a, b) -> a + b),
        MINUS('-', 10, (a, b) -> a - b),
        MULTIPLY('*', 20, (a, b) -> a * b),
        DIVIDE('/', 20, (a, b) -> a / b),
        UNARY_PLUS('+', 30, (a, b) -> b),
        UNARY_MINUS('-', 30, (a, b) -> -b),
        POWER('^', 40, (a, b) -> Math.pow(a, b)),
        SCIENTIFIC('e', 50, (a, b) -> a * Math.pow(10, b)),

        OPEN_BRACKET('(', 1, (a, b) -> b);

        public final char name;
        public final int priority;

        public double evaluate(double left, double right) {
            return evaluator.apply(left, right);
        }

        private final BiFunction<Double, Double, Double> evaluator;

        Operator(char name, int priority, BiFunction<Double, Double, Double> evaluator) {
            this.name = name;
            this.priority = priority;
            this.evaluator = evaluator;
        }

        @Override
        public String toString() {
            return String.valueOf(name);
        }
    }

    private enum Suffix {

        THOUSAND(1_000d),
        MILLION(1_000_000d),
        BILLION(1_000_000_000d),
        TRILLION(1_000_000_000_000d),
        STACK(64),
        INGOT(144),
        PERCENT(0); // Handled separately.

        public final double multiplier;

        Suffix(double multiplier) {
            this.multiplier = multiplier;
        }
    }

    /**
     * Pass an instance of this to {@link MathExpressionParser#parse} to configure details of parsing.
     */
    public static class Context {

        private double emptyValue = 0;

        /**
         * Value to return if the expression is empty.
         * <p>
         * Default: 0
         */
        public Context setEmptyValue(double emptyValue) {
            this.emptyValue = emptyValue;
            return this;
        }

        private double errorValue = 0;

        /**
         * Value to return if the expression contains an error. Note that this only catches syntax errors, not
         * evaluation errors like overflow or division by zero.
         * <p>
         * Default: 0
         */
        public Context setErrorValue(double errorValue) {
            this.errorValue = errorValue;
            return this;
        }

        /**
         * Default value to return when the expression is empty or has an error.
         * <p>
         * Equivalent to <code>ctx.setEmptyValue(defaultValue).setErrorValue(defaultValue)</code>.
         */
        public Context setDefaultValue(double defaultValue) {
            this.emptyValue = defaultValue;
            this.errorValue = defaultValue;
            return this;
        }

        private double hundredPercent = 100;

        /**
         * Value to be considered 100% for expressions which contain percentages. For example, if this is 500, then
         * "20%" evaluates to 100.
         * <p>
         * Default: 100
         */
        public Context setHundredPercent(double hundredPercent) {
            this.hundredPercent = hundredPercent;
            return this;
        }

        private NumberFormat numberFormat = DecimalFormat.getNumberInstance(Locale.US);

        /**
         * Format in which to expect the input expression to be. The main purpose of specifying this is properly
         * handling thousands separators and decimal point.
         * <p>
         * This defaults to the EN_US locale. Care should be taken when changing this in a multiplayer setting. Code
         * that blindly trusts the player's system locale will run into issues. One player could input a value, which
         * will be formatted for that player's locale and potentially stored as a string. Then another player with a
         * <b>different</b> locale might open the same UI, and see what to their client looks like a malformed string.
         * <p>
         * Proper locale-aware code needs to communicate only the numeric value between server and all clients, and let
         * every client both parse and format it on their own.
         */
        public Context setNumberFormat(NumberFormat numberFormat) {
            this.numberFormat = numberFormat;
            return this;
        }

        private boolean plainOnly = false;

        /**
         * If this is true, no expression parsing is performed, and the input is expected to be just a plain number. The
         * parsing still handles localization, error handling, etc.
         * <p>
         * Default: false
         */
        public Context setPlainOnly(boolean plainOnly) {
            this.plainOnly = plainOnly;
            return this;
        }

        private boolean success = false;

        /**
         * Call this after parsing has finished.
         *
         * @return true if the last parsing operation using this context was successful.
         */
        public boolean wasSuccessful() {
            return success;
        }

        private String errorMessage = "";

        /**
         * Call this after parsing has finished.
         *
         * @return If the parsing has failed with an error, this will try to explain what went wrong.
         */
        public String getErrorMessage() {
            return errorMessage;
        }

    }
}
