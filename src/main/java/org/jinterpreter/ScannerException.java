package org.jinterpreter;

public class ScannerException extends RuntimeException {

	public ScannerException(int line, String message) {
		super("line " + line + ": " + message);
	}
}

