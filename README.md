# Texterify OTA

An Android SDK for OTA (Over-The-Air) translation updates with [Texterify](https://github.com/chrztoph/texterify/).

> This SDK is currently a preview release for early adopters to collect more feedback and should _not_ be used in production code.

The SDK will wrap calls to `Resources` and hook into the view inflation to replace any string resources with their latest, cached version.  
Currently there is support for a few attributes of `TextView`, `ImageView` and subclasses thereof, as well as `Toolbar` (found in `ota-appcompat`). Menu inflation (option menus, bottom bars, etc.) is currently not supported.

Support for more attributes/types can be added via custom implementations of `Transcriber.Factory` by registering them with `Texterify.Config#addTranscriberFactory`.
 
## How to use

Include the following in your `build.gradle` file:

```gradle
implementation "com.texterify.android:texterify-ota:$latestRelease"
// optional extension for Toolbar support
implementation "com.texterify.android.ota:ota-appcompat:$latestRelease"
```

And add the repository in your app-level `build.gradle` file
```gradle
allprojects {
    repositories {
        // ...
        jcenter()
        mavenCentral()
        // ...

        maven { url 'https://dl.bintray.com/texterify/mvn/' }
    }
}
```

### Setup

In your `Application#onCreate` method initialize the SDK by calling the following lines with _your_ Texterify configuration:

```kotlin
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val config = Texterify.config(this)
            .host("https://texterify.mydomain.io")
            .projectId("my-project-id")
            .exportConfigId("my-export-config-id")

        Texterify.init(config)
    }
}
```

Then in your Activity override `attachBaseContext(..)` to add OTA support for your inflated views.

```kotlin
override fun attachBaseContext(newBase: Context) {
    super.attachBaseContext(Texterify.wrapContext(newBase))
}
```

## Sample

The `./app` directory contains a sample project. To test the OTA features you need to add your own texterify configuration to the `local.properties` file (or replace the configuration code directly in `App.kt`)

```properties
# Texterify configuration
txtfy.host=https://texterify.mydomain.io
txtfy.projectId=my-project-id-1234
txtfy.exportConfigId=my-android-export-config-id
```

You can then start replacing the strings found in `res/values/strings.xml` on Texterify. New translations will be loaded on app start, but there might be some caching delays.
