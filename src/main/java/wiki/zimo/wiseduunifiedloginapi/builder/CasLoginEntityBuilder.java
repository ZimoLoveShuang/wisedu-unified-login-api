package wiki.zimo.wiseduunifiedloginapi.builder;

import wiki.zimo.wiseduunifiedloginapi.entity.CasLoginEntity;

public class CasLoginEntityBuilder extends BaseLoginEntityBuilder {
    public CasLoginEntity build() {
        this.needcaptchaUrl = protocol + "://" + host + "/authserver/needCaptcha.html";
        this.captchaUrl = protocol + "://" + host + "/authserver/captcha.html";
        return new CasLoginEntity(loginUrl, needcaptchaUrl, captchaUrl);
    }
}
