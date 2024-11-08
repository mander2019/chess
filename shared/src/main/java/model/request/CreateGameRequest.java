package model.request;

public record CreateGameRequest(String authToken, String gameName) {
}
