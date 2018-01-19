//package gateway;
//
//import java.io.IOException;
//import java.util.regex.Pattern;
//
//import com.google.common.base.CharMatcher;
//import com.google.common.base.Predicate;
//import com.google.common.base.Predicates;
//
//public class MatcherParser {
//
//	private static final CharMatcher NON_WHITESPACE = CharMatcher.whitespace().negate();
//	private final String line;
//	private int index;
//
//	private MatcherParser(String line) {
//		this.line = line;
//	}
//
//	public static Predicate<String> parse(String line) {
//		return new MatcherParser(line).get();
//	}
//
//	private Predicate<String> get() {
//		int index = NON_WHITESPACE.indexIn(line);
//		if (index == -1) {
//			throw new IllegalArgumentException();
//		}
//		switch (line.charAt(index)) {
//			case '"':
//				return Predicates.equalTo(parseString('"'));
//			case '/':
//				return containsPattern(Pattern.compile(parseString('/')));
//			default:
//				return parseToken();
//		}
//	}
//
//	private Predicate<String> containsPattern(final Pattern pattern) {
//		return new Predicate<String>() {
//
//			@Override
//			public boolean apply(String t) {
//				return pattern.matcher(t).find();
//			}
//		};
//	}
//
//	private String parseString(final char end) {
//		StringBuilder builder = new StringBuilder();
//		while (index < line.length()) {
//			char c = line.charAt(index++);
//			if (c == end) {
//				return builder.toString();
//			}
//			if (c == '\\') {
//				builder.append(readEscapeChar());
//			} else {
//				builder.append(c);
//			}
//		}
//		throw new IllegalArgumentException("unclosed string");
//	}
//	
//	private char readEscapeChar() {
//
//	    char escaped = line.charAt(index++);
//	    switch (escaped) {
//	    case 'u':
//	      if (index + 4 > line.length()) {
//	        throw new IllegalArgumentException("unterminated escape sequence");
//	      }
//	      char result = 0;
//	      for (int i = pos, end = i + 4; i < end; i++) {
//	        char c = buffer[i];
//	        result <<= 4;
//	        if (c >= '0' && c <= '9') {
//	          result += (c - '0');
//	        } else if (c >= 'a' && c <= 'f') {
//	          result += (c - 'a' + 10);
//	        } else if (c >= 'A' && c <= 'F') {
//	          result += (c - 'A' + 10);
//	        } else {
//	          throw new NumberFormatException("\\u" + new String(buffer, pos, 4));
//	        }
//	      }
//	      pos += 4;
//	      return result;
//
//	    case 't':
//	      return '\t';
//	    case 'b':
//	      return '\b';
//	    case 'n':
//	      return '\n';
//	    case 'r':
//	      return '\r';
//	    case 'f':
//	      return '\f';
//	    case '\n':
//	      return '\n';
//	    case '\'':
//	    case '"':
//	    case '\\':
//	    default:
//	      return escaped;
//	    }
//	  }
//
//	private Predicate<String> parseToken() {
//		int start = index;
//		index = NON_WHITESPACE.indexIn(line, start + 1) + 1;
//		return Predicates.equalTo(line.substring(start, index));
//	}
//
//}
