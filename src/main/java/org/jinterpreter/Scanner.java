package org.jinterpreter;

import org.jinterpreter.exceptions.ScannerException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Scanner {

    private static final Map<String, TokenType> KEYWORDS = Map.of(
            "if",     TokenType.IF,
            "then",   TokenType.THEN,
            "else",   TokenType.ELSE,
            "while",  TokenType.WHILE,
            "do",     TokenType.DO,
            "fun",    TokenType.FUN,
            "return", TokenType.RETURN,
            "true",   TokenType.TRUE,
            "false",  TokenType.FALSE
    );

    private final String      input;
    private final List<Token> tokens = new ArrayList<>();
    private int tokenStart = 0;
    private int currentPosition = 0;
    private int currentLine = 1;

    public Scanner(String input) {
        this.input = input;
    }

    public List<Token> scan() {
        while (!reachedEndOfInput()) {
            tokenStart = currentPosition;
            scanNextToken();
        }
        addToken(TokenType.END_OF_FILE);
        return tokens;
    }

    private void scanNextToken() {
        char c = advance();
        switch (c) {
            case '('             -> addToken(TokenType.PAREN_OPEN);
            case ')'             -> addToken(TokenType.PAREN_CLOSE);
            case '{'             -> addToken(TokenType.BRACE_OPEN);
            case '}'             -> addToken(TokenType.BRACE_CLOSE);
            case ','             -> addToken(TokenType.COMMA);
            case '+'             -> addToken(TokenType.PLUS);
            case '-'             -> addToken(TokenType.MINUS);
            case '*'             -> addToken(TokenType.STAR);
            case '/'             -> addToken(TokenType.SLASH);
            case '<'             -> addToken(nextCharIs('=') ? TokenType.LESS_THAN_OR_EQUAL    : TokenType.LESS_THAN);
            case '>'             -> addToken(nextCharIs('=') ? TokenType.GREATER_THAN_OR_EQUAL : TokenType.GREATER_THAN);
            case '='             -> addToken(nextCharIs('=') ? TokenType.EQUALS                : TokenType.ASSIGN);
            case '!'             -> scanBangOrNotEquals();
            case ' ', '\t', '\r' -> skipWhitespace();
            case '\n'            -> advanceLine();
            default              -> scanNumberOrIdentifier(c);
        }
    }

    private void scanBangOrNotEquals() {
        if (nextCharIs('='))
            addToken(TokenType.NOT_EQUALS);
        else
            throw new ScannerException(currentLine, "unexpected '!', did you mean '!='?");
    }

    private void scanNumberOrIdentifier(char c) {
        if (isDigit(c))
            scanNumber();
        else if (isLetter(c))
            scanIdentifierOrKeyword();
        else
            throw new ScannerException(currentLine, "unexpected character '" + c + "'");
    }

    private void scanNumber() {
        advanceWhileDigit();
        if (nextCharsAreDecimalPoint())
            advanceDecimalPart();
        addToken(TokenType.NUMBER);
    }

    private void advanceWhileDigit() {
        while (!reachedEndOfInput() && isDigit(currentChar()))
            advance();
    }

    private boolean nextCharsAreDecimalPoint() {
        return !reachedEndOfInput()
                && currentChar() == '.'
                && isDigit(charAfterCurrent());
    }

    private void advanceDecimalPart() {
        advance();
        advanceWhileDigit();
    }

    private void scanIdentifierOrKeyword() {
        while (!reachedEndOfInput() && isLetterOrDigit(currentChar())) advance();
        String word = input.substring(tokenStart, currentPosition);
        addToken(KEYWORDS.getOrDefault(word, TokenType.IDENTIFIER));
    }

    private void skipWhitespace() {}

    private void advanceLine() {
        currentLine++;
    }

    private char advance() {
        return input.charAt(currentPosition++);
    }

    private boolean nextCharIs(char expectedChat) {
        if (reachedEndOfInput() || input.charAt(currentPosition) != expectedChat) return false;
        currentPosition++;
        return true;
    }

    private char currentChar() {
        return reachedEndOfInput() ? '\0' : input.charAt(currentPosition);
    }

    private char charAfterCurrent() {
        return currentPosition + 1 >= input.length() ? '\0' : input.charAt(currentPosition + 1);
    }

    private boolean reachedEndOfInput() {
        return currentPosition >= input.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isLetterOrDigit(char c) {
        return isLetter(c) || isDigit(c);
    }

    private void addToken(TokenType type) {
        tokens.add(new Token(type, input.substring(tokenStart, currentPosition), currentLine));
    }
}
