# managementportal-client

## Requirements

Building the API client library requires [Maven](https://maven.apache.org/) to be installed.

## Installation

To install the API client library to your local Maven repository, simply execute:

```shell
mvn install
```

To deploy it to a remote Maven repository instead, configure the settings of the repository and execute:

```shell
mvn deploy
```

Refer to the [official documentation](https://maven.apache.org/plugins/maven-deploy-plugin/usage.html) for more information.

### Maven users

Add this dependency to your project's POM:

```xml
<dependency>
    <groupId>org.radarcns</groupId>
    <artifactId>managementportal-client</artifactId>
    <version>0.3.2-SNAPSHOT</version>
    <scope>compile</scope>
</dependency>
```

### Gradle users

Add this dependency to your project's build file:

```groovy
compile "org.radarcns:managementportal-client:0.3.2-SNAPSHOT"
```

### Others

At first generate the JAR by executing:

    mvn package

Then manually install the following JARs:

* target/managementportal-client-0.3.2-SNAPSHOT.jar
* target/lib/*.jar

## Getting Started

Please follow the [installation](#installation) instruction and execute the following Java code:

```java

import org.radarcns.management.client.*;
import org.radarcns.management.client.auth.*;
import org.radarcns.management.client.model.*;
import org.radarcns.management.client.api.AccountResourceApi;

import java.io.File;
import java.util.*;

public class AccountResourceApiExample {

    public static void main(String[] args) {
        
        AccountResourceApi apiInstance = new AccountResourceApi();
        String key = "key_example"; // String | key
        try {
            String result = apiInstance.activateAccountUsingGET(key);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AccountResourceApi#activateAccountUsingGET");
            e.printStackTrace();
        }
    }
}

```

## Documentation for API Endpoints

All URIs are relative to *https://localhost:8080/*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*AccountResourceApi* | [**activateAccountUsingGET**](docs/AccountResourceApi.md#activateAccountUsingGET) | **GET** /api/activate | activateAccount
*AccountResourceApi* | [**changePasswordUsingPOST**](docs/AccountResourceApi.md#changePasswordUsingPOST) | **POST** /api/account/change_password | changePassword
*AccountResourceApi* | [**finishPasswordResetUsingPOST**](docs/AccountResourceApi.md#finishPasswordResetUsingPOST) | **POST** /api/account/reset_password/finish | finishPasswordReset
*AccountResourceApi* | [**getAccountUsingGET**](docs/AccountResourceApi.md#getAccountUsingGET) | **GET** /api/account | getAccount
*AccountResourceApi* | [**isAuthenticatedUsingGET**](docs/AccountResourceApi.md#isAuthenticatedUsingGET) | **GET** /api/authenticate | isAuthenticated
*AccountResourceApi* | [**requestPasswordResetUsingPOST**](docs/AccountResourceApi.md#requestPasswordResetUsingPOST) | **POST** /api/account/reset_password/init | requestPasswordReset
*AccountResourceApi* | [**saveAccountUsingPOST**](docs/AccountResourceApi.md#saveAccountUsingPOST) | **POST** /api/account | saveAccount
*AuthorityResourceApi* | [**getAllAuthoritiesUsingGET**](docs/AuthorityResourceApi.md#getAllAuthoritiesUsingGET) | **GET** /api/authorities | getAllAuthorities
*OAuthClientsResourceApi* | [**createOAuthClientUsingPOST**](docs/OAuthClientsResourceApi.md#createOAuthClientUsingPOST) | **POST** /api/oauth-clients | createOAuthClient
*OAuthClientsResourceApi* | [**deleteOAuthClientUsingDELETE**](docs/OAuthClientsResourceApi.md#deleteOAuthClientUsingDELETE) | **DELETE** /api/oauth-clients/{id} | deleteOAuthClient
*OAuthClientsResourceApi* | [**getOAuthClientByIdUsingGET**](docs/OAuthClientsResourceApi.md#getOAuthClientByIdUsingGET) | **GET** /api/oauth-clients/{id} | getOAuthClientById
*OAuthClientsResourceApi* | [**getOAuthClientsUsingGET**](docs/OAuthClientsResourceApi.md#getOAuthClientsUsingGET) | **GET** /api/oauth-clients | getOAuthClients
*OAuthClientsResourceApi* | [**getRefreshTokenUsingGET**](docs/OAuthClientsResourceApi.md#getRefreshTokenUsingGET) | **GET** /api/oauth-clients/pair | getRefreshToken
*OAuthClientsResourceApi* | [**updateOAuthClientUsingPUT**](docs/OAuthClientsResourceApi.md#updateOAuthClientUsingPUT) | **PUT** /api/oauth-clients | updateOAuthClient
*ProfileInfoResourceApi* | [**getActiveProfilesUsingGET**](docs/ProfileInfoResourceApi.md#getActiveProfilesUsingGET) | **GET** /api/profile-info | getActiveProfiles
*ProjectResourceApi* | [**createProjectUsingPOST**](docs/ProjectResourceApi.md#createProjectUsingPOST) | **POST** /api/projects | createProject
*ProjectResourceApi* | [**deleteProjectUsingDELETE**](docs/ProjectResourceApi.md#deleteProjectUsingDELETE) | **DELETE** /api/projects/{projectName} | deleteProject
*ProjectResourceApi* | [**getAllProjectsUsingGET**](docs/ProjectResourceApi.md#getAllProjectsUsingGET) | **GET** /api/projects | getAllProjects
*ProjectResourceApi* | [**getAllSourcesForProjectUsingGET**](docs/ProjectResourceApi.md#getAllSourcesForProjectUsingGET) | **GET** /api/projects/{projectName}/sources | getAllSourcesForProject
*ProjectResourceApi* | [**getAllSubjectsUsingGET**](docs/ProjectResourceApi.md#getAllSubjectsUsingGET) | **GET** /api/projects/{projectName}/subjects | getAllSubjects
*ProjectResourceApi* | [**getProjectUsingGET**](docs/ProjectResourceApi.md#getProjectUsingGET) | **GET** /api/projects/{projectName} | getProject
*ProjectResourceApi* | [**getRolesByProjectUsingGET**](docs/ProjectResourceApi.md#getRolesByProjectUsingGET) | **GET** /api/projects/{projectName}/roles | getRolesByProject
*ProjectResourceApi* | [**getSourceTypesOfProjectUsingGET**](docs/ProjectResourceApi.md#getSourceTypesOfProjectUsingGET) | **GET** /api/projects/{projectName}/source-types | getSourceTypesOfProject
*ProjectResourceApi* | [**updateProjectUsingPUT**](docs/ProjectResourceApi.md#updateProjectUsingPUT) | **PUT** /api/projects | updateProject
*RoleResourceApi* | [**createRoleUsingPOST**](docs/RoleResourceApi.md#createRoleUsingPOST) | **POST** /api/roles | createRole
*RoleResourceApi* | [**getAllAdminRolesUsingGET**](docs/RoleResourceApi.md#getAllAdminRolesUsingGET) | **GET** /api/roles/admin | getAllAdminRoles
*RoleResourceApi* | [**getAllRolesUsingGET**](docs/RoleResourceApi.md#getAllRolesUsingGET) | **GET** /api/roles | getAllRoles
*RoleResourceApi* | [**getRoleUsingGET**](docs/RoleResourceApi.md#getRoleUsingGET) | **GET** /api/roles/{projectName}/{authorityName} | getRole
*RoleResourceApi* | [**updateRoleUsingPUT**](docs/RoleResourceApi.md#updateRoleUsingPUT) | **PUT** /api/roles | updateRole
*SourceDataResourceApi* | [**createSourceDataUsingPOST**](docs/SourceDataResourceApi.md#createSourceDataUsingPOST) | **POST** /api/source-data | createSourceData
*SourceDataResourceApi* | [**deleteSourceDataUsingDELETE**](docs/SourceDataResourceApi.md#deleteSourceDataUsingDELETE) | **DELETE** /api/source-data/{sourceDataName} | deleteSourceData
*SourceDataResourceApi* | [**getAllSourceDataUsingGET**](docs/SourceDataResourceApi.md#getAllSourceDataUsingGET) | **GET** /api/source-data | getAllSourceData
*SourceDataResourceApi* | [**getSourceDataUsingGET**](docs/SourceDataResourceApi.md#getSourceDataUsingGET) | **GET** /api/source-data/{sourceDataName} | getSourceData
*SourceDataResourceApi* | [**updateSourceDataUsingPUT**](docs/SourceDataResourceApi.md#updateSourceDataUsingPUT) | **PUT** /api/source-data | updateSourceData
*SourceResourceApi* | [**createSourceUsingPOST**](docs/SourceResourceApi.md#createSourceUsingPOST) | **POST** /api/sources | createSource
*SourceResourceApi* | [**deleteSourceUsingDELETE**](docs/SourceResourceApi.md#deleteSourceUsingDELETE) | **DELETE** /api/sources/{sourceName} | deleteSource
*SourceResourceApi* | [**getAllSourcesUsingGET**](docs/SourceResourceApi.md#getAllSourcesUsingGET) | **GET** /api/sources | getAllSources
*SourceResourceApi* | [**getSourceUsingGET**](docs/SourceResourceApi.md#getSourceUsingGET) | **GET** /api/sources/{sourceName} | getSource
*SourceResourceApi* | [**updateSourceUsingPUT**](docs/SourceResourceApi.md#updateSourceUsingPUT) | **PUT** /api/sources | updateSource
*SourceTypeResourceApi* | [**createSourceTypeUsingPOST**](docs/SourceTypeResourceApi.md#createSourceTypeUsingPOST) | **POST** /api/source-types | createSourceType
*SourceTypeResourceApi* | [**deleteSourceTypeUsingDELETE**](docs/SourceTypeResourceApi.md#deleteSourceTypeUsingDELETE) | **DELETE** /api/source-types/{producer}/{model}/{version} | deleteSourceType
*SourceTypeResourceApi* | [**getAllSourceTypesUsingGET**](docs/SourceTypeResourceApi.md#getAllSourceTypesUsingGET) | **GET** /api/source-types | getAllSourceTypes
*SourceTypeResourceApi* | [**getSourceTypesUsingGET**](docs/SourceTypeResourceApi.md#getSourceTypesUsingGET) | **GET** /api/source-types/{producer}/{model}/{version} | getSourceTypes
*SourceTypeResourceApi* | [**getSourceTypesUsingGET1**](docs/SourceTypeResourceApi.md#getSourceTypesUsingGET1) | **GET** /api/source-types/{producer}/{model} | getSourceTypes
*SourceTypeResourceApi* | [**getSourceTypesUsingGET2**](docs/SourceTypeResourceApi.md#getSourceTypesUsingGET2) | **GET** /api/source-types/{producer} | getSourceTypes
*SourceTypeResourceApi* | [**updateSourceTypeUsingPUT**](docs/SourceTypeResourceApi.md#updateSourceTypeUsingPUT) | **PUT** /api/source-types | updateSourceType
*SubjectResourceApi* | [**assignSourcesUsingPOST**](docs/SubjectResourceApi.md#assignSourcesUsingPOST) | **POST** /api/subjects/{login}/sources | assignSources
*SubjectResourceApi* | [**createSubjectUsingPOST**](docs/SubjectResourceApi.md#createSubjectUsingPOST) | **POST** /api/subjects | createSubject
*SubjectResourceApi* | [**deleteSubjectUsingDELETE**](docs/SubjectResourceApi.md#deleteSubjectUsingDELETE) | **DELETE** /api/subjects/{login} | deleteSubject
*SubjectResourceApi* | [**discontinueSubjectUsingPUT**](docs/SubjectResourceApi.md#discontinueSubjectUsingPUT) | **PUT** /api/subjects/discontinue | discontinueSubject
*SubjectResourceApi* | [**getAllSubjectsUsingGET1**](docs/SubjectResourceApi.md#getAllSubjectsUsingGET1) | **GET** /api/subjects | getAllSubjects
*SubjectResourceApi* | [**getSubjectSourcesUsingGET**](docs/SubjectResourceApi.md#getSubjectSourcesUsingGET) | **GET** /api/subjects/{login}/sources | getSubjectSources
*SubjectResourceApi* | [**getSubjectUsingGET**](docs/SubjectResourceApi.md#getSubjectUsingGET) | **GET** /api/subjects/{login} | getSubject
*SubjectResourceApi* | [**updateSubjectUsingPUT**](docs/SubjectResourceApi.md#updateSubjectUsingPUT) | **PUT** /api/subjects | updateSubject
*UserResourceApi* | [**createUserUsingPOST**](docs/UserResourceApi.md#createUserUsingPOST) | **POST** /api/users | createUser
*UserResourceApi* | [**deleteUserUsingDELETE**](docs/UserResourceApi.md#deleteUserUsingDELETE) | **DELETE** /api/users/{login} | deleteUser
*UserResourceApi* | [**getAllUsersUsingGET**](docs/UserResourceApi.md#getAllUsersUsingGET) | **GET** /api/users | getAllUsers
*UserResourceApi* | [**getUserProjectsUsingGET**](docs/UserResourceApi.md#getUserProjectsUsingGET) | **GET** /api/users/{login}/projects | getUserProjects
*UserResourceApi* | [**getUserUsingGET**](docs/UserResourceApi.md#getUserUsingGET) | **GET** /api/users/{login} | getUser
*UserResourceApi* | [**updateUserUsingPUT**](docs/UserResourceApi.md#updateUserUsingPUT) | **PUT** /api/users | updateUser


## Documentation for Models

 - [AttributeMapDTO](docs/AttributeMapDTO.md)
 - [ClientDetailsDTO](docs/ClientDetailsDTO.md)
 - [ClientPairInfoDTO](docs/ClientPairInfoDTO.md)
 - [KeyAndPasswordVM](docs/KeyAndPasswordVM.md)
 - [ManagedUserVM](docs/ManagedUserVM.md)
 - [MinimalProjectDetailsDTO](docs/MinimalProjectDetailsDTO.md)
 - [MinimalSourceDetailsDTO](docs/MinimalSourceDetailsDTO.md)
 - [MinimalSourceTypeDTO](docs/MinimalSourceTypeDTO.md)
 - [ProfileInfoVM](docs/ProfileInfoVM.md)
 - [ProjectDTO](docs/ProjectDTO.md)
 - [ResponseEntity](docs/ResponseEntity.md)
 - [RoleDTO](docs/RoleDTO.md)
 - [SourceDTO](docs/SourceDTO.md)
 - [SourceDataDTO](docs/SourceDataDTO.md)
 - [SourceTypeDTO](docs/SourceTypeDTO.md)
 - [SourceTypeId](docs/SourceTypeId.md)
 - [SubjectDTO](docs/SubjectDTO.md)
 - [UserDTO](docs/UserDTO.md)


## Documentation for Authorization

All endpoints do not require authorization.
Authentication schemes defined for the API:

## Recommendation

It's recommended to create an instance of `ApiClient` per thread in a multithreaded environment to avoid any potential issues.

## Author



