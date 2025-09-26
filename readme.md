cd /home/user/projects/excel-api
mvn clean package
cp -p ./target/excel-api-0.0.1-SNAPSHOT.jar ./excel-api-dist/
cd excel-api-dist
cf push -f manifest.yml -p excel-api-0.0.1-SNAPSHOT.jar 
