package gateway;

public enum HttpStatus {

	/** The HTTP status {@code 100 Continue}. */
	_100_CONTINUE("100", "Continue"),
	/** The HTTP status {@code 101 Switching Protocols}. */
	_101_SWITCHING_PROTOCOLS("101", "Switching Protocols"),

	/** The HTTP status {@code 200 OK}. */
	_200_OK("200", "OK"),
	/** The HTTP status {@code 201 Created}. */
	_201_CREATED("201", "Created"),
	/** The HTTP status {@code 202 Accepted}. */
	_202_ACCEPTED("202", "Accepted"),
	/** The HTTP status {@code 203 Non-Authoritative Information}. */
	_203_NON_AUTHORITATIVE_INFORMATION("203", "Non-Authoritative Information"),
	/** The HTTP status {@code 204 No Content}. */
	_204_NO_CONTENT("204", "No Content"),
	/** The HTTP status {@code 205 Reset Content}. */
	_205_RESET_CONTENT("205", "Reset Content"),
	/** The HTTP status {@code 206 Partial Content}. */
	_206_PARTIAL_CONTENT("206", "Partial Content"),
	/** The HTTP status {@code 207 Multi-Status}. */
	_207_MULTI_STATUS("207", "Multi-Status"),
	/** The HTTP status {@code 208 Already Reported}. */
	_208_ALREADY_REPORTED("208", "Already Reported"),
	/** The HTTP status {@code 226 IM Used}. */
	_226_IM_USED("226", "IM Used"),

	/** The HTTP status {@code 300 Multiple Choices}. */
	_300_MULTIPLE_CHOICES("300", "Multiple Choices"),
	/** The HTTP status {@code 301 Permanently Moved}. */
	_301_PERMANENTLY_MOVED("301", "Permanently Moved"),
	/** The HTTP status {@code 302 Found}. */
	_302_FOUND("302", "Found"),
	/** The HTTP status {@code 303 See Other}. */
	_303_SEE_OTHER("303", "See Other"),
	/** The HTTP status {@code 304 Not Modified}. */
	_304_NOT_MODIFIED("304", "Not Modified"),
	/** The HTTP status {@code 305 Use Proxy}. */
	_305_USE_PROXY("305", "Use Proxy"),
	/** The HTTP status {@code 306 Switch Proxy}. */
	_306_SWITCH_PROXY("306", "Switch Proxy"),
	/** The HTTP status {@code 307 Temporary Redirect}. */
	_307_TEMPORARY_REDIRECT("307", "Temporary Redirect"),
	/** The HTTP status {@code 308 Permanent Redirect}. */
	_308_PERMANENT_REDIRECT("308", "Permanent Redirect"),

	/** The HTTP status {@code 400 Bad Request}. */
	_400_BAD_REQUEST("400", "Bad Request"),
	/** The HTTP status {@code 401 Unauthorized}. */
	_401_UNAUTHORIZED("401", "Unauthorized"),
	/** The HTTP status {@code 402 Payment Required}. */
	_402_PAYMENT_REQUIRED("402", "Payment Required"),
	/** The HTTP status {@code 403 Forbidden}. */
	_403_FORBIDDEN("403", "Forbidden"),
	/** The HTTP status {@code 404 Not Found}. */
	_404_NOT_FOUND("404", "Not Found"),
	/** The HTTP status {@code 405 Method Not Allowed}. */
	_405_METHOD_NOT_ALLOWED("405", "Method Not Allowed"),
	/** The HTTP status {@code 406 Not Acceptable}. */
	_406_NOT_ACCEPTABLE("406", "Not Acceptable"),
	/** The HTTP status {@code 407 Proxy Authentication Required}. */
	_407_PROXY_AUTHENTICATION_REQUIRED("407", "Proxy Authentication Required"),
	/** The HTTP status {@code 408 Request Timeout}. */
	_408_REQUEST_TIMEOUT("408", "Request Timeout"),
	/** The HTTP status {@code 409 Conflict}. */
	_409_CONFLICT("409", "Conflict"),
	/** The HTTP status {@code 410 Gone}. */
	_410_GONE("410", "Gone"),
	/** The HTTP status {@code 411 Length Required}. */
	_411_LENGTH_REQUIRED("411", "Length Required"),
	/** The HTTP status {@code 412 Precondition Failed}. */
	_412_PRECONDITION_FAILED("412", "Precondition Failed"),
	/** The HTTP status {@code 413 Payload Too Large}. */
	_413_PAYLOAD_TOO_LARGE("413", "Payload Too Large"),
	/** The HTTP status {@code 414 URI Too Long}. */
	_414_URI_TOO_LONG("414", "URI Too Long"),
	/** The HTTP status {@code 415 Unsupported Media Type}. */
	_415_UNSUPPORTED_MEDIA_TYPE("415", "Unsupported Media Type"),
	/** The HTTP status {@code 416 Range Not Satisfiable}. */
	_416_RANGE_NOT_SATISFIABLE("416", "Range Not Satisfiable"),
	/** The HTTP status {@code 417 Expectation Failed}. */
	_417_EXPECTATION_FAILED("417", "Expectation Failed"),
	/** The HTTP status {@code 418 I'm a teapot}. */
	_418_IM_A_TEAPOT("418", "I'm a teapot"),
	/** The HTTP status {@code 421 Misdirected Request}. */
	_421_MISDIRECTED_REQUEST("421", "Misdirected Request"),
	/** The HTTP status {@code 422 Unprocessable Entity}. */
	_422_UNPROCESSABLE_ENTITY("422", "Unprocessable Entity"),
	/** The HTTP status {@code 423 Locked}. */
	_423_LOCKED("423", "Locked"),
	/** The HTTP status {@code 424 Failed Dependency}. */
	_424_FAILED_DEPENDENCY("424", "Failed Dependency"),
	/** The HTTP status {@code 426 Upgrade Required}. */
	_426_UPGRADE_REQUIRED("426", "Upgrade Required"),
	/** The HTTP status {@code 428 Precondition Required}. */
	_428_PRECONDITION_REQUIRED("428", "Precondition Required"),
	/** The HTTP status {@code 429 Too Many Requests}. */
	_429_TOO_MANY_REQUESTS("429", "Too Many Requests"),
	/** The HTTP status {@code 431 Request Header Fields Too Large}. */
	_431_REQUEST_HEADER_FIELDS_TOO_LARGE("431", "Request Header Fields Too Large"),
	/** The HTTP status {@code 451 Unavailable For Legal Reasons}. */
	_451_UNAVAILABLE_FOR_LEGAL_REASONS("451", "Unavailable For Legal Reasons"),

	/** The HTTP status {@code 500 Internal Server Error}. */
	_500_INTERNAL_SERVER_ERROR("500", "Internal Server Error"),
	/** The HTTP status {@code 501 Not Implemented}. */
	_501_NOT_IMPLEMENTED("501", "Not Implemented"),
	/** The HTTP status {@code 502 Bad Gateway}. */
	_502_BAD_GATEWAY("502", "Bad Gateway"),
	/** The HTTP status {@code 503 Service Unavailable}. */
	_503_SERVICE_UNAVAILABLE("503", "Service Unavailable"),
	/** The HTTP status {@code 504 Gateway Timeout}. */
	_504_GATEWAY_TIMEOUT("504", "Gateway Timeout"),
	/** The HTTP status {@code 505 HTTP Version Not Supported}. */
	_505_HTTP_VERSION_NOT_SUPPORTED("505", "HTTP Version Not Supported"),
	/** The HTTP status {@code 506 Variant Also Negotiates}. */
	_506_VARIANT_ALSO_NEGOTIATES("506", "Variant Also Negotiates"),
	/** The HTTP status {@code 507 Insufficient Storage}. */
	_507_INSUFFICIENT_STORAGE("507", "Insufficient Storage"),
	/** The HTTP status {@code 508 Loop Detected}. */
	_508_LOOP_DETECTED("508", "Loop Detected"),
	/** The HTTP status {@code 510 Not Extended}. */
	_510_NOT_EXTENDED("510", "Not Extended"),
	/** The HTTP status {@code 511 Network Authentication Required}. */
	_511_NETWORK_AUTHENTICATION_REQUIRED("511", "Network Authentication Required");

	private final String code;
	private final String title;

	HttpStatus(String code, String message) {
		this.code = code;
		this.title = message;
	}

	public String code() {
		return code;
	}

	public String title() {
		return title;
	}

	@Override
	public String toString() {
		return code + " " + title;
	}

}
