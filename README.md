# DTU-Payment-Service

## Project Structure
The DTU-Payment-Service consists of 7 projects.
<br>
1. The ``end-to-end`` tests.
2. A Token Management microservice in ``token-management-service`` responsible for managing, consuming and issuing tokens.
3. An Account Management microservice in ``account-management-service`` microservice that is responsible for accounts management in DTU Pay.
4. A Payment Management microservice in ``payment-management-service`` that is responsible for the communication with the SOAP-based bank service.
5. A Report Management microservice in ``reporting-service`` that is responsible for reporting services for customers, merchants and managers.
6. A facade microservice in ``dtupay-facade`` that offers an external REST interface. Also, this service communicates through message queues.
7. A message utilities in ``messaging-utilities`` provided by Hubert Baumeister.

## Prerequisites
Before proceeding to the installation of the project, make sure you have the following technologies installed:
1. [Docker](https://www.docker.com/)
2. [Java 23](https://www.oracle.com/java/technologies/downloads/)
3. An IDE, such as [IntelliJ](https://www.jetbrains.com/idea/download/), or [Eclipse](https://www.eclipse.org/downloads/)
## Installation Guide

1. **Clone the repository**:
``git clone https://github.com/Zedrichu/DTU-Payment-Service.git``
2. Navigate to the end-to-end-test folder``cd DTU-Payment-Service/end-to-end-test``
3. To build and install the system locally using docker, execute the following script:``sh ./build_deploy_test.sh``
<br>The script builds and deploys the docker images, run the tests and then stops the images.
<br><br>
In case you want to keep the docker images turned on after running the tests, instead of running the ``./build_deploy_test.sh`` from ``./end-to-end-test`` directory, run the ``./build_deploy.sh`` script.

### Jenkins Access
[Jenkins URL](http://fm-18.compute.dtu.dk:8282/view/DTUPay-Platform/)
<br><br>
Credentials:
``Login: huba |
Password: group18``