package com.tournament.app.domain.service;

import com.tournament.app.domain.model.DomainExceptions.DomainValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class FormulaEvaluationEngine {

    public BigDecimal evaluate(String expression, Map<String, Double> variables) {
        if (expression == null || expression.isBlank()) {
            throw new DomainValidationException("Mathematical formula expression cannot be blank");
        }
        Map<String, Double> safeVars = variables != null ? variables : Map.of();
        Parser parser = new Parser(expression.trim(), safeVars);
        double result = parser.parseExpression();
        parser.expectEnd();
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_UP);
    }

    public void validateSyntax(String expression) {
        evaluate(expression, Map.ofEntries(
                Map.entry("wins", 1.0),
                Map.entry("losses", 0.0),
                Map.entry("draws", 0.0),
                Map.entry("placement", 1.0),
                Map.entry("kills", 2.0),
                Map.entry("tournament_points", 9.0),
                Map.entry("rank", 1.0),
                Map.entry("match_wins", 3.0),
                Map.entry("matches_played", 3.0),
                Map.entry("attended", 1.0),
                Map.entry("current_points", 6.0),
                Map.entry("league_points", 15.0),
                Map.entry("omw_percentage", 0.65),
                Map.entry("times_paired", 0.0)
        ));
    }

    private static final class Parser {
        private final String input;
        private final Map<String, Double> variables;
        private int pos = 0;

        private Parser(String input, Map<String, Double> variables) {
            this.input = input;
            this.variables = variables;
        }

        private void skipWhitespace() {
            while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
                pos++;
            }
        }

        private boolean consume(char expected) {
            skipWhitespace();
            if (pos < input.length() && input.charAt(pos) == expected) {
                pos++;
                return true;
            }
            return false;
        }

        private void expectEnd() {
            skipWhitespace();
            if (pos < input.length()) {
                throw new DomainValidationException(
                        "Unexpected token at position " + pos + " in formula: " + input);
            }
        }

        private double parseExpression() {
            double value = parseTerm();
            while (true) {
                skipWhitespace();
                if (consume('+')) {
                    value += parseTerm();
                } else if (consume('-')) {
                    value -= parseTerm();
                } else {
                    return value;
                }
            }
        }

        private double parseTerm() {
            double value = parseFactor();
            while (true) {
                skipWhitespace();
                if (consume('*')) {
                    value *= parseFactor();
                } else if (consume('/')) {
                    double divisor = parseFactor();
                    value = Math.abs(divisor) < 1e-9 ? 0.0 : (value / divisor);
                } else {
                    return value;
                }
            }
        }

        private double parseFactor() {
            skipWhitespace();
            if (consume('+')) {
                return parseFactor();
            }
            if (consume('-')) {
                return -parseFactor();
            }
            if (consume('(')) {
                double inside = parseExpression();
                if (!consume(')')) {
                    throw new DomainValidationException("Missing closing parenthesis ')' in formula: " + input);
                }
                return inside;
            }
            skipWhitespace();
            if (pos >= input.length()) {
                throw new DomainValidationException("Unexpected end of formula: " + input);
            }
            char c = input.charAt(pos);
            if (Character.isDigit(c) || c == '.') {
                int start = pos;
                while (pos < input.length()
                        && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
                    pos++;
                }
                try {
                    return Double.parseDouble(input.substring(start, pos));
                } catch (NumberFormatException ex) {
                    throw new DomainValidationException("Invalid number in formula: " + input.substring(start, pos));
                }
            }
            if (Character.isLetter(c) || c == '_') {
                int start = pos;
                while (pos < input.length()
                        && (Character.isLetterOrDigit(input.charAt(pos)) || input.charAt(pos) == '_')) {
                    pos++;
                }
                String identifier = input.substring(start, pos);
                if (consume('(')) {
                    double firstArg = parseExpression();
                    if (!consume(',')) {
                        throw new DomainValidationException(
                                "Function '" + identifier + "' requires two arguments separated by ','");
                    }
                    double secondArg = parseExpression();
                    if (!consume(')')) {
                        throw new DomainValidationException("Missing closing ')' for function '" + identifier + "'");
                    }
                    return switch (identifier.toLowerCase()) {
                        case "min" -> Math.min(firstArg, secondArg);
                        case "max" -> Math.max(firstArg, secondArg);
                        default -> throw new DomainValidationException("Unsupported function in formula: " + identifier);
                    };
                }
                return variables.getOrDefault(identifier, 0.0);
            }
            throw new DomainValidationException("Unexpected character '" + c + "' in formula: " + input);
        }
    }
}
