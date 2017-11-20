# SubjectResourceApi

All URIs are relative to *https://localhost:8080/*

Method | HTTP request | Description
------------- | ------------- | -------------
[**assignSourcesUsingPOST**](SubjectResourceApi.md#assignSourcesUsingPOST) | **POST** /api/subjects/{login}/sources | assignSources
[**createSubjectUsingPOST**](SubjectResourceApi.md#createSubjectUsingPOST) | **POST** /api/subjects | createSubject
[**deleteSubjectUsingDELETE**](SubjectResourceApi.md#deleteSubjectUsingDELETE) | **DELETE** /api/subjects/{login} | deleteSubject
[**discontinueSubjectUsingPUT**](SubjectResourceApi.md#discontinueSubjectUsingPUT) | **PUT** /api/subjects/discontinue | discontinueSubject
[**getAllSubjectsUsingGET1**](SubjectResourceApi.md#getAllSubjectsUsingGET1) | **GET** /api/subjects | getAllSubjects
[**getSubjectSourcesUsingGET**](SubjectResourceApi.md#getSubjectSourcesUsingGET) | **GET** /api/subjects/{login}/sources | getSubjectSources
[**getSubjectUsingGET**](SubjectResourceApi.md#getSubjectUsingGET) | **GET** /api/subjects/{login} | getSubject
[**updateSubjectUsingPUT**](SubjectResourceApi.md#updateSubjectUsingPUT) | **PUT** /api/subjects | updateSubject


<a name="assignSourcesUsingPOST"></a>
# **assignSourcesUsingPOST**
> MinimalSourceDetailsDTO assignSourcesUsingPOST(login, sourceDTO)

assignSources

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SubjectResourceApi;


SubjectResourceApi apiInstance = new SubjectResourceApi();
String login = "login_example"; // String | login
MinimalSourceDetailsDTO sourceDTO = new MinimalSourceDetailsDTO(); // MinimalSourceDetailsDTO | sourceDTO
try {
    MinimalSourceDetailsDTO result = apiInstance.assignSourcesUsingPOST(login, sourceDTO);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SubjectResourceApi#assignSourcesUsingPOST");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **login** | **String**| login |
 **sourceDTO** | [**MinimalSourceDetailsDTO**](MinimalSourceDetailsDTO.md)| sourceDTO |

### Return type

[**MinimalSourceDetailsDTO**](MinimalSourceDetailsDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="createSubjectUsingPOST"></a>
# **createSubjectUsingPOST**
> SubjectDTO createSubjectUsingPOST(subjectDTO)

createSubject

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SubjectResourceApi;


SubjectResourceApi apiInstance = new SubjectResourceApi();
SubjectDTO subjectDTO = new SubjectDTO(); // SubjectDTO | subjectDTO
try {
    SubjectDTO result = apiInstance.createSubjectUsingPOST(subjectDTO);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SubjectResourceApi#createSubjectUsingPOST");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **subjectDTO** | [**SubjectDTO**](SubjectDTO.md)| subjectDTO |

### Return type

[**SubjectDTO**](SubjectDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="deleteSubjectUsingDELETE"></a>
# **deleteSubjectUsingDELETE**
> deleteSubjectUsingDELETE(login)

deleteSubject

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SubjectResourceApi;


SubjectResourceApi apiInstance = new SubjectResourceApi();
String login = "login_example"; // String | login
try {
    apiInstance.deleteSubjectUsingDELETE(login);
} catch (ApiException e) {
    System.err.println("Exception when calling SubjectResourceApi#deleteSubjectUsingDELETE");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **login** | **String**| login |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="discontinueSubjectUsingPUT"></a>
# **discontinueSubjectUsingPUT**
> SubjectDTO discontinueSubjectUsingPUT(subjectDTO)

discontinueSubject

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SubjectResourceApi;


SubjectResourceApi apiInstance = new SubjectResourceApi();
SubjectDTO subjectDTO = new SubjectDTO(); // SubjectDTO | subjectDTO
try {
    SubjectDTO result = apiInstance.discontinueSubjectUsingPUT(subjectDTO);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SubjectResourceApi#discontinueSubjectUsingPUT");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **subjectDTO** | [**SubjectDTO**](SubjectDTO.md)| subjectDTO |

### Return type

[**SubjectDTO**](SubjectDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getAllSubjectsUsingGET1"></a>
# **getAllSubjectsUsingGET1**
> List&lt;SubjectDTO&gt; getAllSubjectsUsingGET1(projectName, externalId)

getAllSubjects

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SubjectResourceApi;


SubjectResourceApi apiInstance = new SubjectResourceApi();
String projectName = "projectName_example"; // String | projectName
String externalId = "externalId_example"; // String | externalId
try {
    List<SubjectDTO> result = apiInstance.getAllSubjectsUsingGET1(projectName, externalId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SubjectResourceApi#getAllSubjectsUsingGET1");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **projectName** | **String**| projectName | [optional]
 **externalId** | **String**| externalId | [optional]

### Return type

[**List&lt;SubjectDTO&gt;**](SubjectDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getSubjectSourcesUsingGET"></a>
# **getSubjectSourcesUsingGET**
> List&lt;MinimalSourceDetailsDTO&gt; getSubjectSourcesUsingGET(login)

getSubjectSources

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SubjectResourceApi;


SubjectResourceApi apiInstance = new SubjectResourceApi();
String login = "login_example"; // String | login
try {
    List<MinimalSourceDetailsDTO> result = apiInstance.getSubjectSourcesUsingGET(login);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SubjectResourceApi#getSubjectSourcesUsingGET");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **login** | **String**| login |

### Return type

[**List&lt;MinimalSourceDetailsDTO&gt;**](MinimalSourceDetailsDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getSubjectUsingGET"></a>
# **getSubjectUsingGET**
> SubjectDTO getSubjectUsingGET(login)

getSubject

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SubjectResourceApi;


SubjectResourceApi apiInstance = new SubjectResourceApi();
String login = "login_example"; // String | login
try {
    SubjectDTO result = apiInstance.getSubjectUsingGET(login);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SubjectResourceApi#getSubjectUsingGET");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **login** | **String**| login |

### Return type

[**SubjectDTO**](SubjectDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="updateSubjectUsingPUT"></a>
# **updateSubjectUsingPUT**
> SubjectDTO updateSubjectUsingPUT(subjectDTO)

updateSubject

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SubjectResourceApi;


SubjectResourceApi apiInstance = new SubjectResourceApi();
SubjectDTO subjectDTO = new SubjectDTO(); // SubjectDTO | subjectDTO
try {
    SubjectDTO result = apiInstance.updateSubjectUsingPUT(subjectDTO);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SubjectResourceApi#updateSubjectUsingPUT");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **subjectDTO** | [**SubjectDTO**](SubjectDTO.md)| subjectDTO |

### Return type

[**SubjectDTO**](SubjectDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

