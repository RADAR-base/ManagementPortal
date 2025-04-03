Make sure you run Node <16.*.* 

Tested on Java 17 


# DATABASE

1]  Install and run postgres

2] Create a user "radarbase" with password "radarbase"

3] Create a database called managementportal

4]  Then 

`\c managamentportal`

5] Then assign schema public to radarbase user 

`GRANT USAGE,CREATE on SCHEMA public TO radarbase;`

6] Assign the DB to radarbase user

`GRANT CONNECT ON DATABASE managementportal TO radarbase;
`



# **BACKEND** 

1] Run identity stack in Docker:

`docker-compose -f ./src/main/docker/non_managementportal/docker-compose.yml up -d`

2] Add this in application-prod.yaml under managementportal/identity (for adminEmail choose whatever email you want) :

```

    adminEmail: jindrichgorner@gmail.com
    serverUrl: http://127.0.0.1:4433
    serverAdminUrl: http://127.0.0.1:4434
    loginUrl: http://127.0.0.1:3000
    
```
    
    
Also change baseUrl and managementPortal to:

```
    baseUrl: http://127.0.0.1:8080/managementportal # Modify according to your server's URL
    managementPortalBaseUrl: http://127.0.0.1:8080/managementportal
    privacyPolicyUrl: http://info.thehyve.nl/radar-cns-privacy-policy

```     
    
    

4] Run the backend 

`./gradlew bootRun -Pprod`


# RESET CREDENTIALS 


1] Visit, click Account Recovery, put the email from above there 

`http://localhost:3000/welcome`

2] Open (this will open email catcher and you should be ablet to see the email to reset the passowrd there) 

`http://localhost:4436/#`

3] Reset the password and setup 2fa by clicking the recovery url in the email body 

4] Go to the managementportal database -> open table radar_user and change "login" to the email you provided above.  

5] Now you should be able to log in at: 

`http://localhost:8080/managementportal/#/managementportal/`