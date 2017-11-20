# ProjectResourceApi

All URIs are relative to *https://localhost:8080/*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createProjectUsingPOST**](ProjectResourceApi.md#createProjectUsingPOST) | **POST** /api/projects | createProject
[**deleteProjectUsingDELETE**](ProjectResourceApi.md#deleteProjectUsingDELETE) | **DELETE** /api/projects/{projectName} | deleteProject
[**getAllProjectsUsingGET**](ProjectResourceApi.md#getAllProjectsUsingGET) | **GET** /api/projects | getAllProjects
[**getAllSourcesForProjectUsingGET**](ProjectResourceApi.md#getAllSourcesForProjectUsingGET) | **GET** /api/projects/{projectName}/sources | getAllSourcesForProject
[**getAllSubjectsUsingGET**](ProjectResourceApi.md#getAllSubjectsUsingGET) | **GET** /api/projects/{projectName}/subjects | getAllSubjects
[**getProjectUsingGET**](ProjectResourceApi.md#getProjectUsingGET) | **GET** /api/projects/{projectName} | getProject
[**getRolesByProjectUsingGET**](ProjectResourceApi.md#getRolesByProjectUsingGET) | **GET** /api/projects/{projectName}/roles | getRolesByProject
[**getSourceTypesOfProjectUsingGET**](ProjectResourceApi.md#getSourceTypesOfProjectUsingGET) | **GET** /api/projects/{projectName}/source-types | getSourceTypesOfProject
[**updateProjectUsingPUT**](ProjectResourceApi.md#updateProjectUsingPUT) | **PUT** /api/projects | updateProject


<a name="createProjectUsingPOST"></a>
# **createProjectUsingPOST**
> ProjectDTO createProjectUsingPOST(projectDTO)

createProject

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.ProjectResourceApi;


ProjectResourceApi apiInstance = new ProjectResourceApi();
ProjectDTO projectDTO = new ProjectDTO(); // ProjectDTO | projectDTO
try {
    ProjectDTO result = apiInstance.createProjectUsingPOST(projectDTO);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ProjectResourceApi#createProjectUsingPOST");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **projectDTO** | [**ProjectDTO**](ProjectDTO.md)| projectDTO |

### Return type

[**ProjectDTO**](ProjectDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="deleteProjectUsingDELETE"></a>
# **deleteProjectUsingDELETE**
> deleteProjectUsingDELETE(projectName)

deleteProject

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.ProjectResourceApi;


ProjectResourceApi apiInstance = new ProjectResourceApi();
String projectName = "projectName_example"; // String | projectName
try {
    apiInstance.deleteProjectUsingDELETE(projectName);
} catch (ApiException e) {
    System.err.println("Exception when calling ProjectResourceApi#deleteProjectUsingDELETE");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **projectName** | **String**| projectName |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getAllProjectsUsingGET"></a>
# **getAllProjectsUsingGET**
> List&lt;ProjectDTO&gt; getAllProjectsUsingGET(minimized)

getAllProjects

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.ProjectResourceApi;


ProjectResourceApi apiInstance = new ProjectResourceApi();
Boolean minimized = false; // Boolean | minimized
try {
    List<ProjectDTO> result = apiInstance.getAllProjectsUsingGET(minimized);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ProjectResourceApi#getAllProjectsUsingGET");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **minimized** | **Boolean**| minimized | [optional] [default to false]

### Return type

[**List&lt;ProjectDTO&gt;**](ProjectDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getAllSourcesForProjectUsingGET"></a>
# **getAllSourcesForProjectUsingGET**
> ResponseEntity getAllSourcesForProjectUsingGET(projectName, assigned, minimized)

getAllSourcesForProject

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.ProjectResourceApi;


ProjectResourceApi apiInstance = new ProjectResourceApi();
String projectName = "projectName_example"; // String | projectName
Boolean assigned = true; // Boolean | assigned
Boolean minimized = false; // Boolean | minimized
try {
    ResponseEntity result = apiInstance.getAllSourcesForProjectUsingGET(projectName, assigned, minimized);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ProjectResourceApi#getAllSourcesForProjectUsingGET");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **projectName** | **String**| projectName |
 **assigned** | **Boolean**| assigned | [optional]
 **minimized** | **Boolean**| minimized | [optional] [default to false]

### Return type

[**ResponseEntity**](ResponseEntity.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getAllSubjectsUsingGET"></a>
# **getAllSubjectsUsingGET**
> List&lt;SubjectDTO&gt; getAllSubjectsUsingGET(projectName)

getAllSubjects

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.ProjectResourceApi;


ProjectResourceApi apiInstance = new ProjectResourceApi();
String projectName = "projectName_example"; // String | projectName
try {
    List<SubjectDTO> result = apiInstance.getAllSubjectsUsingGET(projectName);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ProjectResourceApi#getAllSubjectsUsingGET");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **projectName** | **String**| projectName |

### Return type

[**List&lt;SubjectDTO&gt;**](SubjectDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getProjectUsingGET"></a>
# **getProjectUsingGET**
> ProjectDTO getProjectUsingGET(projectName)

getProject

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.ProjectResourceApi;


ProjectResourceApi apiInstance = new ProjectResourceApi();
String projectName = "projectName_example"; // String | projectName
try {
    ProjectDTO result = apiInstance.getProjectUsingGET(projectName);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ProjectResourceApi#getProjectUsingGET");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **projectName** | **String**| projectName |

### Return type

[**ProjectDTO**](ProjectDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getRolesByProjectUsingGET"></a>
# **getRolesByProjectUsingGET**
> List&lt;RoleDTO&gt; getRolesByProjectUsingGET(projectName)

getRolesByProject

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.ProjectResourceApi;


ProjectResourceApi apiInstance = new ProjectResourceApi();
String projectName = "projectName_example"; // String | projectName
try {
    List<RoleDTO> result = apiInstance.getRolesByProjectUsingGET(projectName);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ProjectResourceApi#getRolesByProjectUsingGET");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **projectName** | **String**| projectName |

### Return type

[**List&lt;RoleDTO&gt;**](RoleDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getSourceTypesOfProjectUsingGET"></a>
# **getSourceTypesOfProjectUsingGET**
> List&lt;SourceTypeDTO&gt; getSourceTypesOfProjectUsingGET(projectName)

getSourceTypesOfProject

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.ProjectResourceApi;


ProjectResourceApi apiInstance = new ProjectResourceApi();
String projectName = "projectName_example"; // String | projectName
try {
    List<SourceTypeDTO> result = apiInstance.getSourceTypesOfProjectUsingGET(projectName);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ProjectResourceApi#getSourceTypesOfProjectUsingGET");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **projectName** | **String**| projectName |

### Return type

[**List&lt;SourceTypeDTO&gt;**](SourceTypeDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="updateProjectUsingPUT"></a>
# **updateProjectUsingPUT**
> ProjectDTO updateProjectUsingPUT(projectDTO)

updateProject

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.ProjectResourceApi;


ProjectResourceApi apiInstance = new ProjectResourceApi();
ProjectDTO projectDTO = new ProjectDTO(); // ProjectDTO | projectDTO
try {
    ProjectDTO result = apiInstance.updateProjectUsingPUT(projectDTO);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ProjectResourceApi#updateProjectUsingPUT");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **projectDTO** | [**ProjectDTO**](ProjectDTO.md)| projectDTO |

### Return type

[**ProjectDTO**](ProjectDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

