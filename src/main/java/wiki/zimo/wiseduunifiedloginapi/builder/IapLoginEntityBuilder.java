package wiki.zimo.wiseduunifiedloginapi.builder;

import wiki.zimo.wiseduunifiedloginapi.entity.IapLoginEntity;

import java.net.MalformedURLException;
import java.net.URL;

public class IapLoginEntityBuilder {
    private String loginUrl;
    private String doLoginUrl;
    private String itUrl;
    private String needcaptchaUrl;
    private String captchaUrl;
    private String host;
    private String protocol;

    public IapLoginEntityBuilder loginUrl(String loginUrl) {
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

    public IapLoginEntity build() {
        this.doLoginUrl = protocol + "://" + host + "/iap/doLogin";
        this.itUrl = protocol + "://" + host + "/iap/security/lt";
        this.needcaptchaUrl = protocol + "://" + host + "/iap/checkNeedCaptcha";
        this.captchaUrl = protocol + "://" + host + "/iap/generateCaptcha";
        return new IapLoginEntity(loginUrl, doLoginUrl, itUrl, needcaptchaUrl, captchaUrl);
    }
}
