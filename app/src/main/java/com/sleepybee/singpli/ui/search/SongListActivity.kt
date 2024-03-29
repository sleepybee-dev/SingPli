package com.sleepybee.singpli.ui.search

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Toast
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
import com.sleepybee.singpli.ui.common.BaseDataBindingActivity
import com.sleepybee.singpli.utils.INTENT_KEY_SNIPPET
import com.sleepybee.singpli.utils.INTENT_KEY_SONG_LIST
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import timber.log.Timber
import java.time.LocalDateTime
import java.util.*

@AndroidEntryPoint
class SongListActivity : BaseDataBindingActivity<ActivitySongListBinding>(R.layout.activity_song_list), View.OnClickListener {

    private lateinit var searchViewModel: SearchViewModel
    private lateinit var snippetItem: SnippetItem
    private var songList: ArrayList<SongItem> = arrayListOf()
    private val songListAdapter = SongListAdapter()

    private var videoUrl: String = ""

    override fun initBinding() {
        searchViewModel =
            ViewModelProvider(this)[SearchViewModel::class.java]

        snippetItem = intent.getSerializableExtra(INTENT_KEY_SNIPPET) as SnippetItem
        intent.getStringExtra(INTENT_KEY_SONG_LIST)?.let {
            songList =
                ArrayList(Gson().fromJson(it, Array<SongItem>::class.java).toList())
        }

        runBlocking {
            if (songList.isEmpty()) {
                searchViewModel.getSnippetById(snippetItem.videoId).collect { snippet ->
                    snippet?.let {
                        it.songList?.let { list ->
                            if (list.isNotEmpty()) {
                                songList.addAll(list)
                            }
                        }
                    }
                }
            }
        }

        videoUrl = "https://www.youtube.com/watch?v=${snippetItem.videoId}"
        initSnippetView(snippetItem)

        binding.rvSongList.adapter = songListAdapter

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
            searchViewModel.updateHeart(snippetItem)
        }
    }

    private fun fetchSongs() {
        CoroutineScope(IO).launch {
            try {
                val doc = Jsoup.connect(videoUrl).get()

                val elems = doc.select("script")
                if (elems.toString().contains("ytInitialData") && elems.toString()
                        .contains("carouselLockups")
                ) {
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

                                    var title = if (titleObject.toString().contains("simpleText")) {
                                        titleObject.getString("simpleText")
                                    } else {
                                        titleObject.getJSONArray("runs")
                                            .getJSONObject(0)["text"] as String
                                    }

                                    val infoObject = song.getJSONArray("infoRows")

                                    val artistObject =
                                        infoObject.getJSONObject(0).getJSONObject("infoRowRenderer")
                                            .getJSONObject("defaultMetadata")

                                    var artist =
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

                                    Timber.d("search : $artist / $title")

                                    // 괄호 거르기
                                    val regEx = "\\(.*?\\)".toRegex()
                                    title = regEx.replace(title, "")

                                    // 특문 거르기
//                                    val regEx2 = "[^A-Za-z0-9]".toRegex()
//                                    title = regEx2.replace(title, "")
//                                    artist = regEx2.replace(artist, "")

                                    title = title.replace("\"", "")
                                    artist = artist.replace("|", " ").replace("\"", "")

                                    Timber.d("search2 : $artist / $title")

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
                        Toast.makeText(
                            this@SongListActivity,
                            "음악 정보가 포함되지 않은 비디오입니다.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Main) {
                    binding.progressSongList.visibility = View.GONE
                    binding.btnHeartSongList.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun updateRecentSnippet(snippetItem: SnippetItem, songList: List<SongItem>) {
        val today = LocalDateTime.now()
        snippetItem.viewDate = today.toString()
        searchViewModel.insertRecentSnippet(snippetItem)
        searchViewModel.clearAndInsertSongs(snippetItem.videoId, songList)
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

        val doc =
            Jsoup.connect("https://m.search.naver.com/search.naver?sm=mtp_hty.top&where=m&query=$artist+%2F+$title+노래방+노래검색")
                .get()
        val searchElems: Elements =
            doc.select("div#container div#ct")[0].getElementsByClass("singroom_card_sub")
        if (!searchElems.isNullOrEmpty()) {
            val tableElems = searchElems[0].select("table")[0].select("tbody tr")
            val maxElemCount = if (tableElems.size > 5) 5 else tableElems.size
            val searchResults: ArrayList<KaraokeItem> = arrayListOf()
            for (i in 0 until maxElemCount) {
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


        val songDoc =
            Jsoup.connect("https://m.search.naver.com/search.naver?sm=mtp_hty.top&where=m&query=$japaneseWord+$artist+$title+노래방+노래검색")
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

    override fun onClick(view: View) {
        when(view.id) {
            R.id.btn_youtube_song_list ->
                videoUrl.let {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                    startActivity(browserIntent)
                }
            R.id.btn_heart_song_list ->
                updateHearted(snippetItem)
            R.id.btn_back_song_list ->
                finish()
        }
    }
}

