@echo off
start "Client"   java -jar customerclient/target/customerclient-1.0.jar
start "FrontEnd" java -jar frontend/target/frontend-1.0.jar
start "SEQQC"    java -jar sequencer/target/sequencer-1.0.jar QC
start "SEQBC"    java -jar sequencer/target/sequencer-1.0.jar BC
start "SEQMB"    java -jar sequencer/target/sequencer-1.0.jar MB
start "SEQNB"    java -jar sequencer/target/sequencer-1.0.jar NB
start "RMQC1"    java -jar replicamanager/target/replicamanager-1.0.jar SY QC 1
start "RMQC2"    java -jar replicamanager/target/replicamanager-1.0.jar SY QC 2
start "RMQC3"    java -jar replicamanager/target/replicamanager-1.0.jar SY QC 3
start "RMBC1"    java -jar replicamanager/target/replicamanager-1.0.jar SY BC 1
start "RMBC2"    java -jar replicamanager/target/replicamanager-1.0.jar SY BC 2
start "RMBC3"    java -jar replicamanager/target/replicamanager-1.0.jar SY BC 3
start "RMNB1"    java -jar replicamanager/target/replicamanager-1.0.jar SY NB 1
start "RMNB2"    java -jar replicamanager/target/replicamanager-1.0.jar SY NB 2
start "RMNB3"    java -jar replicamanager/target/replicamanager-1.0.jar SY NB 3
start "RMMB1"    java -jar replicamanager/target/replicamanager-1.0.jar SY MB 1
start "RMMB2"    java -jar replicamanager/target/replicamanager-1.0.jar SY MB 2
start "RMMB3"    java -jar replicamanager/target/replicamanager-1.0.jar SY MB 3
pause