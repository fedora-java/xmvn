#!/bin/sh
set -e

version=$1
test -n "$version"

wget http://services.gradle.org/distributions/gradle-$version-bin.zip
unzip gradle-$version-bin.zip

for artifactId in gradle-base-services gradle-base-services-groovy gradle-core gradle-dependency-management gradle-resources; do
    file=$PWD/gradle-$version/lib/$artifactId-$version.jar
    test -e $file || file=$PWD/gradle-$version/lib/plugins/$artifactId-$version.jar

    mvn deploy:deploy-file \
	-DrepositoryId=xmvn \
	-Durl=file:$PWD/repo \
	-Dfile=$file \
	-DgroupId=org.gradle \
	-DartifactId=$artifactId \
	-Dversion=$version
done

(cd ./repo/org && tar cz gradle) | ssh people.redhat.com "cd public_html/xmvn/repo/org && rm -rf gradle && tar xzv"
