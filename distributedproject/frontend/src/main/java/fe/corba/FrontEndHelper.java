package fe.corba;


/**
* fe._FEPackage/FrontEndHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from FrontEnd.idl
* Friday, November 24, 2017 11:04:17 o'clock AM EST
*/

abstract public class FrontEndHelper
{
  private static String  _id = "IDL:FEPackage/FrontEnd:1.0";

  public static void insert (org.omg.CORBA.Any a, FrontEnd that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static FrontEnd extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (FrontEndHelper.id (), "FrontEnd");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static FrontEnd read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_FrontEndStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, FrontEnd value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static FrontEnd narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof FrontEnd)
      return (FrontEnd)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      _FrontEndStub stub = new _FrontEndStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static FrontEnd unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof FrontEnd)
      return (FrontEnd)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      _FrontEndStub stub = new _FrontEndStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}