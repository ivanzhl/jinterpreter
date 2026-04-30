package org.jinterpreter.exceptions;

public class InterpreterException extends RuntimeException {

    public InterpreterException(String message) {
        super("Interpreter error: " + message);
    }
}