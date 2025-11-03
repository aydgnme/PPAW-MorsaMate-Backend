# SystemApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**getApiRoot**](#getapiroot) | **GET** /api | API root endpoint|

# **getApiRoot**
> GetApiRoot200Response getApiRoot()


### Example

```typescript
import {
    SystemApi,
    Configuration
} from 'morsemate-api-client';

const configuration = new Configuration();
const apiInstance = new SystemApi(configuration);

const { status, data } = await apiInstance.getApiRoot();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**GetApiRoot200Response**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | API information |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

