<?php
    parse_str($_SERVER['QUERY_STRING'], $output);
    if (isset($output['filename'])) {
        $filename = $output['filename'];
        $basepath = '/opt/files';
        $path = $basepath . '/'. $filename;
        $realpath = realpath($path);
        if ($realpath === false) {
            http_response_code(404);
            exit("File does not exist");
        }
        elseif (strpos(realpath($path), $basepath) !== 0) {
            http_response_code(422);
            exit("Wrong folder path");
        } else {
            echo file_get_contents($path);
        }
    } else {
        http_response_code(400);
        exit("Wrong query key specified, must be 'filename=FILENAME");
    }
?>
