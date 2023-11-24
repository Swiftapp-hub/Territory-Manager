package fr.swiftapp.territorymanager.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.navOptions
import fr.swiftapp.territorymanager.R
import fr.swiftapp.territorymanager.data.Territory
import fr.swiftapp.territorymanager.data.TerritoryDatabase
import fr.swiftapp.territorymanager.settings.getNameListAsFlow
import fr.swiftapp.territorymanager.ui.components.ChipWithSubItems
import fr.swiftapp.territorymanager.ui.components.MaterialButtonToggleGroup
import fr.swiftapp.territorymanager.ui.lists.TerritoryListItem
import kotlinx.coroutines.launch

@Composable
fun TerritoriesPage(database: TerritoryDatabase, navController: NavController) {
    var isShops by rememberSaveable {
        mutableStateOf(false)
    }
    var status by rememberSaveable {
        mutableIntStateOf(0)
    }
    var publisher by remember {
        mutableIntStateOf(0)
    }
    var publisherBkp by rememberSaveable {
        mutableStateOf("")
    }

    val names = getNameListAsFlow(LocalContext.current).collectAsState(initial = "")

    val territories = database.territoryDao().getAll(if (isShops) 1 else 0).collectAsState(initial = emptyList())
    val finalList = remember {
        mutableStateListOf<Territory>()
    }

    val coroutineScope = rememberCoroutineScope()
    val updateItem: (territory: Territory) -> Unit = { territory ->
        coroutineScope.launch {
            database.territoryDao().update(territory)
        }
    }

    val scrollState = rememberLazyListState()

    LaunchedEffect(Unit) {
        scrollState.animateScrollToItem(0)
    }

    LaunchedEffect(status, publisher, territories.value, Unit) {
        finalList.clear()
        val tmpTerritories: ArrayList<Territory> = ArrayList()

        when (status) {
            0 -> {
                tmpTerritories.addAll(territories.value)
            }

            1 -> {
                tmpTerritories.addAll(territories.value.filter { it.isAvailable }.sortedBy { it.returnDate })
                publisher = 0
            }

            2 -> {
                tmpTerritories.addAll(territories.value.filter { !it.isAvailable }.sortedBy { it.givenDate })
            }
        }

        if (publisher > 0) {
            finalList.addAll(tmpTerritories.filter { it.givenName == names.value?.split(',')?.get(publisher - 1) && !it.isAvailable })
        } else {
            finalList.addAll(tmpTerritories)
        }
    }

    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                if (publisher > 0)
                    publisherBkp = names.value?.split(',')?.get(publisher - 1) ?: ""
                publisher = 0
            } else if (event == Lifecycle.Event.ON_RESUME) {
                if (publisherBkp.isNotEmpty()) {
                    val i = names.value?.split(',')?.indexOf(publisherBkp) ?: 0
                    if (i >= 0) publisher = i + 1
                }
            }
        }
        owner.lifecycle.addObserver(observer)

        onDispose {
            owner.lifecycle.removeObserver(observer)
        }
    }

    Column {
        MaterialButtonToggleGroup(
            items = listOf(stringResource(id = R.string.territories), stringResource(R.string.shops)),
            value = if (isShops) 1 else 0,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp, 4.dp),
            onClick = { isShops = it == 1 }
        )

        Row(
            modifier = Modifier
                .padding(10.dp, 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ChipWithSubItems(
                chipLabel = "Status : ",
                chipItems = listOf(stringResource(R.string.all), stringResource(id = R.string.available), stringResource(R.string.in_progress)),
                onClick = { status = it },
                value = status
            )

            if (status != 1)
                names.value?.split(',')?.let { listOf("Tous les proclamateurs", *it.toTypedArray()) }?.let { list ->
                    ChipWithSubItems(
                        chipLabel = "",
                        chipItems = list,
                        onClick = { publisher = it },
                        value = publisher
                    )
                }
        }

        if (finalList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.no_territories),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Box {
                LazyColumn(modifier = Modifier.fillMaxSize(), state = scrollState) {
                    item {
                        Spacer(modifier = Modifier.height(15.dp))
                    }

                    items(finalList) { territory ->
                        key(territory.id) {
                            TerritoryListItem(
                                territory,
                                true,
                                { updateItem(it) },
                                {
                                    navController.navigate("EditTerritory/${territory.id}", navOptions {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }

                                        launchSingleTop = true
                                        restoreState = true
                                    })
                                }
                            )
                        }
                    }

                    item {
                        Text(
                            text = "${finalList.size} ${stringResource(id = R.string.territories)}",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(90.dp))
                    }
                }

                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(25.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    Color.Transparent
                                )
                            )
                        )
                )

                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(25.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .align(Alignment.BottomCenter)
                )
            }
        }
    }
}