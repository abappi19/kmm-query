# KMP Query (Kotlin Multiplatform Query)

[![Maven Central](https://img.shields.io/maven-central/v/io.github.abappi19/kmp-query)](https://central.sonatype.com/search?namespace=io.github.abappi19)

A powerful data fetching and caching library for Kotlin Multiplatform, built for speed, simplicity, and reliability.

## Features

- Declarative query definition & execution
- Automatic background refetching
- Multiplatform caching layer
- Observables for UI integration
- Customizable retry policies
- Automatic garbage collection
- Type-safe API surface

## Installation

Add the dependency to your common source set:

```kotlin
commonMain.dependencies {
    implementation("io.github.abappi19:kmp-query:1.0.0")
}
```

## Basic Usage

```kotlin
val queryClient = QueryClient(config = QueryConfig(
    cacheTime = Duration.minutes(5),
    staleTime = Duration.seconds(30)
))

// Create query
val userQuery = queryClient.createQuery(
    queryKey = listOf("user", userId),
    queryFn = { fetchUser(userId) }
)

// React to state changes
userQuery.observe { state ->
    when {
        state.isLoading -> showLoading()
        state.isError -> showError(state.error)
        else -> displayUser(state.data)
    }
}

// Manual invalidation
fun updateProfile() {
    queryClient.invalidateQueries(listOf("user", userId))
}
```

## Advanced Patterns

### Parallel Queries
```kotlin
val postsQuery = queryClient.createQuery(listOf("posts"), fetchPosts)
val commentsQuery = queryClient.createQuery(listOf("comments"), fetchComments)

// Combine using coroutines
val combinedData = suspend {
    awaitAll(postsQuery.await(), commentsQuery.await())
}
```

### Mutations
```kotlin
val updateUser = queryClient.createMutation(
    mutationFn = { user -> api.updateUser(user) },
    onSuccess = { invalidateUserQueries(it.id) }
)
```

[//]: # ()
[//]: # (## API Reference)

## Contributing

See <mcurl name="CONTRIBUTING.md" url="https://github.com/abappi19/kmp-query/blob/main/CONTRIBUTING.md"></mcurl> for guidelines.

## License

Distributed under Apache 2.0 License

