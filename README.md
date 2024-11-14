[![build status](https://img.shields.io/github/actions/workflow/status/fedora-java/xmvn/maven.yml?branch=master)](https://github.com/fedora-java/xmvn/actions/workflows/maven.yml?query=branch%3Amaster)



    ___  ___ __  __
    \  \/  /|  \/  |__ __ ___
     \    / | |\/| |\ V /| ' \
     /    \ |_|  |_| \_/ |_||_|  v. 5.0.0-SNAPSHOT
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


Contact
-------

XMvn is a community project, new contributotrs are welcome. The most
straightforward way to introduce new features is writting them yourself.
The preferred way to requests features and report bugs is through Github.

The easiest way to get support is asking on IRC -- #fedora-java on freenode.
You can also write to Fedora Java list <java-devel@lists.fedoraproject.org>
or directly to XMVn maintainers <xmvn-owner@fedoraproject.org>.
