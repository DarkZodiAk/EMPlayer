# Evgeny's Music Player
Music player for Android written in Kotlin

## Features
- Sort songs by title, artist, album, duration, add & modify time
- Create and edit playlists
- Access music folders
- Search songs by title and artist
- Shuffle and playback modes
- Playback notification
- Restore player state when restarting the app

## Screenshots
![Group 4](https://github.com/user-attachments/assets/3d6b0014-41d4-47df-bebd-c7f60ed483c6)



## What could be done
- Use string resources instead of placing plain text in screens
- Artists tabs
- Sleep timer
- Fix issue: the app crashes when it tries to load invalid/corrupted song files

## About features and project structure
- The project wasn't built in "Ideal" Clean Architecture to simplify project structure. Basically, here I'm using the KISS principle.
- In presentation layer I divide screens in 2 parts: The actual screen, that only accepts it's state and onAction lambda; The root function, that holds ViewModel and other arguments.
It provides state and onAction function to actual screen. The benefits: making previews for actual screen become painless and fast; managing screen actions become easier, cause they are processed in one function.

## Used technologies & libraries
- Coroutines & Flows
- Jetpack Compose
- Navigation Compose (Type-Safe)
- Room DB
- Dagger & Hilt
- ExoPlayer
- Coil
