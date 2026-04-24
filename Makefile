.PHONY: build build-latest up down help

# Configuration
IMAGE_NAME 	:= jersey-app-backend
GIT_HASH 	:= $(shell git rev-parse --short HEAD)
FULL_IMAGE 	:= $(IMAGE_NAME):$(GIT_HASH)

## make         Default target
.DEFAULT_GOAL := help

## make test	tests the application
test:
	mvn test -f pom.xml

## make build 	build the docker image from the Dockerfile
build: test
	@echo "Building $(FULL_IMAGE) ..."
	docker build \
		--build-arg BUILD_DATE=$(shell date -u +"%Y-%m-%dT%H:%M:%SZ") \
		-t $(FULL_IMAGE) \
		.

build-latest: build
	docker tag $(FULL_IMAGE) $(IMAGE_NAME):latest
	@echo "Tagged $(FULL_IMAGE) as $(IMAGE_NAME):latest"

## make up      Start all services in detached mode
up: build-latest
	docker compose up -d

## make down    Stop and remove containers, networks, and volumes
down:
	docker compose down -v

## make help    Show available commands
help:
	@echo ""
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@grep -E '^##' $(MAKEFILE_LIST) | sed 's/## /  /'
	@echo ""