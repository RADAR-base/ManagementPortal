# RoleResourceApi

All URIs are relative to *https://localhost:8080/*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createRoleUsingPOST**](RoleResourceApi.md#createRoleUsingPOST) | **POST** /api/roles | createRole
[**getAllAdminRolesUsingGET**](RoleResourceApi.md#getAllAdminRolesUsingGET) | **GET** /api/roles/admin | getAllAdminRoles
[**getAllRolesUsingGET**](RoleResourceApi.md#getAllRolesUsingGET) | **GET** /api/roles | getAllRoles
[**getRoleUsingGET**](RoleResourceApi.md#getRoleUsingGET) | **GET** /api/roles/{projectName}/{authorityName} | getRole
[**updateRoleUsingPUT**](RoleResourceApi.md#updateRoleUsingPUT) | **PUT** /api/roles | updateRole


<a name="createRoleUsingPOST"></a>
# **createRoleUsingPOST**
> RoleDTO createRoleUsingPOST(roleDTO)

createRole

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.RoleResourceApi;


RoleResourceApi apiInstance = new RoleResourceApi();
RoleDTO roleDTO = new RoleDTO(); // RoleDTO | roleDTO
try {
    RoleDTO result = apiInstance.createRoleUsingPOST(roleDTO);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling RoleResourceApi#createRoleUsingPOST");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **roleDTO** | [**RoleDTO**](RoleDTO.md)| roleDTO |

### Return type

[**RoleDTO**](RoleDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getAllAdminRolesUsingGET"></a>
# **getAllAdminRolesUsingGET**
> List&lt;RoleDTO&gt; getAllAdminRolesUsingGET()

getAllAdminRoles

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.RoleResourceApi;


RoleResourceApi apiInstance = new RoleResourceApi();
try {
    List<RoleDTO> result = apiInstance.getAllAdminRolesUsingGET();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling RoleResourceApi#getAllAdminRolesUsingGET");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**List&lt;RoleDTO&gt;**](RoleDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getAllRolesUsingGET"></a>
# **getAllRolesUsingGET**
> List&lt;RoleDTO&gt; getAllRolesUsingGET()

getAllRoles

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.RoleResourceApi;


RoleResourceApi apiInstance = new RoleResourceApi();
try {
    List<RoleDTO> result = apiInstance.getAllRolesUsingGET();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling RoleResourceApi#getAllRolesUsingGET");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**List&lt;RoleDTO&gt;**](RoleDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getRoleUsingGET"></a>
# **getRoleUsingGET**
> RoleDTO getRoleUsingGET(projectName, authorityName)

getRole

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.RoleResourceApi;


RoleResourceApi apiInstance = new RoleResourceApi();
String projectName = "projectName_example"; // String | projectName
String authorityName = "authorityName_example"; // String | authorityName
try {
    RoleDTO result = apiInstance.getRoleUsingGET(projectName, authorityName);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling RoleResourceApi#getRoleUsingGET");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **projectName** | **String**| projectName |
 **authorityName** | **String**| authorityName |

### Return type

[**RoleDTO**](RoleDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="updateRoleUsingPUT"></a>
# **updateRoleUsingPUT**
> RoleDTO updateRoleUsingPUT(roleDTO)

updateRole

### Example
```java
// Import classes:
//import org.radarcns.management.client.ApiException;
//import org.radarcns.management.client.api.RoleResourceApi;


RoleResourceApi apiInstance = new RoleResourceApi();
RoleDTO roleDTO = new RoleDTO(); // RoleDTO | roleDTO
try {
    RoleDTO result = apiInstance.updateRoleUsingPUT(roleDTO);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling RoleResourceApi#updateRoleUsingPUT");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **roleDTO** | [**RoleDTO**](RoleDTO.md)| roleDTO |

### Return type

[**RoleDTO**](RoleDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

