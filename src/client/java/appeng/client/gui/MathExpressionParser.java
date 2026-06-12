package appeng.client.gui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class MathExpressionParser {
    private static final BigDecimal THIRTY = BigDecimal.valueOf(30);
    private static final BigDecimal ONE_BILLION = BigDecimal.valueOf(1e9);

    public static Optional<BigDecimal> parse(String expression, DecimalFormat decimalFormat) {
        // Parse using the Shunting Yard Algorithm

        List<Object> output = new ArrayList<>();
        Stack<Character> operatorStack = new Stack<>();
        boolean wasNumberOrRightBracket = false;

        for (int i = 0; i < expression.length();) {
            if (Character.isWhitespace(expression.charAt(i))) {
                i++;
                continue;
            }

            if (!wasNumberOrRightBracket && expression.charAt(i) != '-') {
                var position = new ParsePosition(i);
                Number parsedNumber = decimalFormat.parse(expression, position);
                if (position.getErrorIndex() == -1) { // no error
                    if (!(parsedNumber instanceof BigDecimal decimal)) {
                        // NaN or infinity
                        return Optional.empty();
                    }
                    output.add(decimal);
                    i = position.getIndex();
                    wasNumberOrRightBracket = true;
                    continue;
                }
            }

            char currentOperator = expression.charAt(i);
            if (currentOperator == '-' && !wasNumberOrRightBracket) {
                currentOperator = 'u'; // unitary minus
            }

            wasNumberOrRightBracket = false;

            switch (currentOperator) {
                case '(', 'u' -> {
                    operatorStack.push(currentOperator);
                }
                case ')' -> {
                    while (true) {
                        if (operatorStack.isEmpty()) {
                            return Optional.empty(); // mismatched parenthesis
                        }
                        char operator = operatorStack.pop();
                        if (operator == '(') {
                            break;
                        } else {
                            output.add(operator);
                        }
                    }
                    wasNumberOrRightBracket = true;
                }
                case '+', '-', '*', '/', '^' -> {
                    while (!operatorStack.isEmpty()) {
                        char operator = operatorStack.peek();
                        if (operator != '(' && precedenceCheck(operator, currentOperator)) {
                            operatorStack.pop();
                            output.add(operator);
                        } else {
                            break;
                        }
                    }
                    operatorStack.push(currentOperator);
                }
                default -> {
                    return Optional.empty();
                }

            }
            i++;

        }

        while (!operatorStack.isEmpty()) {
            output.add(operatorStack.pop());
        }

        Stack<BigDecimal> number = new Stack<>();

        for (Object object : output) {
            if (object instanceof BigDecimal bigDecimal) {
                number.push(bigDecimal);
            } else {
                char currentOperator = (char) object;
                if (currentOperator != 'u') {
                    if (number.size() < 2) {
                        return Optional.empty();
                    } else {
                        BigDecimal right = number.pop();
                        BigDecimal left = number.pop();
                        switch (currentOperator) {
                            case '+' -> {
                                number.push(right.add(left));
                            }
                            case '*' -> {
                                number.push(right.multiply(left));
                            }
                            case '-' -> {
                                number.push(left.subtract(right));
                            }
                            case '/' -> {
                                if (right.compareTo(BigDecimal.ZERO) == 0) {
                                    return Optional.empty(); // division by zeroes
                                } else {
                                    number.push(left.divide(right, 8, RoundingMode.FLOOR));
                                }
                            }
                            case '^' -> {
                                right = right.stripTrailingZeros();
                                // if has a decimal part or is smaller than 0 -> nope
                                if (right.scale() > 0 || right.compareTo(BigDecimal.ZERO) < 0) {
                                    return Optional.empty();
                                }
                                // limit exponent to 30
                                if (right.compareTo(THIRTY) > 0) {
                                    return Optional.empty(); // exponent too big
                                }
                                // limit base number to 1e9
                                if (left.compareTo(ONE_BILLION) > 0) {
                                    return Optional.empty(); // base number too big
                                }
                                number.push(left.pow(right.intValueExact()));
                            }
                            case '(', ')' -> {
                                return Optional.empty(); // should not have any remaining parenthesis in the stack
                            }
                            default -> {
                                throw new IllegalStateException("Unreachable character : " + currentOperator);
                            }
                        }
                    }
                } else {
                    if (number.isEmpty()) {
                        return Optional.empty();
                    } else {
                        number.push(number.pop().negate());
                    }
                }
            }
        }

        if (number.size() != 1) {
            return Optional.empty();
        } else {
            return Optional.of(number.pop().stripTrailingZeros());
        }

    }

    private static int getPrecedence(char operator) {
        return switch (operator) {
            case '^' -> -1;
            case 'u' -> 0;
            case '/', '*' -> 1;
            case '+', '-' -> 2;
            default -> throw new IllegalArgumentException("Invalid Operator : " + operator);
        };
    }

    private static boolean precedenceCheck(char first, char second) {
        return getPrecedence(first) <= getPrecedence(second);
    }

}
