#!/bin/sh

FILE=$1

cat $FILE | \
sed -n '1h;1!H;${;g;s/Logger\.v([^\;]*)\;//g;p;}' #> $FILE

