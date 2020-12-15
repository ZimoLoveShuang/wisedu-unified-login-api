package wiki.zimo.wiseduunifiedloginapi.helper;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;

public class TesseractOCRHelper {
    public static final int MAX_TRY_TIMES = 20;

    public static String doOcr(String path) throws TesseractException {
        ITesseract instance = new Tesseract();
        String result = instance.doOCR(new File(path));
        result = result.replaceAll("\\s+", "");
        return result;
    }
}
