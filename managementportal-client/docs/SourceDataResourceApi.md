# SourceDataResourceApi

All URIs are relative to *https://localhost:8080/*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createSourceDataUsingPOST**](SourceDataResourceApi.md#createSourceDataUsingPOST) | **POST** /api/source-data | createSourceData
[**deleteSourceDataUsingDELETE**](SourceDataResourceApi.md#deleteSourceDataUsingDELETE) | **DELETE** /api/source-data/{sourceDataName} | deleteSourceData
[**getAllSourceDataUsingGET**](SourceDataResourceApi.md#getAllSourceDataUsingGET) | **GET** /api/source-data | getAllSourceData
[**getSourceDataUsingGET**](SourceDataResourceApi.md#getSourceDataUsingGET) | **GET** /api/source-data/{sourceDataName} | getSourceData
[**updateSourceDataUsingPUT**](SourceDataResourceApi.md#updateSourceDataUsingPUT) | **PUT** /api/source-data | updateSourceData


<a name="createSourceDataUsingPOST"></a>
# **createSourceDataUsingPOST**
> SourceDataDTO createSourceDataUsingPOST(sourceDataDTO)

createSourceData

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SourceDataResourceApi;


SourceDataResourceApi apiInstance = new SourceDataResourceApi();
SourceDataDTO sourceDataDTO = new SourceDataDTO(); // SourceDataDTO | sourceDataDTO
try {
    SourceDataDTO result = apiInstance.createSourceDataUsingPOST(sourceDataDTO);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SourceDataResourceApi#createSourceDataUsingPOST");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **sourceDataDTO** | [**SourceDataDTO**](SourceDataDTO.md)| sourceDataDTO |

### Return type

[**SourceDataDTO**](SourceDataDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="deleteSourceDataUsingDELETE"></a>
# **deleteSourceDataUsingDELETE**
> deleteSourceDataUsingDELETE(sourceDataName)

deleteSourceData

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SourceDataResourceApi;


SourceDataResourceApi apiInstance = new SourceDataResourceApi();
String sourceDataName = "sourceDataName_example"; // String | sourceDataName
try {
    apiInstance.deleteSourceDataUsingDELETE(sourceDataName);
} catch (ApiException e) {
    System.err.println("Exception when calling SourceDataResourceApi#deleteSourceDataUsingDELETE");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **sourceDataName** | **String**| sourceDataName |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getAllSourceDataUsingGET"></a>
# **getAllSourceDataUsingGET**
> List&lt;SourceDataDTO&gt; getAllSourceDataUsingGET()

getAllSourceData

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SourceDataResourceApi;


SourceDataResourceApi apiInstance = new SourceDataResourceApi();
try {
    List<SourceDataDTO> result = apiInstance.getAllSourceDataUsingGET();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SourceDataResourceApi#getAllSourceDataUsingGET");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**List&lt;SourceDataDTO&gt;**](SourceDataDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getSourceDataUsingGET"></a>
# **getSourceDataUsingGET**
> SourceDataDTO getSourceDataUsingGET(sourceDataName)

getSourceData

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SourceDataResourceApi;


SourceDataResourceApi apiInstance = new SourceDataResourceApi();
String sourceDataName = "sourceDataName_example"; // String | sourceDataName
try {
    SourceDataDTO result = apiInstance.getSourceDataUsingGET(sourceDataName);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SourceDataResourceApi#getSourceDataUsingGET");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **sourceDataName** | **String**| sourceDataName |

### Return type

[**SourceDataDTO**](SourceDataDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="updateSourceDataUsingPUT"></a>
# **updateSourceDataUsingPUT**
> SourceDataDTO updateSourceDataUsingPUT(sourceDataDTO)

updateSourceData

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SourceDataResourceApi;


SourceDataResourceApi apiInstance = new SourceDataResourceApi();
SourceDataDTO sourceDataDTO = new SourceDataDTO(); // SourceDataDTO | sourceDataDTO
try {
    SourceDataDTO result = apiInstance.updateSourceDataUsingPUT(sourceDataDTO);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SourceDataResourceApi#updateSourceDataUsingPUT");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **sourceDataDTO** | [**SourceDataDTO**](SourceDataDTO.md)| sourceDataDTO |

### Return type

[**SourceDataDTO**](SourceDataDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

