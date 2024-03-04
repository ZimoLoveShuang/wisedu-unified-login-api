package wiki.zimo.wiseduunifiedloginapi;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

@SpringBootTest
class WiseduUnifiedLoginApiApplicationTests {

    @Test
    public void OCRTest() {
        File file = new File(System.getProperty("user.dir"));
        ITesseract tesseract = new Tesseract();
        Arrays.stream(Objects.requireNonNull(file.listFiles(f -> f.getName().endsWith(".jpg")))).forEach(f -> {
            try {
                String code = tesseract.doOCR(f);
                System.out.println(f.getName() + " = " + code);
            } catch (TesseractException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
