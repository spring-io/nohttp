package io.spring.nohttp.checkstyle.check;

import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.ExternalResourceHolder;
import com.puppycrawl.tools.checkstyle.api.FileText;
import io.spring.nohttp.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Rob Winch
 */
public class NoHttpCheck extends AbstractFileSetCheck implements ExternalResourceHolder {
	private HttpMatcher matcher = new RegexHttpMatcher();

	private String whitelistFileName = "";

	public void setWhitelistFileName(String whitelistFileName) {
		if (whitelistFileName == null) {
			throw new IllegalArgumentException("whitelistFileName cannot be null");
		}
		this.whitelistFileName = whitelistFileName;
	}

	private boolean isWhitelistFileSet() {
		return !this.whitelistFileName.isEmpty();
	}

	private InputStream createWhitelistInputStream() {
		if (isWhitelistFileSet()) {
			return createWhitelistFileInput();
		}
		return null;
	}

	private InputStream createWhitelistFileInput() {
		File whitelistFile = new File(this.whitelistFileName);
		try {
			return new FileInputStream(whitelistFile);
		} catch (FileNotFoundException e) {
			throw new IllegalStateException("Could not load file '" + whitelistFile +"'", e);
		}
	}

	@Override
	protected void finishLocalSetup() throws CheckstyleException {
		InputStream inputStream = createWhitelistInputStream();
		if (inputStream == null) {
			return;
		}

		RegexHttpMatcher matcher = new RegexHttpMatcher();

		if (this.isWhitelistFileSet()) {
			matcher.addHttpUrlWhitelist(RegexPredicate.createWhitelist(inputStream));
		}
		this.matcher = matcher;
	}

	@Override
	protected void processFiltered(File file, FileText fileText)
			throws CheckstyleException {
		int lineNum = 0;
		for (int index = 0; index < fileText.size(); index++) {
			final String line = fileText.get(index);
			lineNum++;
			List<HttpMatchResult> results = this.matcher.findHttp(line);
			for(HttpMatchResult result : results) {
				log(lineNum, result.getStart() + 1, "nohttp", result.getHttpUrl());
			}
		}
	}

	@Override
	public Set<String> getExternalResourceLocations() {
		Set<String> result = new HashSet<>();
		if (!isWhitelistFileSet()) {
			result.add(this.whitelistFileName);
		}
		return result;
	}
}
