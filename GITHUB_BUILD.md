# Build APK avec GitHub Actions

Ce projet contient déjà un workflow GitHub Actions :

`.github/workflows/android-build.yml`

## Étapes

1. Créer un dépôt GitHub vide, par exemple `PlateWatcher`.
2. Envoyer tout le contenu du dossier `PlateWatcher` dans le dépôt.
3. Aller dans l'onglet **Actions** du dépôt.
4. Lancer le workflow **Build Android APK** avec **Run workflow**.
5. Quand le build est terminé, ouvrir le run et télécharger l'artifact **PlateWatcher-debug-apk**.

Le fichier obtenu contiendra l'APK debug, généralement :

`app-debug.apk`

## Remarque

Ce workflow installe Gradle 8.9 dans GitHub Actions. Il ne nécessite donc pas de fichier `gradlew` dans le dépôt.
