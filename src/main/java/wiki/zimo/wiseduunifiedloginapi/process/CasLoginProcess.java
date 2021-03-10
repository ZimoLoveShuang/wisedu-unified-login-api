package wiki.zimo.wiseduunifiedloginapi.process;

import net.jodah.expiringmap.ExpiringMap;
import net.sourceforge.tess4j.TesseractException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import wiki.zimo.wiseduunifiedloginapi.builder.CasLoginEntityBuilder;
import wiki.zimo.wiseduunifiedloginapi.entity.CasLoginEntity;
import wiki.zimo.wiseduunifiedloginapi.helper.AESHelper;
import wiki.zimo.wiseduunifiedloginapi.helper.ImageHelper;
import wiki.zimo.wiseduunifiedloginapi.helper.TesseractOCRHelper;
import wiki.zimo.wiseduunifiedloginapi.utils.RandomUA;
import wiki.zimo.wiseduunifiedloginapi.trust.HttpsUrlValidator;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

/**
 * 金智统一cas登陆
 */
public class CasLoginProcess {
	private CasLoginEntity loginEntity;
	private Map<String, String> params;
	private static Map<String, Map<String, String>> map;
	private HMACSHA ciper;

	static {
		map = ExpiringMap.builder().maxSize(12).expiration(3, TimeUnit.HOURS).build();
	}

	public CasLoginProcess(String loginUrl, Map<String, String> params) {
		try {
			this.loginEntity = new CasLoginEntityBuilder()
					.loginUrl(
							"https://" + new URL(loginUrl).getHost() + "/wec-counselor-sign-apps/stu/mobile/index.html")
					.build();
		} catch (Throwable e) {
		}
		
		;
		this.params = params;
		this.ciper = new HMACSHA();
	}

	public Map<String, String> login() throws Exception {

		// 忽略证书错误

		cert: {
			HttpsUrlValidator.trustAllHttpsCertificates();
			HttpsURLConnection.setDefaultHostnameVerifier(HttpsUrlValidator.hv);

			break cert;
		}

		// 请求登陆页
		Connection con = Jsoup.connect(loginEntity.getLoginUrl())
				.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0")
				.header("Connection", "keep-alive").header("Host", new URL(loginEntity.getLoginUrl()).getHost())
				.followRedirects(true);
		Connection.Response res = con.execute();

		// 解析登陆页
		Document doc = res.parse();
//        System.out.println(doc);

		// 全局cookie
		Map<String, String> cookies = res.cookies();

		// 获取登陆表单
		Element form = doc.getElementById("casLoginForm");
//        System.out.println(form);
		if (form == null) {
			throw new RuntimeException("网页中没有找到casLoginForm，请联系开发者！！！");
		}

		// 处理加密的盐
		Element saltElement = doc.getElementById("pwdDefaultEncryptSalt");

		String salt = null;
		if (saltElement != null) {
			salt = saltElement.val();
		}

		// 网页中可能不存在id为pwdDefaultEncryptSalt的元素中，但提交的密码参数仍然需要加密
		if (saltElement == null) {
			Elements scripts = doc.getElementsByTag("script");
			for (Element script : scripts) {
				if (script.data().contains("pwdDefaultEncryptSalt")) {
//                    System.out.println(script.data());
					// 用正则表达式匹配盐
					String pattern = "\"\\w{16}\"";
					Pattern p = Pattern.compile(pattern);
					Matcher m = p.matcher(script.data());
					if (m.find()) {
						String group = m.group();
						salt = group.substring(1, group.length() - 1);
//                        System.out.println(group);
//                        System.out.println(salt);
					}
					break;
				}
			}
		}

//        System.out.println("盐是 " + salt);

		// 获取登陆表单里的输入
		Elements inputs = form.getElementsByTag("input");

		String username = this.params.get("username");
		String password = this.params.get("password");

		// 构造post请求参数
		Map<String, String> params = new HashMap<>();
		for (Element e : inputs) {

			// 填充用户名
			if (e.attr("name").equals("username")) {
				e.attr("value", username);
			}

			// 填充密码
			if (e.attr("name").equals("password")) {
				if (salt != null) {
					e.attr("value", AESHelper.encryptAES(password, salt));
				} else {
					e.attr("value", password);
				}
			}

			// 排除空值表单属性
			if (e.attr("name").length() > 0) {
				// 排除记住我
				if (e.attr("name").equals("rememberMe")) {
					continue;
				} else if (e.attr("name").equals("captchaResponse")) {
					continue;
				}

				params.put(e.attr("name"), e.attr("value"));
			}
		}

		// System.out.println("登陆参数 " + params);

		try {
			Thread.sleep(1200L); // WAF
		} catch (Throwable c) {
		}

		return casSendLoginData(res.url().getProtocol() + "://" + res.url().getHost() + form.attr("action"), cookies,
				params, res.url());

	}

	/**
	 * cas发送登陆请求，返回cookies
	 *
	 * @param login_url
	 * @param cookies
	 * @param params
	 * @return
	 * @throws Exception
	 */
	private Map<String, String> casSendLoginData(String login_url, Map<String, String> cookies,
			Map<String, String> params, URL BaseUrl) throws Exception {
		Connection con = Jsoup.connect(login_url);
//        System.out.println(login_url);

		// 构造请求头
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("Accept-Encoding", "gzip, deflate, br");
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		headers.put("Cache-Control", "max-age=0");
		headers.put("Connection", "keep-alive");
		headers.put("Host", BaseUrl.getHost());
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0");
		headers.put("Origin", BaseUrl.getProtocol() + "://" + BaseUrl.getHost());
		headers.put("Referer", login_url);

		cookies.put("org.springframework.web.servlet.i18n.CookieLocaleResolver.LOCALE", "zh_CN");

		Connection.Response login = con.headers(headers).ignoreContentType(true).followRedirects(false)
				.method(Connection.Method.POST).data(params).cookies(cookies).execute();

		if (login.statusCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
			// 重定向代表登陆成功
			// 更新cookie

			Map<String, String> neon = login.cookies().entrySet().stream().filter(key -> key.getKey().equals("CASTGC"))
					.collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

			neon.putAll(cookies);

			// 拿到重定向的地址
			String location = login.header("location");

			boolean finished = false;

			Map<String, String> header = new HashMap<>();
			header.put("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
			header.put("Accept-Encoding", "gzip, deflate");
			header.put("Cache-Control", "max-age=0");
			header.put("Connection", "keep-alive");
			header.put("Host", new URL(location).getProtocol() + "://" + new URL(location).getHost());
			header.put("Upgrade-Insecure-Requests", "1");
			header.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0");
			header.put("Referer", login_url);

			while (!finished) {

				con = Jsoup.connect(location).ignoreContentType(true).followRedirects(false).headers(header)
						.method(Connection.Method.GET)
						.header("User-Agent",
								"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0")
						.cookies(new URL(login_url).getHost().equals(new URL(location).getHost()) ? neon : cookies);
				// 请求，再次更新cookie
				login = con.execute();

				cookies.putAll(login.cookies());

				neon.putAll(login.cookies());

				finished = login.statusCode() != HttpURLConnection.HTTP_MOVED_TEMP;

				location = login.header("location");

			}

			setCookies(this.params.get("username") + this.params.get("password"), neon);
			return cookies;
		} else if (login.statusCode() == HttpURLConnection.HTTP_OK) {
			// 登陆失败
			Document doc = login.parse();
			Element msg = doc.getElementById("msg");
//            System.out.println(msg);
			Map<String, String> cookie = null;
			if (!msg.text().equals("无效的验证码")
					&& (cookie = getCookies(this.params.get("username") + this.params.get("password"))) == null) {
				throw new RuntimeException(msg.text());
			} else if (cookie != null)
				return cookie;
		} else {
			// 服务器可能出错
			throw new RuntimeException("教务系统服务器可能出错了，Http状态码是：" + login.statusCode());
		}
		return null;
	}

	/**
	 * 处理Cookies缓存
	 * 
	 * 
	 * 
	 */
	private Map<String, String> getCookies(String key) {
		return map.getOrDefault(this.ciper.sha(key, loginEntity.getCaptchaUrl()), null);
	}

	/**
	 * 置入Cookies缓存
	 * 
	 * 
	 * 
	 */
	private Object setCookies(String key, Map<String, String> value) {
		return map.put(this.ciper.sha(key, loginEntity.getCaptchaUrl()), value);
	}

	/**
	 * 处理验证码识别
	 *
	 * @param cookies
	 * @param captcha_url
	 * @return
	 * @throws IOException
	 * @throws TesseractException
	 */
	@SuppressWarnings("unused")
	private String ocrCaptcha(Map<String, String> cookies, Map<String, String> headers, String captcha_url)
			throws IOException, TesseractException {
		while (true) {
			String filePach = System.getProperty("user.dir") + File.separator + System.currentTimeMillis() + ".jpg";
//            System.out.println(filePach);
//            System.out.println(captcha_url);
			Connection.Response response = Jsoup.connect(captcha_url).headers(headers).cookies(cookies)
					.ignoreContentType(true).execute();

			// 四位验证码，背景有噪点
			ImageHelper.saveImageFile(ImageHelper.binaryzation(response.bodyStream()), filePach);
			String s = TesseractOCRHelper.doOcr(filePach);

			File temp = new File(filePach);
			temp.delete();

			if (judge(s, 4)) {
				return s;
			}
		}
	}

	/**
	 * 判断ocr识别出来的结果是否符合条件
	 *
	 * @param s
	 * @param len
	 * @return
	 */
	private boolean judge(String s, int len) {
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

	class HMACSHA {

		/**
		 * 将加密后的字节数组转换成字符串
		 * 
		 * @param b 字节数组
		 * @return 字符串
		 */
		private String byteArrayToHexString(byte[] b) {
			StringBuilder hs = new StringBuilder();
			String stmp;
			for (int n = 0; b != null && n < b.length; n++) {
				stmp = Integer.toHexString(b[n] & 0XFF);
				if (stmp.length() == 1)
					hs.append('0');

				hs.append(stmp);
			}
			return hs.toString().toLowerCase();
		}

		/**
		 * sha256_HMAC加密
		 * 
		 * @param message 消息
		 * @param secret  秘钥
		 * @return 加密后字符串
		 */
		private String sha(String message, String secret) {
			String hash = "";
			try {
				Mac sha_HMAC = Mac.getInstance("HmacSHA512");
				SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA512");
				sha_HMAC.init(secret_key);
				byte[] bytes = sha_HMAC.doFinal(message.getBytes());
				hash = byteArrayToHexString(bytes);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return hash;
		}
	}
}
