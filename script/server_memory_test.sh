#!/bin/bash
# script to load all existing XHTML pages of the IPAR frontend in memory


# following only for timing & performance testing remove this for the final version for PROD
# prints time gone since $1 as a timestamp in seconds
function print_time {
    start=$1
    now=`date +%s`
    (( now -= start ))
    (( mins = now / 60 ))
    (( secs = now % 60 ))
	if (( secs < 10 )) ; then secs="0$secs"; fi
    echo "--> took $mins:$secs"
}

function print_url {

	URL=$SERVER_HOST
	if [ $SERVER_PORT ]
	then

		URL=$URL:$SERVER_PORT$CONTEXT
	fi

	echo $URL
}


SEARCH=$1
START_TIMESTAMP=`date +%s`

# tomcat server to test
SERVER_HOST=http://localhost
SERVER_PORT=9000
CONTEXT=

WEB_BASE_DIR='../public'

HTMLCOUNT=0


echo search all $SEARCH
echo start on `date`
echo ""

HTML_FILES_CMD=`echo find $WEB_BASE_DIR -name $SEARCH -type f`
echo $HTML_FILES_CMD
HTML_FILES_ALL=`$HTML_FILES_CMD | wc -l`
HTML_FILES=`$HTML_FILES_CMD`

HTML_FILE_URL=`print_url`
echo getting cookie from $HTML_FILE_URL
curl -c /tmp/cookie.txt $HTML_FILE_URL > /dev/null 2>&1
cat /tmp/cookie.txt

for htmlFile in $HTML_FILES ; do

    htmlFileWithoutLeadingDot=`echo $htmlFile | sed -e 's/^\.*//'`
    #echo get $HTML_FILE_URL$htmlFileWithoutLeadingDot
    #curl $HTML_FILE_URL$htmlFileWithoutLeadingDot > /dev/null 2>&1
    curl -b /tmp/cookie.txt $HTML_FILE_URL$htmlFileWithoutLeadingDot > /dev/null 2>&1

    (( HTMLCOUNT += 1 ))

    # output formatting
    if [ `expr $HTMLCOUNT % 1000` -eq 0 ] ; then
        echo "| $HTMLCOUNT/$HTML_FILES_ALL ("`print_time $START_TIMESTAMP`")"
    elif [ `expr $HTMLCOUNT % 100` -eq 0 ] ; then
        echo -n "|"
    elif [ `expr $HTMLCOUNT % 10` -eq 0 ] ; then
        echo -n "."
    fi
done

echo
echo ----------------------------
echo -n "Fetched HTMLs: "
echo $HTMLCOUNT
echo -n "Overall time: "
print_time $START_TIMESTAMP
echo ----------------------------


