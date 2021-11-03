# JavaBot2
The second iteration of the general-purpose bot for managing the Java Discord server.

**Currently very-much a work-in-progress. Do not commit to `main` without a pull request!**.

## Running the Bot
The bot requires a few dependencies, such as PostgreSQL and MongoDB. You can either install your own instances of these services, or use the included `docker-compose.yaml` file to boot up all of them in docker containers.

To do this, execute the following command from the terminal:
```bash
docker-compose -p javabot up
```

For your convenience, the docker-compose file also includes admin tools that can be useful for debugging.
- MongoExpress is available at [localhost:5050](http://localhost:5050)
- PgAdmin is available at [locahost:5051](http://localhost:5051)
