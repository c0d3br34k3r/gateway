package gateway;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;

public class HttpDateTimeFormat {

	private static final DateTimeFormatter FORMAT =
			DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
					.withZoneUTC().withLocale(Locale.US);

	private static final int BUFFER_SIZE = 0x1000;

	public static String print(DateTime time) {
		return FORMAT.print(time);
	}

	public static DateTime parse(String time) {
		return FORMAT.parseDateTime(time);
	}

	public static String hashMd5(Path path) throws IOException {
		try (InputStream in = Files.newInputStream(path)) {
			return hashMd5(in);
		}
	}

	/**
	 * Returns the last modified time of the file as a DateTime, rounding the
	 * seconds field down (effectively setting the milliseconds field to 0).
	 * This provides interoperability with the {@code If-Modified-Since} header,
	 * which does not retain the precision of milliseconds.
	 * 
	 * @param file the location of the file
	 * @return the last modified time of the file
	 * @throws IOException if an I/O error occurs
	 */
	public static DateTime getLastModifiedTime(Path file) throws IOException {
		return new DateTime(Files.getLastModifiedTime(file).toMillis())
				.secondOfMinute().roundFloorCopy();
	}

	public static String hashMd5(InputStream in) throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		Hasher hasher = Hashing.md5().newHasher();
		for (;;) {
			int r = in.read(buffer);
			if (r == -1) {
				break;
			}
			hasher.putBytes(buffer, 0, r);
		}
		return hasher.hash().toString();
	}

	public static ByteSource pathAsByteSource(final Path file) {
		return new ByteSource() {

			@Override
			public InputStream openStream() throws IOException {
				return Files.newInputStream(file);
			}

			@Override
			public Optional<Long> sizeIfKnown() {
				try {
					return Optional.of(Files.size(file));
				} catch (IOException e) {
					return Optional.absent();
				}
			}

			@Override
			public long size() throws IOException {
				return Files.size(file);
			}

			@Override
			public byte[] read() throws IOException {
				return Files.readAllBytes(file);
			}
		};
	}

	public static <T> Supplier<T> forName(String className) {
		final Class<T> clazz;
		try {
			@SuppressWarnings("unchecked")
			Class<T> cast = (Class<T>) Class.forName(className);
			clazz = cast;
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
		return new Supplier<T>() {

			@Override
			public T get() {
				try {
					return clazz.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					throw new IllegalArgumentException(e);
				}
			}
		};
	}

}
