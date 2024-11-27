package fr.swiftapp.territorymanager.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.swiftapp.territorymanager.R
import fr.swiftapp.territorymanager.settings.getNameList
import fr.swiftapp.territorymanager.settings.updateNamesList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogName(isOpen: Boolean, close: (name: String?) -> Unit) {
    if (isOpen) {
        var name by remember { mutableStateOf("") }
        val context = LocalContext.current
        val names = mutableListOf<String>()
        val namesDropDown = remember { mutableStateListOf<String>() }

        val coroutineScope = rememberCoroutineScope()
        val getAll: () -> Unit = {
            coroutineScope.launch {
                val data = getNameList(context)
                if (data != "") {
                    names.addAll(data.split(','))
                    namesDropDown.addAll(names)
                }
            }
        }
        getAll()

        val focusRequester = remember { FocusRequester() }
        val keyboard = LocalSoftwareKeyboardController.current

        LaunchedEffect(focusRequester) {
            focusRequester.requestFocus()
            delay(100)
            keyboard?.show()
        }

        val updateNames: () -> Unit = {
            coroutineScope.launch {
                updateNamesList(context, names.joinToString(","))
            }
        }

        BasicAlertDialog(
            onDismissRequest = {
                focusRequester.freeFocus()
                close(null)
            }
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            namesDropDown.clear()
                            names.forEach {
                                if (it.lowercase().contains(name.lowercase()))
                                    namesDropDown.add(it)
                            }

                            name = it
                        },
                        label = { Text(stringResource(R.string.name_of_publisher)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                    LazyColumn(
                        modifier = Modifier
                            .heightIn(0.dp, 200.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                MaterialTheme.shapes.medium
                            )
                    ) {
                        item {
                            DropdownMenuItem(
                                onClick = {
                                    if (name.isNotBlank() && !names.any {
                                            it.equals(
                                                name,
                                                ignoreCase = true
                                            )
                                        }) {
                                        names.add(name)
                                        updateNames()
                                        focusRequester.freeFocus()
                                        close(name)
                                    }
                                },
                                text = {
                                    Text(text = stringResource(R.string.save_and_assign))
                                },
                                enabled = name.isNotBlank() && !names.any {
                                    it.equals(
                                        name,
                                        ignoreCase = true
                                    )
                                }
                            )
                        }
                        items(namesDropDown) {
                            DropdownMenuItem(
                                onClick = {
                                    name = it
                                },
                                text = {
                                    Text(text = it)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        TextButton(
                            onClick = {
                                focusRequester.freeFocus()
                                close(null)
                            }
                        ) {
                            Text(stringResource(id = R.string.cancel))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                focusRequester.freeFocus()
                                close(name)
                            },
                            enabled = name.isNotBlank()
                        ) {
                            Text(stringResource(R.string.assign))
                        }
                    }
                }
            }
        }
    }
}