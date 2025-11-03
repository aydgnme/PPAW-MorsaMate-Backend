/**
 * MorseMate API Client - Example Usage
 *
 * Bu dosya, oluşturulan TypeScript client'ı nasıl kullanacağınızı gösterir.
 */

import { HealthApi, CategoriesApi, SystemApi, Configuration } from './';

// API Configuration
const config = new Configuration({
  basePath: 'http://localhost:8080',
  // JWT token kullanıyorsanız:
  // accessToken: 'your-jwt-token-here'
});

// API Instances
const healthApi = new HealthApi(config);
const categoriesApi = new CategoriesApi(config);
const systemApi = new SystemApi(config);

/**
 * Health Check örneği
 */
async function checkHealth() {
  try {
    const response = await healthApi.healthCheck();
    console.log('Health Status:', response.data);
  } catch (error) {
    console.error('Health check failed:', error);
  }
}

/**
 * System Info örneği
 */
async function getSystemInfo() {
  try {
    const response = await systemApi.getApiRoot();
    console.log('API Info:', response.data);
  } catch (error) {
    console.error('Failed to get system info:', error);
  }
}

/**
 * Get All Categories örneği
 */
async function getAllCategories() {
  try {
    const response = await categoriesApi.getAllCategories();
    console.log('Categories:', response.data);
    return response.data;
  } catch (error) {
    console.error('Failed to get categories:', error);
    return [];
  }
}

/**
 * Get Category by ID örneği
 */
async function getCategoryById(id: number) {
  try {
    const response = await categoriesApi.getCategoryById(id);
    console.log('Category:', response.data);
    return response.data;
  } catch (error) {
    console.error(`Failed to get category ${id}:`, error);
    return null;
  }
}

/**
 * Create Category örneği
 * Not: Bu endpoint authentication gerektirir
 */
async function createCategory() {
  try {
    const newCategory = {
      name: 'Test Category',
      description: 'A test category created via API',
      difficulty: 'BEGINNER' as const,
      orderIndex: 1,
      isActive: true
    };

    const response = await categoriesApi.createCategory(newCategory);
    console.log('Created Category:', response.data);
    return response.data;
  } catch (error) {
    console.error('Failed to create category:', error);
    return null;
  }
}

/**
 * Update Category örneği
 * Not: Bu endpoint authentication gerektirir
 */
async function updateCategory(id: number) {
  try {
    const updatedCategory = {
      name: 'Updated Category',
      description: 'Updated description',
      difficulty: 'INTERMEDIATE' as const,
      orderIndex: 2,
      isActive: true
    };

    const response = await categoriesApi.updateCategory(id, updatedCategory);
    console.log('Updated Category:', response.data);
    return response.data;
  } catch (error) {
    console.error(`Failed to update category ${id}:`, error);
    return null;
  }
}

/**
 * Delete Category örneği
 * Not: Bu endpoint authentication gerektirir
 */
async function deleteCategory(id: number) {
  try {
    await categoriesApi.deleteCategory(id);
    console.log(`Category ${id} deleted successfully`);
    return true;
  } catch (error) {
    console.error(`Failed to delete category ${id}:`, error);
    return false;
  }
}

/**
 * Ana test fonksiyonu
 */
async function main() {
  console.log('=== MorseMate API Client Test ===\n');

  // 1. Health Check
  console.log('1. Checking API Health...');
  await checkHealth();
  console.log('');

  // 2. System Info
  console.log('2. Getting System Info...');
  await getSystemInfo();
  console.log('');

  // 3. Get All Categories
  console.log('3. Getting All Categories...');
  const categories = await getAllCategories();
  console.log('');

  // 4. Get Category by ID (if any exist)
  if (categories.length > 0) {
    console.log('4. Getting First Category...');
    await getCategoryById(categories[0].id!);
    console.log('');
  }

  // 5. Try to get non-existent category
  console.log('5. Testing 404 Error...');
  await getCategoryById(99999);
  console.log('');

  console.log('=== Test Complete ===');
}

// Export functions for use in other files
export {
  checkHealth,
  getSystemInfo,
  getAllCategories,
  getCategoryById,
  createCategory,
  updateCategory,
  deleteCategory
};

// Run main if this file is executed directly
if (require.main === module) {
  main().catch(console.error);
}
