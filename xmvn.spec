# RPM spec file for building pre-release RPM packages for testing.
# This file derrived from Fedora project and thus it is licensed under
# the following terms:
#
# Permission is hereby granted, free of charge, to any person obtaining
# a copy of this software and associated documentation files (the
# "Software"), to deal in the Software without restriction, including
# without limitation the rights to use, copy, modify, merge, publish,
# distribute, sublicense, and/or sell copies of the Software, and to
# permit persons to whom the Software is furnished to do so, subject to
# the following conditions:
#
# The above copyright notice and this permission notice shall be included
# in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
# EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
# MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
# IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
# CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
# TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
# SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


# XMvn uses OSGi environment provided by Tycho, it shouldn't require
# any additional bundles.
%global __requires_exclude %{?__requires_exclude:%__requires_exclude|}^osgi\\($

# Integration tests are disabled by default, but you can run them by
# adding "--with its" to rpmbuild or mock invocation.
%bcond_with its

%bcond_without gradle

Name:           xmvn
Version:        3.0.0
Release:        0.git.%(date +%%Y%%m%%d.%%H%%M%%S)
Summary:        Local Extensions for Apache Maven
License:        ASL 2.0
URL:            https://fedora-java.github.io/xmvn/
BuildArch:      noarch

Source0:        https://github.com/fedora-java/xmvn/releases/download/%{version}/xmvn-%{version}.tar.xz

BuildRequires:  maven >= 3.4.0
BuildRequires:  maven-local
BuildRequires:  beust-jcommander
BuildRequires:  cglib
BuildRequires:  maven-dependency-plugin
BuildRequires:  maven-plugin-build-helper
BuildRequires:  maven-assembly-plugin
BuildRequires:  maven-install-plugin
BuildRequires:  maven-plugin-plugin
BuildRequires:  objectweb-asm
BuildRequires:  modello
BuildRequires:  xmlunit
BuildRequires:  apache-ivy
BuildRequires:  junit
BuildRequires:  easymock
BuildRequires:  maven-invoker
BuildRequires:  plexus-containers-container-default
BuildRequires:  plexus-containers-component-annotations
BuildRequires:  plexus-containers-component-metadata
%if %{with gradle}
BuildRequires:  gradle >= 2.5
%endif

Requires:       %{name}-minimal = %{version}-%{release}
Requires:       maven >= 3.4.0

%description
This package provides extensions for Apache Maven that can be used to
manage system artifact repository and use it to resolve Maven
artifacts in offline mode, as well as Maven plugins to help with
creating RPM packages containing Maven artifacts.

%package        minimal
Summary:        Dependency-reduced version of XMvn
Requires:       maven-lib >= 3.4.0
Requires:       %{name}-api = %{version}-%{release}
Requires:       %{name}-connector-aether = %{version}-%{release}
Requires:       %{name}-core = %{version}-%{release}
Requires:       apache-commons-cli
Requires:       apache-commons-lang3
Requires:       atinject
Requires:       google-guice
Requires:       guava
Requires:       maven-lib
Requires:       maven-resolver-api
Requires:       maven-resolver-impl
Requires:       maven-resolver-spi
Requires:       maven-resolver-util
Requires:       maven-wagon-provider-api
Requires:       objectweb-asm
Requires:       plexus-cipher
Requires:       plexus-containers-component-annotations
Requires:       plexus-interpolation
Requires:       plexus-sec-dispatcher
Requires:       plexus-utils
Requires:       sisu-inject
Requires:       sisu-plexus
Requires:       slf4j

%description    minimal
This package provides minimal version of XMvn, incapable of using
remote repositories.

%package        parent-pom
Summary:        XMvn Parent POM

%description    parent-pom
This package provides XMvn parent POM.

%package        api
Summary:        XMvn API
Obsoletes:      %{name}-launcher < 3.0.0

%description    api
This package provides XMvn API module which contains public interface
for functionality implemented by XMvn Core.

%package        core
Summary:        XMvn Core

%description    core
This package provides XMvn Core module, which implements the essential
functionality of XMvn such as resolution of artifacts from system
repository.

%package        connector-aether
Summary:        XMvn Connector for Maven Resolver

%description    connector-aether
This package provides XMvn Connector for Maven Resolver, which
provides integration of Maven Resolver with XMvn.  It provides an
adapter which allows XMvn resolver to be used as Maven workspace
reader.

%if %{with gradle}
%package        connector-gradle
Summary:        XMvn Connector for Gradle

%description    connector-gradle
This package provides XMvn Connector for Gradle, which provides
integration of Gradle with XMvn.  It provides an adapter which allows
XMvn resolver to be used as Gradle resolver.
%endif

%package        connector-ivy
Summary:        XMvn Connector for Apache Ivy

%description    connector-ivy
This package provides XMvn Connector for Apache Ivy, which provides
integration of Apache Ivy with XMvn.  It provides an adapter which
allows XMvn resolver to be used as Ivy resolver.

%package        mojo
Summary:        XMvn MOJO

%description    mojo
This package provides XMvn MOJO, which is a Maven plugin that consists
of several MOJOs.  Some goals of these MOJOs are intended to be
attached to default Maven lifecycle when building packages, others can
be called directly from Maven command line.

%package        tools-pom
Summary:        XMvn Tools POM

%description    tools-pom
This package provides XMvn Tools parent POM.

%package        resolve
Summary:        XMvn Resolver

%description    resolve
This package provides XMvn Resolver, which is a very simple
commald-line tool to resolve Maven artifacts from system repositories.
Basically it's just an interface to artifact resolution mechanism
implemented by XMvn Core.  The primary intended use case of XMvn
Resolver is debugging local artifact repositories.

%package        bisect
Summary:        XMvn Bisect

%description    bisect
This package provides XMvn Bisect, which is a debugging tool that can
diagnose build failures by using bisection method.

%package        subst
Summary:        XMvn Subst

%description    subst
This package provides XMvn Subst, which is a tool that can substitute
Maven artifact files with symbolic links to corresponding files in
artifact repository.

%package        install
Summary:        XMvn Install

%description    install
This package provides XMvn Install, which is a command-line interface
to XMvn installer.  The installer reads reactor metadata and performs
artifact installation according to specified configuration.

%package        javadoc
Summary:        API documentation for %{name}

%description    javadoc
This package provides %{summary}.

%prep
%setup -q

# Bisect IT has no chances of working in local, offline mode, without
# network access - it needs to access remote repositories.
find -name BisectIntegrationTest.java -delete

# Resolver IT won't work either - it tries to execute JAR file, which
# relies on Class-Path in manifest, which is forbidden in Fedora...
find -name ResolverIntegrationTest.java -delete

%pom_remove_plugin -r :maven-site-plugin

%mvn_package ":xmvn{,-it}" __noinstall

%if %{without gradle}
%pom_disable_module xmvn-connector-gradle
%endif

# Upstream code quality checks, not relevant when building RPMs
%pom_remove_plugin -r :apache-rat-plugin
%pom_remove_plugin -r :maven-checkstyle-plugin
%pom_remove_plugin -r :jacoco-maven-plugin
# FIXME pom macros don't seem to support submodules in profile
%pom_remove_plugin :jacoco-maven-plugin xmvn-it

# remove dependency plugin maven-binaries execution
# we provide apache-maven by symlink
%pom_xpath_remove "pom:executions/pom:execution[pom:id[text()='maven-binaries']]"

# Don't put Class-Path attributes in manifests
%pom_remove_plugin :maven-jar-plugin xmvn-tools

# get mavenVersion that is expected
maven_home=$(readlink -f $(dirname $(readlink $(which mvn)))/..)
mver=$(sed -n '/<mavenVersion>/{s/.*>\(.*\)<.*/\1/;p}' \
           xmvn-parent/pom.xml)
mkdir -p target/dependency/
cp -aL ${maven_home} target/dependency/apache-maven-$mver

%build
%if %{with its}
%mvn_build -s -j -- -Prun-its
%else
%mvn_build -s -j
%endif

tar --delay-directory-restore -xvf target/*tar.bz2
chmod -R +rwX %{name}-%{version}*
# These are installed as doc
rm -f %{name}-%{version}*/{AUTHORS-XMVN,README-XMVN.md,LICENSE,NOTICE,NOTICE-XMVN}
# Not needed - we use JPackage launcher scripts
rm -Rf %{name}-%{version}*/lib/{installer,resolver,subst,bisect}/
# Irrelevant Maven launcher scripts
rm -f %{name}-%{version}*/bin/{mvn.cmd,mvnDebug.cmd,mvn-script}


%install
%mvn_install

maven_home=$(readlink -f $(dirname $(readlink $(which mvn)))/..)

install -d -m 755 %{buildroot}%{_datadir}/%{name}
cp -r %{name}-%{version}*/* %{buildroot}%{_datadir}/%{name}/

for cmd in mvn mvnDebug mvnyjp; do
    cat <<EOF >%{buildroot}%{_datadir}/%{name}/bin/$cmd
#!/bin/sh -e
export _FEDORA_MAVEN_HOME="%{_datadir}/%{name}"
exec ${maven_home}/bin/$cmd "\${@}"
EOF
    chmod 755 %{buildroot}%{_datadir}/%{name}/bin/$cmd
done

# helper scripts
%jpackage_script org.fedoraproject.xmvn.tools.bisect.BisectCli "" "-Dxmvn.home=%{_datadir}/%{name}" xmvn/xmvn-bisect:beust-jcommander:maven-invoker:plexus/utils xmvn-bisect
%jpackage_script org.fedoraproject.xmvn.tools.install.cli.InstallerCli "" "" xmvn/xmvn-install:xmvn/xmvn-api:xmvn/xmvn-core:beust-jcommander:slf4j/api:slf4j/simple:objectweb-asm/asm xmvn-install
%jpackage_script org.fedoraproject.xmvn.tools.resolve.ResolverCli "" "" xmvn/xmvn-resolve:xmvn/xmvn-api:xmvn/xmvn-core:beust-jcommander xmvn-resolve
%jpackage_script org.fedoraproject.xmvn.tools.subst.SubstCli "" "" xmvn/xmvn-subst:xmvn/xmvn-api:xmvn/xmvn-core:beust-jcommander xmvn-subst

# copy over maven lib directory
cp -r ${maven_home}/lib/* %{buildroot}%{_datadir}/%{name}/lib/

# possibly recreate symlinks that can be automated with xmvn-subst
%{name}-subst -s -R %{buildroot} %{buildroot}%{_datadir}/%{name}/

# /usr/bin/xmvn
ln -s %{_datadir}/%{name}/bin/mvn %{buildroot}%{_bindir}/%{name}

# mvn-local symlink
ln -s %{name} %{buildroot}%{_bindir}/mvn-local

# make sure our conf is identical to maven so yum won't freak out
install -d -m 755 %{buildroot}%{_datadir}/%{name}/conf/
cp -P ${maven_home}/conf/settings.xml %{buildroot}%{_datadir}/%{name}/conf/
cp -P ${maven_home}/bin/m2.conf %{buildroot}%{_datadir}/%{name}/bin/

%files
%{_bindir}/mvn-local
%{_datadir}/%{name}/lib/aopalliance.jar
%{_datadir}/%{name}/lib/cdi-apicdi-api.jar
%{_datadir}/%{name}/lib/commons-codec.jar
%{_datadir}/%{name}/lib/commons-io.jar
%{_datadir}/%{name}/lib/commons-lang.jar
%{_datadir}/%{name}/lib/commons-logging.jar
%{_datadir}/%{name}/lib/httpcomponents_httpclient.jar
%{_datadir}/%{name}/lib/httpcomponents_httpcore.jar
%{_datadir}/%{name}/lib/jsoup_jsoup.jar
%{_datadir}/%{name}/lib/jsr-305.jar
%{_datadir}/%{name}/lib/maven-resolver_maven-resolver-connector-basic.jar
%{_datadir}/%{name}/lib/maven-resolver_maven-resolver-transport-wagon.jar
%{_datadir}/%{name}/lib/maven-wagon_file.jar
%{_datadir}/%{name}/lib/maven-wagon_http-shaded.jar
%{_datadir}/%{name}/lib/maven-wagon_http-shared.jar

%files minimal
%{_bindir}/%{name}
%dir %{_datadir}/%{name}
%dir %{_datadir}/%{name}/bin
%dir %{_datadir}/%{name}/lib
%exclude %{_datadir}/%{name}/lib/aopalliance.jar
%exclude %{_datadir}/%{name}/lib/cdi-apicdi-api.jar
%exclude %{_datadir}/%{name}/lib/commons-codec.jar
%exclude %{_datadir}/%{name}/lib/commons-io.jar
%exclude %{_datadir}/%{name}/lib/commons-lang.jar
%exclude %{_datadir}/%{name}/lib/commons-logging.jar
%exclude %{_datadir}/%{name}/lib/httpcomponents_httpclient.jar
%exclude %{_datadir}/%{name}/lib/httpcomponents_httpcore.jar
%exclude %{_datadir}/%{name}/lib/jsoup_jsoup.jar
%exclude %{_datadir}/%{name}/lib/jsr-305.jar
%exclude %{_datadir}/%{name}/lib/maven-resolver_maven-resolver-connector-basic.jar
%exclude %{_datadir}/%{name}/lib/maven-resolver_maven-resolver-transport-wagon.jar
%exclude %{_datadir}/%{name}/lib/maven-wagon_file.jar
%exclude %{_datadir}/%{name}/lib/maven-wagon_http-shaded.jar
%exclude %{_datadir}/%{name}/lib/maven-wagon_http-shared.jar
%{_datadir}/%{name}/lib/*.jar
%{_datadir}/%{name}/lib/ext
%{_datadir}/%{name}/bin/m2.conf
%{_datadir}/%{name}/bin/mvn
%{_datadir}/%{name}/bin/mvnDebug
%{_datadir}/%{name}/bin/mvnyjp
%{_datadir}/%{name}/boot
%{_datadir}/%{name}/conf

%files parent-pom -f .mfiles-xmvn-parent
%doc LICENSE NOTICE

%files core -f .mfiles-xmvn-core

%files api -f .mfiles-xmvn-api
%doc LICENSE NOTICE
%doc AUTHORS README.md

%files connector-aether -f .mfiles-xmvn-connector-aether

%if %{with gradle}
%files connector-gradle -f .mfiles-xmvn-connector-gradle
%endif

%files connector-ivy -f .mfiles-xmvn-connector-ivy

%files mojo -f .mfiles-xmvn-mojo

%files tools-pom -f .mfiles-xmvn-tools

%files resolve -f .mfiles-xmvn-resolve
%{_bindir}/%{name}-resolve

%files bisect -f .mfiles-xmvn-bisect
%{_bindir}/%{name}-bisect

%files subst -f .mfiles-xmvn-subst
%{_bindir}/%{name}-subst

%files install -f .mfiles-xmvn-install
%{_bindir}/%{name}-install

%files javadoc
%doc LICENSE NOTICE

%changelog
* Mon Aug 15 2016 Michael Simacek <msimacek@redhat.com> - 2.5.0-15
- Switch launcher scripts

* Thu Aug 11 2016 Michael Simacek <msimacek@redhat.com> - 2.5.0-14
- Add Requires on all symlinked jars to xmvn-minimal

* Mon Aug  8 2016 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.5.0-13
- Remove temp symlinks

* Mon Aug  8 2016 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.5.0-12
- Add temp symlinks needed for updating to Maven 3.4.0

* Mon Jul 04 2016 Michael Simacek <msimacek@redhat.com> - 2.5.0-11
- Don't install POM files for Tycho projects

* Thu Jun 30 2016 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.5.0-10
- Full xmvn should require full maven

* Tue Jun 28 2016 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.5.0-9
- Introduce xmvn-minimal subpackage

* Wed Jun 15 2016 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.5.0-8
- Add missing build-requires

* Mon May 30 2016 Michael Simacek <msimacek@redhat.com> - 2.5.0-7
- Add missing BR easymock

* Fri Feb 05 2016 Fedora Release Engineering <releng@fedoraproject.org> - 2.5.0-6
- Rebuilt for https://fedoraproject.org/wiki/Fedora_24_Mass_Rebuild

* Thu Nov 26 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.5.0-5
- Try to procect builddep MOJO against patological cases

* Mon Nov 23 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.5.0-4
- Remove temporary Maven 3.3.9 workaround

* Mon Nov 23 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.5.0-3
- Add temporary workaround for Maven 3.3.9 transition

* Wed Oct 28 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.5.0-2
- Fix symlinks in lib/core

* Wed Oct 28 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.5.0-1
- Update to upstream version 2.5.0

* Tue Jul 14 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.4.0-5
- Require persistent artifact files in XML resolver API

* Tue Jun 30 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.4.0-4
- Port to Gradle 2.5-rc-1

* Fri Jun 19 2015 Fedora Release Engineering <rel-eng@lists.fedoraproject.org> - 2.4.0-3
- Rebuilt for https://fedoraproject.org/wiki/Fedora_23_Mass_Rebuild

* Mon May 11 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.4.0-2
- Add patches for rhbz#1220394

* Wed May  6 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.4.0-1
- Update to upstream version 2.4.0

* Fri Apr 24 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.3.2-8
- Port to Gradle 2.4-rc-1

* Thu Apr 16 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.3.2-7
- Disable doclint in javadoc:aggregate MOJO executions

* Thu Apr  9 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.3.2-6
- Install mvn-local symlink

* Wed Mar 25 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.3.2-5
- Remove workarunds for RPM bug #646523

* Wed Mar 25 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.3.2-4
- Port to Gradle 2.3

* Mon Mar 16 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.3.2-3
- Build with Maven 3.3.0

* Mon Mar 16 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.3.2-2
- Add temporary explicit maven-builder-support.jar symlink

* Thu Mar 12 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.3.2-1
- Update to upstream version 2.3.2

* Fri Mar 06 2015 Michal Srb <msrb@redhat.com> - 2.3.1-4
- Rebuild to fix symlinks in lib/core

* Thu Feb 19 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.3.1-3
- Remove temporary explicit ASM symlinks

* Wed Feb 18 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.3.1-2
- Temporarly add explicit symlinks to ASM

* Fri Feb 13 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.3.1-1
- Update to upstream version 2.3.1

* Wed Feb 11 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.3.0-1
- Update to upstream version 2.3.0

* Wed Feb  4 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.2.1-1
- Update to upstream version 2.2.1

* Fri Jan 23 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.2.0-1
- Update to upstream version 2.2.0
- Add connector-gradle subpackage

* Wed Jan 21 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.1.1-2
- Add BR on maven-site-plugin
- Resolves: rhbz#1184608

* Mon Jan  5 2015 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.1.1-1
- Update to upstream version 2.1.1

* Wed Dec 10 2014 Michal Srb <msrb@redhat.com> - 2.1.0-8
- Add fully qualified osgi version to install plan when tycho detected
- Resolves: rhbz#1172225

* Thu Dec  4 2014 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.1.0-7
- Ignore any system dependencies in Tycho projects

* Wed Nov 26 2014 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.1.0-6
- Use topmost repository namespace during installation
- Resolves: rhbz#1166743

* Tue Oct 28 2014 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.1.0-5
- Fix conversion of Ivy to XMvn artifacts
- Resolves: rhbz#1127804

* Mon Oct 13 2014 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.1.0-4
- Fix FTBFS caused by new wersion of plexus-archiver

* Wed Sep 24 2014 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.1.0-3
- Fix installation of attached Eclipse artifacts

* Wed Sep 10 2014 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.1.0-2
- Avoid installing the same attached artifact twice

* Thu Sep  4 2014 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.1.0-1
- Update to upstream version 2.1.0
- Remove p2 subpackage

* Fri Jun  6 2014 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.0.1-1
- Update to upstream version 2.0.1

* Thu Jun  5 2014 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.0.0-6
- Bump Maven version in build-requires

* Thu Jun  5 2014 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.0.0-5
- Add missing requires on subpackages

* Fri May 30 2014 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.0.0-4
- Don't modify system properties during artifact resolution

* Fri May 30 2014 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.0.0-3
- Add patch to support xmvn.resolver.disableEffectivePom property

* Thu May 29 2014 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.0.0-2
- Add patch for injecting Javapackages manifests

* Thu May 29 2014 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.0.0-1
- Update to upstream version 2.0.0

* Tue Apr 22 2014 Mikolaj Izdebski <mizdebsk@redhat.com> - 1.5.0-0.25.gitcb3a0a6
- Use ASM 5.0.1 directly instead of Sisu-shaded ASM

* Fri Mar 28 2014 Mikolaj Izdebski <mizdebsk@redhat.com> - 1.5.0-0.24.gitcb3a0a6
- Override extensions of skipped artifacts

* Fri Mar 28 2014 Mikolaj Izdebski <mizdebsk@redhat.com> - 1.5.0-0.23.gitcb3a0a6
- Skip installation of artifacts which files are not regular files
- Resolves: rhbz#1078967

* Mon Mar 17 2014 Michal Srb <msrb@redhat.com> - 1.5.0-0.22.gitcb3a0a6
- Add missing BR: modello-maven-plugin

* Tue Mar 04 2014 Stanislav Ochotnicky <sochotnicky@redhat.com> - 1.5.0-0.21.gitcb3a0a6
- Use Requires: java-headless rebuild (#1067528)

* Wed Feb 19 2014 Mikolaj Izdebski <mizdebsk@redhat.com> - 1.5.0-0.20.gitcb3a0a6
- Fix unowned directory

* Tue Jan 14 2014 Mikolaj Izdebski <mizdebsk@redhat.com> - 1.5.0-0.19.gitcb3a0a6
- Update to pre-release of upstream version 1.5.0

* Mon Dec  9 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 1.4.0-1
- Update to upstream version 1.4.0

* Thu Nov 14 2013 Michael Simacek <msimacek@redhat.com> - 1.3.0-4
- Update to Sisu 0.1.0

* Thu Nov 14 2013 Michal Srb <msrb@redhat.com> - 1.3.0-3
- Add dep org.sonatype.sisu:sisu-guice::no_aop:

* Fri Nov  8 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 1.3.0-2
- Add wagon-http-shared4 to plexus.core

* Wed Nov 06 2013 Stanislav Ochotnicky <sochotnicky@redhat.com> - 1.3.0-1
- Update to upstream release 1.3.0

* Tue Nov  5 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 1.2.0-5
- Require Maven >= 3.1.1-5
- Resolves: rhbz#1014355

* Wed Oct 23 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 1.2.0-4
- Rebuild to regenerate broken POMs
- Related: rhbz#1021484

* Wed Oct 23 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 1.2.0-3
- Temporarly skip running tests

* Wed Oct 23 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 1.2.0-2
- Don't inject manifest if it does not already exist
- Resolves: rhbz#1021484

* Fri Oct 18 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 1.2.0-1
- Update to upstream version 1.2.0

* Mon Oct 07 2013 Stanislav Ochotnicky <sochotnicky@redhat.com> - 1.1.0-2
- Apply patch for rhbz#1015596

* Tue Oct 01 2013 Stanislav Ochotnicky <sochotnicky@redhat.com> - 1.1.0-1
- Update to upstream version 1.1.0

* Fri Sep 27 2013 Stanislav Ochotnicky <sochotnicky@redhat.com> - 1.0.2-3
- Add __default package specifier support

* Mon Sep 23 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 1.0.2-2
- Don't try to relativize symlink targets
- Restotre support for relative symlinks

* Fri Sep 20 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 1.0.2-1
- Update to upstream version 1.0.2

* Tue Sep 10 2013 Stanislav Ochotnicky <sochotnicky@redhat.com> - 1.0.0-2
- Workaround broken symlinks for core and connector (#986909)

* Mon Sep 09 2013 Stanislav Ochotnicky <sochotnicky@redhat.com> - 1.0.0-1
- Updating to upstream 1.0.0

* Tue Sep  3 2013 Stanislav Ochotnicky <sochotnicky@redhat.com> 1.0.0-0.2.alpha1
- Update to upstream version 1.0.0 alpha1

* Sun Aug 04 2013 Fedora Release Engineering <rel-eng@lists.fedoraproject.org> - 0.5.1-4
- Rebuilt for https://fedoraproject.org/wiki/Fedora_20_Mass_Rebuild

* Tue Jul 23 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.5.1-3
- Rebuild without bootstrapping

* Tue Jul 23 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.5.1-2
- Install symlink to simplelogger.properties in %{_sysconfdir}

* Tue Jul 23 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.5.1-1
- Update to upstream version 0.5.1

* Tue Jul 23 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.5.0-7
- Allow installation of Eclipse plugins in javadir

* Mon Jul 22 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.5.0-6
- Remove workaround for plexus-archiver bug
- Use sonatype-aether symlinks

* Wed Jun  5 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.5.0-5
- Fix resolution of tools.jar

* Fri May 31 2013 Stanislav Ochotnicky <sochotnicky@redhat.com> - 0.5.0-4
- Fix handling of packages with dots in groupId
- Previous versions also fixed bug #948731

* Tue May 28 2013 Stanislav Ochotnicky <sochotnicky@redhat.com> - 0.5.0-3
- Move pre scriptlet to pretrans and implement in lua

* Fri May 24 2013 Stanislav Ochotnicky <sochotnicky@redhat.com> - 0.5.0-2
- Fix upgrade path scriptlet
- Add patch to fix NPE when debugging is disabled

* Fri May 24 2013 Stanislav Ochotnicky <sochotnicky@redhat.com> - 0.5.0-1
- Update to upstream version 0.5.0

* Fri May 17 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.4.2-3
- Add patch: install MOJO fix

* Wed Apr 17 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.4.2-2
- Update plexus-containers-container-default JAR location

* Tue Apr  9 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.4.2-1
- Update to upstream version 0.4.2

* Thu Mar 21 2013 Michal Srb <msrb@redhat.com> - 0.4.1-1
- Update to upstream version 0.4.1

* Fri Mar 15 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.4.0-1
- Update to upstream version 0.4.0

* Fri Mar 15 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.4.0-0.7
- Enable tests

* Thu Mar 14 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.4.0-0.6
- Update to newer snapshot

* Wed Mar 13 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.4.0-0.5
- Update to newer snapshot

* Wed Mar 13 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.4.0-0.4
- Set proper permissions for scripts in _bindir

* Tue Mar 12 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.4.0-0.3
- Update to new upstream snapshot
- Create custom /usr/bin/xmvn instead of using %%jpackage_script
- Mirror maven directory structure
- Add Plexus Classworlds config file

* Wed Mar  6 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.4.0-0.2
- Update to newer snapshot

* Wed Mar  6 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.4.0-0.1
- Update to upstream snapshot of version 0.4.0

* Mon Feb 25 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.3.1-2
- Install effective POMs into a separate directory

* Thu Feb  7 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.3.1-1
- Update to upstream version 0.3.1

* Tue Feb  5 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.3.0-1
- Update to upstream version 0.3.0
- Don't rely on JPP symlinks when resolving artifacts
- Blacklist more artifacts
- Fix dependencies

* Thu Jan 24 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.2.6-1
- Update to upstream version 0.2.6

* Mon Jan 21 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.2.5-1
- Update to upstream version 0.2.5

* Fri Jan 11 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.2.4-1
- Update to upstream version 0.2.4

* Wed Jan  9 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.2.3-1
- Update to upstream version 0.2.3

* Tue Jan  8 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.2.2-1
- Update to upstream version 0.2.2

* Tue Jan  8 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.2.1-1
- Update to upstream version 0.2.1

* Mon Jan  7 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.2.0-1
- Update to upstream version 0.2.0
- New major features: depmaps, compat symlinks, builddep MOJO
- Install effective POMs for non-POM artifacts
- Multiple major and minor bugfixes
- Drop support for resolving artifacts from %%_javajnidir

* Fri Dec  7 2012 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.1.5-1
- Update to upstream version 0.1.5

* Fri Dec  7 2012 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.1.4-1
- Update to upstream version 0.1.4

* Fri Dec  7 2012 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.1.3-1
- Update to upstream version 0.1.3

* Fri Dec  7 2012 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.1.2-1
- Update to upstream version 0.1.2

* Fri Dec  7 2012 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.1.1-1
- Update to upstream version 0.1.1

* Thu Dec  6 2012 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.1.0-1
- Update to upstream version 0.1.0
- Implement auto requires generator

* Mon Dec  3 2012 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.0.2-1
- Update to upstream version 0.0.2

* Thu Nov 29 2012 Mikolaj Izdebski <mizdebsk@redhat.com> - 0.0.1-1
- Update to upstream version 0.0.1

* Wed Nov 28 2012 Mikolaj Izdebski <mizdebsk@redhat.com> - 0-2
- Add jpackage scripts

* Mon Nov  5 2012 Mikolaj Izdebski <mizdebsk@redhat.com> - 0-1
- Initial packaging
