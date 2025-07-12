[![build status](https://img.shields.io/github/actions/workflow/status/fedora-java/xmvn/maven.yml?branch=master)](https://github.com/fedora-java/xmvn/actions/workflows/maven.yml?query=branch%3Amaster)
[![License](https://img.shields.io/github/license/fedora-java/xmvn.svg?label=License)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central version](https://img.shields.io/maven-central/v/org.fedoraproject.xmvn/xmvn.svg?label=Maven%20Central)](https://search.maven.org/artifact/org.fedoraproject.xmvn/xmvn)
![Fedora Rawhide version](https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fmdapi.fedoraproject.org%2Frawhide%2Fpkg%2Fxmvn5&query=%24.version&label=Fedora%20Rawhide)
[![Javadoc](https://javadoc.io/badge2/org.fedoraproject.xmvn/xmvn-api/javadoc.svg)](https://javadoc.io/doc/org.fedoraproject.xmvn/xmvn-api)



    ___  ___ __  __
    \  \/  /|  \/  |__ __ ___
     \    / | |\/| |\ V /| ' \
     /    \ |_|  |_| \_/ |_||_|  v. 5.2.0-SNAPSHOT
    /__/\  \
         \_/   ~ intelligent packaging ~

https://fedora-java.github.io/xmvn/

XMvn is a set of extensions for Apache Maven that can be used to
manage system artifact repository and use it to resolve Maven
artifacts in offline mode. It also provides Maven plugins to help with
creating RPM packages containing Maven artifacts.

XMvn is free software. You can redistribute and/or modify it under the
terms of Apache License Version 2.0.

XMvn was written by Mikolaj Izdebski.


Running integration tests
-------------------------

After making code changes it is recommended to run integration tests
to verify that the change did not break anything.

To save time, integration tests are not ran during default Maven
build.  To run them you need to activate `run-its` profile.  For
example:

    mvn -Prun-its clean verify

Note that integration tests are ran as part of commit and pull request
validation.

Integration tests rely on dist tarball (`xmvn-${version}.tar.gz`) to
be assembled and available in local Maven repository.  When ITs are
ran from Maven from command line, the dist tarball should be unpacked
automatically.  But when ITs are ran from Eclipse JDT, the dist
tarball needs to be manually assembled and unpacked into the right
directory.  This can be done by first running `mvn install` from
command line on the whole XMvn project to assemble the `.tar.gz` and
install it into Maven local repository, and then unpacking it Eclipse
by clicking "xmvn-it" -> "Run As" -> "Maven Build", setting "Goals" to
"process-test-resources", setting "Profiles" to "eclipse,unpack-dist",
and ensuring that "Resolve Workspace artifacts" is not checked, then
clicking "Run".


Contact
-------

XMvn is a community project, new contributotrs are welcome. The most
straightforward way to introduce new features is writting them yourself.
The preferred way to requests features and report bugs is through Github.

The easiest way to get support is asking on IRC -- #fedora-java on freenode.
You can also write to Fedora Java list <java-devel@lists.fedoraproject.org>
or directly to XMVn maintainers <xmvn-owner@fedoraproject.org>.
