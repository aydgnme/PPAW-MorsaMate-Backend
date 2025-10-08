.PHONY: help build up down restart logs clean test db-shell backend-shell

# Default target
help:
	@echo "🎵 Morse Code Platform - Docker Commands"
	@echo ""
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@echo "  build         - Build all Docker images"
	@echo "  up            - Start all services"
	@echo "  down          - Stop all services"
	@echo "  restart       - Restart all services"
	@echo "  logs          - Show logs (all services)"
	@echo "  logs-backend  - Show backend logs"
	@echo "  logs-db       - Show database logs"
	@echo "  clean         - Remove all containers and volumes"
	@echo "  test          - Run tests"
	@echo "  db-shell      - Open PostgreSQL shell"
	@echo "  backend-shell - Open backend container shell"
	@echo "  db-backup     - Backup database"
	@echo "  db-restore    - Restore database from backup.sql"

# Build all images
build:
	docker-compose build

# Start all services
up:
	docker-compose up -d
	@echo "✅ Services started!"
	@echo "🔗 Backend API: http://localhost:8080/api"
	@echo "🗄️  PostgreSQL: localhost:5432"

# Stop all services
down:
	docker-compose down

# Restart all services
restart:
	docker-compose restart

# Show logs
logs:
	docker-compose logs -f

# Backend logs only
logs-backend:
	docker-compose logs -f backend

# Database logs only
logs-db:
	docker-compose logs -f postgres

# Clean everything (including volumes!)
clean:
	@echo "⚠️  This will remove all containers, volumes, and data!"
	@read -p "Are you sure? [y/N] " ans && [ $${ans:-N} = y ]
	docker-compose down -v
	docker system prune -f

# Run tests
test:
	./gradlew test

# Open PostgreSQL shell
db-shell:
	docker-compose exec postgres psql -U postgres -d morse_code_db

# Open backend container shell
backend-shell:
	docker-compose exec backend sh

# Backup database
db-backup:
	@echo "📦 Creating database backup..."
	docker-compose exec postgres pg_dump -U postgres morse_code_db > backup_$(shell date +%Y%m%d_%H%M%S).sql
	@echo "✅ Backup created!"

# Restore database
db-restore:
	@echo "⚠️  This will overwrite current database!"
	@read -p "Continue? [y/N] " ans && [ $${ans:-N} = y ]
	docker-compose exec -T postgres psql -U postgres -d morse_code_db < backup.sql
	@echo "✅ Database restored!"

# Check service status
status:
	docker-compose ps

# Rebuild and restart backend only
rebuild-backend:
	docker-compose build backend
	docker-compose up -d backend
	@echo "✅ Backend rebuilt and restarted!"