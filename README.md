# ph-ee-id-account-validator-impl
account validator implementations for PHEE Id mapper. This repo contains the implemntation for gsma and mojaloop account validation which will be used as a dependency in  Identity Account Mapper.

To upload the jar file for this project to Artifactory, use the artifactoryPublish Gradle task. 
The jar file can be built with either the GSMA or Mojaloop implementation by using the ./gradlew jar -Dimplementation=<implementation> command. The implementation parameter must be set to either gsma or mojaloop.