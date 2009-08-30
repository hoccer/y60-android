#!/bin/sh

FILE=$1

cat $FILE | \
sed -e '/Logger\.v/d' > $FILE

