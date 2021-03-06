package fe.corba;


/**
* _FEPackage/FrontEndPOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from FrontEnd.idl
* Tuesday, November 28, 2017 2:55:11 o'clock PM EST
*/

public abstract class FrontEndPOA extends org.omg.PortableServer.Servant
 implements fe.corba.FrontEndOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("deposit", new java.lang.Integer (0));
    _methods.put ("withdraw", new java.lang.Integer (1));
    _methods.put ("getBalance", new java.lang.Integer (2));
    _methods.put ("transferFund", new java.lang.Integer (3));
    _methods.put ("createAccountRecord", new java.lang.Integer (4));
    _methods.put ("editRecord", new java.lang.Integer (5));
    _methods.put ("getAccountCount", new java.lang.Integer (6));
    _methods.put ("transferFundManager", new java.lang.Integer (7));
    _methods.put ("shutdown", new java.lang.Integer (8));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {
       case 0:  // _FEPackage/FrontEnd/deposit
       {
         String customerID = in.read_string ();
         String amount = in.read_string ();
         String $result = null;
         $result = this.deposit (customerID, amount);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 1:  // _FEPackage/FrontEnd/withdraw
       {
         String customerID = in.read_string ();
         String amount = in.read_string ();
         String $result = null;
         $result = this.withdraw (customerID, amount);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 2:  // _FEPackage/FrontEnd/getBalance
       {
         String customerID = in.read_string ();
         String $result = null;
         $result = this.getBalance (customerID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 3:  // _FEPackage/FrontEnd/transferFund
       {
         String sourceCustomerID = in.read_string ();
         String amount = in.read_string ();
         String destinationCustomerID = in.read_string ();
         String $result = null;
         $result = this.transferFund (sourceCustomerID, amount, destinationCustomerID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 4:  // _FEPackage/FrontEnd/createAccountRecord
       {
         String managerID = in.read_string ();
         String firstName = in.read_string ();
         String lastName = in.read_string ();
         String address = in.read_string ();
         String phone = in.read_string ();
         String branch = in.read_string ();
         String $result = null;
         $result = this.createAccountRecord (managerID, firstName, lastName, address, phone, branch);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 5:  // _FEPackage/FrontEnd/editRecord
       {
         String managerID = in.read_string ();
         String customerID = in.read_string ();
         String fieldName = in.read_string ();
         String newValue = in.read_string ();
         String $result = null;
         $result = this.editRecord (managerID, customerID, fieldName, newValue);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 6:  // _FEPackage/FrontEnd/getAccountCount
       {
         String managerID = in.read_string ();
         String $result = null;
         $result = this.getAccountCount (managerID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 7:  // _FEPackage/FrontEnd/transferFundManager
       {
         String managerID = in.read_string ();
         String amount = in.read_string ();
         String sourceCustomerID = in.read_string ();
         String destinationCustomerID = in.read_string ();
         String $result = null;
         $result = this.transferFundManager (managerID, amount, sourceCustomerID, destinationCustomerID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 8:  // _FEPackage/FrontEnd/shutdown
       {
         this.shutdown ();
         out = $rh.createReply();
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:FEPackage/FrontEnd:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public FrontEnd _this() 
  {
    return FrontEndHelper.narrow(
    super._this_object());
  }

  public FrontEnd _this(org.omg.CORBA.ORB orb) 
  {
    return FrontEndHelper.narrow(
    super._this_object(orb));
  }


} // class FrontEndPOA
