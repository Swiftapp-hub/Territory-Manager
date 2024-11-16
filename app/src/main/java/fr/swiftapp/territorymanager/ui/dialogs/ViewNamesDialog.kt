package fr.swiftapp.territorymanager.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import fr.swiftapp.territorymanager.R

@Composable
fun ViewNamesDialog(
    isOpen: Boolean,
    names: List<String>,
    close: () -> Unit,
    updateNames: (index: Int) -> Unit
) {
    if (isOpen)
        Dialog(
            onDismissRequest = {
                close()
            }
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = AlertDialogDefaults.TonalElevation,
                modifier = Modifier.height(400.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween
                ) {
                    if (names.isNotEmpty())
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.verticalScroll(rememberScrollState())
                            ) {
                                names.forEachIndexed { index, item ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = item,
                                            fontSize = 16.sp,
                                            overflow = TextOverflow.Ellipsis,
                                            maxLines = 1,
                                            modifier = Modifier
                                                .padding(10.dp, 0.dp, 0.dp, 0.dp)
                                                .weight(1f)
                                        )
                                        TextButton(
                                            onClick = {
                                                updateNames(index)
                                            }
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.rounded_delete_24),
                                                contentDescription = stringResource(R.string.delete)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(
                                Modifier
                                    .fillMaxWidth()
                                    .height(15.dp)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.surfaceColorAtElevation(
                                                    AlertDialogDefaults.TonalElevation
                                                ),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            Spacer(
                                Modifier
                                    .fillMaxWidth()
                                    .height(15.dp)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.surfaceColorAtElevation(
                                                    AlertDialogDefaults.TonalElevation
                                                )
                                            )
                                        )
                                    )
                                    .align(Alignment.BottomCenter)
                            )
                        }
                    else
                        Text(
                            text = stringResource(R.string.no_names),
                            modifier = Modifier.padding(10.dp)
                        )

                    Spacer(modifier = Modifier.height(15.dp))
                    Row {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        TextButton(
                            onClick = {
                                close()
                            },
                        ) {
                            Text(stringResource(R.string.close))
                        }
                    }
                }
            }
        }
}