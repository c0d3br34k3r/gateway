package server;

public enum HttpStatus {

	_100_CONTINUE(100, "Continue"),
	_101_SWITCHING_PROTOCOLS(101, "Switching Protocols"),

	_200_OK(200, "OK"),
	_201_CREATED(201, "Created"),
	_202_ACCEPTED(202, "Accepted"),
	_203_NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
	_204_NO_CONTENT(204, "No Content"),
	_205_RESET_CONTENT(205, "Reset Content"),
	_206_PARTIAL_CONTENT(206, "Partial Content"),
	_207_MULTI_STATUS(207, "Multi-Status"),
	_208_ALREADY_REPORTED(208, "Already Reported"),
	_226_IM_USED(226, "IM Used"),

	_300_MULTIPLE_CHOICES(300, "Multiple Choices"),
	_301_PERMANENTLY_MOVED(301, "Permanently Moved"),
	_302_FOUND(302, "Found"),
	_303_SEE_OTHER(303, "See Other"),
	_304_NOT_MODIFIED(304, "Not Modified"),
	_305_USE_PROXY(305, "Use Proxy"),
	_306_SWITCH_PROXY(306, "Switch Proxy"),
	_307_TEMPORARY_REDIRECT(307, "Temporary Redirect"),
	_308_PERMANENT_REDIRECT(308, "Permanent Redirect"),

	_400_BAD_REQUEST(400, "Bad Request"),
	_401_UNAUTHORIZED(401, "Unauthorized"),
	_402_PAYMENT_REQUIRED(402, "Payment Required"),
	_403_FORBIDDEN(403, "Forbidden"),
	_404_NOT_FOUND(404, "Not Found"),
	_405_METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
	_406_NOT_ACCEPTABLE(406, "Not Acceptable"),
	_407_PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
	_408_REQUEST_TIMEOUT(408, "Request Timeout"),
	_409_CONFLICT(409, "Conflict"),
	_410_GONE(410, "Gone"),
	_411_LENGTH_REQUIRED(411, "Length Required"),
	_412_PRECONDITION_FAILED(412, "Precondition Failed"),
	_413_PAYLOAD_TOO_LARGE(413, "Payload Too Large"),
	_414_URI_TOO_LONG(414, "URI Too Long"),
	_415_UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
	_416_RANGE_NOT_SATISFIABLE(416, "Range Not Satisfiable"),
	_417_EXPECTATION_FAILED(417, "Expectation Failed"),
	_418_IM_A_TEAPOT(418, "I'm a teapot"),
	_421_MISDIRECTED_REQUEST(421, "Misdirected Request"),
	_422_UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),
	_423_LOCKED(423, "Locked"),
	_424_FAILED_DEPENDENCY(424, "Failed Dependency"),
	_426_UPGRADE_REQUIRED(426, "Upgrade Required"),
	_428_PRECONDITION_REQUIRED(428, "Precondition Required"),
	_429_TOO_MANY_REQUESTS(429, "Too Many Requests"),
	_431_REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),
	_451_UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons"),

	_500_INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
	_501_NOT_IMPLEMENTED(501, "Not Implemented"),
	_502_BAD_GATEWAY(502, "Bad Gateway"),
	_503_SERVICE_UNAVAILABLE(503, "Service Unavailable"),
	_504_GATEWAY_TIMEOUT(504, "Gateway Timeout"),
	_505_HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),
	_506_VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates"),
	_507_INSUFFICIENT_STORAGE(507, "Insufficient Storage"),
	_508_LOOP_DETECTED(508, "Loop Detected"),
	_510_NOT_EXTENDED(510, "Not Extended"),
	_511_NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required");

	private final int code;
	private final String title;

	HttpStatus(int code, String message) {
		this.code = code;
		this.title = message;
	}

	public int code() {
		return code;
	}

	public String title() {
		return title;
	}

	@Override public String toString() {
		return "HTTP/1.1 " + code + ' ' + title;
	}

}
