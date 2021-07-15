dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        @Suppress("JcenterRepositoryObsolete") jcenter()
        maven("https://jitpack.io")
    }
}
include(":app")
rootProject.name = "Themer"
