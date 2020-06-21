#! /bin/bash

function assert_success() {
    "${@}"
    local status=${?}
    if [ ${status} -ne 0 ]; then
        echo "### Error ${status} at: ${BASH_LINENO[*]} ###"
        exit ${status}
    fi
}

xwiki_version="$XWIKI_VERSION" # for case if it will be set up from environment variable
if [ -z "$xwiki_version" ]; then
    xwiki_version="11.10.5"
fi

url="https://maven.xwiki.org/releases/org/xwiki/platform/xwiki-platform-distribution-flavor-jetty-hsqldb/$xwiki_version/xwiki-platform-distribution-flavor-jetty-hsqldb-$xwiki_version.zip"
filename="xwiki-platform-distribution-flavor-jetty-hsqldb-$xwiki_version"
checkAddress=http://127.0.0.1:8080/xwiki/bin/get/Main

assert_success mkdir xwiki
assert_success cd xwiki

assert_success curl "$url" -o xwiki.zip
assert_success unzip xwiki.zip

assert_success cd "$filename"
assert_success chmod +x start_xwiki.sh

assert_success ./start_xwiki.sh &
assert_success curl --retry 5 --retry-delay 10 "$checkAddress"

echo "XWiki has been started"

sleep 60

assert_success curl "$checkAddress"
