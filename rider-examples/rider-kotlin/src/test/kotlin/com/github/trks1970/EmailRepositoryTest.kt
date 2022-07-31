package com.github.trks1970

import com.github.database.rider.core.api.configuration.DBUnit
import com.github.database.rider.core.api.dataset.DataSet
import com.github.database.rider.junit5.api.DBRider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
    classes = [
        H2JPAConfig::class,
    ]
)
@DBRider
@DBUnit(caseSensitiveTableNames = true, escapePattern = "\"?\"", mergeDataSets = true)
@DataSet(value = ["/data.yaml"], cleanAfter = true)
class EmailRepositoryTest {
    @Autowired
    lateinit var emailRepository: EmailRepository

    @Test
    fun testFindAll() {
        assertThat(emailRepository.findAll()).hasSize(4)
    }

    @Test
    fun testDeleteAll() {
        emailRepository.deleteAll()
        assertThat(emailRepository.findAll().size).isEqualTo(0)
    }

    @Test
    fun testInsert() {
        emailRepository.save(
            EmailEntity(1000, "uriel@heaven.org")
        )
        assertThat(emailRepository.findAll().size).isEqualTo(5)
    }
}