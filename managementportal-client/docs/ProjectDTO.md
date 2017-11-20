
# ProjectDTO

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**attributes** | [**List&lt;AttributeMapDTO&gt;**](AttributeMapDTO.md) |  |  [optional]
**description** | **String** |  | 
**endDate** | [**DateTime**](DateTime.md) |  |  [optional]
**id** | **Long** |  |  [optional]
**location** | **String** |  | 
**organization** | **String** |  |  [optional]
**projectAdmin** | **Long** |  |  [optional]
**projectName** | **String** |  | 
**projectStatus** | [**ProjectStatusEnum**](#ProjectStatusEnum) |  |  [optional]
**sourceTypes** | [**List&lt;SourceTypeDTO&gt;**](SourceTypeDTO.md) |  |  [optional]
**startDate** | [**DateTime**](DateTime.md) |  |  [optional]


<a name="ProjectStatusEnum"></a>
## Enum: ProjectStatusEnum
Name | Value
---- | -----
PLANNING | &quot;PLANNING&quot;
ONGOING | &quot;ONGOING&quot;
ENDED | &quot;ENDED&quot;



