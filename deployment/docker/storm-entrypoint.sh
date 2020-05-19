#!/bin/sh

# if command starts with something that is not executable, prepend our deploy command
if ! which "${1}" >/dev/null; then
  set -- storm -c nimbus.seeds="${NIMBUS_SEEDS:-"[\"nimbus\"]"}" jar $TOPOLOGY_JAR $TOPOLOGY_CLASS "$@"
fi

exec "$@"
