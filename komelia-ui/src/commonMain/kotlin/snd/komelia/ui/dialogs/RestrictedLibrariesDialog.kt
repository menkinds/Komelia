package snd.komelia.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.dialog_confirm
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_restricted_library
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_restricted_library_description
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.common.components.CheckboxWithLabel
import snd.komelia.ui.platform.cursorForHand
import snd.komga.client.library.KomgaLibrary

/** Selection of the restricted libraries. Every change is saved at once. */
@Composable
fun RestrictedLibrariesDialog(
    libraries: List<KomgaLibrary>,
    selectedIds: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    AppDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 600.dp),
        header = {
            Text(
                stringResource(Res.string.settings_restricted_library),
                fontSize = 20.sp,
                modifier = Modifier.padding(10.dp)
            )
        },
        content = {
            Column(Modifier.padding(10.dp)) {
                Text(
                    stringResource(Res.string.settings_restricted_library_description),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                libraries.forEach { library ->
                    val id = library.id.value
                    CheckboxWithLabel(
                        checked = id in selectedIds,
                        onCheckedChange = { checked ->
                            onSelectionChange(if (checked) selectedIds + id else selectedIds - id)
                        },
                        label = { Text(library.name) }
                    )
                }
            }
        },
        controlButtons = {
            FlowRow(Modifier.padding(10.dp)) {
                Spacer(Modifier.weight(1f))
                FilledTonalButton(
                    onClick = onDismiss,
                    modifier = Modifier.cursorForHand(),
                ) {
                    Text(stringResource(Res.string.dialog_confirm))
                }
            }
        }
    )
}
