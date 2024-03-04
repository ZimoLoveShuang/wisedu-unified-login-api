package wiki.zimo.wiseduunifiedloginapi.process;

import net.sourceforge.tess4j.TesseractException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import wiki.zimo.wiseduunifiedloginapi.entity.BaseLoginEntity;
import wiki.zimo.wiseduunifiedloginapi.helper.ImageHelper;
import wiki.zimo.wiseduunifiedloginapi.helper.TesseractOCRHelper;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * 包含验证码的登陆流程类
 *
 * @author SanseYooyea
 */
public abstract class OcrLoginProcess extends BaseLoginProcess {
    public OcrLoginProcess(String loginUrl, Map<String, String> params, Class<?> loginEntityBuilderClass) {
        super(loginUrl, params, loginEntityBuilderClass);
    }

    public OcrLoginProcess(BaseLoginEntity loginEntity, Map<String, String> params) {
        super(loginEntity, params);
    }

    protected String ocrCaptcha(Map<String, String> cookies, Map<String, String> headers, String captcha_url, int length) throws IOException, TesseractException {
        while (true) {
            String filePach = System.getProperty("user.dir") + File.separator + System.currentTimeMillis() + ".jpg";
            Connection.Response response = Jsoup.connect(captcha_url)
                    .headers(headers).cookies(cookies)
                    .ignoreContentType(true)
                    .execute();

            // 四位验证码，背景有噪点
            ImageHelper.saveImageFile(ImageHelper.binaryzation(response.bodyStream()), filePach);
            String s = TesseractOCRHelper.doOcr(filePach);

            File temp = new File(filePach);
//            temp.delete();

            // 判断是否为字母或数字
            if (judge(s, length)) {
                return s;
            }
        }
    }

    protected boolean judge(String s, int len) {
        if (s == null || s.length() != len) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (!(Character.isDigit(ch) || Character.isLetter(ch))) {
                return false;
            }
        }
        return true;
    }
}
