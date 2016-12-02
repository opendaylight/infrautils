How to try out the OpenDaylight infrautils caches example:

cd ../../infrautils/common/karaf
mvn -Pq clean package
target/assembly/bin/karaf

cd infrautils/caches
mvn -Pq clean install -DaddInstallRepositoryPath=../../../common/karaf/target/assembly/system/

bundle:watch *
stack-traces-print
feature:install odl-infrautils-caches-sample
cache:sample
