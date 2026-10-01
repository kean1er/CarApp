package com.carscan.app

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun App() {
    val ctx = LocalContext.current
    val store = remember { Store(ctx) }
    val scope = rememberCoroutineScope()

    val tabs = remember { mutableStateListOf(Tab(Kind.START)) }
    var cur by remember { mutableIntStateOf(0) }
    var switcher by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<Uri?>(null) }
    val favorites = remember { mutableStateListOf<Saved>().apply { addAll(store.read("fav")) } }
    val recents = remember { mutableStateListOf<Saved>().apply { addAll(store.read("rec")) } }

    if (cur > tabs.lastIndex) cur = tabs.lastIndex
    val tab = tabs[cur]

    fun open(t: Tab) {
        tabs.add(t)
        cur = tabs.lastIndex
        switcher = false
    }

    fun close(i: Int) {
        tabs.removeAt(i)
        if (tabs.isEmpty()) tabs.add(Tab(Kind.START))
        if (cur > tabs.lastIndex) cur = tabs.lastIndex
        else if (i < cur) cur -= 1
    }

    fun openCar(plate: String, model: String, photo: Uri?) {
        val t = Tab(Kind.CAR)
        t.plate = plate
        t.model = model
        t.photo = photo
        t.refreshTitle()
        open(t)
        if (plate.isNotEmpty() || model.isNotEmpty()) {
            recents.removeAll { it.plate == plate && it.model == model }
            recents.add(0, Saved(plate, model))
            while (recents.size > 8) recents.removeAt(recents.lastIndex)
            store.write("rec", recents)
        }
    }

    fun openWeb(url: String) {
        val t = Tab(Kind.WEB)
        t.url = url
        open(t)
    }

    fun submit(text: String) {
        val q = text.trim()
        if (q.isEmpty()) return
        val p = Plate.parse(q)
        when {
            p != null -> openCar(p.plate, "", null)
            q.startsWith("http") -> openWeb(q)
            q.contains('.') && !q.contains(' ') -> openWeb("https://$q")
            else -> openCar("", q, null)
        }
        input = ""
    }

    fun handlePhoto(uri: Uri) {
        scope.launch {
            busy = true
            val plate = PlateReader.read(ctx, uri)
            busy = false
            openCar(plate ?: "", "", uri)
        }
    }

    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) handlePhoto(uri)
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) pending?.let { handlePhoto(it) }
    }

    fun launchCamera() {
        val f = File(ctx.cacheDir, "shot_${System.currentTimeMillis()}.jpg")
        val u = FileProvider.getUriForFile(ctx, "${ctx.packageName}.files", f)
        pending = u
        camera.launch(u)
    }

    fun goBack() {
        if (switcher) {
            switcher = false
            return
        }
        if (tab.kind == Kind.WEB && tab.canBack) {
            tab.web?.goBack()
            return
        }
        close(cur)
    }

    fun share() {
        val text = when (tab.kind) {
            Kind.WEB -> tab.url
            Kind.CAR -> tab.title
            else -> ""
        }
        if (text.isNotBlank()) {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            ctx.startActivity(Intent.createChooser(send, null))
        }
    }

    val isFav = tab.kind == Kind.CAR && favorites.any { it.plate == tab.plate && it.model == tab.model }

    fun toggleFav() {
        if (tab.kind != Kind.CAR) return
        if (isFav) favorites.removeAll { it.plate == tab.plate && it.model == tab.model }
        else favorites.add(Saved(tab.plate, tab.model))
        store.write("fav", favorites)
    }

    BackHandler(enabled = switcher || tab.kind != Kind.START || tabs.size > 1) { goBack() }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                key(tab.id) {
                    when (tab.kind) {
                        Kind.START -> StartPage(favorites, recents) { openCar(it.plate, it.model, null) }
                        Kind.CAR -> CarPage(tab) { openWeb(it) }
                        Kind.WEB -> WebPage(tab)
                    }
                }
                if (busy) CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
            BottomBar(
                input = input,
                onInput = { input = it },
                onGo = { submit(it) },
                onCamera = { launchCamera() },
                onGallery = { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                canForward = tab.kind == Kind.WEB && tab.canForward,
                onBack = { goBack() },
                onForward = { tab.web?.goForward() },
                onShare = { share() },
                marked = isFav,
                onBookmark = { toggleFav() },
                tabCount = tabs.size,
                onTabs = { switcher = true }
            )
        }
        if (switcher) {
            Switcher(
                tabs = tabs, cur = cur,
                onPick = { cur = it; switcher = false },
                onClose = { close(it) },
                onNew = { open(Tab(Kind.START)) },
                onDone = { switcher = false }
            )
        }
    }
}

@Composable
private fun BottomBar(
    input: String, onInput: (String) -> Unit, onGo: (String) -> Unit,
    onCamera: () -> Unit, onGallery: () -> Unit,
    canForward: Boolean, onBack: () -> Unit, onForward: () -> Unit,
    onShare: () -> Unit, marked: Boolean, onBookmark: () -> Unit,
    tabCount: Int, onTabs: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    var menu by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth().background(cs.background)) {
        TextField(
            value = input,
            onValueChange = onInput,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            placeholder = { Text("Номер или фото") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = cs.surfaceVariant,
                unfocusedContainerColor = cs.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { onGo(input) }),
            trailingIcon = {
                Box {
                    IconButton(onClick = { menu = true }) {
                        Icon(Icons.Default.CameraAlt, "Фото", tint = cs.primary)
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text("Снять") }, onClick = { menu = false; onCamera() })
                        DropdownMenuItem(text = { Text("Из галереи") }, onClick = { menu = false; onGallery() })
                    }
                }
            }
        )
        Row(
            Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBackIosNew, "Назад", tint = cs.primary) }
            IconButton(onClick = onForward, enabled = canForward) {
                Icon(
                    Icons.Default.ArrowForwardIos, "Вперёд",
                    tint = if (canForward) cs.primary else cs.onSurfaceVariant
                )
            }
            IconButton(onClick = onShare) { Icon(Icons.Default.Share, "Поделиться", tint = cs.primary) }
            IconButton(onClick = onBookmark) {
                Icon(
                    if (marked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    "В избранное", tint = cs.primary
                )
            }
            IconButton(onClick = onTabs) {
                Box(
                    Modifier.size(24.dp).border(1.5.dp, cs.primary, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$tabCount", fontSize = 12.sp, color = cs.primary)
                }
            }
        }
    }
}
