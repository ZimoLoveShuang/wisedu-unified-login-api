package wiki.zimo.wiseduunifiedloginapi.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import wiki.zimo.wiseduunifiedloginapi.process.*;
import wiki.zimo.wiseduunifiedloginapi.service.LoginService;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoginServiceImpl implements LoginService {

    @Value("${LOGIN_API}")
    private String LOGIN_API;// 登陆接口

    @Override
    public Map<String, String> login(String login_url, String username, String password) throws Exception {
        if (StringUtils.isEmpty(login_url)) {
            login_url = LOGIN_API;
        }

        if (StringUtils.isAllBlank(username) || StringUtils.isAllBlank(password)) {
            throw new RuntimeException("用户名或者密码为空");
        }

        // 封装参数
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);

        // 根据login_url判断类型
        if (login_url.trim().contains("/iap")) {
            IapLoginProcess process = new IapLoginProcess(login_url, params);
            return process.login();
        } else if (login_url.trim().contains("cumt.edu.cn")) {
            CumtCasLoginProcess process = new CumtCasLoginProcess(login_url, params);
            return process.login();
        } else if (login_url.trim().contains("henu.edu.cn")) {
            HenuCasLoginProcess process = new HenuCasLoginProcess(login_url, params);
            return process.login();
        } else if (login_url.trim().contains("ahjzu.edu.cn")) {
            AhjzuCasLoginProcess process = new AhjzuCasLoginProcess(login_url, params);
            return process.login();
        } else if (login_url.trim().contains("kmu.edu.cn")) {
            KmuCasLoginProcess process = new KmuCasLoginProcess(login_url, params);
            return process.login();
        } else if (login_url.trim().contains("whpu.edu.cn")) {
            WhpuCasLoginProcess process = new WhpuCasLoginProcess(login_url, params);
            return process.login();
        } else if (login_url.trim().contains("sduc.edu.cn")) {
            SducCasLoginProcess process = new SducCasLoginProcess(login_url, params);
            return process.login();
        } else if (login_url.trim().contains("cuit.edu.cn")) {
            CuitCasLoginProcess process = new CuitCasLoginProcess(login_url, params);
            return process.login();
        } else if (login_url.trim().contains("hfnu.edu.cn")) {
            HfnuCasLoginProcess process = new HfnuCasLoginProcess(login_url, params);
            return process.login();
        } else if (login_url.trim().contains("hfut.edu.cn")) {
            HfutCasLoginProcess process = new HfutCasLoginProcess(login_url, params);
            return process.login();
        } else if (login_url.trim().contains("just.edu.cn")) {
            JustCasLoginProcess process = new JustCasLoginProcess(login_url, params);
            return process.login();
        } else if (login_url.trim().contains("hytc.edu.cn")) {
            HytcCasLoginProcess process = new HytcCasLoginProcess(login_url, params);
            return process.login();
        } else {
            CasLoginProcess process = new CasLoginProcess(login_url, params);
            return process.login();
        }
    }


    @Override
    public Map<String, String> login(String username, String password) throws Exception {
        return login(null, username, password);
    }

}
