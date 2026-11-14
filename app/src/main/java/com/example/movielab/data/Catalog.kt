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
