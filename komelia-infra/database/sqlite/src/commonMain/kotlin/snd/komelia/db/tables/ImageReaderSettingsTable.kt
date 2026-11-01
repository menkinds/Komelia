package snd.komelia.db.tables

import org.jetbrains.exposed.v1.core.Table

object ImageReaderSettingsTable : Table("ImageReaderSettings") {
    val bookId = text("book_id")

    val readerType = text("reader_type")
    val stretchToFit = bool("stretch_to_fit")

    val pagedScaleType = text("paged_scale_type")
    val pagedReadingDirection = text("paged_reading_direction")
    val pagedPageLayout = text("paged_page_layout")

    val continuousReadingDirection = text("continuous_reading_direction")
    val continuousPadding = float("continuous_padding")
    val continuousPageSpacing = integer("continuous_page_spacing")
    val cropBorders = bool("crop_borders")

    val loadThumbnailPreviews = bool("load_thumbnail_previews")
    val volumeKeysNavigation = bool("volume_keys_navigation")
    val autoTurnIntervalSeconds = integer("auto_turn_interval_seconds")
    val showClock = bool("fork_show_clock")
    val textAwareAutoTurn = bool("fork_auto_turn_text_aware")
    val textAwarePerCharMs = integer("fork_auto_turn_per_char_ms")
    val textAwareMinSeconds = integer("fork_auto_turn_min_seconds")
    val textAwareMaxSeconds = integer("fork_auto_turn_max_seconds")
    val textAwareOcrLanguage = text("fork_auto_turn_ocr_language")

    val flashOnPageChange = bool("flash_on_page_change")
    val flashDuration = long("flash_duration")
    val flashEveryNPages = integer("flash_every_n_pages")
    val flashWith = text("flash_with")

    val downsamplingKernel = text("downsampling_kernel")
    val linearLightDownsampling = bool("linear_light_downsampling")
    val upsamplingMode = text("upsampling_mode")

    val ortDeviceId = integer("onnx_runtime_device_id")
    val ortUpscalerMode = text("onnx_runtime_mode")
    val ortUpscalerTileSize = integer("onnx_runtime_tile_size")
    val ortUpscalerUserModelPath = text("onnx_runtime_model_path").nullable()

    override val primaryKey = PrimaryKey(bookId)
}