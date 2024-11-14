Overview
========

XMvn is a set of free software components that are useful in packaging
Java software whose build is managed by Apache Maven.  XMvn maintains
a system-wide repository of artifacts, which it uses to build projects
without having to connect to the Internet to download artifacts from
remote Maven repositories, such as Maven Central.  XMvn also provides
plugins for Apache Maven that can help with creating and debugging RPM
packages containing Maven artifacts.  XMvn name is derived from "local
eXtensions for MaVeN".


Reasons for existence of XMvn
-----------------------------

According to one of Moore's laws, the volume of computer software
grows exponentially with time.  This is reflected in a fast increase
of the number of packages in GNU/Linux distributions.  Unfortunately
the number of contributors doesn't grow as fast.  There is a clear
need for better tooling so that package maintainers can keep up with
fast changes in the free software world.

Fortunately for Java packagers a current trend in Java build systems
is Apache Maven, which, as opposed to Apache Ant, is based on the
project object model (POM).  The model is stored in standardized XML
form in `pom.xml` files.  This form of managing project configuration
opens the door for improvements in packaging of Maven artifacts in
GNU/Linux distributions.

Most of information that need to be included in every RPM package is
already present in a Maven POM.  This includes simple things like
name, description or licensing, but also complex things such as
dependencies, which are even more powerful than RPM dependencies
(think of dependency scopes or exclusions).

Metadata redundancy creates a need for constantly synchronizing POM
metadata with RPM metadata with every package update.  Outdated
package description may be quite harmless, but incorrect dependencies
can result in severe packaging bugs.  Synchronizing dependencies is
very important, but tedious at the same time, which unfortunately
makes many packagers update dependencies carelessly or even skip this
step completely.  Inaccurate RPM requires can cause installation of
hundreds of unneeded packages, but it can also (more importantly)
break of other packages.

The `%install` section of RPM spec files is another example of
redundant data.  Traditionally each JAR or POM file would need to be
explicitly listed there, which wasn't as error-prone as listing
dependencies, but still a tedious task, which could be easily
automated.

Similar techniques are already in use.  Fedora has automatic requires
generator for Perl packages, there is also a tool for generation of
spec files.  Shared libraries have automatic requires generation in
all major Linux distributions.  These techniques have not yet been
widely adopted for Java packages, possibly because free Java was
available much later than other major programming languages used in
GNU/Linux.

Keeping RPM and upstream metadata in sync was the main reason for
creating XMvn.


Pros
----

XMvn offers significant advantages over the traditional ways of
packaging Java, especially in RPM-based distributions, such as
improved dependency management, faster updates or easy subpackage
creation.

Packages that are using XMvn to build are simpler and more readable.
This allows developers to maintain much more packages that they would
interesting or important tasks.

The simplicity of packages using XMvn allows more people to contribute
to Java packaging.  Maintenance of Maven packages no longer requires
deep knowledge of packaging guidelines and handling corner cases.
Only developers maintaining more complicated packages need to know
mode advanced aspects of packaging Java using XMvn.

Packages using XMvn can be updated much faster than traditional Java
packages.  Often just version bump is enough.  This is because most of
package metadata as well as `%build` and `%install` sections are
maintained by XMvn so there is no need to change them manually.

Using XMvn will usually result in a quality improvement.  One of
reasons for that is automated dependency management, which when done
manually was the source of many packaging bugs.  Improved quality is
also an indirect consequence of more readable spec files (bugs are
easier to notice).

Metadata redundancy is reduced in XMVn packages.  This means that if
someone wants to update some package information this can be done in a
single place.

Maintenance of multi-module packages is much easier with XMvn, almost
as easy as single-module packages.  "One artifact per package" rule is
closer to upstream packaging, improves granularity of packaging,
dependency graphs are more accurate.

The existence of XMvn allows upstream Maven to be used without any
patching.  To build packages with Maven a custom patch set had to be
maintained and rebased with each update of Maven.  XMvn obsoletes that
custom patches and allows Maven to be included in a distribution
unmodified.

Lastly, a very important advantage: changes in distribution policy are
much easier to implement as there is only one place to implement the
changes - XMvn configuration.  With traditional way of packaging any
change in packaging policy requires all affected packages to be
modified, which often was a difficult task.


Cons
----

As you may expect, these advantages comes at a price.  In XMvn's case,
there are several tradeoffs that have been made in order to allow its
function.  One of the greatest targets of complaints is the lack of
packager's control over what is happening behind the scenes.  This is
a valid concern, but every simplification is accompanied by a loss of
control.  To accommodate for this fact, XMvn does not prevent the
packager from adhering to the existing packaging customs, henceforth
leaving control in hands of those who require it.

In cases where a packaging bug occurs, using XMvn may under certain
circumstances make debugging more difficult compared to the
traditional way of Java packaging.  However, since most packages use a
rather standard way of packaging, in most cases no serious hardships
are expected, especially after the initial bugs are ironed out.

Even though XMvn allows the traditional way of packaging, it is not
always compatible with some older systems (to name a few: RHEL 5, RHEL
6, Fedora 18 and older).  In theory, this fact makes packager's work
harder by forcing him to keep two versions of the same package, but
since the updating policy of the affected systems allows only security
patches or the systems are beyond their projected life time, we do not
consider this a show stopper.


Future
------

Even though many hardships while packaging Java projects have already
been addressed by XMvn, many other are still untackled as of today.
In the near future, we expect XMvn to be able to completely generate
`*.spec` files from POM files.  While this might sound as too daring
an endeavor, most of the necessary data is already contained in POMs
(name, version, license, build dependencies, runtime dependencies, URI
of the project, SCM location, modules etc.).  Of course, the ability
to generate the spec file automatically doesn't mean that the package
maintainer may just run XMvn and file a package review request; while
most of the simple work will be automated, checking the resulting
file, verifying the content's accuracy or license information still
remains completely a responsibility of the maintainer.

As other likely features go, XMvn might not remain strictly a command
line-based tool.  Either a graphical interface or integration with
some Java IDE seem as valuable extension of the current program.
