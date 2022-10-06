## How to
- how to use docker images for aws Lambda

### compile project
```
$ mvn compile dependency:copy-dependencies -DincludeScop=runtime -U
```

### Create Docker image
```
docker build -t [image name] -f docker/Dockerfile .
```
