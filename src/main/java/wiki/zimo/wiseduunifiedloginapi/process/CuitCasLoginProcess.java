package wiki.zimo.wiseduunifiedloginapi.process;

import net.sourceforge.tess4j.TesseractException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import wiki.zimo.wiseduunifiedloginapi.builder.CasLoginEntityBuilder;
import wiki.zimo.wiseduunifiedloginapi.entity.CasLoginEntity;
import wiki.zimo.wiseduunifiedloginapi.helper.ImageHelper;
import wiki.zimo.wiseduunifiedloginapi.helper.TesseractOCRHelper;
import wiki.zimo.wiseduunifiedloginapi.trust.HttpsUrlValidator;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * 成都信息工程大学认证
 */
public class CuitCasLoginProcess {
    private CasLoginEntity loginEntity;
    private Map<String, String> params;

    public CuitCasLoginProcess(String loginUrl, Map<String, String> params) {
        this.loginEntity = new CasLoginEntityBuilder()
                .loginUrl(loginUrl)
                .build();
        loginEntity.setCaptchaUrl("https://sso.cuit.edu.cn/authserver/captcha");
        this.params = params;
    }

    public Map<String, String> login() throws Exception {

        // 忽略证书错误
        HttpsUrlValidator.retrieveResponseFromServer(loginEntity.getLoginUrl());

        // 构造请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9");
        headers.put("Cache-Control", "max-age=0");
        headers.put("Connection", "keep-alive");
        headers.put("Host", "authserver.cumt.edu.cn");
        headers.put("Upgrade-Insecure-Requests", "1");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.125 Safari/537.36");


        // 请求登陆页
        Connection con = Jsoup.connect(loginEntity.getLoginUrl())
                .followRedirects(true)
                .headers(headers);
        Connection.Response res = con.execute();
//        System.out.println(res.url());

        // 更新login_url
        loginEntity.setLoginUrl(res.url().toString());
        // 全局cookie
        Map<String, String> cookies = res.cookies();

        // 解析登陆页
        Document doc = Jsoup.connect(loginEntity.getLoginUrl())
                .followRedirects(true)
                .headers(headers)
                .cookies(cookies)
                .get();

//        System.out.println(doc);

        // 获取登陆表单
        Element form = doc.getElementById("fm1");

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
            // 排除空值表单属性
            if (e.attr("name").length() > 0) {
                // 排除记住我
                if (e.attr("name").equals("rememberMe")) {
                    continue;
                }
                params.put(e.attr("name"), e.attr("value"));
            }
        }

        params.put("username", username);
        params.put("password", password);

//        System.out.println("登陆参数 " + params);

        // 模拟登陆之前首先请求是否需要验证码接口
        boolean isNeedCaptcha = true;
//        System.out.println(isNeedCaptcha);
        if (isNeedCaptcha) {
            // 识别验证码后模拟登陆，最多尝试20次
            int time = TesseractOCRHelper.MAX_TRY_TIMES;
            while (time-- > 0) {
                String code = ocrCaptcha(cookies, headers, loginEntity.getCaptchaUrl());
//                System.out.println(code);
                params.put("captcha", code);
//                System.out.println("登陆参数 " + params);
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
//        System.out.println(login_url);
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9");
        headers.put("Cache-Control", "max-age=0");
        headers.put("Connection", "keep-alive");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Host", "sso.cuit.edu.cn");
        headers.put("Origin", "https://sso.cuit.edu.cn");
        headers.put("Referer", "https://sso.cuit.edu.cn/authserver/login?service=http%3A%2F%2Fjwgl.cuit.edu.cn%2Feams%2Flogin.action");
        headers.put("Upgrade-Insecure-Requests", "1");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.125 Safari/537.36");

//        System.out.println("headers " + headers);
//        System.out.println("cookies " + cookies);

        Connection con = Jsoup.connect(login_url);
//        System.out.println(login_url);
        Connection.Response login = con.headers(headers)
                .followRedirects(false)
                .method(Connection.Method.POST)
                .data(params)
                .cookies(cookies)
                .execute();
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
                    .cookies(cookies);
            // 请求，再次更新cookie
            login = con.execute();
            cookies.putAll(login.cookies());
            // 只有登陆成功才返回cookie
            return cookies;
        } else if (login.statusCode() == HttpURLConnection.HTTP_OK) {
            // 登陆失败
            Document doc = login.parse();
//            System.out.println(doc);
            Element msg = doc.getElementsByClass("textError").get(0);
//            System.out.println(msg);
            if (!msg.text().equals("验证码无效")) {
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
                    .headers(headers)
                    .cookies(cookies)
                    .ignoreContentType(true)
                    .execute();

            // 四位验证码，背景没有噪点，但存在黑色干扰线
            ImageHelper.saveImageFile(ImageHelper.removeBlack(response.bodyStream()), filePach);
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
