package be.habran.platewatcher

import java.util.Locale

object PlateClassifier {
    fun normalize(raw: String): String = raw
        .uppercase(Locale.ROOT)
        .replace(" ", "")
        .replace("-", "")
        .replace(".", "")
        .replace("\n", "")
        .replace("O", "0") // correction utile pour les chiffres, appliquée avec prudence via formats ci-dessous

    fun classify(rawToken: String): PlateCandidate? {
        val original = rawToken.uppercase(Locale.ROOT).replace(Regex("[^A-Z0-9]"), "")
        val clean = original.take(10)
        val numericFriendly = clean.replace("O", "0")

        return when {
            // Belgique récente: 1-ABC-123 / 2-ABC-123, séparateurs déjà retirés
            Regex("^[1-9][A-Z]{3}[0-9]{3}$").matches(clean) -> PlateCandidate(clean, "BE", 0.94f, null)
            Regex("^[1-9][A-Z]{3}[0-9]{3}$").matches(numericFriendly) -> PlateCandidate(numericFriendly, "BE", 0.88f, null)

            // France SIV: AB-123-CD
            Regex("^[A-Z]{2}[0-9]{3}[A-Z]{2}$").matches(clean) -> PlateCandidate(clean, "FR", 0.92f, null)

            // Pays-Bas: beaucoup de variantes; fiable seulement si pattern mixte sur 6 caractères.
            Regex("^[A-Z0-9]{6}$").matches(clean) && clean.any { it.isDigit() } && clean.any { it.isLetter() } ->
                PlateCandidate(clean, "NL?", 0.70f, null)

            // Allemagne simplifiée sans tirets/sceau: estimation faible.
            Regex("^[A-Z]{1,5}[0-9]{1,4}$").matches(clean) -> PlateCandidate(clean, "DE?", 0.58f, null)

            else -> null
        }
    }
}
