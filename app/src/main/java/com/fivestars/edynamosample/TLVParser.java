package com.fivestars.edynamosample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;


// TLVParser provides functions for parsing data encoded with the TLV (tag-length-value) scheme.
// https://en.wikipedia.org/wiki/Type-length-value
// Used in EdynamoPlugin to parse TLV-encoded data from the device.
public class TLVParser {
    public static List<HashMap<String, String>> parseTLVData(byte[] data, boolean hasSizeHeader, String stringPadRight) {
        List<HashMap<String, String>> fillMaps = new ArrayList<>();

        if (data != null)  {
            int dataLen = data.length;

            if (dataLen >= 2) {
                int tlvLen;
                byte tlvData[] = data;

                if (hasSizeHeader) {
                    tlvLen = ((data[0] & 0x000000FF) << 8) + (data[1] & 0x000000FF);

                    tlvData = new byte[tlvLen];
                    System.arraycopy(data, 2, tlvData, 0, tlvLen);
                }

                int iTLV;
                int iTag;
                int iLen;
                boolean bTag;
                boolean bMoreTagBytes;
                boolean bConstructedTag;
                boolean bPrivateClassFlag;
                byte byteValue;
                int lengthValue;

                byte tagBytes[] = null;

                final byte MoreTagBytesFlag 	= (byte) 0x80;
                final byte PrivateClassFlag 	= (byte) 0xC0;
                final byte ConstructedFlag 		= (byte) 0x20;
                final byte MoreLengthFlag 		= (byte) 0x80;
                final byte OneByteLengthMask 	= (byte) 0x7F;

                byte TagBuffer[] = new byte[50];

                bTag = true;
                iTLV = 0;

                while (iTLV < tlvData.length) {
                    byteValue = tlvData[iTLV];

                    if (bTag) {
                        // Get Tag
                        iTag = 0;
                        bMoreTagBytes = true;

                        while (bMoreTagBytes && (iTLV < tlvData.length)) {
                            byteValue = tlvData[iTLV];
                            iTLV++;

                            TagBuffer[iTag] = byteValue;
                            iTag++;

                            bMoreTagBytes = ((byteValue & MoreTagBytesFlag) == MoreTagBytesFlag);
                        }

                        tagBytes = new byte[iTag];
                        System.arraycopy(TagBuffer, 0, tagBytes, 0, iTag);

                        bTag = false;
                    }
                    else {
                        // Get Length
                        lengthValue = 0;

                        if ((byteValue & MoreLengthFlag) == MoreLengthFlag) {
                            int nLengthBytes = byteValue & OneByteLengthMask;

                            iTLV++;
                            iLen = 0;

                            while ((iLen < nLengthBytes) && (iTLV < tlvData.length)) {
                                byteValue = tlvData[iTLV];
                                iTLV++;
                                lengthValue = ((lengthValue & 0x000000FF) << 8) + (byteValue & 0x000000FF);
                                iLen++;
                            }
                        }
                        else {
                            lengthValue = byteValue & OneByteLengthMask;
                            iTLV++;
                        }

                        int tagByte = tagBytes[0];

                        bConstructedTag = ((tagByte & ConstructedFlag) == ConstructedFlag);
                        bPrivateClassFlag = ((tagByte & PrivateClassFlag) == PrivateClassFlag);

                        if (bConstructedTag || bPrivateClassFlag) {
                            // Constructed
                            HashMap<String, String> map = new HashMap<>();
                            map.put("tag", getHexString(tagBytes, 0, stringPadRight));
                            map.put("len", "" + lengthValue);
                            map.put("value", "[Container]");
                            fillMaps.add(map);
                        }
                        else {
                            // Primitive
                            int endIndex = iTLV + lengthValue;

                            if (endIndex > tlvData.length)
                                endIndex =  tlvData.length;

                            byte valueBytes[] = null;
                            int len = endIndex - iTLV;
                            if (len > 0) {
                                valueBytes = new byte[len];
                                System.arraycopy(tlvData, iTLV, valueBytes, 0, len);
                            }

                            HashMap<String, String> map = new HashMap<>();
                            map.put("tag", getHexString(tagBytes, 0, stringPadRight));
                            map.put("len", "" + lengthValue);

                            if (valueBytes != null) {
                                map.put("value", getTextString(valueBytes, 0));
                            }
                            else {
                                map.put("value", "");
                            }

                            fillMaps.add(map);

                            iTLV += lengthValue;
                        }

                        bTag = true;
                    }
                }
            }
        }

        return fillMaps;
    }

    public static List<HashMap<String, String>> parseEMVData(byte[] data, boolean hasSizeHeader, String stringPadRight) {
        List<HashMap<String, String>> fillMaps = new ArrayList<>();

        if (data != null) {
            int dataLen = data.length;

            if (dataLen >= 2) {
                int tlvLen;
                byte tlvData[] = data;

                if (hasSizeHeader) {
                    tlvLen = ((data[0] & 0x000000FF) << 8) + (data[1] & 0x000000FF);

                    tlvData = new byte[tlvLen];
                    System.arraycopy(data, 2, tlvData, 0, tlvLen);
                }

                int iTLV;
                int iTag;
                int iLen;
                boolean bTag;
                boolean bMoreTagBytes;
                boolean bConstructedTag;
                byte byteValue;
                int lengthValue;

                byte tagBytes[] = null;

                final byte MoreTagBytesFlag1 	= (byte) 0x1F;
                final byte MoreTagBytesFlag2 	= (byte) 0x80;
                final byte ConstructedFlag 		= (byte) 0x20;
                final byte MoreLengthFlag 		= (byte) 0x80;
                final byte OneByteLengthMask 	= (byte) 0x7F;

                byte TagBuffer[] = new byte[50];

                bTag = true;
                iTLV = 0;

                while (iTLV < tlvData.length) {
                    byteValue = tlvData[iTLV];

                    if (bTag) {
                        // Get Tag
                        iTag = 0;
                        bMoreTagBytes = true;

                        while (bMoreTagBytes && (iTLV < tlvData.length)) {
                            byteValue = tlvData[iTLV];
                            iTLV++;

                            TagBuffer[iTag] = byteValue;

                            if (iTag == 0) {
                                bMoreTagBytes = ((byteValue & MoreTagBytesFlag1) == MoreTagBytesFlag1);
                            }
                            else {
                                bMoreTagBytes = ((byteValue & MoreTagBytesFlag2) == MoreTagBytesFlag2);
                            }

                            iTag++;
                        }

                        tagBytes = new byte[iTag];
                        System.arraycopy(TagBuffer, 0, tagBytes, 0, iTag);

                        bTag = false;
                    }
                    else {
                        // Get Length
                        lengthValue = 0;

                        if ((byteValue & MoreLengthFlag) == MoreLengthFlag) {
                            int nLengthBytes = byteValue & OneByteLengthMask;

                            iTLV++;
                            iLen = 0;

                            while ((iLen < nLengthBytes) && (iTLV < tlvData.length)) {
                                byteValue = tlvData[iTLV];
                                iTLV++;
                                lengthValue = ((lengthValue & 0x000000FF) << 8) + (byteValue & 0x000000FF);
                                iLen++;
                            }
                        }
                        else {
                            lengthValue = byteValue & OneByteLengthMask;
                            iTLV++;
                        }

                        int tagByte = tagBytes[0];

                        bConstructedTag = ((tagByte & ConstructedFlag) == ConstructedFlag);

                        if (bConstructedTag) {
                            // Constructed
                            HashMap<String, String> map = new HashMap<>();
                            map.put("tag", getHexString(tagBytes, 0, stringPadRight));
                            map.put("len", "" + lengthValue);
                            map.put("value", "[Container]");
                            fillMaps.add(map);
                        }
                        else {
                            // Primitive
                            int endIndex = iTLV + lengthValue;

                            if (endIndex > tlvData.length)
                                endIndex =  tlvData.length;

                            byte valueBytes[] = null;
                            int len = endIndex - iTLV;
                            if (len > 0) {
                                valueBytes = new byte[len];
                                System.arraycopy(tlvData, iTLV, valueBytes, 0, len);
                            }

                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("tag", getHexString(tagBytes, 0, stringPadRight));
                            map.put("len", "" + lengthValue);

                            if (valueBytes != null)
                                map.put("value", getHexString(valueBytes));
                            else
                                map.put("value", "");

                            fillMaps.add(map);

                            iTLV += lengthValue;
                        }

                        bTag = true;
                    }
                }
            }
        }

        return fillMaps;
    }

    public static String getTagValue(List<HashMap<String, String>> fillMaps, String tagString) {
        String valueString = "";

        for (HashMap<String, String> map : fillMaps) {
            if (map.get("tag").equalsIgnoreCase(tagString)) {
                valueString = map.get("value");
            }
        }

        return valueString;
    }

    public static String getTextString(byte[] data, int start) {
        String result = "";

        if (data != null && data.length > 0) {
            result = getTextString(data, start, data.length);
        }

        return result;
    }

    public static String getTextString(byte[] data, int start, int length) {
        String result = "";

        if (data != null && data.length > 0) {
            StringBuilder stringBuilder = new StringBuilder(data.length+1);
            for(int i = start; i < length; i++) {
                try {
                    stringBuilder.append(String.format("%c", data[i]));
                }
                catch (Exception ex) {
                    stringBuilder.append("<?>");
                }
            }
            result = stringBuilder.toString();
        }

        return result;
    }

    public static String getHexString(byte[] data) {
        return getHexString(data, 0, "");
    }

    public static String getHexString(byte[] data, int start, String stringPadRight) {
        String result = "";

        if (data != null && data.length > 0) {
            int byteLength = 2;

            if (stringPadRight != null) {
                byteLength += stringPadRight.length();
            }

            StringBuilder stringBuilder = new StringBuilder(data.length*byteLength+1);

            for(int i = start; i < data.length; i++) {
                try {
                    stringBuilder.append(String.format("%02X", data[i]));
                }
                catch (Exception ex) {
                    stringBuilder.append("  ");
                }

                if (stringPadRight != null) {
                    stringBuilder.append(stringPadRight);
                }
            }
            result = stringBuilder.toString();
        }

        return result;
    }

    public static byte[] getByteArrayFromHexString(String hexString) {
        return getByteArrayFromHexString(hexString, "");
    }

    public static byte[] getByteArrayFromHexString(String hexString, String stringPadRight) {
        int byteLength = 2;

        if (stringPadRight != null) {
            byteLength += stringPadRight.length();
        }

        byte result[] = null;

        if (hexString != null) {
            result = new byte[hexString.length() / byteLength];

            char hexCharArray[] = hexString.toUpperCase().toCharArray();

            StringBuffer sbCurrent;

            for (int i = 0; i < result.length; i++) {
                sbCurrent = new StringBuffer("");
                sbCurrent.append(String.valueOf(hexCharArray[i*byteLength]));
                sbCurrent.append(String.valueOf(hexCharArray[i*byteLength + 1]));
                try {
                    result[i] = (byte) Integer.parseInt(sbCurrent.toString(), 16);
                } catch (Exception ignored) {

                }
            }
        }

        return result;
    }

    /**
     * Card holder data (tagged with DFDF4D) needs to be hex decoded parsed.
     * @param cardHolderData - format similar to: ;4147000040000859=200420100000000000?
     *                       and hex encoded on top of that
     * @return map of the parsed values (iin, last_four, exp_date, service_code)
     */
    /**
     * Card holder data is parsed from parsed EMV data.  Hex encoded name will come from
     * tag 5F20 and the remaining data will come from tag DFDF4D, which is also hex encoded.
     * When the additional data is decoded, it will be in a format similar to:
     * ;414700000000859=200420100000000000?
     * We then parse that to get the remaining data.  At this time, if there are any issues
     * with the parsing, we will just return whatever we have parsed.
     * @param parsedTLVList - result of parseEMVData (parsed EMV data)
     * @return Card holder information
     */
    public static CardHolderInfo parseCardHolderDataEMVData(List<HashMap<String, String>> parsedTLVList) {
        CardHolderInfo cardHolderInfo = new CardHolderInfo();
        try {
            String hexName = TLVParser.getTagValue(parsedTLVList, "5F20");
            cardHolderInfo.setName(new String(TLVParser.getByteArrayFromHexString(hexName)));
            String hexInfo = TLVParser.getTagValue(parsedTLVList, "DFDF4D");
            String info = new String(TLVParser.getByteArrayFromHexString(hexInfo));
            String[] parts = info.split("=");
            // Get first six after sentinal in part 0
            cardHolderInfo.setIin(parts[0].substring(1,7));
            // Get last four in part 0
            cardHolderInfo.setLastFour(parts[0].substring(parts[0].length() - 4));
            // Get exp date (first four) from part 1
            cardHolderInfo.setExpirationDate(parts[1].substring(0,4));
            // Get service code (three after exp date)
            cardHolderInfo.setServiceCode(parts[1].substring(4,7));
        } catch (Exception ignored) {

        }
        return cardHolderInfo;
    }

}
