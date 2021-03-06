#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

FROM openjdk:8u181-jdk-stretch

LABEL MAINTAINER=fx19880617@gmail.com

ARG PINOT_BRANCH=develop
ARG PINOT_GIT_URL="https://github.com/apache/incubator-pinot.git"
RUN echo "Trying to build Pinot from [ ${PINOT_GIT_URL} ] on branch [ ${PINOT_BRANCH} ]"
ENV PINOT_HOME=/opt/pinot
ENV PINOT_BUILD_DIR=/opt/pinot-build

# extra dependency for running launcher
RUN apt-get update && apt-get install -y --no-install-recommends \
		vim wget curl git maven\
	&& rm -rf /var/lib/apt/lists/*

RUN git clone ${PINOT_GIT_URL} ${PINOT_BUILD_DIR}  && \
    cd ${PINOT_BUILD_DIR} && \
    git checkout ${PINOT_BRANCH} && \
    mvn install package -DskipTests -Pbin-dist && \
    mkdir -p ${PINOT_HOME}/configs && \
    mkdir -p ${PINOT_HOME}/data && \
    cp -r pinot-distribution/target/apache-pinot-*-bin/apache-pinot-*-bin/* ${PINOT_HOME}/. && \
    chmod +x ${PINOT_HOME}/bin/*.sh && \
    mvn dependency:purge-local-repository -DactTransitively=false -DreResolve=false --fail-at-end && \
    rm -rf ${PINOT_BUILD_DIR}

VOLUME ["${PINOT_HOME}/configs", "${PINOT_HOME}/data"]

EXPOSE 9000 8099 8098 8097 8096 9514

WORKDIR ${PINOT_HOME}

ENTRYPOINT ["./bin/pinot-admin.sh"]

CMD ["run"]
