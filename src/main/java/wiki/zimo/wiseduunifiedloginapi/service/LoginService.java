package wiki.zimo.wiseduunifiedloginapi.service;

import java.util.Map;

public interface LoginService {
    /**
     * 登陆
     * @param login_url 登陆接口
     * @param username 用户名
     * @param password 密码
     * @return 返回登陆后的cookie
     * @throws Exception 异常
     */
    Map<String, String> login(String login_url, String username, String password) throws Exception;

    /**
     * 登陆
     * @param username
     * @param password
     * @return
     * @throws Exception
     */
    Map<String, String> login(String username, String password) throws Exception;
}
