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

            if (!wasNumberOrRightBracket || expression.charAt(i) != '-') {
                var position = new ParsePosition(i);
                BigDecimal decimal = (BigDecimal) decimalFormat.parse(expression, position);
                if (position.getErrorIndex() == -1) { // no error
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
                case '+', '-', '*', '/' -> {
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
                                if (right.equals(BigDecimal.ZERO)) {
                                    return Optional.empty(); // division by zeroes
                                } else {
                                    number.push(left.divide(right, 8, RoundingMode.FLOOR));
                                }
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
                    if (number.size() < 1) {
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
            return Optional.of(number.pop());
        }

    }

    private static int getPrecedence(char operator) {
        return switch (operator) {
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
