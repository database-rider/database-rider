package com.github.trks1970

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class EmailEntity (
    @Id
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var mail: String? = null
){}