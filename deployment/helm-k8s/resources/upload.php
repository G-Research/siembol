<?PHP
  openlog("uploadFileScript", LOG_PID | LOG_PERROR, LOG_LOCAL0);
  $MAX_SIZE = "30MB";
  if(!empty($_FILES['uploaded_file'])) {
    $error_code = $_FILES['uploaded_file']['name'];
    $filename = basename($_FILES['uploaded_file']['name']);
    if (!check_filename($filename)) {
      logs(LOG_WARNING, "Warning: Filename is not valid, accepting names like: myfile.json, my_file-3.json");
      closelog();
      http_response_code(422);
      exit("Warning: Filename is not valid.");
    }
    if ($error_code == 1) { //the uploaded file exceeds the upload_max_filesize directive in php.ini-local
      logs(LOG_WARNING, "File: $filename exceeded size limit. Must be less than $MAX_SIZE");
      closelog();
      http_response_code(413);
      exit("File: $filename exceeded size limit. Must be less than $MAX_SIZE");
    }

    $base_path = "/opt/files";
    $user_full_path = "$base_path/";
    if (isset($_POST['directory_path'])) {
      $user_dir  = $_POST['directory_path'];
      //the allowed characters, i.e. we do not accept e.g.: ../ . %2e%2e%2f etc.
      if (!preg_match("/^(\/[a-zA-Z0-9]{1,}){1,}$/", $user_dir)) {
        logs(LOG_WARNING, "Warning: Not a valid directory path");
        closelog();
        http_response_code(422);
        exit("Warning: Not a valid directory path");
      }

      $user_full_path = $base_path . $user_dir;
      if (!file_exists($user_full_path)) {
        if (mkdir($user_full_path, 0777, true)) {
          logs(LOG_INFO, "Directory $user_full_path created.");
        } else {
          logs(LOG_INFO, "Directory $user_full_path could not be created.");
        }
      } else {
        logs(LOG_INFO, "Directory $user_full_path exists.");
      } 
    }

    $real_path = realpath($user_full_path);
    $f_size = $_FILES['uploaded_file']['size'];
    if (move_uploaded_file($_FILES['uploaded_file']['tmp_name'], "$real_path/$filename")) {
      $msg = "The file ". basename($filename). " (filesize: $f_size bytes) has been uploaded to $real_path";
      logs(LOG_INFO, $msg);
    } else {
      $msg = "Error uploading the file: ". basename($filename). " (filesize: $f_size bytes) to $real_path";
      logs(LOG_INFO, $msg);
      http_response_code(500);
    }
  } else {
     http_response_code(400);
     logs(LOG_WARNING, "User specified wrong key, must be 'uploaded_file=FILENAME'");
  }
  closelog();

  function logs($level, $msg)
  {
    $timestamp = date("d/M/Y H:i:s");
    syslog($level, "[$timestamp] $msg");
  }
  function check_filename($name)
  {
    //the allowed filename string, accepts e.g. myfile.json, my-file-3.json
    return preg_match("/^([a-zA-Z0-9-_]{1,})(.json)$/", $name);
  }
?>
