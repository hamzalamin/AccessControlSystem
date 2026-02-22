.PHONY: help build run stop restart logs clean test verify

APP_NAME=docaccess
COMPOSE=docker compose

help:
	@echo ""
	@echo "  Available commands:"
	@echo ""
	@echo "  make build      Build the application image"
	@echo "  make run        Start all services"
	@echo "  make stop       Stop all services"
	@echo "  make restart    Restart all services"
	@echo "  make logs       Follow application logs"
	@echo "  make clean      Stop and remove containers, volumes"
	@echo "  make test       Run unit tests"
	@echo "  make verify     Run E2E tests (requires Docker)"
	@echo ""

build:
	$(COMPOSE) build --no-cache

run:
	$(COMPOSE) up -d

stop:
	$(COMPOSE) down

restart:
	$(COMPOSE) down
	$(COMPOSE) up -d

logs:
	$(COMPOSE) logs -f app

clean:
	$(COMPOSE) down -v --remove-orphans

test:
	./mvnw test

verify:
	./mvnw verify