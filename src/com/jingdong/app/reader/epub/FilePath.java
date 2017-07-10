package com.jingdong.app.reader.epub;

import java.io.File;

public class FilePath {

	/**
	 * resolve a relative URL string against an absolute URL string.
	 * 
	 * This method was adapted from the CalCom library at http://www.calcom.de
	 * 
	 * <p>
	 * the absolute URL string is the start point for the relative path.
	 * </p>
	 * 
	 * <p>
	 * <b>Example:</b>
	 * </p>
	 * 
	 * <pre>
	 *   relative path:  ../images/test.jpg
	 *   absolute path:  file:/d:/eigene dateien/eigene bilder/
	 *   result:         file:/d:/eigene dateien/images/test.jpg
	 * </pre>
	 * 
	 * @param relPath
	 *            The relative URL string to resolve. Unlike the Calcom version,
	 *            this may be an absolute path, if it starts with "/".
	 * @param absPath
	 *            The absolute URL string to start at. Unlike the CalCom
	 *            version, this may be a filename rather than just a path.
	 * 
	 * @return the absolute URL string resulting from resolving relPath against
	 *         absPath
	 * 
	 * @author Ulrich Hilger
	 * @author CalCom
	 * @author <a href="http://www.calcom.de">http://www.calcom.de</a>
	 * @author <a href="mailto:info@calcom.de">info@calcom.de</a>
	 * @author Dennis Brown (eInnovation)
	 */
	public static String resolveRelativePath(String absPath, String relPath) {
		// if relative path is really absolute, then ignore absPath (eInnovation
		// change)
		if (relPath.startsWith("/")) {
			absPath = "";
		}

		String newAbsPath = absPath;
		String newRelPath = relPath;
		if (relPath.startsWith("$")) {
			// $开头表示隐藏文件
			return relPath;
		} else if (absPath.endsWith("/")) {
			newAbsPath = absPath.substring(0, absPath.length() - 1);
		} else {
			// absPath ends with a filename, remove it (eInnovation change)
			int lastSlashIndex = absPath.lastIndexOf('/');
			if (lastSlashIndex >= 0) {
				newAbsPath = absPath.substring(0, lastSlashIndex);
			} else {
				newAbsPath = "";
			}
		}

		int relPos = newRelPath.indexOf("../");
		while (relPos > -1) {
			newRelPath = newRelPath.substring(relPos + 3);
			int lastSlashInAbsPath = newAbsPath.lastIndexOf("/");
			if (lastSlashInAbsPath >= 0) {
				newAbsPath = newAbsPath.substring(0,
						newAbsPath.lastIndexOf("/"));
			} else {
				// eInnovation change: fix potential exception
				newAbsPath = "";
			}
			relPos = newRelPath.indexOf("../");
		}
		String returnedPath;
		if (newRelPath.startsWith("/")) {
			returnedPath = newAbsPath + newRelPath;
		} else {
			returnedPath = newAbsPath + "/" + newRelPath;
		}

		// remove any "." references to current directory (eInnovation change)
		// For example:
		// "./junk" becomes "junk"
		// "/./junk" becomes "/junk"
		// "junk/." becomes "junk"
		while (returnedPath.endsWith("/.")) {
			returnedPath = returnedPath.substring(0, returnedPath.length() - 2);
		}
		do {
			int dotSlashIndex = returnedPath.lastIndexOf("./");
			if (dotSlashIndex < 0) {
				break;
			} else if (dotSlashIndex == 0
					|| returnedPath.charAt(dotSlashIndex - 1) != '.') {
				String firstSubstring;
				if (dotSlashIndex > 0) {
					firstSubstring = returnedPath.substring(0, dotSlashIndex);
				} else {
					firstSubstring = "";
				}
				String secondSubstring;
				if (dotSlashIndex + 2 < returnedPath.length()) {
					secondSubstring = returnedPath.substring(dotSlashIndex + 2,
							returnedPath.length());
				} else {
					secondSubstring = "";
				}
				returnedPath = firstSubstring + secondSubstring;
			}
		} while (true);

		return returnedPath;
	}

	public static String getRealPath(String basePath, String relativePath) {
		File dir = new File(basePath);
		while (relativePath.startsWith("../")) {
			relativePath = relativePath.substring(3, relativePath.length());
			dir = dir.getParentFile();
		}
		String path = new File(dir, relativePath).getPath();
		return path;
	}
}
