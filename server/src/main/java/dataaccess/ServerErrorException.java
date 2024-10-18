package dataaccess;

public class ServerErrorException extends Exception {
    final private int statusCode;

    public ServerErrorException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int StatusCode() {
        return statusCode;
    }
}
