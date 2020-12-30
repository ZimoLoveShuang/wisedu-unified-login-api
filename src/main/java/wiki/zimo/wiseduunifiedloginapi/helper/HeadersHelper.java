package wiki.zimo.wiseduunifiedloginapi.helper;

public class HeadersHelper {
    public static void main(String[] args) {
        String headers = "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9\n" +
                "Accept-Encoding: gzip, deflate\n" +
                "Accept-Language: zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6\n" +
                "Connection: keep-alive\n" +
                "Host: auth.hfut.edu.cn\n" +
                "Upgrade-Insecure-Requests: 1\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36 Edg/87.0.664.66";
        String[] split = headers.split("\n");
        for (String s : split) {
            int index = s.indexOf(":");
            System.out.printf("headers.put(\"%s\",\"%s\");\n", s.substring(0, index).trim(), s.substring(index + 1).trim());
        }
    }
}
