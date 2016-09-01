    ___  ___ __  __
    \  \/  /|  \/  |__ __ ___
     \    / | |\/| |\ V /| ' \
     /    \ |_|  |_| \_/ |_||_|  v. 2.6.0-SNAPSHOT
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

[![Build Status](https://travis-ci.org/fedora-java/xmvn.svg?branch=master)](https://travis-ci.org/fedora-java/xmvn)


Building
--------

Some parts of XMvn require Gradle, which is not available in Maven
Central repository.  Therefore the first time you build XMvn you'll
need to download and install required dependencies into Maven local
repository by running the following command:

    mvn -f libs install

After Gradle is available in your local repository you'll be able to
build XMvn using standard Maven commands, or import it into IDEs like
Eclipse.

In some scenarios, like automated builds or continous integration, it
may be useful to download Gradle libraries and build XMvn in one step.
This can be achieved by activating libs profile (`mvn -P libs ...`).
This profile is activated automatically when CI environmental variable
is set to true, for example on TravisCI.


Contact
-------

XMvn is a community project, new contributotrs are welcome. The most
straightforward way to introduce new features is writting them yourself.
The preferred way to requests features and report bugs is Red Hat Bugzilla:
  http://bugzilla.redhat.com/enter_bug.cgi?product=fedora&component=xmvn

The easiest way to get support is asking on IRC -- #fedora-java on freenode.
You can also write to Fedora Java list <java-devel@lists.fedoraproject.org>
or directly to XMVn maintainers <xmvn-owner@fedoraproject.org>.
