# SourceTypeResourceApi

All URIs are relative to *https://localhost:8080/*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createSourceTypeUsingPOST**](SourceTypeResourceApi.md#createSourceTypeUsingPOST) | **POST** /api/source-types | createSourceType
[**deleteSourceTypeUsingDELETE**](SourceTypeResourceApi.md#deleteSourceTypeUsingDELETE) | **DELETE** /api/source-types/{producer}/{model}/{version} | deleteSourceType
[**getAllSourceTypesUsingGET**](SourceTypeResourceApi.md#getAllSourceTypesUsingGET) | **GET** /api/source-types | getAllSourceTypes
[**getSourceTypesUsingGET**](SourceTypeResourceApi.md#getSourceTypesUsingGET) | **GET** /api/source-types/{producer}/{model}/{version} | getSourceTypes
[**getSourceTypesUsingGET1**](SourceTypeResourceApi.md#getSourceTypesUsingGET1) | **GET** /api/source-types/{producer}/{model} | getSourceTypes
[**getSourceTypesUsingGET2**](SourceTypeResourceApi.md#getSourceTypesUsingGET2) | **GET** /api/source-types/{producer} | getSourceTypes
[**updateSourceTypeUsingPUT**](SourceTypeResourceApi.md#updateSourceTypeUsingPUT) | **PUT** /api/source-types | updateSourceType


<a name="createSourceTypeUsingPOST"></a>
# **createSourceTypeUsingPOST**
> SourceTypeDTO createSourceTypeUsingPOST(sourceTypeDTO)

createSourceType

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SourceTypeResourceApi;


SourceTypeResourceApi apiInstance = new SourceTypeResourceApi();
SourceTypeDTO sourceTypeDTO = new SourceTypeDTO(); // SourceTypeDTO | sourceTypeDTO
try {
    SourceTypeDTO result = apiInstance.createSourceTypeUsingPOST(sourceTypeDTO);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SourceTypeResourceApi#createSourceTypeUsingPOST");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **sourceTypeDTO** | [**SourceTypeDTO**](SourceTypeDTO.md)| sourceTypeDTO |

### Return type

[**SourceTypeDTO**](SourceTypeDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="deleteSourceTypeUsingDELETE"></a>
# **deleteSourceTypeUsingDELETE**
> deleteSourceTypeUsingDELETE(producer, model, version)

deleteSourceType

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SourceTypeResourceApi;


SourceTypeResourceApi apiInstance = new SourceTypeResourceApi();
String producer = "producer_example"; // String | producer
String model = "model_example"; // String | model
String version = "version_example"; // String | version
try {
    apiInstance.deleteSourceTypeUsingDELETE(producer, model, version);
} catch (ApiException e) {
    System.err.println("Exception when calling SourceTypeResourceApi#deleteSourceTypeUsingDELETE");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **producer** | **String**| producer |
 **model** | **String**| model |
 **version** | **String**| version |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getAllSourceTypesUsingGET"></a>
# **getAllSourceTypesUsingGET**
> List&lt;SourceTypeDTO&gt; getAllSourceTypesUsingGET()

getAllSourceTypes

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SourceTypeResourceApi;


SourceTypeResourceApi apiInstance = new SourceTypeResourceApi();
try {
    List<SourceTypeDTO> result = apiInstance.getAllSourceTypesUsingGET();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SourceTypeResourceApi#getAllSourceTypesUsingGET");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**List&lt;SourceTypeDTO&gt;**](SourceTypeDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getSourceTypesUsingGET"></a>
# **getSourceTypesUsingGET**
> SourceTypeDTO getSourceTypesUsingGET(producer, model, version)

getSourceTypes

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SourceTypeResourceApi;


SourceTypeResourceApi apiInstance = new SourceTypeResourceApi();
String producer = "producer_example"; // String | producer
String model = "model_example"; // String | model
String version = "version_example"; // String | version
try {
    SourceTypeDTO result = apiInstance.getSourceTypesUsingGET(producer, model, version);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SourceTypeResourceApi#getSourceTypesUsingGET");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **producer** | **String**| producer |
 **model** | **String**| model |
 **version** | **String**| version |

### Return type

[**SourceTypeDTO**](SourceTypeDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getSourceTypesUsingGET1"></a>
# **getSourceTypesUsingGET1**
> List&lt;SourceTypeDTO&gt; getSourceTypesUsingGET1(producer, model)

getSourceTypes

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SourceTypeResourceApi;


SourceTypeResourceApi apiInstance = new SourceTypeResourceApi();
String producer = "producer_example"; // String | producer
String model = "model_example"; // String | model
try {
    List<SourceTypeDTO> result = apiInstance.getSourceTypesUsingGET1(producer, model);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SourceTypeResourceApi#getSourceTypesUsingGET1");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **producer** | **String**| producer |
 **model** | **String**| model |

### Return type

[**List&lt;SourceTypeDTO&gt;**](SourceTypeDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getSourceTypesUsingGET2"></a>
# **getSourceTypesUsingGET2**
> List&lt;SourceTypeDTO&gt; getSourceTypesUsingGET2(producer)

getSourceTypes

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SourceTypeResourceApi;


SourceTypeResourceApi apiInstance = new SourceTypeResourceApi();
String producer = "producer_example"; // String | producer
try {
    List<SourceTypeDTO> result = apiInstance.getSourceTypesUsingGET2(producer);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SourceTypeResourceApi#getSourceTypesUsingGET2");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **producer** | **String**| producer |

### Return type

[**List&lt;SourceTypeDTO&gt;**](SourceTypeDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="updateSourceTypeUsingPUT"></a>
# **updateSourceTypeUsingPUT**
> SourceTypeDTO updateSourceTypeUsingPUT(sourceTypeDTO)

updateSourceType

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.SourceTypeResourceApi;


SourceTypeResourceApi apiInstance = new SourceTypeResourceApi();
SourceTypeDTO sourceTypeDTO = new SourceTypeDTO(); // SourceTypeDTO | sourceTypeDTO
try {
    SourceTypeDTO result = apiInstance.updateSourceTypeUsingPUT(sourceTypeDTO);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SourceTypeResourceApi#updateSourceTypeUsingPUT");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **sourceTypeDTO** | [**SourceTypeDTO**](SourceTypeDTO.md)| sourceTypeDTO |

### Return type

[**SourceTypeDTO**](SourceTypeDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

