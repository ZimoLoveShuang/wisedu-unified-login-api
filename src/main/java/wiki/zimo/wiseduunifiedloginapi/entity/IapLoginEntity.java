package wiki.zimo.wiseduunifiedloginapi.entity;

public class IapLoginEntity extends BaseLoginEntity {
    private String doLoginUrl;
    private String itUrl;

    public IapLoginEntity(String loginUrl, String doLoginUrl, String itUrl, String needcaptchaUrl, String captchaUrl) {
        super(loginUrl, needcaptchaUrl, captchaUrl);
        this.doLoginUrl = doLoginUrl;
        this.itUrl = itUrl;
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

    @Override
    public String toString() {
        return "IapLoginEntity{" +
                "loginUrl='" + getLoginUrl() + '\'' +
                ", doLoginUrl='" + getDoLoginUrl() + '\'' +
                ", itUrl='" + getItUrl() + '\'' +
                ", needcaptchaUrl='" + getNeedcaptchaUrl() + '\'' +
                ", captchaUrl='" + getCaptchaUrl() + '\'' +
                '}';
    }
}
