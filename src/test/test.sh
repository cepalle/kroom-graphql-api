#!/usr/bin/env bash
echo "GET http://localhost:8080/graphql?query={PlayListEditorsPublic{name}}" | vegeta attack -duration=30s | tee results.bin | vegeta report
echo "GET http://localhost:8080/graphql?query={PlayListEditorsPublic{name}}" | vegeta attack -duration=0s > /dev/null
echo "GET http://localhost:8080/graphql?query={PlayListEditorsPublic{name}}" | vegeta attack -header "Kroom-token-id: yMAjqtKWFgjxRaQBpi5vGZdqz2_MfxzGmuZrPN0RxjWM0" -duration=0s > /dev/null

echo 'GET http://localhost:8080/graphql?query={DeezerTrack(id:3100000){track{title}errors{messages}}}' | vegeta attack -header "Kroom-token-id: yMAjqtKWFgjxRaQBpi5vGZdqz2_MfxzGmuZrPN0RxjWM0" -duration=0s > /dev/null

echo 'POST http://localhost:8080/graphql' | vegeta attack -body ~/api/src/test/test.json -header "Kroom-token-id: yMAjqtKWFgjxRaQBpi5vGZdqz2_MfxzGmuZrPN0RxjWM0" -header "Content-Type: application/json"  -duration=0s

echo 'POST http://localhost:8080/graphql' | vegeta attack -body ~/api/src/test/test.json -header "Kroom-token-id: yMAjqtKWFgjxRaQBpi5vGZdqz2_MfxzGmuZrPN0RxjWM0" -header "Content-Type: application/json"  -duration=30s | tee results.bin | vegeta report
# ---



{ DeezerSearch(search: "eminen", strict: false) { search { title album { title tracks { title album { title tracks { title album { title } } } } } } errors { field messages } } }
{ DeezerSearch(search: "eminen", strict: false) { search { title } } }
{ DeezerTrack(id: 3100000) { track { title } errors { field messages } } }

curl 'http://localhost:8080/graphql'
 -H 'Accept-Encoding: gzip, deflate, br'
 -H 'Content-Type: application/json'
 -H 'Accept: application/json'
 -H 'Connection: keep-alive'
 -H 'DNT: 1'
 -H 'Origin: http://localhost:8080'
 -H 'Kroom-token-id: IwxP5qXxKsUrqjSw1LfYEsGHbPIwkNH3qb3F3F2U2qonH'
 --data-binary '{"query":"query Test($id: Int!){\n  DeezerTrack(id: $id) {\n    track {\n      title\n    }\n    errors {\n      field\n      messages\n    }\n  }\n}\n","variables":{"id":3100001}}'
 --compressed
