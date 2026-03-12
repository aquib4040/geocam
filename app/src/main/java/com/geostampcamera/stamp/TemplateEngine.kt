package com.geostampcamera.stamp

import com.geostampcamera.data.model.AppSettings
import com.geostampcamera.data.model.StampTemplate
import com.geostampcamera.data.model.StampTextAlignment
import javax.inject.Inject
import javax.inject.Singleton

// Layout configuration produced by a template
data class TemplateConfig(
    val position: StampPosition,
    val mapAlignment: MapAlignment,
    val textAlignment: StampTextAlignment
)

@Singleton
class TemplateEngine @Inject constructor() {

    // Return layout configuration based on the selected stamp template
    fun getTemplate(settings: AppSettings): TemplateConfig {
        return when (settings.stampTemplate) {
            StampTemplate.CLASSIC -> TemplateConfig(
                position = StampPosition.BOTTOM,
                mapAlignment = MapAlignment.RIGHT,
                textAlignment = settings.textAlignment
            )
            StampTemplate.MODERN -> TemplateConfig(
                position = StampPosition.BOTTOM,
                mapAlignment = MapAlignment.LEFT,
                textAlignment = StampTextAlignment.LEFT
            )
            StampTemplate.MINIMAL -> TemplateConfig(
                position = StampPosition.BOTTOM,
                mapAlignment = MapAlignment.RIGHT,
                textAlignment = StampTextAlignment.LEFT
            )
            StampTemplate.SURVEY -> TemplateConfig(
                position = StampPosition.BOTTOM,
                mapAlignment = MapAlignment.RIGHT,
                textAlignment = StampTextAlignment.LEFT
            )
            StampTemplate.TRAVEL -> TemplateConfig(
                position = StampPosition.TOP,
                mapAlignment = MapAlignment.CENTER,
                textAlignment = StampTextAlignment.CENTER
            )
            StampTemplate.PROFESSIONAL -> TemplateConfig(
                position = StampPosition.BOTTOM,
                mapAlignment = MapAlignment.LEFT,
                textAlignment = StampTextAlignment.RIGHT
            )
        }
    }
}
