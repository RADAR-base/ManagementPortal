# SourceResourceApi

All URIs are relative to *https://localhost:8080/*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createSourceUsingPOST**](SourceResourceApi.md#createSourceUsingPOST) | **POST** /api/sources | createSource
[**deleteSourceUsingDELETE**](SourceResourceApi.md#deleteSourceUsingDELETE) | **DELETE** /api/sources/{sourceName} | deleteSource
[**getAllSourcesUsingGET**](SourceResourceApi.md#getAllSourcesUsingGET) | **GET** /api/sources | getAllSources
[**getSourceUsingGET**](SourceResourceApi.md#getSourceUsingGET) | **GET** /api/sources/{sourceName} | getSource
[**updateSourceUsingPUT**](SourceResourceApi.md#updateSourceUsingPUT) | **PUT** /api/sources | updateSource


<a name="createSourceUsingPOST"></a>
# **createSourceUsingPOST**
> SourceDTO createSourceUsingPOST(sourceDTO)

createSource

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SourceResourceApi;


SourceResourceApi apiInstance = new SourceResourceApi();
SourceDTO sourceDTO = new SourceDTO(); // SourceDTO | sourceDTO
try {
    SourceDTO result = apiInstance.createSourceUsingPOST(sourceDTO);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SourceResourceApi#createSourceUsingPOST");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **sourceDTO** | [**SourceDTO**](SourceDTO.md)| sourceDTO |

### Return type

[**SourceDTO**](SourceDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="deleteSourceUsingDELETE"></a>
# **deleteSourceUsingDELETE**
> deleteSourceUsingDELETE(sourceName)

deleteSource

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SourceResourceApi;


SourceResourceApi apiInstance = new SourceResourceApi();
String sourceName = "sourceName_example"; // String | sourceName
try {
    apiInstance.deleteSourceUsingDELETE(sourceName);
} catch (ApiException e) {
    System.err.println("Exception when calling SourceResourceApi#deleteSourceUsingDELETE");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **sourceName** | **String**| sourceName |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getAllSourcesUsingGET"></a>
# **getAllSourcesUsingGET**
> List&lt;SourceDTO&gt; getAllSourcesUsingGET()

getAllSources

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SourceResourceApi;


SourceResourceApi apiInstance = new SourceResourceApi();
try {
    List<SourceDTO> result = apiInstance.getAllSourcesUsingGET();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SourceResourceApi#getAllSourcesUsingGET");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**List&lt;SourceDTO&gt;**](SourceDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getSourceUsingGET"></a>
# **getSourceUsingGET**
> SourceDTO getSourceUsingGET(sourceName)

getSource

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SourceResourceApi;


SourceResourceApi apiInstance = new SourceResourceApi();
String sourceName = "sourceName_example"; // String | sourceName
try {
    SourceDTO result = apiInstance.getSourceUsingGET(sourceName);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SourceResourceApi#getSourceUsingGET");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **sourceName** | **String**| sourceName |

### Return type

[**SourceDTO**](SourceDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="updateSourceUsingPUT"></a>
# **updateSourceUsingPUT**
> SourceDTO updateSourceUsingPUT(sourceDTO)

updateSource

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SourceResourceApi;


SourceResourceApi apiInstance = new SourceResourceApi();
SourceDTO sourceDTO = new SourceDTO(); // SourceDTO | sourceDTO
try {
    SourceDTO result = apiInstance.updateSourceUsingPUT(sourceDTO);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SourceResourceApi#updateSourceUsingPUT");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **sourceDTO** | [**SourceDTO**](SourceDTO.md)| sourceDTO |

### Return type

[**SourceDTO**](SourceDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

