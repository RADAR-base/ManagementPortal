
# SubjectDTO

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**attributes** | **Map&lt;String, String&gt;** |  |  [optional]
**createdBy** | **String** |  |  [optional]
**createdDate** | [**DateTime**](DateTime.md) |  |  [optional]
**externalId** | **String** |  |  [optional]
**externalLink** | **String** |  |  [optional]
**id** | **Long** |  |  [optional]
**lastModifiedBy** | **String** |  |  [optional]
**lastModifiedDate** | [**DateTime**](DateTime.md) |  |  [optional]
**login** | **String** |  |  [optional]
**project** | [**ProjectDTO**](ProjectDTO.md) |  |  [optional]
**sources** | [**List&lt;MinimalSourceDetailsDTO&gt;**](MinimalSourceDetailsDTO.md) |  |  [optional]
**status** | [**StatusEnum**](#StatusEnum) |  |  [optional]


<a name="StatusEnum"></a>
## Enum: StatusEnum
Name | Value
---- | -----
DEACTIVATED | &quot;DEACTIVATED&quot;
ACTIVATED | &quot;ACTIVATED&quot;
DISCONTINUED | &quot;DISCONTINUED&quot;
INVALID | &quot;INVALID&quot;



