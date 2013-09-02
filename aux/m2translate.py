#!/usr/bin/env python
# Copyright (c) 2013 Red Hat, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Written by Michal Srb <msrb@redhat.com>

# translate Maven local repository (~/.m2) into the repository
# structure used in Fedora

import os
import re
import random
from shutil import copyfile


class Artifact:
    def __init__(self, gid, aid, version, classifier, extension, path):
        self.gid = gid
        self.aid = aid
        self.version = version
        self.classifier = classifier
        self.extension = extension
        self.path = path

    def __getitem__(self, index):
        return self.__dict__[index]


def parse_path(path):
 
    result = re.search('(.*)/([^/]*)/([^/]*)/\\2-\\3-?([^/.]*)\.([^/.]*)$', path)
    if result:
        gid = result.group(1).replace('/', '.')
        aid = result.group(2)
        version = result.group(3)
        classifier = result.group(4)
        extension = result.group(5)
        #print "%s %s %s %s %s" % (gid, aid, version, classifier, extension)

        return Artifact(gid, aid, version, classifier, extension, os.path.join(PATH_PREFIX, path))

    return None


def create_depmap(artifact, compat, subdir):

    version = ''
    if compat:
        version = "\n%s<version>%s</version>" % (' ' * 8, artifact.version)

    classifier = ''
    if artifact.classifier != '':
        classifier = "\n%s<classifier>%s</classifier>" % (' ' * 8, artifact.classifier)

    extension = ''
    if artifact.extension not in ['', 'pom', 'jar']:
        extension = "\n%s<extension>%s</extension>" % (' ' * 8, artifact.extension)

    if len(subdir) > 0:
        subdir = "/%s" % subdir

    depmap="""
<dependency>
    <maven>
        <groupId>%s</groupId>
        <artifactId>%s</artifactId>%s%s%s
    </maven>
    <jpp>
        <groupId>JPP%s</groupId>
        <artifactId>%s</artifactId>%s%s%s
    </jpp>
</dependency>\n"""

    return depmap % (artifact.gid, artifact.aid, version, classifier,\
                     extension, subdir, artifact.aid, version,\
                     classifier, extension)


def copy_jar(artifact, compat, subdir):

    filename = artifact.aid

    if compat:
        filename = "%s-%s" % (artifact.aid, artifact.version)

    if len(artifact.classifier) != 0:
        filename = "%s-%s" % (filename, artifact.classifier)

    if len(subdir) > 0:
        create_dir(os.path.join(JAVADIR, subdir))

    filename = "%s.%s" % (filename, artifact.extension)

    copyfile(artifact.path, os.path.join(JAVADIR, subdir, filename))

    return filename


def copy_pom(artifact, compat, subdir):

    if len(subdir) > 0:
        subdir = ".%s-" % subdir
    else:
        subdir = '-'

    filename = "JPP%s%s%s" % (subdir, artifact.aid, ".pom")

    if compat:
        root, exp = os.path.splitext(filename)
        filename = "%s-%s%s" % (root, artifact.version, exp)

    copyfile(artifact.path, os.path.join(JAVAPOMS, filename))

    return filename


def create_dir(dirpath):
    try:
        os.makedirs(dirpath)
    except OSError:
        pass


config_header = """
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-dependency-plugin</artifactId>
  <version>2.8</version>
  <executions>
    <execution>
      <id>copy</id>
      <phase>package</phase>
      <goals>
        <goal>copy</goal>
      </goals>
      <configuration>
        <artifactItems>
"""

config_footer = """
        </artifactItems>
      </configuration>
    </execution>
  </executions>
</plugin>
"""


def create_config_entry(artifact, filename, subdir):

    classifier = artifact.classifier
    classifier_conf = ''
    if len(classifier) > 0:
        classifier = '-' + classifier
        classifier_conf = '\n%s<classifier>%s</classifier>' % (' ' * 12, artifact.classifier)

    if artifact.extension == 'pom':
        dest = JAVAPOMS
    else:
        dest = os.path.join(JAVADIR, subdir)

    return """
          <artifactItem>
            <groupId>%s</groupId>
            <artifactId>%s</artifactId>
            <version>%s</version>
            <type>%s</type>%s
            <outputDirectory>%s</outputDirectory>
            <destFileName>%s</destFileName>
          </artifactItem>""" % (artifact.gid, artifact.aid, artifact.version,\
                                artifact.extension, classifier_conf, dest, filename)


M2DIR=os.environ['HOME'] + "/.m2"
PATH_PREFIX=os.path.join(M2DIR, "repository/")
JAVADIR="./java"
JAVAFRAGMENTSDIR="./maven-fragments"
JAVAPOMS="./maven-poms"
JAVAFRAGMENTFILE="maven-fragments"


if __name__ == "__main__":

    artifacts=dict()
    used_versions=list()

    create_dir(JAVADIR)
    create_dir(JAVAFRAGMENTSDIR)
    create_dir(JAVAPOMS)

    try:
        os.remove(os.path.join(JAVAFRAGMENTSDIR, JAVAFRAGMENTFILE))
    except (OSError):
       pass

    for dirname, dirnames, filenames in os.walk(M2DIR):
    
        # gather information about artifacts in M2 directory
        for filename in filenames:
            fullpath = os.path.join(PATH_PREFIX, dirname, filename)
            repopath = os.path.join(dirname, filename)[len(PATH_PREFIX):]

            artifact = parse_path(repopath)
            if artifact:
                key = "%s:%s:%s" % (artifact.gid, artifact.aid, artifact.version)
                if key not in artifacts:
                    artifacts[key] = list()
                artifacts[key].append(artifact)

    print config_header

    #process artifacts
    for key in artifacts:
        values = artifacts[key]

        compat = False
        used = "%s:%s" % (values[0].gid, values[0].aid)
        if used in used_versions:
            # versioned jar
            compat = True
        else:
            used_versions.append(used)

        subdir = ''
        if random.choice([True, False]):
            subdir = values[0].aid

        for artifact in values:
            if artifact.extension == "pom":
                filename = copy_pom(artifact, compat, subdir)
            else:
                filename = copy_jar(artifact, compat, subdir)

            print create_config_entry(artifact, filename, subdir)
    
            depmap = create_depmap(artifact, compat, subdir)
            with open(os.path.join(JAVAFRAGMENTSDIR, JAVAFRAGMENTFILE), "a") as fragmentfile:
                fragmentfile.write(depmap)
    
    print config_footer

