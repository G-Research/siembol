<?php

    $basepath = '/opt/files';
    $objects = new RecursiveIteratorIterator(new RecursiveDirectoryIterator($basepath), RecursiveIteratorIterator::SELF_FIRST);
    iterateDirectory($objects, $basepath);

    function iterateDirectory($objects, $basepath) 
    {
        $dom = new DomDocument("1.0");
        $h3 = $dom->createElement("h3", "Index of /opt/files");
        $dom->appendChild($h3);
        $list = $dom->createElement("ul");
        $dom->appendChild($list);
        $node = $list;
        $depth = 0;
        foreach($objects as $name => $object){
            $file = $object->getFilename();
            if ($file === '.') continue;
            if ($file === '..') continue;

            if ($objects->getDepth() == $depth){
               //just add another li as the depth hasn't changed
               if ($object-> isDir()) {
                  $li = $dom->createElement('li', $file);
               } else {
                  $li = create_href_li($dom, $object->getPath(), $file, $basepath);
               }
               $node->appendChild($li);
            }
            elseif ($objects->getDepth() > $depth){
                //the depth increased, the last li is a non-empty folder
                $li = $node->lastChild;
                $ul = $dom->createElement('ul');
                $li->appendChild($ul);

                if ($object-> isDir()) {
                   $ul->appendChild($dom->createElement('li', $file));
                } else {
                   $li = create_href_li($dom, $object->getPath(), $file, $basepath);
                   $ul->appendChild($li);
                }
                $node = $ul;
            }
            else { //depth decreased, going back/up
               $difference = $depth - $objects->getDepth();
               for ($i = 0; $i < $difference; $difference--) {
                  $node = $node->parentNode->parentNode;
               }
               $li = $dom->createElement('li', $file);
               $node->appendChild($li);
            }
            $depth = $objects->getDepth();
        }
        echo $dom->saveHtml();
    }

    function create_href_li($dom, $path, $file, $basepath)
    {
        $script = "download.php?filename=";
        $li = $dom->createElement('li', "");
        $a = $dom->createElement('a', $file);
        if (str_starts_with($path, $basepath)) {
           $path = substr($path, strlen($basepath), strlen($path));
        }
        $link = $script . $path . "/" . $file;
        $a->setAttribute('href', $link);
        $li->appendChild($a);
        return $li;
    }
?>