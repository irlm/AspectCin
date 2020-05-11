/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util;

import java.io.IOException;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.io.OutputStream;

import java.util.Arrays;
import java.util.Comparator;

/**
 * This class provides helper methods for repositiory id
 * handling.
 *
 * @author Chris Wood
 * @version $Revision: 1.8 $ $Date: 2004/05/13 02:25:33 $
 */
public final class RepoIDHelper
{
    /**
     * Utility class. Do not instantiate.
     */
    private RepoIDHelper()
    {
    }

    private static java.io.OutputStream s_dummy_output_stream =
          new DummyOutputStream();

    private static class DummyOutputStream
        extends OutputStream
    {
        public void write( int b )
        {
        }

        public void write( byte [] buf )
        {
        }

        public void write( byte [] buf, int off, int len )
        {
        }

        public void flush()
        {
        }

        public void close()
        {
        }
    }

    private static Comparator s_field_lex_comparator = new Comparator()
        {
            public int compare( Object obj1, Object obj2 )
            {
                if ( obj1 == obj2 )
                {
                    return 0;
                }
                String fld1 = ( ( ObjectStreamField ) obj1 ).getName();
                String fld2 = ( ( ObjectStreamField ) obj2 ).getName();
                return fld1.compareTo( fld2 );
            }
        };

    /** A plain class type (ITF.java). */
    public static final int TYPE_PLAIN = 0;
    /** A stub class type (ITFStub.java). */
    public static final int TYPE_STUB = 1;
    /** A helper class type (ITFHelper.java). */
    public static final int TYPE_HELPER = 2;
    /** A holder class type (ITFHolder.java). */
    public static final int TYPE_HOLDER = 3;
    /** A default factory type (???). */
    public static final int TYPE_DEFAULT_FACTORY = 4;
    /** A operations class type (ITFOperations.java). */
    public static final int TYPE_OPERATIONS = 255;
    /** An implbase class type (ITFImplBase.java (BOA)). */
    public static final int TYPE_IMPLBASE = 256;
    /** A POA class type (ITFPOA.java). */
    public static final int TYPE_POA = 257;
    /** A POA tie class type (ITFPOATie.java). */
    public static final int TYPE_POATIE = 258;
    /** A tie class type (_ITFTie.java). */
    public static final int TYPE_TIE = 259;

    /**
     * This methods decorates a class name with the suffixes
     * depending on the type of the class about to be created.
     *
     * @param name The class name to be decorated.
     * @param type The type of the class about to be created.
     * @return The decorated name.
     */
    public static String decorate( String name, int type )
    {
        StringBuffer ret = new StringBuffer();
        switch ( type )
        {
            case TYPE_STUB:
            case TYPE_IMPLBASE:
                ret.append( '_' );
                break;

            default:
                // append nothing
                break;
        }
        ret.append( name );
        switch ( type )
        {
            case TYPE_PLAIN:
                break;

            case TYPE_STUB:
                ret.append( "Stub" );
                break;

            case TYPE_HELPER:
                ret.append( "Helper" );
                break;

            case TYPE_HOLDER:
                ret.append( "Holder" );
                break;

            case TYPE_DEFAULT_FACTORY:
                ret.append( "DefaultFactory" );
                break;

            case TYPE_OPERATIONS:
                ret.append( "Operations" );
                break;

            case TYPE_IMPLBASE:
                ret.append( "ImplBase" );
                break;

            case TYPE_POA:
                ret.append( "POA" );
                break;

            case TYPE_POATIE:
                ret.append( "POATie" );
                break;

            case TYPE_TIE:
                ret.append( "Tie" );
                break;

            default:
                throw new IllegalArgumentException();
        }
        return ret.toString();
    }

    /**
     * This methods checks whether the passed repository identifier is valid
     * or not.
     *
     * @param id The repository id to check.
     * @return True if id is a valid repository identifier, false otherwise.
     */
    public static boolean checkID( String id )
    {
        // TODO: support "RMI:"
        if ( id.startsWith( "IDL:" ) )
        {
            int dot = id.indexOf( '.' );
            int lcl = id.lastIndexOf( ':' );
            int fsla = id.indexOf( '/' );
            int lsla = id.lastIndexOf( '/', lcl );
            int nsla;
            if ( dot < lcl )
            {
                int prev = fsla;
                while ( ( dot = id.lastIndexOf( '.', prev ) ) >= 0 )
                {
                    if ( !checkIdentifier( id.substring( dot + 1, prev ) ) )
                    {
                        return false;
                    }
                    prev = dot - 1;
                }
                if ( !checkIdentifier( id.substring( 4, prev + 1 ) ) )
                {
                    return false;
                }
                while ( fsla != lsla )
                {
                    nsla = id.indexOf( '/', fsla + 1 );
                    if ( !checkIdentifier( id.substring( fsla + 1, nsla ) ) )
                    {
                        return false;
                    }
                    fsla = nsla;
                }
            }
            else if ( lsla >= 0 )
            {
                fsla = 3;
                while ( fsla != lsla )
                {
                    nsla = id.indexOf( '/', fsla + 1 );
                    if ( !checkIdentifier( id.substring( fsla + 1, nsla ) ) )
                    {
                        return false;
                    }
                    fsla = nsla;
                }
            }
            else
            {
                lsla = 3;
            }
            if ( !checkIdentifier( id.substring( lsla + 1, lcl ) ) )
            {
                return false;
            }
            return true;
        }
        else if ( id.indexOf( ':' ) > 0 )
        {
            return true;
        }
        return false;
    }

    /**
     * This methods checks an indetifier for validity in the
     * target language. The identifier must match the following pattern
     * <code>([a-z][A-Z])+([a-z][A-Z_][0-9])*</code>.
     *
     * @param name The identifier to check.
     * @return True if ??? false otherwise.
     */
    public static boolean checkIdentifier( String name )
    {
        if ( name.length() == 0 )
        {
            return false;
        }
        char c = name.charAt( 0 );
        if ( !( ( c >= 'a' && c <= 'z' ) || ( c >= 'A' && c <= 'Z' ) ) )
        {
            return false;
        }
        for ( int i = 1; i < name.length(); ++i )
        {
            c = name.charAt( i );
            if ( ( c >= 'a' && c <= 'z' ) || ( c >= 'A' && c <= 'Z' )
                    || c == '0' || ( c >= '1' && c <= '9' ) || c == '_' )
            {
                continue;
            }
            return false;
        }
        return true;
    }


    /**
     * Extract the Java package name from the IDL repository identifier.
     *
     * @param name The repository id to extract the package name from.
     * @return The package information converted from the repository id.
     */
    public static String idToPackage( String name )
    {
        if ( name.startsWith( "IDL:" ) )
        {
            StringBuffer ret = new StringBuffer();
            int dot = name.indexOf( '.' );
            int lcl = name.lastIndexOf( ':' );
            int fsla = name.indexOf( '/' );
            int lsla = name.lastIndexOf( '/', lcl );
            if ( dot < lcl )
            {
                int prev = fsla;
                while ( ( dot = name.lastIndexOf( '.', prev ) ) >= 0 )
                {
                    ret.append( name.substring( dot + 1, prev ) ).append( '.' );
                    prev = dot - 1;
                }
                ret.append( name.substring( 4, prev + 1 ) );
                if ( fsla != lsla )
                {
                    ret.append( '.' ).append(
                        name.substring( fsla + 1, lsla ).replace( '/', '.' ) );
                }
            }
            else if ( lsla >= 0 )
            {
                ret.append( name.substring( 4, lsla ).replace( '/', '.' ) );
            }
            return ret.toString();
        }
        else if ( name.startsWith( "RMI:" ) )
        {
            // TODO: fillin stuff here to extract classname
            // from RMI repositoy ID
            throw new IllegalArgumentException();
        }
        throw new IllegalArgumentException();
    }

    /**
     * Convert the repository id to a class name.
     *
     * @param name The repository id to extract the class name from.
     * @return The name of the class extracted from the repository id.
     */
    public static String idToClassname( String name )
    {
        if ( name.startsWith( "IDL:" ) )
        {
            int end = name.lastIndexOf( ':' );
            int beg;
            if ( ( beg = name.lastIndexOf( '/', end ) ) == -1 )
            {
                beg = 3;
            }
            return name.substring( beg + 1, end );
        }
        else if ( name.startsWith( "RMI:" ) )
        {
            // TODO: fillin stuff here to extract classname
            // from RMI repositoy ID
            throw new IllegalArgumentException();
        }
        throw new IllegalArgumentException();
    }

    /**
     * This method tries to extract the class name from the repository id.
     *
     * @param name The repository id name.
     * @return The Java class name for the repository id.
     */
    public static String idToClass( String name )
    {
        return idToClass( name, 0 );
    }

    /**
     * This method tries to extract the class name from the repository id.
     *
     * @param name The repository id name.
     * @param type The type of the class to create.
     * @return The Java class name for the repository id.
     */
    public static String idToClass( String name, int type )
    {
        if ( name.startsWith( "IDL:" ) )
        {
            StringBuffer ret = new StringBuffer();
            int dot = name.indexOf( '.' );
            int lcl = name.lastIndexOf( ':' );
            int fsla = name.indexOf( '/' );
            int lsla = name.lastIndexOf( '/', lcl );
            if ( dot < lcl )
            {
                int prev = fsla - 1;
                while ( ( dot = name.lastIndexOf( '.', prev ) ) >= 0 )
                {
                    ret.append( name.substring(
                            dot + 1, prev + 1 ) ).append( '.' );
                    prev = dot - 1;
                }
                ret.append( name.substring( 4, prev + 1 ) ).append( '.' );
                if ( fsla != lsla )
                {
                    ret.append( name.substring( fsla + 1, lsla ).replace(
                        '/', '.' ) ).append( '.' );
                }
            }
            else if ( lsla >= 0 )
            {
                ret.append( name.substring( 4, lsla ).replace(
                    '/', '.' ) ).append( '.' );
            }
            else
            {
                lsla = 3;
            }
            ret.append( decorate( name.substring( lsla + 1, lcl ), type ) );
            return ret.toString();
        }
        else if ( name.startsWith( "RMI:" ) )
        {
            // TODO: fillin stuff here to extract classname from RMI
            // repositoy ID return empty string for types not supported
            //  (i.e. operations interface)
            throw new IllegalArgumentException();
        }
        throw new IllegalArgumentException();
    }

    /**
     * This returns an object which can be used to test if some other
     * repository ID is an instance of this one. <p>Examples:<p/>
     * <pre>
     * RepoIDHelper.createIsATest( "IDL:MyObj:1.0" ).equals(
     *         "IDL:MyObj:1.0" ) == true
     * RepoIDHelper.createIsATest( "IDL:MyObj:1.1" ).equals(
     *         "IDL:MyObj:1.0" ) == true
     * RepoIDHelper.createIsATest( "IDL:MyObj:1.0" ).equals(
     *         "IDL:MyObj:1.1" ) == false
     * RepoIDHelper.createIsATest( "IDL:MyObj:2.0" ).equals(
     *         "IDL:MyObj:1.1" ) == false
     * </pre>
     *
     * @param id The repository id to test.
     * @return The test object or the id if the parsing
     * failed.
     */
    public static Object createIsATest( String id )
    {
        try
        {
            if ( id.startsWith( "IDL:" ) )
            {
                return new IsA( id );
            }
        }
        catch ( NumberFormatException ex )
        {
            // we return the id that was passed
        }
        return id;
    }

    private static class IsA
    {
        private String m_me;
        private String m_nomi;
        private int m_minor;

        public IsA( String me )
             throws NumberFormatException
        {
            m_me = me;
            int idx = m_me.lastIndexOf( '.' );
            m_nomi = m_me.substring( 0, idx + 1 );
            m_minor = Integer.parseInt( m_me.substring( idx + 1 ) );
        }

        public String toString()
        {
            return m_me;
        }

        public boolean equals( Object obj )
        {
            if ( obj == null )
            {
                return false;
            }
            if ( this == obj )
            {
                return true;
            }
            String str = obj.toString();
            if ( str.equals( m_me ) )
            {
                return true;
            }
            if ( !str.startsWith( m_nomi ) )
            {
                return false;
            }
            int minor;
            try
            {
                minor = Integer.parseInt( str.substring( m_nomi.length() ) );
            }
            catch ( NumberFormatException ex )
            {
                return false;
            }
            return m_minor <= minor;
        }

        public int hashCode()
        {
            return m_nomi.hashCode();
        }
    }

    /**
     * Mangle the java classname, replacing non latin-1 chars with unicode
     * escapes. This returns a two element array, the first is the mangled
     * class name, the second is the short name. These are used in repository
     * IDs.
     *
     * @param clz The class name to mangle.
     * @return The two element string array.
     */
    public static String [] mangleClassName( Class clz )
    {
        if ( clz.isArray() )
        {
            String [] names = new String[] { clz.getName(), null };
            int l = 1;
            Class base = clz.getComponentType();
            while ( base.isArray() )
            {
                base = base.getComponentType();
                l++;
            }
            if ( base.isPrimitive() )
            {
                if ( base.equals( boolean.class ) )
                {
                    names[ 1 ] = "seq" + l + "_boolean";
                }
                else if ( base.equals( byte.class ) )
                {
                    names[ 1 ] = "seq" + l + "_octet";
                }
                else if ( base.equals( short.class ) )
                {
                    names[ 1 ] = "seq" + l + "_short";
                }
                else if ( base.equals( int.class ) )
                {
                    names[ 1 ] = "seq" + l + "_long";
                }
                else if ( base.equals( long.class ) )
                {
                    names[ 1 ] = "seq" + l + "_long_long";
                }
                else if ( base.equals( float.class ) )
                {
                    names[ 1 ] = "seq" + l + "_float";
                }
                else if ( base.equals( double.class ) )
                {
                    names[ 1 ] = "seq" + l + "_double";
                }
                else if ( base.equals( char.class ) )
                {
                    names[ 1 ] = "seq" + l + "_wchar";
                }
                else
                {
                    throw new Error( "Invalid primitive type" );
                }
            }
            else
            {
                String [] cnames = extractClassName( base );
                names[ 1 ] = "seq" + l + "_" + cnames[ 1 ];
            }
            return names;
        }
        else
        {
            String [] cnames = extractClassName( clz );
            if ( cnames[ 0 ].length() == 0 )
            {
                return new String [] { cnames[ 1 ], cnames[ 1 ] };
            }
            else
            {
                return new String [] { cnames[ 0 ] + '.' + cnames[ 1 ], cnames[ 1 ] };
            }
        }
    }

    /**
     * Unmangle the java classname, replacing the escapes with unicode chars.
     *
     * @param className The class name to unmangle.
     * @return the unmangled class name.
     */
    public static String unmangleClassName( String className )
    {
        // TODO: Implement this !!!
        return className;
    }

    /**
     * Mangle the java class name into the package and class name parts.
     *
     * @param clz The fully qualified class name.
     * @return A 2 element string array, first element is the package name,
     * second element is the class name.
     */
    public static String [] extractClassName( Class clz )
    {
        if ( clz.isArray() || clz.isPrimitive() )
        {
            throw new IllegalArgumentException( "Class is primitive or array type" );
        }
        Package pkg = clz.getPackage();
        String [] ret;
        if ( pkg != null )
        {
            ret = new String [] { pkg.getName(), null };
            ret[ 1 ] = clz.getName().substring( ret[ 0 ].length() + 1 );
        }
        else
        {
            String clzName = clz.getName();
            // WORKAROUND: BUG #501292
            // The ClassLoader sometimes does not load the package information
            // of a new class correctly so that the getPackage() call returned
            // null. This has been observed when a custom ClassLoader has not
            // been derived from URLClassLoader and thus does not load the
            // internal package map correctly.
            if ( clzName != null && clzName.lastIndexOf( '.' ) != -1 )
            {
                ret = new String [] { null, null };
                ret[ 0 ] = clzName.substring( 0, clzName.lastIndexOf( '.' ) );
                ret[ 1 ] = clzName.substring( ret[ 0 ].length() + 1 );
            }
            else
            {
                ret = new String [] { "", clzName };
            }
        }
        return ret;
    }

    /**
     * Mangle the member names. The array elements will be replaced with the
     * mangled names.
     *
     * @param clz The class name to check for member name collisions.
     * @param memberNames The members to mangle.
     */
    public static void mangleMemberNames( Class clz, String[] memberNames )
    {
        String [] cln = extractClassName( clz );
        for ( int i = 0; i < memberNames.length; ++i )
        {
            if ( memberNames[ i ].startsWith( "_" ) )
            {
                memberNames[ i ] = "J" + memberNames[ i ];
            }
            if ( memberNames[ i ].equals( cln[ 1 ] ) )
            {
                memberNames[ i ] = memberNames[ i ] + "_";
            }
        }
    }

    /**
     * Unmangle the repo ID into the class name.
     *
     * @param repoID The repository id to unmangle.
     * @return the classname, or null for failure.
     */
    public static String unmangleRepoIDtoClassName( String repoID )
    {
        if ( !repoID.startsWith( "RMI:" ) )
        {
            return null;
        }
        int idx = repoID.indexOf( ':', 5 );
        if ( idx < 0 )
        {
            return null;
        }
        return unmangleClassName( repoID.substring( 4, idx ) );
    }

    /**
     * The syntax of the repository ID is the standard OMG RMI Hashed format (10.6.2 RMI
     * Hashed Format) with an initial "RMI:" followed by the Java class name, followed by
     * a hash code string, followed optionally by a serialization Version UID string.
     */
    public static String getRepoID( Class clz )
    {
        String result = null;
        if ( clz != null )
        {
            String [] names = mangleClassName( clz );
            if ( clz.isArray() )
            {
                Class cmpt = clz.getComponentType();
                result = "RMI:" + names[ 0 ] + ":";
                if ( ReflectionUtils.isAssignableFrom( "org.omg.CORBA.portable.ValueBase", cmpt ) )
                {
                    long repo_id_hash = calculateStructuralHash( cmpt );
                    String hash = Long.toHexString( repo_id_hash ).toUpperCase();
                    result += "0000000000000000".substring( hash.length() ) + hash;
                    ObjectStreamClass wubble = ObjectStreamClass.lookup( cmpt );
                    hash = Long.toHexString( wubble.getSerialVersionUID() ).toUpperCase();
                    result += ":" + "0000000000000000".substring( hash.length() ) + hash;
                }
                else if ( java.io.Serializable.class.isAssignableFrom( cmpt )
                      && !( java.rmi.Remote.class.isAssignableFrom( cmpt )
                      || ReflectionUtils.isAssignableFrom( "org.omg.CORBA.Object", cmpt )
                      || ( ReflectionUtils.isAssignableFrom( "org.omg.CORBA.portable.IDLEntity",
                            cmpt )
                      && cmpt.isInterface() ) ) )
                {
                    String repo_id = getRepoID( cmpt );
                    result +=
                            repo_id.substring( repo_id.indexOf( ':', 5 ) + 1 );
                }
                else
                {
                    result += "0000000000000000";
                }
            }
            else
            {
                if ( !java.io.Serializable.class.isAssignableFrom( clz )
                      || ReflectionUtils.isAssignableFrom( "org.omg.CORBA.Object", clz )
                      || ReflectionUtils.isAssignableFrom( "org.omg.CORBA.portable.ValueBase", clz )
                      || java.rmi.Remote.class.isAssignableFrom( clz ) )
                {
                    result = "RMI:" + names[ 0 ] + ":0000000000000000";
                }
                else
                {
                    String hash = Long.toHexString( getRepoIDHash( clz ) ).toUpperCase();
                    hash = "0000000000000000".substring( hash.length() ) + hash;
                    result = "RMI:" + names[ 0 ] + ":" + hash;
                    if ( !java.io.Externalizable.class.isAssignableFrom( clz ) )
                    {
                        ObjectStreamClass wubble = ObjectStreamClass.lookup( clz );
                        if ( wubble != null )
                        {
                            hash = Long.toHexString( wubble.getSerialVersionUID() ).toUpperCase();
                            result += ":" + "0000000000000000".substring( hash.length() ) + hash;
                        }
                        else
                        {
                            result = "RMI:" + names[ 0 ] + ":0000000000000000";
                        }
                    }
                }
            }
            result = result.intern();
        }
        else
        {
            Thread.dumpStack();
        }
        return result;
    }

    /**
     * The syntax of the repository ID is the standard OMG RMI Hashed format (10.6.2 RMI
     * Hashed Format) with an initial "RMI:" followed by the Java class name, followed by
     * a hash code string, followed optionally by a serialization Version UID string.
     */
    public static long getRepoIDHash( Class clz )
    {
        long result = 0L;
        if ( clz != null )
        {
            if ( clz.isArray() )
            {
                Class cmpt = clz.getComponentType();
                if ( ReflectionUtils.isAssignableFrom( "org.omg.CORBA.portable.ValueBase", cmpt ) )
                {
                    result = calculateStructuralHash( cmpt );
                }
                else if ( java.io.Serializable.class.isAssignableFrom( cmpt )
                      && !( java.rmi.Remote.class.isAssignableFrom( cmpt )
                      || ReflectionUtils.isAssignableFrom( "org.omg.CORBA.Object", cmpt )
                      || ( ReflectionUtils.isAssignableFrom( "org.omg.CORBA.portable.IDLEntity",
                            cmpt )
                      && cmpt.isInterface() ) ) )
                {
                    result = getRepoIDHash( cmpt );
                }
            }
            else
            {
                if ( java.io.Serializable.class.isAssignableFrom( clz )
                      && !ReflectionUtils.isAssignableFrom( "org.omg.CORBA.Object", clz )
                      && !ReflectionUtils.isAssignableFrom( "org.omg.CORBA.portable.ValueBase",
                      clz )
                      && !java.rmi.Remote.class.isAssignableFrom( clz ) )
                {
                    result = calculateStructuralHash( clz );
                }
            }
        }
        else
        {
            Thread.dumpStack();
        }
        return result;
    }

    private static boolean isCustom( Class clz )
    {
        boolean result = false;
        if ( clz != null )
        {
            if ( !ReflectionUtils.isAssignableFrom( "org.omg.CORBA.portable.IDLEntity", clz ) )
            {
                if ( java.io.Externalizable.class.isAssignableFrom( clz ) )
                {
                    result = true;
                }
                else
                {
                    if ( !clz.isArray() )
                    {
                        result = ReflectionUtils.hasWriteObjectMethod( clz );
                        if ( !result )
                        {
                            Class superclz = clz.getSuperclass();
                            if ( superclz != null )
                            {
                                result = isCustom( superclz );
                            }
                        }
                    }
                }
            }
        }
        else
        {
            Thread.dumpStack();
        }
        return result;
    }

    /**
     * Calculate the structural hash code as described in 10.6.2 RMI Hashed Format.
     *
     * @param clz The class to calculate the structural hash code for.
     * @return The structural hash code.
     */
    public static long calculateStructuralHash( Class clz )
    {
        if ( java.io.Externalizable.class.isAssignableFrom( clz ) )
        {
            return 1;
        }
        if ( !java.io.Serializable.class.isAssignableFrom( clz ) )
        {
            return 0;
        }
        ObjectStreamClass osc = ObjectStreamClass.lookup( clz );
        if ( osc == null )
        {
            return 0;
        }
        try
        {
            // Calculate the structural hash code.
            java.security.MessageDigest digest =
                java.security.MessageDigest.getInstance( "SHA" );
            java.io.DataOutputStream dos = new java.io.DataOutputStream(
                    new java.security.DigestOutputStream( s_dummy_output_stream, digest ) );
            // 1. parent hash
            Class parent = clz.getSuperclass();
            if ( parent != null && java.io.Serializable.class.isAssignableFrom( parent ) )
            {
                dos.writeLong( calculateStructuralHash( parent ) );
            }
            else
            {
                dos.writeLong( 0L );
            }
            // 2. writeObject present
            dos.writeInt( isCustom( clz ) ? 2 : 1 );

            // 3. fields. The order writing the fields here is different
            // to the order that they are written to the stream.
            ObjectStreamField [] sfields = osc.getFields();
            Arrays.sort( sfields, s_field_lex_comparator );
            for ( int i = 0; i < sfields.length; ++i )
            {
                // 3a. field name
                dos.writeUTF( sfields[ i ].getName() );
                // 3b. field descriptor. Is this the right thing??
                if ( sfields[ i ].isPrimitive() )
                {
                    dos.writeUTF( "" + sfields[ i ].getTypeCode() );
                }
                else
                {
                    dos.writeUTF( sfields[ i ].getTypeString() );
                }
            }
            dos.close();
            byte [] sha = digest.digest();
            long repoIDHash = 0;
            for ( int i = 0; i < ( ( 8 < sha.length ) ? 8 : sha.length ); i++ )
            {
                repoIDHash += ( long ) ( sha[ i ] & 255 ) << ( i * 8 );
            }
            return repoIDHash;
        }
        catch ( java.security.NoSuchAlgorithmException ex )
        {
            return 0xABADF00DABADF00DL;
        }
        catch ( IOException ex )
        {
            throw new InternalError( "Unexpected IOException (" + ex + ")" );
        }
    }
}

