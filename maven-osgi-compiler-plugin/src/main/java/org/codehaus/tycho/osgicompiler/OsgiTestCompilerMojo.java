/*
 * 	Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.tycho.osgicompiler;

import java.io.File;
import java.util.List;

/**
 * @goal testCompile
 * @phase test-compile
 * @requiresDependencyResolution test
 * @description Compiles test application sources with eclipse plugin
 *              dependencies
 */
public class OsgiTestCompilerMojo extends AbstractOsgiCompilerMojo {

	/**
	 * The source directories containing the test-source to be compiled.
	 * 
	 * @parameter expression="${project.testCompileSourceRoots}"
	 * @required
	 * @readonly
	 */
	private List<String> compileSourceRoots;

	/**
	 * The directory where compiled test classes go.
	 * 
	 * @parameter expression="${project.build.testOutputDirectory}"
	 * @required
	 * @readonly
	 */
	private File testOutputDirectory;

	/**
	 * The directory where compiled test classes go.
	 * 
	 * @parameter expression="${project.build.outputDirectory}"
	 * @required
	 * @readonly
	 */
	private File outputDirectory;

	@Override
	protected List<String> getConfiguredCompileSourceRoots() {
		return compileSourceRoots;
	}

	/**
	 * output directory for this compile - the test output directory
	 */
	protected File getConfiguredOutputDirectory() {
		return testOutputDirectory;
	}

	public List getClasspathElements() {
		List result = super.getClasspathElements();
		result.add(0, outputDirectory.getAbsolutePath() + "[+**/*]");
		return result;
	}

}
