package com.example.ktxmonitor.monitoring

import com.example.ktxmonitor.domain.model.CheckResult
import com.example.ktxmonitor.domain.model.TrainInfo
import com.example.ktxmonitor.domain.model.WatchTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class KorailHttpMonitoringEngine : MonitoringEngine {

    companion object {
        private const val BASE_URL =
            "https://smart.letskorail.com/classes/com.korail.mobile.seatMovie.ScheduleView"
        private const val USER_AGENT =
            "Dalvik/2.1.0 (Linux; U; Android 14; Pixel 7 Build/UQ1A.240205.002)"
    }

    override suspend fun check(target: WatchTarget): CheckResult = withContext(Dispatchers.IO) {
        try {
            val dateFormatted = target.date.replace("-", "")
            val timeFormatted = (target.startTime.replace(":", "") + "00").padEnd(6, '0')

            val queryParams = listOf(
                "Device" to "AD",
                "radJobId" to "1",
                "selGoTrain" to "100",
                "txtCardPsgCnt" to "0",
                "txtGoAbrdDt" to dateFormatted,
                "txtGoEnd" to target.arrivalStation,
                "txtGoHour" to timeFormatted,
                "txtGoStart" to target.departureStation,
                "txtMenuId" to "11",
                "txtPsgFlg_1" to target.passengerCount.toString(),
                "txtPsgFlg_2" to "0",
                "txtPsgFlg_3" to "0",
                "txtPsgFlg_4" to "0",
                "txtPsgFlg_5" to "0",
                "txtPsgFlg_8" to "0",
                "txtSeatAttCd_2" to "000",
                "txtSeatAttCd_3" to "000",
                "txtSeatAttCd_4" to "015",
                "txtTrnGpCd" to "100",
                "Version" to "240101001"
            ).joinToString("&") { (key, value) ->
                "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}"
            }

            val fullUrl = "$BASE_URL?$queryParams"
            val url = URL(fullUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", USER_AGENT)
                connectTimeout = 8000
                readTimeout = 8000
            }

            val responseCode = connection.responseCode
            val inputStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val responseText = BufferedReader(InputStreamReader(inputStream, "UTF-8")).use {
                it.readText()
            }

            parseResponse(responseText)
        } catch (e: Exception) {
            CheckResult.Error(message = e.localizedMessage ?: "네트워크 요청 실패")
        }
    }

    private fun parseResponse(jsonString: String): CheckResult {
        return try {
            val json = JSONObject(jsonString)
            val result = json.optString("strResult", "FAIL")

            if (result == "SUCC") {
                val trnInfos = json.optJSONObject("trn_infos")
                val trnInfoArray = trnInfos?.optJSONArray("trn_info")
                val trains = mutableListOf<TrainInfo>()

                if (trnInfoArray != null) {
                    for (i in 0 until trnInfoArray.length()) {
                        val obj = trnInfoArray.getJSONObject(i)
                        val trainNo = obj.optString("h_trn_no")
                        val trainType = obj.optString("h_trn_clsf_nm", "KTX")
                        val depStation = obj.optString("h_dpt_rs_stn_nm")
                        val arrStation = obj.optString("h_arv_rs_stn_nm")
                        val depTimeRaw = obj.optString("h_dpt_tm")
                        val arrTimeRaw = obj.optString("h_arv_tm")
                        val speSeatCode = obj.optString("h_spe_rsv_cd")
                        val genSeatCode = obj.optString("h_gen_rsv_cd")
                        val rsvPsbNm = obj.optString("h_rsv_psb_nm", "")

                        val depTime = if (depTimeRaw.length >= 4) {
                            "${depTimeRaw.substring(0, 2)}:${depTimeRaw.substring(2, 4)}"
                        } else depTimeRaw

                        val arrTime = if (arrTimeRaw.length >= 4) {
                            "${arrTimeRaw.substring(0, 2)}:${arrTimeRaw.substring(2, 4)}"
                        } else arrTimeRaw

                        trains.add(
                            TrainInfo(
                                trainNo = trainNo,
                                trainType = trainType,
                                departureStation = depStation,
                                arrivalStation = arrStation,
                                departureTime = depTime,
                                arrivalTime = arrTime,
                                hasGeneralSeat = genSeatCode == "11",
                                hasSpecialSeat = speSeatCode == "11",
                                reservePossibleText = rsvPsbNm
                            )
                        )
                    }
                }

                if (trains.isEmpty()) {
                    CheckResult.NoResult()
                } else {
                    CheckResult.Success(trains = trains)
                }
            } else {
                val msgCd = json.optString("h_msg_cd", "ERROR")
                val msgTxt = json.optString("h_msg_txt", "조회 실패")
                CheckResult.Error(message = msgTxt, errorCode = msgCd)
            }
        } catch (e: Exception) {
            CheckResult.Error(message = "응답 파싱 오류: ${e.message}")
        }
    }
}
