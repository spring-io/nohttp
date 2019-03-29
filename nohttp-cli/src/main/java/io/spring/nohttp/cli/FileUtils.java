package io.spring.nohttp.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Rob Winch
 */
class FileUtils {

	static class ExcludeDirFileVisitor extends SimpleFileVisitor<Path> {
		private Set<String> exludeDirs = Collections.singleton(".git");

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
			if (this.exludeDirs.contains(dir.toFile().getName())) {
				return FileVisitResult.SKIP_SUBTREE;
			}
			return FileVisitResult.CONTINUE;
		}
	}

	private static boolean isTextFile(File file) {
		try {
			return new ProcessBuilder("grep", "-Iq", ".", file.getAbsolutePath())
					.directory(file.getParentFile())
					.start()
					.waitFor() == 0;
		} catch (Exception e) {
			throw new RuntimeException("Could not determine if " + file + " is a text file. Try explicitly providing the dir or files you want to process", e);
		}
	}

	static List<File> getTextFiles(File dir) {
		System.out.print("Finding text files in " + dir + " ...");
		List<File> files = new ArrayList<>();

		try {
			Files.walkFileTree(dir.toPath(), new ExcludeDirFileVisitor() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
						throws IOException {
					File f = file.toFile();

					if (isTextFile(f)) {
						files.add(f);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}
		catch (IOException e) {
			throw new RuntimeException("Cannot get text files for " + dir, e);
		}

		System.out.println("Done!");
		return files;
	}
}
