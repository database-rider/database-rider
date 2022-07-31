package com.github.trks1970

import com.github.trks1970.EmailEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EmailRepository: JpaRepository<EmailEntity, Long>