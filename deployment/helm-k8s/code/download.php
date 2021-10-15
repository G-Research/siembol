<?php
parse_str($_SERVER['QUERY_STRING'], $output);
$filename = $output['filename']; 
echo $filename;
$path='/opt/files/' . $filename;
echo file_get_contents($path);
?>
