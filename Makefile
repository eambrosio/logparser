start:
	docker-compose up --build > log &
	sleep 10
	sbt run

clean:
	docker-compose stop; docker-compose down; docker-compose rm;