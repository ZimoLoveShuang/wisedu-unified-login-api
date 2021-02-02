package wiki.zimo.wiseduunifiedloginapi.process;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import wiki.zimo.wiseduunifiedloginapi.builder.CasLoginEntityBuilder;
import wiki.zimo.wiseduunifiedloginapi.entity.CasLoginEntity;
import wiki.zimo.wiseduunifiedloginapi.helper.RSAHelper;
import wiki.zimo.wiseduunifiedloginapi.trust.HttpsUrlValidator;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 淮阴师范学院认证
 */
public class HytcCasLoginProcess {
    private CasLoginEntity loginEntity;
    private Map<String, String> params;

    public HytcCasLoginProcess(String loginUrl, Map<String, String> params) {
        this.loginEntity = new CasLoginEntityBuilder()
                .loginUrl(loginUrl)
                .build();
        this.params = params;
    }

    public Map<String, String> login() throws Exception {

        // 忽略证书错误
        HttpsUrlValidator.retrieveResponseFromServer(loginEntity.getLoginUrl());

        // 请求登陆页
        Connection con = Jsoup.connect(loginEntity.getLoginUrl())
                .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1")
                .followRedirects(true);
        Connection.Response res = con.execute();

        loginEntity.setLoginUrl(res.url().toString());

        // 解析登陆页
        Document doc = res.parse();
//        System.out.println(doc);

        // 全局cookie
        Map<String, String> cookies = res.cookies();

        // 获取登陆表单
        Element form = doc.getElementById("fm1");
//        System.out.println(form);
        if (form == null) {
            throw new RuntimeException("网页中没有找到fm1，请联系开发者！！！");
        }

        // 获取登陆表单里的输入
        Elements inputs = form.getElementsByTag("input");


        String username = this.params.get("username");
        String password = this.params.get("password");

        // 构造post请求参数
        Map<String, String> params = new HashMap<>();
        for (Element e : inputs) {

            // 填充用户名
            if (e.attr("name").equals("username")) {
                e.attr("value", username);
            }

            // 填充密码
            if (e.attr("name").equals("password")) {
                e.attr("value", RSAHelper.encrypt4(password));
            }

            // 排除空值表单属性
            if (e.attr("name").length() > 0) {
                // 排除记住我
                if (e.attr("name").equals("rememberMe")) {
                    continue;
                }
                params.put(e.attr("name"), e.attr("value"));
            }
        }

//        System.out.println("登陆参数 " + params);

        // 构造请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Cache-Control", "max-age=0");
        headers.put("Connection", "keep-alive");
        headers.put("Host", new URL(loginEntity.getLoginUrl()).getHost());
        headers.put("Upgrade-Insecure-Requests", "1");
        headers.put("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1");

        // 不需要验证码过程，或者说验证码过程有设计缺陷，根本不会触发，所以直接模拟登陆
        return casSendLoginData(loginEntity.getLoginUrl(), cookies, params);
    }

    /**
     * cas发送登陆请求，返回cookies
     *
     * @param login_url
     * @param cookies
     * @param params
     * @return
     * @throws Exception
     */
    private Map<String, String> casSendLoginData(String login_url, Map<String, String> cookies, Map<String, String> params) throws Exception {
        Connection con = Jsoup.connect(login_url);
//        System.out.println(login_url);
        Connection.Response login = con.ignoreContentType(true).followRedirects(false).method(Connection.Method.POST).data(params).cookies(cookies).execute();
//        System.out.println(params);
//        System.out.println(login.statusCode());
        if (login.statusCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
            // 重定向代表登陆成功
            // 更新cookie
            cookies.putAll(login.cookies());
            // 拿到重定向的地址
            String location = login.header("location");
//            System.out.println(location);
            con = Jsoup.connect(location)
                    .ignoreContentType(true)
                    .followRedirects(true)
                    .method(Connection.Method.POST)
                    .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1")
                    .cookies(cookies);
            // 请求，再次更新cookie
            login = con.execute();
            cookies.putAll(login.cookies());
            // 只有登陆成功才返回cookie
            return cookies;
        } else if (login.statusCode() == HttpURLConnection.HTTP_OK) {
            // 登陆失败
            Document doc = login.parse();
            Element msg = doc.getElementById("msg");
//            System.out.println(msg);
            throw new RuntimeException(msg.text());
        } else {
            // 服务器可能出错
            throw new RuntimeException("教务系统服务器可能出错了，Http状态码是：" + login.statusCode());
        }
    }
}