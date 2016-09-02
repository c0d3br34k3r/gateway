package server;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;

public class Cookie {

	public static final String EXPIRES = "Expires";
	public static final String MAX_AGE = "Max-Age";
	public static final String DOMAIN = "Domain";
	public static final String PATH = "Path";
	public static final String SECURE = "Path";
	public static final String HTTP_ONLY = "HttpOnly";

	public static final CharMatcher NAME = asciiExceptFor(" \"(),/:;<=>?@[\\]{}");
	public static final CharMatcher VALUE = asciiExceptFor(" \",;\\");

	private static final DateTimeFormatter RFC1123_DATE_TIME =
			DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
					.withZoneUTC().withLocale(Locale.US);

	private static CharMatcher asciiExceptFor(String disallowed) {
		return CharMatcher.inRange((char) 0x20, (char) 0x7E)
				.and(CharMatcher.noneOf(disallowed))
				.precomputed();
	}

	private final String name;
	private final String value;

	private DateTime expires = null;
	private int maxAge = 0;
	private String domain = null;
	private String path = null;
	private boolean secure = false;
	private boolean httpOnly = false;

	public Cookie(String name, String value) {
		this.name = checkChars(name, NAME);
		this.value = checkChars(value, VALUE);
	}

	public Cookie(String name, String value, DateTime expires) {
		this(name, value);
		this.expires = expires;
	}

	private static String checkChars(String s, CharMatcher matcher) {
		if (!matcher.matchesAllOf(s)) {
			throw new IllegalArgumentException("invalid chars: " + matcher.removeFrom(s));
		}
		return s;
	}

	public Cookie setExpires(DateTime expires) {
		this.expires = expires;
		return this;
	}

	public Cookie setMaxAge(int maxAge) {
		if (maxAge < 1) {
			throw new IllegalArgumentException("Max-Age must be greater than 0");
		}
		this.maxAge = maxAge;
		return this;
	}

	public Cookie setDomain(String domain) {
		this.domain = domain;
		return this;
	}

	public Cookie setPath(String path) {
		this.path = path;
		return this;
	}

	public Cookie setSecure(boolean secure) {
		this.secure = secure;
		return this;
	}

	public Cookie setHttpOnly(boolean httpOnly) {
		this.httpOnly = httpOnly;
		return this;
	}

	private static final Joiner JOINER = Joiner.on("; ");

	@Override public String toString() {
		List<String> parts = new ArrayList<>();
		parts.add(name + '=' + value);
		if (expires != null) {
			parts.add(EXPIRES + '=' + RFC1123_DATE_TIME.print(expires));
		}
		if (maxAge != 0) {
			parts.add(MAX_AGE + '=' + maxAge);
		}
		if (domain != null) {
			parts.add(DOMAIN + '=' + domain);
		}
		if (path != null) {
			parts.add(PATH + '=' + path);
		}
		if (secure) {
			parts.add(SECURE);
		}
		if (httpOnly) {
			parts.add(HTTP_ONLY);
		}
		return JOINER.join(parts);
	}

}
