Make sure you have dumped the database stored in db_dumps folder.
Put the appropriate username and password.
Assumption database lies in the same host (localhost).
To make changes: Modify the package-project.sh accordingly.

Open this project with Intellij IDEA Community. Best way: import pom.xml from the Intellij Open Dialog at startup.

4 sites:
* localhost
* mcare.clinic (dbname: mwellness)
* telemedmobile.com (dbname: telemedmobile)
* smarttelemed.com (dbname: smarttelemed)


- jdbc.properties is updated by "package-project.sh" as described below
- Update ServerSettings.java (ctrl + n  to find class)


- Now To Create a war file. Open the Terminal. `cd` to the project's root folder and you should find a script "package-project.sh". Run it. Make your selection for deployment.
- War file will be created in .target/sleep*portal*.war. Also a copy of the jar catering to the selected deployment server will be created with a suffix (example: -mcare.jar)
- You can then run the upload script in the same folder to upload to particular location

