#!/bin/bash

echo -e "1) Package for localhost\nSelect the operation: "

read n
case $n in
    1) echo -e "you chose localhost\n\n" && jdbc_url="jdbc:mysql://localhost:3306/mwellness"
       target_server="localhost.war"
        ;;
    *) invalid option;;
esac

echo -e "JDBC URL: $jdbc_url\n\n"

jdbc_properties_file="src/main/resources/jdbc.properties";

## remove line that contains 'jdbc.url'
awk '!/jdbc\.url/' $jdbc_properties_file > temp && mv temp $jdbc_properties_file;


## add jdbc.url property line in jdbc.properties file
jdbc_url_line_str="jdbc.url=$jdbc_url";

echo $jdbc_url_line_str >> $jdbc_properties_file;

echo -e "Packaging for $jdbc_url\n";

echo -e "Removing base sleepportal war file ... \n";
rm -f target/sleepportal-1.0.war;

echo -e "Removing base $target_server war file ... \n"
rm -f war_files/sleepportal-1.0-$target_server;

echo -e "Packaging ... for $target_server";
mvn clean && mvn package && cp target/sleepportal-1.0.war war_files/sleepportal-1.0-$target_server;

echo -e "=================================\n"
echo -e "output of ls war_files:\n"
ls war_files;


## remove line that contains 'jdbc.url'
# awk '!/jdbc\.url/' $jdbc_properties_file > temp && mv temp $jdbc_properties_file;

