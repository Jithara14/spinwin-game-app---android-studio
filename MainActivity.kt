package com.example.spingame

import androidx.appcompat.app.AppCompatActivity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.spingame.R
import java.util.Random

class MainActivity : AppCompatActivity() {

    private lateinit var wheel: ImageView
    private lateinit var arrow: ImageView
    private lateinit var caption: TextView
    private lateinit var startButton: Button // New button for starting spinning
    private lateinit var totalPointsTextView: TextView // TextView to display total points
    private lateinit var totalPrizeTextView: TextView // TextView to display total prize
    private var rotation = 0
    private var rotationSpeed = 5
    private val stopPosition = intArrayOf(720, 780, 840, 900, 960, 1020)
    private val winPoints = intArrayOf(50, 10, 20, 100, 90, 70)
    private val moneyPrizes = intArrayOf(1000, 500, 700, 2000, 1500, 1800)
    private val prizeProbabilities = intArrayOf(10, 20, 15, 5, 25, 25) // Probabilities for each prize
    private var randPosition = 0
    private var spinCount = 0
    private var totalMoney = 0
    private var totalPoints = 0
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wheel = findViewById(R.id.wheel)
        arrow = findViewById(R.id.arrow)
        caption = findViewById(R.id.caption)
        startButton = findViewById(R.id.startButton) // Initialize the new button
        totalPointsTextView = findViewById(R.id.totalPointsTextView) // Initialize total points TextView
        totalPrizeTextView = findViewById(R.id.totalPrizeTextView) // Initialize total prize TextView

        // Initialize shared preferences
        prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // Restore totalMoney from shared preferences
        totalMoney = prefs.getInt("totalMoney", 0)
        totalPointsTextView.text = "count = $spinCount" // Set initial total points
        totalPrizeTextView.text = "$ $totalMoney" // Set initial total prize

        // Set click listener for the startButton
        startButton.setOnClickListener {
            if (spinCount < 5) {
                randPosition = getRandomPosition()
                startSpin()
                spinCount++
                totalPointsTextView.text = "count = $spinCount" // Update total points TextView
                if (spinCount == 5) {
                    startButton.isEnabled = false
                }
                caption.visibility = View.GONE
            } else {
                if (totalMoney > 4000) {
                    showCongratulations()
                } else {
                    showBadLuck()
                }
                // Reset spin count after every 5 spins
                if (spinCount % 5 == 0) {
                    spinCount = 0
                    totalPoints = 0 // Reset total points after every 5 spins
                    totalPointsTextView.text = "count = $spinCount" // Update total points TextView
                    totalPrizeTextView.text = "$ 0" // Set prize value to 0 after every 5 spins
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Save totalMoney to shared preferences when the activity is stopped
        prefs.edit().putInt("totalMoney", totalMoney).apply()
    }

    private fun startSpin() {
        Handler().postDelayed({
            wheel.rotation = rotation.toFloat()
            if (rotation >= 300) {
                rotationSpeed = 4
            }
            if (rotation >= 400) {
                rotationSpeed = 3
            }
            if (rotation >= 500) {
                rotationSpeed = 2
            }
            rotation += rotationSpeed
            if (rotation >= stopPosition[randPosition]) {
                showPopup(winPoints[randPosition], moneyPrizes[randPosition])
            } else {
                startSpin()
            }
        }, 1)
    }

    private fun showPopup(points: Int, moneyPrize: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.win_popup)
        dialog.setCancelable(true)
        dialog.show()

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setGravity(Gravity.CENTER)

        val winText = dialog.findViewById<TextView>(R.id.win_text)
        winText.text = " $points points and $moneyPrize Rs"

        totalMoney += moneyPrize
        totalPoints += points
        totalPointsTextView.text = "count = $spinCount" // Update total points TextView
        totalPrizeTextView.text = "$ $totalMoney" // Update total prize TextView

        val btn = dialog.findViewById<Button>(R.id.button)
        btn.setOnClickListener {
            dialog.cancel()
            rotation = 0
            rotationSpeed = 5
            randPosition = 0
            if (spinCount == 5) {
                if (totalMoney > 5000) {
                    showCongratulations()
                } else {
                    showBadLuck()
                }
                // Reset total points after 5th spin
                totalMoney = 0
                totalPoints = 0
                totalPointsTextView.text = "count = $spinCount"
                totalPrizeTextView.text = "$ 0"
            }
        }
    }

    private fun showBadLuck() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.win_popup)
        dialog.setCancelable(true)
        dialog.show()

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setGravity(Gravity.CENTER)

        val winText = dialog.findViewById<TextView>(R.id.win_text)
        winText.text = "😞 Bad luck, try again! 😞"

        val btn = dialog.findViewById<Button>(R.id.button)
        btn.text = "Spin More"
        btn.setOnClickListener {
            dialog.cancel()
        }
    }

    private fun showCongratulations() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.win_popup)
        dialog.setCancelable(true)
        dialog.show()

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setGravity(Gravity.CENTER)

        val winText = dialog.findViewById<TextView>(R.id.win_text)
        winText.text = "🎉🥳 Congratulations!! You won total prize of $ $totalMoney  🎉🥳"

        val btn = dialog.findViewById<Button>(R.id.button)
        btn.setOnClickListener {
            dialog.cancel()
            spinCount = 0
            totalMoney = 0
            startButton.isEnabled = true
            caption.visibility = View.VISIBLE // Show caption again after all spins are completed
        }
    }

    private fun getRandomPosition(): Int {
        val totalProbability = prizeProbabilities.sum()
        val randomValue = Random().nextInt(totalProbability) // Generate a random value
        var cumulativeProbability = 0
        for (i in prizeProbabilities.indices) {
            cumulativeProbability += prizeProbabilities[i]
            if (randomValue < cumulativeProbability) {
                return i // Return the index of the prize based on the random value
            }
        }
        return 0
    }

    // Function to navigate to SecondActivity
    private fun navigateToSecondActivity() {
        val intent = Intent(this, SecondActivity::class.java)
        startActivity(intent)
    }
}

class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        // You can add further initialization or logic for SecondActivity here
    }
}
