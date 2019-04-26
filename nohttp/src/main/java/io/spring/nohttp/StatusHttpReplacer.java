package io.spring.nohttp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * @author Rob Winch
 */
public class StatusHttpReplacer implements Function<String, String> {
	private Logger logger = LoggerFactory.getLogger(StatusHttpReplacer.class);

	private static final Integer OK = Integer.valueOf(200);

	private ConcurrentMap<String, Integer> urlToStatus = new ConcurrentHashMap<>();

	private int timeout = 1000;

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public String apply(String httpUrl) {
		String httpsUrl = httpUrl.replaceFirst("http", "https");
		Integer status = findStatus(httpsUrl);
		return OK.equals(status) ? httpsUrl : httpUrl;
	}

	private Integer findStatus(String url) {
		return this.urlToStatus.computeIfAbsent(url, key -> getStatus(key));
	}

	private Integer getStatus(String url) {
		this.logger.debug("Looking up status for {}", url);
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setConnectTimeout(this.timeout);
			connection.setReadTimeout(this.timeout);
			return connection.getResponseCode();
		}
		catch (Exception e) {
			return null;
		}
	}
}
