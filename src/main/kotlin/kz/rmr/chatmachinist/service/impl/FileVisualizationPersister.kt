package kz.rmr.chatmachinist.service.impl

import kz.rmr.chatmachinist.model.DialogVisualization
import kz.rmr.chatmachinist.service.VisualizationPersister
import mu.KotlinLogging
import java.io.File
import kotlin.reflect.jvm.jvmName

class FileVisualizationPersister : VisualizationPersister {

    private val logger = KotlinLogging.logger(this::class.jvmName)

    override fun persist(visualization: DialogVisualization) {
        val mmdFile = File(
            "mermaid/${visualization.chatName}/${visualization.dialogName}.mmd"
                .replace(" ", "_")
        )

        if (mmdFile.exists()) {
            mmdFile.delete()
        }

        mmdFile.parentFile.mkdirs()
        mmdFile.createNewFile()
        logger.debug { "Created file ${mmdFile.path}" }
        mmdFile.writeText(visualization.mermaid)
    }
}