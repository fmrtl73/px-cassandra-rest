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
INSERT INTO people.people (id, firstName, lastName, address) VALUES ('1', 'Francois', 'Martel', { line1: '191 Rue St. Charles', line2: 'apt-3245', city: 'Austin', state: 'TX', zipcode: 75015});
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

Update the k8s-yaml/values.yaml file if you want larger memory and cpu shares or to change any other settings.

Create a portworx storage class with repl3 and use helm to deploy Cassandra and pass in the storage class name and the values.yaml file.

```console
user@host:~/px-cassandra-rest$ kubectl create -f k8s-yaml/px-repl3-sc.yaml
user@host:~/px-cassandra-rest$ helm install --name px -f k8s-yaml/values.yaml incubator/cassandra
```

Create the people database using cqlsh
```console
user@host:~/px-cassandra-rest$ CASSANDRA_IP=`kubectl get endpoints | grep cassandra | awk '{print substr($2,0,index($2,":")-1)}'`
user@host:~/px-cassandra-rest$ docker run -it cassandra:3.11.2 sh -c 'exec cqlsh $CASSANDRA_IP'
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
INSERT INTO people.people (id, firstName, lastName, address) VALUES ('1', 'Francois', 'Martel', { line1: '191 Rue St. Charles', line2: 'apt-3245', city: 'Austin', state: 'TX', zipcode: 75015});
exit
```
Deploy rest api (you should edit it to make sure the service name matches your cassandra service name)

```console
user@host:~/px-cassandra-rest$ kubectl create -f k8s-yaml/cassandra-deploy.yaml
```

Get the svc IP and curl some data

```console
user@host:~/px-cassandra-rest$ REST_API_IP=`kubectl get svc | grep cassandra-rest-api | awk '{print $3}'`
user@host:~/px-cassandra-rest$ curl -i -X POST -H "Content-Type:application/json" -d "{\"firstName\": \"Francois\",\"lastName\": \"Martel\",\"address\": {\"line1\": \"465 Washington\",\"line2\": \"apt-3425\",\"city\": \"Kansas\",\"state\": \"Texas\",\"zipcode\": \"03452\"}}" http://$REST_API_IP:8080/people
user@host:~/px-cassandra-rest$ curl http://$REST_API_IP:8080/people
```
