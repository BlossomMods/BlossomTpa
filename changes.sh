#!/bin/sh

filename='CHANGELOG.md'

# remove + metadata (supported mc version)
version=$(echo "$1" | cut -d + -f 1)

# find starting line (starting with `# $version')
startLine=$(grep -nxm 1 "# $version" "$filename" | cut -d : -f 1)

# add 2 to skip the heading & the blank line that follows it
startLine=$((startLine + 2))

# find end line (starting at $startLine + 2, starting with `# ')
endLine=$(sed -n "$startLine,\$ p" "$filename" | grep -nm 1 '^# ' | cut -d : -f 1)

# if $endLine is not empty,
if [ -n "$endLine" ]; then
    # find end line absolute position by adding $startLine & subtracting 3 for files starting at line 1, the next
    # heading, and the blank line that precedes it
    endLine=$((endLine + startLine - 3))
else
    # set it to the length of file, which is also the index of one to last line (the last line apparently isn't counted)
    endLine=$(wc -l < "$filename")
fi

# get changes between $startLine and $endLine
result=$(sed -n "$startLine,$endLine p" "$filename")

# verify that everything is fine
# the $result should be preceded & followed by 1 blank line
if [ "$(sed "$((startLine - 1))q;d" "$filename")" = "" ] && [ "$(sed "$(( endLine  + 1))q;d" "$filename")" = "" ]; then
    if [ -n "$2" ]; then
        echo "$result" > "$2"
    else
        echo "$result"
    fi
    exit 0
else
    # debug printing
    if [ -n "$DEBUG" ]; then
        echo "head: $startLine; tail: $endLine"
        [ "$(sed "$((startLine - 1))q;d" "$filename")" = "" ] && echo "head ok" || echo "head bad: $(sed "$((startLine - 1))q;d" "$filename")"
        [ "$(sed "$(( endLine  + 1))q;d" "$filename")" = "" ] && echo "tail ok" || echo "tail bad: $(sed "$(( endLine  + 1))q;d" "$filename")"
        echo "%EOF%$result%EOF%"
    fi
    exit 1
fi
