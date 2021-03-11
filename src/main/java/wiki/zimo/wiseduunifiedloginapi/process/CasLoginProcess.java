package wiki.zimo.wiseduunifiedloginapi.process;

import net.sourceforge.tess4j.TesseractException;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import wiki.zimo.wiseduunifiedloginapi.builder.CasLoginEntityBuilder;
import wiki.zimo.wiseduunifiedloginapi.entity.CasLoginEntity;
import wiki.zimo.wiseduunifiedloginapi.helper.AESHelper;
import wiki.zimo.wiseduunifiedloginapi.helper.ImageHelper;
import wiki.zimo.wiseduunifiedloginapi.helper.TesseractOCRHelper;
import wiki.zimo.wiseduunifiedloginapi.trust.HttpsUrlValidator;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NOTCLOUD认证
 */
public class CasLoginProcess {
    private CasLoginEntity loginEntity;
    private Map<String, String> params;

    public CasLoginProcess(String loginUrl, Map<String, String> params) {
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

        // 解析登陆页
        Document doc = res.parse();
//        System.out.println(doc);

        // 全局cookie
        Map<String, String> cookies = res.cookies();

        // 获取登陆表单
        Element form = doc.getElementById("casLoginForm");
//        System.out.println(form);
        if (form == null) {
            throw new RuntimeException("网页中没有找到casLoginForm，请联系开发者！！！");
        }

        // 处理加密的盐
        Element saltElement = doc.getElementById("pwdDefaultEncryptSalt");

        String salt = null;
        if (saltElement != null) {
            salt = saltElement.val();
        }

        // 网页中可能不存在id为pwdDefaultEncryptSalt的元素中，但提交的密码参数仍然需要加密
        if (saltElement == null) {
            Elements scripts = doc.getElementsByTag("script");
            for (Element script : scripts) {
                if (script.data().contains("pwdDefaultEncryptSalt")) {
//                    System.out.println(script.data());
                    // 用正则表达式匹配盐
                    String pattern = "\"\\w{16}\"";
                    Pattern p = Pattern.compile(pattern);
                    Matcher m = p.matcher(script.data());
                    if (m.find()) {
                        String group = m.group();
                        salt = group.substring(1, group.length() - 1);
//                        System.out.println(group);
//                        System.out.println(salt);
                    }
                    break;
                }
            }
        }

//        System.out.println("盐是 " + salt);

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

        // 构造请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Cache-Control", "max-age=0");
        headers.put("Connection", "keep-alive");
        headers.put("Host", new URL(loginEntity.getLoginUrl()).getHost());
        headers.put("Upgrade-Insecure-Requests", "1");
        headers.put("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1");

        // 模拟登陆之前首先请求是否需要验证码接口
        doc = Jsoup.connect(loginEntity.getNeedcaptchaUrl() + "?username=" + username)
                .headers(headers)
                .cookies(cookies)
                .get();
        boolean isNeedCaptcha = Boolean.valueOf(doc.body().text());
//        System.out.println(isNeedCaptcha);
        if (isNeedCaptcha) {
            // 识别验证码后模拟登陆，最多尝试20次
            int time = TesseractOCRHelper.MAX_TRY_TIMES;
            while (time-- > 0) {
                String code = ocrCaptcha(cookies, headers, loginEntity.getCaptchaUrl());
//                System.out.println(code);
                params.put("captchaResponse", code);
                Map<String, String> cookies2 = casSendLoginData(loginEntity.getLoginUrl(), cookies, params);
                if (cookies2 != null) {
                    return cookies2;
                }
            }
            // 执行到这里就代表验证码识别尝试已经达到了最大的次数
            throw new RuntimeException("验证码识别错误，请重试");
        } else {
            // 直接模拟登陆
            return casSendLoginData(loginEntity.getLoginUrl(), cookies, params);
        }
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
            // 第一次尝试自动更新cookie
            String location = null;
            try {
                cookies.putAll(login.cookies());
                // 拿到重定向的地址
                location = login.header("location");
//                System.out.println(location);
                con = Jsoup.connect(location)
                        .ignoreContentType(true)
                        .followRedirects(true)
                        .method(Connection.Method.POST)
                        .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1")
                        .cookies(cookies);
                // 请求，再次更新cookie
                login = con.execute();
                cookies.putAll(login.cookies());
            } catch (HttpStatusException e) {
//                e.printStackTrace();
                // 第一次自动更新cookie，失败了，携带已获取到的cookie再次尝试/portal/login接口
                location = location.substring(0, location.lastIndexOf('?'));
//                System.out.println(location);
                con = Jsoup.connect(location)
                        .ignoreContentType(true)
                        .followRedirects(true)
                        .method(Connection.Method.GET)
                        .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1")
                        .cookies(cookies);
                login = con.execute();
                cookies.putAll(login.cookies());
            }
            // 只有登陆成功才返回cookie
            return cookies;
        } else if (login.statusCode() == HttpURLConnection.HTTP_OK) {
            // 登陆失败
            Document doc = login.parse();
            Element msg = doc.getElementById("msg");
//            System.out.println(msg);
            if (!msg.text().equals("无效的验证码")) {
                throw new RuntimeException(msg.text());
            }
        } else {
            // 服务器可能出错
            throw new RuntimeException("教务系统服务器可能出错了，Http状态码是：" + login.statusCode());
        }
        return null;
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
    private String ocrCaptcha(Map<String, String> cookies, Map<String, String> headers, String captcha_url) throws IOException, TesseractException {
        while (true) {
            String filePach = System.getProperty("user.dir") + File.separator + System.currentTimeMillis() + ".jpg";
//            System.out.println(filePach);
//            System.out.println(captcha_url);
            Connection.Response response = Jsoup.connect(captcha_url)
                    .headers(headers).cookies(cookies)
                    .ignoreContentType(true)
                    .execute();

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
