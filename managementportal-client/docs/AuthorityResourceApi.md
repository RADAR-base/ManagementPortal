# AuthorityResourceApi

All URIs are relative to *https://localhost:8080/*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getAllAuthoritiesUsingGET**](AuthorityResourceApi.md#getAllAuthoritiesUsingGET) | **GET** /api/authorities | getAllAuthorities


<a name="getAllAuthoritiesUsingGET"></a>
# **getAllAuthoritiesUsingGET**
> List&lt;String&gt; getAllAuthoritiesUsingGET()

getAllAuthorities

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.AuthorityResourceApi;


AuthorityResourceApi apiInstance = new AuthorityResourceApi();
try {
    List<String> result = apiInstance.getAllAuthoritiesUsingGET();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling AuthorityResourceApi#getAllAuthoritiesUsingGET");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

**List&lt;String&gt;**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

