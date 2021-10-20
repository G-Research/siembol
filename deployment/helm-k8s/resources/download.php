<?php
parse_str($_SERVER['QUERY_STRING'], $output);
$basepath = '/opt/files';
$filename = $output['filename'];
$path=$basepath . '/'. $filename;
$realpath = realpath($path);
if ($realpath === false) {
    echo "File does not exist.";
}
elseif (strpos(realpath($path), $basepath) !== 0) {
    echo "Wrong folder path.";
} else {
    echo file_get_contents($path);
}
?>
