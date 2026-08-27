package com.example.scaffold.ui.feature.contactform

import com.example.scaffold.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContactFormValidationTest {
    @Test
    fun `requiredFieldError flags blank values`() {
        assertEquals(R.string.contact_form_error_required, requiredFieldError(""))
        assertEquals(R.string.contact_form_error_required, requiredFieldError("   "))
    }

    @Test
    fun `requiredFieldError passes non-blank values`() {
        assertNull(requiredFieldError("Ada"))
    }

    /**
     * Post Code Validation
     */

    @Test
    fun `postcodeError flags a blank postcode as required`() {
        assertEquals(R.string.contact_form_error_required, postcodeError(""))
    }

    @Test
    fun `postcodeError flags a malformed postcode as invalid`() {
        assertEquals(R.string.contact_form_error_invalid_postcode, postcodeError("NOTAPOSTCODE"))
    }

    @Test
    fun `postcodeError passes a valid UK postcode`() {
        assertNull(postcodeError("SW1A 1AA"))
    }

    @Test
    fun `postcodeError is case-insensitive`() {
        assertNull(postcodeError("sw1a 1aa"))
    }

    // One real, documented example per outward-code shape (Royal Mail
    // publishes six shapes: A9, A99, A9A, AA9, AA99, AA9A).
    @Test
    fun `postcodeError accepts every outward-code shape`() {
        assertNull(postcodeError("M1 1AE")) // A9  (Manchester)
        assertNull(postcodeError("M60 1NW")) // A99 (Manchester)
        assertNull(postcodeError("W1A 0AX")) // A9A (BBC Broadcasting House)
        assertNull(postcodeError("CR2 6XH")) // AA9  (Croydon)
        assertNull(postcodeError("DN55 1PT")) // AA99 (Doncaster)
        assertNull(postcodeError("EC1A 1BB")) // AA9A (City of London)
    }

    @Test
    fun `postcodeError accepts the GIR 0AA special case`() {
        assertNull(postcodeError("GIR 0AA"))
        assertNull(postcodeError("gir 0aa"))
    }

    @Test
    fun `postcodeError rejects Q, V or X as the area's first letter`() {
        assertEquals(R.string.contact_form_error_invalid_postcode, postcodeError("QW1 1AA"))
        assertEquals(R.string.contact_form_error_invalid_postcode, postcodeError("V1 1AA"))
        assertEquals(R.string.contact_form_error_invalid_postcode, postcodeError("X1 1AA"))
    }

    @Test
    fun `postcodeError rejects I, J or Z as a two-letter area's second letter`() {
        assertEquals(R.string.contact_form_error_invalid_postcode, postcodeError("BI1 1AA"))
        assertEquals(R.string.contact_form_error_invalid_postcode, postcodeError("AJ1 1AA"))
        assertEquals(R.string.contact_form_error_invalid_postcode, postcodeError("BZ1 1AA"))
    }

    @Test
    fun `postcodeError rejects a district letter outside the A9A allow-list`() {
        // "I" is not one of ABCDEFGHJKPSTUW, the only letters Royal Mail
        // uses in this position (e.g. real W1A, not W1I).
        assertEquals(R.string.contact_form_error_invalid_postcode, postcodeError("W1I 1AA"))
    }

    @Test
    fun `postcodeError rejects a district letter outside the AA9A allow-list`() {
        // "C" is not one of ABEHMNPRVWXY, the only letters Royal Mail uses
        // in this position (e.g. real EC1A, not EC1C).
        assertEquals(R.string.contact_form_error_invalid_postcode, postcodeError("EC1C 1AA"))
    }

    @Test
    fun `postcodeError rejects C, I, K, M, O or V in the inward code's letters`() {
        assertEquals(R.string.contact_form_error_invalid_postcode, postcodeError("SW1A 1AC"))
        assertEquals(R.string.contact_form_error_invalid_postcode, postcodeError("SW1A 1AI"))
        assertEquals(R.string.contact_form_error_invalid_postcode, postcodeError("SW1A 1AK"))
        assertEquals(R.string.contact_form_error_invalid_postcode, postcodeError("SW1A 1AM"))
        assertEquals(R.string.contact_form_error_invalid_postcode, postcodeError("SW1A 1AO"))
        assertEquals(R.string.contact_form_error_invalid_postcode, postcodeError("SW1A 1AV"))
    }

    @Test
    fun `postcodeError accepts a postcode typed without a space`() {
        assertNull(postcodeError("SW1A1AA"))
    }

    @Test
    fun `postcodeError rejects more than one space between outward and inward codes`() {
        assertEquals(R.string.contact_form_error_invalid_postcode, postcodeError("SW1A  1AA"))
    }

    @Test
    fun `postcodeError ignores surrounding whitespace`() {
        assertNull(postcodeError("  SW1A 1AA  "))
    }

    @Test
    fun `postcodeError rejects an inward code that is too short or too long`() {
        assertEquals(R.string.contact_form_error_invalid_postcode, postcodeError("SW1A 1A"))
        assertEquals(R.string.contact_form_error_invalid_postcode, postcodeError("SW1A 1AAA"))
    }
}
