# PlateWatcher Android

Prototype Android Kotlin prêt à ouvrir dans Android Studio.

## Fonctionnalités incluses
- Caméra arrière via CameraX.
- Analyse temps réel avec zone centrale optimisée pour véhicules arrivant de face.
- OCR on-device via ML Kit Text Recognition.
- Classification basique de plaques BE / FR / NL? / DE?.
- Stabilisation sur plusieurs frames avant enregistrement.
- Enregistrement local Room : plaque, pays estimé, confiance, date/heure, chemin de l'image recadrée.
- Sauvegarde d'un crop JPEG de la plaque détectée dans le stockage privé de l'application.
- Purge automatique des détections de plus de 7 jours.

## Compilation APK
1. Ouvrir ce dossier dans Android Studio.
2. Laisser Gradle synchroniser.
3. Brancher un téléphone Android réel.
4. Cliquer sur Run pour installer l'app.

Pour générer un APK :
- Android Studio > Build > Build Bundle(s) / APK(s) > Build APK(s)
- ou terminal : `./gradlew assembleDebug`

L'APK sera dans : `app/build/outputs/apk/debug/app-debug.apk`.

## Limite importante
Cette version ne contient pas de modèle ANPR professionnel entraîné spécifiquement sur les plaques européennes. Elle utilise un recadrage de zone utile + OCR + reconnaissance de formats. Pour une fiabilité élevée en mouvement, il faudra ajouter un modèle TFLite de détection de plaques, puis lancer l'OCR uniquement sur le crop détecté.

## Vie privée / RGPD
Ne pas utiliser en continu sur voie publique sans base légale claire. Les plaques sont des données personnelles. Par défaut, le prototype ne sauvegarde pas de vidéo complète, uniquement la plaque détectée et un crop local.
