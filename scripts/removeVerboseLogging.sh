#!/bin/sh

FILE=$1

cat $FILE | \
sed -n '1h;1!H;${;g;s/Logger\.v([^\;]*)\;/### removed logger ###/g;p;}' > /tmp/removeLogger.tmp #$FILE

cat /tmp/removeLogger.tmp | \
sed -e '/### removed logger ###/d' > $FILE

