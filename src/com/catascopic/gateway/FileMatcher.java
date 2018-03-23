package com.catascopic.gateway;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

public class FileMatcher implements Predicate<String> {

	private static final Splitter WILDCARDS = Splitter.on("*");

	private final String start;
	private final String end;
	private final List<String> middle;

	public static Predicate<String> of(String pattern) {
		List<String> parts = WILDCARDS.splitToList(pattern);
		switch (parts.size()) {
			case 0:
				throw new AssertionError();
			case 1:
				return Predicates.equalTo(parts.get(0));
			default:
				return new FileMatcher(parts.get(0), parts.get(parts.size() - 1),
						ImmutableList.copyOf(parts.subList(1, parts.size() - 1)));
		}
	}

	private FileMatcher(String start, String end, List<String> middle) {
		this.start = start;
		this.end = end;
		this.middle = middle;
	}

	@Override
	public boolean apply(String filename) {
		if (!filename.regionMatches(true, 0, start, 0, start.length())) {
			return false;
		}
		if (!filename.regionMatches(true, filename.length() - end.length(), end, 0,
				end.length())) {
			return false;
		}
		int pos = start.length();
		for (String part : middle) {
			int index = indexOf(filename, part, pos);
			if (index == -1 || index >= filename.length() - end.length()) {
				return false;
			}
			pos = index + part.length();
		}
		return true;
	}

	public static int indexOf(String source, String target, int start) {
		int limit = source.length() - target.length() + 1;
		for (int i = start; i < limit; i++) {
			if (source.regionMatches(true, i, target, 0, target.length())) {
				return i;
			}
		}
		return -1;
	}

}
