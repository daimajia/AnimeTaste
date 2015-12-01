package com.diandi.klob.player.spider;


import android.util.Base64;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * *******************************************************************************
 * *********    Author : klob(kloblic@gmail.com) .
 * *********    Date : 2015-12-01  .
 * *********    Time : 18:04 .
 * *********    Version : 1.0
 * *********    Copyright © 2015, klob, All Rights Reserved
 * *******************************************************************************
 */
public class Requester {
    public static String SERVER_URL = "http://115.28.106.179:8080/index.jsp?newsId=";

    public static void main(String[] args) throws Exception {
        String url1 = "http://v.youku.com/v_dfgsdfshow/id_XMTM5NDE3OfghfghfdhsDU5Ng==.html?from=y1.6-85.3.1.af0bc5280e8a11e5b5ce";
        // System.out.println(url1);
        // url1 = URLEncoder.encode(url1);
        //   url1 = URLEncoder.encode(url1) + "&format=high";
        // url1 = URLEncoder.encode(url1) + "&format=super";
        // System.out.println(url1);
        // Document document = Jsoup.connect("http://115.28.106.179:8080/index.jsp?newsId=http://v.youku.com/v_show/id_XMTM5NDE3ODU5Ng==.html?from=y1.6-85.3.1.af0bc5280e8a11e5b5ce" + url1).get();
        // System.out.print(document);
        // String a = decryptFlvcd("6f3ded0234eb056808331b883a1cc372ec2f98d8ab08069a924f59dc654d8e098d5ca3d259500474002c549addbc8e066494d83265581e0f217b7b0004f3309f0019b09d6a61ff13747a266a1a1040ec37f09e3e3f9fd27168f75463686fad1115e97217c21e26f61bd429dec9e0df7f4370cff5ef6e4a6c58babeca9479e1350698030f9e140b1f857822988b57661f1d38f03f5993fb726860e0bc6abf64b794bf026bf1d5f314b37e08e9c2e6f904d816de69fe53237a33555c1ee9c0d08a2efdaf958d9a27cc37f6c4e05045177a39f7f7d48710f75c399f0a9a74860a0bfbc0298c9cff3e5398ebc939448d80528cc5b82ea89ee580343903ba0d10ad42763d9d040eea42243c1d74c13876fd77d4119cfab61c689bb84b2ad86365b80fb4289e8c5369727f074a549fc4ba814b0182f442054335332507400b308e0492213499ca7341ca387518153729173dff38a8bc793f9f94325cf16a715a6cae7800c64c20f400368028fb14e2d0b8d04e706381b2d81b734853dcb3f5880ce27826c0132faa074507880e55848e576613045fad4a6f8ff414765f8cfa5c9132bba6df6176f1c1f70d891a08b4f1e2b85dde2cd132de0a0a3c3059405cfcd0dd8204d1eba9919b30ad29f4d1f6744d0e6d1ad3dadfd7709b6a55a96c8c11b31805a9f720d7faf01169fbcfaa5f2596bb72f4c0d20dcbfedebf580878f772279a405c3e810f0cd10a3d263004c81554e17eda029ddfb21f70bf954d55e96277f13d844ca3e04c7f444b0c164fecc6aa95456dc0d33e5372651b1679770b11f35d921034a1fd5444dc195606373b237043bf15a9b4433db6c1306cce7e606e32c62b14f90044c20310942ec5769ddf96b373474dac9eeb364d4e4abaddcd960f821837e20800982a6835bb787391893079280629832d46acc5135f6ed9bc648563faa1a47170cfff9415a56d40b2cbce926fee10983bea7c151d033e481ee7fea6fa2b999d88bbed06a835cbd6bf6e5a6e2509eb9bf0e760b52043ab0b9315b00b1fafab28e9c9973f56e6bbc8142596bf6ef7b5847bfec3b1a3601c2bfc74338e03723d820701d21a47231b07a513439674ed0198cba80f63dc946270844a64a74c9d7581d7596e0428183e57e9ddaba4537989e2737d7519460e226f1510bb2fcd0428b2c86369ab275a06776a3f3940f53af3bc643da5f46361e0486d7735b2670ec6721df93c17bf37d70cc2cb87cf7d6870a7bdc1094d734fe5de8b970edc6028ee2e14a0356b56926d41abbb4958260939f5034684ef4e596284c4709551f693a4537ec3a0881e894561aedfce8a7bd82eec38d8526e21303f7a15d1ffaee6329a9aa98cb309d00293dee56b5c2b6731e6c687e44d8b2656aa31ae14d7695897cf1eccff963014ccbad4201490dd58f49bb120c9c6b99452183fb86e1dfc3e6f3aed6106db1d37206f04da1a499239");
        // System.out.print(a);
        System.out.print("data:" + request(url1));

    }

    public static String request(String url) throws Exception {
        Document document = Jsoup.connect(SERVER_URL + url).get();
        String h5 = document.body().text();
        h5 = decryptFlvcd(h5);
        System.out.print(h5);
        String data = substringBetween(h5, "[CDATA[", "]]");

        return data;


    }

    public static String substringBetween(String paramString1, String paramString2, String paramString3) {
        if ((paramString1 == null) || (paramString2 == null) || (paramString3 == null))
            return "";
        int i = paramString1.lastIndexOf(paramString2);
        if (i != -1) {
            int j = paramString1.lastIndexOf(paramString3);
            if (j != -1)
                return paramString1.substring(i + paramString2.length(), j);
        }
        return "";
    }

    public static String decryptFlvcd(String paramString)
            throws Exception {
        char[] arrayOfChar = paramString.toCharArray();
        int i = paramString.length();
        byte[] arrayOfByte = new byte[i >> 1];
        int j = 0;
        int k = 0;
        while (true) {
            if (k >= i)
                return new String(Base64.decode(transformFlvcd(arrayOfByte), 0), "utf-8");
            int l = Character.digit(arrayOfChar[k], 16) << 4;
            int i1 = k + 1;
            int i2 = l | Character.digit(arrayOfChar[i1], 16);
            k = i1 + 1;
            arrayOfByte[j] = (byte) (i2 & 0xFF);
            ++j;
        }
    }

    private static byte[] transformFlvcd(byte[] paramArrayOfByte)
            throws Exception {
        byte[] arrayOfByte1 = new byte[256];
        arrayOfByte1[0] = 63;
        arrayOfByte1[1] = 121;
        arrayOfByte1[2] = -44;
        arrayOfByte1[3] = 54;
        arrayOfByte1[4] = 86;
        arrayOfByte1[5] = -68;
        arrayOfByte1[6] = 114;
        arrayOfByte1[7] = 15;
        arrayOfByte1[8] = 108;
        arrayOfByte1[9] = 94;
        arrayOfByte1[10] = 77;
        arrayOfByte1[11] = -15;
        arrayOfByte1[12] = 89;
        arrayOfByte1[13] = 46;
        arrayOfByte1[14] = -81;
        arrayOfByte1[15] = 4;
        arrayOfByte1[16] = -114;
        arrayOfByte1[17] = 69;
        arrayOfByte1[18] = -88;
        arrayOfByte1[19] = -79;
        arrayOfByte1[20] = -26;
        arrayOfByte1[21] = 91;
        arrayOfByte1[22] = 50;
        arrayOfByte1[23] = -19;
        arrayOfByte1[24] = -37;
        arrayOfByte1[25] = 38;
        arrayOfByte1[26] = 27;
        arrayOfByte1[27] = -80;
        arrayOfByte1[28] = 7;
        arrayOfByte1[29] = 32;
        arrayOfByte1[30] = -64;
        arrayOfByte1[31] = 127;
        arrayOfByte1[32] = -41;
        arrayOfByte1[33] = 27;
        arrayOfByte1[34] = -49;
        arrayOfByte1[35] = -89;
        arrayOfByte1[36] = 3;
        arrayOfByte1[37] = 42;
        arrayOfByte1[38] = 52;
        arrayOfByte1[39] = 29;
        arrayOfByte1[40] = 86;
        arrayOfByte1[41] = 122;
        arrayOfByte1[42] = 6;
        arrayOfByte1[43] = -35;
        arrayOfByte1[44] = -110;
        arrayOfByte1[45] = -1;
        arrayOfByte1[46] = -57;
        arrayOfByte1[47] = 41;
        arrayOfByte1[48] = 52;
        arrayOfByte1[49] = -13;
        arrayOfByte1[50] = -73;
        arrayOfByte1[51] = 10;
        arrayOfByte1[52] = 48;
        arrayOfByte1[53] = 49;
        arrayOfByte1[54] = 92;
        arrayOfByte1[55] = 117;
        arrayOfByte1[56] = 67;
        arrayOfByte1[57] = 72;
        arrayOfByte1[58] = 45;
        arrayOfByte1[59] = 121;
        arrayOfByte1[60] = 93;
        arrayOfByte1[61] = -63;
        arrayOfByte1[62] = 101;
        arrayOfByte1[63] = -90;
        arrayOfByte1[64] = 73;
        arrayOfByte1[65] = 108;
        arrayOfByte1[66] = -29;
        arrayOfByte1[67] = -91;
        arrayOfByte1[68] = 7;
        arrayOfByte1[69] = 46;
        arrayOfByte1[70] = -110;
        arrayOfByte1[71] = 85;
        arrayOfByte1[73] = 81;
        arrayOfByte1[74] = 67;
        arrayOfByte1[75] = 83;
        arrayOfByte1[76] = 113;
        arrayOfByte1[77] = 67;
        arrayOfByte1[78] = 9;
        arrayOfByte1[79] = -57;
        arrayOfByte1[80] = 116;
        arrayOfByte1[81] = -102;
        arrayOfByte1[82] = -26;
        arrayOfByte1[83] = 15;
        arrayOfByte1[84] = 92;
        arrayOfByte1[85] = -14;
        arrayOfByte1[86] = -91;
        arrayOfByte1[87] = 90;
        arrayOfByte1[88] = 56;
        arrayOfByte1[89] = -76;
        arrayOfByte1[90] = 18;
        arrayOfByte1[91] = 1;
        arrayOfByte1[92] = 57;
        arrayOfByte1[93] = 95;
        arrayOfByte1[94] = -1;
        arrayOfByte1[95] = 83;
        arrayOfByte1[96] = 67;
        arrayOfByte1[97] = -84;
        arrayOfByte1[98] = 52;
        arrayOfByte1[99] = 117;
        arrayOfByte1[100] = -93;
        arrayOfByte1[101] = 86;
        arrayOfByte1[102] = 116;
        arrayOfByte1[103] = -58;
        arrayOfByte1[104] = 120;
        arrayOfByte1[105] = -112;
        arrayOfByte1[106] = 70;
        arrayOfByte1[107] = -88;
        arrayOfByte1[108] = -123;
        arrayOfByte1[109] = -45;
        arrayOfByte1[110] = -122;
        arrayOfByte1[111] = 10;
        arrayOfByte1[112] = 38;
        arrayOfByte1[113] = 39;
        arrayOfByte1[114] = -10;
        arrayOfByte1[115] = -60;
        arrayOfByte1[116] = -114;
        arrayOfByte1[117] = 93;
        arrayOfByte1[118] = 31;
        arrayOfByte1[119] = 25;
        arrayOfByte1[120] = 1;
        arrayOfByte1[121] = -120;
        arrayOfByte1[122] = -121;
        arrayOfByte1[123] = -66;
        arrayOfByte1[124] = -40;
        arrayOfByte1[125] = 74;
        arrayOfByte1[126] = -69;
        arrayOfByte1[127] = 83;
        arrayOfByte1[''] = 101;
        arrayOfByte1[''] = -86;
        arrayOfByte1[''] = 107;
        arrayOfByte1[''] = 121;
        arrayOfByte1[''] = -6;
        arrayOfByte1[''] = 109;
        arrayOfByte1[''] = 50;
        arrayOfByte1[''] = 111;
        arrayOfByte1[''] = -33;
        arrayOfByte1[''] = 62;
        arrayOfByte1[''] = 27;
        arrayOfByte1[''] = -63;
        arrayOfByte1[''] = -33;
        arrayOfByte1[''] = 1;
        arrayOfByte1[''] = 52;
        arrayOfByte1[''] = 81;
        arrayOfByte1[''] = 83;
        arrayOfByte1[''] = 109;
        arrayOfByte1[''] = -59;
        arrayOfByte1[''] = 122;
        arrayOfByte1[''] = 11;
        arrayOfByte1[''] = -57;
        arrayOfByte1[''] = -75;
        arrayOfByte1[''] = 34;
        arrayOfByte1[''] = 58;
        arrayOfByte1[''] = 38;
        arrayOfByte1[''] = -75;
        arrayOfByte1[''] = -115;
        arrayOfByte1[''] = 62;
        arrayOfByte1[''] = -46;
        arrayOfByte1[''] = 7;
        arrayOfByte1[''] = -114;
        arrayOfByte1[' '] = -60;
        arrayOfByte1['¡'] = -20;
        arrayOfByte1['¢'] = 55;
        arrayOfByte1['£'] = 4;
        arrayOfByte1['¤'] = -107;
        arrayOfByte1['¥'] = -110;
        arrayOfByte1['¦'] = -62;
        arrayOfByte1['§'] = 103;
        arrayOfByte1['¨'] = -21;
        arrayOfByte1['©'] = 40;
        arrayOfByte1['ª'] = 56;
        arrayOfByte1['«'] = -62;
        arrayOfByte1['¬'] = -110;
        arrayOfByte1['­'] = -91;
        arrayOfByte1['®'] = -64;
        arrayOfByte1['¯'] = 53;
        arrayOfByte1['°'] = -69;
        arrayOfByte1['±'] = 123;
        arrayOfByte1['²'] = -87;
        arrayOfByte1['³'] = 66;
        arrayOfByte1['´'] = -67;
        arrayOfByte1['µ'] = 57;
        arrayOfByte1['¶'] = 91;
        arrayOfByte1['·'] = 74;
        arrayOfByte1['¸'] = 82;
        arrayOfByte1['¹'] = 13;
        arrayOfByte1['º'] = 14;
        arrayOfByte1['»'] = 109;
        arrayOfByte1['¼'] = -77;
        arrayOfByte1['½'] = -108;
        arrayOfByte1['¾'] = -28;
        arrayOfByte1['¿'] = -78;
        arrayOfByte1['À'] = 103;
        arrayOfByte1['Á'] = -85;
        arrayOfByte1['Â'] = -37;
        arrayOfByte1['Ã'] = -47;
        arrayOfByte1['Ä'] = -33;
        arrayOfByte1['Å'] = -33;
        arrayOfByte1['Æ'] = 97;
        arrayOfByte1['Ç'] = -103;
        arrayOfByte1['È'] = 102;
        arrayOfByte1['É'] = -96;
        arrayOfByte1['Ê'] = -78;
        arrayOfByte1['Ë'] = -116;
        arrayOfByte1['Ì'] = 57;
        arrayOfByte1['Í'] = 55;
        arrayOfByte1['Î'] = 91;
        arrayOfByte1['Ï'] = 20;
        arrayOfByte1['Ð'] = 80;
        arrayOfByte1['Ñ'] = -66;
        arrayOfByte1['Ò'] = -82;
        arrayOfByte1['Ó'] = -77;
        arrayOfByte1['Ô'] = -78;
        arrayOfByte1['Õ'] = 39;
        arrayOfByte1['Ö'] = -63;
        arrayOfByte1['×'] = 19;
        arrayOfByte1['Ø'] = 12;
        arrayOfByte1['Ù'] = -2;
        arrayOfByte1['Ú'] = 93;
        arrayOfByte1['Û'] = -32;
        arrayOfByte1['Ü'] = 65;
        arrayOfByte1['Ý'] = -25;
        arrayOfByte1['Þ'] = 89;
        arrayOfByte1['ß'] = 104;
        arrayOfByte1['à'] = -51;
        arrayOfByte1['á'] = -102;
        arrayOfByte1['â'] = 76;
        arrayOfByte1['ã'] = -68;
        arrayOfByte1['ä'] = -86;
        arrayOfByte1['å'] = -90;
        arrayOfByte1['æ'] = 121;
        arrayOfByte1['ç'] = 39;
        arrayOfByte1['è'] = -83;
        arrayOfByte1['é'] = -118;
        arrayOfByte1['ê'] = -102;
        arrayOfByte1['ë'] = 110;
        arrayOfByte1['ì'] = 113;
        arrayOfByte1['í'] = -3;
        arrayOfByte1['î'] = -23;
        arrayOfByte1['ï'] = 52;
        arrayOfByte1['ð'] = -71;
        arrayOfByte1['ñ'] = -16;
        arrayOfByte1['ò'] = -21;
        arrayOfByte1['ó'] = 72;
        arrayOfByte1['ô'] = -99;
        arrayOfByte1['õ'] = -86;
        arrayOfByte1['ö'] = -120;
        arrayOfByte1['÷'] = -16;
        arrayOfByte1['ø'] = 2;
        arrayOfByte1['ù'] = 114;
        arrayOfByte1['ú'] = 72;
        arrayOfByte1['û'] = -50;
        arrayOfByte1['ü'] = 56;
        arrayOfByte1['ý'] = 73;
        arrayOfByte1['þ'] = -56;
        arrayOfByte1['ÿ'] = 117;
        byte[] arrayOfByte2 = new byte[paramArrayOfByte.length];
        for (int i = 0; ; ++i) {
            if (i >= paramArrayOfByte.length)
                return arrayOfByte2;
            arrayOfByte2[i] = (byte) (arrayOfByte1[(i % 256)] ^ paramArrayOfByte[i]);
        }
    }

    /*public List<task> gettask(String paramString)
            throws Exception
    {
        ArrayList localArrayList = null;
        String str1 = "";
        String str2 = "";
        String str3 = "";
        String str4 = "";
        String str5 = "";
        String str6 = "";
        XmlPullParser localXmlPullParser = XmlPullParserFactory.newInstance().newPullParser();
        localXmlPullParser.setInput(new StringReader(paramString));
        int i = localXmlPullParser.getEventType();
        if (i == 1)
            label57: return localArrayList;
        switch (i)
        {
            case 1:
            default:
            case 0:
            case 2:
        }
        while (true)
        {
            i = localXmlPullParser.next();
            break label57:
            localArrayList = new ArrayList();
            continue;
            if ("title".equals(localXmlPullParser.getName()))
                str1 = localXmlPullParser.nextText();
            if (("R".equals(localXmlPullParser.getName())) && (localXmlPullParser.getAttributeName(0).equals("source")))
                str2 = "  " + localXmlPullParser.getAttributeValue(0) + "  ";
            if ("U".equals(localXmlPullParser.getName()))
            {
                if (str3.equals("CUSTOM"))
                {
                    String str8 = localXmlPullParser.nextText();
                    String str9 = download2(str8, str6);
                    if (str9.equals(""))
                        str9 = download(str8, str6);
                    String str10 = substringBetween(str9, str4, str5);
                    if (str10.equals(""))
                        str10 = substringBetween(str9, "'m3u':'", "'");
                    String str11 = str10.replace("\\/", "/");
                    str1 = str1.replace("/", "|");
                    task localtask2 = new task();
                    localtask2.setName(str1);
                    localtask2.setWebsite(str2);
                    localtask2.setref(str6);
                    localtask2.seturl(str11);
                    localArrayList.add(localtask2);
                }
                String str7 = localXmlPullParser.nextText();
                task localtask1 = new task();
                localtask1.setName(str1);
                localtask1.setWebsite(str2);
                localtask1.setref(str6);
                localtask1.seturl(str7);
                localArrayList.add(localtask1);
            }
            if ("TYPE".equals(localXmlPullParser.getName()))
                str3 = localXmlPullParser.nextText();
            if ("ts".equals(localXmlPullParser.getName()))
                str4 = localXmlPullParser.nextText();
            if ("te".equals(localXmlPullParser.getName()))
                str5 = localXmlPullParser.nextText();
            if (!"ref".equals(localXmlPullParser.getName()))
                continue;
            str6 = localXmlPullParser.nextText();
        }
    }*/
}
