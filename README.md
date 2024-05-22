# Lock-Application

# App Time Manager

App Time Manager is an Android application designed to help users manage their time on certain apps by blocking them for a specified duration. It's built using Kotlin and Java, which are programming languages widely used for Android app development. The app also uses a tool called Gradle for building and managing the project.

## Features

- List of all the apps installed on your device.
- Select which apps you want to block and for how long.
- The app will prevent you from accessing the blocked apps for the time you've specified.
- Uses a feature called "Foreground Service" to keep running even when it's not actively being used.
- Uses "Usage Stats" to monitor which apps are being used, and "System Alert Window" to display a message when a blocked app is launched.

## Pending Features

- Implementation of Room Database to keep track of which apps are blocked and how much time is left until they're unblocked.
- A summary dashboard showing information about blocked apps and remaining time.

## Technologies Used

- Kotlin
- Java
- Gradle

## How to Use

1. Clone this repository.
2. Open the project in Android Studio.
3. Run the project on an emulator or physical device.

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License

[MIT](https://choosealicense.com/licenses/mit/)
