package com.purecanvas.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.purecanvas.app.domain.ProcessingSettings
import com.purecanvas.app.ui.EditorViewModel

private val Ink=Color(0xFF24272D); private val Paper=Color(0xFFFAFAF8); private val Muted=Color(0xFF777B82); private val Accent=Color(0xFFB9A98E)
class MainActivity:ComponentActivity(){ private val vm:EditorViewModel by viewModels()
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);window.statusBarColor=android.graphics.Color.rgb(250,250,248);window.navigationBarColor=android.graphics.Color.rgb(250,250,248);window.decorView.systemUiVisibility=android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
 setContent { MaterialTheme(colorScheme=lightColorScheme(primary=Ink,background=Paper,surface=Paper,secondary=Accent)){ val s by vm.state.collectAsStateWithLifecycle(); val picker = rememberLauncherForActivityResult(
    ActivityResultContracts.OpenDocument()
) { uri ->
    uri?.let {
        try {
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // Continue with temporary access if persistence is unavailable.
        }

        vm.import(it)
    }
}; PureCanvasScreen(s, {picker.launch(arrayOf("image/jpeg", "image/png", "image/webp"))},vm) } }
 }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun PureCanvasScreen(s:com.purecanvas.app.ui.EditorState,onImport:()->Unit,vm:EditorViewModel){
 var tab by remember { mutableStateOf(0) }; var compare by remember { mutableFloatStateOf(.5f) }; var format by remember { mutableStateOf("JPEG") }; var quality by remember { mutableFloatStateOf(92f) }; var showExport by remember { mutableStateOf(false) }
 Scaffold(containerColor=Paper,topBar={TopAppBar(title={Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(Ink),contentAlignment=Alignment.Center){Icon(Icons.Rounded.AutoAwesome,null,tint=Color.White,modifier=Modifier.size(18.dp))}; Spacer(Modifier.width(10.dp)); Text("PureCanvas",fontWeight=FontWeight.Bold,color=Ink,letterSpacing=(-.5).sp)}},actions={Text("ON-DEVICE",fontSize=10.sp,fontWeight=FontWeight.Bold,color=Muted,modifier=Modifier.padding(end=18.dp))},colors=TopAppBarDefaults.topAppBarColors(containerColor=Paper))}) { pad ->
 Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal=20.dp)) {
  Spacer(Modifier.height(12.dp)); Text(if(s.original==null) "A clearer view of your art." else "Your editing studio",fontSize=26.sp,fontWeight=FontWeight.SemiBold,color=Ink); Spacer(Modifier.height(5.dp)); Text("Thoughtful enhancement. Your images stay on this device.",fontSize=13.sp,color=Muted)
  Spacer(Modifier.height(22.dp))
  if(s.original==null){
   Box(Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(26.dp)).background(Brush.linearGradient(listOf(Color(0xFFECE9E2),Color(0xFFF8F7F3)))),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Rounded.ImageSearch,null,tint=Ink,modifier=Modifier.size(54.dp));Spacer(Modifier.height(16.dp));Text("Bring your image into focus",fontWeight=FontWeight.SemiBold,fontSize=17.sp,color=Ink);Spacer(Modifier.height(6.dp));Text("JPG · PNG · WebP",fontSize=12.sp,color=Muted)}}
   Spacer(Modifier.height(16.dp)); Button(onClick=onImport,modifier=Modifier.fillMaxWidth().height(54.dp),shape=RoundedCornerShape(16.dp),colors=ButtonDefaults.buttonColors(containerColor=Ink)){Icon(Icons.Rounded.AddPhotoAlternate,null);Spacer(Modifier.width(9.dp));Text("Import image",fontWeight=FontWeight.SemiBold)}
   Spacer(Modifier.height(28.dp)); Text("THE TOOLKIT",fontSize=11.sp,fontWeight=FontWeight.Bold,color=Muted,letterSpacing=1.4.sp); Spacer(Modifier.height(12.dp)); Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){ToolCard("Denoise","Reduce image noise",Icons.Rounded.Grain,Modifier.weight(1f));ToolCard("Tone","Balance light & color",Icons.Rounded.Tune,Modifier.weight(1f))}; Spacer(Modifier.height(10.dp));Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){ToolCard("Detail","Refine edges",Icons.Rounded.Details,Modifier.weight(1f));ToolCard("Export","Save & share",Icons.Rounded.IosShare,Modifier.weight(1f))}
  } else {
   Row(verticalAlignment=Alignment.CenterVertically){Text("PREVIEW",fontSize=11.sp,fontWeight=FontWeight.Bold,color=Muted,letterSpacing=1.3.sp);Spacer(Modifier.weight(1f));TextButton(onClick={vm.reset()}){Icon(Icons.Rounded.RestartAlt,null,Modifier.size(16.dp));Text("Reset")}}
   Box(Modifier.fillMaxWidth().heightIn(min=220.dp,max=390.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFFE9E8E4)),contentAlignment=Alignment.Center){val bmp=if(tab==0)s.enhanced?:s.original else s.original; bmp?.let{Image(it.asImageBitmap(),null,modifier=Modifier.fillMaxWidth().heightIn(max=390.dp),contentScale=androidx.compose.ui.layout.ContentScale.Fit)};if(s.busy)CircularProgressIndicator(Modifier.align(Alignment.Center),color=Ink)}
   if(s.busy){Spacer(Modifier.height(8.dp));LinearProgressIndicator(progress={s.progress/100f},modifier=Modifier.fillMaxWidth(),color=Ink);Text("Enhancing · ${s.progress}%",fontSize=11.sp,color=Muted)}
   Spacer(Modifier.height(12.dp));Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(Color(0xFFEDECE8)).padding(4.dp)){listOf("Enhanced","Original").forEachIndexed{i,t->Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if(tab==i)Color.White else Color.Transparent).clickable{tab=i}.padding(vertical=10.dp),contentAlignment=Alignment.Center){Text(t,fontWeight=FontWeight.SemiBold,fontSize=13.sp,color=Ink)}}}
   Spacer(Modifier.height(20.dp));Text("ADJUSTMENTS",fontWeight=FontWeight.Bold,fontSize=11.sp,color=Muted,letterSpacing=1.3.sp)
   SettingSlider("Denoise",s.settings.denoise,{vm.update(s.settings.copy(denoise=it))});SettingSlider("Sharpness",s.settings.sharpness,{vm.update(s.settings.copy(sharpness=it))});SettingSlider("Contrast",(s.settings.contrast-.5f)/1.3f,{vm.update(s.settings.copy(contrast=.5f+it*1.3f))});SettingSlider("Brightness",(s.settings.brightness+.25f)/.5f,{vm.update(s.settings.copy(brightness=it*.5f-.25f))});SettingSlider("Saturation",s.settings.saturation/2f,{vm.update(s.settings.copy(saturation=it*2f))});SettingSlider("Color warmth",(s.settings.warmth+.25f)/.5f,{vm.update(s.settings.copy(warmth=it*.5f-.25f))});SettingSlider("Detail",s.settings.detail,{vm.update(s.settings.copy(detail=it))})
   Spacer(Modifier.height(12.dp));Button(onClick={showExport=true},modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(15.dp),colors=ButtonDefaults.buttonColors(containerColor=Ink),enabled=!s.busy&&s.enhanced!=null){Icon(Icons.Rounded.SaveAlt,null);Spacer(Modifier.width(8.dp));Text("Export image")};OutlinedButton(onClick=onImport,modifier=Modifier.fillMaxWidth().padding(top=8.dp),shape=RoundedCornerShape(15.dp)){Icon(Icons.Rounded.AddPhotoAlternate,null);Spacer(Modifier.width(8.dp));Text("Choose another image")}
  }
  s.error?.let{Spacer(Modifier.height(12.dp));Text(it,color=Color(0xFFB3261E),fontSize=12.sp)}
  s.savedUri?.let{Spacer(Modifier.height(12.dp));Text("Saved to Pictures / PureCanvas",color=Color(0xFF287A50),fontSize=13.sp)}
  Spacer(Modifier.height(30.dp));Text("PRIVATE BY DESIGN  ·  NO CLOUD UPLOADS",fontSize=10.sp,color=Muted,letterSpacing=1.sp,modifier=Modifier.align(Alignment.CenterHorizontally));Spacer(Modifier.height(24.dp))
 }
 }
 if(showExport) AlertDialog(onDismissRequest={showExport=false},title={Text("Export image")},text={Column{Text("Format",fontWeight=FontWeight.SemiBold);Row{listOf("JPEG","PNG").forEach{FilterChip(selected=format==it,onClick={format=it},label={Text(it)},modifier=Modifier.padding(end=8.dp))}};if(format=="JPEG"){Text("Quality · ${quality.toInt()}%",fontWeight=FontWeight.SemiBold);Slider(value=quality,onValueChange={quality=it},valueRange=60f..100f)}}},confirmButton={TextButton(onClick={vm.save(format,quality.toInt());showExport=false}){Text("Save to gallery")}},dismissButton={TextButton(onClick={showExport=false}){Text("Cancel")}})
}
@Composable private fun SettingSlider(label:String,value:Float,onChange:(Float)->Unit){Column(Modifier.padding(top=9.dp)){Row(verticalAlignment=Alignment.CenterVertically){Text(label,fontSize=13.sp,fontWeight=FontWeight.Medium,color=Ink);Spacer(Modifier.weight(1f));Text("${(value*100).toInt()}",fontSize=11.sp,color=Muted)};Slider(value=value.coerceIn(0f,1f),onValueChange=onChange,modifier=Modifier.height(30.dp),colors=SliderDefaults.colors(thumbColor=Ink,activeTrackColor=Ink))}}
@Composable private fun ToolCard(title:String,sub:String,icon:androidx.compose.ui.graphics.vector.ImageVector,modifier:Modifier=Modifier){Row(modifier.height(76.dp).clip(RoundedCornerShape(16.dp)).background(Color.White).padding(12.dp),verticalAlignment=Alignment.CenterVertically){Icon(icon,null,tint=Ink,modifier=Modifier.size(23.dp));Spacer(Modifier.width(10.dp));Column{Text(title,fontWeight=FontWeight.SemiBold,fontSize=13.sp,color=Ink);Text(sub,fontSize=10.sp,color=Muted)}}}
