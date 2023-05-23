package wiki.zimo.wiseduunifiedloginapi.builder;


import wiki.zimo.wiseduunifiedloginapi.entity.BistuLoginEntity;

public class BistuLoginEntityBuilder extends BaseLoginEntityBuilder {

    public BistuLoginEntity build() {
        this.needcaptchaUrl = protocol + "://" + host + "/authserver/checkNeedCaptcha.htl";
        this.captchaUrl = protocol + "://" + host + "/authserver/getCaptcha.htl";
        return new BistuLoginEntity(loginUrl, needcaptchaUrl, captchaUrl);
    }
}
