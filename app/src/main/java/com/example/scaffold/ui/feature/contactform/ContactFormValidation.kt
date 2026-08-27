package com.example.scaffold.ui.feature.contactform

import androidx.annotation.StringRes
import com.example.scaffold.R

// UK postcode shape, per Royal Mail's published format rules (see Wikipedia,
// "Postcodes in the United Kingdom" § Validation):
//   - outward code is one of six shapes: A9, A99, A9A, AA9, AA99, AA9A
//   - the area's first letter is never Q, V or X
//   - a two-letter area's second letter is never I, J or Z
//   - the single letter in an A9A outward code is one of ABCDEFGHJKPSTUW
//   - the single letter in an AA9A outward code is one of ABEHMNPRVWXY
//   - the inward code's two letters are never C, I, K, M, O or V (visually
//     confusable with digits, or with each other when handwritten)
//   - "GIR 0AA" (former Girobank HQ) is a standing special case
// This validates the *shape* is correct, not that the postcode is actually
// allocated — which area uses which outward-code shape isn't itself a
// pattern, so confirming a postcode really exists needs a postcode lookup
// service, not a regex.
private val UK_POSTCODE_REGEX =
    Regex(
        """
        ^(
            GIR\ 0AA
          | [A-PR-UWYZ]\d{1,2}                   \s? \d[A-BD-HJLNP-UW-Z]{2}
          | [A-PR-UWYZ]\d[ABCDEFGHJKPSTUW]        \s? \d[A-BD-HJLNP-UW-Z]{2}
          | [A-PR-UWYZ][A-HK-Y]\d{1,2}            \s? \d[A-BD-HJLNP-UW-Z]{2}
          | [A-PR-UWYZ][A-HK-Y]\d[ABEHMNPRVWXY]   \s? \d[A-BD-HJLNP-UW-Z]{2}
        )$
        """.trimIndent(),
        setOf(RegexOption.IGNORE_CASE, RegexOption.COMMENTS),
    )

@StringRes
fun requiredFieldError(value: String): Int? = if (value.isBlank()) R.string.contact_form_error_required else null

@StringRes
fun postcodeError(value: String): Int? =
    when {
        value.isBlank() -> R.string.contact_form_error_required
        !UK_POSTCODE_REGEX.matches(value.trim()) -> R.string.contact_form_error_invalid_postcode
        else -> null
    }
