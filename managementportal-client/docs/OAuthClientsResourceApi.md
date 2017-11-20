# OAuthClientsResourceApi

All URIs are relative to *https://localhost:8080/*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createOAuthClientUsingPOST**](OAuthClientsResourceApi.md#createOAuthClientUsingPOST) | **POST** /api/oauth-clients | createOAuthClient
[**deleteOAuthClientUsingDELETE**](OAuthClientsResourceApi.md#deleteOAuthClientUsingDELETE) | **DELETE** /api/oauth-clients/{id} | deleteOAuthClient
[**getOAuthClientByIdUsingGET**](OAuthClientsResourceApi.md#getOAuthClientByIdUsingGET) | **GET** /api/oauth-clients/{id} | getOAuthClientById
[**getOAuthClientsUsingGET**](OAuthClientsResourceApi.md#getOAuthClientsUsingGET) | **GET** /api/oauth-clients | getOAuthClients
[**getRefreshTokenUsingGET**](OAuthClientsResourceApi.md#getRefreshTokenUsingGET) | **GET** /api/oauth-clients/pair | getRefreshToken
[**updateOAuthClientUsingPUT**](OAuthClientsResourceApi.md#updateOAuthClientUsingPUT) | **PUT** /api/oauth-clients | updateOAuthClient


<a name="createOAuthClientUsingPOST"></a>
# **createOAuthClientUsingPOST**
> ClientDetailsDTO createOAuthClientUsingPOST(clientDetailsDTO)

createOAuthClient

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.OAuthClientsResourceApi;


OAuthClientsResourceApi apiInstance = new OAuthClientsResourceApi();
ClientDetailsDTO clientDetailsDTO = new ClientDetailsDTO(); // ClientDetailsDTO | clientDetailsDTO
try {
    ClientDetailsDTO result = apiInstance.createOAuthClientUsingPOST(clientDetailsDTO);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling OAuthClientsResourceApi#createOAuthClientUsingPOST");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **clientDetailsDTO** | [**ClientDetailsDTO**](ClientDetailsDTO.md)| clientDetailsDTO |

### Return type

[**ClientDetailsDTO**](ClientDetailsDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="deleteOAuthClientUsingDELETE"></a>
# **deleteOAuthClientUsingDELETE**
> deleteOAuthClientUsingDELETE(id)

deleteOAuthClient

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.OAuthClientsResourceApi;


OAuthClientsResourceApi apiInstance = new OAuthClientsResourceApi();
String id = "id_example"; // String | id
try {
    apiInstance.deleteOAuthClientUsingDELETE(id);
} catch (ApiException e) {
    System.err.println("Exception when calling OAuthClientsResourceApi#deleteOAuthClientUsingDELETE");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **String**| id |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getOAuthClientByIdUsingGET"></a>
# **getOAuthClientByIdUsingGET**
> ClientDetailsDTO getOAuthClientByIdUsingGET(id)

getOAuthClientById

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.OAuthClientsResourceApi;


OAuthClientsResourceApi apiInstance = new OAuthClientsResourceApi();
String id = "id_example"; // String | id
try {
    ClientDetailsDTO result = apiInstance.getOAuthClientByIdUsingGET(id);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling OAuthClientsResourceApi#getOAuthClientByIdUsingGET");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **String**| id |

### Return type

[**ClientDetailsDTO**](ClientDetailsDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getOAuthClientsUsingGET"></a>
# **getOAuthClientsUsingGET**
> List&lt;ClientDetailsDTO&gt; getOAuthClientsUsingGET()

getOAuthClients

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.OAuthClientsResourceApi;


OAuthClientsResourceApi apiInstance = new OAuthClientsResourceApi();
try {
    List<ClientDetailsDTO> result = apiInstance.getOAuthClientsUsingGET();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling OAuthClientsResourceApi#getOAuthClientsUsingGET");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**List&lt;ClientDetailsDTO&gt;**](ClientDetailsDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getRefreshTokenUsingGET"></a>
# **getRefreshTokenUsingGET**
> ClientPairInfoDTO getRefreshTokenUsingGET(login, clientId)

getRefreshToken

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.OAuthClientsResourceApi;


OAuthClientsResourceApi apiInstance = new OAuthClientsResourceApi();
String login = "login_example"; // String | login
String clientId = "clientId_example"; // String | clientId
try {
    ClientPairInfoDTO result = apiInstance.getRefreshTokenUsingGET(login, clientId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling OAuthClientsResourceApi#getRefreshTokenUsingGET");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **login** | **String**| login |
 **clientId** | **String**| clientId |

### Return type

[**ClientPairInfoDTO**](ClientPairInfoDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="updateOAuthClientUsingPUT"></a>
# **updateOAuthClientUsingPUT**
> ClientDetailsDTO updateOAuthClientUsingPUT(clientDetailsDTO)

updateOAuthClient

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.OAuthClientsResourceApi;


OAuthClientsResourceApi apiInstance = new OAuthClientsResourceApi();
ClientDetailsDTO clientDetailsDTO = new ClientDetailsDTO(); // ClientDetailsDTO | clientDetailsDTO
try {
    ClientDetailsDTO result = apiInstance.updateOAuthClientUsingPUT(clientDetailsDTO);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling OAuthClientsResourceApi#updateOAuthClientUsingPUT");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **clientDetailsDTO** | [**ClientDetailsDTO**](ClientDetailsDTO.md)| clientDetailsDTO |

### Return type

[**ClientDetailsDTO**](ClientDetailsDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

