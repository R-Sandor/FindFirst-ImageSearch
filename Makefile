.PHONY: server frontend
.DEFAULT_GOAL:= default 

default: 
	cd server; ./gradlew clean build
	$(MAKE) build_server
	$(MAKE) build_client

server: 
	cd ./server && ./gradlew build && cd .. && docker build -t findfirst-backend -f ./docker/server/Dockerfile ./server

frontend: 
	docker build -t findfirst-frontend -f ./docker/frontend/Dockerfile ./frontend

clean: 
	cd server; ./gradlew clean; 
	cd client; npm ci
