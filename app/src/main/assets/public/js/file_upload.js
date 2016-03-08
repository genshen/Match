function addNewFile(id,file){
// id = m_file1
$(".lean-overlay").remove();
$('#bottom-modal').openModal();
var list_item = "<div class='m_file' id='m_file" + id + "'>"
    +"<span style='overflow: hidden; text-overflow: ellipsis;'>"+file.name+"</span>"
    +"<b style='margin-left: 18px;'>等待上传</b>"
    +"<div class='progress'><div class='determinate' style='width: 0%'></div></div>"
    +"</div>";
  $("#file-upload-modal").append(list_item);
}

function toastError(m){
var snackbarContainer = document.querySelector('#toast-t');
var da = {message: m};
snackbarContainer.MaterialSnackbar.showSnackbar(da);
 }

$('#main-container').dmUploader({
        url: '../../files/upload.html',
        dataType: 'json',
        allowedTypes: '*',
        onInit: function(){
          console.log('Penguin initialized');
        },
        onBeforeUpload: function(id){
//           console.log('Starting the upload of #' + id);
           $('#m_file' + id).find('b').text("正在上传");
        },
         onNewFile: function(id, file){
//            console.log('New file added to queue #' + id);
            addNewFile(id,file);
        },
        onComplete: function(){
           console.log('All pending tranfers finished');
        },
        onUploadProgress: function(id, percent){
          var percentStr = percent + '%';
//          console.log(id+":"+percentStr);
          var r = $('#m_file' + id);
          r.find('div.determinate').width(percentStr);
          r.find('b').text("正在上传/"+percentStr);
        },
        onUploadSuccess: function(id, data){
//           console.log('Upload of file #' + id + ' completed');
           console.log('Server Response for file #' + id + ': ' + JSON.stringify(data));
           $('#m_file' + id).find('b').text("上传完成");
        },
        onUploadError: function(id, message){
//          console.log('Failed to Upload file #' + id + ': ' + message);
          $('#m_file' + id).find('b').text("上传失败");
          toastError("文件上传失败 "+ message)
        },
        onFileTypeError: function(file){
          console.log(file.name + ' cannot be added: must be an image');
          $('#m_file' + id).find('b').text("上传失败");
          toastError("文件"+file.name+"上传失败(文件类型不符)")
        },
        onFileSizeError: function(file){
//           console.log(file.name + 'cannot be added: size excess limit');
           $('#m_file' + id).find('b').text("上传失败");
           toastError("文件"+file.name+"上传失败(文件过大)")
        },
        onFallbackMode: function(message){
            toastError('Browser not supported(do something else here!): ' + message)
        }
      });