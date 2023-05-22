package wiki.zimo.wiseduunifiedloginapi.entity;

/**
 * 登陆实体基类
 * @author SanseYooyea
 */
public abstract class BaseLoginEntity {
    private String loginUrl;
    private String needcaptchaUrl;
    private String captchaUrl;

    public BaseLoginEntity(String loginUrl, String needcaptchaUrl, String captchaUrl) {
        this.loginUrl = loginUrl;
        this.needcaptchaUrl = needcaptchaUrl;
        this.captchaUrl = captchaUrl;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getNeedcaptchaUrl() {
        return needcaptchaUrl;
    }

    public void setNeedcaptchaUrl(String needcaptchaUrl) {
        this.needcaptchaUrl = needcaptchaUrl;
    }

    public String getCaptchaUrl() {
        return captchaUrl;
    }

    public void setCaptchaUrl(String captchaUrl) {
        this.captchaUrl = captchaUrl;
    }

    @Override
    public String toString() {
        return "BaseLoginEntity{" +
                "loginUrl='" + loginUrl + '\'' +
                ", needcaptchaUrl='" + needcaptchaUrl + '\'' +
                ", captchaUrl='" + captchaUrl + '\'' +
                '}';
    }
}
