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

    // for debugging purposes
    /*$str = "";
    foreach ($data as $key => $value) {
        $str .= $key.": ".(is_array($value) ? "[".implode($value, ", ")."]" : $value)."\n";
    }
    file_put_contents("data.txt", $str);*/

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
            $value = $bytes[$i] - ($bytes[$i] >> 0x07 ? pow(2, 8) : 0);
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
    $sign = $bytes[0] >> 0x07;
    return ($bytes[0] << 0x08) + $bytes[1] - ($sign ? pow(2, 16) : 0);
}

function toInt($bytes) {
    $result = ($bytes[0] << 0x18) + ($bytes[1] << 0x10) + ($bytes[2] << 0x08) + $bytes[3];
    if (PHP_INT_SIZE === 8) { // we're using 64-bit integers so we can't rely on overflow to sign it for us
        $sign = $bytes[0] >> 0x07;
        $result -= $sign ? pow(2, 32) : 0;
    }
    return $result;
}

function toLong($bytes) {
    if (PHP_INT_SIZE === 8) { // we can use 64-bit arithmetic, yay
        return ($bytes[0] << 0x38) + ($bytes[1] << 0x30) + ($bytes[2] << 0x28) + ($bytes[3] << 0x20)
             + ($bytes[4] << 0x18) + ($bytes[5] << 0x10) + ($bytes[6] << 0x08) + $bytes[7];
    } else { // we can't use 64-bit arithmetic
        $sign = $bytes[0] >> 0x07;
        $result = lsh($bytes[0], 0x38);
        $result = bcadd($result, lsh($bytes[1], 0x30));
        $result = bcadd($result, lsh($bytes[2], 0x28));
        $result = bcadd($result, lsh($bytes[3], 0x20));
        $result = bcadd($result, lsh($bytes[4], 0x18));
        $result = bcadd($result, lsh($bytes[5], 0x10));
        $result = bcadd($result, lsh($bytes[6], 0x08));
        $result = bcadd($result, $bytes[7]);
        if ($sign) {
            $result = bcsub($result, bcpow(2, 64));
        }
        return $result;
    }
}

function toFloat($bytes) {
    return unpack('f', pack('i', toInt($bytes)));
}

function toDouble($bytes) {
    if (PHP_INT_SIZE === 8) { // we can use 64-bit arithmetic
        return unpack('d', pack('l', toLong($bytes)));
    } else { // otherwise, we're kinda stuck and have to stomach the precision loss
        $sign = $bytes[0] >> 0x07;
        $exp = ((($bytes[0] & 0x7F) << 0x04) + ($bytes[1] >> 0x04)) - 1023;
        $exp = ($exp < 0 ? -1 : 1) * (abs($exp) & 0xFF) + 127;
        $newBytes = array();
        $newBytes[0] = ($sign << 0x07) + ($exp >> 0x01);
        $newBytes[1] = (($exp & 0x01) << 0x07) + ($bytes[2] >> 0x01);
        $newBytes[2] = (($bytes[2] & 0x01) << 0x07) + ($bytes[3] >> 0x01);
        $newBytes[3] = (($bytes[3] & 0x01) << 0x07) + ($bytes[4] >> 0x01);
        return toFloat($newBytes);
    }
}

function array_pack($arr) {
    return call_user_func_array("pack", array_merge(array("c*"), $arr));
}

// because PHP doesn't have always native long support, nor does it have ANY support for long int bitwise operations
function lsh($num, $bits) {
    return bcmul($num, bcpow('2', $bits));
}

function rsh($num, $bits) {
    return bcdiv($num, bcpow('2', $bits));
}
?>
