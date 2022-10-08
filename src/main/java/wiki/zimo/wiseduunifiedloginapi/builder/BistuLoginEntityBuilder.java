package wiki.zimo.wiseduunifiedloginapi.builder;


import wiki.zimo.wiseduunifiedloginapi.entity.*;

import java.net.*;

public class BistuLoginEntityBuilder {
    private String loginUrl;
    private String needcaptchaUrl;
    private String captchaUrl;
    private String host;
    private String protocol;

    public BistuLoginEntityBuilder loginUrl(String loginUrl) {
        URL url = null;
        try {
            url = new URL(loginUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.host = url.getHost();
        this.protocol = url.getProtocol();
        this.loginUrl = loginUrl;
        return this;
    }

    public BistuLoginEntity build() {

        this.needcaptchaUrl = protocol + "://" + host + "/authserver/checkNeedCaptcha.htl";
        this.captchaUrl = protocol + "://" + host + "/authserver/getCaptcha.htl";
        System.out.println("是否需要验证码链接：" + this.needcaptchaUrl);
        return new BistuLoginEntity(loginUrl, needcaptchaUrl, captchaUrl);
    }
}
