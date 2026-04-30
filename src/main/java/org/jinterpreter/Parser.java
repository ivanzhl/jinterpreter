package org.jinterpreter;

import org.jinterpreter.ast.Node;
import org.jinterpreter.exceptions.ParseException;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Token> tokens;

    private int currentPosition = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Node> parse() {
        final List<Node> statements = new ArrayList<>();
        while (!reachedEndOfTokens()) {
            statements.add(parseStatement());
            skipStatementSeparator();
        }
        return statements;
    }

    private Node parseStatement() {
        if (isCurrentTokenOfType(TokenType.FUN))    return parseFunctionDefinition();
        return parseNestedStatement();
    }

    private Node parseNestedStatement() {
        if (isCurrentTokenOfType(TokenType.FUN))
            throw new ParseException(currentToken().line(), "function definitions are only allowed at the top level");
        if (isCurrentTokenOfType(TokenType.RETURN)) return parseReturnStatement();
        if (isCurrentTokenOfType(TokenType.IF))     return parseIfStatement();
        if (isCurrentTokenOfType(TokenType.WHILE))  return parseWhileStatement();
        return parseAssignment();
    }

    private Node parseFunctionDefinition() {
        advance();
        final String name = expectIdentifier("function name");
        final List<String> parameters = parseParameterList();
        expectToken(TokenType.BRACE_OPEN, "'{'");
        final Node body = parseBlockBody();
        expectToken(TokenType.BRACE_CLOSE, "'}'");
        return new Node.Fun(name, parameters, body);
    }

    private Node parseReturnStatement() {
        advance();
        return new Node.Return(parseExpression());
    }

    private Node parseIfStatement() {
        advance();
        final Node condition  = parseExpression();
        expectToken(TokenType.THEN, "'then'");
        final Node thenBranch = parseNestedStatement();
        final Node elseBranch = parseElseBranchIfPresent();
        return new Node.If(condition, thenBranch, elseBranch);
    }

    private Node parseElseBranchIfPresent() {
        if (isCurrentTokenOfType(TokenType.ELSE)) {
            advance();
            return parseNestedStatement();
        }
        return new Node.Block(List.of());
    }

    private Node parseWhileStatement() {
        advance();
        final Node condition = parseExpression();
        expectToken(TokenType.DO, "'do'");
        final Node body = parseLoopBody();
        return new Node.While(condition, body);
    }

    private Node parseAssignment() {
        if (isCurrentTokenOfType(TokenType.IDENTIFIER) && isNextTokenOfType(TokenType.ASSIGN)) {
            final String name = advance().text();
            advance();
            return new Node.Assign(name, parseExpression());
        }
        return parseExpression();
    }

    private Node parseLoopBody() {
        final List<Node> statements = new ArrayList<>();
        statements.add(parseNestedStatement());
        while (isCurrentTokenOfType(TokenType.COMMA)) {
            advance();
            statements.add(parseNestedStatement());
        }
        if (statements.size() == 1) return statements.getFirst();
        return new Node.Block(statements);
    }

    private List<String> parseParameterList() {
        expectToken(TokenType.PAREN_OPEN, "'('");
        final List<String> parameters = new ArrayList<>();
        if (!isCurrentTokenOfType(TokenType.PAREN_CLOSE)) {
            parameters.add(expectIdentifier("parameter name"));
            while (isCurrentTokenOfType(TokenType.COMMA)) {
                advance();
                parameters.add(expectIdentifier("parameter name"));
            }
        }
        expectToken(TokenType.PAREN_CLOSE, "')'");
        return parameters;
    }

    private Node parseBlockBody() {
        final List<Node> statements = new ArrayList<>();
        while (!isCurrentTokenOfType(TokenType.BRACE_CLOSE) && !reachedEndOfTokens()) {
            statements.add(parseNestedStatement());
            skipStatementSeparator();
        }
        return new Node.Block(statements);
    }

    private Node parseExpression() {
        Node left = parseAdditionOrSubtraction();
        while (isCurrentTokenComparisonOperator()) {
            final String operator = advance().text();
            final Node right = parseAdditionOrSubtraction();
            left = new Node.BinaryOperation(operator, left, right);
        }
        return left;
    }

    private boolean isCurrentTokenComparisonOperator() {
        return switch (currentToken().type()) {
            case LESS_THAN, LESS_THAN_OR_EQUAL,
                 GREATER_THAN, GREATER_THAN_OR_EQUAL,
                 EQUALS, NOT_EQUALS -> true;
            default -> false;
        };
    }

    private Node parseAdditionOrSubtraction() {
        Node left = parseMultiplicationOrDivision();
        while (isCurrentTokenOfType(TokenType.PLUS) || isCurrentTokenOfType(TokenType.MINUS)) {
            final String operator = advance().text();
            final Node right = parseMultiplicationOrDivision();
            left = new Node.BinaryOperation(operator, left, right);
        }
        return left;
    }

    private Node parseMultiplicationOrDivision() {
        Node left = parsePrimaryExpression();
        while (isCurrentTokenOfType(TokenType.STAR) || isCurrentTokenOfType(TokenType.SLASH)) {
            final String operator = advance().text();
            final Node right = parsePrimaryExpression();
            left = new Node.BinaryOperation(operator, left, right);
        }
        return left;
    }

    private Node parsePrimaryExpression() {
        if (isCurrentTokenOfType(TokenType.MINUS))      return parseNegation();
        if (isCurrentTokenOfType(TokenType.NUMBER))     return parseNumberLiteral();
        if (isCurrentTokenOfType(TokenType.TRUE))       return parseBooleanLiteral(1);
        if (isCurrentTokenOfType(TokenType.FALSE))      return parseBooleanLiteral(0);
        if (isCurrentTokenOfType(TokenType.IDENTIFIER)) return parseIdentifierOrFunctionCall();
        if (isCurrentTokenOfType(TokenType.PAREN_OPEN)) return parseGroupedExpression();
        throw new ParseException(currentToken().line(),
                "unexpected token '" + currentToken().text() + "'");
    }

    private Node parseNegation() {
        advance();
        return new Node.BinaryOperation("*", new Node.Number(-1), parsePrimaryExpression());
    }

    private Node parseNumberLiteral() {
        return new Node.Number(Double.parseDouble(advance().text()));
    }

    private Node parseBooleanLiteral(double value) {
        advance();
        return new Node.Number(value);
    }

    private Node parseIdentifierOrFunctionCall() {
        final String name = advance().text();
        if (isCurrentTokenOfType(TokenType.PAREN_OPEN))
            return parseFunctionCall(name);
        return new Node.Identifier(name);
    }

    private Node parseFunctionCall(String name) {
        advance();
        final List<Node> arguments = parseArgumentList();
        expectToken(TokenType.PAREN_CLOSE, "')'");
        return new Node.Call(name, arguments);
    }

    private List<Node> parseArgumentList() {
        final List<Node> arguments = new ArrayList<>();
        if (!isCurrentTokenOfType(TokenType.PAREN_CLOSE)) {
            arguments.add(parseExpression());
            while (isCurrentTokenOfType(TokenType.COMMA)) {
                advance();
                arguments.add(parseExpression());
            }
        }
        return arguments;
    }

    private Node parseGroupedExpression() {
        advance();
        final Node expression = parseExpression();
        expectToken(TokenType.PAREN_CLOSE, "')'");
        return expression;
    }

    private Token currentToken() {
        return tokens.get(currentPosition);
    }

    private Token advance() {
        final Token token = tokens.get(currentPosition);
        currentPosition++;
        return token;
    }

    private void skipStatementSeparator() {
        if (isCurrentTokenOfType(TokenType.COMMA)) advance();
    }

    private boolean isCurrentTokenOfType(TokenType type) {
        return currentToken().type() == type;
    }

    private boolean isNextTokenOfType(TokenType type) {
        if (currentPosition + 1 >= tokens.size()) return false;
        return tokens.get(currentPosition + 1).type() == type;
    }

    private boolean reachedEndOfTokens() {
        return isCurrentTokenOfType(TokenType.END_OF_FILE);
    }

    private void expectToken(TokenType type, String description) {
        if (!isCurrentTokenOfType(type))
            throw new ParseException(currentToken().line(),
                    "expected " + description + " but found '" + currentToken().text() + "'");
        advance();
    }

    private String expectIdentifier(String description) {
        if (!isCurrentTokenOfType(TokenType.IDENTIFIER))
            throw new ParseException(currentToken().line(),
                    "expected " + description + " but found '" + currentToken().text() + "'");
        return advance().text();
    }
}
