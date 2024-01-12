package ee.sebgros.secureapplication.services;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Service;

import java.util.function.Predicate;
import java.util.regex.Pattern;


@Service
public class HtmlSanitizer {
	PolicyFactory policy;

	public HtmlSanitizer() {
		policy = new HtmlPolicyBuilder()
				.allowStandardUrlProtocols()
				.allowElements("a", "p", "div", "i", "b", "em", "blockquote", "tt", "strong",
						"br", "ul", "ol", "li", "quote", "ecode", "img", "h1", "h2", "h3", "h4", "h5")
				.allowAttributes("href").matching(URL).onElements("a")
				.allowAttributes("src").matching(URL).onElements("img")
				.toFactory();
	}

	static final Pattern URL = Pattern.compile("(.*?)");

	private static Predicate<String> matchesEither(
			final Pattern a, final Pattern b) {
		return new Predicate<String>() {
			public boolean apply(String s) {
				return a.matcher(s).matches() || b.matcher(s).matches();
			}

			@SuppressWarnings("all")
			public boolean test(String s) {
				return apply(s);
			}
		};
	}

	public String sanitize(String html) {
		return policy.sanitize(html);
	}

}
