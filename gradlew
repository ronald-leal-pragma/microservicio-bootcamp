#!/usr/bin/env sh
##############################################################################
# Gradle start up script for UN*X
##############################################################################

# Resolve links - $0 may be a link
PRG="$0"
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`"/"$link
  fi
done

PRGDIR=`dirname "$PRG"`

DEFAULT_JVM_OPTS=""
APP_NAME="gradle"
APP_BASE_NAME=`basename "$0"`

CLASSPATH="$PRGDIR/gradle/wrapper/gradle-wrapper.jar"

if [ -z "$JAVA_HOME" ]; then
  JAVA_CMD=`which java 2>/dev/null`
else
  JAVA_CMD="$JAVA_HOME/bin/java"
fi

if [ -z "$JAVA_CMD" ]; then
  echo "Error: JAVA_HOME is not set and java not in PATH."
  exit 1
fi

exec "$JAVA_CMD" $DEFAULT_JVM_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
