package com.example.aman.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android

object SupabaseClient {
    // Make these constants public for use in SupabaseRestClient
    const val SUPABASE_URL = "https://nqkccargwpcsbygurmbd.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5xa2NjYXJnd3Bjc2J5Z3VybWJkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDk4MDgzODgsImV4cCI6MjA2NTM4NDM4OH0.0jMAsYbpny4zy-aNMjGYlxbu4pXvPnqePXOwrJ1gF1w"
    
    // Initialize the HTTP client
    private val httpClient = HttpClient(Android)
    
    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY,
    ) {
        install(Postgrest)
        install(Realtime)
    }
}