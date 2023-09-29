#! /bin/sh
docker run --rm --volumes-from es-docker-es01-1 -v $(pwd):/backup ubuntu bash -c "cd  /usr/share/elasticsearch/data && tar xvf /backup/esdata01-backup.tar --strip 1"


docker run --rm --volumes-from es-docker-es02-1 -v $(pwd):/backup ubuntu bash -c "cd  /usr/share/elasticsearch/data && tar xvf /backup/esdata02-backup.tar --strip 1"
