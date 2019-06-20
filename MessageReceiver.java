
/**
 * This class implements the receiver side of the data link layer.
 */

public class MessageReceiver {
    // Fields ----------------------------------------------------------

    private int mtu;                      // maximum transfer unit (frame length limit)
    private FrameReceiver physicalLayer;  // physical layer object
    private TerminalStream terminal;      // terminal stream manager

 
    // Constructor -----------------------------------------------------
    /**
     * MessageReceiver constructor - Create and
     * initialize new MessageReceiver.
     *
     * @param mtu the maximum transfer unit (MTU) (the length of a frame must
     * not exceed the MTU)
     * @throws ProtocolException if error detected
     */
    public MessageReceiver(int mtu) throws ProtocolException {
        // Initialize fields
        // Create physical layer and terminal stream manager

        this.mtu = mtu;
        this.physicalLayer = new FrameReceiver();
        this.terminal = new TerminalStream("MessageReceiver");
        terminal.printlnDiag("data link layer ready (mtu = " + mtu + ")");
    }

    // Methods ---------------------------------------------------------
    /**
     * Receive a single message 
     *
     * @return the message received, or null if the end of the input stream has
     * been reached. See receiveFrame documentation for further explanation of
     * how the end of the input stream is detected and handled.
     * @throws ProtocolException immediately without attempting to receive any
     * further frames if any error is detected, such as a corrupt frame, even if
     * the end of the input stream has also been reached (i.e. signalling an
     * error takes precedence over signalling the end of the input stream)
     */
    public String receiveMessage() throws ProtocolException, Exception {
        String message = "";    // whole of message as a single string
        // initialise to empty string

        // Report action to terminal
        // the terminal messages aren't part of the protocol,
        // they're just included to help with testing and debugging
        terminal.printlnDiag("  receiveMessage starting");

 
        String frame = "";
        
        //message flags
        boolean messageStarted = false; 
        boolean longMessage = true;
        
        //colon flags
        boolean firstColon = false;
        boolean secondColon = false;
        
        //Checksum and frame indicator .,+
        String CheckSum = "";
        char shortORLong;
        
        String shortMessage = "";
        
        while (longMessage == true) {  //While the message is long
            frame = physicalLayer.receiveFrame(); //call for a frame 
            
            messageStarted = false;
            firstColon = false;
            secondColon = false;
            CheckSum = "";
            shortORLong = ' ';
            longMessage = true;
            
            //frame input is over / long message is finished. 
            if ("(.)".equals(frame)) { 
                message = null;
                longMessage = false; }
            
            // If the length exceeds the mtu throw an error
            else if (frame.length() > this.mtu) {
                throw new Exception("frame exceeded mtu limitation");  } 
            
            
            else {
                shortMessage = "";
                
                for (int i = 0; i < frame.length(); i++) {
                    
                    if (secondColon == true) {
                        shortORLong = frame.charAt(i);
                        
                        if (shortORLong == '.') {
                            longMessage = false;
                        } else if (shortORLong == '+') {
                            longMessage = true;

                        } else {
                            longMessage = false;
                        }

                        secondColon = false;
                        break; 
                    }
                    
                    if (frame.charAt(i) == ':' && firstColon == true) {
                        messageStarted = false;
                        secondColon = true; }
                        
                    if (firstColon == true && secondColon == false) {
                        CheckSum += frame.charAt(i);}
                        
                    if (frame.charAt(i) == ':' && firstColon == false) {
                        messageStarted = false;
                        firstColon = true; }

                    if (messageStarted == true) {
                        shortMessage += frame.charAt(i); }

                    if (frame.charAt(i) == '(') {
                        messageStarted = true; }

                }
                
                message += shortMessage;
                String calculatedCheckSum = generateChecksum(shortMessage) + ""; //calculate the checksum 
                 
                //Padd checksum with two zeros
                if (calculatedCheckSum.length() == 1) {
                    calculatedCheckSum = "00" + calculatedCheckSum; } 
                //Padd checksum with one zero
                else if (calculatedCheckSum.length() == 2) {
                    calculatedCheckSum = "0" + calculatedCheckSum;
                } 
                //Checksum does not need padding
                else if (calculatedCheckSum.length() == 3) {
                    calculatedCheckSum = calculatedCheckSum; } 
                
                else {
                    //calculate the new checksum
                    String newSum = "";
                    int length = calculatedCheckSum.length();
                    for (int k = length - 3; k < length; k++) {
                        newSum += calculatedCheckSum.charAt(k);
                    }
                    calculatedCheckSum = newSum;
                }
                
                //Check to see if the checksum is correct 
                if (!calculatedCheckSum.equals(CheckSum) ) {

                    throw new Exception("Checksum calculated differs from that recorded");
                }
            }
           }


        
 
        // Return message
        if (message == null) {
            terminal.printlnDiag("  receiveMessage returning null (end of input stream)");
        } else {
            terminal.printlnDiag("  receiveMessage returning \"" + message + "\"");
        }
        return message;

        } // end of method receiveMessage
    


     /**
     * Calculate the checksum value used to validate a frame.
     */
      private int generateChecksum(String message) {
        int count=0;
        int Checksum=0;
        for(int i = 0; i < message.length() ; i++) { //Loop through the message and calculate the checksum
                count = message.charAt(i); 
                Checksum = Checksum + count; 
                }
        return Checksum; //return the calculated checksum
     }

    

} // end of class MessageReceiver

