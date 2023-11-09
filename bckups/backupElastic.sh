#! /bin/sh 
docker run --rm --volumes-from findfirst-es01-1 -v $(pwd):/backup ubuntu tar cvf backup/esdata01-backup.tar /usr/share/elasticsearch/data
docker run --rm --volumes-from findfirst-es02-1 -v $(pwd):/backup ubuntu tar cvf backup/esdata02-backup.tar /usr/share/elasticsearch/data
