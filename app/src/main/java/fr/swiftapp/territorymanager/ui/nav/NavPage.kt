package fr.swiftapp.territorymanager.ui.nav

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import androidx.navigation.navigation
import fr.swiftapp.territorymanager.ChangesActivity
import fr.swiftapp.territorymanager.R
import fr.swiftapp.territorymanager.SettingsActivity
import fr.swiftapp.territorymanager.data.TerritoryDatabase
import fr.swiftapp.territorymanager.ui.pages.AddTerritoryPage
import fr.swiftapp.territorymanager.ui.pages.EditTerritory
import fr.swiftapp.territorymanager.ui.pages.TerritoriesPage

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavPage() {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val parentRouteName = navBackStackEntry.value?.destination?.route

    val context = LocalContext.current

    val db = TerritoryDatabase.getDatabase(context)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (parentRouteName != null) {
                        Text(
                            text = when (parentRouteName) {
                                "Home" -> stringResource(R.string.territories)
                                "Changes" -> stringResource(R.string.changes)
                                "AddTerritory" -> stringResource(R.string.add_territory)
                                else -> {
                                    if (parentRouteName.contains("EditTerritory")) stringResource(R.string.details)
                                    else stringResource(R.string.loading)
                                }
                            },
                        )
                    }
                },
                navigationIcon = {
                    if (parentRouteName != null) {
                        AnimatedVisibility(
                            visible = (parentRouteName == "AddTerritory") || (parentRouteName.contains(
                                "EditTerritory"
                            )),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back)
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        context.startActivity(
                            Intent(context, ChangesActivity::class.java),
                            null
                        )
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.rounded_history_24),
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                    IconButton(onClick = {
                        context.startActivity(
                            Intent(context, SettingsActivity::class.java),
                            null
                        )
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = parentRouteName == "Home",
                enter = fadeIn(initialAlpha = 0f),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate("AddTerritory", navOptions {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }

                            launchSingleTop = true
                            restoreState = true
                        })
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_territory)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.add_territory), fontSize = 16.sp)
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) {
        NavHost(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            navController = navController,
            startDestination = "Territories"
        ) {
            navigation(startDestination = "Home", route = "Territories") {
                composable("Home", deepLinks = listOf(NavDeepLink("deeplink://home"))) {
                    TerritoriesPage(database = db, navController = navController)
                }
                composable(
                    "AddTerritory",
                    deepLinks = listOf(NavDeepLink("deeplink://addTerritory"))
                ) {
                    AddTerritoryPage(database = db, navController = navController)
                }
                composable(
                    "EditTerritory/{territoryId}",
                    arguments = listOf(navArgument("territoryId") { type = NavType.IntType }),
                    deepLinks = listOf(NavDeepLink("deeplink://editTerritory/{territoryId}"))
                ) {
                    EditTerritory(
                        database = db,
                        navController = navController,
                        it.arguments?.getInt("territoryId")
                    )
                }
            }
        }
    }
}