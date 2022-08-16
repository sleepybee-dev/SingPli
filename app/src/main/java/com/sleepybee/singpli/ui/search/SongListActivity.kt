package com.sleepybee.singpli.ui.search

import android.content.Intent
import android.graphics.LightingColorFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.sleepybee.singpli.PLBLApplication
import com.sleepybee.singpli.R
import com.sleepybee.singpli.databinding.ActivitySongListBinding
import com.sleepybee.singpli.item.KaraokeItem
import com.sleepybee.singpli.item.SnippetItem
import com.sleepybee.singpli.item.SongItem
import com.sleepybee.singpli.ui.adapter.SongListAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.net.URLEncoder
import java.time.LocalDateTime
import java.util.*

class SongListActivity : AppCompatActivity() {

    private var _binding: ActivitySongListBinding? = null
    private val binding get() = _binding!!

    private var searchViewModel: SearchViewModel? = null
    private lateinit var snippetItem: SnippetItem
    private var songList: ArrayList<SongItem> = arrayListOf()
    private val songListAdapter = SongListAdapter()

    private var videoUrl: String = ""
    var source: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivitySongListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        searchViewModel =
            ViewModelProvider(this)[SearchViewModel::class.java]

        snippetItem = intent.getSerializableExtra("snippet") as SnippetItem
        intent.getSerializableExtra("songList")?.let {
            songList =
                ArrayList(Gson().fromJson(it as String, Array<SongItem>::class.java).toList())
        }

        videoUrl = "https://www.youtube.com/watch?v=${snippetItem.videoId}"
        initSnippetView(snippetItem)

        binding.rvSongList.adapter = songListAdapter

        binding.btnBackSongList.setOnClickListener {
            finish()
        }
        binding.btnHeartSongList.setOnClickListener {
            updateHearted(snippetItem)
        }

        if (songList.isEmpty()) {
            fetchSongs()
        } else {
            songListAdapter.setSongList(songList)
            binding.progressSongList.visibility = View.GONE
            binding.btnHeartSongList.visibility = View.VISIBLE
            updateRecentSnippet(
                snippetItem, songList
            )
        }
    }

    private fun updateHearted(snippetItem: SnippetItem) {
        if (songList.isEmpty()) {
            Toast.makeText(this, "찜할 리스트가 없네요.", Toast.LENGTH_LONG).show()
            return
        }
        CoroutineScope(IO).launch {
            val isHeartedNew = !snippetItem.isHearted
            snippetItem.isHearted = isHeartedNew
            withContext(Main) {
                binding.btnHeartSongList.setBackgroundResource(if (isHeartedNew) R.drawable.ic_on_smallheart else R.drawable.ic_off_smallheart)
            }
            searchViewModel?.updateHeart(snippetItem)
        }
    }

    private fun fetchSongs() {
        CoroutineScope(IO).launch {
            try {
                val doc = Jsoup.connect(videoUrl).get()

                val elems = doc.select("script")
                if (elems.toString().contains("ytInitialData") && elems.toString().contains("carouselLockups")) {
                    elems.reverse()
                    for (scriptElem in elems) {
                        if (scriptElem.data().contains("ytInitialData") && scriptElem.data()
                                .contains("carouselLockups")
                        ) {
                            val wholeData = scriptElem.data().replace("\n", "").replace(": ", ":")
                            val regEx = "(\"carouselLockups\":)(\\[.*])".toRegex()
                            regEx.find(wholeData)?.let { matchResult ->
                                Log.d("sblee", matchResult.value)
                                val data = JSONObject("{" + matchResult.value + "}")
                                val list = data.getJSONArray("carouselLockups")

                                for (i in 0 until list.length()) {
                                    val song =
                                        list.getJSONObject(i)
                                            .getJSONObject("carouselLockupRenderer")
                                    val titleObject = song.getJSONObject("videoLockup")
                                        .getJSONObject("compactVideoRenderer")
                                        .getJSONObject("title")

                                    val title = if (titleObject.toString().contains("simpleText")) {
                                        titleObject.getString("simpleText")
                                    } else {
                                        titleObject.getJSONArray("runs")
                                            .getJSONObject(0)["text"] as String
                                    }

                                    val infoObject = song.getJSONArray("infoRows")

                                    val artistObject =
                                        infoObject.getJSONObject(0).getJSONObject("infoRowRenderer")
                                            .getJSONObject("defaultMetadata")

                                    val artist =
                                        if (artistObject.toString().contains("simpleText")) {
                                            artistObject.getString("simpleText")
                                        } else {
                                            artistObject.getJSONArray("runs")
                                                .getJSONObject(0)["text"] as String
                                        }

                                    var license = ""
                                    if (infoObject.length() >= 2) {
                                        license = infoObject.getJSONObject(infoObject.length() - 1)
                                            .getJSONObject("infoRowRenderer")
                                            .getJSONObject("expandedMetadata")["simpleText"] as String
                                    }

                                    Log.d("sblee", "title : $title/$artist / $license")
                                    if (license.lowercase(Locale.getDefault()).contains("japan")) {
                                        searchWithJapanese(title, artist)
                                    } else {
                                        searchSongNumber(title, artist)
                                    }

                                }

                                updateRecentSnippet(
                                    snippetItem, songList
                                )
                                withContext(Main) {
                                    binding.progressSongList.visibility = View.GONE
                                    binding.btnHeartSongList.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                } else {
                    withContext(Main) {
                        binding.progressSongList.visibility = View.GONE
                        binding.btnHeartSongList.visibility = View.VISIBLE
                        Toast.makeText(this@SongListActivity, "음악 정보가 포함되지 않은 비디오입니다.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
}

private fun updateRecentSnippet(snippetItem: SnippetItem, songList: List<SongItem>) {
    val today = LocalDateTime.now()
    snippetItem.viewDate = today.toString()
    searchViewModel?.insertRecentSnippet(snippetItem)
    searchViewModel?.insertSongs(songList)
}

private fun initSnippetView(snippetItem: SnippetItem) {
    with(snippetItem) {
        binding.tvTitleSongList.text = title
        binding.tvChannelDateSongList.text = "$channelTitle | ${publishedAt.split("T")[0]}"
        Glide.with(binding.root.context)
            .load(thumbnail)
            .centerCrop()
            .into(binding.ivThumbnailSongList)
        binding.btnHeartSongList.setBackgroundResource(if (isHearted) R.drawable.ic_on_smallheart else R.drawable.ic_off_smallheart)

        binding.btnYoutubeSongList.setOnClickListener {
            val videoUrl = "https://www.youtube.com/watch?v=${videoId}"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
            binding.root.context.startActivity(browserIntent)
        }
    }

    binding.btnYoutubeSongList.setOnClickListener {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
        startActivity(browserIntent)
    }
}

private fun searchSongNumber(originTitle: String, originArtist: String) {
    var title = originTitle.lowercase()
    var artist = originArtist.lowercase()

    if (PLBLApplication.artistTransData.containsKey(artist) && PLBLApplication.artistTransData[artist] != null) {
        artist = PLBLApplication.artistTransData[artist]!!
    }
    if (PLBLApplication.titleTransData.containsKey(title) && PLBLApplication.titleTransData[title] != null) {
        title = PLBLApplication.titleTransData[title]!!
    }

//    val regEx = "₩([^)]*₩)".toRegex()
//    regEx.find(title)?.let { matchResult ->
//        title = title.replace(matchResult.value, "")
//    }

    artist = URLEncoder.encode(artist, "US-ASCII")
    title = URLEncoder.encode(title, "US-ASCII")

    val doc =
        Jsoup.connect("https://m.search.naver.com/search.naver?sm=mtp_hty.top&where=m&query=$artist+%2F+$title+노래방+노래검색")
            .get()
    val searchElems: Elements =
        doc.select("div#container div#ct")[0].getElementsByClass("singroom_card_sub")
    if (!searchElems.isNullOrEmpty()) {
        val tableElems = searchElems[0].select("table")[0].select("tbody tr")
        Log.d("SB", "table Elems : " + tableElems.size)
        val maxElemCount = if (tableElems.size > 5) 5 else tableElems.size
        val searchResults: ArrayList<KaraokeItem> = arrayListOf()
        for (i in 0 until maxElemCount) {
            val elems = tableElems[i].select("td")
            val searchTitles = elems[0].text().split("/")
            val number = elems[1].text().toInt()
            val brand = elems[2].text()
            val searchResult = "[$brand] $searchTitles - $number"
            Log.d("sblee", "search : $searchResult")
            val karaokeItem = KaraokeItem(
                brand = brand,
                number = number,
                title = searchTitles[0].trim(),
                artist = searchTitles[1].trim()
            )
            searchResults.add(karaokeItem)
        }
        val songItem = SongItem(
            songId = null,
            videoId = snippetItem.videoId,
            title = originTitle,
            artist = originArtist,
            karaokeList = Gson().toJson(searchResults)
        )

        addSong(songItem)

    } else {
        searchWithJapanese(originTitle, originArtist)
    }
}

private fun addSong(songItem: SongItem) {
    CoroutineScope(Dispatchers.Main).launch {
        songList.add(songItem)
        songListAdapter.addSong(songItem)
    }
}

private fun searchWithJapanese(title: String, artist: String) {
    // 제목이 훈독 히라가나 일본곡인지 한번 더 돌아본다
    val generalDoc =
        Jsoup.connect("https://m.search.naver.com/search.naver?sm=mtp_hty.top&where=m_view&query=$artist+$title")
            .get()

    val generalElems = generalDoc.getElementsByClass("api_txt_lines total_tit _cross_trigger")
    var japaneseWord = ""
    for (i in 0..10) {
        if (i >= generalElems.size) break
        val elem = generalDoc.getElementsByClass("api_txt_lines total_tit _cross_trigger")[i]

        Log.d("sblee", "additional search : " + elem.text())
        japaneseWord = getJapanese(elem.text())
        if (japaneseWord.isNotEmpty()) {
            break
        }
    }

    if (japaneseWord.isEmpty()) return

    Log.d("sblee", "additional search : $japaneseWord")

    japaneseWord = URLEncoder.encode(japaneseWord, "US-ASCII")
    val songDoc =
        Jsoup.connect("https://m.search.naver.com/search.naver?sm=mtp_hty.top&where=m&query=$japaneseWord+$artist+%2F+$title+노래방+노래검색")
            .get()
    val searchElems: Elements =
        songDoc.select("div#container div#ct")[0].getElementsByClass("singroom_card_sub")
    if (!searchElems.isNullOrEmpty()) {
        val tableElems = searchElems[0].select("table")[0].select("tbody tr")
        // 상위 세개 긁어오기
        val searchResults: ArrayList<KaraokeItem> = arrayListOf()
        for (i in 0 until 2) {
            val elems = tableElems[i].select("td")
            val searchTitles = elems[0].text().split("/")
            val number = elems[1].text().toInt()
            val brand = elems[2].text()
            val searchResult = "[$brand] $searchTitles - $number"
            val karaokeItem = KaraokeItem(
                brand = brand,
                number = number,
                title = searchTitles[0].trim(),
                artist = searchTitles[1].trim()
            )
            searchResults.add(karaokeItem)

            Log.d("sblee", "additional search : $searchResult")
        }
        val songItem = SongItem(
            songId = null,
            videoId = snippetItem.videoId,
            title = title,
            artist = artist,
            karaokeList = Gson().toJson(searchResults)
        )
        addSong(songItem)
    }
}

private fun getJapanese(elemText: String): String {
    var resultJapanese = ""
    for (i in elemText.indices) {
        if (isJapanese(elemText[i])) {
            resultJapanese += elemText[i]
        }
    }
    return resultJapanese
}

private fun isJapanese(char: Char): Boolean {
    return char in '\u4E00'..'\u9FFF' || char in '\u3040'..'\u309F' || char in '\u30A0'..'\u30FF'
}

}

