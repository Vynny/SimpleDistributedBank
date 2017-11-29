@echo off
start "Client"   java -jar customerclient/target/customerclient-1.0.jar
start "FrontEnd" java -jar frontend/target/frontend-1.0.jar
start "SEQQC"    java -jar sequencer/target/sequencer-1.0.jar QC
start "RMQC1"    java -jar replicamanager/target/replicamanager-1.0.jar SY QC 1
start "RMQC2"    java -jar replicamanager/target/replicamanager-1.0.jar SY QC 2
start "RMQC3"    java -jar replicamanager/target/replicamanager-1.0.jar SY QC 3
pause