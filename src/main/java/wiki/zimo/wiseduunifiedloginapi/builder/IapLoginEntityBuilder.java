package wiki.zimo.wiseduunifiedloginapi.builder;

import wiki.zimo.wiseduunifiedloginapi.entity.IapLoginEntity;

public class IapLoginEntityBuilder extends BaseLoginEntityBuilder {
    public IapLoginEntity build() {
        String doLoginUrl = protocol + "://" + host + "/iap/doLogin";
        String itUrl = protocol + "://" + host + "/iap/security/lt";
        this.needcaptchaUrl = protocol + "://" + host + "/iap/checkNeedCaptcha";
        this.captchaUrl = protocol + "://" + host + "/iap/generateCaptcha";
        return new IapLoginEntity(loginUrl, doLoginUrl, itUrl, needcaptchaUrl, captchaUrl);
    }
}
