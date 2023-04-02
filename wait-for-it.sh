while :
do
    (echo > /dev/tcp/$WAITFORIT_HOST/$WAITFORIT_PORT) >/dev/null 2>&1
    result=$?
    if [[ $result -eq 0 ]]; then
        break
    else
        echo "Waiting for $WAITFORIT_HOST:$WAITFORIT_PORT... Retrying in 1 second."
    fi
    sleep 1
done
