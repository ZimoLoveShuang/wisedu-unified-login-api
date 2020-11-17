package wiki.zimo.wiseduunifiedloginapi.helper;

public class HeadersHelper {
    public static void main(String[] args) {
        String headers = "Host: sdcsjs.campusphere.net\n" +
                "Connection: keep-alive\n" +
                "Upgrade-Insecure-Requests: 1\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.193 Safari/537.36\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9\n" +
                "Sec-Fetch-Site: cross-site\n" +
                "Sec-Fetch-Mode: navigate\n" +
                "Sec-Fetch-User: ?1\n" +
                "Sec-Fetch-Dest: document\n" +
                "Accept-Encoding: gzip, deflate, br\n" +
                "Accept-Language: zh-CN,zh;q=0.9\n";
        String[] split = headers.split("\n");
        for (String s : split) {
            int index = s.indexOf(":");
            System.out.printf("headers.put(\"%s\",\"%s\");\n", s.substring(0, index).trim(), s.substring(index + 1).trim());
        }
    }
}
