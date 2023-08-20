
README
------

This plugin can be used to update the IP Geolocation database provided by Maxmind in dotcms.


## How to update your MaxMind Geo IPdatabase
-------------------------


Purchace and download an updated version of the `GeoLite2-City.mmdb` database from https://www.maxmind.com.  Clone this plugin locally and place the new version of the database into the `src/main/resources` directory and make sure you name it `GeoLite2-City.mmdb`.  Then run  `./gradlew jar` from the plugin root directory.   This will create two jars in the `build/libs` directory, both of which you should upload into your dotCMS.


### Steps to update

```
git clone https://github.com/dotcms-plugins/com.dotcms.update.maxmind.git
cd com.dotcms.update.maxmind
COPY GeoLite2-City.mmdb to src/main/resources
./gradlew jar
```

Then upload into dotCMS.  If you tail the dotCMS logs, you should see a message that indicates the GEO IP database has been updated, something like this:
```
20:26:04.487  INFO  osgi.Activator - Current GeoDB MD5 : 5f3e600a3a8f382128e8eefd8988a18f
20:26:04.487  INFO  osgi.Activator - New GeoDB MD5     : c83586d8575554b1bdc3bdd15f61355d
20:26:04.487  INFO  osgi.Activator - Replacing GEOLOCATION DB
20:26:05.065  INFO  osgi.Activator - Updated GeoDB MD5 : c83586d8575554b1bdc3bdd15f61355d
```
