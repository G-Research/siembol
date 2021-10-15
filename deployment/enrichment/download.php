<?php
parse_str($_SERVER['QUERY_STRING'], $output); //TODO: Need some input validation
$filename = $output['filename'];
$path='/opt/files/' . $filename;
echo file_get_contents($path);
?>
