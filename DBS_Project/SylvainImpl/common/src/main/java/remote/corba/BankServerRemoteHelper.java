package remote.corba;


/**
* remote/corba/BankServerRemoteHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from BankServer.idl
* Sunday, October 22, 2017 6:52:51 PM EDT
*/

abstract public class BankServerRemoteHelper
{
  private static String  _id = "IDL:remote/corba/BankServerRemote:1.0";

  public static void insert (org.omg.CORBA.Any a, remote.corba.BankServerRemote that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static remote.corba.BankServerRemote extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (remote.corba.BankServerRemoteHelper.id (), "BankServerRemote");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static remote.corba.BankServerRemote read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_BankServerRemoteStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, remote.corba.BankServerRemote value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static remote.corba.BankServerRemote narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof remote.corba.BankServerRemote)
      return (remote.corba.BankServerRemote)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      remote.corba._BankServerRemoteStub stub = new remote.corba._BankServerRemoteStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static remote.corba.BankServerRemote unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof remote.corba.BankServerRemote)
      return (remote.corba.BankServerRemote)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      remote.corba._BankServerRemoteStub stub = new remote.corba._BankServerRemoteStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}