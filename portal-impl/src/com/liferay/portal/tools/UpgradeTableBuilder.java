/**
 * Copyright (c) 2000-2009 Liferay, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.liferay.portal.tools;

import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.util.FileImpl;

import org.apache.tools.ant.DirectoryScanner;

/**
 * <a href="UpgradeTableBuilder.java.html"><b><i>View Source</i></b></a>
 *
 * @author Brian Wing Shun Chan
 */
public class UpgradeTableBuilder {

	public static void main(String[] args) {
		try {
			new UpgradeTableBuilder(args[0]);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public UpgradeTableBuilder(String upgradeTableDir) throws Exception {
		DirectoryScanner ds = new DirectoryScanner();

		ds.setBasedir(".");
		ds.setIncludes(new String[] {"**\\upgrade\\v**\\util\\*Table.java"});

		ds.scan();

		String[] fileNames = ds.getIncludedFiles();

		for (String fileName : fileNames) {
			fileName = StringUtil.replace(fileName, "\\", "/");

			int x = fileName.indexOf("upgrade/v");
			int y = fileName.indexOf("/util", x);

			String version = StringUtil.replace(
				fileName.substring(x + 9, y), "_", ".");

			x = fileName.indexOf("/", y + 1);
			y = fileName.indexOf("Table.java", x);

			String upgradeFileName =
				upgradeTableDir + "/" + version + "/" +
					fileName.substring(x, y) + "ModelImpl.java";

			if (!_fileUtil.exists(upgradeFileName)) {
				continue;
			}

			String content = _fileUtil.read(upgradeFileName);

			String packagePath =
				"com.liferay.portal.upgrade.v" +
					StringUtil.replace(version, ".", "_") + ".util";
			String className = fileName.substring(x + 1, y) + "Table";

			content = _getContent(packagePath, className, content);

			_fileUtil.write(fileName, content);
		}
	}

	private String _getContent(
			String packagePath, String className, String content)
		throws Exception {

		int x = content.indexOf("public static final String TABLE_NAME =");

		if (x == -1) {
			x = content.indexOf("public static String TABLE_NAME =");
		}

		int y = content.indexOf("public static final String TABLE_SQL_DROP =");

		if (y == -1) {
			y = content.indexOf("public static String TABLE_SQL_DROP =");
		}

		y = content.indexOf(";", y);

		content = content.substring(x, y + 1);

		content = StringUtil.replace(
			content,
			new String[] {
				"\t", "{ \"", ") }"
			},
			new String[] {
				"", "{\"", ")}"
			});

		while (content.contains("\n\n")) {
			content = StringUtil.replace(content, "\n\n", "\n");
		}

		StringBuilder sb = new StringBuilder();

		sb.append(_fileUtil.read("../copyright.txt"));

		sb.append("\n\npackage " + packagePath + ";\n\n");

		sb.append("import java.sql.Types;\n\n");

		sb.append("/**\n");
		sb.append(
			" * <a href=\"" + className +
				".java.html\"><b><i>View Source</i></b></a>\n");
		sb.append(" *\n");
		sb.append(" * @author Brian Wing Shun Chan\n");
		sb.append(" */\n");
		sb.append("public class " + className + " {\n\n");

		String[] lines = StringUtil.split(content, "\n");

		for (String line : lines) {
			if (line.startsWith("public static") ||
				line.startsWith("};")) {

				sb.append("\t");
			}
			else if (line.startsWith("{\"")) {
				sb.append("\t\t");
			}

			sb.append(line);
			sb.append("\n");

			if (line.endsWith(";")) {
				sb.append("\n");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	private static FileImpl _fileUtil = FileImpl.getInstance();

}