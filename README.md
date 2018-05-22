Purpose
-
This project aims to implement a simple distributed bank system that can either be fault tolerant or highly available.
Written in fall 2016 for SOEN423 as a final project.

Refer to the [design document](distributed-bank-design.pdf) for more details about the design and implementation of this project.


Building The Project
-
### Maven
1) Run 'mvn clean package'

Running Built Binaries
-

### Run ORB Daemon
1) Open a console window
2) Run the command
    - **Unix**: orbd -ORBInitialPort 1050 -ORBInitialHost 127.0.0.1
    - **Windows**: start orbd -ORBInitialPort 1050 -ORBInitialHost 127.0.0.1


### Start FrontEnd
3) From server/
4) Run 'java -jar target/frontend-1.0.jar


### Start Sequencer
5) From sequencer/
6) Run 'java -jar target/sequencer-1.0.jar <branch>'
    - **branch**: QC, MB, NB, BC


### Start ReplicaManager
7) From replicamanager/
8) Run 'java -jar target/replicamanager-1.0.jar \<implementation> \<branch> \<id>'
    - **implementation**: RA (Radu), SY (Sylvain), MA (Mathieu)
    - **branch**: QC, MB, NB, BC
    - **id**: 1, 2, 3 (do not start duplicates)


### Start Client(s)
#### CustomerClient
9) From customerclient/
10) Run 'java -jar target/customerclient-1.0.jar'

#### ManagerClient
11) From managerclient/
12) Run 'java -jar target/managerclient-1.0.jar'


Running Entire System
-
### Full Start
A full start requires:

- 12 ReplicaManagers (3/branch, one each of SY, RA, MA)
- 4 Sequencers (1/branch)
- 1 FrontEnd
- n Clients (manager or customer)

To start all the required binaries at once on windows, run **startall.bat**

### Triggering Errors
#### Byzantine
Deposit $423 into an account 3 times. Sylvain IMPL will byzantine.

#### Crash
Deposit $42 into an account. Sylvain IMPL will crash.
