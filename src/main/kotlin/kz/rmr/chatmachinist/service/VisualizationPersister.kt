package kz.rmr.chatmachinist.service

import kz.rmr.chatmachinist.model.DialogVisualization

interface VisualizationPersister {

    fun persist(visualization: DialogVisualization)
}