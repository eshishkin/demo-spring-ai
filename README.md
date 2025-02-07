# demo-spring-ai

## Build

### Build telegram JNI lib

- Go to `tdlib` and build telegram binary through docker - `docker build -f Dockerfile.tdlib -t tdlib .`
- Create temporary container to extract telegram binary - `docker create --name tdlib tdlib`
- Copy generated binary from docker image to the host machine - `docker cp tdlib:/tdlib .`
- Delete temporary container - `docker rm tdlib`