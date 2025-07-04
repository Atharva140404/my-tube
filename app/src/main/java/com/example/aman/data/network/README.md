# Supabase REST API Usage Guide

This guide explains how to use the Supabase REST API directly with cURL commands and how we've implemented them in our Android app.

## Base URL Structure

```
https://[PROJECT_REF].supabase.co/rest/v1/
```

For our app, the base URL is:

```
https://nqkccargwpcsbygurmbd.supabase.co/rest/v1/
```

## Authentication Headers

All requests require these headers:

```
apikey: YOUR_SUPABASE_KEY
Authorization: Bearer YOUR_SUPABASE_KEY
```

## Common Operations with cURL

### Get All Videos

```bash
curl -X GET 'https://nqkccargwpcsbygurmbd.supabase.co/rest/v1/video?select=*' \
-H "apikey: YOUR_SUPABASE_KEY" \
-H "Authorization: Bearer YOUR_SUPABASE_KEY"
```

### Get Videos by Category

```bash
curl -X GET 'https://nqkccargwpcsbygurmbd.supabase.co/rest/v1/video?category_id=eq.CATEGORY_ID&select=*' \
-H "apikey: YOUR_SUPABASE_KEY" \
-H "Authorization: Bearer YOUR_SUPABASE_KEY"
```

### Get Videos with Category Information (Foreign Table)

```bash
curl -X GET 'https://nqkccargwpcsbygurmbd.supabase.co/rest/v1/video?select=*,categories(*)' \
-H "apikey: YOUR_SUPABASE_KEY" \
-H "Authorization: Bearer YOUR_SUPABASE_KEY"
```

### Get Paginated Videos

```bash
curl -X GET 'https://nqkccargwpcsbygurmbd.supabase.co/rest/v1/video?select=*&limit=20&offset=0&order=title.asc' \
-H "apikey: YOUR_SUPABASE_KEY" \
-H "Authorization: Bearer YOUR_SUPABASE_KEY"
```

## Implementation in Our App

We've implemented two approaches to make REST API calls:

1. **SupabaseRestClient** - Uses Ktor HTTP client (already used in our app)
2. **CurlStyleRequests** - Shows how to use OkHttp to make cURL-style requests

### Using SupabaseRestClient

```kotlin
val restClient = SupabaseRestClient()

// Get all videos
val videos = restClient.getAllVideos()

// Get videos by category
val categoryVideos = restClient.getVideosByCategory("category-id")

// Get videos with category info
val videosWithCategories = restClient.getVideosWithCategoryInfo()
```

### Using CurlStyleRequests (OkHttp)

```kotlin
// Get all videos
val videos = CurlStyleRequests.getAllVideos()

// Get videos by category
val categoryVideos = CurlStyleRequests.getVideosByCategory("category-id")

// Get popular videos
val popularVideos = CurlStyleRequests.getPopularVideos(10)
```

## Query Parameter Options

Supabase REST API supports various query parameters:

- **select**: Specify columns to return (`?select=id,title`)
- **order**: Sort results (`?order=title.asc` or `?order=views.desc`)
- **limit/offset**: Pagination (`?limit=10&offset=20`)
- **Filters**:
  - Equal: `?column=eq.value`
  - Greater than: `?column=gt.value`
  - Less than: `?column=lt.value`
  - Like: `?column=like.%value%`
  - In: `?column=in.(value1,value2)`

## Testing with Postman

You can test these endpoints in Postman:

1. Set request type to GET
2. Add these headers:
   - apikey: YOUR_SUPABASE_KEY
   - Authorization: Bearer YOUR_SUPABASE_KEY
   - Content-Type: application/json
3. Use the URLs shown in the cURL examples above 