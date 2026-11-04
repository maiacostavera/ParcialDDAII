plugins {
    alias(libs.plugins.android.application)
    // Plugin del compilador de Compose. Desde AGP 9 el soporte de Kotlin ya viene
    // incluido, pero Compose necesita igual su propio plugin para generar el codigo
    // de las funciones @Composable.
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.example.parcial2breweryguide"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.parcial2breweryguide"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        // View Binding: por cada layout XML se genera una clase Binding con las vistas
        // que tienen id. Es lo que usamos en vez de findViewById.
        viewBinding = true

        // Habilita Jetpack Compose, que usamos solo en la pantalla de detalle.
        compose = true
    }

    lint {
        // Falso positivo: Picasso incluye una clase para mostrar imagenes en
        // notificaciones que la app nunca usa, y lint pide el permiso igual.
        disable += "NotificationPermission"
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.material)

    // MVVM: ViewModel + LiveData
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Listas de cervecerias y de favoritas
    implementation(libs.androidx.recyclerview)

    // Jetpack Compose, solo para la pantalla de detalle.
    // El BOM se importa con platform() para que todas las librerias de abajo
    // tomen su version de ahi y no haya que escribirla en cada linea.
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    // Preview: la anotacion va en el codigo, el motor que la dibuja solo en debug.
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Retrofit para consumir la API + Gson para pasar el JSON a data classes
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

    // Picasso para bajar y mostrar los iconos de las cervecerias
    implementation("com.squareup.picasso:picasso:2.8")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
