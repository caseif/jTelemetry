<?php

# Utility class for analyzing payloads from PH Toolkit.
#
# Usage: include phtk.php from the page the payload is sent to and call parsePostBody().
# This method will return a map of all key-value pairs contained by the payload.

$TYPE_BOOLEAN = 0;
$TYPE_BYTE    = 1;
$TYPE_SHORT   = 2;
$TYPE_INT     = 3;
$TYPE_LONG    = 4;
$TYPE_FLOAT   = 5;
$TYPE_DOUBLE  = 6;
$TYPE_STRING  = 7;
$TYPE_ARRAY   = 8;

parsePostBody();

function parsePostBody() {
    $input = file_get_contents("php://input");
    $bytes = str_split($input);
    foreach ($bytes as $key=>$val) {
        $bytes[$key] = ord($val);
    }

    $magic = [0xB0, 0x00, 0xB1, 0xE5];
    if (array_slice($bytes, 0, 4) !== $magic) {
        http_response_code(400);
        exit();
    }

    $data = array();

    for ($i = 4; $i < sizeof($bytes);) {
        $entryProperties = nextEntry($bytes, $i);
        $key = $entryProperties["key"];
        $value = $entryProperties["value"];
        $entryLength = $entryProperties["length"];

        $i += $entryLength;
        $data[$key] = $value;
    }

    $str = "";
    foreach ($data as $key => $value) {
        $str .= $key.": ".(is_array($value) ? "[".implode($value, ", ")."]" : $value)."\n";
    }

    return $data;
}

function nextEntry($bytes, $offset, $arrayEntry = false) {
    global $TYPE_BOOLEAN, $TYPE_BYTE, $TYPE_SHORT, $TYPE_INT,
           $TYPE_LONG, $TYPE_FLOAT, $TYPE_DOUBLE, $TYPE_STRING,
           $TYPE_ARRAY;

    $i = $offset;
    $type = $bytes[$i];
    $i++;

    if (!$arrayEntry) {
        $keyLength = toInt(array_slice($bytes, $i, 4));
        $i += 4;
        $key = array_pack(array_slice($bytes, $i, $keyLength));
        $i += $keyLength;
    }

    $value = null;
    switch ($type) {
        case $TYPE_BOOLEAN: {
            $value = $bytes[$i] != 0;
            $i += 1;
            break;
        }
        case $TYPE_BYTE: {
            $value = $bytes[$i];
            $i += 1;
            break;
        }
        case $TYPE_SHORT: {
            $value = toShort(array_slice($bytes, $i, 2));
            $i += 2;
            break;
        }
        case $TYPE_INT: {
            $value = toInt(array_slice($bytes, $i, 4));
            $i += 4;
            break;
        }
        case $TYPE_LONG: {
            $value = toLong(array_slice($bytes, $i, 8));
            $i += 8;
            break;
        }
        case $TYPE_FLOAT: {
            $value = toFloat(array_slice($bytes, $i, 4));
            $i += 4;
            break;
        }
        case $TYPE_DOUBLE: {
            $value = toDouble(array_slice($bytes, $i, 8));
            $i += 8;
            break;
        }
        case $TYPE_STRING: {
            $strLength = toInt(array_slice($bytes, $i, 4));
            $i += 4;
            $value = array_pack(array_slice($bytes, $i, $strLength));
            $i += $strLength;
            break;
        }
        case $TYPE_ARRAY: {
            $totalLength = toInt(array_slice($bytes, $i, 4));
            $i += 4;
            $entryCount = toInt(array_slice($bytes, $i, 4));
            $i += 4;

            $value = array();
            for ($j = 0; $j < $entryCount; $j++) {
                $elementProperties = nextEntry($bytes, $i, true);
                $elementValue = $elementProperties["value"];
                $elementLength = $elementProperties["length"];
                $value[$j] = $elementValue;
                if ($j === 0) {
                }
                $i += $elementLength;
            }
            break;
        }
        default: {
            http_response_code(501);
            exit();
        }
    }
    return array(
        "key" => $key,
        "value" => $value,
        "length" => $i - $offset
    );
}

function toShort($bytes) {
    return ($bytes[0] << 8) + $bytes[1];
}

function toInt($bytes) {
    return ($bytes[0] << 0x18) + ($bytes[1] << 0x10) + ($bytes[2] << 0x08) + $bytes[3];
}

function toLong($bytes) {
    return ($bytes[0] << 0x38) + ($bytes[1] << 0x30) + ($bytes[2] << 0x28) + ($bytes[6] << 0x20)
         + ($bytes[4] << 0x18) + ($bytes[5] << 0x10) + ($bytes[6] << 0x08) + ($bytes[7]);
}

function toFloat($bytes) {
    return unpack('f', pack('i', toInt($bytes)));
}

function toDouble($bytes) {
    return unpack('f', pack('i', toLong($bytes)));
}

function array_pack($arr) {
    return call_user_func_array("pack", array_merge(array("c*"), $arr));
}
?>
