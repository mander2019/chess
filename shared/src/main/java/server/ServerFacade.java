package server;

import com.google.gson.Gson;
import service.request.*;
import service.response.*;
import exception.*;

import java.io.*;
import java.net.*;

public class ServerFacade {
    private final String serverUrl;
    private Server server;

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
        server = new Server();
        server.run(8080);
    }

    public Object register(String username, String password, String email) throws ResponseException {
        var path = "/register";
        var body = new Gson().toJson(new RegisterRequest(username, password, email));
        return this.makeRequest("POST", path, body, RegisterResponse.class);
    }

    private <T> T makeRequest(String method, String path, String body, Class<T> responseClass) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            writeBody(body, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private static void writeBody(String body, HttpURLConnection http) throws IOException {
        if (body != null) {
            try (var os = http.getOutputStream()) {
                var input = body.getBytes();
                os.write(input, 0, input.length);
            }
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }

        return response;
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            throw new ResponseException(status, "failure: " + status);
        }
    }

    private boolean isSuccessful(int status) {
        return status == 200;
    }
}
