package wiki.zimo.wiseduunifiedloginapi.builder;


import wiki.zimo.wiseduunifiedloginapi.entity.BistuLoginEntity;

public class BistuLoginEntityBuilder extends BaseLoginEntityBuilder {

    public BistuLoginEntity build() {
        this.needcaptchaUrl = protocol + "://" + host + "/authserver/checkNeedCaptcha.htl";
        this.captchaUrl = protocol + "://" + host + "/authserver/getCaptcha.htl";
        System.out.println("是否需要验证码链接：" + this.needcaptchaUrl);
        return new BistuLoginEntity(loginUrl, needcaptchaUrl, captchaUrl);
    }
}
