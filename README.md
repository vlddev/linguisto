Linguisto Portal
================
This software was used on http://linguisto.eu/ (the same databese is used now on http://linguisto.epizy.com/)

Features:
* Viewing and editing bilingual dictionaries.
* Test your vocabulary size (Ukrainian, English, German).
* Mark known words in portal dictionaries (registered users only).
* Learning texts: Prepare some text by adding notes to words unknown for the user. Prepared text can be viewed in browser or exported to fb2 file.

Портал «Лінгвісто»
==================
Дане програмне забезпечення використовувалося на сайті http://linguisto.eu/. (та сама база даних використовується на http://linguisto.epizy.com/)

Можливості:
* Створення, перегляд та наповнення двомовних онлайн-словників.
* Редагувати дані словників можуть всі зареєстровані користувачі.
* Тестування словникового запасу (українська, англійска, німецька).
* Можливість позначати вже вивчені слова певного словника (тільки для зареєстрованих користувачів)
* Навчальні тексти: можливість підготовити текст з врахуванням вже знайомих користувачу слів певного словника. До всіх незнайомих слів додаються зноски з перекладом, що спрощує читання тексту. Оброблений таким чином текст можна переглядати в браузері або зберегти в форматі fb2 й читати за допомогою програми CoolReader3 на вашому улюбленому пристрої. (тільки для зареєстрованих користувачів)


Installation
============


Installation (on Ubuntu Linux)
------------

### Database

1. Create database linguistodb in MySql

   run in command line in the directory **linguisto/src/main/db$** `mysql -u <username> -p<password>`
   
   run install.sql in mysql: **mysql>** `source install.sql`

More about database structure and scrips you can read here: https://github.com/vlddev/linguisto/tree/master/src/main/db

### Build linguisto.war

1. Run `mvn clean install` in command line in directory /linguisto , linguisto.war will be generated in /linguisto/target/

### Configure Wildfly (JBoss).

This application was tested with Wildfly 8.2.0

1. Add mysql driver to Wildfly

2. Add datasource to wildfly-8.2.0/standalone/configuration/standalone.xml

```xml
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
```

3. Deploy linguisto.war to wildfly-8.2.0/standalone/deployments/

4. Start Wildfly by running wildfly-8.2.0/bin/standalone.sh

5. Visit linguisto web-app in your browser under http://localhost:8080/
