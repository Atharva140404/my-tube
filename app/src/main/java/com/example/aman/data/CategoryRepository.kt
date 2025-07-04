package com.example.aman.data

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CategoryRepository {
    private val client = SupabaseClient.client
    private val tableName = "categories"
    
    suspend fun getAllCategories(): List<Category> = withContext(Dispatchers.IO) {
        try {
            client.postgrest.from(tableName)
                .select {
                    order("id", Order.ASCENDING)
                }
                .decodeList<Category>()
        } catch (e: Exception) {
            println("Error fetching categories: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    
    suspend fun getCategoryById(id: String): Category? = withContext(Dispatchers.IO) {
        try {
            client.postgrest.from(tableName)
                .select {
                    eq("id", id)
                }
                .decodeSingle<Category>()
        } catch (e: Exception) {
            println("Error fetching category by id: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    suspend fun addCategory(category: Category): Category? = withContext(Dispatchers.IO) {
        try {
            client.postgrest.from(tableName)
                .insert(category)
                .decodeSingle<Category>()
        } catch (e: Exception) {
            println("Error adding category: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    suspend fun updateCategory(category: Category): Boolean = withContext(Dispatchers.IO) {
        try {
            category.id?.let { id ->
                client.postgrest.from(tableName)
                    .update(mapOf("name" to category.name)) {
                        eq("id", id)
                    }
                true
            } ?: false
        } catch (e: Exception) {
            println("Error updating category: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    suspend fun deleteCategory(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            client.postgrest.from(tableName)
                .delete {
                    eq("id", id)
                }
            true
        } catch (e: Exception) {
            println("Error deleting category: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    companion object {
        suspend fun getCategories(): List<Category> {
            return CategoryRepository().getAllCategories()
        }
    }
} 