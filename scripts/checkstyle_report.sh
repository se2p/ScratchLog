#! /usr/bin/env bash

function read_dom() {
    local IFS=\>
    read -d \< ENTITY CONTENT
}

function map_severity() {
    local severity="$1"

    if [ "$severity" == "error" ]; then
        echo "critical"
    elif [ "$severity" == "warning" ]; then
        echo "major"
    elif [ "$severity" == "info" ]; then
        echo "minor"
    else
        echo "info"
    fi
}

function extract_value() {
    local input=$1
    local key=$2

    echo "$input" | sed -nE "s#.*$key=\"([^\"]+)\".*#\1#p"
}

workspace=$(pwd)
first=1
error_count=0

echo "["

while read_dom; do
    entity_type=$(echo "$ENTITY" | awk '{ print $1 }')
    if [[ "$entity_type" = "file" ]]; then
        current_file=$(echo "$ENTITY" | sed -nE "s#.*name=\"$workspace/([^\"]+)\".*#\1#p")
    fi

    if [[ "$entity_type" = "error" || "$entity_type" = "warning" || "$entity_type" = "info" ]]; then
        line=$(extract_value "$ENTITY" "line")
        severityCheckstyle=$(extract_value "$ENTITY" "severity")
        severity=$(map_severity "$severityCheckstyle")
        message=$(extract_value "$ENTITY" "message" | sed "s/&quot;/\\\\\"/g; s/&apos;/\\\\\"/g; s/&amp;/\&/g; s/&lt;/</g; s/&gt;/>/g")
        checksum=$(printf "%s %s %d" "$current_file" "$message" "$line" | sha1sum | awk '{ print $1 }')

        if [[ $first -ne 1 ]]; then
            echo ","
        fi

        printf '{ "description": "%s", "severity": "%s", "fingerprint": "%s", "location": { "path": "%s", "lines": { "begin": %d } } }' "$message" "$severity" "$checksum" "$current_file" "$line"

        first=0
        error_count=$((error_count + 1))
    fi
done

echo
echo "]"

exit 0
