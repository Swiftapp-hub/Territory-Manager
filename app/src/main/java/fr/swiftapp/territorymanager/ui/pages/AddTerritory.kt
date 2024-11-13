package fr.swiftapp.territorymanager.ui.pages

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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import fr.swiftapp.territorymanager.ui.components.MaskField
import fr.swiftapp.territorymanager.ui.components.MaterialButtonToggleGroup
import fr.swiftapp.territorymanager.utils.convertDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AddTerritoryPage(database: TerritoryDatabase, navController: NavHostController) {
    var isShops by remember {
        mutableStateOf(false)
    }
    var number by remember {
        mutableStateOf("")
    }
    var name by remember {
        mutableStateOf("")
    }
    var date by remember {
        mutableStateOf("")
    }

    var error by remember {
        mutableStateOf(false)
    }

    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
        delay(100)
        keyboard?.show()
    }

    val coroutineScope = rememberCoroutineScope()
    val insertItem: () -> Unit = {
        val territory = Territory(
            number = number.toInt(),
            name = name,
            givenDate = "",
            returnDate = convertDate(date),
            isAvailable = true,
            givenName = "",
            isShops = isShops
        )

        coroutineScope.launch {
            database.territoryDao().insert(territory)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(10.dp, 0.dp)
    ) {
        MaterialButtonToggleGroup(
            items = listOf(stringResource(id = R.string.territories), stringResource(R.string.shops)),
            value = if (isShops) 1 else 0,
            modifier = Modifier.fillMaxWidth().padding(0.dp, 10.dp),
            onClick = { isShops = it == 1 }
        )

        OutlinedTextField(
            value = number,
            singleLine = true,
            onValueChange = { text -> number = text },
            label = { Text(stringResource(R.string.number)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
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
            date = date,
            text = stringResource(R.string.date_dd_mm_yy),
            mask = "xx/xx/xx",
            maskNumber = 'x',
            onDateChanged = { date = it }
        )

        Spacer(modifier = Modifier.height(15.dp))

        Button(onClick = {
            if (number.toIntOrNull() == null || number.isBlank() || name.isBlank() || date.length != 6)
                error = true
            else {
                insertItem()
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

        if (error)
            Text(
                text = stringResource(R.string.check_values),
                modifier = Modifier.padding(0.dp, 5.dp),
                color = Color.Red
            )

        Spacer(modifier = Modifier.height(20.dp))
    }
}