package com.carscan.app

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage

@Composable
private fun Card(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) { content() }
    }
}

@Composable
private fun InfoRow(label: String, value: String, divider: Boolean = true) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
        Text(value, fontSize = 15.sp)
    }
    if (divider) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
}

@Composable
private fun Header(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        modifier = Modifier.padding(start = 14.dp, top = 18.dp, bottom = 6.dp)
    )
}

@Composable
fun StartPage(fav: List<Saved>, rec: List<Saved>, onOpen: (Saved) -> Unit) {
    val colors = listOf(Color(0xFF007AFF), Color(0xFF34C759), Color(0xFFFF9500), Color(0xFFAF52DE), Color(0xFFFF3B30))
    fun label(s: Saved) = s.model.ifBlank { Plate.pretty(s.plate) }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
            Text(
                "Избранное", fontSize = 28.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
            )
        }
        item {
            if (fav.isEmpty()) {
                Text(
                    "Открой машину и нажми закладку внизу, она появится здесь",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp
                )
            } else {
                fav.chunked(4).forEach { row ->
                    Row(Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
                        row.forEach { s ->
                            Column(
                                Modifier.weight(1f).clickable { onOpen(s) },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    Modifier.size(56.dp).clip(RoundedCornerShape(14.dp))
                                        .background(colors[(label(s).hashCode() and 0x7fffffff) % colors.size]),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label(s).take(1), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Text(
                                    label(s), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                        repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
        if (rec.isNotEmpty()) {
            item { Header("Недавние") }
            item {
                Card {
                    rec.forEachIndexed { i, s ->
                        Row(
                            Modifier.fillMaxWidth().clickable { onOpen(s) }.padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (s.plate.isNotEmpty()) Plate.pretty(s.plate) else "Без номера", fontSize = 15.sp)
                            Text(s.model, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
                        }
                        if (i < rec.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun CarPage(tab: Tab, onOpenWeb: (String) -> Unit) {
    val info = if (tab.plate.isNotEmpty()) Plate.parse(tab.plate) else null
    var wiki by remember { mutableStateOf<WikiInfo?>(null) }
    var loading by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }

    LaunchedEffect(tab.model) {
        wiki = null
        if (tab.model.isNotBlank()) {
            loading = true
            wiki = Wiki.summary(tab.model)
            loading = false
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Box(
            Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            val img: Any? = tab.photo ?: wiki?.thumb
            if (img != null) {
                AsyncImage(
                    model = img, contentDescription = null,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Default.DirectionsCar, null, Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            tab.model.ifBlank { "Модель не указана" }, fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 12.dp)
        )

        if (tab.model.isBlank()) {
            Text(
                "Модель пока вводится вручную, начни печатать",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )
            OutlinedTextField(
                value = draft, onValueChange = { draft = it },
                label = { Text("Марка и модель") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Models.suggest(draft).forEach { m ->
                    AssistChip(onClick = { tab.model = m; tab.refreshTitle() }, label = { Text(m) })
                }
            }
            Button(
                onClick = { if (draft.isNotBlank()) { tab.model = draft.trim(); tab.refreshTitle() } },
                modifier = Modifier.padding(top = 8.dp)
            ) { Text("Готово") }
        } else {
            TextButton(onClick = { draft = tab.model; tab.model = ""; tab.refreshTitle() }) { Text("Изменить модель") }
        }

        if (info != null) {
            Header("Номер")
            Card {
                InfoRow("Номер", Plate.pretty(info.plate))
                InfoRow("Регион", info.region + (info.regionName?.let { " · $it" } ?: ""))
                InfoRow("Формат", "Стандартный", divider = false)
            }
        } else if (tab.photo != null) {
            Header("Номер")
            Text(
                "На фото номер не найден. Введи его в строке внизу.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp,
                modifier = Modifier.padding(start = 14.dp)
            )
        }

        if (tab.model.isNotBlank()) {
            Header("Характеристики")
            Card {
                if (loading) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.size(24.dp))
                    }
                } else if (wiki != null) {
                    Text(
                        wiki!!.extract, fontSize = 14.sp, maxLines = 8, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                    wiki!!.page?.let { p ->
                        TextButton(onClick = { onOpenWeb(p) }) { Text("Читать в Википедии") }
                    }
                } else {
                    Text(
                        "В Википедии ничего не нашлось. Попробуй написать название иначе.",
                        fontSize = 14.sp, modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }

        if (info != null) {
            Header("История номера")
            Card {
                Sources.forPlate.forEachIndexed { i, s ->
                    Column(
                        Modifier.fillMaxWidth().clickable { onOpenWeb(s.url(info.plate)) }.padding(vertical = 12.dp)
                    ) {
                        Text(s.title, color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
                        Text(s.hint, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    if (i < Sources.forPlate.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
            Text(
                "Откроется в новой вкладке. На некоторых сайтах полный отчёт платный.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp,
                modifier = Modifier.padding(start = 14.dp, top = 6.dp)
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPage(tab: Tab) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val w = tab.web ?: WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        tab.title = view?.title ?: tab.title
                        tab.url = view?.url ?: tab.url
                        tab.canBack = view?.canGoBack() ?: false
                        tab.canForward = view?.canGoForward() ?: false
                    }

                    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                        tab.canBack = view?.canGoBack() ?: false
                        tab.canForward = view?.canGoForward() ?: false
                    }
                }
                loadUrl(tab.url)
                tab.web = this
            }
            (w.parent as? ViewGroup)?.removeView(w)
            w
        }
    )
}

@Composable
fun Switcher(
    tabs: List<Tab>, cur: Int,
    onPick: (Int) -> Unit, onClose: (Int) -> Unit, onNew: () -> Unit, onDone: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onNew) { Text("Новая") }
            Text("Вкладок: ${tabs.size}", fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onDone) { Text("Готово") }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(tabs) { i, t ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = if (i == cur) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier.height(150.dp).clickable { onPick(i) }
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                when (t.kind) { Kind.START -> "Избранное"; Kind.CAR -> "Машина"; Kind.WEB -> "Сайт" },
                                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Default.Close, "Закрыть",
                                Modifier.size(20.dp).clickable { onClose(i) },
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            t.title.ifBlank { "Вкладка" }, fontWeight = FontWeight.Medium,
                            maxLines = 4, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
