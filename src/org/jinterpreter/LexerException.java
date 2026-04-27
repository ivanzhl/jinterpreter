package org.jinterpreter;

public class LexerException extends RuntimeException {

    public LexerException(int line, String message) {
        super("line " + line + ": " + message);
    }
}

