<?php

    $basepath = '/opt/files';
    $objects = new RecursiveIteratorIterator(new RecursiveDirectoryIterator($basepath, RecursiveIteratorIterator::SELF_FIRST));
    iterateDirectory($objects, $basepath);

    function iterateDirectory($objects, $basepath) 
    {
        $dom = new Document("1.0");
        $h3 = $dom->createElement("h3", "Index of /opt/files");
        $dom->appendChild($h3);
        
    }
?>