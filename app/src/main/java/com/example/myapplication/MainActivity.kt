package com.example.myapplication

import Rates
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.*

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
    // Полный список валют (пример)
    val allCurrencies = listOf(
        "USD - Доллар США", "EUR - Евро", "GBP - Фунт стерлингов",
        "JPY - Японская иена", "RUB - Российский рубль", "CNY - Китайский юань",
        "AUD - Австралийский доллар", "CAD - Канадский доллар", "CHF - Швейцарский франк",
        "HKD - Гонконгский доллар", "SGD - Сингапурский доллар", "KRW - Южнокорейская вона"
    )

    // Состояния для первого списка
    var currency1 by remember { mutableStateOf("") }
    var expanded1 by remember { mutableStateOf(false) }
    val filteredCurrencies1 = remember(currency1) {
        if (currency1.isEmpty()) allCurrencies
        else allCurrencies.filter { it.contains(currency1, ignoreCase = true) }
    }

    // Состояния для второго списка
    var currency2 by remember { mutableStateOf("") }
    var expanded2 by remember { mutableStateOf(false) }
    val filteredCurrencies2 = remember(currency2) {
        if (currency2.isEmpty()) allCurrencies
        else allCurrencies.filter { it.contains(currency2, ignoreCase = true) }
    }

    // Результат
    var result by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Coroutine scope для выполнения suspend функций
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                if (filteredCurrencies1.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Не найдено") },
                        onClick = { expanded1 = false }
                    )
                } else {
                    filteredCurrencies1.forEach { item ->
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
                if (filteredCurrencies2.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Не найдено") },
                        onClick = { expanded2 = false }
                    )
                } else {
                    filteredCurrencies2.forEach { item ->
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

        // Поле результата
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

        Spacer(modifier = Modifier.height(32.dp))

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
                        result = "1 $srcCurrency = $rate $dstCurrency"
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
    }
}

suspend fun getRate(src: String, dst: String): Double? {
    Log.d("Rates", "$src -> $dst")
    val rates = Rates()
    rates.loadRates()
    val x = rates.getExchangeRate(src, dst)
    return x
}

@Preview(showBackground = true)
@Composable
fun CurrencyConverterPreview() {
    MyApplicationTheme {
        CurrencyConverterScreen()
    }
}