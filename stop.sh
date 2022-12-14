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

filename="xwiki-platform-distribution-flavor-jetty-hsqldb-$xwiki_version"

assert_success cd xwiki
assert_success cd "$filename"

assert_success chmod +x stop_xwiki.sh

assert_success ./stop_xwiki.sh

echo "XWiki has been stopped"