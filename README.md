# DTU-Payment-Service

## Installation Guide

1. **Clone the repository**:
``git clone https://github.com/Zedrichu/DTU-Payment-Service.git``
2. Navigate to the end-to-end-test folder``cd DTU-Payment-Service/end-to-end-test``
3. To build and install the system locally using docker, execute the following script:``sh ./build_deploy_test.sh``
<br>The script builds and deploys the docker images, run the tests and then stops the images.
<br><br>
In case you want to keep the docker images turned on after running the tests, instead of running the ``./build_deploy_test.sh`` from ``./end-to-end-test`` directory, run the ``./build_deploy.sh`` script.