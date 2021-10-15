<?PHP
if(!empty($_FILES['uploaded_file'])) {
  {
    $path = "/opt/files";
    $filename = basename( $_FILES['uploaded_file']['name']);

    if(move_uploaded_file($_FILES['uploaded_file']['tmp_name'], "$path/$filename")) {
      echo "The file ".  basename( $_FILES['uploaded_file']['name']). 
      " has been uploaded";
    } else{
        echo "There was an error uploading the file, please try again!";
    }
  }
} else {
    echo "Please specify a file with correct key; 'uploaded_file=FILENAME'";
}
?>
