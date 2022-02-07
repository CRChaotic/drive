package exception;

public class InvalidUserInfoException extends RuntimeException{
    public InvalidUserInfoException() {
        super("Invalid user info");
    }
}
