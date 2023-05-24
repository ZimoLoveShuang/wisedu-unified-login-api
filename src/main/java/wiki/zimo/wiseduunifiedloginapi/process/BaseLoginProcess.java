package wiki.zimo.wiseduunifiedloginapi.process;

import wiki.zimo.wiseduunifiedloginapi.builder.BaseLoginEntityBuilder;
import wiki.zimo.wiseduunifiedloginapi.entity.BaseLoginEntity;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * 登陆流程基类
 *
 * @author SanseYooyea
 */
public abstract class BaseLoginProcess {
    protected final BaseLoginEntity loginEntity;
    protected final Map<String, String> params;

    public BaseLoginProcess(String loginUrl, Map<String, String> params, Class<?> loginEntityBuilderClass) {
        try {
            BaseLoginEntityBuilder builderInstance = (BaseLoginEntityBuilder) loginEntityBuilderClass.getDeclaredConstructor().newInstance();
            this.loginEntity = builderInstance
                    .loginUrl(loginUrl)
                    .build();
            this.params = params;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public BaseLoginProcess(BaseLoginEntity loginEntity, Map<String, String> params) {
        this.loginEntity = loginEntity;
        this.params = params;
    }

    public abstract Map<String, String> login() throws Exception;

    /**
     * cas发送登陆请求，返回cookies
     *
     * @param login_url 登录地址
     * @param cookies   cookies
     * @param params    参数
     * @return cookies
     * @throws Exception
     */
    protected abstract Map<String, String> casSendLoginData(String login_url, Map<String, String> cookies, Map<String, String> params) throws Exception;
}