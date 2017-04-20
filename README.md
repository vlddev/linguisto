Installation
------------

Database

1. Create database linguistodb in MySql
   run "mysql -u <username> -p<password>"
   run install.sql: mysql> source install.sql

Build linguisto.war

1. Run 'mvn clean install' in /linguisto , linguisto.war will be generated in /linguisto/target/

Configure Wildfly (JBoss).

This application was tested with Wildfly 8.2.0

1. Add mysql driver to Wildfly

2. Add datasource to wildfly-8.2.0/standalone/configuration/standalone.xml

                <datasource jndi-name="java:jboss/datasources/MySQLDS" pool-name="MySQLDS" enabled="true" use-java-context="true" spy="true" use-ccm="true">
                    <connection-url>jdbc:mysql://localhost/linguistodb?autoCommit=false&amp;useUnicode=true&amp;characterEncoding=UTF-8</connection-url>
                    <connection-property name="autoCommit">false</connection-property>
                    <driver>mysql</driver>
                    <pool>
                        <min-pool-size>1</min-pool-size>
                        <max-pool-size>20</max-pool-size>
                    </pool>
                    <security>
                        <user-name>appuser</user-name>
                        <password>appuser</password>
                    </security>
                    <validation>
                        <check-valid-connection-sql>SELECT 1</check-valid-connection-sql>
                        <background-validation>true</background-validation>
                        <background-validation-millis>60000</background-validation-millis>
                    </validation>
                </datasource>

3. Deploy linguisto.war to wildfly-8.2.0/standalone/deployments/

4. Start Wildfly by running wildfly-8.2.0/bin/standalone.sh

5. Visit linguisto web-app in your browser under http://localhost:8080/
