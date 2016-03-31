   function getJsonArrayLength(jsonArr){
      var length = 0;
      for(var key in jsonArr){
         length++;
      }
      return length;
    }

    var icons = ["filesystem_icon_word.png","filesystem_icon_apk.png","filesystem_icon_photo.png",
            "filesystem_icon_excel.png","filesystem_icon_rar.png","filesystem_icon_web.png",
            "filesystem_icon_text.png","filesystem_icon_music.png","filesystem_icon_movie.png",
            "filesystem_icon_pdf.png","filesystem_icon_ppt.png","filesystem_icon_default.png"];
    function getFileIcon(filename){
       var ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
       switch(ext){
               case "doc":return icons[0];case "apk":  return icons[1];case "bmp":   return icons[2];case "docx": return icons[0];
               case "xls": return icons[3];case "xlsx": return icons[3];case "gif": return icons[2];case "gz":return icons[4];
               case "htm":return icons[5];case "html": return icons[5];case "jpeg": return icons[2];case "jpg": return icons[2];
               case "log": return icons[6];case "mp3":return icons[7];case "mp4":return icons[8];case "pdf":return icons[9];
               case "png": return icons[2];case "ppt":return icons[10];case "pptx": return icons[10];case "tar":return icons[4];
               case "tgz": return icons[4]; case "txt":return icons[6];case "wav":return icons[7];case "wmv": return icons[8];
               case "wps":return icons[0];case "zip": return icons[4]; case "7z": return icons[4]; default: return icons[11];
       }
    }

    var length_str = ["B", "KB", "MB", "GB", "TB"];
    var radix = 1024;
    function uniformFileSize(file_length){
        var next = 0,i = 0;
        while (parseInt(file_length / radix) > 0) {
            next =  (file_length % radix);
            file_length = parseInt(file_length / radix);
            i++;
        }
        if (i == 0) {   return file_length + length_str[i];}
        else { return file_length + "." + ((next >= 1000 ? 1 : parseInt(next / 100)) + "" + parseInt(next / 10) % 10) + length_str[i];
        }
    }

        function Toast(message){
             var snackbarContainer = document.querySelector('#toast-t');
             var data = {message:message};
             snackbarContainer.MaterialSnackbar.showSnackbar(data);
        }