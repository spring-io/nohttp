package io.spring.nohttp.checkstyle.check;

import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;
import io.spring.nohttp.*;

import java.io.File;
import java.util.List;

/**
 * @author Rob Winch
 */
public class NoHttpCheck extends AbstractFileSetCheck {
	private final HttpMatcher matcher = new WhitelistHttpMatcher();

	@Override
	protected void processFiltered(File file, FileText fileText)
			throws CheckstyleException {
		int lineNum = 0;
		for (int index = 0; index < fileText.size(); index++) {
			final String line = fileText.get(index);
			lineNum++;
			List<HttpMatchResult> results = matcher.findHttp(line);
			for(HttpMatchResult result : results) {
				log(lineNum, result.getStart() + 1, "nohttp", result.getHttpUrl());
			}
		}
	}
}
