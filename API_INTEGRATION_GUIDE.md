# CinemaVerse - API Integration Guide

This document explains how to integrate optional OMDB and TMDB APIs to enhance MCU viewing data with ratings, cast, crew, and additional metadata.

## Quick Start (Offline - No APIs)

The CinemaVerse app is **fully functional offline** with bundled JSON data and 52 local posters. No API keys are required for the core experience.

```bash
# Build and run without any API configuration
./gradlew :app:assembleGithubDebug
adb install app/build/outputs/apk/github/debug/app-github-debug.apk
```

**What works offline:**
- All MCU titles with local posters
- Complete viewing order (release, timeline, phase, saga)
- Search and filtering
- About section with project credits

**What requires APIs:**
- TMDB/IMDb ratings and scores
- Full cast and crew lists
- Extended plot summaries
- Dynamic poster updates from TMDB
- Language and runtime enrichment from OMDb

## API Providers

### OMDb (Open Movie Database)

**Purpose:** Ratings, ratings details, IMDb links, plot, runtime, language, type.

**Sign-up:** https://www.omdbapi.com/apikey.aspx

**Plans:**
- Free: 1,000 requests/day
- Paid: $9.99/month for 100,000 requests/month

**What we use:**
- Title ratings (G, PG, PG-13, R, etc.)
- Plot summaries
- Runtime and language
- Production details
- Awards and nominations (if available)

**API Endpoint:**
```
GET https://www.omdbapi.com/?i=<imdbID>&apikey=<API_KEY>
```

**Example Response:**
```json
{
  "Title": "Iron Man",
  "Year": "2008",
  "Rated": "PG-13",
  "Runtime": "126 min",
  "Plot": "...",
  "imdbRating": "7.9",
  "Language": "English",
  "Cast": "...",
  "Production": "..."
}
```

### TMDB (The Movie Database)

**Purpose:** Posters, backdrops, cast, crew, ratings, production info, images, videos.

**Sign-up:** https://www.themoviedb.org/settings/api

**Plans:**
- Free: Unlimited read access
- Requires account registration only

**What we use:**
- Official movie/series posters
- Backdrop images
- Full cast lists with character names
- Crew information (director, writer, producer)
- Ratings and vote counts
- Release dates
- Production companies
- Genres and keywords

**API Endpoints:**
```
# v3 API (with API key)
GET https://api.themoviedb.org/3/movie/{movie_id}?api_key=<API_KEY>&language=en-US

# v4 API (with Read Access Token)
GET https://api.themoviedb.org/4/discover/movie
Authorization: Bearer <READ_ACCESS_TOKEN>
```

**Example Response:**
```json
{
  "id": 24428,
  "title": "Iron Man",
  "release_date": "2008-05-02",
  "poster_path": "/tnChHMxGBr45FF5QCmMo3iwLj4e.jpg",
  "backdrop_path": "/...",
  "vote_average": 7.9,
  "vote_count": 15234,
  "overview": "...",
  "genres": [{"id": 28, "name": "Action"}],
  "credits": {
    "cast": [...],
    "crew": [...]
  }
}
```

## Setup Instructions

### 1. Get API Keys

**OMDb:**
1. Visit https://www.omdbapi.com/apikey.aspx
2. Enter your email and choose Free or Paid plan
3. Check your email for API key
4. Copy key to `.env` file

**TMDB:**
1. Create account at https://www.themoviedb.org/signup
2. Go to https://www.themoviedb.org/settings/api
3. Accept Terms and click "Create" to generate API key
4. For higher security, also generate Read Access Token (v4 API)
5. Copy keys to `.env` file

### 2. Create `.env` File (Local Development)

```bash
# In project root (DO NOT commit this file)
cp .env.example .env

# Edit .env with your actual API keys
nano .env
```

Contents:
```bash
OMDB_API_KEY=your-actual-omdb-key-here
TMDB_API_KEY=your-actual-tmdb-key-here
TMDB_READ_ACCESS_TOKEN=your-actual-tmdb-token-here
```

### 3. Export Variables Before Building

```bash
# Export to shell environment
export OMDB_API_KEY="your-actual-key"
export TMDB_API_KEY="your-actual-key"
export TMDB_READ_ACCESS_TOKEN="your-actual-token"

# Build with APIs enabled
./gradlew :app:assembleGithubDebug
```

Or use a convenience script:

```bash
#!/bin/bash
# build-with-apis.sh

source .env  # Load from .env file
export OMDB_API_KEY
export TMDB_API_KEY
export TMDB_READ_ACCESS_TOKEN

echo "Building with API enrichment..."
echo "OMDB: ${OMDB_API_KEY:0:10}..."
echo "TMDB: ${TMDB_API_KEY:0:10}..."

./gradlew :app:assembleGithubDebug
adb install app/build/outputs/apk/github/debug/app-github-debug.apk
```

### 4. Verify APIs Are Active

In the app:
1. Open Settings (gear icon in Marvel Spectrum header)
2. Go to About section
3. Look for "Metadata providers: TMDB, OMDb"
4. Browse to a MCU title detail page
5. Should see ratings, cast, crew, and high-quality poster

## Build Configuration

The project's `app/build.gradle.kts` reads API keys from environment variables at build time:

```gradle
buildConfigField("String", "OMDB_API_KEY", "\"${System.getenv("OMDB_API_KEY") ?: ""}\"")
buildConfigField("String", "TMDB_API_KEY", "\"${System.getenv("TMDB_API_KEY") ?: ""}\"")
buildConfigField("String", "TMDB_READ_ACCESS_TOKEN", "\"${System.getenv("TMDB_READ_ACCESS_TOKEN") ?: ""}\"")
```

These are then available in code as:
```kotlin
import com.cinemaverse.mcu.BuildConfig

val omdbKey = BuildConfig.OMDB_API_KEY
val tmdbKey = BuildConfig.TMDB_API_KEY
val tmdbToken = BuildConfig.TMDB_READ_ACCESS_TOKEN
```

## API Usage in Code

### MovieMetadataService

The main service for API calls:

```kotlin
// src/main/java/com/cinemaverse/mcu/shared/data/service/MovieMetadataService.kt

val service = MovieMetadataService()

// Fetch metadata for a title
val omdbData = service.getOmdbMetadata(imdbId = "tt0371746")  // Iron Man

val tmdbData = service.getTmdbMetadata(tmdbId = 24428)  // Iron Man
```

### Configuration Messages

Check what's configured:

```kotlin
val message = service.getConfigurationMessage()
// "Metadata providers: TMDB, OMDb"
// or "No external APIs configured; using bundled data"
```

### Automatic Fallback

The app gracefully handles missing APIs:
- If TMDB is down: Falls back to OMDb, then local data
- If OMDb is down: Uses TMDB and local data
- If both down: Uses only bundled JSON + local posters
- No crashes, just degraded enrichment

## CI/CD Integration

### GitHub Actions

If you have a `.github/workflows/build.yml`:

```yaml
- name: Build APK with API keys
  env:
    OMDB_API_KEY: ${{ secrets.OMDB_API_KEY }}
    TMDB_API_KEY: ${{ secrets.TMDB_API_KEY }}
    TMDB_READ_ACCESS_TOKEN: ${{ secrets.TMDB_READ_ACCESS_TOKEN }}
  run: |
    ./gradlew :app:assembleRelease
```

**Setup secrets:**
1. Go to GitHub repo > Settings > Secrets and variables > Actions
2. Click "New repository secret"
3. Add:
   - Name: `OMDB_API_KEY`, Value: your key
   - Name: `TMDB_API_KEY`, Value: your key
   - Name: `TMDB_READ_ACCESS_TOKEN`, Value: your token

### Vercel (Web Version)

If deploying the web version to Vercel:

1. Go to project Settings > Environment Variables
2. Add:
   - `VITE_OMDB_API_KEY`
   - `VITE_TMDB_API_KEY`
   - `VITE_TMDB_READ_ACCESS_TOKEN`
3. Set to "Production" for production deployments only
4. Local development: Use `.env.local` (not committed)

## Troubleshooting

### APIs Not Activating

**Symptom:** "No external APIs configured; using bundled data"

**Solution:**
1. Verify .env file exists and has real keys (not "YOUR_KEY_HERE")
2. Verify environment variables exported before build:
   ```bash
   echo $OMDB_API_KEY  # Should print your actual key
   ```
3. Clean rebuild:
   ```bash
   ./gradlew clean :app:assembleGithubDebug
   ```

### Ratings/Posters Not Loading

**Symptom:** Titles show but no ratings or TMDB posters

**Possible causes:**
- API keys are invalid or expired
- TMDB/OMDb services are down (check https://status.themoviedb.org)
- Title not found in TMDB/OMDb database (use fallback poster)
- Network connectivity issue

**Solution:**
1. Test API key manually:
   ```bash
   curl "https://www.omdbapi.com/?i=tt0371746&apikey=YOUR_KEY"
   # Should return JSON with movie data
   ```
2. Check logcat for API error messages:
   ```bash
   adb logcat | grep -i "tmdb\|omdb\|metadata"
   ```

### Slow App Startup

**Symptom:** App takes 5+ seconds to load

**Cause:** Might be waiting for API responses on main thread

**Solution:**
- APIs should be fetched asynchronously in background
- Verify MovieMetadataService uses Coroutines or async/await
- Check if API timeouts are configured (suggest 5-second timeout max)

### API Quota Exceeded

**Symptom:** API calls fail with 429 (Too Many Requests)

**For OMDb (free tier):**
- Limited to 1,000 requests/day
- Respect rate limits; add request throttling
- Consider upgrading to paid plan

**For TMDB:**
- Free tier is unlimited but may have per-minute limits
- Add exponential backoff for retries
- Consider caching responses to disk

## Rate Limiting and Caching

To avoid hitting API quotas:

1. **Disk Cache:** Coil automatically caches posters to disk
2. **Memory Cache:** Images cached in memory for current session
3. **API Response Cache:** MovieMetadataService should cache API responses with TTL
4. **Request Throttling:** Limit concurrent API calls to 2-3 per second

Example (pseudo-code):
```kotlin
// Cache API responses for 7 days
private val apiCache = mutableMapOf<String, Pair<Long, ApiResponse>>()

suspend fun getTmdbMetadata(id: Int): ApiResponse? {
    val cacheKey = "tmdb_$id"
    val cached = apiCache[cacheKey]
    
    if (cached != null && (System.currentTimeMillis() - cached.first) < 7 * 24 * 60 * 60 * 1000) {
        return cached.second  // Return cached, don't call API
    }
    
    val response = apiClient.getTmdb(id)
    apiCache[cacheKey] = System.currentTimeMillis() to response
    return response
}
```

## Security Best Practices

1. **Never commit real API keys** to git
2. **Use `.env.local` for development** (add to `.gitignore`)
3. **Use GitHub Secrets for CI/CD** (not hardcoded in workflows)
4. **Rotate keys regularly** if compromised
5. **Monitor API usage** for unusual patterns
6. **Use Read Access Tokens for TMDB** (more secure than API key)
7. **Server-side proxy for sensitive endpoints** (if possible)

## Testing Without APIs

All tests should pass with empty API keys:

```bash
# Build without any API keys
./gradlew :app:assembleGithubDebug

# App should load fully with bundled data
adb install app/build/outputs/apk/github/debug/app-github-debug.apk
```

## Additional Resources

- **OMDb Documentation:** https://www.omdbapi.com/
- **TMDB API Reference:** https://developers.themoviedb.org/3
- **TMDB v4 API:** https://developers.themoviedb.org/4
- **Android Secrets Management:** https://developer.android.com/training/articles/security-key-attestation
- **Kotlin Coroutines:** https://kotlinlang.org/docs/coroutines-overview.html

---

**Last Updated:** June 3, 2026
**Status:** Ready for API integration testing
