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
package org.fedoraproject.maven.model;

public class Artifact implements Comparable<Artifact> {
	private static final String DEFAULT_VERSION = "SYSTEM";

	private final String groupId;
	private final String artifactId;
	private final String version;
	private final String extension;

	/**
	 * Dummy artifact that is resolved to an empty JAR provided by Maven itself.
	 * Used to skip dependencies.
	 */
	public static final Artifact DUMMY = new Artifact("JPP/maven", "empty-dep");

	public Artifact(String groupId, String artifactId) {
		this(groupId, artifactId, null);
	}

	public Artifact(String groupId, String artifactId, String version) {
		this(groupId, artifactId, version, null);
	}

	public Artifact(String groupId, String artifactId, String version,
			String extension) {

		if (groupId == null)
			throw new IllegalArgumentException("groupId may not be null");
		if (artifactId == null)
			throw new IllegalArgumentException("artifactId may not be null");

		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.extension = extension;
	}

	@Override
	public int compareTo(Artifact rhs) {
		if (!groupId.equals(rhs.groupId))
			return groupId.compareTo(rhs.groupId);
		if (!artifactId.equals(rhs.artifactId))
			return artifactId.compareTo(rhs.artifactId);
		if (!getVersion().equals(rhs.getVersion()))
			return getVersion().compareTo(rhs.getVersion());
		return getExtension().compareTo(rhs.getExtension());
	}

	@Override
	public boolean equals(Object rhs) {
		return rhs != null && rhs instanceof Artifact
				&& compareTo((Artifact) rhs) == 0;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getVersion() {
		if (version == null)
			return DEFAULT_VERSION;
		return version;
	}

	public String getExtension() {
		if (extension == null)
			return "pom";
		return extension;
	}

	public boolean isJppArtifact() {
		return groupId.equals("JPP") || groupId.startsWith("JPP/");
	}

	public boolean isPom() {
		return getExtension().equals("pom");
	}

	public Artifact copyVersionAndExtension(Artifact rhs) {
		return new Artifact(groupId, artifactId, rhs.version, rhs.extension);
	}

	public Artifact clearVersionAndExtension() {
		return new Artifact(groupId, artifactId);
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append('[');

		result.append(groupId);
		result.append(':');
		result.append(artifactId);

		if (version != null) {
			result.append(':');
			result.append(version);
		}

		if (extension != null) {
			result.append(':');
			result.append(extension);
		}

		result.append(']');
		return result.toString();
	}
}
