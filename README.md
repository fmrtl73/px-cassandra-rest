### Build
```console
user@host:~/px-cassandra-rest$ ./mvnw clean package
user@host:~/px-cassandra-rest$ ./mvnw install dockerfile:build
user@host:~/px-cassandra-rest$ docker push fmrtl73/px-cassandra-rest
```

### Test locally with with curl

```console
user@host:~/px-cassandra-rest$ docker run --name cassandra1 -m 2g -d cassandra:3.11.2
user@host:~/px-cassandra-rest$ CASSANDRA_IP=`docker inspect --format='{{ .NetworkSettings.IPAddress }}' cassandra1`
```

Start the client container and create the people database
```console
user@host:~/px-cassandra-rest$ docker run -it --link cassandra1 --rm cassandra:3.11.2 sh -c 'exec cqlsh $CASSANDRA_IP'
CREATE KEYSPACE people WITH REPLICATION = {'class':'SimpleStrategy','replication_factor':3};
use people;
CREATE TYPE people.address (
  line1 text,
  line2 text,
  city text,
  state text,
  zipcode int,
);
CREATE TABLE people (id varchar PRIMARY KEY, firstName varchar, lastName varchar, address frozen <address>);
INSERT INTO people.people (id, firstName, lastName, address) VALUES (1, 'Francois', 'Martel', { line1: '191 Rue St. Charles', line2: 'apt-3245', city: 'Austin', state: 'TX', zip_code: 75015});
exit
```

Run the rest api and create a record

```console
user@host:~/px-cassandra-rest$ java -jar target/px-cassandra-rest-0.1.0.jar --spring.profiles.active=dev
```

```console
user@host:~/px-cassandra-rest$ curl -i -X POST -H "Content-Type:application/json" -d "{\"firstName\": \"Francois\",\"lastName\": \"Martel\",\"address\": {\"line1\": \"465 Washington\",\"line2\": \"apt-3425\",\"city\": \"Kansas\",\"state\": \"Texas\",\"zipcode\": \"03452\"}}" http://localhost:8080/people
user@host:~/px-cassandra-rest$ curl http://localhost:8080/people
```

### Test with Kubernetes

Create a portworx storage class with repl3 and use helm to deploy Postgres and pass in the storage class name

```console
user@host:~/px-cassandra-rest$ kubectl create -f k8s-yaml/px-repl3-sc.yaml
user@host:~/px-cassandra-rest$ helm install --name px-psql --set postgresUser=postgres,postgresPassword=password,persistence.storageClass=px-repl3-sc stable/postgresql
```

Create the people database
```console
user@host:~/px-cassandra-rest$ PGPASSWORD=$(kubectl get secret --namespace default px-psql2-postgresql -o jsonpath="{.data.postgres-password}" | base64 --decode; echo)
user@host:~/px-cassandra-rest$ kubectl run --namespace default px-psql2-postgresql-client --restart=Never --rm --tty -i --image postgres --env "PGPASSWORD=$PGPASSWORD" --command -- psql -U postgres -h px-psql2-postgresql postgres
create database people;
\q
```
Deploy rest api

```console
user@host:~/px-cassandra-rest$ kubectl create -f k8s-yaml/jpa-deploy.yaml
```

Get the svc IP and curl some data

```console
user@host:~/px-cassandra-rest$ PSQL_SERVICE_IP=`kubectl get svc | grep jpa-rest-api | awk '{print $3}'`
user@host:~/px-cassandra-rest$ curl -i -X POST -H "Content-Type:application/json" -d "{\"firstName\": \"Francois\",\"lastName\": \"Martel\",\"address\": {\"line1\": \"465 Washington\",\"line2\": \"apt-3425\",\"city\": \"Kansas\",\"state\": \"Texas\",\"zipcode\": \"03452\"}}" http://$PSQL_SERVICE_IP:8080/people
user@host:~/px-cassandra-rest$ curl http://$PSQL_SERVICE_IP:8080/people
```
