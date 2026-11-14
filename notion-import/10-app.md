# app

源码快照 commit：`9441209aadafb1f33c3694643eed5880fa715177`

以下是该目录在该 commit 的完整文本，不是片段。二进制文件只出现在索引里。

## `app/build.gradle.kts`

sha256 `d47712ca382cc002bd37cbae1c80aefcfc925dac184775695363650e48d42cdb`

```kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.movielab"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.movielab"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    lint {
        lintConfig = file("lint.xml")
        abortOnError = true
        warningsAsErrors = false
        checkReleaseBuilds = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.material.icons.core)
    implementation(libs.activity.compose)
    implementation(libs.adaptive)
    implementation(libs.adaptive.layout)
    implementation(libs.adaptive.navigation3)
    implementation(libs.navigation3.runtime)
    implementation(libs.navigation3.ui)
    implementation(libs.serialization.core)
    debugImplementation(libs.compose.ui.tooling)
    testImplementation(libs.junit)
    lintChecks(project(":checks"))
}
```

## `app/lint.xml`

sha256 `7c134094bdd751481088ea94eed8df6c038f9ada8977723cac6281fd89fe58ee`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<lint>
    <!-- lifecycle-runtime ships a lint jar this AGP cannot load. Ignoring the meta-warning does not enable those checks. -->
    <issue id="ObsoleteLintCustomCheck" severity="ignore" />
    <!-- Lab rollout: registry default is warning; this module treats the mapped rules as errors. -->
    <issue id="AdaptiveScreenOrientationLock" severity="error" />
    <issue id="AdaptiveConfigChangesOptOut" severity="error" />
    <issue id="UnsaveableScrollRemember" severity="error" />
    <issue id="AdaptivePaneScaffoldBanned" severity="error" />
    <issue id="MissingEnableEdgeToEdge" severity="error" />
</lint>
```

## `app/src/main/AndroidManifest.xml`

sha256 `a561014e208ce1a561856c7035e00ba9854f22d0717bd8da53689c8f3221364c`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:allowBackup="true"
        android:icon="@drawable/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.MovieLab">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

## `app/src/main/java/com/example/movielab/MainActivity.kt`

sha256 `a9662fd7aa462639c476c7e97765f360de2ec45b7971ec6b68fc9daeab121b8e`

```kt
package com.example.movielab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.movielab.ui.MovieApp
import com.example.movielab.ui.MovieLabTheme

/**
 * Hand-written reference activity, not a model sample.
 * enableEdgeToEdge from ComponentActivity runs before super.onCreate and before setContent.
 * The manifest does not set screenOrientation or configChanges.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MovieLabTheme {
                MovieApp()
            }
        }
    }
}
```

## `app/src/main/java/com/example/movielab/data/Catalog.kt`

sha256 `c17750622572eb77759380ba13125cad9e934d1492a0e0c7cb807f4704b42237`

```kt
package com.example.movielab.data

/**
 * Local catalog. No network, no API key. Titles and synopses are fictional.
 * Posters are drawn in the UI and labeled as placeholders.
 */
data class Movie(
    val id: String,
    val title: String,
    val year: Int,
    val runtimeMinutes: Int,
    val genre: String,
    val synopsis: String,
    val hue: Int,
)

enum class CatalogMode {
    Content,
    Loading,
    Empty,
    Error,
}

data class CatalogPresentation(
    val mode: CatalogMode,
    val query: String,
    val movies: List<Movie>,
    val message: String?,
)

fun presentCatalog(mode: CatalogMode, query: String, all: List<Movie>): CatalogPresentation {
    val trimmed = query.trim()
    return when (mode) {
        CatalogMode.Loading -> CatalogPresentation(
            mode = mode,
            query = trimmed,
            movies = emptyList(),
            message = "正在读取本地片单。没有网络请求。",
        )
        CatalogMode.Empty -> CatalogPresentation(
            mode = mode,
            query = trimmed,
            movies = emptyList(),
            message = "片单是空的。这是本地模拟状态，没有网络请求。",
        )
        CatalogMode.Error -> CatalogPresentation(
            mode = mode,
            query = trimmed,
            movies = emptyList(),
            message = "片单读取失败。这是本地模拟状态，没有网络请求。",
        )
        CatalogMode.Content -> {
            val filtered = if (trimmed.isEmpty()) {
                all
            } else {
                all.filter { movie ->
                    movie.title.contains(trimmed, ignoreCase = true) ||
                        movie.genre.contains(trimmed, ignoreCase = true) ||
                        movie.synopsis.contains(trimmed, ignoreCase = true)
                }
            }
            CatalogPresentation(
                mode = mode,
                query = trimmed,
                movies = filtered,
                message = if (filtered.isEmpty()) "没有匹配「$trimmed」的电影。" else null,
            )
        }
    }
}

object Movies {
    val all: List<Movie> = listOf(
        movie("m01", "雾港夜车", 1998, 104, "公路", "一辆只在雾里出现的夜班车，把没写完的信送到对岸。", 205),
        movie("m02", "纸月亮信差", 2004, 96, "剧情", "小镇信差用纸折的月亮给失眠的人标出明天的路。", 42),
        movie("m03", "盐湖来信", 1987, 118, "家庭", "干涸的盐湖边，一封迟到的家书改变了晒盐的季节。", 188),
        movie("m04", "静音教室", 2012, 91, "校园", "学校广播坏掉的一周，学生们学会用纸条上课。", 220),
        movie("m05", "北纬灯塔", 1976, 128, "冒险", "守塔人在极夜里用备用灯泡拼出回家的方向。", 210),
        movie("m06", "铜钥匙", 2001, 102, "悬疑", "旧公寓的铜钥匙能打开邻居不曾承认的储藏间。", 28),
        movie("m07", "雨季邮差", 1994, 99, "剧情", "邮差把被雨洇开的地址一张张重新抄清。", 200),
        movie("m08", "玻璃温室", 2016, 108, "科幻", "温室里的植物会记住参观者离开时说的最后一句话。", 140),
        movie("m09", "旧电台", 1969, 112, "音乐", "海边电台用别人丢掉的磁带播出一整季的天气。", 12),
        movie("m10", "深蓝站台", 2008, 101, "公路", "末班地铁的站台只在时刻表写错的夜里开放。", 230),
        movie("m11", "枫桥短笺", 1983, 94, "爱情", "桥栏上的短笺被风按顺序吹给两个互不相识的人。", 18),
        movie("m12", "看不见的合唱", 2019, 110, "音乐", "社区合唱团在停电的晚上靠脚步声找齐拍子。", 280),
        movie("m13", "冬眠旅店", 2006, 115, "剧情", "山路封闭的周末，旅店把每个房间的钟拨慢一小时。", 205),
        movie("m14", "橙巷", 1991, 97, "喜剧", "一条只卖橙子的小巷，店主们用果皮交换新闻。", 32),
        movie("m15", "星图修补匠", 2021, 119, "科幻", "修补匠把破损的星图缝回孩子们的天花板。", 250),
        movie("m16", "河对岸", 1979, 123, "家庭", "渡船停运后，两岸用玻璃瓶传递晚饭的菜谱。", 175),
        movie("m17", "午夜菜谱", 2011, 93, "喜剧", "餐馆在午夜只接受用故事换来的点单。", 8),
        movie("m18", "风的度量", 2003, 106, "纪录", "虚构的观测站用风筝记录一座城的风向变化。", 190),
        movie("m19", "小型天文台", 2014, 111, "家庭", "屋顶天文台小到只能装下一个人和一架旧望远镜。", 255),
        movie("m20", "回声花园", 1988, 100, "奇幻", "花园把白天的对话存到夜里，再轻轻放出来。", 130),
        movie("m21", "铁皮旅行箱", 1997, 105, "公路", "一只不肯关上的铁皮箱带着主人穿过三个车站。", 20),
        movie("m22", "第七节车厢", 2009, 109, "悬疑", "夜车的第七节车厢没有座号，只有一扇会移动的门。", 240),
        movie("m23", "暖海", 2018, 102, "剧情", "北方小镇用室内泳池模拟他们从没见过的海。", 195),
        movie("m24", "空舞台", 1972, 116, "音乐", "剧团在空舞台上排练一场永远不公开的演出。", 330),
        movie("m25", "云下集市", 1995, 98, "喜剧", "集市搭在低云下面，摊主用铃铛互相找路。", 48),
        movie("m26", "慢镜头", 2007, 113, "剧情", "摄影师只用慢镜头拍摄人们道别的那一秒。", 350),
        movie("m27", "南门开着", 1981, 107, "家庭", "城南门整夜不关，守门人给晚归的人留一盏灯。", 160),
        movie("m28", "铅笔星球", 2015, 95, "动画", "铅笔屑堆成的小岛上，居民用素描修改天气。", 55),
        movie("m29", "潮声档案", 2000, 121, "纪录", "档案馆把每次退潮的声音编成可以借阅的磁带。", 185),
        movie("m30", "双层书店", 2013, 103, "剧情", "书店二楼只卖还没写完的书，读者可以续最后一页。", 265),
        movie("m31", "未命名岛屿", 1990, 127, "冒险", "地图上没名字的岛，渔民用口头故事给它定位。", 170),
        movie("m32", "清晨班车", 2024, 92, "剧情", "第一班公车的司机认识每一位还没睡醒的乘客。", 215),
    )

    fun byId(id: String): Movie? = all.firstOrNull { it.id == id }

    private fun movie(
        id: String,
        title: String,
        year: Int,
        runtimeMinutes: Int,
        genre: String,
        synopsis: String,
        hue: Int,
    ) = Movie(id, title, year, runtimeMinutes, genre, synopsis, hue)
}
```

## `app/src/main/java/com/example/movielab/ui/MovieApp.kt`

sha256 `78b4bedbcb901cca0ab07f72cb0687a3268b0f9c16b2d5db9902332a69970f60`

```kt
@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi::class,
)

package com.example.movielab.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.movielab.data.CatalogMode
import com.example.movielab.data.Movie
import com.example.movielab.data.Movies
import com.example.movielab.data.presentCatalog
import kotlinx.serialization.Serializable

/**
 * Hand-written reference UI. Phone vs expanded width follows
 * calculatePaneScaffoldDirective used by rememberListDetailSceneStrategy:
 * Compact and Medium stay one pane; Expanded and wider show list and detail.
 * That is the frozen Material recipe, not a custom breakpoint.
 */

@Serializable
private data object MovieList : NavKey

@Serializable
private data class MovieDetailKey(val movieId: String) : NavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MovieApp() {
    val backStack = rememberNavBackStack(MovieList)
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
    val singlePane = calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2())
        .maxHorizontalPartitions < 2

    NavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        sceneStrategies = listOf(listDetailStrategy),
        entryProvider = entryProvider {
            entry<MovieList>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("选择一部电影")
                        }
                    },
                ),
            ) {
                MovieListPane(
                    onOpen = { id ->
                        while (backStack.size > 1) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                        backStack.add(MovieDetailKey(id))
                    },
                )
            }
            entry<MovieDetailKey>(
                metadata = ListDetailSceneStrategy.detailPane(),
            ) { key ->
                MovieDetailPane(
                    movieId = key.movieId,
                    showBack = singlePane,
                    onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieListPane(onOpen: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var modeName by rememberSaveable { mutableStateOf(CatalogMode.Content.name) }
    val mode = CatalogMode.entries.firstOrNull { it.name == modeName } ?: CatalogMode.Content
    val presentation = presentCatalog(mode, query, Movies.all)
    val gridState = rememberLazyGridState()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { TopAppBar(title = { Text("本地片单") }) },
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 168.dp),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding)
                .imePadding(),
            contentPadding = innerPadding,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "search", contentType = "chrome", span = { GridItemSpan(maxLineSpan) }) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    label = { Text("搜索标题、类型或简介") },
                    singleLine = true,
                )
            }
            item(key = "modes", contentType = "chrome", span = { GridItemSpan(maxLineSpan) }) {
                ModeRow(
                    mode = mode,
                    onMode = { modeName = it.name },
                )
            }
            if (mode == CatalogMode.Loading) {
                item(key = "loading", contentType = "status", span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                        Text(presentation.message.orEmpty(), modifier = Modifier.padding(top = 12.dp))
                    }
                }
            } else if (presentation.movies.isEmpty()) {
                item(key = "message", contentType = "status", span = { GridItemSpan(maxLineSpan) }) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
                        Text(presentation.message.orEmpty(), style = MaterialTheme.typography.titleMedium)
                        if (mode == CatalogMode.Error) {
                            Button(onClick = { modeName = CatalogMode.Content.name }, modifier = Modifier.padding(top = 12.dp)) {
                                Text("重试")
                            }
                        }
                    }
                }
            } else {
                items(
                    items = presentation.movies,
                    key = { it.id },
                    contentType = { "movie" },
                ) { movie ->
                    MovieCard(movie, onOpen)
                }
            }
        }
    }
}

@Composable
private fun ModeRow(mode: CatalogMode, onMode: (CatalogMode) -> Unit) {
    Column(Modifier.padding(horizontal = 12.dp)) {
        Text("演示状态（本地，无网络）", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            CatalogMode.entries.forEach { candidate ->
                FilterChip(
                    selected = mode == candidate,
                    onClick = { onMode(candidate) },
                    label = {
                        Text(
                            when (candidate) {
                                CatalogMode.Content -> "内容"
                                CatalogMode.Loading -> "加载"
                                CatalogMode.Empty -> "空"
                                CatalogMode.Error -> "错误"
                            },
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun MovieCard(movie: Movie, onOpen: (String) -> Unit) {
    Card(onClick = { onOpen(movie.id) }, modifier = Modifier.padding(horizontal = 12.dp)) {
        Column {
            PlaceholderPoster(title = movie.title, hue = movie.hue, heightDp = 140)
            Column(Modifier.padding(12.dp)) {
                Text(movie.title, style = MaterialTheme.typography.titleMedium)
                Text("${movie.year} · ${movie.genre} · ${movie.runtimeMinutes} 分钟", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieDetailPane(movieId: String, showBack: Boolean, onBack: () -> Unit) {
    val movie = Movies.byId(movieId)
    val scroll = rememberScrollState()
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text(movie?.title ?: "未找到") },
                navigationIcon = {
                    if (showBack) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (movie == null) {
                Text("片单里没有这个 id。")
            } else {
                PlaceholderPoster(title = movie.title, hue = movie.hue, heightDp = 240)
                Text("${movie.year} · ${movie.genre} · ${movie.runtimeMinutes} 分钟")
                Text(movie.synopsis, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "海报是按色相在本地绘制的占位图，不是电影海报，也没有版权图片。",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
```

## `app/src/main/java/com/example/movielab/ui/PlaceholderPoster.kt`

sha256 `55e8ca5265d9df5b6d8ae848438988c439d9fdeaa102ff435ba8895a6247ab59`

```kt
package com.example.movielab.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun PlaceholderPoster(
    title: String,
    hue: Int,
    modifier: Modifier = Modifier,
    heightDp: Int = 180,
) {
    val top = Color.hsl(hue.toFloat(), 0.45f, 0.62f)
    val bottom = Color.hsl((hue + 28).toFloat() % 360f, 0.50f, 0.32f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .semantics { contentDescription = "占位海报，非真实电影海报：$title" },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(brush = Brush.linearGradient(listOf(top, bottom), start = Offset.Zero, end = Offset(size.width, size.height)))
        }
        Column(Modifier.align(Alignment.BottomStart).padding(12.dp)) {
            Text("占位海报", color = Color.White, style = MaterialTheme.typography.labelLarge)
            Text("PLACEHOLDER", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelMedium)
            Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}
```

## `app/src/main/java/com/example/movielab/ui/Theme.kt`

sha256 `2dbc78a54dc5b30c9f774d5676f9e300afca09c1882fdff8faf409dc18e303a3`

```kt
package com.example.movielab.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * ComponentActivity.enableEdgeToEdge already sets system-bar icon contrast.
 * The frozen edge-to-edge skill says not to also force icon appearance in that case.
 */
@Composable
fun MovieLabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme(),
        content = content,
    )
}
```

## `app/src/main/res/drawable/ic_launcher.xml`

sha256 `26830f170462f364de15f01a19b47388db98854b84b8592993c20385d3f21451`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#1F4B6E"
        android:pathData="M0,0h108v108h-108z" />
    <path
        android:fillColor="#F4E7C5"
        android:pathData="M28,24h52v60h-52z" />
    <path
        android:fillColor="#1F4B6E"
        android:pathData="M36,36h36v6h-36zM36,48h28v6h-28zM36,60h22v6h-22z" />
</vector>
```

## `app/src/main/res/values/strings.xml`

sha256 `0ee09eedf0c570673da5a0ed2b4657f00c9eec0f56e9b94fb3448e89461ed258`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">电影列表实验</string>
</resources>
```

## `app/src/main/res/values/themes.xml`

sha256 `053360a4f0caf8f1058609576c8fa7f325c727eea9c6dca5b3a9d1ea263ef5b4`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.MovieLab" parent="android:Theme.Material.Light.NoActionBar">
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
        <item name="android:windowDrawsSystemBarBackgrounds">true</item>
    </style>
</resources>
```

## `app/src/test/java/com/example/movielab/data/CatalogTest.kt`

sha256 `85e3f54eb10b864aabab1e56eca8ae873048e52a0a9fae0089e6ceeaf1a5b1ad`

```kt
package com.example.movielab.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Local data invariants. Not a model-generation sample. */
class CatalogTest {
    @Test
    fun atLeastThirtyLocalMovies() {
        assertTrue(Movies.all.size >= 30)
    }

    @Test
    fun idsAreUniqueAndStable() {
        assertEquals(Movies.all.size, Movies.all.map { it.id }.toSet().size)
        assertEquals("雾港夜车", Movies.byId("m01")?.title)
        assertNull(Movies.byId("missing"))
    }

    @Test
    fun contentFiltersByTitleGenreOrSynopsis() {
        val byTitle = presentCatalog(CatalogMode.Content, "灯塔", Movies.all)
        assertEquals(listOf("m05"), byTitle.movies.map { it.id })
        assertNull(byTitle.message)

        val byGenre = presentCatalog(CatalogMode.Content, "喜剧", Movies.all)
        assertTrue(byGenre.movies.all { it.genre == "喜剧" })
        assertTrue(byGenre.movies.size >= 3)

        val none = presentCatalog(CatalogMode.Content, "___nomatch___", Movies.all)
        assertTrue(none.movies.isEmpty())
        assertEquals("没有匹配「___nomatch___」的电影。", none.message)
    }

    @Test
    fun simulatedModesAreDeterministicAndOffline() {
        val loading = presentCatalog(CatalogMode.Loading, "灯塔", Movies.all)
        assertTrue(loading.movies.isEmpty())
        assertTrue(loading.message!!.contains("没有网络请求"))

        val empty = presentCatalog(CatalogMode.Empty, "", Movies.all)
        assertTrue(empty.movies.isEmpty())
        assertTrue(empty.message!!.contains("没有网络请求"))

        val error = presentCatalog(CatalogMode.Error, "", Movies.all)
        assertTrue(error.movies.isEmpty())
        assertTrue(error.message!!.contains("没有网络请求"))
    }
}
```
