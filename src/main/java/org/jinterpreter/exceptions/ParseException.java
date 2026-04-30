package org.jinterpreter.exceptions;

public class ParseException extends RuntimeException {

    public ParseException(int line, String message) {
        super("Parse error (line " + line + "): " + message);
    }
}