public record Token(TokenType type, String text, int line) {

    @Override
    public String toString() {
        return type + "(" + text + ")@" + line;
    }
}