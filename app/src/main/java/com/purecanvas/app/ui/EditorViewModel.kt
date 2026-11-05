package com.purecanvas.app.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.purecanvas.app.data.ImageRepository
import com.purecanvas.app.domain.ProcessingSettings
import com.purecanvas.app.engine.ImageProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

 data class EditorState(val original:Bitmap?=null,val enhanced:Bitmap?=null,val settings:ProcessingSettings=ProcessingSettings.Default,val busy:Boolean=false,val progress:Int=0,val error:String?=null,val savedUri:Uri?=null)
class EditorViewModel(app:Application):AndroidViewModel(app){
 private val repo=ImageRepository(app); private val processor=ImageProcessor(); private val _state=MutableStateFlow(EditorState()); val state=_state.asStateFlow()
 fun import(uri:Uri){ viewModelScope.launch { _state.value=_state.value.copy(busy=true,error=null); try { val bmp=withContext(Dispatchers.IO){repo.load(uri)}; _state.value=EditorState(original=bmp,busy=false); process() } catch(e:Exception){_state.value=_state.value.copy(busy=false,error=e.message?:"Unable to import image")} } }
 fun update(settings:ProcessingSettings){_state.value=_state.value.copy(settings=settings); process()}
 fun reset(){update(ProcessingSettings.Default)}
 fun process(){ val src=_state.value.original ?: return; val settings=_state.value.settings; viewModelScope.launch { _state.value=_state.value.copy(busy=true,progress=0,error=null); try { val result=processor.process(src,settings){p-> _state.value=_state.value.copy(progress=p)}; _state.value=_state.value.copy(enhanced=result,busy=false,progress=100) }catch(e:Exception){_state.value=_state.value.copy(busy=false,error=e.message?:"Processing failed")} } }
 fun save(format:String,quality:Int){val bmp=_state.value.enhanced?:return;viewModelScope.launch{try{val uri=withContext(Dispatchers.IO){repo.save(bmp,format,quality)};_state.value=_state.value.copy(savedUri=uri,error=null)}catch(e:Exception){_state.value=_state.value.copy(error=e.message?:"Export failed")}}}
}
