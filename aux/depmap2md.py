#!/usr/bin/env python
# Copyright (c) 2016-2019 Red Hat, Inc.
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
# Written by Mikolaj Izdebski <mizdebsk@redhat.com>

# Tries to convert legacy depmap fragment files to new metadata
# format.  Accepts optional argument - path prefix, for example
# chroot.  Metadata file is printed on stdout.

from xml.etree.ElementTree import ElementTree
import os
import sys
import glob

plan = False
namespace = None
for i in reversed(range(1, len(sys.argv))):
    if sys.argv[i] == '--plan':
        plan = True
        del sys.argv[i]
    elif sys.argv[i] == '--scl':
        del sys.argv[i]
        namespace = sys.argv[i]
        del sys.argv[i]

prefix = ''
if len(sys.argv) > 1:
    prefix = sys.argv[1]

md = {}
md_versionless = {}

def process_dep(dep, fragment, force_pom=False):
    maven = dep.find('maven')
    groupId = maven.find('groupId').text
    if groupId == 'org.fedoraproject.xmvn':
        return
    artifactId = maven.find('artifactId').text
    extension = maven.find('extension').text if maven.find('extension') is not None else 'jar'
    if force_pom:
        extension = 'pom'
    classifier = maven.find('classifier').text if maven.find('classifier') is not None else ''
    version = maven.find('version').text
    key = (groupId, artifactId, extension, classifier, version)
    jpp = dep.find('jpp')
    gid = jpp.find('groupId').text
    aid = jpp.find('artifactId').text
    if extension == 'pom':
        name = gid.replace('/', '.') + '-' + aid
        search = ['/usr/share/maven-effective-poms', '/usr/share/maven-poms']
    else:
        name = gid.replace('JPP', '') + '/' + aid
        search = ['/usr/share/java', '/usr/lib/java']
    if classifier:
        name = name + '-' + classifier
    name = name + '.' + extension
    found = False
    for dir in search:
        path = dir + '/' + name
        if os.path.exists(prefix + path):
            found = path.replace('//', '/')
            break

    if not found:
        # Can happen for ex. for JAR files without POM
        return

    compat = ""
    if jpp.find('version') is not None:
        compat = jpp.find('version').text

    entry = md.get(key, None)
    if not compat:
        versionless_key = (groupId, artifactId, extension, classifier)
        if md_versionless.get(versionless_key, None):
            # We already have metadata for this artifact!  Behaviour
            # of depmaps is to pick random implementation.  Lets avoid
            # adding duplicate metadata, which wloud cause XMvn to
            # ignore duplicates.
            return
        md_versionless[versionless_key] = True
    if not entry:
        entry = (found, [])
        md[key] = entry

    if compat:
        entry[1].append(compat)


for fragment in glob.glob(prefix + '/usr/share/maven-fragments/*'):
    for dep in ElementTree(file=fragment).findall('dependency'):
        process_dep(dep, fragment)
        process_dep(dep, fragment, True)



print "<metadata>"
if not plan:
    print "  <uuid>dummy</uuid>"
print "  <artifacts>"

for key, value in md.iteritems():

    print "    <artifact>"
    if not plan:
        print "      <uuid>dummy</uuid>"
    print "      <groupId>%s</groupId>" % key[0]
    print "      <artifactId>%s</artifactId>" % key[1]
    if key[2] != 'jar':
        print "      <extension>%s</extension>" % key[2]
    if key[3] != '':
        print "      <classifier>%s</classifier>" % key[3]
    print "      <version>%s</version>" % key[4]
    print "      <path>%s</path>" % value[0]
    if namespace:
        print "      <namespace>%s</namespace>" % namespace
    if value[1]:
        if plan:
            sys.stderr.write("Compat artifact: %s:%s:%s:%s:%s, versions: %s\n"
                             % (key[0], key[1], key[2], key[3], key[4], ", ".join(value[1])))
        else:
            print "      <compatVersions>"
            for v in value[1]:
                print "        <version>%s</version>" % v
            print "      </compatVersions>"
    print "      <properties>"
    print "        <xmvn.resolver.disableEffectivePom>true</xmvn.resolver.disableEffectivePom>"
    print "      </properties>"
    print "    </artifact>"

print "  </artifacts>"
print "</metadata>"
