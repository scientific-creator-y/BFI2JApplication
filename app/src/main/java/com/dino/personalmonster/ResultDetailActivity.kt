package com.dino.personalmonster

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import com.dino.personalmonster.ui.InsetsUtil
import java.io.File
import java.io.FileOutputStream

class ResultDetailActivity : AppCompatActivity() {

    data class ColorSet(
        val titleColor: String,
        val bgColor: String,
        val cardColor: String,
        val researchBtnColor: String,
        val shareBtnColor: String,
        val backBtnColor: String,
    )

    data class ProfileData(
        val typelevel: String,
        val profTitleText: Int,
        val profDescText: Int,
        val profImageSrc: Int,
        val featureTitleText :Int,
        val profileFeatureDescText: Int,
        val profileFeature_1_SubtitleText :Int,
        val profileFeature_2_SubtitleText :Int,
        val profileFeature_3_SubtitleText :Int,
        val profileFeature_4_SubtitleText :Int,
        val profileFeature_5_SubtitleText :Int,
        val profileFeature_6_SubtitleText :Int,
        val profileFeature_1_SubDescText: Int,
        val profileFeature_2_SubDescText: Int,
        val profileFeature_3_SubDescText: Int,
        val profileFeature_4_SubDescText: Int,
        val profileFeature_5_SubDescText: Int,
        val profileFeature_6_SubDescText: Int
    )

    private lateinit var type: String
    private var score = 0f
    private lateinit var level: String

    private lateinit var profileDataset: ProfileData



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)


        setContentView(R.layout.activity_extraversion_result)

        // Viewを取得して余白を入れる
        val rootLayout = findViewById<ScrollView>(R.id.rootLayout)
        InsetsUtil.applySystemBarsInsets(rootLayout)


        // 画面のタイトル設定
        supportActionBar?.title = "あなたの性格診断の結果"

        type = intent.getStringExtra("type") ?:"E"
        score = intent.getFloatExtra("score", 0f)
        level = intent.getStringExtra("level") ?:"high"

        // レベルの判定
//        if(score > 3) {
//            level = "high"
//        } else {
//            level = "low"
//        }

//        Log.i("tag", "取得できたのは…タイプ：${type}、スコア：${score}、レベル：${level}")
//        Log.i("tag", "表示する画面のレイアウトは${type}-${level}の配色で。")

        // UIを取得
        val contentLayoutRoot = findViewById<LinearLayout>(R.id.contentLayoutRoot)
        val profileTitle = findViewById<TextView>(R.id.tvProfileTitle)
        val image = findViewById<ImageView>(R.id.ivProfileImage)
        val profileDescription = findViewById<TextView>(R.id.tvProfileDescription)
        val featureTitle = findViewById<TextView>(R.id.tvProfileFeatureTitle)
        val profileFeatureDesc = findViewById<TextView>(R.id.tvProfileFeatureDesc)

        // が高い人の6つの特徴
        val profileFeature_1 = findViewById<CardView>(R.id.cvProfileFeature_1)
        val profileFeature_2 = findViewById<CardView>(R.id.cvProfileFeature_2)
        val profileFeature_3 = findViewById<CardView>(R.id.cvProfileFeature_3)
        val profileFeature_4 = findViewById<CardView>(R.id.cvProfileFeature_4)
        val profileFeature_5 = findViewById<CardView>(R.id.cvProfileFeature_5)
        val profileFeature_6 = findViewById<CardView>(R.id.cvProfileFeature_6)

        // ー的であるなど
        val profileFeature_1_Subtitle = findViewById<TextView>(R.id.tvProfileFeature_1_Subtitle)
        val profileFeature_2_Subtitle = findViewById<TextView>(R.id.tvProfileFeature_2_Subtitle)
        val profileFeature_3_Subtitle = findViewById<TextView>(R.id.tvProfileFeature_3_Subtitle)
        val profileFeature_4_Subtitle = findViewById<TextView>(R.id.tvProfileFeature_4_Subtitle)
        val profileFeature_5_Subtitle = findViewById<TextView>(R.id.tvProfileFeature_5_Subtitle)
        val profileFeature_6_Subtitle = findViewById<TextView>(R.id.tvProfileFeature_6_Subtitle)

        // ●ーーのリスト
        val profileFeature_1_SubDesc = findViewById<TextView>(R.id.tvProfileFeature_1_SubDesc)
        val profileFeature_2_SubDesc = findViewById<TextView>(R.id.tvProfileFeature_2_SubDesc)
        val profileFeature_3_SubDesc = findViewById<TextView>(R.id.tvProfileFeature_3_SubDesc)
        val profileFeature_4_SubDesc = findViewById<TextView>(R.id.tvProfileFeature_4_SubDesc)
        val profileFeature_5_SubDesc = findViewById<TextView>(R.id.tvProfileFeature_5_SubDesc)
        val profileFeature_6_SubDesc = findViewById<TextView>(R.id.tvProfileFeature_6_SubDesc)

        // ボタンのUI取得
//        val btnResearchLink = findViewById<Button>(R.id.btnResearchLink)
        val btnShare = findViewById<Button>(R.id.btnShare)
        val btnBackToList = findViewById<Button>(R.id.btnBackToList)
//        Log.i("tag", "findViewById完了")

//        ボタンテキストを反映 → いったんWebURLなしで
//        when(type) {
//            "E" -> btnResearchLink.text = getString(R.string.btn_prof_e_toBlog)
//            "A" -> btnResearchLink.text = getString(R.string.btn_prof_a_toBlog)
//            "C" -> btnResearchLink.text = getString(R.string.btn_prof_c_toBlog)
//            "N" -> btnResearchLink.text = getString(R.string.btn_prof_n_toBlog)
//            "O" -> btnResearchLink.text = getString(R.string.btn_prof_o_toBlog)
//        }


//        "#6A1B9A", // タイトル（深い紫）
//        "#F3E5F5", // 背景（かなり薄い紫）
//        "#CE93D8", // カード（やや明るい紫）
//        "#8E24AA", // ボタン1（メイン）
//        "#BA68C8", // ボタン2（サブ）
//        "#4A148C"


        // 配色セットを用意
        val colorScheme = when("$type-$level") {
            "E-high" -> ColorSet("#D32F2F", "#FFF5F5","#FF8A80","#E64A19","#F57C00","#BF360C",)
            "E-low" -> ColorSet("#6A1B9A", "#F9F0FF","#CE93D8","#8E24AA","#AB47BC","#4A148C",)
            "E-mid" -> ColorSet("#6A1B9A", "#F9F0FF","#CE93D8","#8E24AA","#AB47BC","#4A148C",)

            "A-high" -> ColorSet("#388E3C", "#F5FFF5","#A5D6A7","#43A047","#66BB6A","#2E7D32",)
            "A-low" -> ColorSet("#1B5E20", "#F3FFF5","#81C784","#388E3C","#43A047","#2E7D32",)
            "A-mid" -> ColorSet("#1B5E20", "#F3FFF5","#81C784","#388E3C","#43A047","#2E7D32",)

            "C-high" -> ColorSet("#1976D2", "#F5FAFF","#90CAF9","#1E88E5","#42A5F5","#1565C0",)
            "C-low" -> ColorSet("#00ACC1", "#F5FFFF","#80DEEA","#0097A7","#26C6DA","#00838F",)
            "C-mid" -> ColorSet("#00ACC1", "#F5FFFF","#80DEEA","#0097A7","#26C6DA","#00838F",)

            "N-high" -> ColorSet("#6A1B9A", "#F3E5F5","#CE93D8","#8E24AA","#BA68C8","#4A148C",)
            "N-low" -> ColorSet("#5C6BC0", "#F8F9FF","#C5CAE9","#7986CB","#9FA8DA","#3949AB",)
            "N-mid" -> ColorSet("#5C6BC0", "#F8F9FF","#C5CAE9","#7986CB","#9FA8DA","#3949AB",)

            "O-high" -> ColorSet("#FBC02D", "#FFFCF5","#FFF59D","#FDD835","#FFEE58","#F9A825",)
            "O-low" -> ColorSet("#757575", "#FAFAFA","#E0E0E0","#9E9E9E","#BDBDBD","#616161",)
            "O-mid" -> ColorSet("#757575", "#FAFAFA","#E0E0E0","#9E9E9E","#BDBDBD","#616161",)

            else -> ColorSet("#444","#444","#444","#444","#444","#444",)
        }

        // 配色を反映
        contentLayoutRoot.setBackgroundColor(Color.parseColor(colorScheme.bgColor))
        profileTitle.setTextColor(Color.parseColor(colorScheme.titleColor))
        featureTitle.setTextColor(Color.parseColor(colorScheme.titleColor))
        profileFeature_1.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor(colorScheme.cardColor)))
        profileFeature_2.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor(colorScheme.cardColor)))
        profileFeature_3.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor(colorScheme.cardColor)))
        profileFeature_4.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor(colorScheme.cardColor)))
        profileFeature_5.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor(colorScheme.cardColor)))
        profileFeature_6.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor(colorScheme.cardColor)))

//        btnResearchLink.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorScheme.researchBtnColor))
        btnShare.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorScheme.shareBtnColor))
        btnBackToList.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorScheme.backBtnColor))
//        Log.i("tag", "背景色設定完了")


        // データをすべて入れる定数
        profileDataset = when("$type-$level") {
            "E-high" -> ResultDetailActivity.ProfileData(
                "E-high",
                R.string.tv_prof_e_high_title,
                R.string.tv_prof_e_high_desc,
                R.drawable.header_001,
                R.string.tv_prof_e_high_feature_title,
                R.string.tv_prof_e_high_feature_desc,
                R.string.tv_prof_e_high_feature_1_subtitle, R.string.tv_prof_e_high_feature_2_subtitle, R.string.tv_prof_e_high_feature_3_subtitle, R.string.tv_prof_e_high_feature_4_subtitle, R.string.tv_prof_e_high_feature_5_subtitle, R.string.tv_prof_e_high_feature_6_subtitle,
                R.string.tv_prof_e_high_feature_1_subdesc, R.string.tv_prof_e_high_feature_2_subdesc, R.string.tv_prof_e_high_feature_3_subdesc, R.string.tv_prof_e_high_feature_4_subdesc, R.string.tv_prof_e_high_feature_5_subdesc, R.string.tv_prof_e_high_feature_6_subdesc,
                )


            "E-low" -> ResultDetailActivity.ProfileData("E-low",R.string.tv_prof_e_low_title, R.string.tv_prof_e_low_desc,R.drawable.header_011,
                R.string.tv_prof_e_low_feature_title, R.string.tv_prof_e_low_feature_desc,
                R.string.tv_prof_e_low_feature_1_subtitle, R.string.tv_prof_e_low_feature_2_subtitle, R.string.tv_prof_e_low_feature_3_subtitle, R.string.tv_prof_e_low_feature_4_subtitle, R.string.tv_prof_e_low_feature_5_subtitle, R.string.tv_prof_e_low_feature_6_subtitle,
                R.string.tv_prof_e_low_feature_1_subdesc, R.string.tv_prof_e_low_feature_2_subdesc, R.string.tv_prof_e_low_feature_3_subdesc, R.string.tv_prof_e_low_feature_4_subdesc, R.string.tv_prof_e_low_feature_5_subdesc, R.string.tv_prof_e_low_feature_6_subdesc, )

            "E-mid" -> ResultDetailActivity.ProfileData("E-mid",R.string.tv_prof_e_mid_title, R.string.tv_prof_e_mid_desc,R.drawable.header_006,
                R.string.tv_prof_e_mid_feature_title, R.string.tv_prof_e_mid_feature_desc,
                R.string.tv_prof_e_mid_feature_1_subtitle, R.string.tv_prof_e_mid_feature_2_subtitle, R.string.tv_prof_e_mid_feature_3_subtitle, R.string.tv_prof_e_mid_feature_4_subtitle, R.string.tv_prof_e_mid_feature_5_subtitle, R.string.tv_prof_e_mid_feature_6_subtitle,
                R.string.tv_prof_e_mid_feature_1_subdesc, R.string.tv_prof_e_mid_feature_2_subdesc, R.string.tv_prof_e_mid_feature_3_subdesc, R.string.tv_prof_e_mid_feature_4_subdesc, R.string.tv_prof_e_mid_feature_5_subdesc, R.string.tv_prof_e_mid_feature_6_subdesc, )


            // 協調性
            "A-high" -> ResultDetailActivity.ProfileData("A-high",R.string.tv_prof_a_high_title, R.string.tv_prof_a_high_desc,R.drawable.header_002,
                R.string.tv_prof_a_high_feature_title, R.string.tv_prof_a_high_feature_desc,
                R.string.tv_prof_a_high_feature_1_subtitle, R.string.tv_prof_a_high_feature_2_subtitle, R.string.tv_prof_a_high_feature_3_subtitle, R.string.tv_prof_a_high_feature_4_subtitle, R.string.tv_prof_a_high_feature_5_subtitle, R.string.tv_prof_a_high_feature_6_subtitle,
                R.string.tv_prof_a_high_feature_1_subdesc, R.string.tv_prof_a_high_feature_2_subdesc, R.string.tv_prof_a_high_feature_3_subdesc, R.string.tv_prof_a_high_feature_4_subdesc, R.string.tv_prof_a_high_feature_5_subdesc, R.string.tv_prof_a_high_feature_6_subdesc, )

            "A-low" -> ResultDetailActivity.ProfileData("A-low",R.string.tv_prof_a_low_title, R.string.tv_prof_a_low_desc,R.drawable.header_012,
                R.string.tv_prof_a_low_feature_title, R.string.tv_prof_a_low_feature_desc,
                R.string.tv_prof_a_low_feature_1_subtitle, R.string.tv_prof_a_low_feature_2_subtitle, R.string.tv_prof_a_low_feature_3_subtitle, R.string.tv_prof_a_low_feature_4_subtitle, R.string.tv_prof_a_low_feature_5_subtitle, R.string.tv_prof_a_low_feature_6_subtitle,
                R.string.tv_prof_a_low_feature_1_subdesc, R.string.tv_prof_a_low_feature_2_subdesc, R.string.tv_prof_a_low_feature_3_subdesc, R.string.tv_prof_a_low_feature_4_subdesc, R.string.tv_prof_a_low_feature_5_subdesc, R.string.tv_prof_a_low_feature_6_subdesc, )
            "A-mid" -> ResultDetailActivity.ProfileData("A-mid",R.string.tv_prof_a_mid_title, R.string.tv_prof_a_mid_desc,R.drawable.header_007,
                R.string.tv_prof_a_mid_feature_title, R.string.tv_prof_a_mid_feature_desc,
                R.string.tv_prof_a_mid_feature_1_subtitle, R.string.tv_prof_a_mid_feature_2_subtitle, R.string.tv_prof_a_mid_feature_3_subtitle, R.string.tv_prof_a_mid_feature_4_subtitle, R.string.tv_prof_a_mid_feature_5_subtitle, R.string.tv_prof_a_mid_feature_6_subtitle,
                R.string.tv_prof_a_mid_feature_1_subdesc, R.string.tv_prof_a_mid_feature_2_subdesc, R.string.tv_prof_a_mid_feature_3_subdesc, R.string.tv_prof_a_mid_feature_4_subdesc, R.string.tv_prof_a_mid_feature_5_subdesc, R.string.tv_prof_a_mid_feature_6_subdesc, )


            // 勤勉性
            "C-high" -> ResultDetailActivity.ProfileData("C-high",R.string.tv_prof_c_high_title, R.string.tv_prof_c_high_desc,R.drawable.header_003,
                R.string.tv_prof_c_high_feature_title, R.string.tv_prof_c_high_feature_desc,
                R.string.tv_prof_c_high_feature_1_subtitle, R.string.tv_prof_c_high_feature_2_subtitle, R.string.tv_prof_c_high_feature_3_subtitle, R.string.tv_prof_c_high_feature_4_subtitle, R.string.tv_prof_c_high_feature_5_subtitle, R.string.tv_prof_c_high_feature_6_subtitle,
                R.string.tv_prof_c_high_feature_1_subdesc, R.string.tv_prof_c_high_feature_2_subdesc, R.string.tv_prof_c_high_feature_3_subdesc, R.string.tv_prof_c_high_feature_4_subdesc, R.string.tv_prof_c_high_feature_5_subdesc, R.string.tv_prof_c_high_feature_6_subdesc, )

            "C-low" -> ResultDetailActivity.ProfileData("C-low",R.string.tv_prof_c_low_title, R.string.tv_prof_c_low_desc,R.drawable.header_013,
                R.string.tv_prof_c_low_feature_title, R.string.tv_prof_c_low_feature_desc,
                R.string.tv_prof_c_low_feature_1_subtitle, R.string.tv_prof_c_low_feature_2_subtitle, R.string.tv_prof_c_low_feature_3_subtitle, R.string.tv_prof_c_low_feature_4_subtitle, R.string.tv_prof_c_low_feature_5_subtitle, R.string.tv_prof_c_low_feature_6_subtitle,
                R.string.tv_prof_c_low_feature_1_subdesc, R.string.tv_prof_c_low_feature_2_subdesc, R.string.tv_prof_c_low_feature_3_subdesc, R.string.tv_prof_c_low_feature_4_subdesc, R.string.tv_prof_c_low_feature_5_subdesc, R.string.tv_prof_c_low_feature_6_subdesc, )

            "C-mid" -> ResultDetailActivity.ProfileData("C-mid",R.string.tv_prof_c_mid_title, R.string.tv_prof_c_mid_desc,R.drawable.header_008,
                R.string.tv_prof_c_mid_feature_title, R.string.tv_prof_c_mid_feature_desc,
                R.string.tv_prof_c_mid_feature_1_subtitle, R.string.tv_prof_c_mid_feature_2_subtitle, R.string.tv_prof_c_mid_feature_3_subtitle, R.string.tv_prof_c_mid_feature_4_subtitle, R.string.tv_prof_c_mid_feature_5_subtitle, R.string.tv_prof_c_mid_feature_6_subtitle,
                R.string.tv_prof_c_mid_feature_1_subdesc, R.string.tv_prof_c_mid_feature_2_subdesc, R.string.tv_prof_c_mid_feature_3_subdesc, R.string.tv_prof_c_mid_feature_4_subdesc, R.string.tv_prof_c_mid_feature_5_subdesc, R.string.tv_prof_c_mid_feature_6_subdesc, )


            // 情緒安定性
            "N-high" -> ResultDetailActivity.ProfileData("N-high",R.string.tv_prof_n_high_title, R.string.tv_prof_n_high_desc,R.drawable.header_004,
                R.string.tv_prof_n_high_feature_title, R.string.tv_prof_n_high_feature_desc,
                R.string.tv_prof_n_high_feature_1_subtitle, R.string.tv_prof_n_high_feature_2_subtitle, R.string.tv_prof_n_high_feature_3_subtitle, R.string.tv_prof_n_high_feature_4_subtitle, R.string.tv_prof_n_high_feature_5_subtitle, R.string.tv_prof_n_high_feature_6_subtitle,
                R.string.tv_prof_n_high_feature_1_subdesc, R.string.tv_prof_n_high_feature_2_subdesc, R.string.tv_prof_n_high_feature_3_subdesc, R.string.tv_prof_n_high_feature_4_subdesc, R.string.tv_prof_n_high_feature_5_subdesc, R.string.tv_prof_n_high_feature_6_subdesc, )

            "N-low" -> ResultDetailActivity.ProfileData("N-low",R.string.tv_prof_n_low_title, R.string.tv_prof_n_low_desc,R.drawable.header_014,
                R.string.tv_prof_n_low_feature_title, R.string.tv_prof_n_low_feature_desc,
                R.string.tv_prof_n_low_feature_1_subtitle, R.string.tv_prof_n_low_feature_2_subtitle, R.string.tv_prof_n_low_feature_3_subtitle, R.string.tv_prof_n_low_feature_4_subtitle, R.string.tv_prof_n_low_feature_5_subtitle, R.string.tv_prof_n_low_feature_6_subtitle,
                R.string.tv_prof_n_low_feature_1_subdesc, R.string.tv_prof_n_low_feature_2_subdesc, R.string.tv_prof_n_low_feature_3_subdesc, R.string.tv_prof_n_low_feature_4_subdesc, R.string.tv_prof_n_low_feature_5_subdesc, R.string.tv_prof_n_low_feature_6_subdesc, )
            "N-mid" -> ResultDetailActivity.ProfileData("N-mid",R.string.tv_prof_n_mid_title, R.string.tv_prof_n_mid_desc,R.drawable.header_009,
                R.string.tv_prof_n_mid_feature_title, R.string.tv_prof_n_mid_feature_desc,
                R.string.tv_prof_n_mid_feature_1_subtitle, R.string.tv_prof_n_mid_feature_2_subtitle, R.string.tv_prof_n_mid_feature_3_subtitle, R.string.tv_prof_n_mid_feature_4_subtitle, R.string.tv_prof_n_mid_feature_5_subtitle, R.string.tv_prof_n_mid_feature_6_subtitle,
                R.string.tv_prof_n_mid_feature_1_subdesc, R.string.tv_prof_n_mid_feature_2_subdesc, R.string.tv_prof_n_mid_feature_3_subdesc, R.string.tv_prof_n_mid_feature_4_subdesc, R.string.tv_prof_n_mid_feature_5_subdesc, R.string.tv_prof_n_mid_feature_6_subdesc, )


            // 開放性
            "O-high" -> ResultDetailActivity.ProfileData("O-high",R.string.tv_prof_o_high_title, R.string.tv_prof_o_high_desc,R.drawable.header_005,
                R.string.tv_prof_o_high_feature_title, R.string.tv_prof_o_high_feature_desc,
                R.string.tv_prof_o_high_feature_1_subtitle, R.string.tv_prof_o_high_feature_2_subtitle, R.string.tv_prof_o_high_feature_3_subtitle, R.string.tv_prof_o_high_feature_4_subtitle, R.string.tv_prof_o_high_feature_5_subtitle, R.string.tv_prof_o_high_feature_6_subtitle,
                R.string.tv_prof_o_high_feature_1_subdesc, R.string.tv_prof_o_high_feature_2_subdesc, R.string.tv_prof_o_high_feature_3_subdesc, R.string.tv_prof_o_high_feature_4_subdesc, R.string.tv_prof_o_high_feature_5_subdesc, R.string.tv_prof_o_high_feature_6_subdesc, )

            "O-low" -> ResultDetailActivity.ProfileData("O-low",R.string.tv_prof_o_low_title, R.string.tv_prof_o_low_desc,R.drawable.header_015,
                R.string.tv_prof_o_low_feature_title, R.string.tv_prof_o_low_feature_desc,
                R.string.tv_prof_o_low_feature_1_subtitle, R.string.tv_prof_o_low_feature_2_subtitle, R.string.tv_prof_o_low_feature_3_subtitle, R.string.tv_prof_o_low_feature_4_subtitle, R.string.tv_prof_o_low_feature_5_subtitle, R.string.tv_prof_o_low_feature_6_subtitle,
                R.string.tv_prof_o_low_feature_1_subdesc, R.string.tv_prof_o_low_feature_2_subdesc, R.string.tv_prof_o_low_feature_3_subdesc, R.string.tv_prof_o_low_feature_4_subdesc, R.string.tv_prof_o_low_feature_5_subdesc, R.string.tv_prof_o_low_feature_6_subdesc, )

            "O-mid" -> ResultDetailActivity.ProfileData("O-mid",R.string.tv_prof_o_mid_title, R.string.tv_prof_o_mid_desc,R.drawable.header_010,
                R.string.tv_prof_o_mid_feature_title, R.string.tv_prof_o_mid_feature_desc,
                R.string.tv_prof_o_mid_feature_1_subtitle, R.string.tv_prof_o_mid_feature_2_subtitle, R.string.tv_prof_o_mid_feature_3_subtitle, R.string.tv_prof_o_mid_feature_4_subtitle, R.string.tv_prof_o_mid_feature_5_subtitle, R.string.tv_prof_o_mid_feature_6_subtitle,
                R.string.tv_prof_o_mid_feature_1_subdesc, R.string.tv_prof_o_mid_feature_2_subdesc, R.string.tv_prof_o_mid_feature_3_subdesc, R.string.tv_prof_o_mid_feature_4_subdesc, R.string.tv_prof_o_mid_feature_5_subdesc, R.string.tv_prof_o_mid_feature_6_subdesc, )

            else -> ResultDetailActivity.ProfileData(
                "E-high",
                R.string.tv_prof_e_high_title,
                R.string.tv_prof_e_high_desc,
                R.drawable.e_high_header,
                R.string.tv_prof_e_high_feature_title,
                R.string.tv_prof_e_high_feature_desc,
                R.string.tv_prof_e_high_feature_1_subtitle, R.string.tv_prof_e_high_feature_2_subtitle, R.string.tv_prof_e_high_feature_3_subtitle, R.string.tv_prof_e_high_feature_4_subtitle, R.string.tv_prof_e_high_feature_5_subtitle, R.string.tv_prof_e_high_feature_6_subtitle,
                R.string.tv_prof_e_high_feature_1_subdesc, R.string.tv_prof_e_high_feature_2_subdesc, R.string.tv_prof_e_high_feature_3_subdesc, R.string.tv_prof_e_high_feature_4_subdesc, R.string.tv_prof_e_high_feature_5_subdesc, R.string.tv_prof_e_high_feature_6_subdesc,
            )


        }


        // タイトル・説明・画像のセットを用意する関数
        fun setPersonalityProfile(
            typelevel: String,
            profTitleText: Int,
            profDescText: Int,
            profImageSrc: Int,
            featureTitleText :Int,
            profileFeatureDescText: Int,
            profileFeature_1_SubtitleText :Int,
            profileFeature_2_SubtitleText :Int,
            profileFeature_3_SubtitleText :Int,
            profileFeature_4_SubtitleText :Int,
            profileFeature_5_SubtitleText :Int,
            profileFeature_6_SubtitleText :Int,
            profileFeature_1_SubDescText: Int,
            profileFeature_2_SubDescText: Int,
            profileFeature_3_SubDescText: Int,
            profileFeature_4_SubDescText: Int,
            profileFeature_5_SubDescText: Int,
            profileFeature_6_SubDescText: Int, ) {

            // あなたのー性を表すパソモンは…
                profileTitle.text = getString(profTitleText)
                // ー性が高いあなたは…
                profileDescription.text = getString(profDescText)
                // 画像をセット
                image.setImageResource(profImageSrc)

                featureTitle.text = getString(featureTitleText)
                profileFeatureDesc.text = getString(profileFeatureDescText)

                profileFeature_1_Subtitle.text = getString(profileFeature_1_SubtitleText)
                profileFeature_2_Subtitle.text = getString(profileFeature_2_SubtitleText)
                profileFeature_3_Subtitle.text = getString(profileFeature_3_SubtitleText)
                profileFeature_4_Subtitle.text = getString(profileFeature_4_SubtitleText)
                profileFeature_5_Subtitle.text = getString(profileFeature_5_SubtitleText)
                profileFeature_6_Subtitle.text = getString(profileFeature_6_SubtitleText)

                profileFeature_1_SubDesc.text = getString(profileFeature_1_SubDescText)
                profileFeature_2_SubDesc.text = getString(profileFeature_2_SubDescText)
                profileFeature_3_SubDesc.text = getString(profileFeature_3_SubDescText)
                profileFeature_4_SubDesc.text = getString(profileFeature_4_SubDescText)
                profileFeature_5_SubDesc.text = getString(profileFeature_5_SubDescText)
                profileFeature_6_SubDesc.text = getString(profileFeature_6_SubDescText)
        }

//        // タイトル・説明・画像のセットを用意する関数
//        fun setPersonalityProfile(
//            typelevel: String, profTitleText: Int, profDescText: Int, profImageSrc: Int,
//            featureTitleText :Int, profileFeatureDescText: Int,
//            profileFeature_1_SubtitleText :Int, profileFeature_2_SubtitleText :Int, profileFeature_3_SubtitleText :Int, profileFeature_4_SubtitleText :Int, profileFeature_5_SubtitleText :Int, profileFeature_6_SubtitleText :Int,
//            profileFeature_1_SubDescText: Int, profileFeature_2_SubDescText: Int, profileFeature_3_SubDescText: Int, profileFeature_4_SubDescText: Int, profileFeature_5_SubDescText: Int, profileFeature_6_SubDescText: Int, ) {
//
//            // あなたのー性を表すパソモンは…
//            profileTitle.text = getString(profTitleText)
//            // ー性が高いあなたは…
//            profileDescription.text = getString(profDescText)
//            // 画像をセット
//            image.setImageResource(profImageSrc)
//
//            featureTitle.text = getString(featureTitleText)
//            profileFeatureDesc.text = getString(profileFeatureDescText)
//
//            profileFeature_1_Subtitle.text = getString(profileFeature_1_SubtitleText)
//            profileFeature_2_Subtitle.text = getString(profileFeature_2_SubtitleText)
//            profileFeature_3_Subtitle.text = getString(profileFeature_3_SubtitleText)
//            profileFeature_4_Subtitle.text = getString(profileFeature_4_SubtitleText)
//            profileFeature_5_Subtitle.text = getString(profileFeature_5_SubtitleText)
//            profileFeature_6_Subtitle.text = getString(profileFeature_6_SubtitleText)
//
//            profileFeature_1_SubDesc.text = getString(profileFeature_1_SubDescText)
//            profileFeature_2_SubDesc.text = getString(profileFeature_2_SubDescText)
//            profileFeature_3_SubDesc.text = getString(profileFeature_3_SubDescText)
//            profileFeature_4_SubDesc.text = getString(profileFeature_4_SubDescText)
//            profileFeature_5_SubDesc.text = getString(profileFeature_5_SubDescText)
//            profileFeature_6_SubDesc.text = getString(profileFeature_6_SubDescText)
//        }

        setPersonalityProfile(
            typelevel = profileDataset.typelevel,
            profTitleText = profileDataset.profTitleText,
            profDescText = profileDataset.profDescText,
            profImageSrc = profileDataset.profImageSrc,
            featureTitleText = profileDataset.featureTitleText,
            profileFeatureDescText = profileDataset.profileFeatureDescText,
            profileFeature_1_SubtitleText = profileDataset.profileFeature_1_SubtitleText,
            profileFeature_2_SubtitleText = profileDataset.profileFeature_2_SubtitleText,
            profileFeature_3_SubtitleText = profileDataset.profileFeature_3_SubtitleText,
            profileFeature_4_SubtitleText = profileDataset.profileFeature_4_SubtitleText,
            profileFeature_5_SubtitleText = profileDataset.profileFeature_5_SubtitleText,
            profileFeature_6_SubtitleText = profileDataset.profileFeature_6_SubtitleText,
            profileFeature_1_SubDescText =  profileDataset.profileFeature_1_SubDescText,
            profileFeature_2_SubDescText=  profileDataset.profileFeature_2_SubDescText,
            profileFeature_3_SubDescText=  profileDataset.profileFeature_3_SubDescText,
            profileFeature_4_SubDescText=  profileDataset.profileFeature_4_SubDescText,
            profileFeature_5_SubDescText=  profileDataset.profileFeature_5_SubDescText,
            profileFeature_6_SubDescText=  profileDataset.profileFeature_6_SubDescText,

            )


        // タイトル・説明・画像を反映
//        when("$type-$level") {
//            // 外向性
//            "E-high" -> setPersonalityProfile(
//                "E-high",
//                R.string.tv_prof_e_high_title,
//                R.string.tv_prof_e_high_desc,
//                R.drawable.e_high_header,
//                R.string.tv_prof_e_high_feature_title,
//                R.string.tv_prof_e_high_feature_desc,
//                R.string.tv_prof_e_high_feature_1_subtitle, R.string.tv_prof_e_high_feature_2_subtitle, R.string.tv_prof_e_high_feature_3_subtitle, R.string.tv_prof_e_high_feature_4_subtitle, R.string.tv_prof_e_high_feature_5_subtitle, R.string.tv_prof_e_high_feature_6_subtitle,
//                R.string.tv_prof_e_high_feature_1_subdesc, R.string.tv_prof_e_high_feature_2_subdesc, R.string.tv_prof_e_high_feature_3_subdesc, R.string.tv_prof_e_high_feature_4_subdesc, R.string.tv_prof_e_high_feature_5_subdesc, R.string.tv_prof_e_high_feature_6_subdesc, )
//
//            "E-low" -> setPersonalityProfile("E-low",R.string.tv_prof_e_low_title, R.string.tv_prof_e_low_desc,R.drawable.e_low_header,
//            R.string.tv_prof_e_low_feature_title, R.string.tv_prof_e_low_feature_desc,
//            R.string.tv_prof_e_low_feature_1_subtitle, R.string.tv_prof_e_low_feature_2_subtitle, R.string.tv_prof_e_low_feature_3_subtitle, R.string.tv_prof_e_low_feature_4_subtitle, R.string.tv_prof_e_low_feature_5_subtitle, R.string.tv_prof_e_low_feature_6_subtitle,
//            R.string.tv_prof_e_low_feature_1_subdesc, R.string.tv_prof_e_low_feature_2_subdesc, R.string.tv_prof_e_low_feature_3_subdesc, R.string.tv_prof_e_low_feature_4_subdesc, R.string.tv_prof_e_low_feature_5_subdesc, R.string.tv_prof_e_low_feature_6_subdesc, )
//
//            "E-mid" -> setPersonalityProfile("E-mid",R.string.tv_prof_e_mid_title, R.string.tv_prof_e_mid_desc,R.drawable.e_high_header,
//            R.string.tv_prof_e_mid_feature_title, R.string.tv_prof_e_mid_feature_desc,
//            R.string.tv_prof_e_mid_feature_1_subtitle, R.string.tv_prof_e_mid_feature_2_subtitle, R.string.tv_prof_e_mid_feature_3_subtitle, R.string.tv_prof_e_mid_feature_4_subtitle, R.string.tv_prof_e_mid_feature_5_subtitle, R.string.tv_prof_e_mid_feature_6_subtitle,
//            R.string.tv_prof_e_mid_feature_1_subdesc, R.string.tv_prof_e_mid_feature_2_subdesc, R.string.tv_prof_e_mid_feature_3_subdesc, R.string.tv_prof_e_mid_feature_4_subdesc, R.string.tv_prof_e_mid_feature_5_subdesc, R.string.tv_prof_e_mid_feature_6_subdesc, )
//
//
//            // 協調性
//            "A-high" -> setPersonalityProfile("A-high",R.string.tv_prof_a_high_title, R.string.tv_prof_a_high_desc,R.drawable.a_high_header,
//                    R.string.tv_prof_a_high_feature_title, R.string.tv_prof_a_high_feature_desc,
//                    R.string.tv_prof_a_high_feature_1_subtitle, R.string.tv_prof_a_high_feature_2_subtitle, R.string.tv_prof_a_high_feature_3_subtitle, R.string.tv_prof_a_high_feature_4_subtitle, R.string.tv_prof_a_high_feature_5_subtitle, R.string.tv_prof_a_high_feature_6_subtitle,
//                    R.string.tv_prof_a_high_feature_1_subdesc, R.string.tv_prof_a_high_feature_2_subdesc, R.string.tv_prof_a_high_feature_3_subdesc, R.string.tv_prof_a_high_feature_4_subdesc, R.string.tv_prof_a_high_feature_5_subdesc, R.string.tv_prof_a_high_feature_6_subdesc, )
//
//            "A-low" -> setPersonalityProfile("A-low",R.string.tv_prof_a_low_title, R.string.tv_prof_a_low_desc,R.drawable.a_low_header,
//                R.string.tv_prof_a_low_feature_title, R.string.tv_prof_a_low_feature_desc,
//                R.string.tv_prof_a_low_feature_1_subtitle, R.string.tv_prof_a_low_feature_2_subtitle, R.string.tv_prof_a_low_feature_3_subtitle, R.string.tv_prof_a_low_feature_4_subtitle, R.string.tv_prof_a_low_feature_5_subtitle, R.string.tv_prof_a_low_feature_6_subtitle,
//                R.string.tv_prof_a_low_feature_1_subdesc, R.string.tv_prof_a_low_feature_2_subdesc, R.string.tv_prof_a_low_feature_3_subdesc, R.string.tv_prof_a_low_feature_4_subdesc, R.string.tv_prof_a_low_feature_5_subdesc, R.string.tv_prof_a_low_feature_6_subdesc, )
//            "A-mid" -> setPersonalityProfile("A-mid",R.string.tv_prof_a_mid_title, R.string.tv_prof_a_mid_desc,R.drawable.a_high_header,
//                R.string.tv_prof_a_mid_feature_title, R.string.tv_prof_a_mid_feature_desc,
//                R.string.tv_prof_a_mid_feature_1_subtitle, R.string.tv_prof_a_mid_feature_2_subtitle, R.string.tv_prof_a_mid_feature_3_subtitle, R.string.tv_prof_a_mid_feature_4_subtitle, R.string.tv_prof_a_mid_feature_5_subtitle, R.string.tv_prof_a_mid_feature_6_subtitle,
//                R.string.tv_prof_a_mid_feature_1_subdesc, R.string.tv_prof_a_mid_feature_2_subdesc, R.string.tv_prof_a_mid_feature_3_subdesc, R.string.tv_prof_a_mid_feature_4_subdesc, R.string.tv_prof_a_mid_feature_5_subdesc, R.string.tv_prof_a_mid_feature_6_subdesc, )
//
//
//            // 勤勉性
//            "C-high" -> setPersonalityProfile("C-high",R.string.tv_prof_c_high_title, R.string.tv_prof_c_high_desc,R.drawable.c_high_header,
//                    R.string.tv_prof_c_high_feature_title, R.string.tv_prof_c_high_feature_desc,
//                    R.string.tv_prof_c_high_feature_1_subtitle, R.string.tv_prof_c_high_feature_2_subtitle, R.string.tv_prof_c_high_feature_3_subtitle, R.string.tv_prof_c_high_feature_4_subtitle, R.string.tv_prof_c_high_feature_5_subtitle, R.string.tv_prof_c_high_feature_6_subtitle,
//                    R.string.tv_prof_c_high_feature_1_subdesc, R.string.tv_prof_c_high_feature_2_subdesc, R.string.tv_prof_c_high_feature_3_subdesc, R.string.tv_prof_c_high_feature_4_subdesc, R.string.tv_prof_c_high_feature_5_subdesc, R.string.tv_prof_c_high_feature_6_subdesc, )
//
//            "C-low" -> setPersonalityProfile("C-low",R.string.tv_prof_c_low_title, R.string.tv_prof_c_low_desc,R.drawable.c_low_header,
//                R.string.tv_prof_c_low_feature_title, R.string.tv_prof_c_low_feature_desc,
//                R.string.tv_prof_c_low_feature_1_subtitle, R.string.tv_prof_c_low_feature_2_subtitle, R.string.tv_prof_c_low_feature_3_subtitle, R.string.tv_prof_c_low_feature_4_subtitle, R.string.tv_prof_c_low_feature_5_subtitle, R.string.tv_prof_c_low_feature_6_subtitle,
//                R.string.tv_prof_c_low_feature_1_subdesc, R.string.tv_prof_c_low_feature_2_subdesc, R.string.tv_prof_c_low_feature_3_subdesc, R.string.tv_prof_c_low_feature_4_subdesc, R.string.tv_prof_c_low_feature_5_subdesc, R.string.tv_prof_c_low_feature_6_subdesc, )
//
//            "C-mid" -> setPersonalityProfile("C-mid",R.string.tv_prof_c_mid_title, R.string.tv_prof_c_mid_desc,R.drawable.c_high_header,
//                R.string.tv_prof_c_mid_feature_title, R.string.tv_prof_c_mid_feature_desc,
//                R.string.tv_prof_c_mid_feature_1_subtitle, R.string.tv_prof_c_mid_feature_2_subtitle, R.string.tv_prof_c_mid_feature_3_subtitle, R.string.tv_prof_c_mid_feature_4_subtitle, R.string.tv_prof_c_mid_feature_5_subtitle, R.string.tv_prof_c_mid_feature_6_subtitle,
//                R.string.tv_prof_c_mid_feature_1_subdesc, R.string.tv_prof_c_mid_feature_2_subdesc, R.string.tv_prof_c_mid_feature_3_subdesc, R.string.tv_prof_c_mid_feature_4_subdesc, R.string.tv_prof_c_mid_feature_5_subdesc, R.string.tv_prof_c_mid_feature_6_subdesc, )
//
//
//            // 情緒安定性
//            "N-high" -> setPersonalityProfile("N-high",R.string.tv_prof_n_high_title, R.string.tv_prof_n_high_desc,R.drawable.n_high_header,
//                R.string.tv_prof_n_high_feature_title, R.string.tv_prof_n_high_feature_desc,
//                R.string.tv_prof_n_high_feature_1_subtitle, R.string.tv_prof_n_high_feature_2_subtitle, R.string.tv_prof_n_high_feature_3_subtitle, R.string.tv_prof_n_high_feature_4_subtitle, R.string.tv_prof_n_high_feature_5_subtitle, R.string.tv_prof_n_high_feature_6_subtitle,
//                R.string.tv_prof_n_high_feature_1_subdesc, R.string.tv_prof_n_high_feature_2_subdesc, R.string.tv_prof_n_high_feature_3_subdesc, R.string.tv_prof_n_high_feature_4_subdesc, R.string.tv_prof_n_high_feature_5_subdesc, R.string.tv_prof_n_high_feature_6_subdesc, )
//
//            "N-low" -> setPersonalityProfile("N-low",R.string.tv_prof_n_low_title, R.string.tv_prof_n_low_desc,R.drawable.n_low_header,
//                R.string.tv_prof_n_low_feature_title, R.string.tv_prof_n_low_feature_desc,
//                R.string.tv_prof_n_low_feature_1_subtitle, R.string.tv_prof_n_low_feature_2_subtitle, R.string.tv_prof_n_low_feature_3_subtitle, R.string.tv_prof_n_low_feature_4_subtitle, R.string.tv_prof_n_low_feature_5_subtitle, R.string.tv_prof_n_low_feature_6_subtitle,
//                R.string.tv_prof_n_low_feature_1_subdesc, R.string.tv_prof_n_low_feature_2_subdesc, R.string.tv_prof_n_low_feature_3_subdesc, R.string.tv_prof_n_low_feature_4_subdesc, R.string.tv_prof_n_low_feature_5_subdesc, R.string.tv_prof_n_low_feature_6_subdesc, )
//            "N-mid" -> setPersonalityProfile("N-mid",R.string.tv_prof_n_mid_title, R.string.tv_prof_n_mid_desc,R.drawable.n_high_header,
//                R.string.tv_prof_n_mid_feature_title, R.string.tv_prof_n_mid_feature_desc,
//                R.string.tv_prof_n_mid_feature_1_subtitle, R.string.tv_prof_n_mid_feature_2_subtitle, R.string.tv_prof_n_mid_feature_3_subtitle, R.string.tv_prof_n_mid_feature_4_subtitle, R.string.tv_prof_n_mid_feature_5_subtitle, R.string.tv_prof_n_mid_feature_6_subtitle,
//                R.string.tv_prof_n_mid_feature_1_subdesc, R.string.tv_prof_n_mid_feature_2_subdesc, R.string.tv_prof_n_mid_feature_3_subdesc, R.string.tv_prof_n_mid_feature_4_subdesc, R.string.tv_prof_n_mid_feature_5_subdesc, R.string.tv_prof_n_mid_feature_6_subdesc, )
//
//
//            // 開放性
//            "O-high" -> setPersonalityProfile("O-high",R.string.tv_prof_o_high_title, R.string.tv_prof_o_high_desc,R.drawable.o_high_header,
//                R.string.tv_prof_o_high_feature_title, R.string.tv_prof_o_high_feature_desc,
//                R.string.tv_prof_o_high_feature_1_subtitle, R.string.tv_prof_o_high_feature_2_subtitle, R.string.tv_prof_o_high_feature_3_subtitle, R.string.tv_prof_o_high_feature_4_subtitle, R.string.tv_prof_o_high_feature_5_subtitle, R.string.tv_prof_o_high_feature_6_subtitle,
//                R.string.tv_prof_o_high_feature_1_subdesc, R.string.tv_prof_o_high_feature_2_subdesc, R.string.tv_prof_o_high_feature_3_subdesc, R.string.tv_prof_o_high_feature_4_subdesc, R.string.tv_prof_o_high_feature_5_subdesc, R.string.tv_prof_o_high_feature_6_subdesc, )
//
//            "O-low" -> setPersonalityProfile("O-low",R.string.tv_prof_o_low_title, R.string.tv_prof_o_low_desc,R.drawable.o_low_header,
//                R.string.tv_prof_o_low_feature_title, R.string.tv_prof_o_low_feature_desc,
//                R.string.tv_prof_o_low_feature_1_subtitle, R.string.tv_prof_o_low_feature_2_subtitle, R.string.tv_prof_o_low_feature_3_subtitle, R.string.tv_prof_o_low_feature_4_subtitle, R.string.tv_prof_o_low_feature_5_subtitle, R.string.tv_prof_o_low_feature_6_subtitle,
//                R.string.tv_prof_o_low_feature_1_subdesc, R.string.tv_prof_o_low_feature_2_subdesc, R.string.tv_prof_o_low_feature_3_subdesc, R.string.tv_prof_o_low_feature_4_subdesc, R.string.tv_prof_o_low_feature_5_subdesc, R.string.tv_prof_o_low_feature_6_subdesc, )
//
//            "O-mid" -> setPersonalityProfile("O-mid",R.string.tv_prof_o_mid_title, R.string.tv_prof_o_mid_desc,R.drawable.o_high_header,
//                R.string.tv_prof_o_mid_feature_title, R.string.tv_prof_o_mid_feature_desc,
//                R.string.tv_prof_o_mid_feature_1_subtitle, R.string.tv_prof_o_mid_feature_2_subtitle, R.string.tv_prof_o_mid_feature_3_subtitle, R.string.tv_prof_o_mid_feature_4_subtitle, R.string.tv_prof_o_mid_feature_5_subtitle, R.string.tv_prof_o_mid_feature_6_subtitle,
//                R.string.tv_prof_o_mid_feature_1_subdesc, R.string.tv_prof_o_mid_feature_2_subdesc, R.string.tv_prof_o_mid_feature_3_subdesc, R.string.tv_prof_o_mid_feature_4_subdesc, R.string.tv_prof_o_mid_feature_5_subdesc, R.string.tv_prof_o_mid_feature_6_subdesc, )
//        }


//        btnResearchLink.setOnClickListener {
//            // 外部URLに飛ぶ
//            var url = ""
//            url = when(type) {
//                "E" -> { "https://mind-read.info/archives/category/personality/extroversion-personality" }
//                "A" -> { "https://mind-read.info/archives/category/personality/agreeableness-personality" }
//                "C" -> { "https://mind-read.info/archives/category/personality/conscientiousness-personality" }
//                "N" -> { "https://mind-read.info/archives/category/personality/neuroticism-personality" }
//                "O" -> { "https://mind-read.info/archives/category/personality/openness-personality" }
//                else -> { "" }
//            }
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//            startActivity(intent)
//        }

        // 診断結果の共有処理
        btnShare.setOnClickListener {
            shareMonster()
        }

        btnBackToList.setOnClickListener { finish() }

    }

    private fun shareMonster() {
        val imageUri = getImageUri()

        val profileTitle = getString(profileDataset.profTitleText)
        val shareText = "${profileTitle}という診断でした！\n#パソモン\n#性格をモンスター化"



        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            setType("text/plain")
        }
        startActivity(Intent.createChooser(shareIntent, "シェアする"))
    }



    // 画像をファイルとして保存する処理
    private fun getImageUri(): Uri {
        val bitmap = BitmapFactory.decodeResource(resources, profileDataset.profImageSrc)

        val file = File(cacheDir, "share_monsterImage.png")

        val outputStream = FileOutputStream(file)
        bitmap.compress(
            Bitmap.CompressFormat.PNG,
            100,
            outputStream
        )

//        outputStream.flush()
//        outputStream.close()

        return FileProvider.getUriForFile(
            this@ResultDetailActivity,
            "${packageName}.provider",
            file
        )
    }
}