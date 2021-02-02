package wiki.zimo.wiseduunifiedloginapi.process;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import net.sourceforge.tess4j.TesseractException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import wiki.zimo.wiseduunifiedloginapi.builder.CasLoginEntityBuilder;
import wiki.zimo.wiseduunifiedloginapi.entity.CasLoginEntity;
import wiki.zimo.wiseduunifiedloginapi.helper.ImageHelper;
import wiki.zimo.wiseduunifiedloginapi.helper.RSAHelper;
import wiki.zimo.wiseduunifiedloginapi.helper.TesseractOCRHelper;
import wiki.zimo.wiseduunifiedloginapi.helper.TimeStampHelper;
import wiki.zimo.wiseduunifiedloginapi.trust.HttpsUrlValidator;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * 山东城市建设职业学院认证
 */
public class SducCasLoginProcess {

    private CasLoginEntity loginEntity;
    private Map<String, String> params;

    public SducCasLoginProcess(String loginUrl, Map<String, String> params) {
        this.loginEntity = new CasLoginEntityBuilder()
                .loginUrl(loginUrl)
                .build();
        this.params = params;
    }

    public Map<String, String> login() throws Exception {

        // 忽略证书错误
        HttpsUrlValidator.retrieveResponseFromServer(loginEntity.getLoginUrl());

        // 请求登陆页
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", "ehall.sduc.edu.cn");
        headers.put("Connection", "keep-alive");
        headers.put("Upgrade-Insecure-Requests", "1");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.193 Safari/537.36");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9");
        headers.put("Cookie", "amp.locale=zh_CN");
        Connection con = Jsoup.connect(loginEntity.getLoginUrl())
//                .ignoreContentType(true)
                .headers(headers)
                .method(Connection.Method.GET)
                .followRedirects(false);
        Connection.Response res = con.execute();

        // 更新loginUrl
        loginEntity.setLoginUrl(res.header("location"));
//        System.out.println(loginEntity.getLoginUrl());

        // 重定向
        headers.clear();
        headers.put("Host", "cas.sduc.edu.cn");
        headers.put("Connection", "keep-alive");
        headers.put("Upgrade-Insecure-Requests", "1");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.193 Safari/537.36");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        headers.put("Sec-Fetch-Site", "none");
        headers.put("Sec-Fetch-Mode", "navigate");
        headers.put("Sec-Fetch-User", "?1");
        headers.put("Sec-Fetch-Dest", "document");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9");
        con = Jsoup.connect(loginEntity.getLoginUrl())
                .ignoreContentType(true)
                .headers(headers)
                .method(Connection.Method.GET);
        res = con.execute();

        // 认证loginUserToken
        String loginUserToken = RSAHelper.encrypt3("lyasp" + (System.currentTimeMillis() / 1000 * 1000));
        headers.clear();
        headers.put("Host", "cas.sduc.edu.cn");
        headers.put("Connection", "keep-alive");
        headers.put("Accept", "application/json, text/plain, */*");
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.193 Safari/537.36");
        headers.put("loginUserToken", loginUserToken);
        headers.put("Sec-Fetch-Site", "same-origin");
        headers.put("Sec-Fetch-Mode", "cors");
        headers.put("Sec-Fetch-Dest", "empty");
        headers.put("Referer", loginEntity.getLoginUrl());
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9");
        String timeSeconds = TimeStampHelper.currentTimeSeconds();
        String url = "https://cas.sduc.edu.cn/lyuapServer/loginType?_t=" + timeSeconds;
//        System.out.println(url);
        Jsoup.connect(url)
                .headers(headers)
                .ignoreContentType(true)
                .get();

        // 获取cookie
        url = "https://cas.sduc.edu.cn/api/uap/unauthorize/pageInfo?_t=" + timeSeconds;
//        System.out.println(url);
        con = Jsoup.connect(url)
                .ignoreContentType(true)
                .headers(headers)
                .followRedirects(false)
                .method(Connection.Method.GET);
        res = con.execute();

        // 全局cookie
        Map<String, String> cookies = res.cookies();
//        System.out.println(cookies);


        // 请求验证码
        url = "https://cas.sduc.edu.cn/lyuapServer/kaptcha?_t=" + TimeStampHelper.currentTimeSeconds() + "&uid=";
//        System.out.println(url);
        Document doc = Jsoup.connect(url)
                .ignoreContentType(true)
                .headers(headers)
                .cookies(cookies)
                .get();
        JSONObject jsonObject = JSON.parseObject(doc.body().text());
        String uid = jsonObject.getString("uid");

        // 识别验证码后模拟登陆，最多尝试20次
        int time = TesseractOCRHelper.MAX_TRY_TIMES;
        while (time-- > 0) {
            String code = ocrCaptcha(headers, cookies, url + uid);
//            System.out.println(code);

            // 校验验证码，实际上不需要校验验证码
            Map<String, String> map = new HashMap<>();
            map.put("id", uid);
            map.put("code", code);
            headers.clear();
            headers.put("Host", "cas.sduc.edu.cn");
            headers.put("Connection", "keep-alive");
            headers.put("Accept", "application/json, text/plain, */*");
            headers.put("X-Requested-With", "XMLHttpRequest");
            headers.put("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1");
            headers.put("loginUserToken", loginUserToken);
            headers.put("Content-Type", "application/json;charset=UTF-8");
            headers.put("Origin", "https://cas.sduc.edu.cn");
            headers.put("Sec-Fetch-Site", "same-origin");
            headers.put("Sec-Fetch-Mode", "cors");
            headers.put("Sec-Fetch-Dest", "empty");
            headers.put("Referer", loginEntity.getLoginUrl());
            headers.put("Accept-Encoding", "gzip, deflate, br");
            headers.put("Accept-Language", "zh-CN,zh;q=0.9");
            url = "https://cas.sduc.edu.cn/lyuapServer/validateLoginCode";
//            System.out.println(url);
            doc = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .cookies(cookies)
                    .headers(headers)
                    .requestBody(JSON.toJSONString(map).toString())
                    .post();
            // 解析校验结果
            jsonObject = JSON.parseObject(doc.body().text());
            Boolean ok = jsonObject.getJSONObject("meta").getBoolean("success");
            if (ok) {
//                System.out.println("校验验证码成功");
                // 获取tickets
                // 构造登陆参数
                params.put("password", RSAHelper.encrypt3(params.get("password")));
                params.put("service", loginEntity.getLoginUrl());
                params.put("loginType", "");

                headers.clear();
                headers.put("Host", "cas.sduc.edu.cn");
                headers.put("Connection", "keep-alive");
                headers.put("Accept", "application/json, text/plain, */*");
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.193 Safari/537.36");
                headers.put("loginUserToken", loginUserToken);
                headers.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                headers.put("Origin", "https://cas.sduc.edu.cn");
                headers.put("Sec-Fetch-Site", "same-origin");
                headers.put("Sec-Fetch-Mode", "cors");
                headers.put("Sec-Fetch-Dest", "empty");
                headers.put("Referer", loginEntity.getLoginUrl());
                headers.put("Accept-Encoding", "gzip, deflate, br");
                headers.put("Accept-Language", "zh-CN,zh;q=0.9");
                url = "https://cas.sduc.edu.cn/lyuapServer/v1/tickets";
//                System.out.println(url);
                // 发起登陆请求
                return casSendLoginData(url, headers, cookies, params);
            }
        }
        // 执行到这里就代表验证码识别尝试已经达到了最大的次数
        throw new RuntimeException("验证码识别错误，请重试");
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
    private Map<String, String> casSendLoginData(String login_url, Map<String, String> headers, Map<String, String> cookies, Map<String, String> params) throws Exception {
        String body = String.format("username=%s&password=%s&service=%s&loginType=%s", params.get("username"), params.get("password"), params.get("service"), params.get("loginType"));
//        System.out.println(body);
        Connection con = Jsoup.connect(login_url);
//        System.out.println(login_url);
        Connection.Response login = con.ignoreContentType(true)
                .followRedirects(false)
                .method(Connection.Method.POST)
                .requestBody(body)
                .cookies(cookies)
                .headers(headers)
                .execute();
        if (login.statusCode() == HttpURLConnection.HTTP_CREATED) {
            String location = login.header("location");
//            System.out.println(location);
            body = "service=" + loginEntity.getLoginUrl().substring("https://cas.sduc.edu.cn/lyuapServer/login?service=".length());
//            System.out.println(body);
            cookies.put("loginType", "1");
            cookies.put("session", "1");
            Document doc = Jsoup.connect(location)
                    .ignoreContentType(true)
                    .followRedirects(true)
                    .method(Connection.Method.POST)
                    .headers(headers)
                    .requestBody(body)
                    .cookies(cookies)
                    .post();
            String ticket = doc.body().text();
//            System.out.println(ticket);
            String sessionToken = loginEntity.getLoginUrl().substring(loginEntity.getLoginUrl().length() - 32);
//            System.out.println(sessionToken);
            String url = "http://ehall.sduc.edu.cn/amp-auth-adapter/loginSuccess?sessionToken=" + sessionToken + "&ticket=" + ticket;
//            System.out.println(url);
            headers.clear();
            headers.put("Host", "ehall.sduc.edu.cn");
            headers.put("Connection", "keep-alive");
            headers.put("Upgrade-Insecure-Requests", "1");
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.193 Safari/537.36");
            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            headers.put("Accept-Encoding", "gzip, deflate");
            headers.put("Accept-Language", "zh-CN,zh;q=0.9");
            headers.put("Cookie", "amp.locale=zh_CN");
            Connection.Response res = Jsoup.connect(url)
                    .followRedirects(false)
                    .method(Connection.Method.GET)
                    .headers(headers)
                    .execute();
            if (res.statusCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
//                System.out.println("登陆成功");
                location = res.header("location");
//                System.out.println(location);
                headers.clear();
                headers.put("Host", "sdcsjs.campusphere.net");
                headers.put("Connection", "keep-alive");
                headers.put("Upgrade-Insecure-Requests", "1");
                headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.193 Safari/537.36");
                headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
                headers.put("Sec-Fetch-Site", "cross-site");
                headers.put("Sec-Fetch-Mode", "navigate");
                headers.put("Sec-Fetch-User", "?1");
                headers.put("Sec-Fetch-Dest", "document");
                headers.put("Accept-Encoding", "gzip, deflate, br");
                headers.put("Accept-Language", "zh-CN,zh;q=0.9");
                res = Jsoup.connect(location)
                        .headers(headers)
                        .followRedirects(true)
                        .method(Connection.Method.GET)
                        .execute();
                cookies.clear();
                cookies.putAll(res.cookies());
//                System.out.println(cookies);
                return cookies;
            }
        }
        throw new RuntimeException("模拟登陆失败，原因是教务系统登陆过程可能更改了");
    }

    /**
     * 处理验证码识别
     *
     * @param cookies
     * @param captcha_url
     * @return
     * @throws IOException
     * @throws TesseractException
     */
    private String ocrCaptcha(Map<String, String> headers, Map<String, String> cookies, String captcha_url) throws IOException, TesseractException {
        while (true) {
            String filePach = System.getProperty("user.dir") + File.separator + System.currentTimeMillis() + ".jpg";
//            System.out.println(filePach);
//            System.out.println(captcha_url);
            Document doc = Jsoup.connect(captcha_url)
                    .ignoreContentType(true)
                    .headers(headers)
                    .cookies(cookies)
                    .get();
            JSONObject jsonObject = JSON.parseObject(doc.body().text());
//            System.out.println(jsonObject.getString("uid"));
            String base64 = jsonObject.getString("content");

            // 五位验证码，背景有噪点
            ImageHelper.saveImageFile(ImageHelper.binaryzation(ImageHelper.base64ToBufferedImage(base64)), filePach);
            String s = TesseractOCRHelper.doOcr(filePach);

            File temp = new File(filePach);
            temp.delete();

            if (judge(s, 5)) {
//                System.out.println(s);
                int a = s.charAt(0) - '0';
                int b = s.charAt(2) - '0';
                switch (s.charAt(1)) {
                    case '+':
                        return String.valueOf(a + b);
                    case '-':
                        return String.valueOf(a - b);
                    case '*':
                        return String.valueOf(a * b);
                    case '/':
                        return String.valueOf(a / b);
                }
            }
        }
    }

    /**
     * 判断ocr识别出来的结果是否符合条件
     *
     * @param s
     * @param len
     * @return
     */
    private boolean judge(String s, int len) {
        if (s == null || s.length() != len) {
            return false;
        }

        if (!s.matches("[0-9](\\+|-|\\*|/)[0-9]=\\?")) {
            return false;
        }

        return true;
    }
}
