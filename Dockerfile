### how to build in docker
#
# TAG=$(git log --oneline -n 1 | cut -f 1 -d ' ')
# NAME=pploy-$TAG
#
# docker build -t pploy-build .
# mkdir -p $PWD/.ivy2 $PWD/.sbt
# docker rm -f $NAME || true
# docker run --name=$NAME -v $PWD/.ivy2:/root/.ivy2 -v $PWD/.sbt:/root/.sbt \
#   pploy-build \
#   bash -c './activator dist'
#
# docker cp $NAME:/pploy/target/universal/pploy-1.0-SNAPSHOT.zip ./
# docker rm -f $NAME || true

FROM java:8-jdk

COPY . /pploy
WORKDIR /pploy

VOLUME ["/root/.ivy2", "/root/.sbt"]
