package org.jinterpreter.exceptions;

public class ScannerException extends RuntimeException {

    public ScannerException(int line, String message) {
        super("line " + line + ": " + message);
    }
}
