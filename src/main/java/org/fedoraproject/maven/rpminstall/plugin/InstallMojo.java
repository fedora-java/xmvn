/*-
 * Copyright (c) 2012 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fedoraproject.maven.rpminstall.plugin;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * @goal install
 */
public class InstallMojo extends AbstractMojo {

  private Log log;
  private String packageName;
  private String buildRoot;

  /**
   * POM
   *
   * @parameter expression="${project}"
   * @readonly
   * @required
   */
  protected MavenProject project;

  private static String getenv(String key) throws MojoExecutionException {
    String val = System.getenv(key);
    if (val == null)
      throw new MojoExecutionException("Environmental variable $" + key + " is not set");
    return val;
  }

  public void execute() throws MojoExecutionException, MojoFailureException {
    log = getLog();
    String packaging = project.getPackaging();
    packageName = getenv("RPM_PACKAGE_NAME");
    buildRoot = getenv("RPM_BUILD_ROOT");

    if (packaging.equals("pom")) {
      installPom();
      addMavenDepmap();
    } else if (packaging.equals("jar") || packaging.equals("bundle") || packaging.equals("maven-plugin")) {
      installPom();
      installJar();
      addMavenDepmap();
    } else {
      throw new MojoFailureException("unsupported packaging: " + packaging);
    }
  }

  private void exec(String cmd) throws MojoExecutionException {
    log.info("Executing: " + cmd);

    try {
      Process process = Runtime.getRuntime().exec(cmd);

      InputStreamReader isr = new InputStreamReader(process.getErrorStream());
      BufferedReader br = new BufferedReader(isr);
      String line;
      while ((line = br.readLine()) != null) {
        log.warn(line);
      }

      int status = process.waitFor();
      if (status != 0)
        throw new MojoExecutionException("Command finished with exit status " + status + ": " + cmd);
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to execute command: " + cmd, e);
    } catch (InterruptedException e) {
      throw new MojoExecutionException("Failed to execute command: " + cmd, e);
    }
  }

  private void install(String src, String dir, String dest) throws MojoExecutionException {
    exec("install -d -m 755 " + buildRoot + dir);
    exec("install -p -m 644 " + project.getBasedir() + "/" + src + " " + buildRoot + dir + "/" + dest);
  }

  private void addMavenDepmap() throws MojoExecutionException {
    try {
      OutputStream os = new FileOutputStream(project.getBasedir() + "/depmap.xml");
      PrintWriter pw = new PrintWriter(os);
      pw.println("<dependency>");
      pw.println("  <maven>");
      pw.println("    <groupId>" + project.getGroupId() + "</groupId>");
      pw.println("    <artifactId>" + project.getArtifactId() + "</artifactId>");
      pw.println("    <version>" + project.getVersion() + "</version>");
      pw.println("  </maven>");
      pw.println("  <jpp>");
      pw.println("    <groupId>JPP/" + packageName + "</groupId>");
      pw.println("    <artifactId>" + project.getArtifactId() + "</artifactId>");
      pw.println("    <version>" + project.getVersion() + "</version>");
      pw.println("  </jpp>");
      pw.println("</dependency>");
      pw.close();
      os.close();
    } catch (IOException e) {
      throw new MojoExecutionException("Unable to write depmap.xml", e);
    }

    install("depmap.xml", "/usr/share/maven-fragments", project.getArtifactId());
  }

  private void installPom() throws MojoExecutionException {
    install("pom.xml", "usr/share/maven-poms", "JPP." + packageName + "." + project.getArtifactId() + ".pom");
  }

  private void installJar() throws MojoExecutionException {
    install("target/" + project.getArtifactId() + "-" + project.getVersion() + ".jar", "usr/share/java/" + packageName, project.getArtifactId() + ".jar");
  }
}
