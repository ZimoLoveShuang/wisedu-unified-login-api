package wiki.zimo.wiseduunifiedloginapi.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import wiki.zimo.wiseduunifiedloginapi.service.LoginService;

import java.util.HashMap;
import java.util.Map;

@RestController
@Api(description = "模拟登陆金智教务系统")
@RequestMapping("/api")
public class ApiController {
    @Autowired
    private LoginService loginService;

    @RequestMapping(value = "login", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiOperation(value = "获取登陆金智教务系统后的cookies")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "login_url", value = "金智教务系统登陆页url", paramType = "query", required = false, dataType = "String"),
            @ApiImplicitParam(name = "username", value = "学号或者工号", paramType = "query", required = true, dataType = "String"),
            @ApiImplicitParam(name = "password", value = "密码", paramType = "query", required = true, dataType = "String"),
    })
    public Map<String, Object> login(@RequestParam(value = "login_url", required = false) String login_url,
                                     @RequestParam("username") String username,
                                     @RequestParam("password") String password) {
        Map<String, Object> map = new HashMap<>();
        try {
            map.put("code", 0);
            map.put("msg", "login success!");
            Map<String, String> cookies = loginService.login(login_url, username, password);
            if (cookies == null) {
                throw new RuntimeException("登陆失败，cookies返回为null");
            }
            StringBuffer buffer = new StringBuffer();
            int size = 0;
            for (String key : cookies.keySet()) {
                if (size == cookies.size() - 1) {
                    buffer.append(key);
                    buffer.append('=');
                    buffer.append(cookies.get(key));
                } else {
                    buffer.append(key);
                    buffer.append('=');
                    buffer.append(cookies.get(key));
                    buffer.append(';');
                }
                size++;
            }
            map.put("cookies", buffer.toString());
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            map.put("code", 1);
            map.put("msg", "login failed! " + e.getMessage());
            map.put("cookies", null);
            return map;
        }
    }
}
