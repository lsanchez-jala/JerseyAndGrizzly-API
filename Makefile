.PHONY: up down help

## make         Default target
.DEFAULT_GOAL := help

## make up      Start all services in detached mode
up:
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