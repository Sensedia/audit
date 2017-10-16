package com.sensedia.triggers.utils

import com.edenred.epay.commons.app.TestApplication
import com.sensedia.test.app.SimpleIntegrationTest
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import javax.sql.DataSource
import org.springframework.jdbc.core.JdbcTemplate



@SpringBootTest
@ContextConfiguration(classes = arrayOf(TestApplication::class))
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TriggersGeneratorTest : SimpleIntegrationTest() {

    @Autowired
    lateinit var dataSource: DataSource

    @Test
    fun validateBinTrigger() {
       /* val template = JdbcTemplate(this.dataSource)
        template.execute("")*/
    }
}