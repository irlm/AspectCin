/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.rmi.complex;

/**
 * This class serves as a container for the different data
 * type families: primitive, array, object.
 *   Primitive data type: int
 *   Array: array of int, array of Object
 *   Object: String (is treated special), Inner Class
 * All instances must be Serializable in order to get marshaled
 * into an IIOP object stream.
 */
public class TypeHolder
    implements java.io.Serializable
{
    public class InnerPublicClass
        implements java.io.Serializable
    {
                  static final int     CONST_I = 0x11;
        public    static final int PUB_CONST_I = 0x12;
        protected static final int PRO_CONST_I = 0x13;
        private   static final int PRI_CONST_I = 0x14;

        /** non-static inner classes cannot have static member fields */
        /*        static int     s_i = 0x21;
        public    static int s_pub_i = 0x22;
        protected static int s_pro_i = 0x23;
        private   static int s_pri_i = 0x24;*/

                  final int     f_i = 0x31;
        public    final int pub_f_i = 0x32;
        protected final int pro_f_i = 0x33;
        private   final int pri_f_i = 0x34;

                  int     i = 0x41;
        public    int pub_i = 0x42;
        protected int pro_i = 0x43;
        private   int pri_i = 0x44;

        public InnerPublicClass()
        {
        }

        public void changeValues()
        {
            // consts are final

            // no static members in inner class

            // can't increment primitive final members

            // only non-final members can be incremented
                i++;
            pub_i++;
            pro_i++;
            pri_i++;
        }
        public String toString()
        {
            StringBuffer sb = new StringBuffer( 2000 );

            sb.append( "\n\t    CONST_I=" +     CONST_I );
            sb.append( "\n\tPUB_CONST_I=" + PUB_CONST_I );
            sb.append( "\n\tPRO_CONST_I=" + PRO_CONST_I );
            sb.append( "\n\tPRI_CONST_I=" + PRI_CONST_I );

            sb.append( "\n\t    s_i=N/A" );
            sb.append( "\n\ts_pub_i=N/A" );
            sb.append( "\n\ts_pro_i=N/A" );
            sb.append( "\n\ts_pri_i=N/A" );

            sb.append( "\n\t    f_i=" +     f_i );
            sb.append( "\n\tpub_f_i=" + pub_f_i );
            sb.append( "\n\tpro_f_i=" + pro_f_i );
            sb.append( "\n\tpri_f_i=" + pri_f_i );

            sb.append( "\n\t    i=" +     i );
            sb.append( "\n\tpub_i=" + pub_i );
            sb.append( "\n\tpro_i=" + pro_i );
            sb.append( "\n\tpri_i=" + pri_i );

            return sb.toString();
        }
    }

    /**
     * This is like a global class except that it must be qualified by the
     * enclosing class. This class can be instantiated from outside of the
     * enclosing class. The fact that it is declared inside of SerializableClass is
     * just a syntactic grouping mechanism.
     */
    public static class InnerPublicStaticClass
        implements java.io.Serializable
    {
                  static final int     CONST_I = 0x51;
        public    static final int PUB_CONST_I = 0x52;
        protected static final int PRO_CONST_I = 0x53;
        private   static final int PRI_CONST_I = 0x54;

                  static int     s_i = 0x61;
        public    static int s_pub_i = 0x62;
        protected static int s_pro_i = 0x63;
        private   static int s_pri_i = 0x64;

                  final int     f_i = 0x71;
        public    final int pub_f_i = 0x72;
        protected final int pro_f_i = 0x73;
        private   final int pri_f_i = 0x74;

                  int     i = 0x81;
        public    int pub_i = 0x82;
        protected int pro_i = 0x83;
        private   int pri_i = 0x84;

        public InnerPublicStaticClass()
        {
        }

        public void changeValues()
        {
            // consts are final

            // non-static final members can be incremented
                s_i++;
            s_pub_i++;
            s_pro_i++;
            s_pri_i++;

            // can't increment primitive final members

            // non-final members can be incremented
                i++;
            pub_i++;
            pro_i++;
            pri_i++;
        }

        public String toString()
        {
            StringBuffer sb = new StringBuffer( 2000 );

            sb.append( "\n\t    CONST_I=" +     CONST_I );
            sb.append( "\n\tPUB_CONST_I=" + PUB_CONST_I );
            sb.append( "\n\tPRO_CONST_I=" + PRO_CONST_I );
            sb.append( "\n\tPRI_CONST_I=" + PRI_CONST_I );

            sb.append( "\n\t    s_i=" +     s_i );
            sb.append( "\n\ts_pub_i=" + s_pub_i );
            sb.append( "\n\ts_pro_i=" + s_pro_i );
            sb.append( "\n\ts_pri_i=" + s_pri_i );

            sb.append( "\n\t    f_i=" +     f_i );
            sb.append( "\n\tpub_f_i=" + pub_f_i );
            sb.append( "\n\tpro_f_i=" + pro_f_i );
            sb.append( "\n\tpri_f_i=" + pri_f_i );

            sb.append( "\n\t    i=" +     i );
            sb.append( "\n\tpub_i=" + pub_i );
            sb.append( "\n\tpro_i=" + pro_i );
            sb.append( "\n\tpri_i=" + pri_i );

            return sb.toString();
        }
    }


    // int
              static final int CONST_I     = 0x91;
    public    static final int PUB_CONST_I = 0x92;
    protected static final int PRO_CONST_I = 0x93;
    private   static final int PRI_CONST_I = 0x94;

              static int     s_i = 0xA1;
    public    static int s_pub_i = 0xA2;
    protected static int s_pro_i = 0xA3;
    private   static int s_pri_i = 0xA4;

              final int     f_i = 0xB1;
    public    final int pub_f_i = 0xB2;
    protected final int pro_f_i = 0xB3;
    private   final int pri_f_i = 0xB4;

              int     i = 0xC1;
    public    int pub_i = 0xC2;
    protected int pro_i = 0xC3;
    private   int pri_i = 0xC4;


    // String
              static final String     CONST_STR = "A static final string";
    public    static final String PUB_CONST_STR = "A static public final string";
    protected static final String PRO_CONST_STR = "A static protected final string";
    private   static final String PRI_CONST_STR = "A static private final string";

              static String     s_str = "A static string";
    public    static String s_pub_str = "A static public string";
    protected static String s_pro_str = "A static protected string";
    private   static String s_pri_str = "A static private string";

              final String     f_str = "A final string";
    public    final String pub_f_str = "A public final string";
    protected final String pro_f_str = "A protected final string";
    private   final String pri_f_str = "A private final string";

              String     str = "A string";
    public    String s_pubtr = "A public string";
    protected String s_protr = "A protected string";
    private   String s_pritr = "A private string";


    // String array
              static final String[]     CONST_STRARR = { "          static final string array element 1",           "static final string array element 2" };
    public    static final String[] PUB_CONST_STRARR = { "public    static final string array element 1", "public    static final string array element 2" };
    protected static final String[] PRO_CONST_STRARR = { "protected static final string array element 1", "protected static final string array element 2" };
    private   static final String[] PRI_CONST_STRARR = { "private   static final string array element 1", "private   static final string array element 2" };

              static String[]     s_strarr = { "          static string array element 1",           "static string array element 2" };
    public    static String[] s_pub_strarr = { "public    static string array element 1", "public    static string array element 2" };
    protected static String[] s_pro_strarr = { "protected static string array element 1", "protected static string array element 2" };
    private   static String[] s_pri_strarr = { "private   static string array element 1", "private   static string array element 2" };

              final String[]     f_strarr = { "          final string array element 1",           "final string array element 2" };
    public    final String[] pub_f_strarr = { "public    final string array element 1", "public    final string array element 2" };
    protected final String[] pro_f_strarr = { "protected final string array element 1", "protected final string array element 2" };
    private   final String[] pri_f_strarr = { "private   final string array element 1", "private   final string array element 2" };

              String[]     strarr = { "          string array element 1",           "string array element 2" };
    public    String[] s_pubtrarr = { "public    string array element 1", "public    string array element 2" };
    protected String[] s_protrarr = { "protected string array element 1", "protected string array element 2" };
    private   String[] s_pritrarr = { "private   string array element 1", "private   string array element 2" };


    // Inner Class
    /* non-static variable this cannot be referenced from a static context
              static final InnerPublicClass     CONST_INNERPUBLIC = new InnerPublicClass();
    public    static final InnerPublicClass PUB_CONST_INNERPUBLIC = new InnerPublicClass();
    protected static final InnerPublicClass PRO_CONST_INNERPUBLIC = new InnerPublicClass();
    private   static final InnerPublicClass PRI_CONST_INNERPUBLIC = new InnerPublicClass();*/

    /* non-static variable this cannot be referenced from a static context
              static InnerPublicClass     s_innerpublic = new InnerPublicClass();
    public    static InnerPublicClass s_pub_innerpublic = new InnerPublicClass();
    protected static InnerPublicClass s_pro_innerpublic = new InnerPublicClass();
    private   static InnerPublicClass s_pri_innerpublic = new InnerPublicClass();*/

              final InnerPublicClass     f_innerpublic = new InnerPublicClass();
    public    final InnerPublicClass pub_f_innerpublic = new InnerPublicClass();
    protected final InnerPublicClass pro_f_innerpublic = new InnerPublicClass();
    private   final InnerPublicClass pri_f_innerpublic = new InnerPublicClass();

              InnerPublicClass     innerpublic = new InnerPublicClass();
    public    InnerPublicClass pub_innerpublic = new InnerPublicClass();
    protected InnerPublicClass pro_innerpublic = new InnerPublicClass();
    private   InnerPublicClass pri_innerpublic = new InnerPublicClass();

    // Inner static class
              static final InnerPublicStaticClass     CONST_SINNERPUBLIC = new InnerPublicStaticClass();
    public    static final InnerPublicStaticClass PUB_CONST_SINNERPUBLIC = new InnerPublicStaticClass();
    protected static final InnerPublicStaticClass PRO_CONST_SINNERPUBLIC = new InnerPublicStaticClass();
    private   static final InnerPublicStaticClass PRI_CONST_SINNERPUBLIC = new InnerPublicStaticClass();

              static InnerPublicStaticClass     s_sinnerpublic = new InnerPublicStaticClass();
    public    static InnerPublicStaticClass s_pub_sinnerpublic = new InnerPublicStaticClass();
    protected static InnerPublicStaticClass s_pro_sinnerpublic = new InnerPublicStaticClass();
    private   static InnerPublicStaticClass s_pri_sinnerpublic = new InnerPublicStaticClass();

              final InnerPublicStaticClass     f_sinnerpublic = new InnerPublicStaticClass();
    public    final InnerPublicStaticClass pub_f_sinnerpublic = new InnerPublicStaticClass();
    protected final InnerPublicStaticClass pro_f_sinnerpublic = new InnerPublicStaticClass();
    private   final InnerPublicStaticClass pri_f_sinnerpublic = new InnerPublicStaticClass();

              InnerPublicStaticClass     sinnerpublic = new InnerPublicStaticClass();
    public    InnerPublicStaticClass s_pubinnerpublic = new InnerPublicStaticClass();
    protected InnerPublicStaticClass s_proinnerpublic = new InnerPublicStaticClass();
    private   InnerPublicStaticClass s_priinnerpublic = new InnerPublicStaticClass();

    //
    // Methods of the enclosing class
    //

    public TypeHolder()
    {
    }

    public String showStringArray( String name, String[] strs )
    {
        String result = null;
        if ( strs != null )
        {
            StringBuffer sb = new StringBuffer( 2000 );
            sb.append( "\n " );
            for ( int j = 0; i < strs.length; i++ )
            {
                sb.append( name + "[" + j + "]=" + strs[ j ] + ", " );
            }
            result = sb.toString();
        }
        return result;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer( 2000 );

        // int
        sb.append( "\n    CONST_I=" +     CONST_I );
        sb.append( "\nPUB_CONST_I=" + PUB_CONST_I );
        sb.append( "\nPRO_CONST_I=" + PRO_CONST_I );
        sb.append( "\nPRI_CONST_I=" + PRI_CONST_I );

        sb.append( "\n    s_i=" +     f_i );
        sb.append( "\ns_pub_i=" + pub_f_i );
        sb.append( "\ns_pro_i=" + pro_f_i );
        sb.append( "\ns_pri_i=" + pri_f_i );

        sb.append( "\n    f_i=" +     f_i );
        sb.append( "\npub_f_i=" + pub_f_i );
        sb.append( "\npro_f_i=" + pro_f_i );
        sb.append( "\npri_f_i=" + pri_f_i );

        sb.append( "\n    i=" +     i );
        sb.append( "\npub_i=" + pub_i );
        sb.append( "\npro_i=" + pro_i );
        sb.append( "\npri_i=" + pri_i );

        // String
        sb.append( "\n" + CONST_STR );
        sb.append( "\n" + PUB_CONST_STR );
        sb.append( "\n" + PRO_CONST_STR );
        sb.append( "\n" + PRI_CONST_STR );

        sb.append( "\n" + s_str );
        sb.append( "\n" + s_pub_str );
        sb.append( "\n" + s_pro_str );
        sb.append( "\n" + s_pri_str );

        sb.append( "\n" + f_str );
        sb.append( "\n" + pub_f_str );
        sb.append( "\n" + pro_f_str );
        sb.append( "\n" + pri_f_str );

        sb.append( "\n" + str );
        sb.append( "\n" + s_pubtr );
        sb.append( "\n" + s_protr );
        sb.append( "\n" + s_pritr );

        // String array
        sb.append( showStringArray( "\n    CONST_STRARR",     CONST_STRARR ) );
        sb.append( showStringArray( "\nPUB_CONST_STRARR", PUB_CONST_STRARR ) );
        sb.append( showStringArray( "\nPRO_CONST_STRARR", PRO_CONST_STRARR ) );
        sb.append( showStringArray( "\nPRI_CONST_STRARR", PRI_CONST_STRARR ) );

        sb.append( showStringArray( "\n    s_strarr",     s_strarr ) );
        sb.append( showStringArray( "\ns_pub_strarr", s_pub_strarr ) );
        sb.append( showStringArray( "\ns_pro_strarr", s_pro_strarr ) );
        sb.append( showStringArray( "\ns_pri_strarr", s_pri_strarr ) );

        sb.append( showStringArray( "\n    f_strarr",     f_strarr ) );
        sb.append( showStringArray( "\npub_f_strarr", pub_f_strarr ) );
        sb.append( showStringArray( "\npro_f_strarr", pro_f_strarr ) );
        sb.append( showStringArray( "\npri_f_strarr", pri_f_strarr ) );

        sb.append( showStringArray( "\n    strarr",     strarr ) );
        sb.append( showStringArray( "\ns_pubtrarr", s_pubtrarr ) );
        sb.append( showStringArray( "\ns_protrarr", s_protrarr ) );
        sb.append( showStringArray( "\ns_pritrarr", s_pritrarr ) );

        // inner class
        sb.append( "\n" +     f_innerpublic );
        sb.append( "\n" + pub_f_innerpublic );
        sb.append( "\n" + pro_f_innerpublic );
        sb.append( "\n" + pri_f_innerpublic );

        sb.append( "\n" +     innerpublic );
        sb.append( "\n" + pub_innerpublic );
        sb.append( "\n" + pro_innerpublic );
        sb.append( "\n" + pri_innerpublic );

        return sb.toString();
    }

    public void changeValues()
    {
        i++;
        pub_i++;
        pro_i++;
        pri_i++;

        if ( str != null )
        {
           str += ( "." + i );
        }

        int len = strarr.length;
        if ( len > 0 )
        {
            String[] newstrarr = new String[ len + 1 ];
            for ( int j = 0; j < len; j++ )
            {
                newstrarr[ j ] = strarr[ j ];
            }
            newstrarr[ len ] = ( "strarr" + i );
            strarr = newstrarr;
        }

        // Inner class
            f_innerpublic.changeValues();
        pub_f_innerpublic.changeValues();
        pro_f_innerpublic.changeValues();
        pri_f_innerpublic.changeValues();

            innerpublic.changeValues();
        pub_innerpublic.changeValues();
        pro_innerpublic.changeValues();
        pri_innerpublic.changeValues();

        // Inner static class
            CONST_SINNERPUBLIC.changeValues();
        PUB_CONST_SINNERPUBLIC.changeValues();
        PRO_CONST_SINNERPUBLIC.changeValues();
        PRI_CONST_SINNERPUBLIC.changeValues();

            s_sinnerpublic.changeValues();
        s_pub_sinnerpublic.changeValues();
        s_pro_sinnerpublic.changeValues();
        s_pri_sinnerpublic.changeValues();

            f_sinnerpublic.changeValues();
        pub_f_sinnerpublic.changeValues();
        pro_f_sinnerpublic.changeValues();
        pri_f_sinnerpublic.changeValues();

            sinnerpublic.changeValues();
        s_pubinnerpublic.changeValues();
        s_proinnerpublic.changeValues();
        s_priinnerpublic.changeValues();
    }

    public boolean equals( Object obj )
    {
        TypeHolder th = ( TypeHolder ) obj;
        // TODO: Implement this !!!
        return true;
    }
}

