package wiki.zimo.wiseduunifiedloginapi.entity;

public class IapLoginEntity {
    private String loginUrl;
    private String doLoginUrl;
    private String itUrl;
    private String needcaptchaUrl;
    private String captchaUrl;

    public IapLoginEntity(String loginUrl, String doLoginUrl, String itUrl, String needcaptchaUrl, String captchaUrl) {
        this.loginUrl = loginUrl;
        this.doLoginUrl = doLoginUrl;
        this.itUrl = itUrl;
        this.needcaptchaUrl = needcaptchaUrl;
        this.captchaUrl = captchaUrl;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getDoLoginUrl() {
        return doLoginUrl;
    }

    public void setDoLoginUrl(String doLoginUrl) {
        this.doLoginUrl = doLoginUrl;
    }

    public String getItUrl() {
        return itUrl;
    }

    public void setItUrl(String itUrl) {
        this.itUrl = itUrl;
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
        return "IapLoginEntity{" +
                "loginUrl='" + loginUrl + '\'' +
                ", doLoginUrl='" + doLoginUrl + '\'' +
                ", itUrl='" + itUrl + '\'' +
                ", needcaptchaUrl='" + needcaptchaUrl + '\'' +
                ", captchaUrl='" + captchaUrl + '\'' +
                '}';
    }
}
