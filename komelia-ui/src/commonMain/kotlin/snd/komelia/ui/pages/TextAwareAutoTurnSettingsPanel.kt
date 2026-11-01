package snd.komelia.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.auto_turn_ocr_chinese
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.auto_turn_ocr_download
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.auto_turn_ocr_japanese
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.auto_turn_ocr_korean
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.auto_turn_ocr_language
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.auto_turn_ocr_latin
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.auto_turn_ocr_reason_model_missing
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.auto_turn_ocr_reason_no_services
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.auto_turn_ocr_reason_unsupported
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.auto_turn_ocr_state_downloading
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.auto_turn_ocr_state_installed
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.auto_turn_ocr_state_missing
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.auto_turn_ocr_state_unavailable
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.auto_turn_text_aware
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.auto_turn_text_aware_max
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.auto_turn_text_aware_min
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.auto_turn_text_aware_per_char
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.common.components.AppSliderDefaults
import snd.komelia.pages.OcrLanguage
import kotlin.math.roundToInt

private fun OcrLanguage.label(): StringResource = when (this) {
    OcrLanguage.JAPANESE -> Res.string.auto_turn_ocr_japanese
    OcrLanguage.KOREAN -> Res.string.auto_turn_ocr_korean
    OcrLanguage.CHINESE -> Res.string.auto_turn_ocr_chinese
    OcrLanguage.LATIN -> Res.string.auto_turn_ocr_latin
}

private fun OcrModelState.label(): StringResource = when (this) {
    OcrModelState.INSTALLED -> Res.string.auto_turn_ocr_state_installed
    OcrModelState.DOWNLOADING -> Res.string.auto_turn_ocr_state_downloading
    OcrModelState.UNAVAILABLE -> Res.string.auto_turn_ocr_state_unavailable
    OcrModelState.MISSING, OcrModelState.UNKNOWN -> Res.string.auto_turn_ocr_state_missing
}

/** Text-aware auto page turn options, shown in the auto page turn overlay below the interval slider. */
@Composable
fun TextAwareAutoTurnSettingsPanel(state: TextAwareAutoTurnState) {
    val recognizer = rememberPageTextRecognizer()
    val settings = state.settings.collectAsState().value
    val statuses = recognizer.modelStates.collectAsState().value
    val servicesAvailable = recognizer.servicesAvailable.collectAsState().value
    LaunchedEffect(recognizer) { recognizer.refresh() }

    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .heightIn(max = 320.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(Res.string.auto_turn_text_aware), modifier = Modifier.weight(1f))
            Switch(settings.enabled, { v -> state.update { it.copy(enabled = v) } })
        }
        if (!settings.enabled) return@Column

        val selectedState = statuses[settings.ocrLanguage]?.state
        val reason = when {
            !recognizer.supported -> Res.string.auto_turn_ocr_reason_unsupported
            !servicesAvailable -> Res.string.auto_turn_ocr_reason_no_services
            selectedState != OcrModelState.INSTALLED -> Res.string.auto_turn_ocr_reason_model_missing
            else -> null
        }
        if (reason != null) {
            Text(stringResource(reason), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        LabeledSlider(
            stringResource(Res.string.auto_turn_text_aware_per_char, settings.perCharMs),
            settings.perCharMs, 0..500, step = 10,
        ) { v -> state.update { it.copy(perCharMs = v) } }
        LabeledSlider(
            stringResource(Res.string.auto_turn_text_aware_min, settings.minSeconds),
            settings.minSeconds, 1..30,
        ) { v -> state.update { it.copy(minSeconds = v, maxSeconds = maxOf(it.maxSeconds, v)) } }
        LabeledSlider(
            stringResource(Res.string.auto_turn_text_aware_max, settings.maxSeconds),
            settings.maxSeconds, 5..120,
        ) { v -> state.update { it.copy(maxSeconds = v, minSeconds = minOf(it.minSeconds, v)) } }

        if (!recognizer.supported) return@Column
        Text(stringResource(Res.string.auto_turn_ocr_language), style = MaterialTheme.typography.titleSmall)
        OcrLanguage.entries.forEach { language ->
            val status = statuses[language] ?: OcrModelStatus(OcrModelState.UNKNOWN)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = settings.ocrLanguage == language,
                    onClick = { state.update { it.copy(ocrLanguage = language) } },
                )
                Text(stringResource(language.label()), modifier = Modifier.weight(1f))
                val progress = status.progressPercent?.takeIf { status.state == OcrModelState.DOWNLOADING }
                Text(
                    stringResource(status.state.label()) + (progress?.let { " $it%" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                )
                if (status.state == OcrModelState.MISSING && servicesAvailable) {
                    TextButton(onClick = { recognizer.download(language) }) {
                        Text(stringResource(Res.string.auto_turn_ocr_download))
                    }
                }
            }
        }
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Int,
    range: IntRange,
    step: Int = 1,
    onChange: (Int) -> Unit,
) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(((it / step).roundToInt() * step).coerceIn(range)) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            colors = AppSliderDefaults.colors(),
        )
    }
}
