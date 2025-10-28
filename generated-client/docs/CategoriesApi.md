# CategoriesApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**createCategory**](#createcategory) | **POST** /v1/categories | Create a new category|
|[**deleteCategory**](#deletecategory) | **DELETE** /v1/categories/{id} | Delete category|
|[**getAllCategories**](#getallcategories) | **GET** /v1/categories | Get all categories|
|[**getCategoryById**](#getcategorybyid) | **GET** /v1/categories/{id} | Get category by ID|
|[**updateCategory**](#updatecategory) | **PUT** /v1/categories/{id} | Update category|

# **createCategory**
> Category createCategory(categoryRequest)


### Example

```typescript
import {
    CategoriesApi,
    Configuration,
    CategoryRequest
} from 'morsemate-api-client';

const configuration = new Configuration();
const apiInstance = new CategoriesApi(configuration);

let categoryRequest: CategoryRequest; //

const { status, data } = await apiInstance.createCategory(
    categoryRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **categoryRequest** | **CategoryRequest**|  | |


### Return type

**Category**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**201** | Category created successfully |  -  |
|**400** | Invalid input |  -  |
|**401** | Unauthorized |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **deleteCategory**
> deleteCategory()


### Example

```typescript
import {
    CategoriesApi,
    Configuration
} from 'morsemate-api-client';

const configuration = new Configuration();
const apiInstance = new CategoriesApi(configuration);

let id: number; // (default to undefined)

const { status, data } = await apiInstance.deleteCategory(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**number**] |  | defaults to undefined|


### Return type

void (empty response body)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**204** | Category deleted successfully |  -  |
|**404** | Category not found |  -  |
|**401** | Unauthorized |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getAllCategories**
> Array<Category> getAllCategories()


### Example

```typescript
import {
    CategoriesApi,
    Configuration
} from 'morsemate-api-client';

const configuration = new Configuration();
const apiInstance = new CategoriesApi(configuration);

const { status, data } = await apiInstance.getAllCategories();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**Array<Category>**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | List of categories |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getCategoryById**
> Category getCategoryById()


### Example

```typescript
import {
    CategoriesApi,
    Configuration
} from 'morsemate-api-client';

const configuration = new Configuration();
const apiInstance = new CategoriesApi(configuration);

let id: number; // (default to undefined)

const { status, data } = await apiInstance.getCategoryById(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**number**] |  | defaults to undefined|


### Return type

**Category**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Category details |  -  |
|**404** | Category not found |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **updateCategory**
> Category updateCategory(categoryRequest)


### Example

```typescript
import {
    CategoriesApi,
    Configuration,
    CategoryRequest
} from 'morsemate-api-client';

const configuration = new Configuration();
const apiInstance = new CategoriesApi(configuration);

let id: number; // (default to undefined)
let categoryRequest: CategoryRequest; //

const { status, data } = await apiInstance.updateCategory(
    id,
    categoryRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **categoryRequest** | **CategoryRequest**|  | |
| **id** | [**number**] |  | defaults to undefined|


### Return type

**Category**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Category updated successfully |  -  |
|**404** | Category not found |  -  |
|**401** | Unauthorized |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

