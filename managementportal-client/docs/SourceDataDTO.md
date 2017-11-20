
# SourceDataDTO

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**dataClass** | [**DataClassEnum**](#DataClassEnum) |  |  [optional]
**enabled** | **Boolean** |  |  [optional]
**frequency** | **String** |  |  [optional]
**id** | **Long** |  |  [optional]
**keySchema** | **String** |  |  [optional]
**processingState** | [**ProcessingStateEnum**](#ProcessingStateEnum) |  |  [optional]
**provider** | **String** |  |  [optional]
**sourceDataName** | **String** |  |  [optional]
**sourceDataType** | **String** |  | 
**sourceType** | [**MinimalSourceTypeDTO**](MinimalSourceTypeDTO.md) |  |  [optional]
**topic** | **String** |  |  [optional]
**unit** | **String** |  |  [optional]
**valueSchema** | **String** |  |  [optional]


<a name="DataClassEnum"></a>
## Enum: DataClassEnum
Name | Value
---- | -----
RAW | &quot;RAW&quot;
DERIVED | &quot;DERIVED&quot;
VENDOR | &quot;VENDOR&quot;


<a name="ProcessingStateEnum"></a>
## Enum: ProcessingStateEnum
Name | Value
---- | -----
RAW | &quot;RAW&quot;
DERIVED | &quot;DERIVED&quot;
VENDOR | &quot;VENDOR&quot;



