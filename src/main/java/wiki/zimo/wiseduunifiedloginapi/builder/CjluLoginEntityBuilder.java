package wiki.zimo.wiseduunifiedloginapi.builder;

import wiki.zimo.wiseduunifiedloginapi.entity.BaseLoginEntity;
import wiki.zimo.wiseduunifiedloginapi.entity.CasLoginEntity;

/**
 * @author SanseYooyea
 */
public class CjluLoginEntityBuilder extends BaseLoginEntityBuilder {
    @Override
    public BaseLoginEntity build() {
        this.needcaptchaUrl = protocol + "://" + host + "/authserver/checkNeedCaptcha.htl";
        this.captchaUrl = protocol + "://" + host + "/authserver/getCaptcha.htl?" + System.currentTimeMillis();
        return new CasLoginEntity(loginUrl, needcaptchaUrl, captchaUrl);
    }
}
