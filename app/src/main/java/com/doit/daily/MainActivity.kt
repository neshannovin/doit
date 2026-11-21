package com.doit.daily

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF102A43)
private val Sky = Color(0xFF56B4D3)
private val Mint = Color(0xFF8EE3C4)
private val Sand = Color(0xFFFFF8EB)
private val Coral = Color(0xFFFF8C70)
private val Mist = Color(0xFFE9F4F6)

class Step(val title: String, done: Boolean = false) {
    var done by mutableStateOf(done)
}
class Task(
    val id: Int,
    val title: String,
    val category: String,
    val time: String,
    val accent: Color,
    done: Boolean = false,
    val steps: MutableList<Step> = mutableListOf()
) {
    var done by mutableStateOf(done)
}

class MainActivity : ComponentActivity() {
    private val permission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        setContent { DoitApp() }
    }
}

@Composable
fun DoitApp() {
    var english by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }
    val tasks = remember { mutableStateListOf(
        Task(1, "تمرین ارائه فردا", "کار", "۹:۳۰", Coral, steps = mutableListOf(Step("اسلایدها را مرور کن", true), Step("زمان‌بندی را تمرین کن"))),
        Task(2, "پیاده‌روی کنار پارک", "شخصی", "۱۷:۳۰", Mint, steps = mutableListOf(Step("بطری آب بردار"))),
        Task(3, "خرید مواد شام", "خانه", "۱۹:۰۰", Sky)
    ) }
    val today = tasks.count { !it.done }
    MaterialTheme(colorScheme = lightColorScheme(primary = Ink, secondary = Sky, surface = Sand, background = Sand)) {
        Scaffold(containerColor = Sand, bottomBar = {
            NavigationBar(containerColor = Color.White) {
                listOf(Icons.Rounded.Today to if (english) "Today" else "امروز", Icons.Rounded.CalendarMonth to if (english) "Plan" else "برنامه", Icons.Rounded.BarChart to if (english) "Pulse" else "نبض").forEachIndexed { i, (icon, label) ->
                    NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { Icon(icon, null) }, label = { Text(label) })
                }
            }
        }, floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }, containerColor = Coral, contentColor = Color.White) { Icon(Icons.Rounded.Add, if (english) "Add task" else "کار جدید") }
        }) { padding ->
            when (tab) {
                0 -> TodayScreen(Modifier.padding(padding), tasks, today, english) { english = !english }
                1 -> PlanScreen(Modifier.padding(padding), tasks, english)
                else -> PulseScreen(Modifier.padding(padding), tasks, english)
            }
        }
        if (showAdd) AddTaskSheet(english, onDismiss = { showAdd = false }) { title, time, category ->
            tasks += Task(tasks.maxOfOrNull { it.id }?.plus(1) ?: 1, title, category, time, listOf(Coral, Mint, Sky).random())
            showAdd = false
        }
    }
}

@Composable
private fun TodayScreen(modifier: Modifier, tasks: List<Task>, remaining: Int, english: Boolean, switchLanguage: () -> Unit) {
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 18.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Header(english, switchLanguage) }
        item { WeatherCard(english) }
        item { ProgressBlock(tasks, remaining, english) }
        item { Text(if (english) "Your rhythm today" else "ریتم امروزت", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.padding(top = 5.dp)) }
        items(tasks, key = { it.id }) { task -> TaskCard(task, english) }
    }
}

@Composable private fun Header(english: Boolean, switch: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column { Text(if (english) "Good morning," else "صبح بخیر،", color = Ink, fontSize = 17.sp); Text(if (english) "Make space for what matters." else "برای مهم‌ها جا باز کن.", color = Ink, fontSize = 26.sp, fontWeight = FontWeight.Black) }
        Surface(shape = CircleShape, color = Mist, modifier = Modifier.clickable { switch() }) { Text(if (english) "FA" else "EN", modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp), color = Ink, fontWeight = FontWeight.Bold) }
    }
}

@Composable private fun WeatherCard(english: Boolean) {
    Card(colors = CardDefaults.cardColors(containerColor = Ink), shape = RoundedCornerShape(26.dp)) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.WbSunny, null, tint = Color(0xFFFFD166), modifier = Modifier.size(46.dp))
            Spacer(Modifier.width(15.dp)); Column(Modifier.weight(1f)) { Text(if (english) "A clear start" else "شروعی روشن", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text(if (english) "Based on your location" else "بر اساس موقعیت شما", color = Color(0xFFC8E5EE), fontSize = 13.sp) }
            Column(horizontalAlignment = Alignment.End) { Text("23°", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black); Text(if (english) "Tehran • sunny" else "تهران • آفتابی", color = Color(0xFFC8E5EE), fontSize = 12.sp) }
        }
    }
}

@Composable private fun ProgressBlock(tasks: List<Task>, remaining: Int, english: Boolean) {
    val complete = tasks.count { it.done }; val ratio = if (tasks.isEmpty()) 0f else complete.toFloat() / tasks.size
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp)) { Column(Modifier.padding(18.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(if (english) "Today at a glance" else "یک نگاه به امروز", fontWeight = FontWeight.Bold, color = Ink); Text(if (english) "$remaining left" else "$remaining کار مانده", color = Coral, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(12.dp)); LinearProgressIndicator(progress = { ratio }, modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape), color = Mint, trackColor = Mist)
        Text(if (english) "${tasks.size} intentional tasks. Take them one at a time." else "${tasks.size} کار آگاهانه؛ یکی‌یکی پیش برو.", color = Color(0xFF547182), fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp))
    } }
}

@Composable private fun TaskCard(task: Task, english: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }) { Column(Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.done, onCheckedChange = { task.done = it }, colors = CheckboxDefaults.colors(checkedColor = task.accent))
            Spacer(Modifier.width(6.dp)); Column(Modifier.weight(1f)) { Text(task.title, color = Ink, fontWeight = FontWeight.Bold, textDecoration = if (task.done) TextDecoration.LineThrough else null, maxLines = 1, overflow = TextOverflow.Ellipsis); Text("${task.category}  •  ${task.time}", fontSize = 12.sp, color = Color(0xFF668191)) }
            if (task.steps.isNotEmpty()) Badge { Text("${task.steps.count { it.done }}/${task.steps.size}", fontSize = 11.sp) }
            Icon(if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, null, tint = Ink)
        }
        AnimatedVisibility(expanded && task.steps.isNotEmpty()) { Column(Modifier.padding(start = 42.dp, top = 8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) { task.steps.forEach { step -> Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = step.done, onCheckedChange = { step.done = it }, modifier = Modifier.size(30.dp)); Text(step.title, fontSize = 13.sp, color = Ink, textDecoration = if (step.done) TextDecoration.LineThrough else null) } } } }
    } }
}

@Composable private fun PlanScreen(modifier: Modifier, tasks: List<Task>, english: Boolean) { Column(modifier.fillMaxSize().padding(22.dp)) { Text(if (english) "Plan your week" else "هفته‌ات را بچین", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Ink); Text(if (english) "Move tasks with intention." else "کارها را با نیت جابه‌جا کن.", color = Color(0xFF547182)); Spacer(Modifier.height(25.dp)); listOf("Mon", "Tue", "Wed", "Thu", "Fri").forEachIndexed { index, day -> Row(Modifier.fillMaxWidth().padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) { Text(if (english) day else listOf("شنبه","یکشنبه","دوشنبه","سه‌شنبه","چهارشنبه")[index], modifier = Modifier.width(72.dp), color = Ink, fontWeight = FontWeight.Bold); Box(Modifier.height(9.dp).weight(1f).clip(CircleShape).background(if (index < 3) listOf(Mint, Sky, Coral)[index] else Mist)) } } } }
@Composable private fun PulseScreen(modifier: Modifier, tasks: List<Task>, english: Boolean) { val done = tasks.count { it.done }; Column(modifier.fillMaxSize().padding(22.dp)) { Text(if (english) "Your pulse" else "نبض تو", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Ink); Spacer(Modifier.height(24.dp)); Card(colors = CardDefaults.cardColors(containerColor = Ink), shape = RoundedCornerShape(28.dp)) { Column(Modifier.padding(24.dp)) { Text(if (english) "This week" else "این هفته", color = Color(0xFFC8E5EE)); Text("$done / ${tasks.size}", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black); Text(if (english) "tasks completed today" else "کار امروز انجام شده", color = Color.White) } }; Spacer(Modifier.height(20.dp)); Text(if (english) "Tiny wins compound." else "بردهای کوچک جمع می‌شوند.", fontSize = 20.sp, color = Ink, fontWeight = FontWeight.Bold) } }

@Composable private fun AddTaskSheet(english: Boolean, onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) { var title by remember { mutableStateOf("") }; var time by remember { mutableStateOf("10:00") }; var category by remember { mutableStateOf(if (english) "Personal" else "شخصی") }; ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Sand) { Column(Modifier.padding(24.dp).padding(bottom = 30.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { Text(if (english) "Make room for a task" else "برای یک کار جا باز کن", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Ink); OutlinedTextField(title, { title = it }, label = { Text(if (english) "What needs doing?" else "چه کاری باید انجام شود؟") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(time, { time = it }, label = { Text(if (english) "Time" else "زمان") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(category, { category = it }, label = { Text(if (english) "Category" else "دسته‌بندی") }, modifier = Modifier.fillMaxWidth()); Button(onClick = { if (title.isNotBlank()) onAdd(title, time, category) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Ink)) { Text(if (english) "Add task" else "افزودن کار") } } } }
@Composable private fun Badge(content: @Composable () -> Unit) { Surface(color = Mist, shape = CircleShape) { Box(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), contentAlignment = Alignment.Center) { content() } } }
