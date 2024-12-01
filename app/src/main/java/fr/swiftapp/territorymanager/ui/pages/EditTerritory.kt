package fr.swiftapp.territorymanager.ui.pages

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import fr.swiftapp.territorymanager.R
import fr.swiftapp.territorymanager.data.Territory
import fr.swiftapp.territorymanager.data.TerritoryDatabase
import fr.swiftapp.territorymanager.data.api.ApiManager
import fr.swiftapp.territorymanager.ui.components.MaskField
import fr.swiftapp.territorymanager.ui.components.MaterialButtonToggleGroup
import fr.swiftapp.territorymanager.ui.dialogs.ConfirmationDialog
import fr.swiftapp.territorymanager.utils.convertDate
import fr.swiftapp.territorymanager.utils.reverseDate
import kotlinx.coroutines.launch

@Composable
fun EditTerritory(database: TerritoryDatabase, navController: NavHostController, id: Int?) {
    val territory =
        id?.let { database.territoryDao().getById(it).collectAsState(initial = null).value }

    var isShops by remember {
        mutableStateOf(false)
    }
    var number by remember {
        mutableStateOf("")
    }
    var name by remember {
        mutableStateOf("")
    }
    var givenDate by remember {
        mutableStateOf("")
    }
    var returnDate by remember {
        mutableStateOf("")
    }
    var givenName by remember {
        mutableStateOf("")
    }

    LaunchedEffect(territory) {
        number = territory?.number.toString()
        name = territory?.name.toString()
        givenDate = reverseDate(territory?.givenDate.toString())
        returnDate = reverseDate(territory?.returnDate.toString())
        givenName = reverseDate(territory?.givenName.toString())
        isShops = territory?.isShops == true
    }

    var error by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current
    val apiManager = remember { ApiManager(context, database) }

    val coroutineScope = rememberCoroutineScope()
    val updatedItem: () -> Unit = {
        val newTerritory = territory?.let {
            Territory(
                id = it.id,
                number = number.toInt(),
                name = name,
                givenDate = convertDate(givenDate),
                returnDate = convertDate(returnDate),
                isAvailable = returnDate.isNotBlank(),
                givenName = givenName,
                isShops = isShops
            )
        }

        coroutineScope.launch {
            if (newTerritory != null) {
                database.territoryDao().update(newTerritory)
                apiManager.uploadChanges {
                    Log.d("MY", "Upload finished $it")
                }
            }
        }
    }

    val deleteItem: () -> Unit = {
        coroutineScope.launch {
            if (territory != null) {
                database.territoryDao().delete(territory)
                apiManager.uploadChanges {
                    Log.d("MY", "Upload finished $it")
                }
            }
        }
    }

    var showDialog by remember {
        mutableStateOf(false)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(10.dp, 0.dp)
    ) {
        MaterialButtonToggleGroup(
            items = listOf(
                stringResource(id = R.string.territories),
                stringResource(R.string.shops)
            ),
            value = if (isShops) 1 else 0,
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 10.dp),
            onClick = { isShops = it == 1 }
        )

        OutlinedTextField(
            value = number,
            singleLine = true,
            onValueChange = { text -> number = text },
            label = { Text(stringResource(id = R.string.number)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            singleLine = true,
            onValueChange = { text -> name = text },
            label = { Text(stringResource(R.string.territory_name)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                autoCorrectEnabled = true,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        MaskField(
            date = givenDate,
            text = stringResource(R.string.release_date_dd_mm_yy),
            mask = "xx/xx/xx",
            maskNumber = 'x',
            onDateChanged = { givenDate = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        MaskField(
            date = returnDate,
            text = stringResource(R.string.return_date_dd_mm_yy),
            mask = "xx/xx/xx",
            maskNumber = 'x',
            onDateChanged = { returnDate = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = givenName,
            singleLine = true,
            onValueChange = { text -> givenName = text },
            label = { Text(stringResource(R.string.name_of_publisher)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                autoCorrectEnabled = true
            )
        )

        Spacer(modifier = Modifier.height(15.dp))

        Button(onClick = {
            if (number.toIntOrNull() == null || number.isBlank() || name.isBlank() || (givenDate.length != 6 && givenDate != "") || (returnDate.length != 6 && returnDate != ""))
                error = true
            else if (givenName.isBlank() && givenDate.isNotBlank()) error = true
            else {
                updatedItem()
                navController.popBackStack()
            }
        }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(5.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.rounded_save_24),
                    contentDescription = stringResource(R.string.save)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = stringResource(R.string.save))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { showDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.delete),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        if (error)
            Text(
                text = stringResource(R.string.check_values),
                modifier = Modifier.padding(0.dp, 5.dp),
                color = Color.Red
            )

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (showDialog)
        ConfirmationDialog(
            title = stringResource(R.string.deletion),
            confirmButtonColor = MaterialTheme.colorScheme.errorContainer,
            confirmButtonTextColor = MaterialTheme.colorScheme.onErrorContainer,
            confirmButtonText = stringResource(R.string.delete),
            onConfirm = {
                showDialog = false
                deleteItem()
                navController.popBackStack()
            },
            onCancel = {
                showDialog = false
            }
        ) {
            Text(text = stringResource(R.string.deletion_confirm))
        }
}
