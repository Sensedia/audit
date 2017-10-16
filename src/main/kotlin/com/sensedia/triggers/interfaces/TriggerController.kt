package com.sensedia.triggers.interfaces

import com.sensedia.triggers.utils.TriggersGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TriggerController {

    @Autowired
    lateinit var triggersGenerator: TriggersGenerator

    @GetMapping("/triggers")
    fun getScripts(): String {
        return triggersGenerator.generateScript()
    }
}