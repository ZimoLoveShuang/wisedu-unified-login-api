package wiki.zimo.wiseduunifiedloginapi.service;

import java.util.Map;

public interface LoginService {
    Map<String, String> login(String login_url, String username, String password) throws Exception;

    Map<String, String> login(String username, String password) throws Exception;
}
