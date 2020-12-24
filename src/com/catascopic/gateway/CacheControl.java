package com.catascopic.gateway;

public class CacheControl {

	public Cacheability cacheability;
	
	

	public enum Cacheability {
		PUBLIC,
		PRIVATE,
		NO_CACHE,
		ONLY_IF_CACHED,
	}

}
