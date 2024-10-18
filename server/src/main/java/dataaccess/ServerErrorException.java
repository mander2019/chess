package dataaccess;

public class ServerErrorException extends RuntimeException {
    final private int statusCode;

    public ServerErrorException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int StatusCode() {
        return statusCode;
    }
}
