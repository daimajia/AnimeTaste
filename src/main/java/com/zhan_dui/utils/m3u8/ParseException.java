package com.zhan_dui.utils.m3u8;

/**
 * @author dkuffner
 */
public class ParseException extends Exception {
    private final String line;
    private final int lineNumber;

    public ParseException(String line, int lineNumber, Throwable cause) {
        super(cause);
        this.line = line;
        this.lineNumber = lineNumber;
    }

    public ParseException(String line, int lineNumber, String message) {
        super(message);
        this.line = line;
        this.lineNumber = lineNumber;
    }

    public String getLine() {
        return line;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String getMessage() {
        return "Error at line " + getLineNumber() + ": " + getLine() + "\n" + super.getMessage();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
