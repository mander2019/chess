package dataaccess;

import service.ClearResponse;
import service.RegisterRequest;
import service.RegisterResponse;

public interface DAO {
    RegisterResponse registerUser(RegisterRequest registerData) throws Exception;
//    LoginResponse loginUser(LoginRequest loginData);
//    LogoutResponse logoutUser(LogoutRequest logoutData);
    ClearResponse clear();




    String getUser(String authToken);
    void deleteUser(String userName);
    String createAuthToken(String username);
    String getAuthData(String username);
    void deleteAuthData(String username);

}
