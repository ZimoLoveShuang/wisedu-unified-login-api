package wiki.zimo.wiseduunifiedloginapi.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import wiki.zimo.wiseduunifiedloginapi.helper.AESHelper;
import wiki.zimo.wiseduunifiedloginapi.helper.ImageHelper;
import wiki.zimo.wiseduunifiedloginapi.helper.TesseractOCRHelper;
import wiki.zimo.wiseduunifiedloginapi.service.LoginService;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

@Service
public class LoginServiceImpl implements LoginService {

    @Value("${LOGIN_API}")
    private String LOGIN_API;// 登陆接口
    @Value("${NEEDCAPTCHA_API}")
    private String NEEDCAPTCHA_API;// 是否需要验证码接口
    @Value("${CAPTCHA_API}")
    private String CAPTCHA_API;// 获取验证码接口

    @Override
    public Map<String, String> login(String login_url, String needcaptcha_url, String captcha_url, String username, String password) throws Exception {
        if (StringUtils.isEmpty(login_url)) {
            login_url = LOGIN_API;
        }

        if (StringUtils.isEmpty(needcaptcha_url)) {
            needcaptcha_url = NEEDCAPTCHA_API;
        }

        if (StringUtils.isEmpty(captcha_url)) {
            captcha_url = CAPTCHA_API;
        }

        if (StringUtils.isAllBlank(username) || StringUtils.isAllBlank(password)) {
            throw new RuntimeException("用户名或者密码为空");
        }

        // 根据login_url判断类型
        if (login_url.trim().contains("/iap")) {
            return iapLogin(login_url, needcaptcha_url, captcha_url, username, password);
        } else {
            return casLogin(login_url, needcaptcha_url, captcha_url, username, password);
        }
    }

    /**
     * 金智统一iap登陆
     *
     * @param login_url
     * @param needcaptcha_url
     * @param captcha_url
     * @param username
     * @param password
     * @return
     */
    private Map<String, String> iapLogin(String login_url, String needcaptcha_url, String captcha_url, String username, String password) throws Exception {
        // 请求登陆页
        Connection con = Jsoup.connect(login_url).followRedirects(true);
        Connection.Response res = con.execute();

        // 构造请求头
        Map<String, String> headers = new HashMap<>();
        String reffer = res.url().toString();
//        System.out.println("reffer:" + reffer);
        String host = reffer.substring((res.url().getProtocol() + "://").length(), reffer.indexOf("/iap"));
//        System.out.println("host:" + host);
        String origin = reffer.substring(0, reffer.indexOf("/iap"));
//        System.out.println("origin:" + origin);
        headers.put("Host", host);
        headers.put("Connection", "keep-alive");
        headers.put("Accept", "application/json, text/plain, */*");
        headers.put("X-Requested-With", "XMLHttpRequest");
//        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.61 Safari/537.36");
        headers.put("User-Agent", "Mozilla/5.0 (Linux; Android 4.4.4; OPPO R11 Plus Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 Safari/537.36");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Origin", origin);
//        headers.put("Sec-Fetch-Site", "same-origin");
//        headers.put("Sec-Fetch-Mode", "cors");
//        headers.put("Sec-Fetch-Dest", "empty");
        headers.put("Referer", res.url().toString());
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9");

        // 全局cookie
        Map<String, String> cookies = res.cookies();

        // 构造请求参数
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        params.put("rememberMe", String.valueOf(false));
        params.put("mobile", "");
        params.put("dllt", "");

        // 申请It
        String itUrl = login_url.substring(0, login_url.lastIndexOf("/")) + "/security/lt";
//        System.out.println(itUrl);
        Document doc = Jsoup.connect(itUrl).ignoreContentType(true).cookies(cookies).post();
//        System.out.println(doc);
        JSONObject jsonObject = JSON.parseObject(doc.body().text());
        if (jsonObject.getInteger("code") != 200) {
            throw new RuntimeException("申请It失败");
        }
        JSONObject result = jsonObject.getJSONObject("result");
        String it = result.getString("_lt");
        params.put("lt", it);

        // 拿到encryptSalt
        String encryptSalt = result.getString("_encryptSalt");
        // 密码暂时不需要加密
//        params.put("password", AESHelper.encryptAES(password, encryptSalt));

        // 登陆地址处理
        login_url = login_url.substring(0, login_url.lastIndexOf("/")) + "/doLogin";

//        System.out.println(login_url);
//        System.out.println(params);

        // 模拟登陆之前首先请求是否需要验证码接口
        doc = Jsoup.connect(needcaptcha_url + "?username=" + username).cookies(cookies).ignoreContentType(true).get();
        Boolean needCaptcha = Boolean.valueOf(doc.body().text());
        if (needCaptcha) {
            // 验证码处理，最多尝试10次
            int time = 10;
            while (time-- > 0) {
                // 识别验证码
                String code = ocrCaptcha(cookies, captcha_url + "?ltId=" + it);
                params.put("captcha", code);

                // 模拟登陆
                return iapSendLoginData(login_url, headers, cookies, params);
            }
        } else {
            params.put("captcha", "");

            // 模拟登陆
            return iapSendLoginData(login_url, headers, cookies, params);
        }

        return null;
    }

    /**
     * iap发送登陆请求，返回cookies
     *
     * @param login_url
     * @param headers
     * @param cookies
     * @param params
     * @return
     * @throws Exception
     */
    private Map<String, String> iapSendLoginData(String login_url, Map<String, String> headers, Map<String, String> cookies, Map<String, String> params) throws Exception {
        try {
            Connection con = Jsoup.connect(login_url).headers(headers).ignoreContentType(true).followRedirects(false).cookies(cookies).data(params).method(Connection.Method.POST);
            Connection.Response res = con.execute();
            // 更新cookie
            cookies.putAll(res.cookies());
            // 修复新乡医学院等iap登陆方式可能被多次重定向的问题
            String body = res.body();
            if (body.contains("307")) {
                String location = res.headers().get("Location");
//                System.out.println(location);
                res = Jsoup.connect(location).headers(headers).ignoreContentType(true).followRedirects(true).cookies(cookies).data(params).method(Connection.Method.POST).execute();
//                System.out.println(res.headers());
//                System.out.println(res.cookies());
//                System.out.println(res.body());
                // 更新cookies
                cookies.putAll(res.cookies());
                // 更新body
                body = res.body();
            }
            // 然后就是正常流程
            JSONObject jsonObject = JSON.parseObject(body);
            String resultCode = jsonObject.getString("resultCode");
            if (!resultCode.equals("REDIRECT")) {
                throw new RuntimeException("用户名或密码错误");
            }
            // 第一次重定向，手动重定向
            String url = headers.get("Origin") + jsonObject.getString("url");
            // 后面会有多次重定向，所以开启自动重定向
            res = Jsoup.connect(url).cookies(cookies).followRedirects(true).ignoreContentType(true).execute();
            // 再次更新cookie，防爬策略：每个页面一个cookie
            cookies.putAll(res.cookies());
            // 登陆成功
            return cookies;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 金智统一cas登陆
     *
     * @param login_url
     * @param needcaptcha_url
     * @param captcha_url
     * @param username
     * @param password
     * @return
     * @throws Exception
     */
    private Map<String, String> casLogin(String login_url, String needcaptcha_url, String captcha_url, String username, String password) throws Exception {
        // 请求登陆页
        Connection con = Jsoup.connect(login_url).followRedirects(true);
        Connection.Response res = con.execute();

        // 解析登陆页
        Document doc = res.parse();

        // 全局cookie
        Map<String, String> cookies = res.cookies();

        // 获取登陆表单
        Element form = doc.getElementById("casLoginForm");
        // 处理加密的盐
        Element saltElement = doc.getElementById("pwdDefaultEncryptSalt");
        String salt = null;
        if (saltElement != null) {
            salt = saltElement.val();
        }

//        System.out.println("盐是 " + salt);

        // 获取登陆表单里的输入
        Elements inputs = form.getElementsByTag("input");

        // 获取post请求参数
        Map<String, String> params = new HashMap<>();
        for (Element e : inputs) {

            // 填充用户名
            if (e.attr("name").equals("username")) {
                e.attr("value", username);
            }

            // 填充密码
            if (e.attr("name").equals("password")) {
                if (salt != null) {
                    e.attr("value", AESHelper.encryptAES(password, salt));
                } else {
                    e.attr("value", password);
                }
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

        // 模拟登陆之前首先请求是否需要验证码接口
        doc = Jsoup.connect(needcaptcha_url + "?username=" + username).cookies(cookies).get();
        boolean isNeedCaptcha = Boolean.valueOf(doc.body().text());
        if (isNeedCaptcha) {
            // 识别验证码后模拟登陆，最多尝试10次
            int time = 10;
            while (time-- > 0) {
                String code = ocrCaptcha(cookies, captcha_url);
                params.put("captchaResponse", code);

                // 模拟登陆
                return casSendLoginData(login_url, cookies, params);
            }
        } else {
            // 直接模拟登陆
            return casSendLoginData(login_url, cookies, params);
        }
        return null;
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
        Connection.Response login = con.ignoreContentType(true).followRedirects(false).method(Connection.Method.POST).data(params).cookies(cookies).execute();
        if (login.statusCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
            // 重定向代表登陆成功
            // 更新cookie
            cookies.putAll(login.cookies());
            // 拿到重定向的地址
            String location = login.header("location");
//            System.out.println(location);
            con = Jsoup.connect(location).ignoreContentType(true).followRedirects(true).method(Connection.Method.POST).cookies(cookies);
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
            if (msg.text().equals("您提供的用户名或者密码有误")) {
                throw new RuntimeException("用户名或者密码错误");
            }
        } else {
            // 服务器可能出错
            throw new RuntimeException("教务系统服务器可能出错了，Http状态码是：" + login.statusCode());
        }
        return null;
    }


    @Override
    public Map<String, String> login(String username, String password) throws Exception {
        return login(null, null, null, username, password);
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
    private String ocrCaptcha(Map<String, String> cookies, String captcha_url) throws IOException, TesseractException {
        while (true) {
            String filePach = System.getProperty("user.dir") + File.separator + System.currentTimeMillis() + ".jpg";
//            System.out.println(filePach);
            Connection.Response response = Jsoup.connect(captcha_url).cookies(cookies).ignoreContentType(true).execute();
            if (captcha_url.contains("ltId=")) {
                // 五位验证码，背景没有噪点
                ImageHelper.saveImageFile(response.bodyStream(), filePach);
                String s = TesseractOCRHelper.doOcr(filePach);

                File temp = new File(filePach);
                temp.delete();
                if (judge(s, 5)) {
                    return s;
                }
            } else {
                // 四位验证码，背景有噪点
                ImageHelper.saveImageFile(ImageHelper.binaryzation(response.bodyStream()), filePach);
                String s = TesseractOCRHelper.doOcr(filePach);

                File temp = new File(filePach);
                temp.delete();
                if (judge(s, 4)) {
                    return s;
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

        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (!(Character.isDigit(ch) || Character.isLetter(ch))) {
                return false;
            }
        }

        return true;
    }
}
