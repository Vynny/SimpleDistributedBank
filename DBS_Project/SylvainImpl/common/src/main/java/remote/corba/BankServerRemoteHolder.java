package remote.corba;

/**
* remote/corba/BankServerRemoteHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from BankServer.idl
* Sunday, October 22, 2017 6:52:51 PM EDT
*/

public final class BankServerRemoteHolder implements org.omg.CORBA.portable.Streamable
{
  public remote.corba.BankServerRemote value = null;

  public BankServerRemoteHolder ()
  {
  }

  public BankServerRemoteHolder (remote.corba.BankServerRemote initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = remote.corba.BankServerRemoteHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    remote.corba.BankServerRemoteHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return remote.corba.BankServerRemoteHelper.type ();
  }

}
