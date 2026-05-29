package com.dino.personalmonster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {
    private var eTotal = 0
    private var aTotal = 0
    private var cTotal = 0
    private var nTotal = 0
    private var oTotal = 0

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("results")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        eTotal = (document.getLong("外向性(初期値)") ?: 0).toInt()
                        aTotal = (document.getLong("協調性(初期値)") ?: 0).toInt()
                        cTotal = (document.getLong("勤勉性(初期値)") ?: 0).toInt()
                        nTotal = (document.getLong("情緒安定性(初期値)") ?: 0).toInt()
                        oTotal = (document.getLong("開放性(初期値)") ?: 0).toInt()
                    }


                    val tvEtitle = findViewById<TextView>(R.id.tvETitle)
                    val tvAtitle = findViewById<TextView>(R.id.tvATitle)
                    val tvCtitle = findViewById<TextView>(R.id.tvCTitle)
                    val tvNtitle = findViewById<TextView>(R.id.tvNTitle)
                    val tvOtitle = findViewById<TextView>(R.id.tvOTitle)

                    val tvEDesc = findViewById<TextView>(R.id.tvEDesc)
                    val tvADesc = findViewById<TextView>(R.id.tvADesc)
                    val tvCDesc = findViewById<TextView>(R.id.tvCDesc)
                    val tvNDesc = findViewById<TextView>(R.id.tvNDesc)
                    val tvODesc = findViewById<TextView>(R.id.tvODesc)

                    fun resultShow(total: Int, title: TextView, desc: TextView, stringHighTitle: Int, stringLowTitle: Int, stringHighDesc: Int, stringLowDesc: Int) {
                        if (total > 3) {
                            title.text = getString(stringHighTitle)
                            desc.text = getString(stringHighDesc)
                        } else {
                            title.text = getString(stringLowTitle)
                            desc.text = getString(stringLowDesc)
                        }
                    }

                    resultShow(eTotal, tvEtitle, tvEDesc, R.string.tv_resultHighTitleE, R.string.tv_resultLowTitleE, R.string.tv_resultHighDescE,R.string.tv_resultLowDescE)
                    resultShow(aTotal, tvAtitle, tvADesc, R.string.tv_resultHighTitleA, R.string.tv_resultLowTitleA, R.string.tv_resultHighDescA,R.string.tv_resultLowDescA)
                    resultShow(cTotal, tvCtitle, tvCDesc, R.string.tv_resultHighTitleC, R.string.tv_resultLowTitleC, R.string.tv_resultHighDescC,R.string.tv_resultLowDescC)
                    resultShow(nTotal, tvNtitle, tvNDesc, R.string.tv_resultHighTitleN, R.string.tv_resultLowTitleN, R.string.tv_resultHighDescN,R.string.tv_resultLowDescN)
                    resultShow(oTotal, tvOtitle, tvODesc, R.string.tv_resultHighTitleO, R.string.tv_resultLowTitleO, R.string.tv_resultHighDescO,R.string.tv_resultLowDescO)
//                    Log.i("tag", "プロフィールに表示したのは${eTotal}, ${aTotal}, ${cTotal}, ${nTotal}, ${oTotal}")

                }
                .addOnFailureListener { e ->
//                    Log.i("tag", "データ取得失敗： ${e.message}")
                }
        }


        // 性格の詳細へのページ
        val btToAlalysisE = findViewById<Button>(R.id.btToAnalysisE)
        val btToAlalysisA = findViewById<Button>(R.id.btToAnalysisA)
        val btToAlalysisC = findViewById<Button>(R.id.btToAnalysisC)
        val btToAlalysisN = findViewById<Button>(R.id.btToAnalysisN)
        val btToAlalysisO = findViewById<Button>(R.id.btToAnalysisO)

        btToAlalysisE.setOnClickListener { openAnalysisPage("E", eTotal)}
        btToAlalysisA.setOnClickListener { openAnalysisPage("A", aTotal)}
        btToAlalysisC.setOnClickListener { openAnalysisPage("C",cTotal)}
        btToAlalysisN.setOnClickListener { openAnalysisPage("N", nTotal)}
        btToAlalysisO.setOnClickListener { openAnalysisPage("O", oTotal)}
    }

//    private fun checkLevel(total) {
//        if(eTotal > 3) {
//            val level = "high"
//        }
//    }

    private fun openAnalysisPage(type: String, total: Int) {
        val score = total

        val intent = Intent(this, ResultDetailActivity::class.java)
        intent.putExtra("type", type)
        intent.putExtra("score", score)


        startActivity(intent)
    }


}
