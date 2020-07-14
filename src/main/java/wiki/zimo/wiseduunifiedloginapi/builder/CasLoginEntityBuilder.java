package wiki.zimo.wiseduunifiedloginapi.builder;

import wiki.zimo.wiseduunifiedloginapi.entity.CasLoginEntity;

import java.net.MalformedURLException;
import java.net.URL;

public class CasLoginEntityBuilder {
    private String loginUrl;
    private String needcaptchaUrl;
    private String captchaUrl;
    private String host;
    private String protocol;

    public CasLoginEntityBuilder loginUrl(String loginUrl) {
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

    public CasLoginEntity build() {
        this.needcaptchaUrl = protocol + "://" + host + "/authserver/needCaptcha.html";
        this.captchaUrl = protocol + "://" + host + "/authserver/captcha.html";
        return new CasLoginEntity(loginUrl, needcaptchaUrl, captchaUrl);
    }
}
