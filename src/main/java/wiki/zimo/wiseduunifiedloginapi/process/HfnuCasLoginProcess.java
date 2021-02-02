package wiki.zimo.wiseduunifiedloginapi.process;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import wiki.zimo.wiseduunifiedloginapi.builder.CasLoginEntityBuilder;
import wiki.zimo.wiseduunifiedloginapi.entity.CasLoginEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * 合肥师范学院认证
 */
public class HfnuCasLoginProcess {
    private CasLoginEntity loginEntity;
    private Map<String, String> params;

    public HfnuCasLoginProcess(String loginUrl, Map<String, String> params) {
        this.loginEntity = new CasLoginEntityBuilder()
                .loginUrl(loginUrl)
                .build();
        loginEntity.setNeedcaptchaUrl(null);
        loginEntity.setCaptchaUrl(null);
        this.params = params;
    }

    public Map<String, String> login() throws Exception {
        // 1. 认证
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        headers.put("Connection", "keep-alive");
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("Host", "ehall.hfnu.edu.cn");
        headers.put("Origin", "http://ehall.hfnu.edu.cn");
        headers.put("Referer", "http://ehall.hfnu.edu.cn/amp-auth-adapter/mobile/auth");
        headers.put("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1 Edg/87.0.4280.88");
        headers.put("X-Requested-With", "XMLHttpRequest");
        Map<String, String> data = new HashMap<>();
        data.put("userId", params.get("username"));
        data.put("password", params.get("password"));
        Connection.Response res = Jsoup.connect("http://ehall.hfnu.edu.cn/amp-auth-adapter/mobile/auth")
                .headers(headers)
                .method(Connection.Method.POST)
                .data(data)
                .ignoreContentType(true)
                .execute();
        Map<String, String> cookies = res.cookies();

        Document doc = res.parse();
        JSONObject jsonObject = JSON.parseObject(doc.body().text());
        String result = jsonObject.getString("result");
        if (!result.equals("success")) {
            throw new RuntimeException(jsonObject.getString("message"));
        }

        String redirectUrl = jsonObject.getJSONObject("data").getString("redirectUrl");

        res = Jsoup.connect("http://ehall.hfnu.edu.cn/favicon.ico")
                .ignoreContentType(true)
                .followRedirects(true)
                .cookies(cookies)
                .method(Connection.Method.GET)
                .execute();
        cookies.putAll(res.cookies());

        // 2. 携带上一步认证成功的cookie，请求今日校园需要的cookie
        res = Jsoup.connect(loginEntity.getLoginUrl())
                .ignoreContentType(true)
                .followRedirects(true)
                .cookies(cookies)
                .method(Connection.Method.GET)
                .execute();
        cookies.putAll(res.cookies());

        return cookies;
    }
}
