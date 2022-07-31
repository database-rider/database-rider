package com.github.trks1970

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class EmailEntity (
    @Id
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var mail: String? = null
){}