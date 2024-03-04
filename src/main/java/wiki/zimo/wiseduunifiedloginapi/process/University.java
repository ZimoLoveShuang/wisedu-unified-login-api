package wiki.zimo.wiseduunifiedloginapi.process;

import org.apache.commons.lang3.StringUtils;
import wiki.zimo.wiseduunifiedloginapi.process.impl.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * @author SanseYooyea
 */
public enum University {
    /**
     * 安徽建筑大学
     */
    AHJZU("ahjzu.edu.cn", AhjzuCasLoginProcess.class),
    /**
     *
     */
    BISTU("bistu.edu.cn", BistuLoginProcess.class),
    /**
     *
     */
    CAS("", CasLoginProcess.class),
    /**
     * 中国计量大学
     */
    CJLU("cjlu.edu.cn", CjluLoginProcess.class),
    /**
     * 成都信息工程大学
     */
    CUIT("cuit.edu.cn", CuitCasLoginProcess.class),
    /**
     * 中国矿业大学
     */
    CUMT("cumt.edu.cn", CumtCasLoginProcess.class),
    /**
     * 河南大学
     */
    HENU("henu.edu.cn", HenuCasLoginProcess.class),
    /**
     * 合肥师范学院
     */
    HFNU("hfnu.edu.cn", HfnuCasLoginProcess.class),
    /**
     * 合肥工业大学
     */
    HFUT("hfut.edu.cn", HfutCasLoginProcess.class),
    /**
     * 淮阴师范学院
     */
    HYTC("hytc.edu.cn", HytcCasLoginProcess.class),
    /**
     *
     */
    IAP("/iap", IapLoginProcess.class),
    /**
     * 江苏科技大学
     */
    KMU("kmu.edu.cn", KmuCasLoginProcess.class),
    /**
     * 昆明学院
     */
    SDUC("sduc.edu.cn", SducCasLoginProcess.class),
    /**
     * 武汉轻工大学
     */
    WHPU("whpu.edu.cn", WhpuCasLoginProcess.class);

    String flagUrl;
    Class<?> processClazz;

    University(String flagUrl, Class<?> processClazz) {
        this.flagUrl = flagUrl;
        this.processClazz = processClazz;
    }

    public static University getUniversityByLoginUrl(String loginUrl) {
        for (University university : University.values()) {
            if (StringUtils.isEmpty(university.flagUrl)) {
                continue;
            }

            if (loginUrl.trim().contains(university.flagUrl)) {
                return university;
            }
        }
        return CAS;
    }

    public BaseLoginProcess newLoginProcess(String loginUrl, Map<String, String> parms) {
        try {
            return (BaseLoginProcess) processClazz.getDeclaredConstructor(String.class, Map.class).newInstance(loginUrl, parms);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
