package com.catascopic.gateway;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;

public class CookieBuilder {

	private static final String EXPIRES = "Expires";
	private static final String MAX_AGE = "Max-Age";
	private static final String DOMAIN = "Domain";
	private static final String PATH = "Path";
	private static final String SECURE = "Secure";
	private static final String HTTP_ONLY = "HttpOnly";
	private static final DateTime INVALIDATE = new DateTime(0L);

	private static final CharMatcher NAME = asciiExceptFor(
			" \"(),/:;<=>?@[\\]{}");
	private static final CharMatcher VALUE = asciiExceptFor(" \",;\\");
	private static final CharMatcher ATTRIBUTE = asciiExceptFor(";");

	private static CharMatcher asciiExceptFor(String disallowed) {
		return CharMatcher.inRange('\u0020', '\u007E')
				.and(CharMatcher.noneOf(disallowed));
	}

	private final String name;
	private final String value;

	private DateTime expires; // = null
	private int maxAge; // = 0
	private String domain; // = null
	private String path; // = null
	private boolean secure; // = false
	private boolean httpOnly; // = false
	private List<String> extensions = new ArrayList<>();

	public CookieBuilder(String name, String value) {
		this.name = checkChars(name, NAME);
		this.value = checkChars(value, VALUE);
	}

	public CookieBuilder(String name, String value, DateTime expires) {
		this(name, value);
		this.expires = expires;
	}
	
	public CookieBuilder(String name, String value, int maxAge) {
		this(name, value);
		setMaxAge(maxAge);
	}

	private static String checkChars(String s, CharMatcher matcher) {
		if (!matcher.matchesAllOf(s)) {
			throw new IllegalArgumentException(
					String.format("invalid chars %s in %s",
							matcher.retainFrom(s), s));
		}
		return s;
	}

	public CookieBuilder setExpires(DateTime expires) {
		this.expires = expires;
		return this;
	}

	public CookieBuilder invalidate() {
		this.expires = INVALIDATE;
		return this;
	}

	public CookieBuilder setMaxAge(int maxAge) {
		if (maxAge < 1) {
			throw new IllegalArgumentException(
					"Max-Age must be greater than 0");
		}
		this.maxAge = maxAge;
		return this;
	}

	public CookieBuilder setDomain(String domain) {
		this.domain = domain;
		return this;
	}

	public CookieBuilder setPath(String path) {
		this.path = path;
		return this;
	}

	public CookieBuilder setSecure(boolean secure) {
		this.secure = secure;
		return this;
	}

	public CookieBuilder setHttpOnly(boolean httpOnly) {
		this.httpOnly = httpOnly;
		return this;
	}

	public CookieBuilder addExtension(String name, String value) {
		extensions.add(checkChars(name, ATTRIBUTE) + '=' + checkChars(value,
				ATTRIBUTE));
		return this;
	}

	public CookieBuilder addExtension(String value) {
		extensions.add(checkChars(value, ATTRIBUTE));
		return this;
	}

	private static final Joiner JOINER = Joiner.on("; ");

	public String build() {
		List<String> parts = new ArrayList<>();
		parts.add(name + '=' + value);
		if (expires != null) {
			addProperty(parts, EXPIRES, HttpDateTimeFormat.print(expires));
		}
		if (maxAge != 0) {
			addProperty(parts, MAX_AGE, maxAge);
		}
		if (domain != null) {
			addProperty(parts, DOMAIN, domain);
		}
		if (path != null) {
			addProperty(parts, PATH, path);
		}
		if (secure) {
			parts.add(SECURE);
		}
		if (httpOnly) {
			parts.add(HTTP_ONLY);
		}
		parts.addAll(extensions);
		return JOINER.join(parts);
	}

	private static void addProperty(List<String> parts, String key,
			Object value) {
		parts.add(key + '=' + value);
	}

	@Override
	public String toString() {
		return build();
	}

}
