package com.example.myapplication

import Rates
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CurrencyConverterScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterScreen() {
    val context = LocalContext.current
    // Полный список валют
    val allCurrencies = listOf(
        "USD - Доллар США", "EUR - Евро", "GBP - Фунт стерлингов",
        "JPY - Японская иена", "RUB - Российский рубль", "CNY - Китайский юань",
        "AUD - Австралийский доллар", "CAD - Канадский доллар", "CHF - Швейцарский франк",
        "HKD - Гонконгский доллар", "SGD - Сингапурский доллар", "KRW - Южнокорейская вона"
    )

    // Состояния для UI
    var currency1 by remember { mutableStateOf("") }
    var expanded1 by remember { mutableStateOf(false) }
    var currency2 by remember { mutableStateOf("") }
    var expanded2 by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentRate by remember { mutableStateOf<Double?>(null) }
    var currentSrcCurrency by remember { mutableStateOf("") }
    var currentDstCurrency by remember { mutableStateOf("") }
    var notification by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Фиксированное поле для уведомлений
        Text(
            text = notification,
            color = if (notification.isNotEmpty()) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            textAlign = TextAlign.Center
        )

        // Первый список с поиском
        ExposedDropdownMenuBox(
            expanded = expanded1,
            onExpandedChange = { expanded1 = it }
        ) {
            TextField(
                value = currency1,
                onValueChange = {
                    currency1 = it
                    expanded1 = it.isNotEmpty()
                },
                label = { Text("Первая валюта") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded1) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded1,
                onDismissRequest = { expanded1 = false }
            ) {
                if (currency1.isEmpty()) {
                    allCurrencies.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                currency1 = item
                                expanded1 = false
                            }
                        )
                    }
                } else {
                    allCurrencies.filter { it.contains(currency1, ignoreCase = true) }
                        .forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    currency1 = item
                                    expanded1 = false
                                }
                            )
                        }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Второй список с поиском
        ExposedDropdownMenuBox(
            expanded = expanded2,
            onExpandedChange = { expanded2 = it }
        ) {
            TextField(
                value = currency2,
                onValueChange = {
                    currency2 = it
                    expanded2 = it.isNotEmpty()
                },
                label = { Text("Вторая валюта") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded2) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded2,
                onDismissRequest = { expanded2 = false }
            ) {
                if (currency2.isEmpty()) {
                    allCurrencies.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                currency2 = item
                                expanded2 = false
                            }
                        )
                    }
                } else {
                    allCurrencies.filter { it.contains(currency2, ignoreCase = true) }
                        .forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    currency2 = item
                                    expanded2 = false
                                }
                            )
                        }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Поле результата конвертации
        TextField(
            value = when {
                isLoading -> "Загрузка..."
                !error.isNullOrEmpty() -> error!!
                else -> result
            },
            onValueChange = {},
            readOnly = true,
            label = { Text("Результат конвертации") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка конвертации
        Button(
            onClick = {
                if (currency1.isEmpty() || currency2.isEmpty()) {
                    result = "Выберите обе валюты"
                    return@Button
                }

                val srcCurrency = currency1.split(" - ")[0]
                val dstCurrency = currency2.split(" - ")[0]

                if (srcCurrency == dstCurrency) {
                    result = "Валюты одинаковые"
                    return@Button
                }

                scope.launch {
                    try {
                        isLoading = true
                        error = null
                        val rate = withContext(Dispatchers.IO) { getRate(srcCurrency, dstCurrency) }
                        if (rate == null) {
                            throw IllegalArgumentException("введена неверная валюта")
                        }
                        currentRate = rate
                        currentSrcCurrency = srcCurrency
                        currentDstCurrency = dstCurrency
                        result = "1 $srcCurrency = %.4f $dstCurrency".format(rate)
                    } catch (e: IllegalArgumentException) {
                        error = "Ошибка: валюта не найдена"
                    } catch (e: Exception) {
                        error = "Ошибка: ${e.localizedMessage}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Конвертация..." else "Конвертировать")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Кнопка добавления в историю
        Button(
            onClick = {
                currentRate?.let { rate ->
                    FileManager.addRateToHistory(
                        context,
                        currentSrcCurrency,
                        currentDstCurrency,
                        rate
                    )
                    notification = "Курс добавлен в историю"
                    scope.launch {
                        delay(3000)
                        notification = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = currentRate != null
        ) {
            Text("Добавить в историю")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Кнопка сохранения в файл
        Button(
            onClick = {
                if (FileManager.saveRatesToFile(context)) {
                    notification = "Файл успешно сохранен"
                } else {
                    notification = "Ошибка при сохранении файла"
                }
                scope.launch {
                    delay(3000)
                    notification = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = FileManager.hasSavedRates()
        ) {
            Text("Сохранить историю в файл")
        }
    }
}

suspend fun getRate(src: String, dst: String): Double? {
    Log.d("Rates", "$src -> $dst")
    val rates = Rates()
    rates.loadRates()
    return rates.getExchangeRate(src, dst)
}

@Preview(showBackground = true)
@Composable
fun CurrencyConverterPreview() {
    MyApplicationTheme {
        CurrencyConverterScreen()
    }
}