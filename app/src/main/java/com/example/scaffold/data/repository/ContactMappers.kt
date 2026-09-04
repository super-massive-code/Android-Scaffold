package com.example.scaffold.data.repository

import com.example.scaffold.data.local.ContactEntity
import com.example.scaffold.model.Contact

fun Contact.toEntity(): ContactEntity =
    ContactEntity(
        // Carried through, not dropped: @Update and @Delete match on the primary key, and an
        // id of 0 (a contact that has never been written) is what triggers autoGenerate.
        id = id,
        firstName = firstName,
        lastName = lastName,
        addressLine1 = addressLine1,
        addressLine2 = addressLine2,
        city = city,
        postcode = postcode,
    )

fun ContactEntity.toDomain(): Contact =
    Contact(
        id = id,
        firstName = firstName,
        lastName = lastName,
        addressLine1 = addressLine1,
        addressLine2 = addressLine2,
        city = city,
        postcode = postcode,
    )
