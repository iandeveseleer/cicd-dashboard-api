package fr.iandeveseleer.cicddashboardapi.exception;

public class EventParsingException extends RuntimeException {
    public EventParsingException(String message) {
        super(message);
    }
    
    public EventParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}