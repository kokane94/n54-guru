package com.example.n54guru.services

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import com.example.n54guru.models.*
import java.util.*

class VoiceCommentaryService(private val context: Context) : OnInitListener {
    private lateinit var textToSpeech: TextToSpeech
    private var selectedAccent = VoiceAccent.HORI_MAORI_NZ
    private var isInitialized = false

    init {
        textToSpeech = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
        }
    }

    fun setAccent(accent: VoiceAccent) {
        selectedAccent = accent
        configureVoiceForAccent(accent)
    }

    private fun configureVoiceForAccent(accent: VoiceAccent) {
        val locale = when (accent) {
            VoiceAccent.HORI_MAORI_NZ -> Locale("mi", "NZ")
            VoiceAccent.PROUD_AUSSIE -> Locale("en", "AU")
            VoiceAccent.AUSSIE_LEBO_WOG -> Locale("en", "AU")
            VoiceAccent.SCOTTISH_BLOKE -> Locale("en", "GB")
            VoiceAccent.BRITISH_POSH -> Locale("en", "GB")
            VoiceAccent.IRISH_CHARM -> Locale("en", "IE")
            VoiceAccent.SOUTH_AFRICAN_BOER -> Locale("af", "ZA")
            VoiceAccent.AMERICAN_SOUTHERN -> Locale("en", "US")
            VoiceAccent.AMERICAN_NEW_YORK -> Locale("en", "US")
            VoiceAccent.CANADIAN_MAPLE -> Locale("en", "CA")
            VoiceAccent.KIWI_BLOKE -> Locale("en", "NZ")
            VoiceAccent.GERMAN_BOSS -> Locale("de", "DE")
            else -> Locale.ENGLISH
        }

        if (isInitialized) {
            textToSpeech.language = locale
            when (accent) {
                VoiceAccent.HORI_MAORI_NZ -> {
                    textToSpeech.setPitch(0.85f)
                    textToSpeech.setSpeechRate(0.9f)
                }
                VoiceAccent.PROUD_AUSSIE -> {
                    textToSpeech.setPitch(0.9f)
                    textToSpeech.setSpeechRate(0.95f)
                }
                VoiceAccent.AUSSIE_LEBO_WOG -> {
                    textToSpeech.setPitch(1.0f)
                    textToSpeech.setSpeechRate(1.1f)
                }
                VoiceAccent.SCOTTISH_BLOKE -> {
                    textToSpeech.setPitch(0.95f)
                    textToSpeech.setSpeechRate(0.85f)
                }
                VoiceAccent.BRITISH_POSH -> {
                    textToSpeech.setPitch(0.95f)
                    textToSpeech.setSpeechRate(0.8f)
                }
                VoiceAccent.IRISH_CHARM -> {
                    textToSpeech.setPitch(0.9f)
                    textToSpeech.setSpeechRate(0.9f)
                }
                VoiceAccent.SOUTH_AFRICAN_BOER -> {
                    textToSpeech.setPitch(0.88f)
                    textToSpeech.setSpeechRate(0.95f)
                }
                VoiceAccent.AMERICAN_SOUTHERN -> {
                    textToSpeech.setPitch(0.85f)
                    textToSpeech.setSpeechRate(0.85f)
                }
                VoiceAccent.AMERICAN_NEW_YORK -> {
                    textToSpeech.setPitch(1.1f)
                    textToSpeech.setSpeechRate(1.15f)
                }
                VoiceAccent.CANADIAN_MAPLE -> {
                    textToSpeech.setPitch(0.9f)
                    textToSpeech.setSpeechRate(0.9f)
                }
                VoiceAccent.KIWI_BLOKE -> {
                    textToSpeech.setPitch(0.88f)
                    textToSpeech.setSpeechRate(0.92f)
                }
                VoiceAccent.GERMAN_BOSS -> {
                    textToSpeech.setPitch(0.85f)
                    textToSpeech.setSpeechRate(1.0f)
                }
            }
        }
    }

    fun speakAlert(alert: DiagnosticAlert) {
        if (!isInitialized) return
        val commentary = generateCommentary(alert)
        speakText(commentary)
    }

    private fun generateCommentary(alert: DiagnosticAlert): String {
        return when (selectedAccent) {
            VoiceAccent.HORI_MAORI_NZ -> when (alert.severity) {
                AlertSeverity.INFO -> "Kia ora bro! ${alert.component} is looking sweet as. ${alert.reason}. ${alert.recommendation}"
                AlertSeverity.WARNING -> "Ey up cuz! Better keep an eye on that ${alert.component}. ${alert.reason}. You might wanna sort it. ${alert.recommendation}"
                AlertSeverity.CRITICAL -> "Nah nah nah mate! Your ${alert.component} is gonna go bang soon! ${alert.reason}. Gotta fix this now bro! ${alert.recommendation}"
            }
            VoiceAccent.PROUD_AUSSIE -> when (alert.severity) {
                AlertSeverity.INFO -> "G'day! Your ${alert.component} is running true blue. ${alert.reason}. No worries mate. ${alert.recommendation}"
                AlertSeverity.WARNING -> "Oi mate! Keep your eyes peeled on that ${alert.component}. ${alert.reason}. Better get onto it soon. ${alert.recommendation}"
                AlertSeverity.CRITICAL -> "Stone the crows! Your ${alert.component} is about to pack it in! ${alert.reason}. Get it fixed ASAP mate! ${alert.recommendation}"
            }
            VoiceAccent.AUSSIE_LEBO_WOG -> when (alert.severity) {
                AlertSeverity.INFO -> "Ey yalla! Your ${alert.component} is going alright yaar. ${alert.reason}. All good habib. ${alert.recommendation}"
                AlertSeverity.WARNING -> "Eh eh eh! Listen here, that ${alert.component} is looking a bit dodgy. ${alert.reason}. You gotta sort it out quick smart! ${alert.recommendation}"
                AlertSeverity.CRITICAL -> "Wallah! Your ${alert.component} is gonna blow! ${alert.reason}. No mucking around, fix it now! ${alert.recommendation}"
            }
            VoiceAccent.SCOTTISH_BLOKE -> when (alert.severity) {
                AlertSeverity.INFO -> "Och aye! Your ${alert.component} is fine and dandy laddie. ${alert.reason}. Nae bother. ${alert.recommendation}"
                AlertSeverity.WARNING -> "Och naw! That ${alert.component} isnae lookin' so grand. ${alert.reason}. Better get it sorted wee man. ${alert.recommendation}"
                AlertSeverity.CRITICAL -> "Och jings! Your ${alert.component} is gonnae break the noo! ${alert.reason}. Get it fixed immediately! ${alert.recommendation}"
            }
            VoiceAccent.BRITISH_POSH -> when (alert.severity) {
                AlertSeverity.INFO -> "Right then! Your ${alert.component} is functioning splendidly. ${alert.reason}. Absolutely brilliant. ${alert.recommendation}"
                AlertSeverity.WARNING -> "I say! Your ${alert.component} appears to be rather under the weather. ${alert.reason}. Do get it attended to forthwith. ${alert.recommendation}"
                AlertSeverity.CRITICAL -> "Good gracious! Your ${alert.component} is about to fail catastrophically! ${alert.reason}. Immediate servicing required! ${alert.recommendation}"
            }
            VoiceAccent.IRISH_CHARM -> when (alert.severity) {
                AlertSeverity.INFO -> "Howya! Your ${alert.component} is grand so it is. ${alert.reason}. No bother at all to ya. ${alert.recommendation}"
                AlertSeverity.WARNING -> "Jaysus! Your ${alert.component} is looking a wee bit dodgy now. ${alert.reason}. Better get it fixed quick like. ${alert.recommendation}"
                AlertSeverity.CRITICAL -> "Feck! Your ${alert.component} is gonna destroy itself! ${alert.reason}. Get it sorted now for the love of Paddy! ${alert.recommendation}"
            }
            VoiceAccent.SOUTH_AFRICAN_BOER -> when (alert.severity) {
                AlertSeverity.INFO -> "Howzit boet! Your ${alert.component} is running lekker. ${alert.reason}. All is kwaai. ${alert.recommendation}"
                AlertSeverity.WARNING -> "Ag hey! That ${alert.component} is looking a bit skeef. ${alert.reason}. You gotta check it out, ou. ${alert.recommendation}"
                AlertSeverity.CRITICAL -> "Eish boet! Your ${alert.component} is going to bust! ${alert.reason}. Fix it now, my bru! ${alert.recommendation}"
            }
            VoiceAccent.AMERICAN_SOUTHERN -> when (alert.severity) {
                AlertSeverity.INFO -> "Well hey there! Your ${alert.component} is purrin' like a kitten. ${alert.reason}. Mighty fine. ${alert.recommendation}"
                AlertSeverity.WARNING -> "Now hold on there! Your ${alert.component} don't look right to me. ${alert.reason}. Y'all better get that sorted out. ${alert.recommendation}"
                AlertSeverity.CRITICAL -> "Lordy! Your ${alert.component} is about to blow sky high! ${alert.reason}. Get that fixed right quick now, ya hear! ${alert.recommendation}"
            }
            VoiceAccent.AMERICAN_NEW_YORK -> when (alert.severity) {
                AlertSeverity.INFO -> "Yo! Your ${alert.component} is running great, fuggedabout it. ${alert.reason}. All good. ${alert.recommendation}"
                AlertSeverity.WARNING -> "Ayo! Your ${alert.component} is actin' funny, bro. ${alert.reason}. You gotta get that looked at, capisce? ${alert.recommendation}"
                AlertSeverity.CRITICAL -> "Fuhgeddabout it! Your ${alert.component} is about to go kaboom! ${alert.reason}. Fix it NOW, pal! ${alert.recommendation}"
            }
            VoiceAccent.CANADIAN_MAPLE -> when (alert.severity) {
                AlertSeverity.INFO -> "Hey there, buddy! Your ${alert.component} is running real good, eh? ${alert.reason}. No worries, pal. ${alert.recommendation}"
                AlertSeverity.WARNING -> "Say, your ${alert.component} doesn't look so hot there, buddy. ${alert.reason}. You might wanna get that looked at, ok? ${alert.recommendation}"
                AlertSeverity.CRITICAL -> "Oh geez! Your ${alert.component} is gonna fail big time! ${alert.reason}. Better fix that right away there, buddy! ${alert.recommendation}"
            }
            VoiceAccent.KIWI_BLOKE -> when (alert.severity) {
                AlertSeverity.INFO -> "Sweet as bro! Your ${alert.component} is going choice. ${alert.reason}. All good. ${alert.recommendation}"
                AlertSeverity.WARNING -> "Eh up! Your ${alert.component} is looking a bit rough there. ${alert.reason}. Better get it sorted, eh? ${alert.recommendation}"
                AlertSeverity.CRITICAL -> "Yeah nah mate! Your ${alert.component} is gonna chuck a wobbly! ${alert.reason}. Fix it now bro! ${alert.recommendation}"
            }
            VoiceAccent.GERMAN_BOSS -> when (alert.severity) {
                AlertSeverity.INFO -> "Sehr gut! Ihr ${alert.component} funktioniert ausgezeichnet. ${alert.reason}. Alles in ordnung. ${alert.recommendation}"
                AlertSeverity.WARNING -> "Achtung! Ihr ${alert.component} ist nicht optimal. ${alert.reason}. Sie müssen es überprüfen! ${alert.recommendation}"
                AlertSeverity.CRITICAL -> "Achtung! Achtung! Ihr ${alert.component} wird ausfallen! ${alert.reason}. Sofort reparieren! ${alert.recommendation}"
            }
            else -> "${alert.severity.name}: ${alert.component}. ${alert.reason}. ${alert.recommendation}"
        }
    }

    private fun speakText(text: String) {
        if (isInitialized) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null)
        }
    }

    fun shutdown() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }
}
