package fr.swiftapp.territorymanager

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import fr.swiftapp.territorymanager.data.Territory
import fr.swiftapp.territorymanager.data.TerritoryDatabase
import fr.swiftapp.territorymanager.settings.getApiUrlAsFlow
import fr.swiftapp.territorymanager.settings.getNameList
import fr.swiftapp.territorymanager.settings.getNameListAsFlow
import fr.swiftapp.territorymanager.settings.updateApiUrl
import fr.swiftapp.territorymanager.settings.updateNamesList
import fr.swiftapp.territorymanager.ui.dialogs.ConfirmationDialog
import fr.swiftapp.territorymanager.ui.dialogs.ViewNamesDialog
import fr.swiftapp.territorymanager.ui.theme.TerritoryManagerTheme
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream

private lateinit var saveFileLauncher: ActivityResultLauncher<Intent>
private lateinit var loadFileLauncher: ActivityResultLauncher<Intent>

private lateinit var versionName: String

class SettingsActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        versionName = packageManager.getPackageInfo(packageName, 0).versionName ?: "Unknown"

        val db = TerritoryDatabase.getDatabase(this)

        val onBackPressedDispatcher = onBackPressedDispatcher
        saveFileLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val uri = result.data?.data
                    if (uri != null) {
                        lifecycleScope.launch {
                            db.territoryDao().exportAll().collect { t ->
                                db.territoryDao().exportAllChanges().collect { tc ->
                                    val names = getNameList(this@SettingsActivity)
                                    val gson =
                                        GsonBuilder().serializeNulls().disableHtmlEscaping()
                                            .create()

                                    val json = JsonObject()
                                    json.addProperty("names", names)
                                    json.add("territories", gson.toJsonTree(t))
                                    json.add("territories_changes", gson.toJsonTree(tc))

                                    contentResolver.openOutputStream(uri)?.use { out ->
                                        out.write(json.toString().toByteArray())
                                    }
                                }
                            }
                        }
                    }
                }
            }

        loadFileLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val uri = result.data?.data
                    if (uri != null) {
                        val stringBuilder = StringBuilder()
                        var inputStream: InputStream? = null

                        try {
                            inputStream = contentResolver.openInputStream(uri)
                            inputStream?.bufferedReader()?.use { reader ->
                                reader.forEachLine {
                                    stringBuilder.append(it)
                                }
                            }

                            val gson =
                                GsonBuilder().serializeNulls().disableHtmlEscaping().create()
                            val json =
                                gson.fromJson(stringBuilder.toString(), JsonObject::class.java)

                            lifecycleScope.launch {
                                updateNamesList(this@SettingsActivity, json.get("names").asString)

                                db.territoryDao().deleteAll()
                                json.get("territories").asJsonArray.forEach {
                                    db.territoryDao()
                                        .insert(gson.fromJson(it, Territory::class.java))
                                }
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        } finally {
                            inputStream?.close()
                        }
                    }
                }
            }

        setContent {
            val scrollBehavior =
                TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

            TerritoryManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Scaffold(
                        topBar = {
                            LargeTopAppBar(
                                navigationIcon = {
                                    IconButton(onClick = { onBackPressedDispatcher.onBackPressed() }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = stringResource(R.string.back)
                                        )
                                    }
                                },
                                title = {
                                    Text(text = stringResource(R.string.settings))
                                },
                                scrollBehavior = scrollBehavior
                            )
                        },
                        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                    ) {
                        SettingsItems(it)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsItems(padding: PaddingValues) {
    val context = LocalContext.current
    var openDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val names =
        getNameListAsFlow(context).collectAsState(initial = "")
            .value?.split(',')?.filter { it.isNotBlank() }?.sortedBy { it }
    val updateNames: (index: Int) -> Unit = { index ->
        coroutineScope.launch {
            val newNames = names?.filterIndexed { i, _ -> i != index } ?: emptyList()
            updateNamesList(context, newNames.joinToString(","))
        }
    }

    val apiUrl = getApiUrlAsFlow(context).collectAsState(initial = "")
    val updateApiUrl: (url: String) -> Unit = { url ->
        coroutineScope.launch {
            updateApiUrl(context, url)
        }
    }

    if (names != null) {
        ViewNamesDialog(
            isOpen = openDialog,
            names = names,
            close = { openDialog = false },
            updateNames = { i -> updateNames(i) }
        )
    }

    var confirmVisible by remember { mutableStateOf(false) }
    if (confirmVisible) {
        ConfirmationDialog(
            title = stringResource(R.string.import_backup),
            confirmButtonColor = MaterialTheme.colorScheme.primary,
            confirmButtonText = stringResource(R.string.confirm),

            onConfirm = {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                }

                loadFileLauncher.launch(intent)
                confirmVisible = false
            },
            onCancel = { confirmVisible = false }
        ) {
            Text(stringResource(R.string.import_warning))
        }
    }

    var editUrlVisible by remember { mutableStateOf(false) }
    if (editUrlVisible) {
        var url by remember { mutableStateOf(apiUrl.value ?: "") }

        ConfirmationDialog(
            title = stringResource(R.string.api_url),
            confirmButtonColor = MaterialTheme.colorScheme.primary,
            confirmButtonText = stringResource(R.string.confirm),
            canConfirm = url.isEmpty() || Patterns.WEB_URL.matcher(url).matches(),
            onConfirm = {
                updateApiUrl(url)
                editUrlVisible = false
            },
            onCancel = { editUrlVisible = false }
        ) {
            Column {
                Text(stringResource(R.string.api_url_info))
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Url") }
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(10.dp, 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth(),
            onClick = { openDialog = true }
        ) {
            Column(Modifier.padding(15.dp)) {
                Text(
                    text = stringResource(R.string.publishers),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = stringResource(R.string.publishers_info),
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 20.dp, 0.dp, 0.dp),
            onClick = { editUrlVisible = true }
        ) {
            Column(Modifier.padding(15.dp)) {
                Text(
                    text = stringResource(R.string.api_url),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = if (apiUrl.value?.isBlank() == true) stringResource(R.string.no_api_url) else "Url: ${apiUrl.value}",
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 20.dp, 0.dp, 0.dp),
            onClick = {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                    putExtra(Intent.EXTRA_TITLE, "backup_territory_manager.json")
                }

                saveFileLauncher.launch(intent)
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.export),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = stringResource(R.string.export_info),
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(modifier = Modifier.width(15.dp))
                Icon(
                    painter = painterResource(id = R.drawable.rounded_arrow_circle_up_24),
                    contentDescription = stringResource(id = R.string.export_info),
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 20.dp, 0.dp, 0.dp),
            onClick = {
                confirmVisible = true
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.import_btn),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = stringResource(R.string.import_btn_info),
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(modifier = Modifier.width(15.dp))
                Icon(
                    painter = painterResource(id = R.drawable.rounded_arrow_circle_down_24),
                    contentDescription = stringResource(R.string.import_btn_info),
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Icon(
            painter = painterResource(id = R.drawable.github_mark),
            contentDescription = "GitHub logo",
            modifier = Modifier.size(50.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(buildAnnotatedString {
                append(stringResource(R.string.project_github_info))
                withLink(
                    LinkAnnotation.Url(
                        url = "https://github.com/Swiftapp-hub/Territory-Manager",
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    )
                ) {
                    append("GitHub")
                }
            })
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(text = "Version $versionName", color = MaterialTheme.colorScheme.outline)
    }
}
