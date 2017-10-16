package com.sensedia.triggers.utils

import com.sensedia.triggers.domain.Table
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*
import javax.sql.DataSource
import kotlin.collections.ArrayList

@Component
class TriggersGenerator {

    @Autowired
    lateinit var dataSource: DataSource
    val api: String = "API_REQUESTOR"

    fun generateScript(): String {
        val rs = dataSource.getConnection().metaData.getTables(null, null, "%", null)
        val tables = listOf("EXAMPLE_ONE", "EXAMPLE_TWO")

        var generatedScripts = ArrayList<String>()

        while (rs.next()) {
            var tableName = rs.getString(3)
            if (tables.contains(tableName) && !generatedScripts.contains(tableName)) {
                val resultSet = dataSource.getConnection().metaData.getColumns(null, null, tableName, null)
                val table = Table(tableName)
                val tableFields = HashMap<String, String>()
                while (resultSet.next()) {
                    val name = resultSet.getString("COLUMN_NAME")
                    val type = resultSet.getString("TYPE_NAME")
                    val size = resultSet.getInt("COLUMN_SIZE")
                    tableFields.set(name, getFormattedField(type, size))
                    table.fields = tableFields
                }
                println(generateScript(table) + "\n")
                generatedScripts.add(tableName)
            }
        }
        if (generatedScripts.size != tables.size) {
            println("It's missing some tables")
        }
        return ""
    }

    private fun getFormattedField(type: String?, size: Int): String = when (type) {
        "VARCHAR2" -> "VARCHAR2($size CHAR)"
        "NUMBER" -> "NUMBER($size, 0)"
        "DATE" -> "DATE"
        "TIMESTAMP" -> "TIMESTAMP"
        "TIMESTAMP(6)" -> "TIMESTAMP"
        "CHAR" -> "CHAR($size)"
        "FLOAT" -> "FLOAT"
        else -> throw IllegalArgumentException("$type is an invalid field")
    }

    private fun generateScript(table: Table): String {
        var createTemplate: String = ""
        var deleteTemplate: String = ""
        var definitionTemplate: String = ""
        var idName: String = "vID"
        var jsonObject: String = "("

        if (table.fields.filterKeys { k -> k == "ID" }.count() == 0) {
            if (table.fields.filterKeys { k -> k == "ACRONYM" }.count() == 1) {
                idName = "vACRONYM"
            } else if (table.fields.filterKeys { k -> k == "CODE" }.count() == 1) {
                idName = "vCODE"
            } else {
                throw IllegalArgumentException("Table must be contain an ID field")
            }
        }

        table.fields.forEach { columnName, size ->
            createTemplate += "    v$columnName := :NEW.$columnName;\n"
            deleteTemplate += "    v$columnName := :OLD.$columnName;\n"
            definitionTemplate += "  v$columnName $size;\n"
            jsonObject += "\'${columnName.toLowerCase()}\' VALUE v$columnName, "
        }

        val sb = StringBuilder(jsonObject)
        sb.setCharAt(jsonObject.length - 1, ')')
        jsonObject = sb.toString().replace(",)", ")")
        var template: String = "CREATE OR REPLACE EDITIONABLE TRIGGER \"$api\".\"TRG_${table.name}\" \n" +
                "AFTER DELETE OR INSERT OR UPDATE ON \"$api\".\"${table.name}\"\n" +
                "FOR EACH ROW\n" +
                "DECLARE\n" +
                "  vOperation VARCHAR2(6);\n" +
                "  vTableName VARCHAR2(30) := '${table.name}';\n" +
                "$definitionTemplate \n" +
                "BEGIN\n" +
                "  \n" +
                "  IF INSERTING THEN\n" +
                "    vOperation := 'INSERT';\n" +
                "$createTemplate \n" +
                "  ELSIF UPDATING THEN\n" +
                "    vOperation := 'UPDATE';\n" +
                "$createTemplate \n" +
                "  ELSIF DELETING THEN\n" +
                "    vOperation := 'DELETE';\n" +
                "$deleteTemplate \n" +
                "  END IF;\n" +
                "  \n" +
                "  INSERT INTO \"AUDIT\" (TABLE_NAME, REGISTER_ID, OPERATION, DATETIME, PO_DOCUMENT)\n" +
                "  VALUES (\n" +
                "    vTableName,\n" +
                "    TO_CHAR($idName),\n" +
                "    vOperation,\n" +
                "    sysdate,\n" +
                "    json_object$jsonObject\n" +
                "  );\n" +
                "\n" +
                "END;"
        return template
    }
}
