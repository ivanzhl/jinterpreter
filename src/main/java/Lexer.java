import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Lexer {

    private static final Map<String, TokenType> KEYWORDS = Map.of("if", TokenType.IF, "then", TokenType.THEN, "else", TokenType.ELSE, "while", TokenType.WHILE, "do", TokenType.DO, "fun", TokenType.FUN, "return", TokenType.RETURN, "true", TokenType.TRUE, "false", TokenType.FALSE);

    private final String input;
    private final List<Token> tokens = new ArrayList<>();
    private int tokenStart = 0;
    private int pos = 0;
    private int line = 1;

    public Lexer(String input) {
        this.input = input;
    }

    public List<Token> scan() {
        while (!atEnd()) {
            tokenStart = pos;
            readToken();
        }
        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    private void readToken() {
        char c = consume();
        switch (c) {
            case '(' -> emit(TokenType.PAREN_OPEN);
            case ')' -> emit(TokenType.PAREN_CLOSE);
            case '{' -> emit(TokenType.BRACE_OPEN);
            case '}' -> emit(TokenType.BRACE_CLOSE);
            case ',' -> emit(TokenType.COMMA);
            case '+' -> emit(TokenType.PLUS);
            case '-' -> emit(TokenType.MINUS);
            case '*' -> emit(TokenType.STAR);
            case '/' -> emit(TokenType.SLASH);
            case '<' -> emit(consumeIf('=') ? TokenType.LESS_THAN_OR_EQUAL : TokenType.LESS_THAN);
            case '>' -> emit(consumeIf('=') ? TokenType.GREATER_THAN_OR_EQUAL : TokenType.GREATER_THAN);
            case '=' -> emit(consumeIf('=') ? TokenType.EQUALS : TokenType.ASSIGN);
            case '!' -> {
                if (consumeIf('=')) emit(TokenType.NOT_EQUALS);
                else throw new LexerException(line, "unexpected '!', did you mean '!='?");
            }
            case ' ', '\t', '\r' -> {
            }
            case '\n' -> line++;
            default -> {
                if (isDigit(c)) readNumber();
                else if (isLetter(c)) readIdentOrKeyword();
                else throw new LexerException(line, "unexpected character '" + c + "'");
            }
        }
    }

    private void readNumber() {
        while (!atEnd() && isDigit(lookahead())) consume();
        if (!atEnd() && lookahead() == '.' && isDigit(lookaheadNext())) {
            consume();
            while (!atEnd() && isDigit(lookahead())) consume();
        }
        emit(TokenType.NUMBER);
    }

    private void readIdentOrKeyword() {
        while (!atEnd() && isLetterOrDigit(lookahead())) consume();
        String word = input.substring(tokenStart, pos);
        emit(KEYWORDS.getOrDefault(word, TokenType.IDENT));
    }

    private char consume() {
        return input.charAt(pos++);
    }

    private char lookahead() {
        return atEnd() ? '\0' : input.charAt(pos);
    }

    private char lookaheadNext() {
        return pos + 1 >= input.length() ? '\0' : input.charAt(pos + 1);
    }

    private boolean consumeIf(char expected) {
        if (atEnd() || input.charAt(pos) != expected) return false;
        pos++;
        return true;
    }

    private boolean atEnd() {
        return pos >= input.length();
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

    private void emit(TokenType type) {
        tokens.add(new Token(type, input.substring(tokenStart, pos), line));
    }
}