/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/
package org.openorb.orb.iiop;

/**
 * This class has been automatically generated on Tue Apr 09 00:07:52 CEST 2002.
 * Use 'java org.openorb.iiop.CodeSetDatabaseInitializer
 *     src/main/org/openorb/iiop/cs_registry1_2h.txt' * to generate it.
 * DO NOT MODIFY MANUALLY !!!
 *
 * @author The CodeSetDatabaseInitializer tool.
 */
public class CodeSetDatabase
{

    /**
     * Convert an encoding name into it's canonical Java name.
     */
    public static String canonicalize( String encoding )
        throws java.io.UnsupportedEncodingException
    {
        // Use this way to get the canonical encoding name.
        // Internally the sun.io.Converters and
        // sun.io.CharacterEncoding are used to convert the
        // name. If we use these classes directly
        // we would limit the number of supported JDKs to the
        // Sun JDKs only. It isn't the most efficient way to
        // create an OutputStreamWriter just for doing a String
        // conversion, but JDKs before 1.4 provide no other
        // to get the canonical name.
        java.io.OutputStreamWriter osw = new java.io.OutputStreamWriter( System.out, encoding );
        return osw.getEncoding();
    }

    /**
     * Populates the map between canonical codeset names
     * and an ArrayList of codeset ids.
     */
    public static void populateNameToIdMap( java.util.HashMap map )
    {
        java.util.ArrayList al = null;

        al = new java.util.ArrayList();
        al.add( new Integer( 0x0003000A ) );
        map.put( "JIS0212", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x00010109 ) );
        al.add( new Integer( 0x00010102 ) );
        al.add( new Integer( 0x00010101 ) );
        al.add( new Integer( 0x00010100 ) );
        map.put( "UNICODEBIGUNMARKED", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100283BA ) );
        map.put( "CP33722", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100201B5 ) );
        map.put( "CP437", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100203EE ) );
        map.put( "CP1006", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x1002039A ) );
        map.put( "CP922", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10020399 ) );
        map.put( "CP921", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x05000010 ) );
        al.add( new Integer( 0x00030010 ) );
        map.put( "EUC_JP", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x00030006 ) );
        al.add( new Integer( 0x00030005 ) );
        al.add( new Integer( 0x00030004 ) );
        map.put( "JIS0208", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10020129 ) );
        map.put( "CP297", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x00030001 ) );
        map.put( "JIS0201", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10027025 ) );
        al.add( new Integer( 0x10020025 ) );
        map.put( "CP037", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100204E9 ) );
        map.put( "CP1257", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100204E8 ) );
        map.put( "CP1256", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100204E7 ) );
        map.put( "CP1255", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100204E6 ) );
        map.put( "CP1254", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100204E5 ) );
        map.put( "CP1253", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100204E4 ) );
        map.put( "CP1252", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100204E3 ) );
        map.put( "CP1251", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10020396 ) );
        map.put( "CP918", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100204E2 ) );
        map.put( "CP1250", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100201A8 ) );
        map.put( "CP424", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100221A4 ) );
        al.add( new Integer( 0x100201A4 ) );
        map.put( "CP420", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10010007 ) );
        map.put( "EUC_CN", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100203CA ) );
        map.put( "CP970", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x1002036B ) );
        map.put( "CP875", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x1002236A ) );
        al.add( new Integer( 0x1002036A ) );
        map.put( "CP874", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10020367 ) );
        map.put( "CP871", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10020366 ) );
        map.put( "CP870", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x1002011D ) );
        map.put( "CP285", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x1002011C ) );
        map.put( "CP284", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10020118 ) );
        map.put( "CP280", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x000B0001 ) );
        map.put( "TIS620", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10010008 ) );
        map.put( "BIG5", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100203C4 ) );
        map.put( "CP964", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10021365 ) );
        al.add( new Integer( 0x10020365 ) );
        map.put( "CP869", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10021364 ) );
        al.add( new Integer( 0x10020364 ) );
        map.put( "CP868", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10020362 ) );
        map.put( "CP866", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x00050010 ) );
        al.add( new Integer( 0x0005000A ) );
        al.add( new Integer( 0x00050002 ) );
        al.add( new Integer( 0x00050001 ) );
        map.put( "EUC_TW", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10022360 ) );
        al.add( new Integer( 0x10021360 ) );
        al.add( new Integer( 0x10020360 ) );
        map.put( "CP864", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x1002035F ) );
        map.put( "CP863", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10020116 ) );
        map.put( "CP278", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10020115 ) );
        map.put( "CP277", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x1002035E ) );
        map.put( "CP862", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x1002035D ) );
        map.put( "CP861", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10020111 ) );
        map.put( "CP273", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x05000012 ) );
        al.add( new Integer( 0x05000011 ) );
        map.put( "SJIS", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x00010020 ) );
        map.put( "ASCII", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100201F4 ) );
        map.put( "CP500", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x1002044A ) );
        map.put( "CP1098", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10020449 ) );
        map.put( "CP1097", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10021359 ) );
        al.add( new Integer( 0x10020359 ) );
        map.put( "CP857", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10028358 ) );
        al.add( new Integer( 0x10026358 ) );
        al.add( new Integer( 0x10021358 ) );
        al.add( new Integer( 0x10020358 ) );
        map.put( "CP856", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100203B6 ) );
        map.put( "CP950", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10021357 ) );
        al.add( new Integer( 0x10020357 ) );
        map.put( "CP855", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10021354 ) );
        al.add( new Integer( 0x10020354 ) );
        map.put( "CP852", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10026352 ) );
        al.add( new Integer( 0x10021352 ) );
        al.add( new Integer( 0x10020352 ) );
        map.put( "CP850", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10020567 ) );
        map.put( "CP1383", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10020565 ) );
        map.put( "CP1381", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100203B5 ) );
        map.put( "CP949", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10020462 ) );
        map.put( "CP1122", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100203B4 ) );
        map.put( "CP948", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10020402 ) );
        map.put( "CP1026", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10020401 ) );
        map.put( "CP1025", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100203AF ) );
        map.put( "CP943", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100203AE ) );
        al.add( new Integer( 0x100203A4 ) );
        map.put( "CP942", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x00010009 ) );
        map.put( "ISO8859_9", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x00010008 ) );
        map.put( "ISO8859_8", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x00010007 ) );
        map.put( "ISO8859_7", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x05010001 ) );
        map.put( "UTF8", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x0001000F ) );
        map.put( "ISO8859_15_FDIS", al );   // JDK 1.3.1 canonical name
        // MANUALLY ADDED FOR JDK1.4.0/1.3.1 INTEROPERABILITY
        map.put( "ISO-8859-15", al );       // JDK 1.4.0 canonical name

        al = new java.util.ArrayList();
        al.add( new Integer( 0x00010006 ) );
        map.put( "ISO8859_6", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100203AB ) );
        map.put( "CP939", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10020458 ) );
        map.put( "CP1112", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x00010005 ) );
        map.put( "ISO8859_5", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x00010004 ) );
        map.put( "ISO8859_4", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100203A9 ) );
        map.put( "CP937", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x00010003 ) );
        map.put( "ISO8859_3", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x00010002 ) );
        map.put( "ISO8859_2", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100203A7 ) );
        map.put( "CP935", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x00010001 ) );
        map.put( "ISO8859_1", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x10022346 ) );
        al.add( new Integer( 0x10020346 ) );
        map.put( "CP838", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x100203A2 ) );
        map.put( "CP930", al );

        al = new java.util.ArrayList();
        al.add( new Integer( 0x0004000A ) );
        al.add( new Integer( 0x00040001 ) );
        map.put( "EUC_KR", al );
    }

    /**
     * Return a CodeSet object for an OSF registry codeset id.
     *
     * @param id An OSF charset and codeset registry id.
     * @return A CodeSet object.
     */
    public static CodeSet getCodeSetFromId( int id )
    {
        switch( id )
        {
            case 0x1002035F:
              return new CodeSet( "IBM-863 (CCSID 00863); PC Data Canadian French", 0x1002035F, new short [] { 0x0011 } ,1, "Cp863", "cp863", 1, false );
            case 0x1002035E:
              return new CodeSet( "IBM-862 (CCSID 00862); PC Data Hebrew", 0x1002035E, new short [] { 0x0018 } ,1, "Cp862", "cp862", 1, false );
            case 0x1002035D:
              return new CodeSet( "IBM-861 (CCSID 00861); PC Data Iceland", 0x1002035D, new short [] { 0x0011 } ,1, "Cp861", "cp861", 1, false );
            case 0x1002011D:
              return new CodeSet( "IBM-285 (CCSID 00285); CECP for United Kingdom", 0x1002011D, new short [] { 0x0011 } ,1, "Cp285", "cp285", 1, false );
            case 0x1002011C:
              return new CodeSet( "IBM-284 (CCSID 00284); CECP for Spain, Latin America (Spanish)", 0x1002011C, new short [] { 0x0011 } ,1, "Cp284", "cp284", 1, false );
            case 0x10020359:
              return new CodeSet( "IBM-857 (CCSID 00857); Turkish Latin-5 PC Data", 0x10020359, new short [] { 0x0019 } ,1, "Cp857", "cp857", 1, false );
            case 0x10020358:
              return new CodeSet( "IBM-856 (CCSID 00856); Hebrew PC Data (extensions)", 0x10020358, new short [] { 0x0018 } ,1, "Cp856", "cp856", 1, false );
            case 0x10020357:
              return new CodeSet( "IBM-855 (CCSID 00855); Cyrillic PC Data", 0x10020357, new short [] { 0x0015 } ,1, "Cp855", "cp855", 1, false );
            case 0x1002236A:
              return new CodeSet( "IBM-874 (CCSID 09066); Thai PC Display Extended SBCS", 0x1002236A, new short [] { 0x0200 } ,1, "Cp874", "cp874", 1, false );
            case 0x10020118:
              return new CodeSet( "IBM-280 (CCSID 00280); CECP for Italy", 0x10020118, new short [] { 0x0011 } ,1, "Cp280", "cp280", 1, false );
            case 0x10020354:
              return new CodeSet( "IBM-852 (CCSID 00852); Multilingual Latin-2", 0x10020354, new short [] { 0x0012 } ,1, "Cp852", "cp852", 1, false );
            case 0x10020116:
              return new CodeSet( "IBM-278 (CCSID 00278); CECP for Finland, Sweden", 0x10020116, new short [] { 0x0011 } ,1, "Cp278", "cp278", 1, false );
            case 0x10020115:
              return new CodeSet( "IBM-277 (CCSID 00277); CECP for Denmark, Norway", 0x10020115, new short [] { 0x0011 } ,1, "Cp277", "Cp277", 1, false );
            case 0x10020352:
              return new CodeSet( "IBM-850 (CCSID 00850); Multilingual IBM PC Data-MLP 222", 0x10020352, new short [] { 0x0011 } ,1, "Cp850", "cp850", 1, false );
            case 0x10020111:
              return new CodeSet( "IBM-273 (CCSID 00273); CECP for Austria, Germany", 0x10020111, new short [] { 0x0011 } ,1, "Cp273", "cp273", 1, false );
            case 0x10022360:
              return new CodeSet( "IBM-864 (CCSID 09056); Arabic PC Data (unshaped)", 0x10022360, new short [] { 0x0016 } ,1, "Cp864", "cp864", 1, false );
            case 0x10020346:
              return new CodeSet( "IBM-838 (CCSID 00838); Thai Host Extended SBCS", 0x10020346, new short [] { 0x0200 } ,1, "Cp838", "cp838", 1, false );
            case 0x10020402:
              return new CodeSet( "IBM-1026 (CCSID 01026); Turkish Latin-5", 0x10020402, new short [] { 0x0019 } ,1, "Cp1026", "cp1026", 1, false );
            case 0x10020401:
              return new CodeSet( "IBM-1025 (CCSID 01025); Cyrillic Multilingual", 0x10020401, new short [] { 0x0015 } ,1, "Cp1025", "cp1025", 1, false );
            case 0x10027025:
              return new CodeSet( "IBM-037 (CCSID 28709); T-Ch Host Extended SBCS", 0x10027025, new short [] { 0x0001 } ,1, "Cp037", "cp037", 1, false );
            case 0x100201B5:
              return new CodeSet( "IBM-437 (CCSID 00437); PC USA", 0x100201B5, new short [] { 0x0011 } ,1, "Cp437", "cp437", 1, false );
            case 0x10022346:
              return new CodeSet( "IBM-838 (CCSID 09030); Thai Host Extended SBCS", 0x10022346, new short [] { 0x0200 } ,1, "Cp838", "cp838", 1, false );
            case 0x100203EE:
              return new CodeSet( "IBM-1006 (CCSID 01006); Urdu 8-bit", 0x100203EE, new short [] { 0x0016 } ,1, "Cp1006", "cp1006", 1, false );
            case 0x10020567:
              return new CodeSet( "IBM-1383 (CCSID 01383); S-Ch EUC GB 2312-80 set (1382)", 0x10020567, new short [] { 0x0001, 0x0300 } ,3, "Cp1383", "cp1383", 1, false );
            case 0x10020565:
              return new CodeSet( "IBM-1381 (CCSID 01381); S-Ch PC Data Mixed incl 1880 UDC", 0x10020565, new short [] { 0x0001, 0x0300 } ,2, "Cp1381", "cp1381", 1, false );
            case 0x100201A8:
              return new CodeSet( "IBM-424 (CCSID 00424); Hebrew", 0x100201A8, new short [] { 0x0018 } ,1, "Cp424", "cp424", 1, false );
            case 0x00050010:
              return new CodeSet( "CNS eucTW:1993; Taiwanese EUC", 0x00050010, new short [] { 0x0001, 0x0181 } ,4, "EUC_TW", "EUC_TW", 1, true );
            case 0x100201A4:
              return new CodeSet( "IBM-420 (CCSID 00420); Arabic (presentation shapes)", 0x100201A4, new short [] { 0x0016 } ,1, "Cp420", "cp420", 1, false );
            case 0x10020025:
              return new CodeSet( "IBM-037 (CCSID 00037); CECP for USA, Canada, NL, Ptgl, Brazil, Australia, NZ", 0x10020025, new short [] { 0x0011 } ,1, "Cp037", "cp037", 1, false );
            case 0x0005000A:
              return new CodeSet( "CNS eucTW:1991; Taiwanese EUC", 0x0005000A, new short [] { 0x0001, 0x0180 } ,4, "EUC_TW", "EUC_TW", 1, true );
            case 0x10026358:
              return new CodeSet( "IBM-856 (CCSID 25432); Hebrew PC Display (extensions)", 0x10026358, new short [] { 0x0018 } ,1, "Cp856", "cp856", 1, false );
            case 0x00050002:
              return new CodeSet( "CNS 11643:1992; Taiwanese Extended Hanzi Graphic Chars", 0x00050002, new short [] { 0x0181 } ,4, "EUC_TW", "CNS11643", 1, true );
            case 0x00050001:
              return new CodeSet( "CNS 11643:1986; Taiwanese Hanzi Graphic Characters", 0x00050001, new short [] { 0x0180 } ,2, "EUC_TW", "CNS11643", 1, true );
            case 0x10026352:
              return new CodeSet( "IBM-850 (CCSID 25426); Multilingual IBM PC Display-MLP", 0x10026352, new short [] { 0x0011 } ,1, "Cp850", "cp850", 1, false );
            case 0x100221A4:
              return new CodeSet( "IBM-420 (CCSID 08612); Arabic (base shapes only)", 0x100221A4, new short [] { 0x0016 } ,1, "Cp420", "cp420", 1, false );
            case 0x100203CA:
              return new CodeSet( "IBM-970 (CCSID 00970); Korean EUC", 0x100203CA, new short [] { 0x0011, 0x0100, 0x0101 } ,2, "Cp970", "cp970", 1, false );
            case 0x0004000A:
              return new CodeSet( "KS eucKR:1991; Korean EUC", 0x0004000A, new short [] { 0x0011, 0x0100, 0x0101 } ,2, "EUC_KR", "EUC_KR", 1, true );
            case 0x100203C4:
              return new CodeSet( "IBM-964 (CCSID 00964); T-Chinese EUC CNS1163 plane 1,2", 0x100203C4, new short [] { 0x0001, 0x0180 } ,4, "Cp964", "cp964", 1, false );
            case 0x10028358:
              return new CodeSet( "IBM-856 (CCSID 33624); Hebrew PC Display", 0x10028358, new short [] { 0x0018 } ,1, "Cp856", "cp856", 1, false );
            case 0x00010109:
              return new CodeSet( "ISO/IEC 10646-1:1993; UTF-16, UCS Transformation Format 16-bit form", 0x00010109, new short [] { 0x1000 } ,2, "UnicodeBigUnmarked", "UTF-16BE", 2, false );
            case 0x00040001:
              return new CodeSet( "KS C5601:1987; Korean Hangul and Hanja Graphic Characters", 0x00040001, new short [] { 0x0100 } ,2, "EUC_KR", "KSC5601", 1, true );
            case 0x10021365:
              return new CodeSet( "IBM-869 (CCSID 04965); Greek PC Data", 0x10021365, new short [] { 0x0017 } ,1, "Cp869", "cp869", 1, false );
            case 0x00010102:
              return new CodeSet( "ISO/IEC 10646-1:1993; UCS-2, Level 3", 0x00010102, new short [] { 0x1000 } ,2, "UnicodeBigUnmarked", "ISO-10646-UCS-2", 2, false );
            case 0x10021364:
              return new CodeSet( "IBM-868 (CCSID 04964); PC Data for Urdu", 0x10021364, new short [] { 0x0016 } ,1, "Cp868", "cp868", 1, false );
            case 0x00010101:
              return new CodeSet( "ISO/IEC 10646-1:1993; UCS-2, Level 2", 0x00010101, new short [] { 0x1000 } ,2, "UnicodeBigUnmarked", "ISO-10646-UCS-2", 2, false );
            case 0x00010100:
              return new CodeSet( "ISO/IEC 10646-1:1993; UCS-2, Level 1", 0x00010100, new short [] { 0x1000 } ,2, "UnicodeBigUnmarked", "ISO-10646-UCS-2", 2, true );
            case 0x100203B6:
              return new CodeSet( "IBM-950 (CCSID 00950); T-Ch PC Data Mixed", 0x100203B6, new short [] { 0x0001, 0x0180 } ,2, "Cp950", "cp950", 1, false );
            case 0x10021360:
              return new CodeSet( "IBM-864 (CCSID 04960); Arabic PC Data (all shapes)", 0x10021360, new short [] { 0x0016 } ,1, "Cp864", "cp864", 1, false );
            case 0x100203B5:
              return new CodeSet( "IBM-949 (CCSID 00949); IBM KS PC Data Mixed", 0x100203B5, new short [] { 0x0001, 0x0100 } ,2, "Cp949", "cp949", 1, false );
            case 0x00030010:
              return new CodeSet( "JIS eucJP:1993; Japanese EUC", 0x00030010, new short [] { 0x0011, 0x0080, 0x0081, 0x0082 } ,3, "EUC_JP", "EUC-JP", 1, true );
            case 0x100203B4:
              return new CodeSet( "IBM-948 (CCSID 00948); T-Ch PC Data Mixed", 0x100203B4, new short [] { 0x0001, 0x0180 } ,2, "Cp948", "cp948", 1, false );
            case 0x100203AF:
              return new CodeSet( "IBM-943 (CCSID 00943); Japanese PC MBCS for Open", 0x100203AF, new short [] { 0x0080, 0x0081 } ,2, "Cp943", "cp943", 1, false );
            case 0x0003000A:
              return new CodeSet( "JIS X0212:1990; Supplementary Japanese Kanji Graphic Chars", 0x0003000A, new short [] { 0x0082 } ,2, "JIS0212", "JIS0212", 1, true );
            case 0x10021359:
              return new CodeSet( "IBM-857 (CCSID 04953); Turkish Latin-5 PC Data", 0x10021359, new short [] { 0x0019 } ,1, "Cp857", "cp857", 1, false );
            case 0x100203AE:
              return new CodeSet( "IBM-942 (CCSID 00942); Japanese PC Data Mixed", 0x100203AE, new short [] { 0x0080, 0x0081 } ,2, "Cp942", "cp942", 1, false );
            case 0x10021358:
              return new CodeSet( "IBM-856 (CCSID 04952); Hebrew PC Data", 0x10021358, new short [] { 0x0018 } ,1, "Cp856", "cp856", 1, false );
            case 0x10021357:
              return new CodeSet( "IBM-855 (CCSID 04951); Cyrillic Personal Computer", 0x10021357, new short [] { 0x0015 } ,1, "Cp855", "cp855", 1, false );
            case 0x10010008:
              return new CodeSet( "HP big5; encoding method for Traditional Chinese", 0x10010008, new short [] { 0x0001, 0x0180 } ,2, "Big5", "big5", 1, false );
            case 0x10010007:
              return new CodeSet( "HP15CN; encoding method for Simplified Chinese", 0x10010007, new short [] { 0x0001, 0x0300 } ,2, "EUC_CN", "GB2312", 1, false );
            case 0x00030006:
              return new CodeSet( "JIS X0208:1990 Japanese Kanji Graphic Characters", 0x00030006, new short [] { 0x0081 } ,2, "JIS0208", "JIS0208", 1, true );
            case 0x100203AB:
              return new CodeSet( "IBM-939 (CCSID 00939); Latin-Kanji Host MBCS", 0x100203AB, new short [] { 0x0080, 0x0081 } ,2, "Cp939", "cp939", 1, false );
            case 0x00030005:
              return new CodeSet( "JIS X0208:1983 Japanese Kanji Graphic Characters", 0x00030005, new short [] { 0x0081 } ,2, "JIS0208", "JIS0208", 1, false );
            case 0x10021354:
              return new CodeSet( "IBM-852 (CCSID 04948); Latin-2 Personal Computer", 0x10021354, new short [] { 0x0012 } ,1, "Cp852", "cp852", 1, false );
            case 0x00030004:
              return new CodeSet( "JIS X0208:1978 Japanese Kanji Graphic Characters", 0x00030004, new short [] { 0x0081 } ,2, "JIS0208", "JIS0208", 1, false );
            case 0x100203A9:
              return new CodeSet( "IBM-937 (CCSID 00937); T-Ch Host Mixed", 0x100203A9, new short [] { 0x0001, 0x0180 } ,2, "Cp937", "cp937", 1, false );
            case 0x10021352:
              return new CodeSet( "IBM-850 (CCSID 04946); Multilingual IBM PC Data-190", 0x10021352, new short [] { 0x0011 } ,1, "Cp850", "cp850", 1, false );
            case 0x100203A7:
              return new CodeSet( "IBM-935 (CCSID 00935); S-Ch Host Mixed", 0x100203A7, new short [] { 0x0001, 0x0300 } ,2, "Cp935", "cp935", 1, false );
            case 0x00030001:
              return new CodeSet( "JIS X0201:1976; Japanese phonetic characters", 0x00030001, new short [] { 0x0080 } ,1, "JIS0201", "JIS0201", 1, true );
            case 0x100203A4:
              return new CodeSet( "IBM-932 (CCSID 00932); Japanese PC Data Mixed", 0x100203A4, new short [] { 0x0080, 0x0081 } ,2, "Cp942", "cp942", 1, false );
            case 0x10020462:
              return new CodeSet( "IBM-1122 (CCSID 01122); Estonia", 0x10020462, new short [] { 0x001A } ,1, "Cp1122", "cp1122", 1, false );
            case 0x100203A2:
              return new CodeSet( "IBM-930 (CCSID 00930); Kat-Kanji Host MBCS Ext-SBCS", 0x100203A2, new short [] { 0x0080, 0x0081 } ,2, "Cp930", "cp930", 1, false );
            case 0x000B0001:
              return new CodeSet( "TIS 620-2529, Thai characters", 0x000B0001, new short [] { 0x0200 } ,1, "TIS620", "TIS620", 1, true );
            case 0x1002039A:
              return new CodeSet( "IBM-922 (CCSID 00922); Estonia 8-Bit", 0x1002039A, new short [] { 0x001A } ,1, "Cp922", "cp922", 1, false );
            case 0x10020458:
              return new CodeSet( "IBM-1112 (CCSID 01112); Baltic Multilingual", 0x10020458, new short [] { 0x001A } ,1, "Cp1112", "cp1112", 1, false );
            case 0x10020399:
              return new CodeSet( "IBM-921 (CCSID 00921); Baltic 8-Bit", 0x10020399, new short [] { 0x001A } ,1, "Cp921", "cp921", 1, false );
            case 0x00010020:
              return new CodeSet( "ISO 646:1991 IRV (International Reference Version)", 0x00010020, new short [] { 0x0001 } ,1, "ASCII", "US-ASCII", 1, true );
            case 0x10020396:
              return new CodeSet( "IBM-918 (CCSID 00918); Urdu", 0x10020396, new short [] { 0x0016 } ,1, "Cp918", "cp918", 1, false );
            case 0x05010001:
              return new CodeSet( "X/Open UTF-8; UCS Transformation Format 8 (UTF-8)", 0x05010001, new short [] { 0x1000 } ,6, "UTF8", "UTF-8", 1, false );
            case 0x05000012:
              return new CodeSet( "OSF Japanese SJIS-2", 0x05000012, new short [] { 0x0001, 0x0080, 0x0081 } ,2, "SJIS", "SJIS", 1, false );
            case 0x1002044A:
              return new CodeSet( "IBM-1098 (CCSID 01098); Farsi PC Data", 0x1002044A, new short [] { 0x0016 } ,1, "Cp1098", "cp1098", 1, false );
            case 0x05000011:
              return new CodeSet( "OSF Japanese SJIS-1", 0x05000011, new short [] { 0x0001, 0x0080, 0x0081 } ,2, "SJIS", "SJIS", 1, false );
            case 0x10020449:
              return new CodeSet( "IBM-1097 (CCSID 01097); Farsi", 0x10020449, new short [] { 0x0016 } ,1, "Cp1097", "cp1097", 1, false );
            case 0x05000010:
              return new CodeSet( "OSF Japanese UJIS", 0x05000010, new short [] { 0x0001, 0x0080, 0x0081 } ,2, "EUC_JP", "EUC_JP", 1, false );
            case 0x0001000F:
              return new CodeSet( "ISO/IEC 8859-15:1999; Latin Alphabet No. 9", 0x0001000F, new short [] { 0x0011 } ,1, "ISO8859_15_FDIS", "ISO-8859-15", 1, true );
            case 0x00010009:
              return new CodeSet( "ISO/IEC 8859-9:1989; Latin Alphabet No. 5", 0x00010009, new short [] { 0x0019 } ,1, "ISO8859_9", "ISO-8859-9", 1, true );
            case 0x00010008:
              return new CodeSet( "ISO 8859-8:1988; Latin-Hebrew Alphabet", 0x00010008, new short [] { 0x0018 } ,1, "ISO8859_8", "ISO-8859-8", 1, true );
            case 0x00010007:
              return new CodeSet( "ISO 8859-7:1987; Latin-Greek Alphabet", 0x00010007, new short [] { 0x0017 } ,1, "ISO8859_7", "ISO-8859-7", 1, true );
            case 0x00010006:
              return new CodeSet( "ISO 8859-6:1987; Latin-Arabic Alphabet", 0x00010006, new short [] { 0x0016 } ,1, "ISO8859_6", "ISO-8859-6", 1, true );
            case 0x00010005:
              return new CodeSet( "ISO/IEC 8859-5:1988; Latin-Cyrillic Alphabet", 0x00010005, new short [] { 0x0015 } ,1, "ISO8859_5", "ISO-8859-5", 1, true );
            case 0x00010004:
              return new CodeSet( "ISO 8859-4:1988; Latin Alphabet No. 4", 0x00010004, new short [] { 0x0014 } ,1, "ISO8859_4", "ISO-8859-4", 1, true );
            case 0x00010003:
              return new CodeSet( "ISO 8859-3:1988; Latin Alphabet No. 3", 0x00010003, new short [] { 0x0013 } ,1, "ISO8859_3", "ISO-8859-3", 1, true );
            case 0x00010002:
              return new CodeSet( "ISO 8859-2:1987; Latin Alphabet No. 2", 0x00010002, new short [] { 0x0012 } ,1, "ISO8859_2", "ISO-8859-2", 1, true );
            case 0x00010001:
              return new CodeSet( "ISO 8859-1:1987; Latin Alphabet No. 1", 0x00010001, new short [] { 0x0011 } ,1, "ISO8859_1", "ISO-8859-1", 1, true );
            case 0x100201F4:
              return new CodeSet( "IBM-500 (CCSID 00500); CECP for Belgium, Switzerland", 0x100201F4, new short [] { 0x0011 } ,1, "Cp500", "cp500", 1, false );
            case 0x100204E9:
              return new CodeSet( "IBM-1257 (CCSID 01257); MS Windows Baltic", 0x100204E9, new short [] { 0x001A } ,1, "Cp1257", "Cp1257", 1, false );
            case 0x1002036B:
              return new CodeSet( "IBM-875 (CCSID 00875); Greek", 0x1002036B, new short [] { 0x0017 } ,1, "Cp875", "cp875", 1, false );
            case 0x100204E8:
              return new CodeSet( "IBM-1256 (CCSID 01256); MS Windows Arabic", 0x100204E8, new short [] { 0x0016 } ,1, "Cp1256", "Cp1256", 1, false );
            case 0x1002036A:
              return new CodeSet( "IBM-874 (CCSID 00874); Thai PC Display Extended SBCS", 0x1002036A, new short [] { 0x0200 } ,1, "Cp874", "cp874", 1, false );
            case 0x100204E7:
              return new CodeSet( "IBM-1255 (CCSID 01255); MS Windows Hebrew", 0x100204E7, new short [] { 0x0018 } ,1, "Cp1255", "Cp1255", 1, false );
            case 0x100204E6:
              return new CodeSet( "IBM-1254 (CCSID 01254); MS Windows Turkey", 0x100204E6, new short [] { 0x0019 } ,1, "Cp1254", "Cp1254", 1, false );
            case 0x100204E5:
              return new CodeSet( "IBM-1253 (CCSID 01253); MS Windows Greek", 0x100204E5, new short [] { 0x0017 } ,1, "Cp1253", "Cp1253", 1, false );
            case 0x10020367:
              return new CodeSet( "IBM-871 (CCSID 00871); CECP for Iceland", 0x10020367, new short [] { 0x0011 } ,1, "Cp871", "cp871", 1, false );
            case 0x100283BA:
              return new CodeSet( "IBM33722 (CCSID 33722); Japanese EUC JISx201,208,212", 0x100283BA, new short [] { 0x0080, 0x0081, 0x0082 } ,3, "Cp33722", "cp33722", 1, false );
            case 0x100204E4:
              return new CodeSet( "IBM-1252 (CCSID 01252); MS Windows Latin-1", 0x100204E4, new short [] { 0x0011 } ,1, "Cp1252", "Cp1252", 1, false );
            case 0x10020129:
              return new CodeSet( "IBM-297 (CCSID 00297); CECP for France", 0x10020129, new short [] { 0x0011 } ,1, "Cp297", "cp297", 1, false );
            case 0x10020366:
              return new CodeSet( "IBM-870 (CCSID 00870); Multilingual Latin-2 EBCDIC", 0x10020366, new short [] { 0x0012 } ,1, "Cp870", "cp870", 1, false );
            case 0x100204E3:
              return new CodeSet( "IBM-1251 (CCSID 01251); MS Windows Cyrillic", 0x100204E3, new short [] { 0x0015 } ,1, "Cp1251", "Cp1251", 1, false );
            case 0x10020365:
              return new CodeSet( "IBM-869 (CCSID 00869); Greek PC Data", 0x10020365, new short [] { 0x0017 } ,1, "Cp869", "cp869", 1, false );
            case 0x100204E2:
              return new CodeSet( "IBM-1250 (CCSID 01250); MS Windows Latin-2", 0x100204E2, new short [] { 0x0012 } ,1, "Cp1250", "Cp1250", 1, false );
            case 0x10020364:
              return new CodeSet( "IBM-868 (CCSID 00868); Urdu PC Data", 0x10020364, new short [] { 0x0016 } ,1, "Cp868", "cp868", 1, false );
            case 0x10020362:
              return new CodeSet( "IBM-866 (CCSID 00866); PC Data Cyrillic 2", 0x10020362, new short [] { 0x0015 } ,1, "Cp866", "cp866", 1, false );
            case 0x10020360:
              return new CodeSet( "IBM-864 (CCSID 00864); Arabic PC Data", 0x10020360, new short [] { 0x0016 } ,1, "Cp864", "cp864", 1, false );
            default:
                return null;
        }
    }

    /**
     * Return the alignment for an OSF registry codeset id.
     * This method replaces the method CodeSet.getAlignmentFromId()
     * and moves the functionality to the time when the class
     * CodeSetDatabase is created from the OSF charset and
     * codeset registry file.
     *
     * @param id An OSF charset and codeset registry id.
     * @return The aligment for the specified codeset
     * <ul>
     * <li><code>-1</code>When the codeset id doesn't exist or the
     * maximum size is 1, i.e.
     * the codeset is a byte-oriented single-byte codeset</li>
     * <li><code>0</code> When it is a byte-oriented multi-byte
     * codeset</li>
     * <li><code>&gt;= 1</code> When it is a fixed-length
     * non-byte-oriented codeset (e.g. 2 for UCS codesets)</li>
     * </ul>
     */
    public static int getAlignmentFromId( int id )
    {
        switch( id )
        {
            case 0x1002035F:
                return 1;
            case 0x1002035E:
                return 1;
            case 0x1002035D:
                return 1;
            case 0x1002011D:
                return 1;
            case 0x1002011C:
                return 1;
            case 0x10020359:
                return 1;
            case 0x10020358:
                return 1;
            case 0x10020357:
                return 1;
            case 0x1002236A:
                return 1;
            case 0x10020118:
                return 1;
            case 0x10020354:
                return 1;
            case 0x10020116:
                return 1;
            case 0x10020115:
                return 1;
            case 0x10020352:
                return 1;
            case 0x10020111:
                return 1;
            case 0x10022360:
                return 1;
            case 0x10020346:
                return 1;
            case 0x10020402:
                return 1;
            case 0x10020401:
                return 1;
            case 0x10027025:
                return 1;
            case 0x100201B5:
                return 1;
            case 0x10022346:
                return 1;
            case 0x100203EE:
                return 1;
            case 0x10020567:
                return 1;
            case 0x10020565:
                return 1;
            case 0x100201A8:
                return 1;
            case 0x00050010:
                return 1;
            case 0x100201A4:
                return 1;
            case 0x10020025:
                return 1;
            case 0x0005000A:
                return 1;
            case 0x10026358:
                return 1;
            case 0x00050002:
                return 1;
            case 0x00050001:
                return 1;
            case 0x10026352:
                return 1;
            case 0x100221A4:
                return 1;
            case 0x100203CA:
                return 1;
            case 0x0004000A:
                return 1;
            case 0x100203C4:
                return 1;
            case 0x10028358:
                return 1;
            case 0x00010109:
                return 2;
            case 0x00040001:
                return 1;
            case 0x10021365:
                return 1;
            case 0x00010102:
                return 2;
            case 0x10021364:
                return 1;
            case 0x00010101:
                return 2;
            case 0x00010100:
                return 2;
            case 0x100203B6:
                return 1;
            case 0x10021360:
                return 1;
            case 0x100203B5:
                return 1;
            case 0x00030010:
                return 1;
            case 0x100203B4:
                return 1;
            case 0x100203AF:
                return 1;
            case 0x0003000A:
                return 1;
            case 0x10021359:
                return 1;
            case 0x100203AE:
                return 1;
            case 0x10021358:
                return 1;
            case 0x10021357:
                return 1;
            case 0x10010008:
                return 1;
            case 0x10010007:
                return 1;
            case 0x00030006:
                return 1;
            case 0x100203AB:
                return 1;
            case 0x00030005:
                return 1;
            case 0x10021354:
                return 1;
            case 0x00030004:
                return 1;
            case 0x100203A9:
                return 1;
            case 0x10021352:
                return 1;
            case 0x100203A7:
                return 1;
            case 0x00030001:
                return 1;
            case 0x100203A4:
                return 1;
            case 0x10020462:
                return 1;
            case 0x100203A2:
                return 1;
            case 0x000B0001:
                return 1;
            case 0x1002039A:
                return 1;
            case 0x10020458:
                return 1;
            case 0x10020399:
                return 1;
            case 0x00010020:
                return 1;
            case 0x10020396:
                return 1;
            case 0x05010001:
                return 1;
            case 0x05000012:
                return 1;
            case 0x1002044A:
                return 1;
            case 0x05000011:
                return 1;
            case 0x10020449:
                return 1;
            case 0x05000010:
                return 1;
            case 0x0001000F:
                return 1;
            case 0x00010009:
                return 1;
            case 0x00010008:
                return 1;
            case 0x00010007:
                return 1;
            case 0x00010006:
                return 1;
            case 0x00010005:
                return 1;
            case 0x00010004:
                return 1;
            case 0x00010003:
                return 1;
            case 0x00010002:
                return 1;
            case 0x00010001:
                return 1;
            case 0x100201F4:
                return 1;
            case 0x100204E9:
                return 1;
            case 0x1002036B:
                return 1;
            case 0x100204E8:
                return 1;
            case 0x1002036A:
                return 1;
            case 0x100204E7:
                return 1;
            case 0x100204E6:
                return 1;
            case 0x100204E5:
                return 1;
            case 0x10020367:
                return 1;
            case 0x100283BA:
                return 1;
            case 0x100204E4:
                return 1;
            case 0x10020129:
                return 1;
            case 0x10020366:
                return 1;
            case 0x100204E3:
                return 1;
            case 0x10020365:
                return 1;
            case 0x100204E2:
                return 1;
            case 0x10020364:
                return 1;
            case 0x10020362:
                return 1;
            case 0x10020360:
                return 1;
            default:
                return -1;
        }
    }

    /**
     * Returns the name of a OSF charset and codeset registry entry.
     *
     * @param id An OSF charset and codeset registry id.
     * @return The name of the codeset.
     */
    public static String getNameFromId( int id )
    {
        switch( id )
        {
            case 0x1002035F:
                return "cp863";
            case 0x1002035E:
                return "cp862";
            case 0x1002035D:
                return "cp861";
            case 0x1002011D:
                return "cp285";
            case 0x1002011C:
                return "cp284";
            case 0x10020359:
                return "cp857";
            case 0x10020358:
                return "cp856";
            case 0x10020357:
                return "cp855";
            case 0x1002236A:
                return "cp874";
            case 0x10020118:
                return "cp280";
            case 0x10020354:
                return "cp852";
            case 0x10020116:
                return "cp278";
            case 0x10020115:
                return "Cp277";
            case 0x10020352:
                return "cp850";
            case 0x10020111:
                return "cp273";
            case 0x10022360:
                return "cp864";
            case 0x10020346:
                return "cp838";
            case 0x10020402:
                return "cp1026";
            case 0x10020401:
                return "cp1025";
            case 0x10027025:
                return "cp037";
            case 0x100201B5:
                return "cp437";
            case 0x10022346:
                return "cp838";
            case 0x100203EE:
                return "cp1006";
            case 0x10020567:
                return "cp1383";
            case 0x10020565:
                return "cp1381";
            case 0x100201A8:
                return "cp424";
            case 0x00050010:
                return "EUC_TW";
            case 0x100201A4:
                return "cp420";
            case 0x10020025:
                return "cp037";
            case 0x0005000A:
                return "EUC_TW";
            case 0x10026358:
                return "cp856";
            case 0x00050002:
                return "CNS11643";
            case 0x00050001:
                return "CNS11643";
            case 0x10026352:
                return "cp850";
            case 0x100221A4:
                return "cp420";
            case 0x100203CA:
                return "cp970";
            case 0x0004000A:
                return "EUC_KR";
            case 0x100203C4:
                return "cp964";
            case 0x10028358:
                return "cp856";
            case 0x00010109:
                return "UTF-16BE";
            case 0x00040001:
                return "KSC5601";
            case 0x10021365:
                return "cp869";
            case 0x00010102:
                return "ISO-10646-UCS-2";
            case 0x10021364:
                return "cp868";
            case 0x00010101:
                return "ISO-10646-UCS-2";
            case 0x00010100:
                return "ISO-10646-UCS-2";
            case 0x100203B6:
                return "cp950";
            case 0x10021360:
                return "cp864";
            case 0x100203B5:
                return "cp949";
            case 0x00030010:
                return "EUC-JP";
            case 0x100203B4:
                return "cp948";
            case 0x100203AF:
                return "cp943";
            case 0x0003000A:
                return "JIS0212";
            case 0x10021359:
                return "cp857";
            case 0x100203AE:
                return "cp942";
            case 0x10021358:
                return "cp856";
            case 0x10021357:
                return "cp855";
            case 0x10010008:
                return "big5";
            case 0x10010007:
                return "GB2312";
            case 0x00030006:
                return "JIS0208";
            case 0x100203AB:
                return "cp939";
            case 0x00030005:
                return "JIS0208";
            case 0x10021354:
                return "cp852";
            case 0x00030004:
                return "JIS0208";
            case 0x100203A9:
                return "cp937";
            case 0x10021352:
                return "cp850";
            case 0x100203A7:
                return "cp935";
            case 0x00030001:
                return "JIS0201";
            case 0x100203A4:
                return "cp942";
            case 0x10020462:
                return "cp1122";
            case 0x100203A2:
                return "cp930";
            case 0x000B0001:
                return "TIS620";
            case 0x1002039A:
                return "cp922";
            case 0x10020458:
                return "cp1112";
            case 0x10020399:
                return "cp921";
            case 0x00010020:
                return "US-ASCII";
            case 0x10020396:
                return "cp918";
            case 0x05010001:
                return "UTF-8";
            case 0x05000012:
                return "SJIS";
            case 0x1002044A:
                return "cp1098";
            case 0x05000011:
                return "SJIS";
            case 0x10020449:
                return "cp1097";
            case 0x05000010:
                return "EUC_JP";
            case 0x0001000F:
            {
                if ( org.openorb.util.JREVersion.V1_4 )
                    return "ISO-8859-15";
                else
                    return "ISO8859_15_FDIS";
            }
            case 0x00010009:
                return "ISO-8859-9";
            case 0x00010008:
                return "ISO-8859-8";
            case 0x00010007:
                return "ISO-8859-7";
            case 0x00010006:
                return "ISO-8859-6";
            case 0x00010005:
                return "ISO-8859-5";
            case 0x00010004:
                return "ISO-8859-4";
            case 0x00010003:
                return "ISO-8859-3";
            case 0x00010002:
                return "ISO-8859-2";
            case 0x00010001:
                return "ISO-8859-1";
            case 0x100201F4:
                return "cp500";
            case 0x100204E9:
                return "Cp1257";
            case 0x1002036B:
                return "cp875";
            case 0x100204E8:
                return "Cp1256";
            case 0x1002036A:
                return "cp874";
            case 0x100204E7:
                return "Cp1255";
            case 0x100204E6:
                return "Cp1254";
            case 0x100204E5:
                return "Cp1253";
            case 0x10020367:
                return "cp871";
            case 0x100283BA:
                return "cp33722";
            case 0x100204E4:
                return "Cp1252";
            case 0x10020129:
                return "cp297";
            case 0x10020366:
                return "cp870";
            case 0x100204E3:
                return "Cp1251";
            case 0x10020365:
                return "cp869";
            case 0x100204E2:
                return "Cp1250";
            case 0x10020364:
                return "cp868";
            case 0x10020362:
                return "cp866";
            case 0x10020360:
                return "cp864";
            default:
                return null;
        }
    }

    /**
     * Returns the canonical Java name of a OSF charset
     * and codeset registry entry.
     *
     * @param id An OSF charset and codeset registry id.
     * @return The name of the codeset.
     */
    public static String getCanonicalNameFromId( int id )
    {
        switch( id )
        {
            case 0x1002035F:
                return "Cp863";
            case 0x1002035E:
                return "Cp862";
            case 0x1002035D:
                return "Cp861";
            case 0x1002011D:
                return "Cp285";
            case 0x1002011C:
                return "Cp284";
            case 0x10020359:
                return "Cp857";
            case 0x10020358:
                return "Cp856";
            case 0x10020357:
                return "Cp855";
            case 0x1002236A:
                return "Cp874";
            case 0x10020118:
                return "Cp280";
            case 0x10020354:
                return "Cp852";
            case 0x10020116:
                return "Cp278";
            case 0x10020115:
                return "Cp277";
            case 0x10020352:
                return "Cp850";
            case 0x10020111:
                return "Cp273";
            case 0x10022360:
                return "Cp864";
            case 0x10020346:
                return "Cp838";
            case 0x10020402:
                return "Cp1026";
            case 0x10020401:
                return "Cp1025";
            case 0x10027025:
                return "Cp037";
            case 0x100201B5:
                return "Cp437";
            case 0x10022346:
                return "Cp838";
            case 0x100203EE:
                return "Cp1006";
            case 0x10020567:
                return "Cp1383";
            case 0x10020565:
                return "Cp1381";
            case 0x100201A8:
                return "Cp424";
            case 0x00050010:
                return "EUC_TW";
            case 0x100201A4:
                return "Cp420";
            case 0x10020025:
                return "Cp037";
            case 0x0005000A:
                return "EUC_TW";
            case 0x10026358:
                return "Cp856";
            case 0x00050002:
                return "EUC_TW";
            case 0x00050001:
                return "EUC_TW";
            case 0x10026352:
                return "Cp850";
            case 0x100221A4:
                return "Cp420";
            case 0x100203CA:
                return "Cp970";
            case 0x0004000A:
                return "EUC_KR";
            case 0x100203C4:
                return "Cp964";
            case 0x10028358:
                return "Cp856";
            case 0x00010109:
                return "UnicodeBigUnmarked";
            case 0x00040001:
                return "EUC_KR";
            case 0x10021365:
                return "Cp869";
            case 0x00010102:
                return "UnicodeBigUnmarked";
            case 0x10021364:
                return "Cp868";
            case 0x00010101:
                return "UnicodeBigUnmarked";
            case 0x00010100:
                return "UnicodeBigUnmarked";
            case 0x100203B6:
                return "Cp950";
            case 0x10021360:
                return "Cp864";
            case 0x100203B5:
                return "Cp949";
            case 0x00030010:
                return "EUC_JP";
            case 0x100203B4:
                return "Cp948";
            case 0x100203AF:
                return "Cp943";
            case 0x0003000A:
                return "JIS0212";
            case 0x10021359:
                return "Cp857";
            case 0x100203AE:
                return "Cp942";
            case 0x10021358:
                return "Cp856";
            case 0x10021357:
                return "Cp855";
            case 0x10010008:
                return "Big5";
            case 0x10010007:
                return "EUC_CN";
            case 0x00030006:
                return "JIS0208";
            case 0x100203AB:
                return "Cp939";
            case 0x00030005:
                return "JIS0208";
            case 0x10021354:
                return "Cp852";
            case 0x00030004:
                return "JIS0208";
            case 0x100203A9:
                return "Cp937";
            case 0x10021352:
                return "Cp850";
            case 0x100203A7:
                return "Cp935";
            case 0x00030001:
                return "JIS0201";
            case 0x100203A4:
                return "Cp942";
            case 0x10020462:
                return "Cp1122";
            case 0x100203A2:
                return "Cp930";
            case 0x000B0001:
                return "TIS620";
            case 0x1002039A:
                return "Cp922";
            case 0x10020458:
                return "Cp1112";
            case 0x10020399:
                return "Cp921";
            case 0x00010020:
                return "ASCII";
            case 0x10020396:
                return "Cp918";
            case 0x05010001:
                return "UTF8";
            case 0x05000012:
                return "SJIS";
            case 0x1002044A:
                return "Cp1098";
            case 0x05000011:
                return "SJIS";
            case 0x10020449:
                return "Cp1097";
            case 0x05000010:
                return "EUC_JP";
            case 0x0001000F:
            {
                if ( org.openorb.util.JREVersion.V1_4 )
                    return "ISO-8859-15";
                else
                    return "ISO8859_15_FDIS";
            }
            case 0x00010009:
                return "ISO8859_9";
            case 0x00010008:
                return "ISO8859_8";
            case 0x00010007:
                return "ISO8859_7";
            case 0x00010006:
                return "ISO8859_6";
            case 0x00010005:
                return "ISO8859_5";
            case 0x00010004:
                return "ISO8859_4";
            case 0x00010003:
                return "ISO8859_3";
            case 0x00010002:
                return "ISO8859_2";
            case 0x00010001:
                return "ISO8859_1";
            case 0x100201F4:
                return "Cp500";
            case 0x100204E9:
                return "Cp1257";
            case 0x1002036B:
                return "Cp875";
            case 0x100204E8:
                return "Cp1256";
            case 0x1002036A:
                return "Cp874";
            case 0x100204E7:
                return "Cp1255";
            case 0x100204E6:
                return "Cp1254";
            case 0x100204E5:
                return "Cp1253";
            case 0x10020367:
                return "Cp871";
            case 0x100283BA:
                return "Cp33722";
            case 0x100204E4:
                return "Cp1252";
            case 0x10020129:
                return "Cp297";
            case 0x10020366:
                return "Cp870";
            case 0x100204E3:
                return "Cp1251";
            case 0x10020365:
                return "Cp869";
            case 0x100204E2:
                return "Cp1250";
            case 0x10020364:
                return "Cp868";
            case 0x10020362:
                return "Cp866";
            case 0x10020360:
                return "Cp864";
            default:
                return null;
        }
    }

    /**
     * Returns the description of a OSF charset and codeset
     * registry entry.
     *
     * @param id An OSF charset and codeset registry id.
     * @return The OSF description of the codeset.
     */
    public static String getDescriptionFromId( int id )
    {
        switch( id )
        {
            case 0x1002035F:
                return "IBM-863 (CCSID 00863); PC Data Canadian French";
            case 0x1002035E:
                return "IBM-862 (CCSID 00862); PC Data Hebrew";
            case 0x1002035D:
                return "IBM-861 (CCSID 00861); PC Data Iceland";
            case 0x1002011D:
                return "IBM-285 (CCSID 00285); CECP for United Kingdom";
            case 0x1002011C:
                return "IBM-284 (CCSID 00284); CECP for Spain, Latin America (Spanish)";
            case 0x10020359:
                return "IBM-857 (CCSID 00857); Turkish Latin-5 PC Data";
            case 0x10020358:
                return "IBM-856 (CCSID 00856); Hebrew PC Data (extensions)";
            case 0x10020357:
                return "IBM-855 (CCSID 00855); Cyrillic PC Data";
            case 0x1002236A:
                return "IBM-874 (CCSID 09066); Thai PC Display Extended SBCS";
            case 0x10020118:
                return "IBM-280 (CCSID 00280); CECP for Italy";
            case 0x10020354:
                return "IBM-852 (CCSID 00852); Multilingual Latin-2";
            case 0x10020116:
                return "IBM-278 (CCSID 00278); CECP for Finland, Sweden";
            case 0x10020115:
                return "IBM-277 (CCSID 00277); CECP for Denmark, Norway";
            case 0x10020352:
                return "IBM-850 (CCSID 00850); Multilingual IBM PC Data-MLP 222";
            case 0x10020111:
                return "IBM-273 (CCSID 00273); CECP for Austria, Germany";
            case 0x10022360:
                return "IBM-864 (CCSID 09056); Arabic PC Data (unshaped)";
            case 0x10020346:
                return "IBM-838 (CCSID 00838); Thai Host Extended SBCS";
            case 0x10020402:
                return "IBM-1026 (CCSID 01026); Turkish Latin-5";
            case 0x10020401:
                return "IBM-1025 (CCSID 01025); Cyrillic Multilingual";
            case 0x10027025:
                return "IBM-037 (CCSID 28709); T-Ch Host Extended SBCS";
            case 0x100201B5:
                return "IBM-437 (CCSID 00437); PC USA";
            case 0x10022346:
                return "IBM-838 (CCSID 09030); Thai Host Extended SBCS";
            case 0x100203EE:
                return "IBM-1006 (CCSID 01006); Urdu 8-bit";
            case 0x10020567:
                return "IBM-1383 (CCSID 01383); S-Ch EUC GB 2312-80 set (1382)";
            case 0x10020565:
                return "IBM-1381 (CCSID 01381); S-Ch PC Data Mixed incl 1880 UDC";
            case 0x100201A8:
                return "IBM-424 (CCSID 00424); Hebrew";
            case 0x00050010:
                return "CNS eucTW:1993; Taiwanese EUC";
            case 0x100201A4:
                return "IBM-420 (CCSID 00420); Arabic (presentation shapes)";
            case 0x10020025:
                return "IBM-037 (CCSID 00037); CECP for USA, Canada, NL, Ptgl, Brazil, Australia, NZ";
            case 0x0005000A:
                return "CNS eucTW:1991; Taiwanese EUC";
            case 0x10026358:
                return "IBM-856 (CCSID 25432); Hebrew PC Display (extensions)";
            case 0x00050002:
                return "CNS 11643:1992; Taiwanese Extended Hanzi Graphic Chars";
            case 0x00050001:
                return "CNS 11643:1986; Taiwanese Hanzi Graphic Characters";
            case 0x10026352:
                return "IBM-850 (CCSID 25426); Multilingual IBM PC Display-MLP";
            case 0x100221A4:
                return "IBM-420 (CCSID 08612); Arabic (base shapes only)";
            case 0x100203CA:
                return "IBM-970 (CCSID 00970); Korean EUC";
            case 0x0004000A:
                return "KS eucKR:1991; Korean EUC";
            case 0x100203C4:
                return "IBM-964 (CCSID 00964); T-Chinese EUC CNS1163 plane 1,2";
            case 0x10028358:
                return "IBM-856 (CCSID 33624); Hebrew PC Display";
            case 0x00010109:
                return "ISO/IEC 10646-1:1993; UTF-16, UCS Transformation Format 16-bit form";
            case 0x00040001:
                return "KS C5601:1987; Korean Hangul and Hanja Graphic Characters";
            case 0x10021365:
                return "IBM-869 (CCSID 04965); Greek PC Data";
            case 0x00010102:
                return "ISO/IEC 10646-1:1993; UCS-2, Level 3";
            case 0x10021364:
                return "IBM-868 (CCSID 04964); PC Data for Urdu";
            case 0x00010101:
                return "ISO/IEC 10646-1:1993; UCS-2, Level 2";
            case 0x00010100:
                return "ISO/IEC 10646-1:1993; UCS-2, Level 1";
            case 0x100203B6:
                return "IBM-950 (CCSID 00950); T-Ch PC Data Mixed";
            case 0x10021360:
                return "IBM-864 (CCSID 04960); Arabic PC Data (all shapes)";
            case 0x100203B5:
                return "IBM-949 (CCSID 00949); IBM KS PC Data Mixed";
            case 0x00030010:
                return "JIS eucJP:1993; Japanese EUC";
            case 0x100203B4:
                return "IBM-948 (CCSID 00948); T-Ch PC Data Mixed";
            case 0x100203AF:
                return "IBM-943 (CCSID 00943); Japanese PC MBCS for Open";
            case 0x0003000A:
                return "JIS X0212:1990; Supplementary Japanese Kanji Graphic Chars";
            case 0x10021359:
                return "IBM-857 (CCSID 04953); Turkish Latin-5 PC Data";
            case 0x100203AE:
                return "IBM-942 (CCSID 00942); Japanese PC Data Mixed";
            case 0x10021358:
                return "IBM-856 (CCSID 04952); Hebrew PC Data";
            case 0x10021357:
                return "IBM-855 (CCSID 04951); Cyrillic Personal Computer";
            case 0x10010008:
                return "HP big5; encoding method for Traditional Chinese";
            case 0x10010007:
                return "HP15CN; encoding method for Simplified Chinese";
            case 0x00030006:
                return "JIS X0208:1990 Japanese Kanji Graphic Characters";
            case 0x100203AB:
                return "IBM-939 (CCSID 00939); Latin-Kanji Host MBCS";
            case 0x00030005:
                return "JIS X0208:1983 Japanese Kanji Graphic Characters";
            case 0x10021354:
                return "IBM-852 (CCSID 04948); Latin-2 Personal Computer";
            case 0x00030004:
                return "JIS X0208:1978 Japanese Kanji Graphic Characters";
            case 0x100203A9:
                return "IBM-937 (CCSID 00937); T-Ch Host Mixed";
            case 0x10021352:
                return "IBM-850 (CCSID 04946); Multilingual IBM PC Data-190";
            case 0x100203A7:
                return "IBM-935 (CCSID 00935); S-Ch Host Mixed";
            case 0x00030001:
                return "JIS X0201:1976; Japanese phonetic characters";
            case 0x100203A4:
                return "IBM-932 (CCSID 00932); Japanese PC Data Mixed";
            case 0x10020462:
                return "IBM-1122 (CCSID 01122); Estonia";
            case 0x100203A2:
                return "IBM-930 (CCSID 00930); Kat-Kanji Host MBCS Ext-SBCS";
            case 0x000B0001:
                return "TIS 620-2529, Thai characters";
            case 0x1002039A:
                return "IBM-922 (CCSID 00922); Estonia 8-Bit";
            case 0x10020458:
                return "IBM-1112 (CCSID 01112); Baltic Multilingual";
            case 0x10020399:
                return "IBM-921 (CCSID 00921); Baltic 8-Bit";
            case 0x00010020:
                return "ISO 646:1991 IRV (International Reference Version)";
            case 0x10020396:
                return "IBM-918 (CCSID 00918); Urdu";
            case 0x05010001:
                return "X/Open UTF-8; UCS Transformation Format 8 (UTF-8)";
            case 0x05000012:
                return "OSF Japanese SJIS-2";
            case 0x1002044A:
                return "IBM-1098 (CCSID 01098); Farsi PC Data";
            case 0x05000011:
                return "OSF Japanese SJIS-1";
            case 0x10020449:
                return "IBM-1097 (CCSID 01097); Farsi";
            case 0x05000010:
                return "OSF Japanese UJIS";
            case 0x0001000F:
                return "ISO/IEC 8859-15:1999; Latin Alphabet No. 9";
            case 0x00010009:
                return "ISO/IEC 8859-9:1989; Latin Alphabet No. 5";
            case 0x00010008:
                return "ISO 8859-8:1988; Latin-Hebrew Alphabet";
            case 0x00010007:
                return "ISO 8859-7:1987; Latin-Greek Alphabet";
            case 0x00010006:
                return "ISO 8859-6:1987; Latin-Arabic Alphabet";
            case 0x00010005:
                return "ISO/IEC 8859-5:1988; Latin-Cyrillic Alphabet";
            case 0x00010004:
                return "ISO 8859-4:1988; Latin Alphabet No. 4";
            case 0x00010003:
                return "ISO 8859-3:1988; Latin Alphabet No. 3";
            case 0x00010002:
                return "ISO 8859-2:1987; Latin Alphabet No. 2";
            case 0x00010001:
                return "ISO 8859-1:1987; Latin Alphabet No. 1";
            case 0x100201F4:
                return "IBM-500 (CCSID 00500); CECP for Belgium, Switzerland";
            case 0x100204E9:
                return "IBM-1257 (CCSID 01257); MS Windows Baltic";
            case 0x1002036B:
                return "IBM-875 (CCSID 00875); Greek";
            case 0x100204E8:
                return "IBM-1256 (CCSID 01256); MS Windows Arabic";
            case 0x1002036A:
                return "IBM-874 (CCSID 00874); Thai PC Display Extended SBCS";
            case 0x100204E7:
                return "IBM-1255 (CCSID 01255); MS Windows Hebrew";
            case 0x100204E6:
                return "IBM-1254 (CCSID 01254); MS Windows Turkey";
            case 0x100204E5:
                return "IBM-1253 (CCSID 01253); MS Windows Greek";
            case 0x10020367:
                return "IBM-871 (CCSID 00871); CECP for Iceland";
            case 0x100283BA:
                return "IBM33722 (CCSID 33722); Japanese EUC JISx201,208,212";
            case 0x100204E4:
                return "IBM-1252 (CCSID 01252); MS Windows Latin-1";
            case 0x10020129:
                return "IBM-297 (CCSID 00297); CECP for France";
            case 0x10020366:
                return "IBM-870 (CCSID 00870); Multilingual Latin-2 EBCDIC";
            case 0x100204E3:
                return "IBM-1251 (CCSID 01251); MS Windows Cyrillic";
            case 0x10020365:
                return "IBM-869 (CCSID 00869); Greek PC Data";
            case 0x100204E2:
                return "IBM-1250 (CCSID 01250); MS Windows Latin-2";
            case 0x10020364:
                return "IBM-868 (CCSID 00868); Urdu PC Data";
            case 0x10020362:
                return "IBM-866 (CCSID 00866); PC Data Cyrillic 2";
            case 0x10020360:
                return "IBM-864 (CCSID 00864); Arabic PC Data";
            default:
                return null;
        }
    }

    /**
     * Return an array of CodeSet objects that support the
     * specified charset.
     *
     * @param charset An OSF charset registry id.
     * @return An array of codeset ids supporting the charset.
     */
    public static int[] getCodeSetsFromCharset( short charset )
    {
        switch( charset )
        {
            case 0x0200:
                return new int[] {
                    0x1002236A,
                    0x10022346,
                    0x1002036A,
                    0x10020346,
                    0x000B0001
                };
            case 0x0082:
                return new int[] {
                    0x100283BA,
                    0x00030010,
                    0x0003000A
                };
            case 0x0081:
                return new int[] {
                    0x100283BA,
                    0x100203AF,
                    0x100203AE,
                    0x100203AB,
                    0x100203A4,
                    0x100203A2,
                    0x05000012,
                    0x05000011,
                    0x05000010,
                    0x00030010,
                    0x00030006,
                    0x00030005,
                    0x00030004
                };
            case 0x0080:
                return new int[] {
                    0x100283BA,
                    0x100203AF,
                    0x100203AE,
                    0x100203AB,
                    0x100203A4,
                    0x100203A2,
                    0x05000012,
                    0x05000011,
                    0x05000010,
                    0x00030010,
                    0x00030001
                };
            case 0x001A:
                return new int[] {
                    0x100204E9,
                    0x10020462,
                    0x10020458,
                    0x1002039A,
                    0x10020399
                };
            case 0x0019:
                return new int[] {
                    0x10021359,
                    0x100204E6,
                    0x10020402,
                    0x10020359,
                    0x00010009
                };
            case 0x0018:
                return new int[] {
                    0x10028358,
                    0x10026358,
                    0x10021358,
                    0x100204E7,
                    0x1002035E,
                    0x10020358,
                    0x100201A8,
                    0x00010008
                };
            case 0x0017:
                return new int[] {
                    0x10021365,
                    0x100204E5,
                    0x1002036B,
                    0x10020365,
                    0x00010007
                };
            case 0x0101:
                return new int[] {
                    0x100203CA,
                    0x0004000A
                };
            case 0x0016:
                return new int[] {
                    0x10022360,
                    0x100221A4,
                    0x10021364,
                    0x10021360,
                    0x100204E8,
                    0x1002044A,
                    0x10020449,
                    0x100203EE,
                    0x10020396,
                    0x10020364,
                    0x10020360,
                    0x100201A4,
                    0x00010006
                };
            case 0x0100:
                return new int[] {
                    0x100203CA,
                    0x100203B5,
                    0x0004000A,
                    0x00040001
                };
            case 0x0015:
                return new int[] {
                    0x10021357,
                    0x100204E3,
                    0x10020401,
                    0x10020362,
                    0x10020357,
                    0x00010005
                };
            case 0x0014:
                return new int[] {
                    0x00010004
                };
            case 0x0013:
                return new int[] {
                    0x00010003
                };
            case 0x0012:
                return new int[] {
                    0x10021354,
                    0x100204E2,
                    0x10020366,
                    0x10020354,
                    0x00010002
                };
            case 0x0011:
                return new int[] {
                    0x10026352,
                    0x10021352,
                    0x100204E4,
                    0x100203CA,
                    0x10020367,
                    0x1002035F,
                    0x1002035D,
                    0x10020352,
                    0x100201F4,
                    0x100201B5,
                    0x10020129,
                    0x1002011D,
                    0x1002011C,
                    0x10020118,
                    0x10020116,
                    0x10020115,
                    0x10020111,
                    0x10020025,
                    0x0004000A,
                    0x00030010,
                    0x0001000F,
                    0x00010001
                };
            case 0x0300:
                return new int[] {
                    0x10020567,
                    0x10020565,
                    0x100203A7,
                    0x10010007
                };
            case 0x0181:
                return new int[] {
                    0x00050010,
                    0x00050002
                };
            case 0x0180:
                return new int[] {
                    0x100203C4,
                    0x100203B6,
                    0x100203B4,
                    0x100203A9,
                    0x10010008,
                    0x0005000A,
                    0x00050001
                };
            case 0x1000:
                return new int[] {
                    0x05010001,
                    0x00010109,
                    0x00010102,
                    0x00010101,
                    0x00010100
                };
            case 0x0001:
                return new int[] {
                    0x10027025,
                    0x10020567,
                    0x10020565,
                    0x100203C4,
                    0x100203B6,
                    0x100203B5,
                    0x100203B4,
                    0x100203A9,
                    0x100203A7,
                    0x10010008,
                    0x10010007,
                    0x05000012,
                    0x05000011,
                    0x05000010,
                    0x00050010,
                    0x0005000A,
                    0x00010020
                };
            default:
                return null;
        }
    }
}
