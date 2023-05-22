package wiki.zimo.wiseduunifiedloginapi.builder;

import wiki.zimo.wiseduunifiedloginapi.entity.BaseLoginEntity;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author SanseYooyea
 */
public abstract class BaseLoginEntityBuilder {
    protected String loginUrl;
    protected String needcaptchaUrl;
    protected String captchaUrl;
    protected String host;
    protected String protocol;

    /**
     * 设置登陆地址
     * @param loginUrl 登陆地址
     * @return this
     */
    public BaseLoginEntityBuilder loginUrl(String loginUrl) {
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

    /**
     * 完成构建
     * @return 构建好的登陆实体
     */
    public abstract BaseLoginEntity build();
}