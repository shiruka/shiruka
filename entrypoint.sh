#!/bin/sh

set -e

MIN_MEMORY=${MIN_MEMORY:=512M}
MAX_MEMORY=${MAX_MEMORY:=1G}

java -Xms$MIN_MEMORY -Xmx$MAX_MEMORY $JAVA_FLAGS -jar /opt/shiruka/Shiruka.jar $SERVER_ARGUMENTS
