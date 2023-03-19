package com.example.random_number

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.random_number.databinding.ActivityMainBinding
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.util.*
import kotlin.collections.ArrayList
import java.io.IOException
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        var view = binding.root
        setContentView(view)


        //로또 번호 생성 버튼
            binding.btn.setOnClickListener {

            if (binding.edit1.text.isEmpty() ||
                binding.edit2.text.isEmpty() ||
                binding.edit3.text.isEmpty() ||
                binding.edit4.text.isEmpty() ||
                binding.edit5.text.isEmpty()
            ) {
                Toast.makeText(this, "0또는 숫자를 입력하세요.", Toast.LENGTH_SHORT).show()
            } else {

                editRandomNumber(binding)

                val text1 = binding.edit1.text?.toString()?.toInt()
                val text2 = binding.edit2.text?.toString()?.toInt()
                val text3 = binding.edit3.text?.toString()?.toInt()
                val text4 = binding.edit4.text?.toString()?.toInt()
                val text5 = binding.edit5.text?.toString()?.toInt()

                findAllDuplicates(editRandomNumber(binding))
                val findlist = findAllDuplicates(editRandomNumber(binding)).toList()

                if (findlist.size > 0) {
                    Toast.makeText(this, "중복된 숫자가 있습니다.", Toast.LENGTH_SHORT).show()
                } else {

                    if (text1!! > 45 || text2!! > 45 || text3!! > 45 || text4!! > 45 || text5!! > 45) {
                        Toast.makeText(this, "1~45사이의 숫자를 입력하세요.", Toast.LENGTH_SHORT).show()
                    } else {
                        val randomNumbers = randomNumber()
                        val randomNumbers2 = randomNumber2(binding)
                        if (editRandomNumber(binding).size > 0)
                            updateLottoBall(randomNumbers2, binding) else updateLottoBall(
                            randomNumbers,
                            binding
                        )
                    }
                }
            }
        }
        //로또 선택 번호 리셋 버튼
        binding.resetbtn.setOnClickListener {
            val number = 0
            binding.edit1.setText(number.toString())
            binding.edit2.setText(number.toString())
            binding.edit3.setText(number.toString())
            binding.edit4.setText(number.toString())
            binding.edit5.setText(number.toString())
        }
        //로또 당첨 번호 조회 버튼
        binding.winbtn.setOnClickListener {

            val edittext = binding.wintext.text.toString()
            val number = edittext.toIntOrNull()


            if (edittext.isEmpty()) {
                Toast.makeText(this, "당첨번호를 조회할 회차를 입력하세요.", Toast.LENGTH_SHORT).show()
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val lastnumber = abc()
                    if (number!! > lastnumber.toInt()  || number!! == 0) {
                        withContext(Dispatchers.Main){
                            Toast.makeText(applicationContext, "마지막 회차는 ${lastnumber} 입니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val wintext = binding.wintext.text
                        withContext(Dispatchers.Main) {
                            binding.textView2.text = "${wintext}회차 당첨번호"
                        }
                            val win = async { winNumber(binding) }
                            val text = "${win.await()}"
                            println(text)
                            val winlist = winNumber(binding)
                            winLottoBall(winlist, binding)
                    }
                }
            }


        }

    }
    //로또 당첨회차 받아오기
    fun abc(): String {
        val url = "https://dhlottery.co.kr/gameResult.do?method=byWin"
        val doc = Jsoup.connect(url).get()
        val lastRound = doc.select("h4")
        val Roundtext = lastRound.toString().replace("h4", "")
        val number = Roundtext.replace("[^0-9]".toRegex(), "")
        return number

    }

    //로또 로또 당첨번호 받아오기
    private fun winNumber(binding: ActivityMainBinding): ArrayList<Int> {

        val editText = binding.wintext.text

        val round = editText.toString()
        val url = "https://dhlottery.co.kr/common.do?method=getLottoNumber&drwNo=$round"
        val lottoNumbers = ArrayList<Int>()

        try {
            val response = URL(url).readText()
            val jsonObject = JsonParser.parseString(response).asJsonObject
            val returnValue = jsonObject.get("returnValue").asString

            if (returnValue == "success") {
                for (i in 1..6) {
                    val lottoNumber = jsonObject.get("drwtNo$i").asInt
                    lottoNumbers.add(lottoNumber)
                }
                val bonusNumber = jsonObject.get("bnusNo").asInt
                lottoNumbers.add(bonusNumber)
//                lottoNumbers.add(round.toInt())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return lottoNumbers

    }
    //번호 중복 값 찾기
    private fun findAllDuplicates(array: ArrayList<Int>): Set<Int> {
        val seen: MutableSet<Int> = mutableSetOf()
        return array.filter { !seen.add(it) }.toSet()
    }

    //번호 입력한 값 리스트
    private fun editRandomNumber(binding: ActivityMainBinding): ArrayList<Int> {

        val editlist = ArrayList<Int>()

        val text1 = binding.edit1.text?.toString()?.toInt()
        val text2 = binding.edit2.text?.toString()?.toInt()
        val text3 = binding.edit3.text?.toString()?.toInt()
        val text4 = binding.edit4.text?.toString()?.toInt()
        val text5 = binding.edit5.text?.toString()?.toInt()


        if (binding.edit1.text.isNotEmpty()) editlist.add(text1!!) else editlist.add(0)
        if (binding.edit2.text.isNotEmpty()) editlist.add(text2!!) else editlist.add(0)
        if (binding.edit3.text.isNotEmpty()) editlist.add(text3!!) else editlist.add(0)
        if (binding.edit4.text.isNotEmpty()) editlist.add(text4!!) else editlist.add(0)
        if (binding.edit5.text.isNotEmpty()) editlist.add(text5!!) else editlist.add(0)

        editlist.removeAll(listOf(0))



        println(editlist)

        return editlist


    }

    private fun getDrawableID(number: Int): Int {
        val number = String.format("%02d", number)
        val string = "ball_${number}"
        val id = resources.getIdentifier(string, "drawable", packageName)
        return id
    }

    //당첨번호 이미지 생성
    private fun winLottoBall(result: ArrayList<Int>, binding: ActivityMainBinding) {
        binding.winnumber1.setImageResource(getDrawableID(result[0]))
        binding.winnumber2.setImageResource(getDrawableID(result[1]))
        binding.winnumber3.setImageResource(getDrawableID(result[2]))
        binding.winnumber4.setImageResource(getDrawableID(result[3]))
        binding.winnumber5.setImageResource(getDrawableID(result[4]))
        binding.winnumber6.setImageResource(getDrawableID(result[5]))
        binding.winnumber7.setImageResource(getDrawableID(result[6]))
    }

    //로또 번호 이미지 생성
    private fun updateLottoBall(result: ArrayList<Int>, binding: ActivityMainBinding) {
        binding.number1.setImageResource(getDrawableID(result[0]))
        binding.number2.setImageResource(getDrawableID(result[1]))
        binding.number3.setImageResource(getDrawableID(result[2]))
        binding.number4.setImageResource(getDrawableID(result[3]))
        binding.number5.setImageResource(getDrawableID(result[4]))
        binding.number6.setImageResource(getDrawableID(result[5]))
    }

    //로또 반자동 리스트 생성
    private fun randomNumber2(binding: ActivityMainBinding): ArrayList<Int> {

        val index = 6 - editRandomNumber(binding).size
        val addList: MutableList<Int> = ArrayList()

        val list2 = arrayListOf<Int>().apply {
            for (i in 1..45) {
                this.add(i)
            }
        }
        list2.removeAll(editRandomNumber(binding))
        list2.shuffle()
        addList.addAll(list2.subList(0, index))
        addList.addAll(editRandomNumber(binding))
        addList.sort()

        return addList as ArrayList<Int>

    }

    //로또번호 랜덤 리스트 생성
    private fun randomNumber(): ArrayList<Int> {

        val list = arrayListOf<Int>().apply {
            for (i in 1..45) {
                this.add(i)
            }
        }
        list.shuffle()
        list.subList(0, 6).sort()
        val newlist = list.subList(0, 6)
        println(newlist)

        return list
    }
}