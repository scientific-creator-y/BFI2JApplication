package com.dino.personalmonster

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import com.dino.personalmonster.databinding.ActivityShareLayoutBinding
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

class ShareLayoutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShareLayoutBinding

    private var eInitial: Float = 0f
    private var aInitial: Float = 0f
    private var cInitial: Float = 0f
    private var nInitial: Float = 0f
    private var oInitial: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)


        binding = ActivityShareLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 渡ってきた初期値を取得
        eInitial = intent.getFloatExtra(DiagnosisActivity.Trait.E.name, 0f)
        aInitial = intent.getFloatExtra(DiagnosisActivity.Trait.A.name, 0f)
        cInitial = intent.getFloatExtra(DiagnosisActivity.Trait.C.name, 0f)
        nInitial = intent.getFloatExtra(DiagnosisActivity.Trait.N.name, 0f)
        oInitial = intent.getFloatExtra(DiagnosisActivity.Trait.O.name, 0f)

        // レーダーチャート作成
        createInitialRadarChart()

        // テキストを設定

        // Viewの描画後にシェア機能を動かす
        binding.shareRoot.post {
            shareAsImage()
        }
    }


    private fun createInitialRadarChart() {
        // レーダーチャートを取得
        val radarChart = findViewById<RadarChart>(R.id.shareRadarChart)
        // 描画される前にリセット
        radarChart.clear()
        radarChart.setExtraOffsets(40f, 40f, 40f, 40f)


        val eDisplay = (eInitial* 10).roundToInt().toFloat()
        val aDisplay = (aInitial* 10).roundToInt().toFloat()
        val cDisplay = (cInitial* 10).roundToInt().toFloat()
        val nDisplay = (nInitial* 10).roundToInt().toFloat()
        val oDisplay = (oInitial* 10).roundToInt().toFloat()

        val entry = listOf(
            RadarEntry(eDisplay),
            RadarEntry(aDisplay),
            RadarEntry(cDisplay),
            RadarEntry(nDisplay),
            RadarEntry(oDisplay)
        )

        // データセットを作成
        val dataset = RadarDataSet(entry, "").apply {
            color = ContextCompat.getColor(this@ShareLayoutActivity, R.color.radarInitialColor)
            fillColor = ContextCompat.getColor(this@ShareLayoutActivity, R.color.radarInitialFillColor)
            fillAlpha = 120
            setDrawFilled(true)

            lineWidth = 2.5f
            valueTextSize = 12f
            valueTextColor = Color.DKGRAY
        }

        // 小数点削除
        dataset.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String? {
                return value.toInt().toString()
            }
        }


        // データセットをデータに流し込む
        val radarData = RadarData(dataset)
        radarChart.data = radarData

        // X軸(ラベル)の設定
        val labels = listOf("社交力", "調和力", "意志力", "精神力", "探究力")
        radarChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        radarChart.xAxis.textSize = 12f
        radarChart.xAxis.textColor = Color.DKGRAY

        // Y軸の設定
        radarChart.yAxis.apply {
            axisMinimum = 0f
            axisMaximum = 50f
            setDrawLabels(false)
        }


        // 網目デザインの設定
        radarChart.webLineWidth = 1f
        radarChart.webColor = Color.LTGRAY
        radarChart.webLineWidthInner = 1f
        radarChart.webColorInner = Color.LTGRAY
        radarChart.webAlpha = 100

        // その他設定
        radarChart.isRotationEnabled = false
        radarChart.description.isEnabled = false
        radarChart.legend.isEnabled = false


        // 描画を更新する
        radarChart.invalidate()

    }

    // 実際にシェアする処理
    private fun shareAsImage() {

        // Bitmap画像の取得
        val bitmap = getBitmapFromView(binding.shareRoot)

        // 共有可能なファイルとして変換・保存
        val uri = saveBitmap(bitmap)

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/png"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.putExtra(Intent.EXTRA_TEXT, "私の診断結果です！\n#パソモン")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        startActivity(Intent.createChooser(intent, "シェアする"))

        finish()

    }


    // BitMap画像を作る処理
    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(
            view.width,
            view.height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        view.draw(canvas)

        return bitmap
    }

    // 画像をファイルとして保存する処理
    private fun saveBitmap(bitmap: Bitmap): Uri {
        val file = File(cacheDir, "share_image.png")

        val outputStream = FileOutputStream(file)
        bitmap.compress(
            Bitmap.CompressFormat.PNG,
            100,
            outputStream
        )

        outputStream.flush()
        outputStream.close()

        return FileProvider.getUriForFile(
            this@ShareLayoutActivity,
            "${packageName}.provider",
            file
        )
    }
 }