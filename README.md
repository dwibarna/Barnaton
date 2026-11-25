# Barnaton

Android app for browsing movies by genre, viewing movie details with trailers, and reading user reviews using the TMDB v3 API.

## Features

- Search movies (`HomeFragment`)
- Browse now playing movies and movie genres (`HomeFragment`)
- Endless movie list per genre with Paging 3 (`MovieListFragment`)
- Movie details, genres, synopsis, and YouTube trailer playback (`MovieDetailFragment`)
- Paginated user reviews (`ReviewsFragment`)
- Loading, empty, and retryable error states (`StateView`)

## Project Structure

- `:app` - Application entry point, `MainActivity`, theme
- `:feature:movies` - Fragments, ViewModels, adapters, layouts, navigation graph
- `:core:data` - `MovieRepository`, PagingSources, mappers
- `:core:network` - `MoviesService`, response models, `ApiKeyInterceptor`, DI module
- `:core:model` - Domain models and `Resource` / `ErrorType`

## Tech Stack

- Kotlin, Coroutines & Flow
- XML + ViewBinding, Material 3, Navigation Component + Safe Args
- Jetpack Paging 3
- Hilt (KSP)
- Retrofit, OkHttp, Kotlinx Serialization
- Coil, Android YouTube Player
- JUnit 4, `kotlinx-coroutines-test`, `paging-testing`, MockK

## Setup

1. Open the project in Android Studio (JDK 17+, `compileSdk` 36, `minSdk` 24).
2. Copy `local.properties.example` to `local.properties` and add your SDK path and TMDB key:
   ```properties
   sdk.dir=/path/to/Android/Sdk
   TMDB_API_KEY=your_tmdb_api_key
   ```
   Both v3 API keys and v4 Read Access Tokens are supported.
3. Sync Gradle and run `:app`.

## Build & Test

```bash
./gradlew assembleDebug testDebugUnitTest
```

On Windows PowerShell:
```powershell
.\gradlew.bat assembleDebug testDebugUnitTest
```
